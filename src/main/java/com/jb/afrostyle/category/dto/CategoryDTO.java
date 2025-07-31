package com.jb.afrostyle.category.dto;

/**
 * Category DTO migré vers Java Record pour réduire le boilerplate
 */
public record CategoryDTO(
    Long id,
    String name,
    String images
) {}