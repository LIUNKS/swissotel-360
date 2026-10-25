package com.swissotel.swissotel360.guests.domain.model.queries;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import java.util.List;

public record GuestPage(List<Guest> content, long totalElements, int page, int size) {
    public int totalPages() { return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size); }
}
