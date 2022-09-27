package cz.lhotatrophy.persist.entity;

import java.util.function.BiFunction;

/**
 * This interface defines an entity with access to the cache. This capability is
 * only available if the entity is loaded from that cache. If the entity is
 * retrieved from the persistence context, access to the cache is not available.
 *
 * @author Petr Vogl
 */
public interface EntityWithCacheAccess {

	/**
	 * Sets an entity get-from-cache function. This function is intended to be
	 * used by an entity to load related entities from the cache.
	 *
	 * @param getter Get-from-cache function
	 */
	void setCachedEntityGetter(BiFunction<Long, Class<? extends EntityLongId>, ? extends EntityLongId> getter);
}
