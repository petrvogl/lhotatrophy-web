package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Clue;
import java.util.Objects;
import lombok.NonNull;

/**
 * The implementation of the Listing Query API for {@link Clue} entities.
 *
 * @author Petr Vogl
 */
@SuppressWarnings("EqualsAndHashcode")
public class ClueListingQuery
		extends EntityListingQuery<Clue, ClueListingQuerySpi>
		implements ClueListingQuerySpi {

	/**
	 * The criterion for the activity status.
	 */
	private Boolean active;

	/**
	 * Constructor.
	 */
	public ClueListingQuery() {
		super(Clue.class);
	}

	/**
	 * Copy constructor.
	 *
	 * @param other Listing query
	 */
	public ClueListingQuery(@NonNull final ClueListingQuerySpi other) {
		super(other);
		this.active = other.getActive();
	}

	@Override
	public ClueListingQuerySpi createBaseListingQuery() {
		final ClueListingQuery baseQuery = new ClueListingQuery(this);
		// reset all criteria except of those which are the base
		baseQuery.active = null;
		// return base query
		return equals(baseQuery) ? null : baseQuery;
	}

	@Override
	public boolean check(final Clue clue) {
		if (!super.check(clue)) {
			return false;
		}
		if (active != null && !active.equals(clue.getActive())) {
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
		final ClueListingQuery other = (ClueListingQuery) obj;
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
	public ClueListingQuerySpi setActive(final Boolean active) {
		this.active = active;
		resetHashCode();
		return this;
	}
}
