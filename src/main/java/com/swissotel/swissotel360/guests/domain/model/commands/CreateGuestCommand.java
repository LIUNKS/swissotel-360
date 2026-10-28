package com.swissotel.swissotel360.guests.domain.model.commands;

public record CreateGuestCommand(Long documentTypeId, String documentNumber, String firstName,
		String lastName, String email, String phone, String countryOfOrigin) {
}
