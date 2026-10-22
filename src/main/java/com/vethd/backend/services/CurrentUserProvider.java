package com.vethd.backend.services;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
