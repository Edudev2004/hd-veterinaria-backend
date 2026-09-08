import { insforge } from '../config/insforge.js';

export interface LoginResult {
  success: boolean;
  data?: any;
  error?: string;
  remainingAttempts?: number;
  isLocked?: boolean;
}

// Registro en memoria de intentos fallidos por correo (para control de seguridad)
const failedAttemptsMap = new Map<string, number>();
const MAX_FAILED_ATTEMPTS = 5;

export class AuthService {
  /**
   * Obtiene el número de intentos fallidos registrados para un correo.
   */
  static getFailedAttempts(email: string): number {
    const normalizedEmail = email.toLowerCase().trim();
    return failedAttemptsMap.get(normalizedEmail) || 0;
  }

  /**
   * Verifica si una cuenta se encuentra bloqueada por intentos fallidos excesivos (>= 5).
   */
  static isAccountLocked(email: string): boolean {
    return this.getFailedAttempts(email) >= MAX_FAILED_ATTEMPTS;
  }

  /**
   * Reinicia el contador de intentos fallidos para un correo.
   */
  static resetFailedAttempts(email: string): void {
    const normalizedEmail = email.toLowerCase().trim();
    failedAttemptsMap.delete(normalizedEmail);
  }

  /**
   * Realiza el inicio de sesión con usuario (email) y contraseña contra InsForge Auth.
   * Maneja el bloqueo de cuenta tras 5 intentos fallidos consecutivos.
   */
  static async login(email: string, password: string): Promise<LoginResult> {
    if (!email || !email.trim()) {
      return {
        success: false,
        error: 'El correo electrónico es requerido.'
      };
    }

    if (!password) {
      return {
        success: false,
        error: 'La contraseña es requerida.'
      };
    }

    const normalizedEmail = email.toLowerCase().trim();

    // 1. Verificar si la cuenta ya se encuentra bloqueada
    if (this.isAccountLocked(normalizedEmail)) {
      return {
        success: false,
        error: 'Cuenta bloqueada por 5 intentos fallidos. Contacte al administrador.',
        isLocked: true,
        remainingAttempts: 0
      };
    }

    try {
      // 2. Intentar autenticación mediante el SDK de InsForge
      const { data, error } = await insforge.auth.signInWithPassword({
        email: normalizedEmail,
        password
      });

      if (error || !data) {
        // Incrementar contador de intentos fallidos
        const currentAttempts = this.getFailedAttempts(normalizedEmail) + 1;
        failedAttemptsMap.set(normalizedEmail, currentAttempts);

        const remaining = Math.max(0, MAX_FAILED_ATTEMPTS - currentAttempts);
        const isNowLocked = currentAttempts >= MAX_FAILED_ATTEMPTS;

        if (isNowLocked) {
          return {
            success: false,
            error: 'Cuenta bloqueada por 5 intentos fallidos. Contacte al administrador.',
            isLocked: true,
            remainingAttempts: 0
          };
        }

        return {
          success: false,
          error: 'Credenciales incorrectas. Verifique su usuario y contraseña.',
          isLocked: false,
          remainingAttempts: remaining
        };
      }

      // 3. Autenticación exitosa: reiniciar contador de intentos
      this.resetFailedAttempts(normalizedEmail);

      return {
        success: true,
        data
      };
    } catch (err: any) {
      // Manejo de errores imprevistos o de red
      const currentAttempts = this.getFailedAttempts(normalizedEmail) + 1;
      failedAttemptsMap.set(normalizedEmail, currentAttempts);
      const remaining = Math.max(0, MAX_FAILED_ATTEMPTS - currentAttempts);
      const isNowLocked = currentAttempts >= MAX_FAILED_ATTEMPTS;

      if (isNowLocked) {
        return {
          success: false,
          error: 'Cuenta bloqueada por 5 intentos fallidos. Contacte al administrador.',
          isLocked: true,
          remainingAttempts: 0
        };
      }

      return {
        success: false,
        error: err?.message || 'Credenciales incorrectas. Verifique su usuario y contraseña.',
        isLocked: false,
        remainingAttempts: remaining
      };
    }
  }
}
