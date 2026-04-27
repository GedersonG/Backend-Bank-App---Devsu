package com.devsu.app.infraestructure.output.persistence.dto;

import java.util.List;

public record PaginatedResult<T>(List<T> content, long totalCount) {}
