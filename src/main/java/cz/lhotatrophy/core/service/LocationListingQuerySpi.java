package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Location;

/**
 * Listing Query API for defining a query to list {@link Location} entities.
 * 
 * @author Petr Vogl
 */
public interface LocationListingQuerySpi extends EntityListingQuerySpi<Location, LocationListingQuerySpi> {

	/**
	 * This method is the factory method for instances of this interface default
	 * implementation.
	 *
	 * @return New instance of this interface default implementation
	 */
	static LocationListingQuerySpi create() {
		return new LocationListingQuery();
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
	LocationListingQuerySpi setActive(Boolean active);
}
