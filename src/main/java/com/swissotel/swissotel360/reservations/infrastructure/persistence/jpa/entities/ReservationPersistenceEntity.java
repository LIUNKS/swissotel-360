package com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="reservations")
public class ReservationPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false) private UUID publicId;
    @Column(name="reservation_code",nullable=false,unique=true,length=20) private String reservationCode;
    @Column(name="guest_id",nullable=false) private Long guestId;
    @Column(name="room_type_id",nullable=false) private Long roomTypeId;
    @Column(name="check_in_date",nullable=false) private LocalDate checkInDate;
    @Column(name="check_out_date",nullable=false) private LocalDate checkOutDate;
    @Column(nullable=false,length=20) private String status;
    @Column(name="payment_type",nullable=false,length=20) private String paymentType;
    @Column(name="total_amount",nullable=false,precision=10,scale=2) private BigDecimal totalAmount;
    @Column(name="created_by") private Long createdBy;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="cancelled_at") private Instant cancelledAt;
    @Column(name="cancellation_reason",length=20) private String cancellationReason;
    @Column(name="cancellation_note",length=200) private String cancellationNote;
    @Column(name="updated_at",insertable=false,updatable=false) private Instant updatedAt;
    public ReservationPersistenceEntity() { }
    public Long getId(){return id;} public UUID getPublicId(){return publicId;} public String getReservationCode(){return reservationCode;}
    public Long getGuestId(){return guestId;} public Long getRoomTypeId(){return roomTypeId;} public LocalDate getCheckInDate(){return checkInDate;}
    public LocalDate getCheckOutDate(){return checkOutDate;} public String getStatus(){return status;} public String getPaymentType(){return paymentType;}
    public BigDecimal getTotalAmount(){return totalAmount;} public Long getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;}
    public Instant getCancelledAt(){return cancelledAt;} public String getCancellationReason(){return cancellationReason;}
    public String getCancellationNote(){return cancellationNote;}
    public void setPublicId(UUID v){publicId=v;} public void setReservationCode(String v){reservationCode=v;} public void setGuestId(Long v){guestId=v;}
    public void setRoomTypeId(Long v){roomTypeId=v;} public void setCheckInDate(LocalDate v){checkInDate=v;} public void setCheckOutDate(LocalDate v){checkOutDate=v;}
    public void setStatus(String v){status=v;} public void setPaymentType(String v){paymentType=v;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public void setCreatedBy(Long v){createdBy=v;} public void setCreatedAt(Instant v){createdAt=v;} public void setCancelledAt(Instant v){cancelledAt=v;}
    public void setCancellationReason(String v){cancellationReason=v;}
    public void setCancellationNote(String v){cancellationNote=v;}
}
