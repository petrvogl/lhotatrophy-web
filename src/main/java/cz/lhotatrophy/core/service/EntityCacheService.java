package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.EntityLongId;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.NonNull;

/**
 * This service provides support for individual entity caching as well as entity
 * list caching.
 *
 * @author Petr Vogl
 */
public interface EntityCacheService extends Service {

	/**
	 * Discards the cached entity if it is present in the cache.
	 *
	 * @param entity Entity instance
	 */
	void removeFromCache(@NonNull EntityLongId entity);

	/**
	 * Discards the cached entity if it is present in the cache.
	 *
	 * @param <T> Entity type
	 * @param id Entity ID
	 * @param cls Entity class
	 */
	<T extends EntityLongId> void removeFromCache(@NonNull Long id, @NonNull Class<T> cls);

	/**
	 * Discards the cached listing if it is present in the cache.
	 *
	 * @param listingQuery Listing query
	 */
	void removeFromCache(@NonNull EntityListingQuerySpi listingQuery);

	/**
	 * Discards all entities in the cache.
	 */
	void cleanEntityCache();

	/**
	 * Discards all listings in the cache.
	 */
	void cleanEntityListingCache();

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
	 * Rerurns a list of entity IDs from the cache according to the listing
	 * query, obtaining that list from IdsLoader if necessary. This method
	 * provides a simple substitute for the conventional "if cached, return;
	 * otherwise create, cache and return" pattern.
	 *
	 * @param <T> Entity type
	 * @param <Q> Query type
	 * @param listingQuery Listing query
	 * @param idsLoader Loader of IDs
	 * @return List of entity IDs
	 */
	<T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> List<Long> getEntityIdsListing(
			@NonNull Q listingQuery,
			@NonNull Function<Q, List<Long>> idsLoader
	);

	/**
	 * Rerurns a stream of entities from the cache according to the listing
	 * query, obtaining source IDs from IdsLoader if necessary. This method
	 * provides a simple substitute for the conventional "if cached, return;
	 * otherwise create, cache and return" pattern.
	 *
	 * @param <T> Entity type
	 * @param <Q> Query type
	 * @param listingQuery Listing query
	 * @param idsLoader Loader of IDs
	 * @return Stream of entities
	 */
	<T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> Stream<T> getEntityListingStream(
			@NonNull Q listingQuery,
			@NonNull Function<Q, List<Long>> idsLoader
	);

	/**
	 * Rerurns a list of entities from the cache according to the listing query,
	 * obtaining source IDs from IdsLoader if necessary. This method provides a
	 * simple substitute for the conventional "if cached, return; otherwise
	 * create, cache and return" pattern.
	 *
	 * @param <T> Entity type
	 * @param <Q> Query type
	 * @param listingQuery Listing query
	 * @param idsLoader Loader of IDs
	 * @return List of entities
	 */
	<T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> List<T> getEntityListing(
			@Nonnull Q listingQuery,
			@Nonnull Function<Q, List<Long>> idsLoader
	);
}
