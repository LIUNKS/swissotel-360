package com.swissotel.swissotel360.rooms.application.internal.commandservices;

import com.swissotel.swissotel360.rooms.application.commandservices.RateCommandService;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateEntityNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateCannotBeDeletedException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateOverlapException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRateCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.CloseRateCommand;
import com.swissotel.swissotel360.rooms.domain.model.commands.DeleteRateCommand;
import com.swissotel.swissotel360.rooms.domain.repositories.RateRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import java.time.LocalDate;
import java.util.UUID;

public class RateCommandServiceImpl implements RateCommandService {
	private final RateRepository rates;
	private final RoomTypeRepository roomTypes;

	public RateCommandServiceImpl(RateRepository rates, RoomTypeRepository roomTypes) {
		this.rates = rates;
		this.roomTypes = roomTypes;
	}

	public Rate create(CreateRateCommand c) {
		if (c.validTo() != null && c.validTo().isBefore(c.validFrom()))
			throw new IllegalArgumentException("validTo must be on or after validFrom");
		var type = roomTypes.findByPublicId(c.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(c.roomTypePublicId()));
		var conflicts = rates.findOverlapping(type.id(), c.validFrom(), c.validTo());
		if (!conflicts.isEmpty())
			throw new RateOverlapException(conflicts.stream()
					.map(r -> r.validFrom().isAfter(c.validFrom()) ? r.validFrom() : c.validFrom()).toList());
		return rates
				.save(new Rate(null, type.id(), type.publicId(), c.price(), c.season(), c.validFrom(), c.validTo()));
	}

	public Rate close(CloseRateCommand command) {
		var type = roomTypes.findByPublicId(command.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(command.roomTypePublicId()));
		var rate = rates.findById(command.rateId())
				.orElseThrow(() -> new RateEntityNotFoundException(command.rateId()));
		if (!rate.roomTypeId().equals(type.id()))
			throw new RateEntityNotFoundException(command.rateId());
		return rates.save(rate.close(command.validTo()));
	}

	public void delete(DeleteRateCommand command) {
		var type = roomTypes.findByPublicId(command.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(command.roomTypePublicId()));
		var rate = rates.findById(command.rateId())
				.orElseThrow(() -> new RateEntityNotFoundException(command.rateId()));
		if (!rate.roomTypeId().equals(type.id()))
			throw new RateEntityNotFoundException(command.rateId());
		rate.validateDeletable(LocalDate.now());
		rates.delete(rate);
	}
}
