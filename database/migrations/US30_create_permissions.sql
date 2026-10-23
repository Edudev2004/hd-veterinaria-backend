-- US-30: Catalogo de permisos y relacion con roles
-- PostgreSQL
-- Revisar y aprobar antes de aplicar en Supabase.

BEGIN;

CREATE TABLE IF NOT EXISTS public.permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS public.role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT pk_role_permissions
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES public.roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES public.permissions(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_permission
    ON public.role_permissions(permission_id);

COMMIT;
