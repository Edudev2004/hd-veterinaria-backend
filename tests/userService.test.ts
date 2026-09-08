import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import { UserService } from '../src/services/userService.js';
import { RoleService } from '../src/services/roleService.js';

describe('UserService - HU-03: Gestión de Usuarios del Sistema', () => {
  beforeEach(() => {
    RoleService.resetStore();
    UserService.resetStore();
  });

  it('debe listar los usuarios registrados por defecto', async () => {
    const res = await UserService.getUsers();
    assert.strictEqual(res.success, true);
    assert.ok(res.users && res.users.length >= 2);
    assert.ok(res.users.some((u) => u.email === 'admin@veterinariahd.com'));
  });

  it('debe registrar un usuario nuevo con nombre, correo y rol', async () => {
    const res = await UserService.createUser({
      name: 'Carlos Recepción',
      email: 'carlos.recepcion@veterinariahd.com',
      roleId: 'role-vet'
    });

    assert.strictEqual(res.success, true);
    assert.ok(res.user);
    assert.strictEqual(res.user.name, 'Carlos Recepción');
    assert.strictEqual(res.user.email, 'carlos.recepcion@veterinariahd.com');
    assert.strictEqual(res.user.roleId, 'role-vet');
    assert.strictEqual(res.user.isActive, true);
  });

  it('NO debe permitir registrar dos usuarios con el mismo correo electrónico', async () => {
    const res = await UserService.createUser({
      name: 'Dr. Duplicado',
      email: 'admin@veterinariahd.com', // Correo ya existente
      roleId: 'role-admin'
    });

    assert.strictEqual(res.success, false);
    assert.ok(res.error?.includes('Ya existe un usuario registrado con el correo'));
  });

  it('debe permitir actualizar la información de un usuario', async () => {
    const createRes = await UserService.createUser({
      name: 'Lucía Gómez',
      email: 'lucia.gomez@veterinariahd.com',
      roleId: 'role-vet'
    });

    assert.ok(createRes.user);
    const userId = createRes.user.id;

    const updateRes = await UserService.updateUser(userId, {
      name: 'Dra. Lucía Gómez',
      email: 'lucia.vet@veterinariahd.com'
    });

    assert.strictEqual(updateRes.success, true);
    assert.strictEqual(updateRes.user?.name, 'Dra. Lucía Gómez');
    assert.strictEqual(updateRes.user?.email, 'lucia.vet@veterinariahd.com');
  });

  it('debe desactivar un usuario lógicamente (isActive = false) sin eliminarlo de la base de datos', async () => {
    const createRes = await UserService.createUser({
      name: 'Pedro Ayudante',
      email: 'pedro.ayudante@veterinariahd.com',
      roleId: 'role-vet'
    });

    assert.ok(createRes.user);
    const userId = createRes.user.id;

    // Desactivar usuario
    const deactRes = await UserService.deactivateUser(userId);
    assert.strictEqual(deactRes.success, true);
    assert.strictEqual(deactRes.user?.isActive, false);

    // Verificar que el registro aún existe en la base de datos
    const getRes = await UserService.getUserById(userId);
    assert.strictEqual(getRes.success, true);
    assert.ok(getRes.user);
    assert.strictEqual(getRes.user.id, userId);
    assert.strictEqual(getRes.user.isActive, false);
  });

  it('debe permitir reactivar a un usuario desactivado', async () => {
    const getRes = await UserService.getUsers();
    const user = getRes.users?.[0];
    assert.ok(user);

    await UserService.deactivateUser(user.id);
    const actRes = await UserService.activateUser(user.id);

    assert.strictEqual(actRes.success, true);
    assert.strictEqual(actRes.user?.isActive, true);
  });
});
