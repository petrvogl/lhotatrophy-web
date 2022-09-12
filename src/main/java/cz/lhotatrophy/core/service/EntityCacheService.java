package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.EntityLongId;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface EntityCacheService extends Service {

	/**
	 * Returns the entity associated with {@code id} in the cache, obtaining
	 * that entity from EntityLoader if necessary. This method provides a simple
	 * substitute for the conventional "if cached, return; otherwise create,
	 * cache and return" pattern.
	 *
	 * @param <T> Entity type
	 * @param id Entity ID
	 * @param cls Entity class
	 * @return The entity associated with {@code id}
	 */
	@Nonnull
	<T extends EntityLongId> Optional<T> getEntityById(@NonNull Long id, @NonNull Class<T> cls);

	/**
	 * Discards the cached entity if it is present in the cache.
	 *
	 * @param entity Entity instance
	 */
	void invalidateCacheEntry(@NonNull EntityLongId entity);

	/**
	 * Discards the cached entity if it is present in the cache.
	 *
	 * @param <T> Entity type
	 * @param id Entity ID
	 * @param cls Entity class
	 */
	<T extends EntityLongId> void invalidateCacheEntry(@NonNull Long id, @NonNull Class<T> cls);
}
