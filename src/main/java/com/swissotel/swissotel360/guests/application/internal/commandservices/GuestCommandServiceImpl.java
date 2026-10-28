package com.swissotel.swissotel360.guests.application.internal.commandservices;

import com.swissotel.swissotel360.guests.application.commandservices.GuestCommandService;
import com.swissotel.swissotel360.guests.domain.exceptions.DocumentTypeNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.DuplicateGuestException;
import com.swissotel.swissotel360.guests.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserAlreadyLinkedException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserNotFoundException;
import com.swissotel.swissotel360.guests.domain.model.commands.LinkGuestAccountCommand;
import com.swissotel.swissotel360.guests.domain.model.valueobjects.IdentityDocumentNumber;
import com.swissotel.swissotel360.guests.application.internal.outboundservices.acl.IamGuestAccountOutboundService;
import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.commands.CreateGuestCommand;
import com.swissotel.swissotel360.guests.domain.model.commands.UpdateGuestCommand;
import com.swissotel.swissotel360.guests.domain.repositories.DocumentTypeRepository;
import com.swissotel.swissotel360.guests.domain.repositories.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestCommandServiceImpl implements GuestCommandService {
    private final GuestRepository guestRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final IamGuestAccountOutboundService iamGuestAccountOutboundService;

    public GuestCommandServiceImpl(GuestRepository guestRepository, DocumentTypeRepository documentTypeRepository,
            IamGuestAccountOutboundService iamGuestAccountOutboundService) {
        this.guestRepository = guestRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.iamGuestAccountOutboundService = iamGuestAccountOutboundService;
    }

    @Override
    @Transactional
    public Guest create(CreateGuestCommand command) {
        var documentType = documentTypeRepository.findById(command.documentTypeId())
                .orElseThrow(() -> new DocumentTypeNotFoundException(command.documentTypeId()));
        var identityDocument = new IdentityDocumentNumber(documentType.code(), command.documentNumber());
        var number = identityDocument.value();
        if (guestRepository.existsByDocument(command.documentTypeId(), number)) {
            throw new DuplicateGuestException(command.documentTypeId(), number);
        }
        var guest = new Guest(null, null, null, documentType.id(), documentType.code(), number,
                command.firstName(), command.lastName(), command.email(), command.phone(), command.countryOfOrigin(), null);
        return guestRepository.save(guest);
    }

    @Override
    @Transactional
    public Guest update(UpdateGuestCommand command) {
        var guest = guestRepository.findByPublicId(command.publicId())
                .orElseThrow(() -> new GuestNotFoundException(command.publicId().toString()));
        guest.updateProfile(command.firstName(), command.lastName(), command.email(), command.phone(), command.countryOfOrigin());
        return guestRepository.save(guest);
    }

    @Override
    @Transactional
    public Guest linkUserAccount(LinkGuestAccountCommand command) {
        var guest = guestRepository.findByPublicId(command.guestPublicId())
                .orElseThrow(() -> new GuestNotFoundException(command.guestPublicId().toString()));
        var userId = iamGuestAccountOutboundService.fetchInternalUserId(command.userPublicId())
                .orElseThrow(() -> new UserNotFoundException(command.userPublicId()));
        if (guest.getUserId() != null || guestRepository.existsByUserId(userId)) {
            throw new UserAlreadyLinkedException();
        }
        guest.linkUserAccount(userId);
        return guestRepository.save(guest);
    }
}
