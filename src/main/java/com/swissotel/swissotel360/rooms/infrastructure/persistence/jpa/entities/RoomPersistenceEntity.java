package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.converters.RoomStatusConverter;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="rooms")
public class RoomPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false) private UUID publicId;
    @ManyToOne(optional=false) @JoinColumn(name="room_type_id",nullable=false) private RoomTypePersistenceEntity roomType;
    @Column(name="room_number",nullable=false,unique=true,length=10) private String roomNumber;
    @Column(nullable=false) private Short floor;
    @Convert(converter=RoomStatusConverter.class) @Column(nullable=false,length=20) private RoomStatus status;
    protected RoomPersistenceEntity() { }
    public RoomPersistenceEntity(Long id, UUID publicId, RoomTypePersistenceEntity roomType, String roomNumber, Short floor, RoomStatus status) { this.id=id; this.publicId=publicId; this.roomType=roomType; this.roomNumber=roomNumber; this.floor=floor; this.status=status; }
    public Long getId(){return id;} public UUID getPublicId(){return publicId;} public RoomTypePersistenceEntity getRoomType(){return roomType;} public String getRoomNumber(){return roomNumber;} public Short getFloor(){return floor;} public RoomStatus getStatus(){return status;}
}
