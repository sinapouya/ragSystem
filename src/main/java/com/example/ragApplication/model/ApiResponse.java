package com.example.ragApplication.model;

public record ApiResponse<T>(
        T data
) {}
