package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Team;

/**
 * Listing Query API for defining a query to list {@link Team} entities.
 *
 * @author Petr Vogl
 */
public interface TeamListingQuerySpi extends EntityListingQuerySpi<Team, TeamListingQuerySpi> {

	/**
	 * This method is the factory method for instances of this interface default
	 * implementation.
	 *
	 * @return New instance of this interface default implementation
	 */
	static TeamListingQuerySpi create() {
		return new TeamListingQuery();
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
	TeamListingQuerySpi setActive(Boolean active);
}
