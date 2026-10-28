package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="rates")
public class RatePersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="room_type_id",nullable=false) private RoomTypePersistenceEntity roomType;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal price;
    @Column(length=30) private String season;
    @Column(name="valid_from",nullable=false) private LocalDate validFrom;
    @Column(name="valid_to") private LocalDate validTo;
    protected RatePersistenceEntity() { }
    public RatePersistenceEntity(Long id, RoomTypePersistenceEntity roomType, BigDecimal price, String season, LocalDate validFrom, LocalDate validTo) { this.id=id; this.roomType=roomType; this.price=price; this.season=season; this.validFrom=validFrom; this.validTo=validTo; }
    public Long getId(){return id;} public RoomTypePersistenceEntity getRoomType(){return roomType;} public BigDecimal getPrice(){return price;} public String getSeason(){return season;} public LocalDate getValidFrom(){return validFrom;} public LocalDate getValidTo(){return validTo;}
}
