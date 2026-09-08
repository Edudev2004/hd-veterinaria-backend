import {
  UserProfile,
  CreateUserDTO,
  UpdateUserDTO,
  UserOperationResult
} from '../types/user.js';
import { RoleService } from './roleService.js';
import { insforge } from '../config/insforge.js';

// Almacenamiento local de perfiles de usuario
const usersStore = new Map<string, UserProfile>();

function initializeDefaultUsers() {
  if (usersStore.size > 0) return;

  const adminUser: UserProfile = {
    id: 'user-admin-1',
    name: 'Dr. Roberto Carlos',
    email: 'admin@veterinariahd.com',
    roleId: 'role-admin',
    roleName: 'administrador',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const vetUser: UserProfile = {
    id: 'user-vet-1',
    name: 'Dra. María Fernández',
    email: 'maria.vet@veterinariahd.com',
    roleId: 'role-vet',
    roleName: 'veterinario',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  usersStore.set(adminUser.id, adminUser);
  usersStore.set(vetUser.id, vetUser);
}

initializeDefaultUsers();

export class UserService {
  /**
   * Resetea el almacén de usuarios a su estado por defecto (para pruebas unitarias).
   */
  static resetStore(): void {
    usersStore.clear();
    initializeDefaultUsers();
  }

  /**
   * Obtiene la lista completa de usuarios registrados.
   */
  static async getUsers(): Promise<UserOperationResult> {
    try {
      const { data, error } = await (insforge as any).database?.from('user_profiles')?.select('*') || { data: null, error: null };

      if (!error && data && data.length > 0) {
        return { success: true, users: data as UserProfile[] };
      }

      const usersList = Array.from(usersStore.values());
      return { success: true, users: usersList };
    } catch {
      const usersList = Array.from(usersStore.values());
      return { success: true, users: usersList };
    }
  }

  /**
   * Obtiene un usuario por su ID.
   */
  static async getUserById(id: string): Promise<UserOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador de usuario es requerido.' };
    }

    const user = usersStore.get(id);
    if (!user) {
      return { success: false, error: 'Usuario no encontrado.' };
    }

    return { success: true, user };
  }

  /**
   * Registra un nuevo usuario en el sistema.
   * Criterios: Nombre, correo y rol son requeridos; el correo debe ser único.
   */
  static async createUser(dto: CreateUserDTO): Promise<UserOperationResult> {
    if (!dto.name || !dto.name.trim()) {
      return { success: false, error: 'El nombre del usuario es requerido.' };
    }

    if (!dto.email || !dto.email.trim()) {
      return { success: false, error: 'El correo electrónico es requerido.' };
    }

    if (!dto.roleId || !dto.roleId.trim()) {
      return { success: false, error: 'El rol del usuario es requerido.' };
    }

    const normalizedEmail = dto.email.toLowerCase().trim();

    // 1. Validar unicidad del correo electrónico
    const existingUser = Array.from(usersStore.values()).find(
      (u) => u.email.toLowerCase() === normalizedEmail
    );

    if (existingUser) {
      return {
        success: false,
        error: `Ya existe un usuario registrado con el correo "${dto.email}".`
      };
    }

    // 2. Validar existencia del rol
    const roleRes = await RoleService.getRoleById(dto.roleId);
    if (!roleRes.success || !roleRes.role) {
      return {
        success: false,
        error: 'El rol especificado no existe.'
      };
    }

    // 3. Crear perfil de usuario activo
    const newUser: UserProfile = {
      id: `user-${Date.now()}`,
      name: dto.name.trim(),
      email: normalizedEmail,
      roleId: dto.roleId,
      roleName: roleRes.role.name,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    usersStore.set(newUser.id, newUser);

    return { success: true, user: newUser };
  }

  /**
   * Edita los datos de un usuario existente.
   */
  static async updateUser(id: string, dto: UpdateUserDTO): Promise<UserOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador de usuario es requerido.' };
    }

    const user = usersStore.get(id);
    if (!user) {
      return { success: false, error: 'Usuario no encontrado.' };
    }

    if (dto.name && dto.name.trim()) {
      user.name = dto.name.trim();
    }

    if (dto.email && dto.email.trim()) {
      const normalizedEmail = dto.email.toLowerCase().trim();
      const existingUser = Array.from(usersStore.values()).find(
        (u) => u.id !== id && u.email.toLowerCase() === normalizedEmail
      );

      if (existingUser) {
        return {
          success: false,
          error: `Ya existe otro usuario con el correo "${dto.email}".`
        };
      }

      user.email = normalizedEmail;
    }

    if (dto.roleId && dto.roleId.trim()) {
      const roleRes = await RoleService.getRoleById(dto.roleId);
      if (!roleRes.success || !roleRes.role) {
        return { success: false, error: 'El rol especificado no existe.' };
      }
      user.roleId = dto.roleId;
      user.roleName = roleRes.role.name;
    }

    if (dto.isActive !== undefined) {
      user.isActive = dto.isActive;
    }

    user.updatedAt = new Date().toISOString();
    usersStore.set(id, user);

    return { success: true, user };
  }

  /**
   * Desactiva un usuario del sistema (Desactivación LÓGICA sin eliminación física de la base de datos).
   */
  static async deactivateUser(id: string): Promise<UserOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador de usuario es requerido.' };
    }

    const user = usersStore.get(id);
    if (!user) {
      return { success: false, error: 'Usuario no encontrado.' };
    }

    user.isActive = false;
    user.updatedAt = new Date().toISOString();
    usersStore.set(id, user);

    return { success: true, user };
  }

  /**
   * Reactiva la cuenta de un usuario desactivado.
   */
  static async activateUser(id: string): Promise<UserOperationResult> {
    if (!id) {
      return { success: false, error: 'El identificador de usuario es requerido.' };
    }

    const user = usersStore.get(id);
    if (!user) {
      return { success: false, error: 'Usuario no encontrado.' };
    }

    user.isActive = true;
    user.updatedAt = new Date().toISOString();
    usersStore.set(id, user);

    return { success: true, user };
  }
}
