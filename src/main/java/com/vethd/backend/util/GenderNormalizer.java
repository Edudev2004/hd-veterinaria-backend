package com.vethd.backend.util;

/**
 * Normaliza el género entre el vocabulario del frontend (macho/hembra)
 * y el que exige la base de datos (MALE/FEMALE, ver pets_gender_check).
 *
 * Es el único punto de traducción para evitar que cualquier ruta de escritura
 * (API, inicializadores de datos, etc.) vuelva a violar la restricción.
 */
public final class GenderNormalizer {

    private GenderNormalizer() {
    }

    /** Convierte el género recibido desde el cliente al formato almacenado en la BD. */
    public static String toDatabase(String gender) {
        if (gender == null) {
            return null;
        }

        String normalized = gender.trim().toLowerCase();
        return switch (normalized) {
            case "macho", "male" -> "MALE";
            case "hembra", "female" -> "FEMALE";
            default -> gender.trim().toUpperCase();
        };
    }

    /** Convierte el género almacenado en la BD al formato que espera el frontend. */
    public static String toClient(String gender) {
        if (gender == null) {
            return null;
        }

        String normalized = gender.trim().toUpperCase();
        return switch (normalized) {
            case "MALE" -> "macho";
            case "FEMALE" -> "hembra";
            default -> gender.trim().toLowerCase();
        };
    }
}
