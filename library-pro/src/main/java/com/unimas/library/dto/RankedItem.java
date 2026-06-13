package com.unimas.library.dto;

/** Generic row for "Top N" report tables (most borrowed books / most active members). */
public record RankedItem(String primary, String secondary, long count) { }
