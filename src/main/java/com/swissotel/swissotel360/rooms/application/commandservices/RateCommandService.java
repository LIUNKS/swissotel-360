package com.swissotel.swissotel360.rooms.application.commandservices;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRateCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.CloseRateCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRateCommand;

public interface RateCommandService {
	Rate create(CreateRateCommand command);

	Rate close(CloseRateCommand command);

	void delete(DeleteRateCommand command);
}
