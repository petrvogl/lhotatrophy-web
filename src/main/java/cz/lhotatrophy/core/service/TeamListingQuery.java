package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Team;
import java.util.Objects;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
@SuppressWarnings("EqualsAndHashcode")
public final class TeamListingQuery
		extends EntityListingQuery<Team, TeamListingQuerySpi>
		implements TeamListingQuerySpi {

	/**
	 * The criterion for an active team entity.
	 */
	private Boolean active;

	/**
	 * Constructor.
	 */
	public TeamListingQuery() {
		super(Team.class);
	}

	/**
	 * Copy constructor.
	 *
	 * @param other Listing query
	 */
	public TeamListingQuery(@NonNull final TeamListingQuerySpi other) {
		super(other);
		this.active = other.getActive();
	}

	@Override
	public TeamListingQuerySpi createBaseListingQuery() {
		final TeamListingQuery baseQuery = new TeamListingQuery(this);
		// reset all criteria except of those which are the base
		baseQuery.active = null;
		// return base query
		return equals(baseQuery) ? null : baseQuery;
	}

	@Override
	public boolean check(final Team team) {
		if (!super.check(team)) {
			return false;
		}
		if (active != null && !active.equals(team.getActive())) {
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
		final TeamListingQuery other = (TeamListingQuery) obj;
		if (!Objects.equals(this.active, other.active)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} if the listed teams must be active. Returns
	 * {@code false} if the listed teams must be inactive. Returns {@code null}
	 * if the listed teams can be active or inactive.
	 */
	@Override
	public Boolean getActive() {
		return active;
	}

	/**
	 * Sets the criterion for an active team entity.
	 *
	 * @param active criterion value
	 * @return Listing query
	 */
	@Override
	public TeamListingQuerySpi setActive(final Boolean active) {
		this.active = active;
		resetHashCode();
		return this;
	}
}
