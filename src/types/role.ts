export type ModuleName = 
  | 'usuarios'
  | 'roles'
  | 'clientes'
  | 'mascotas'
  | 'citas'
  | 'historial_medico'
  | 'inventario'
  | 'facturacion';

export interface ModulePermission {
  module: ModuleName;
  canCreate: boolean;
  canRead: boolean;
  canUpdate: boolean;
  canDelete: boolean;
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  permissions: ModulePermission[];
  assignedUsersCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRoleDTO {
  name: string;
  description?: string;
  permissions?: ModulePermission[];
}

export interface UpdateRoleDTO {
  name?: string;
  description?: string;
  permissions?: ModulePermission[];
}

export interface RoleOperationResult {
  success: boolean;
  role?: Role;
  roles?: Role[];
  error?: string;
}
