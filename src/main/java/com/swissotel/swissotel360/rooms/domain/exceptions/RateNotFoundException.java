package com.swissotel.swissotel360.rooms.domain.exceptions;
import java.time.LocalDate;
public class RateNotFoundException extends RuntimeException { private final LocalDate date; public RateNotFoundException(LocalDate date) { super("No effective rate for " + date); this.date = date; } public LocalDate getDate() { return date; } }
