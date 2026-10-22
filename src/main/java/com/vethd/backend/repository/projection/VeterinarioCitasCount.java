package com.vethd.backend.repository.projection;

import java.util.UUID;

public interface VeterinarioCitasCount {
    UUID getVeterinarianId();
    String getFullName();
    Long getCount();
}
