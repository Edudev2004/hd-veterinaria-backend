-- ==============================================================================
-- VetHD - Script SQL Oficial de Datos de Prueba para Supabase (PostgreSQL)
-- Copia y pega directamente este script en el SQL Editor de tu panel de Supabase
-- Contraseña unificada para todas las cuentas: 12345678
-- Hash BCrypt válido: $2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O
-- ==============================================================================

-- 1. ROLES DEL SISTEMA
INSERT INTO public.roles (id, name, description, created_at)
VALUES 
  (gen_random_uuid(), 'ROLE_ADMIN', 'Rol de administrador general del sistema', NOW()),
  (gen_random_uuid(), 'ROLE_VETERINARIO', 'Rol de médico veterinario clínico', NOW()),
  (gen_random_uuid(), 'ROLE_PROPIETARIO', 'Rol de propietario de mascotas', NOW())
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description;

-- 2. ESPECIALIDADES VETERINARIAS
INSERT INTO public.specialties (id, name, description, active)
VALUES
  (gen_random_uuid(), 'Medicina General', 'Atención médica veterinaria general y preventiva', true),
  (gen_random_uuid(), 'Cirugía General', 'Intervenciones quirúrgicas de tejidos blandos y traumatología', true),
  (gen_random_uuid(), 'Dermatología', 'Diagnóstico y tratamiento de alergias y afecciones cutáneas', true),
  (gen_random_uuid(), 'Oftalmología', 'Cuidado de la visión y problemas oculares', true)
ON CONFLICT (name) DO UPDATE SET active = true;

-- 3. USUARIOS (Password para todos: 12345678)
-- Administrador
INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '30000003', 'Administrador', 'VetHD', 'admin@vethd.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_ADMIN', 'ADMIN') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

-- Médicos Veterinarios
INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '20000002', 'Laura', 'Veterinario', 'veterinario@vethd.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_VETERINARIO', 'VETERINARIO') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '20000004', 'Mateo', 'Gómez Cirujano', 'mateo.cirujano@vethd.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_VETERINARIO', 'VETERINARIO') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '20000005', 'Sofía', 'Martínez Derma', 'sofia.derma@vethd.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_VETERINARIO', 'VETERINARIO') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

-- Clientes / Propietarios
INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '10000001', 'Carlos', 'Propietario', 'propietario@vethd.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_PROPIETARIO', 'PROPIETARIO') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '10000006', 'María', 'López', 'maria.lopez@gmail.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_PROPIETARIO', 'PROPIETARIO') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

