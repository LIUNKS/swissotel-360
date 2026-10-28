package com.swissotel.swissotel360.guests.domain.model.aggregates;

import com.swissotel.swissotel360.guests.domain.exceptions.UserAlreadyLinkedException;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Guest aggregate root. Its identifier exposed outside this context is always
 * publicId.
 */
public class Guest {
	private final Long id;
	private final UUID publicId;
	private Long userId;
	private final Long documentTypeId;
	private final String documentTypeCode;
	private final String documentNumber;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private String countryOfOrigin;
	private final OffsetDateTime registeredAt;

	public Guest(Long id, UUID publicId, Long userId, Long documentTypeId, String documentTypeCode,
			String documentNumber, String firstName, String lastName, String email, String phone,
			String countryOfOrigin, OffsetDateTime registeredAt) {
		if (documentTypeId == null || documentNumber == null || documentNumber.isBlank())
			throw new IllegalArgumentException("A document type and number are required");
		if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank())
			throw new IllegalArgumentException("Guest first and last names are required");
		this.id = id;
		this.publicId = publicId == null ? UUID.randomUUID() : publicId;
		this.userId = userId;
		this.documentTypeId = documentTypeId;
		this.documentTypeCode = documentTypeCode;
		this.documentNumber = documentNumber.trim();
		this.firstName = firstName.trim();
		this.lastName = lastName.trim();
		this.email = email;
		this.phone = phone;
		this.countryOfOrigin = countryOfOrigin;
		this.registeredAt = registeredAt == null ? OffsetDateTime.now() : registeredAt;
	}

	public void updateProfile(String firstName, String lastName, String email, String phone, String countryOfOrigin) {
		if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank())
			throw new IllegalArgumentException("Guest first and last names are required");
		this.firstName = firstName.trim();
		this.lastName = lastName.trim();
		this.email = email;
		this.phone = phone;
		this.countryOfOrigin = countryOfOrigin;
	}

	public void linkUserAccount(Long userId) {
		if (this.userId != null)
			throw new UserAlreadyLinkedException();
		if (userId == null)
			throw new IllegalArgumentException("IAM user identifier is required");
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public UUID getPublicId() {
		return publicId;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getDocumentTypeId() {
		return documentTypeId;
	}

	public String getDocumentTypeCode() {
		return documentTypeCode;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getCountryOfOrigin() {
		return countryOfOrigin;
	}

	public OffsetDateTime getRegisteredAt() {
		return registeredAt;
	}
}
