package com.swissotel.swissotel360.iam.interfaces.acl;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface representing the Anti-Corruption Layer (ACL) facade for the IAM
 * Bounded Context.
 * Offers inter-context communication contracts avoiding domain entity coupling.
 */
public interface IamContextFacade {

	/**
	 * Creates a new user with default role.
	 *
	 * @param fullName
	 *                     the user's full name
	 * @param email
	 *                     the user's email
	 * @param password
	 *                     the user's plain password
	 * @return the newly created user's ID, or 0 if creation fails
	 */
	Long createUser(String fullName, String email, String password);

	/**
	 * Fetches a user identifier by email address.
	 *
	 * @param email
	 *                  the email to search for
	 * @return the user ID, or 0 if not found
	 */
	Long fetchUserIdByEmail(String email);

	/**
	 * Fetches a user email address by user ID.
	 *
	 * @param userId
	 *                   the user ID
	 * @return the email address, or empty string if not found
	 */
	String fetchEmailByUserId(Long userId);

	/** Resolves an IAM user's internal identifier from its public identifier. */
	Optional<Long> fetchUserIdByPublicId(UUID userPublicId);
}
