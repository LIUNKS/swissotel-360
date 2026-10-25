package com.swissotel.swissotel360.guests.domain.model.queries;

public record SearchGuestsQuery(String query, int page, int size) {
    public SearchGuestsQuery {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page or size");
    }
}
