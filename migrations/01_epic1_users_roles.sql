-- ============================================================
-- MIGRACIÓN BASE DE DATOS: ÉPICA 1 - Acceso y Gestión de Usuarios
-- Backend: InsForge PostgreSQL
-- ============================================================

-- 1. Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    assigned_users_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Tabla de Permisos por Módulo
CREATE TABLE IF NOT EXISTS role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id VARCHAR(50) REFERENCES roles(id) ON DELETE CASCADE,
    module VARCHAR(50) NOT NULL,
    can_create BOOLEAN DEFAULT FALSE,
    can_read BOOLEAN DEFAULT TRUE,
    can_update BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    CONSTRAINT unique_role_module UNIQUE(role_id, module)
);

-- 3. Tabla de Perfiles de Usuarios del Sistema
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role_id VARCHAR(50) REFERENCES roles(id),
    is_active BOOLEAN DEFAULT TRUE,
    failed_login_attempts INT DEFAULT 0,
    is_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Datos Iniciales: Roles por Defecto
INSERT INTO roles (id, name, description, assigned_users_count)
VALUES 
    ('role-admin', 'administrador', 'Acceso total al sistema veterinario', 1),
    ('role-vet', 'veterinario', 'Atención médica, recetas e historial clínico', 0),
    ('role-recep', 'recepcionista', 'Gestión de clientes, mascotas y citas', 0)
ON CONFLICT (name) DO NOTHING;

-- 5. Datos Iniciales: Permisos por Defecto para Administrador
INSERT INTO role_permissions (role_id, module, can_create, can_read, can_update, can_delete)
VALUES 
    ('role-admin', 'usuarios', true, true, true, true),
    ('role-admin', 'roles', true, true, true, true),
    ('role-admin', 'clientes', true, true, true, true),
    ('role-admin', 'mascotas', true, true, true, true),
    ('role-admin', 'citas', true, true, true, true),
    ('role-admin', 'historial_medico', true, true, true, true),
    ('role-admin', 'inventario', true, true, true, true),
    ('role-admin', 'facturacion', true, true, true, true)
ON CONFLICT ON CONSTRAINT unique_role_module DO NOTHING;

-- 6. Datos Iniciales: Permisos por Defecto para Recepcionista
INSERT INTO role_permissions (role_id, module, can_create, can_read, can_update, can_delete)
VALUES 
    ('role-recep', 'clientes', true, true, true, false),
    ('role-recep', 'mascotas', true, true, true, false),
    ('role-recep', 'citas', true, true, true, true),
    ('role-recep', 'facturacion', true, true, true, false)
ON CONFLICT ON CONSTRAINT unique_role_module DO NOTHING;
