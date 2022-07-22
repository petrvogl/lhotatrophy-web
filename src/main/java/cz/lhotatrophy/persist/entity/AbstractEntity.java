package cz.lhotatrophy.persist.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity
 *
 * @author Petr Vogl
 * @param <I> Type of entity identifier (the primary key)
 * @param <E> Type of entity
 */
public abstract class AbstractEntity<I extends Serializable, E extends Entity<I, E>>
		implements Entity<I, E> {

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this.getId());
		hash = 19 * hash + Objects.hashCode(this.getClass());
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getId() == null) {
			// Non-persistent instances can't be identified
			// and that's why they're considered different.
			return false;
		}
		// Same class and same ID -> entities are considered equal
		// regardless of the value of the other attributes.
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractEntity other = (AbstractEntity) obj;
		return Objects.equals(this.getId(), other.getId());
	}
	
	@Override
	public int compareTo(final E other) {
		if (other == null) {
			return -1;
		}
		if (this.getId() == null) {
			if (other.getId() == null) {
				return 0;
			} else {
				return 1;
			}
		}
		if (other.getId() == null) {
			return -1;
		}
		return idCompareToId(other.getId());
	}
}
