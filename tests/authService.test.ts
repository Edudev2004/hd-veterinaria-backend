import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import { AuthService } from '../src/services/authService.js';
import { insforge } from '../src/config/insforge.js';

describe('AuthService - HU-01: Inicio de Sesión', () => {
  const testEmail = 'veterinario@veterinariahd.com';

  beforeEach(() => {
    AuthService.resetFailedAttempts(testEmail);
  });

  it('debe solicitar correo y contraseña si están vacíos', async () => {
    const res1 = await AuthService.login('', '123456');
    assert.strictEqual(res1.success, false);
    assert.ok(res1.error?.includes('correo electrónico es requerido'));

    const res2 = await AuthService.login(testEmail, '');
    assert.strictEqual(res2.success, false);
    assert.ok(res2.error?.includes('contraseña es requerida'));
  });

  it('debe mostrar mensaje de error cuando las credenciales son incorrectas', async () => {
    // Mock de fallo de autenticación
    insforge.auth.signInWithPassword = async () => ({
      data: null as any,
      error: { message: 'Invalid credentials' } as any
    });

    const res = await AuthService.login(testEmail, 'claveIncorrecta');
    assert.strictEqual(res.success, false);
    assert.strictEqual(res.error, 'Credenciales incorrectas. Verifique su usuario y contraseña.');
    assert.strictEqual(res.remainingAttempts, 4);
    assert.strictEqual(res.isLocked, false);
  });

  it('debe bloquear el acceso tras 5 intentos fallidos consecutivos', async () => {
    insforge.auth.signInWithPassword = async () => ({
      data: null as any,
      error: { message: 'Invalid credentials' } as any
    });

    // Realizar 4 intentos fallidos
    for (let i = 1; i <= 4; i++) {
      const res = await AuthService.login(testEmail, 'claveErronea');
      assert.strictEqual(res.success, false);
      assert.strictEqual(res.remainingAttempts, 5 - i);
      assert.strictEqual(res.isLocked, false);
    }

    // El intento 5to debe bloquear la cuenta
    const res5 = await AuthService.login(testEmail, 'claveErronea');
    assert.strictEqual(res5.success, false);
    assert.strictEqual(res5.isLocked, true);
    assert.ok(res5.error?.includes('Cuenta bloqueada por 5 intentos fallidos'));

    // Intentos posteriores deben denegarse inmediatamente por estar bloqueado
    const res6 = await AuthService.login(testEmail, 'claveCorrecta');
    assert.strictEqual(res6.success, false);
    assert.strictEqual(res6.isLocked, true);
    assert.ok(res6.error?.includes('Cuenta bloqueada por 5 intentos fallidos'));
  });

  it('debe reiniciar el contador de intentos al lograr un inicio de sesión exitoso', async () => {
    // 2 intentos fallidos
    insforge.auth.signInWithPassword = async () => ({
      data: null as any,
      error: { message: 'Invalid credentials' } as any
    });
    await AuthService.login(testEmail, 'error1');
    await AuthService.login(testEmail, 'error2');

    assert.strictEqual(AuthService.getFailedAttempts(testEmail), 2);

    // Intento exitoso
    const mockUser: any = {
      id: 'user_123',
      email: testEmail,
      metadata: null,
      emailVerified: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      profile: null
    };

    insforge.auth.signInWithPassword = async () => ({
      data: { user: mockUser, accessToken: 'jwt_abc' } as any,
      error: null
    });

    const successRes = await AuthService.login(testEmail, 'claveCorrecta123');
    assert.strictEqual(successRes.success, true);
    assert.strictEqual(successRes.data.user.email, testEmail);
    assert.strictEqual(AuthService.getFailedAttempts(testEmail), 0);
  });
});
