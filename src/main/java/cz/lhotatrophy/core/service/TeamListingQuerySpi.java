package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Team;

/**
 * API for defining a query to list teams.
 *
 * @author Petr Vogl
 */
public interface TeamListingQuerySpi extends EntityListingQuerySpi<Team, TeamListingQuerySpi> {

	static TeamListingQuerySpi create() {
		return new TeamListingQuery();
	}

	Boolean getActive();

	TeamListingQuerySpi setActive(final Boolean active);
}
