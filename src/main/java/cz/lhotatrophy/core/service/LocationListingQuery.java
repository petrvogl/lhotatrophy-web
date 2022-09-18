package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Location;
import java.util.Objects;
import lombok.NonNull;

/**
 * The implementation of the Listing Query API for {@link Location} entities.
 *
 * @author Petr Vogl
 */
@SuppressWarnings("EqualsAndHashcode")
public class LocationListingQuery
		extends EntityListingQuery<Location, LocationListingQuerySpi>
		implements LocationListingQuerySpi {

	/**
	 * The criterion for the activity status.
	 */
	private Boolean active;

	/**
	 * Constructor.
	 */
	public LocationListingQuery() {
		super(Location.class);
	}

	/**
	 * Copy constructor.
	 *
	 * @param other Listing query
	 */
	public LocationListingQuery(@NonNull final LocationListingQuerySpi other) {
		super(other);
		this.active = other.getActive();
	}

	@Override
	public LocationListingQuerySpi createBaseListingQuery() {
		final LocationListingQuery baseQuery = new LocationListingQuery(this);
		// reset all criteria except of those which are the base
		baseQuery.active = null;
		// return base query
		return equals(baseQuery) ? null : baseQuery;
	}

	@Override
	public boolean check(final Location location) {
		if (!super.check(location)) {
			return false;
		}
		if (active != null && !active.equals(location.getActive())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCodeInternal() {
		int hash = super.hashCodeInternal();
		hash = 59 * hash + Objects.hashCode(this.active);
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		final boolean equals = super.equals(obj);
		if (!equals) {
			return false;
		}
		final LocationListingQuery other = (LocationListingQuery) obj;
		if (!Objects.equals(this.active, other.active)) {
			return false;
		}
		return true;
	}

	@Override
	public Boolean getActive() {
		return active;
	}

	@Override
	public LocationListingQuerySpi setActive(final Boolean active) {
		this.active = active;
		resetHashCode();
		return this;
	}
}
