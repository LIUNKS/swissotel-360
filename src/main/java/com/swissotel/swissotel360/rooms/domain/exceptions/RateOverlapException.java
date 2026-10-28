package com.swissotel.swissotel360.rooms.domain.exceptions;
import java.time.LocalDate;
import java.util.List;
public class RateOverlapException extends RuntimeException { private final List<LocalDate> conflicts; public RateOverlapException(List<LocalDate> conflicts) { super("Rate overlaps an existing period"); this.conflicts = List.copyOf(conflicts); } public List<LocalDate> getConflicts() { return conflicts; } }
