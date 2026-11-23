package com.swissotel.swissotel360.guests.application.commandservices;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.commands.CreateGuestCommand;
import com.swissotel.swissotel360.guests.domain.model.commands.UpdateGuestCommand;
import com.swissotel.swissotel360.guests.domain.model.commands.LinkGuestAccountCommand;

public interface GuestCommandService {
	Guest create(CreateGuestCommand command);

	Guest update(UpdateGuestCommand command);

	Guest linkUserAccount(LinkGuestAccountCommand command);
}
