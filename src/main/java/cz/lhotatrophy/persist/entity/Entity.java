package cz.lhotatrophy.persist.entity;

import java.io.Serializable;

/**
 * Entity
 *
 * @author Petr Vogl
 * @param <I> Type of entity identifier (the primary key)
 * @param <E> Type of entity
 */
public interface Entity<I extends Serializable, E extends Entity<I, E>> extends Serializable, Comparable<E> {

	/**
	 * Provides the identifier of an entity.
	 *
	 * @return Entity identifier
	 */
	I getId();

	/**
	 * Sets the identifier of an entity.
	 *
	 * @param id
	 */
	void setId(final I id);

	/**
	 * Compares this object's ID with the specified object's ID for order.
	 * Returns a negative integer, zero, or a positive integer as this object's
	 * ID is less than, equal to, or greater than the specified object's ID.
	 *
	 * @param otherId The ID to be compared.
	 * @return A negative integer, zero, or a positive integer as this object's
	 * ID is less than, equal to, or greater than the specified object's ID.
	 */
	default int idCompareToId(final I otherId) {
		final I thisId = getId();
		if (thisId instanceof Comparable) {
			return ((Comparable) thisId).compareTo(otherId);
		}
		throw new UnsupportedOperationException("Not supported.");
	}
}
