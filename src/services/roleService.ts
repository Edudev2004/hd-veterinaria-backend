import {
  Role,
  CreateRoleDTO,
  UpdateRoleDTO,
  RoleOperationResult,
  ModulePermission,
  ModuleName
} from '../types/role.js';
import { insforge } from '../config/insforge.js';

// Catálogo base de roles en memoria/DB
const rolesStore = new Map<string, Role>();

// Inicializar roles por defecto para el sistema veterinario
function initializeDefaultRoles() {
  if (rolesStore.size > 0) return;

  const defaultModules: ModuleName[] = [
    'usuarios',
    'roles',
    'clientes',
    'mascotas',
    'citas',
    'historial_medico',
    'inventario',
    'facturacion'
  ];

  const adminPermissions: ModulePermission[] = defaultModules.map((m) => ({
    module: m,
    canCreate: true,
    canRead: true,
    canUpdate: true,
    canDelete: true
  }));

  const vetPermissions: ModulePermission[] = defaultModules.map((m) => ({
    module: m,
    canCreate: m !== 'roles' && m !== 'usuarios',
    canRead: true,
    canUpdate: m !== 'roles',
    canDelete: m === 'historial_medico'
  }));

  const adminRole: Role = {
    id: 'role-admin',
    name: 'administrador',
    description: 'Acceso total al sistema veterinario',
    permissions: adminPermissions,
    assignedUsersCount: 2,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const vetRole: Role = {
    id: 'role-vet',
    name: 'veterinario',
    description: 'Atención médica, recetas e historial clínico',
    permissions: vetPermissions,
    assignedUsersCount: 1,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  rolesStore.set(adminRole.id, adminRole);
  rolesStore.set(vetRole.id, vetRole);
}

initializeDefaultRoles();

export class RoleService {
  /**
   * Resetea el almacenamiento a su estado inicial (útil para pruebas).
   */
  static resetStore(): void {
    rolesStore.clear();
    initializeDefaultRoles();
  }

  /**
   * Obtiene la lista completa de roles registrados.
   */
  static async getRoles(): Promise<RoleOperationResult> {
    try {
      // Intentar obtener desde InsForge si existe la tabla `roles`
      const { data, error } = await (insforge as any).database?.from('roles')?.select('*') || { data: null, error: null };

      if (!error && data && data.length > 0) {
        return { success: true, roles: data as Role[] };
      }

      // Retornar almacenamiento local/cache
      const rolesList = Array.from(rolesStore.values());
      return { success: true, roles: rolesList };
    } catch {
      const rolesList = Array.from(rolesStore.values());
      return { success: true, roles: rolesList };
    }
  }

  /**
   * Obtiene un rol por su identificador único.
   */
  static async getRoleById(id: string): Promise<RoleOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador del rol es requerido.' };
    }

    const role = rolesStore.get(id);
    if (!role) {
      return { success: false, error: 'Rol no encontrado.' };
    }

    return { success: true, role };
  }

  /**
   * Crea un nuevo rol en el sistema.
   */
  static async createRole(dto: CreateRoleDTO): Promise<RoleOperationResult> {
    if (!dto.name || !dto.name.trim()) {
      return { success: false, error: 'El nombre del rol es requerido.' };
    }

    const normalizedName = dto.name.toLowerCase().trim();

    // Validar nombre duplicado
    const existing = Array.from(rolesStore.values()).find(
      (r) => r.name.toLowerCase() === normalizedName
    );

    if (existing) {
      return { success: false, error: `Ya existe un rol con el nombre "${dto.name}".` };
    }

    const newRole: Role = {
      id: `role-${Date.now()}`,
      name: normalizedName,
      description: dto.description || '',
      permissions: dto.permissions || [],
      assignedUsersCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    rolesStore.set(newRole.id, newRole);

    return { success: true, role: newRole };
  }

  /**
   * Actualiza el nombre, descripción y permisos de un rol existente.
   */
  static async updateRole(id: string, dto: UpdateRoleDTO): Promise<RoleOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador del rol es requerido.' };
    }

    const role = rolesStore.get(id);
    if (!role) {
      return { success: false, error: 'Rol no encontrado.' };
    }

    if (dto.name && dto.name.trim()) {
      const normalizedName = dto.name.toLowerCase().trim();
      const existing = Array.from(rolesStore.values()).find(
        (r) => r.id !== id && r.name.toLowerCase() === normalizedName
      );

      if (existing) {
        return { success: false, error: `Ya existe otro rol con el nombre "${dto.name}".` };
      }
      role.name = normalizedName;
    }

    if (dto.description !== undefined) {
      role.description = dto.description;
    }

    if (dto.permissions !== undefined) {
      role.permissions = dto.permissions;
    }

    role.updatedAt = new Date().toISOString();
    rolesStore.set(id, role);

    return { success: true, role };
  }

  /**
   * Elimina un rol del sistema.
   * RESTRICCIÓN: No se puede eliminar un rol si tiene usuarios asignados (assignedUsersCount > 0).
   */
  static async deleteRole(id: string): Promise<RoleOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador del rol es requerido.' };
    }

    const role = rolesStore.get(id);
    if (!role) {
      return { success: false, error: 'Rol no encontrado.' };
    }

    // Criterio de aceptación: No se puede eliminar un rol con usuarios asignados
    if (role.assignedUsersCount > 0) {
      return {
        success: false,
        error: `No se puede eliminar el rol "${role.name}" porque tiene ${role.assignedUsersCount} usuario(s) asignado(s).`
      };
    }

    rolesStore.delete(id);

    return { success: true, role };
  }
}
