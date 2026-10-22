package com.vethd.backend.DTOs.response;

public record UploadResponse(
        String url,
        String fileName,
        long size
) {}
