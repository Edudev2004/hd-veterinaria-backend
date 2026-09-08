import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import { RoleService } from '../src/services/roleService.js';
import { ModulePermission } from '../src/types/role.js';

describe('RoleService - HU-02: Gestión de Roles y Permisos', () => {
  beforeEach(() => {
    RoleService.resetStore();
  });

  it('debe listar los roles existentes por defecto (administrador y veterinario)', async () => {
    const res = await RoleService.getRoles();
    assert.strictEqual(res.success, true);
    assert.ok(res.roles && res.roles.length >= 2);
    assert.ok(res.roles.some((r) => r.name === 'administrador'));
    assert.ok(res.roles.some((r) => r.name === 'veterinario'));
  });

  it('debe crear un nuevo rol con sus permisos por módulo', async () => {
    const customPermissions: ModulePermission[] = [
      {
        module: 'clientes',
        canCreate: true,
        canRead: true,
        canUpdate: true,
        canDelete: false
      },
      {
        module: 'citas',
        canCreate: true,
        canRead: true,
        canUpdate: true,
        canDelete: false
      }
    ];

    const res = await RoleService.createRole({
      name: 'Recepcionista',
      description: 'Gestión de citas y atención al cliente',
      permissions: customPermissions
    });

    assert.strictEqual(res.success, true);
    assert.ok(res.role);
    assert.strictEqual(res.role.name, 'recepcionista');
    assert.strictEqual(res.role.permissions.length, 2);
    assert.strictEqual(res.role.assignedUsersCount, 0);
  });

  it('no debe permitir crear dos roles con el mismo nombre', async () => {
    const res = await RoleService.createRole({
      name: 'administrador',
      description: 'Intento duplicado'
    });

    assert.strictEqual(res.success, false);
    assert.ok(res.error?.includes('Ya existe un rol con el nombre'));
  });

  it('debe permitir actualizar el nombre, descripción y permisos de un rol', async () => {
    const createRes = await RoleService.createRole({
      name: 'Asistente',
      description: 'Apoyo veterinario'
    });
    assert.ok(createRes.role);
    const roleId = createRes.role.id;

    const newPermissions: ModulePermission[] = [
      {
        module: 'mascotas',
        canCreate: false,
        canRead: true,
        canUpdate: false,
        canDelete: false
      }
    ];

    const updateRes = await RoleService.updateRole(roleId, {
      name: 'Asistente Médico',
      description: 'Apoyo técnico y registros',
      permissions: newPermissions
    });

    assert.strictEqual(updateRes.success, true);
    assert.strictEqual(updateRes.role?.name, 'asistente médico');
    assert.strictEqual(updateRes.role?.description, 'Apoyo técnico y registros');
    assert.strictEqual(updateRes.role?.permissions.length, 1);
  });

  it('NO debe permitir eliminar un rol que tenga usuarios asignados', async () => {
    // El rol administrador viene con assignedUsersCount = 2 por defecto
    const getRes = await RoleService.getRoles();
    const adminRole = getRes.roles?.find((r) => r.name === 'administrador');
    assert.ok(adminRole);

    const deleteRes = await RoleService.deleteRole(adminRole.id);
    assert.strictEqual(deleteRes.success, false);
    assert.ok(deleteRes.error?.includes('No se puede eliminar el rol'));
    assert.ok(deleteRes.error?.includes('usuario(s) asignado(s)'));
  });

  it('debe permitir eliminar un rol cuando NO tiene usuarios asignados', async () => {
    const createRes = await RoleService.createRole({
      name: 'Peluquero',
      description: 'Servicio de estética animal'
    });
    assert.ok(createRes.role);
    const roleId = createRes.role.id;

    const deleteRes = await RoleService.deleteRole(roleId);
    assert.strictEqual(deleteRes.success, true);

    const getByIdRes = await RoleService.getRoleById(roleId);
    assert.strictEqual(getByIdRes.success, false);
    assert.strictEqual(getByIdRes.error, 'Rol no encontrado.');
  });
});
