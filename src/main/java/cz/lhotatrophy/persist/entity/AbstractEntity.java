package cz.lhotatrophy.persist.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity
 *
 * @author Petr Vogl
 * @param <T> Type of entity identifier (the primary key)
 */
public abstract class AbstractEntity<T extends Serializable> implements Entity<T> {

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
}
