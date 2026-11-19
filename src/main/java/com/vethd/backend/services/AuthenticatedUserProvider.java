package com.vethd.backend.services;

import java.util.UUID;

public interface AuthenticatedUserProvider {
    UUID getCurrentUserId();
}
