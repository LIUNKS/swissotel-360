package com.swissotel.swissotel360.stays.application.commandservices;

import com.swissotel.swissotel360.stays.application.command.*;
import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;

public interface StayCommandService {
	StayView checkIn(CheckInCommand command);

	Stay checkOut(CheckOutCommand command);

	DigitalKey issueKey(IssueDigitalKeyCommand command);

	DigitalKey revokeKey(RevokeDigitalKeyCommand command);
}
