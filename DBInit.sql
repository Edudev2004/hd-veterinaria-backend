create extension if not exists pgcrypto;

-- 1. ROLES & USERS
create table roles (
    id          uuid primary key default gen_random_uuid(),
    name        varchar(50) not null unique,
    description text,
    created_at  timestamptz not null default now()
);

create table users (
    id            uuid primary key default gen_random_uuid(),
    dni           varchar(20) not null unique,
    first_name    varchar(100) not null,
    last_name     varchar(100) not null,
    email         varchar(150) not null unique,
    password_hash text not null,
    role_id       uuid not null references roles(id),
    active        boolean not null default true,
    created_at    timestamptz not null default now()
);

create index idx_users_email on users(email);
create index idx_users_dni on users(dni);
create index idx_users_role on users(role_id);

-- 2. ROLE PROFILES
create table owners (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid not null unique references users(id) on delete cascade,
    phone      varchar(20),
    address    varchar(200)
);

create table specialties (
    id          uuid primary key default gen_random_uuid(),
    name        varchar(100) not null unique,
    description text,
    active      boolean not null default true
);

create table veterinarians (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid not null unique references users(id) on delete cascade,
    active     boolean not null default true
);

-- N:M Relationship between Veterinarians and Specialties
create table veterinarian_specialties (
    veterinarian_id uuid not null references veterinarians(id) on delete cascade,
    specialty_id    uuid not null references specialties(id) on delete cascade,
    primary key (veterinarian_id, specialty_id)
);

-- Base availability schedules
create table availability_schedules (
    id              uuid primary key default gen_random_uuid(),
    veterinarian_id uuid not null references veterinarians(id) on delete cascade,
    day_of_week     smallint not null check (day_of_week between 0 and 6), -- 0: Sunday, 6: Saturday
    start_time      time not null,
    end_time        time not null,
    check (end_time > start_time)
);

create index idx_schedules_veterinarian on availability_schedules(veterinarian_id);

-- 3. PETS
create table pets (
    id            uuid primary key default gen_random_uuid(),
    owner_id      uuid not null references owners(id) on delete cascade,
    name          varchar(100) not null,
    species       varchar(50) not null,
    breed         varchar(50),
    gender        varchar(10) not null check (gender in ('MALE', 'FEMALE')),
    birth_date    date,
    weight        numeric(5,2),
    observations  text,
    photo_url     text,
    active        boolean not null default true,
    created_at    timestamptz not null default now()
);

create index idx_pets_owner on pets(owner_id);

-- 4. APPOINTMENTS
create table appointments (
    id                  uuid primary key default gen_random_uuid(),
    pet_id              uuid not null references pets(id) on delete cascade,
    veterinarian_id     uuid not null references veterinarians(id),
    specialty_id        uuid not null references specialties(id),
    scheduled_at        timestamptz not null,
    status              varchar(20) not null default 'PENDIENTE'
                        check (status in ('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'NO_ATENDIDA', 'CANCELADA')),
    reason              text not null,
    cancellation_reason text, -- Agregado para soportar el modal de cancelación del front
    created_at          timestamptz not null default now(),
    -- Rule: Prevents double-booking for the same vet at the same date/time
    constraint uq_appointment_veterinarian_schedule unique (veterinarian_id, scheduled_at)
);

create index idx_appointments_pet on appointments(pet_id);
create index idx_appointments_veterinarian on appointments(veterinarian_id);
create index idx_appointments_date on appointments(scheduled_at);

-- 5. CLINICAL RECORDS & REVIEWS
create table diagnoses (
    id             uuid primary key default gen_random_uuid(),
    appointment_id uuid not null unique references appointments(id) on delete cascade,
    diagnosis      text not null,
    details        text,
    treatment      text,
    notes          text,
    created_at     timestamptz not null default now()
);

create table reviews (
    id             uuid primary key default gen_random_uuid(),
    appointment_id uuid not null unique references appointments(id) on delete cascade,
    rating         smallint not null check (rating between 1 and 5),
    comment        text,
    created_at     timestamptz not null default now()
);

-- 6. NOTIFICATIONS
create table notifications (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null references users(id) on delete cascade,
    appointment_id uuid references appointments(id) on delete set null,
    title          varchar(100),
    message        text not null,
    type           varchar(30),
    read           boolean not null default false,
    scheduled_at   timestamptz,
    created_at     timestamptz not null default now()
);

create index idx_notifications_user on notifications(user_id);