package com.example.docMind.model;

public record ApiResponse<T>(
        T data
) {}
