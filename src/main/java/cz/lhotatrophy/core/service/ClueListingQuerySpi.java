package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Clue;

/**
 * Listing Query API for defining a query to list {@link Clue} entities.
 *
 * @author Petr Vogl
 */
public interface ClueListingQuerySpi extends EntityListingQuerySpi<Clue, ClueListingQuerySpi> {

	/**
	 * This method is the factory method for instances of this interface default
	 * implementation.
	 *
	 * @return New instance of this interface default implementation
	 */
	static ClueListingQuerySpi create() {
		return new ClueListingQuery();
	}

	/**
	 * Returns the criterion value for the activity status. If it returns
	 * {@code null}, the criterion is not set.
	 */
	Boolean getActive();

	/**
	 * Sets the criterion for the activity status.
	 *
	 * @param active criterion value
	 * @return Listing query
	 */
	ClueListingQuerySpi setActive(Boolean active);
}
