package com.example.swissotel360.iam.interfaces.rest.transform;

import com.example.swissotel360.iam.domain.model.commands.SignInCommand;
import com.example.swissotel360.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    private SignInCommandFromResourceAssembler() {
    }

    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}
