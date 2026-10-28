package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.converters;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;
@Converter
public class RoomStatusConverter implements AttributeConverter<RoomStatus,String> {
    @Override public String convertToDatabaseColumn(RoomStatus status) { return status == null ? null : status.name().toLowerCase(Locale.ROOT); }
    @Override public RoomStatus convertToEntityAttribute(String value) { return value == null ? null : RoomStatus.valueOf(value.toUpperCase(Locale.ROOT)); }
}