INSERT INTO public.users (id, dni, first_name, last_name, email, password_hash, role_id, active, created_at)
VALUES (
  gen_random_uuid(), '10000007', 'Juan', 'Pérez', 'juan.perez@gmail.com',
  '$2a$10$n5s4ANwloVZ01vTFBqTWy.G2jlO4aLVODESHueWm24PENKu9jkV/O',
  (SELECT id FROM public.roles WHERE name IN ('ROLE_PROPIETARIO', 'PROPIETARIO') LIMIT 1),
  true, NOW()
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, active = true;

-- 4. VETERINARIOS (Enlazados dinámicamente por email)
INSERT INTO public.veterinarians (id, user_id, active)
SELECT gen_random_uuid(), id, true FROM public.users WHERE email = 'veterinario@vethd.com'
ON CONFLICT (user_id) DO UPDATE SET active = true;

INSERT INTO public.veterinarians (id, user_id, active)
SELECT gen_random_uuid(), id, true FROM public.users WHERE email = 'mateo.cirujano@vethd.com'
ON CONFLICT (user_id) DO UPDATE SET active = true;

INSERT INTO public.veterinarians (id, user_id, active)
SELECT gen_random_uuid(), id, true FROM public.users WHERE email = 'sofia.derma@vethd.com'
ON CONFLICT (user_id) DO UPDATE SET active = true;

-- 5. ASIGNACIÓN DE ESPECIALIDADES A VETERINARIOS
INSERT INTO public.veterinarian_specialties (veterinarian_id, specialty_id)
SELECT v.id, s.id
FROM public.veterinarians v
JOIN public.users u ON v.user_id = u.id
CROSS JOIN public.specialties s
WHERE u.email = 'veterinario@vethd.com' AND s.name = 'Medicina General'
ON CONFLICT DO NOTHING;

INSERT INTO public.veterinarian_specialties (veterinarian_id, specialty_id)
SELECT v.id, s.id
FROM public.veterinarians v
JOIN public.users u ON v.user_id = u.id
CROSS JOIN public.specialties s
WHERE u.email = 'mateo.cirujano@vethd.com' AND s.name = 'Cirugía General'
ON CONFLICT DO NOTHING;

INSERT INTO public.veterinarian_specialties (veterinarian_id, specialty_id)
SELECT v.id, s.id
FROM public.veterinarians v
JOIN public.users u ON v.user_id = u.id
CROSS JOIN public.specialties s
WHERE u.email = 'sofia.derma@vethd.com' AND s.name = 'Dermatología'
ON CONFLICT DO NOTHING;

-- 6. PROPIETARIOS (Enlazados dinámicamente por email)
INSERT INTO public.owners (id, user_id, phone, address)
SELECT gen_random_uuid(), id, '987654321', 'Av. Las Palmeras 450, Lima'
FROM public.users WHERE email = 'propietario@vethd.com'
ON CONFLICT (user_id) DO UPDATE SET phone = EXCLUDED.phone, address = EXCLUDED.address;

INSERT INTO public.owners (id, user_id, phone, address)
SELECT gen_random_uuid(), id, '912345678', 'Calle Los Jazmines 123, Miraflores'
FROM public.users WHERE email = 'maria.lopez@gmail.com'
ON CONFLICT (user_id) DO UPDATE SET phone = EXCLUDED.phone, address = EXCLUDED.address;

INSERT INTO public.owners (id, user_id, phone, address)
SELECT gen_random_uuid(), id, '998877665', 'Jr. Ayacucho 789, San Isidro'
FROM public.users WHERE email = 'juan.perez@gmail.com'
ON CONFLICT (user_id) DO UPDATE SET phone = EXCLUDED.phone, address = EXCLUDED.address;

-- 7. MASCOTAS (Gender: MALE / FEMALE exacto por check constraint)
INSERT INTO public.pets (id, owner_id, name, species, breed, gender, birthdate, observations, active, created_at)
SELECT gen_random_uuid(), o.id, 'Firulais', 'Perro', 'Golden Retriever', 'MALE', '2021-05-10'::date, 'Vacunas al día. Muy amigable y juguetón.', true, NOW()
FROM public.owners o JOIN public.users u ON o.user_id = u.id
WHERE u.email = 'propietario@vethd.com'
AND NOT EXISTS (SELECT 1 FROM public.pets p WHERE p.owner_id = o.id AND p.name = 'Firulais');

INSERT INTO public.pets (id, owner_id, name, species, breed, gender, birthdate, observations, active, created_at)
SELECT gen_random_uuid(), o.id, 'Rocky', 'Perro', 'Bulldog Francés', 'MALE', '2022-09-15'::date, 'Sensibilidad estomacal. Dieta especial.', true, NOW()
FROM public.owners o JOIN public.users u ON o.user_id = u.id
WHERE u.email = 'propietario@vethd.com'
AND NOT EXISTS (SELECT 1 FROM public.pets p WHERE p.owner_id = o.id AND p.name = 'Rocky');

INSERT INTO public.pets (id, owner_id, name, species, breed, gender, birthdate, observations, active, created_at)
SELECT gen_random_uuid(), o.id, 'Luna', 'Gato', 'Siamés', 'FEMALE', '2020-11-20'::date, 'Esterilizada. Dócil y tranquila.', true, NOW()
FROM public.owners o JOIN public.users u ON o.user_id = u.id
WHERE u.email = 'maria.lopez@gmail.com'
AND NOT EXISTS (SELECT 1 FROM public.pets p WHERE p.owner_id = o.id AND p.name = 'Luna');

INSERT INTO public.pets (id, owner_id, name, species, breed, gender, birthdate, observations, active, created_at)
SELECT gen_random_uuid(), o.id, 'Max', 'Perro', 'Pastor Alemán', 'MALE', '2023-01-08'::date, 'Enérgico, buen estado físico.', true, NOW()
FROM public.owners o JOIN public.users u ON o.user_id = u.id
WHERE u.email = 'maria.lopez@gmail.com'
AND NOT EXISTS (SELECT 1 FROM public.pets p WHERE p.owner_id = o.id AND p.name = 'Max');

INSERT INTO public.pets (id, owner_id, name, species, breed, gender, birthdate, observations, active, created_at)
SELECT gen_random_uuid(), o.id, 'Michi', 'Gato', 'Angora', 'MALE', '2021-08-14'::date, 'Alergia estacional en la piel.', true, NOW()
FROM public.owners o JOIN public.users u ON o.user_id = u.id
WHERE u.email = 'juan.perez@gmail.com'
AND NOT EXISTS (SELECT 1 FROM public.pets p WHERE p.owner_id = o.id AND p.name = 'Michi');

-- 8. CITAS MÉDICAS
-- Cita 1: Firulais - ATENDIDA con Laura
INSERT INTO public.appointments (id, pet_id, veterinarian_id, specialty_id, scheduled_at, status, reason, created_at)
SELECT gen_random_uuid(), p.id, v.id, s.id, '2026-11-10 10:00:00'::timestamp, 'ATENDIDA', 'Vacunación anual y control de peso', '2026-11-08 09:00:00'::timestamp
FROM public.pets p
CROSS JOIN public.veterinarians v JOIN public.users uv ON v.user_id = uv.id
CROSS JOIN public.specialties s
WHERE p.name = 'Firulais' AND uv.email = 'veterinario@vethd.com' AND s.name = 'Medicina General'
AND NOT EXISTS (SELECT 1 FROM public.appointments a WHERE a.pet_id = p.id AND a.scheduled_at = '2026-11-10 10:00:00'::timestamp);

-- Cita 2: Luna - ATENDIDA con Mateo
INSERT INTO public.appointments (id, pet_id, veterinarian_id, specialty_id, scheduled_at, status, reason, created_at)
SELECT gen_random_uuid(), p.id, v.id, s.id, '2026-11-12 11:30:00'::timestamp, 'ATENDIDA', 'Evaluación quirúrgica y esterilización', '2026-11-09 14:00:00'::timestamp
FROM public.pets p
CROSS JOIN public.veterinarians v JOIN public.users uv ON v.user_id = uv.id
CROSS JOIN public.specialties s
WHERE p.name = 'Luna' AND uv.email = 'mateo.cirujano@vethd.com' AND s.name = 'Cirugía General'
AND NOT EXISTS (SELECT 1 FROM public.appointments a WHERE a.pet_id = p.id AND a.scheduled_at = '2026-11-12 11:30:00'::timestamp);

-- Cita 3: Michi - ATENDIDA con Sofía
INSERT INTO public.appointments (id, pet_id, veterinarian_id, specialty_id, scheduled_at, status, reason, created_at)
SELECT gen_random_uuid(), p.id, v.id, s.id, '2026-11-15 16:00:00'::timestamp, 'ATENDIDA', 'Dermatitis alérgica y prurito en orejas', '2026-11-13 11:00:00'::timestamp
FROM public.pets p
CROSS JOIN public.veterinarians v JOIN public.users uv ON v.user_id = uv.id
CROSS JOIN public.specialties s
WHERE p.name = 'Michi' AND uv.email = 'sofia.derma@vethd.com' AND s.name = 'Dermatología'
AND NOT EXISTS (SELECT 1 FROM public.appointments a WHERE a.pet_id = p.id AND a.scheduled_at = '2026-11-15 16:00:00'::timestamp);

-- Cita 4: Rocky - CONFIRMADA con Laura
INSERT INTO public.appointments (id, pet_id, veterinarian_id, specialty_id, scheduled_at, status, reason, created_at)
SELECT gen_random_uuid(), p.id, v.id, s.id, '2026-11-21 09:30:00'::timestamp, 'CONFIRMADA', 'Revisión preventiva por alergia alimentaria', '2026-11-19 16:00:00'::timestamp
FROM public.pets p
CROSS JOIN public.veterinarians v JOIN public.users uv ON v.user_id = uv.id
CROSS JOIN public.specialties s
WHERE p.name = 'Rocky' AND uv.email = 'veterinario@vethd.com' AND s.name = 'Medicina General'
AND NOT EXISTS (SELECT 1 FROM public.appointments a WHERE a.pet_id = p.id AND a.scheduled_at = '2026-11-21 09:30:00'::timestamp);

-- Cita 5: Max - PENDIENTE con Mateo
INSERT INTO public.appointments (id, pet_id, veterinarian_id, specialty_id, scheduled_at, status, reason, created_at)
SELECT gen_random_uuid(), p.id, v.id, s.id, '2026-11-22 15:30:00'::timestamp, 'PENDIENTE', 'Consulta traumatológica por cojera leve', '2026-11-20 10:00:00'::timestamp
FROM public.pets p
CROSS JOIN public.veterinarians v JOIN public.users uv ON v.user_id = uv.id
CROSS JOIN public.specialties s
WHERE p.name = 'Max' AND uv.email = 'mateo.cirujano@vethd.com' AND s.name = 'Cirugía General'
AND NOT EXISTS (SELECT 1 FROM public.appointments a WHERE a.pet_id = p.id AND a.scheduled_at = '2026-11-22 15:30:00'::timestamp);

-- Cita 6: Firulais - CANCELADA con Sofía
INSERT INTO public.appointments (id, pet_id, veterinarian_id, specialty_id, scheduled_at, status, reason, cancellation_reason, created_at)
SELECT gen_random_uuid(), p.id, v.id, s.id, '2026-11-05 14:00:00'::timestamp, 'CANCELADA', 'Chequeo dermatológico preventivo', 'Propietario reprogramó por motivos personales', '2026-11-03 10:00:00'::timestamp
FROM public.pets p
CROSS JOIN public.veterinarians v JOIN public.users uv ON v.user_id = uv.id
CROSS JOIN public.specialties s
WHERE p.name = 'Firulais' AND uv.email = 'sofia.derma@vethd.com' AND s.name = 'Dermatología'
AND NOT EXISTS (SELECT 1 FROM public.appointments a WHERE a.pet_id = p.id AND a.scheduled_at = '2026-11-05 14:00:00'::timestamp);

-- 9. HISTORIALES Y DIAGNÓSTICOS (Para las citas atendidas)
INSERT INTO public.diagnoses (id, appointment_id, diagnosis, details, treatment, notes, next_suggested_appointment, created_at)
SELECT gen_random_uuid(), a.id, 'Paciente canino saludable. Vacunación séxtuple aplicada con éxito.', 'Temperatura 38.5°C, peso 28.5 kg, frecuencia cardíaca normal. Mucosas rosadas.', 'Vacuna séxtuple + desparasitante interno oral.', 'Control anual o si presenta decaimiento.', '2027-11-10'::date, '2026-11-10 10:45:00'::timestamp
FROM public.appointments a JOIN public.pets p ON a.pet_id = p.id
WHERE p.name = 'Firulais' AND a.status = 'ATENDIDA'
AND NOT EXISTS (SELECT 1 FROM public.diagnoses d WHERE d.appointment_id = a.id);

INSERT INTO public.diagnoses (id, appointment_id, diagnosis, details, treatment, notes, next_suggested_appointment, created_at)
SELECT gen_random_uuid(), a.id, 'Cirugía de esterilización electiva completada satisfactoriamente.', 'Ovariohisterectomía sin complicaciones intraoperatorias. Recuperación anestésica estable.', 'Meloxicam 0.1 mg/kg cada 24h por 4 días. Collar isabelino permanente.', 'Retiro de puntos en 10 días.', '2026-11-22'::date, '2026-11-12 12:40:00'::timestamp
FROM public.appointments a JOIN public.pets p ON a.pet_id = p.id
WHERE p.name = 'Luna' AND a.status = 'ATENDIDA'
AND NOT EXISTS (SELECT 1 FROM public.diagnoses d WHERE d.appointment_id = a.id);

INSERT INTO public.diagnoses (id, appointment_id, diagnosis, details, treatment, notes, next_suggested_appointment, created_at)
SELECT gen_random_uuid(), a.id, 'Otitis externa alérgica bilateral.', 'Eritema en pabellones auriculares, secreción ceruminosa moderada, prurito evidente.', 'Limpieza ótica cada 48h. Gotas óticas con corticoide cada 12h por 10 días.', 'Control evolutivo en 15 días.', '2026-11-30'::date, '2026-11-15 16:45:00'::timestamp
FROM public.appointments a JOIN public.pets p ON a.pet_id = p.id
WHERE p.name = 'Michi' AND a.status = 'ATENDIDA'
AND NOT EXISTS (SELECT 1 FROM public.diagnoses d WHERE d.appointment_id = a.id);

-- 10. RESEÑAS Y VALORACIONES
INSERT INTO public.reviews (id, appointment_id, rating, comment, created_at)
SELECT gen_random_uuid(), a.id, 5, 'Excelente trato de la Dra. Laura con Firulais. Muy paciente y puntual.', '2026-11-10 11:30:00'::timestamp
FROM public.appointments a JOIN public.pets p ON a.pet_id = p.id
WHERE p.name = 'Firulais' AND a.status = 'ATENDIDA'
AND NOT EXISTS (SELECT 1 FROM public.reviews r WHERE r.appointment_id = a.id);

INSERT INTO public.reviews (id, appointment_id, rating, comment, created_at)
SELECT gen_random_uuid(), a.id, 5, 'El Dr. Mateo es un cirujano de primera. La recuperación de Luna fue rapidísima.', '2026-11-12 13:00:00'::timestamp
FROM public.appointments a JOIN public.pets p ON a.pet_id = p.id
WHERE p.name = 'Luna' AND a.status = 'ATENDIDA'
AND NOT EXISTS (SELECT 1 FROM public.reviews r WHERE r.appointment_id = a.id);

INSERT INTO public.reviews (id, appointment_id, rating, comment, created_at)
SELECT gen_random_uuid(), a.id, 4, 'Muy buena atención dermatológica, el tratamiento dio resultados rápido.', '2026-11-15 17:15:00'::timestamp
FROM public.appointments a JOIN public.pets p ON a.pet_id = p.id
WHERE p.name = 'Michi' AND a.status = 'ATENDIDA'
AND NOT EXISTS (SELECT 1 FROM public.reviews r WHERE r.appointment_id = a.id);
