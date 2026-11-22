package com.swissotel.swissotel360.shared.domain.model.aggregates;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all domain aggregate roots.
 *
 * <p>
 * Pure Java base class for domain aggregates providing domain event
 * registration
 * and retrieval without pulling in any Spring Data, JPA, or framework-specific
 * concerns.
 * </p>
 *
 * @param <T>
 *                the concrete aggregate root type
 */
@NullMarked
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>> {

	private final transient List<Object> domainEvents = new ArrayList<>();

	/**
	 * Registers a domain event to be published after this aggregate is saved.
	 *
	 * @param event
	 *                  the domain event to register
	 * @return this aggregate root instance
	 */
	@SuppressWarnings("unchecked")
	protected T registerDomainEvent(Object event) {
		this.domainEvents.add(event);
		return (T) this;
	}

	/**
	 * Returns all domain events registered on this aggregate since the last
	 * publication.
	 *
	 * @return an unmodifiable collection of registered domain events
	 */
	public Collection<Object> domainEvents() {
		return Collections.unmodifiableList(this.domainEvents);
	}

	/**
	 * Clears all registered domain events.
	 */
	public void clearDomainEvents() {
		this.domainEvents.clear();
	}
}
