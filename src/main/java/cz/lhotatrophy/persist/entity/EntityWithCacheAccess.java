package cz.lhotatrophy.persist.entity;

import java.util.function.BiFunction;

/**
 * An entity with access to the cache if it is retrieved from that cache. If the
 * entity is retrieved from the persistence context, access to the cache is not
 * available
 *
 * @author Petr Vogl
 */
public interface EntityWithCacheAccess {

	void setCachedEntityGetter(BiFunction<Long, Class<? extends EntityLongId>, ? extends EntityLongId> getter);
}
