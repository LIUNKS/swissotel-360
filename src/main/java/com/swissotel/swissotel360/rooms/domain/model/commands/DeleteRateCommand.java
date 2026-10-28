package com.swissotel.swissotel360.rooms.domain.model.commands;
import java.util.UUID;
public record DeleteRateCommand(UUID roomTypePublicId,Long rateId) { }
