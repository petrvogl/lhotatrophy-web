package cz.lhotatrophy.persist.entity;

import java.io.Serializable;

/**
 * Entity
 *
 * @author Petr Vogl
 * @param <T> Type of entity identifier (the primary key)
 */
public interface Entity<T extends Serializable> extends Serializable {

	/**
	 * Provides the identifier of an entity.
	 *
	 * @return Entity identifier
	 */
	T getId();

	/**
	 * Sets the identifier of an entity.
	 *
	 * @param id
	 */
	void setId(final T id);
}
