package cz.lhotatrophy.persist.entity;

/**
 * Entity with {@link Long} ID.
 *
 * @author Petr Vogl
 * @param <E> Type of entity
 */
public interface EntityLongId<E extends Entity<Long, E>> extends Entity<Long, E> {

	/**
	 * Provides the identifier of an entity.
	 *
	 * @return Entity identifier
	 */
	@Override
	Long getId();
}
