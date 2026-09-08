export interface UserProfile {
  id: string;
  name: string;
  email: string;
  roleId: string;
  roleName?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserDTO {
  name: string;
  email: string;
  roleId: string;
  password?: string;
}

export interface UpdateUserDTO {
  name?: string;
  email?: string;
  roleId?: string;
  isActive?: boolean;
}

export interface UserOperationResult {
  success: boolean;
  user?: UserProfile;
  users?: UserProfile[];
  error?: string;
}
