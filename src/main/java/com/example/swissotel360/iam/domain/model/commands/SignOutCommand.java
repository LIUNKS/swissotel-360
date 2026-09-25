package com.example.swissotel360.iam.domain.model.commands;

/**
 * Sign out command
 * <p>
 *     This class represents the command to sign out a user.
 * </p>
 * @param username the username of the user to sign out
 */
public record SignOutCommand(String username) {
}
