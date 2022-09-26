package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Location;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface LocationService extends Service {

	/**
	 * Creates and save a new contest location.
	 *
	 * @param code Location unique code
	 * @param name Location unique name
	 * @param description Accurate description of location
	 * @return New location instance
	 */
	Location registerNewLocation(
			@NonNull String code,
			@NonNull String name,
			String description
	);

	/**
	 * Saves updated state of contest location.
	 *
	 * @param location Updated {@code Location} entity
	 */
	void updateLocation(@NonNull final Location location);

	/**
	 * Retrieves an entity by its ID from a persistent context.
	 *
	 * @param id Persisted {@code Location} entity ID
	 * @return {@code Location} object with the given ID or
	 * {@link  Optional#empty()} if none found
	 */
	Optional<Location> getLocationById(@NonNull Long id);

	/**
	 * Returns the {@link Location} object associated with {@code id} in the
	 * cache. This method provides a simple substitute for the conventional "if
	 * cached, return; otherwise create, cache and return" pattern.
	 *
	 * @param id Persisted {@code Location} entity ID
	 * @return Cached {@code Location} object with the given ID or
	 * {@link Optional#empty()} if none found
	 */
	public Optional<Location> getLocationByIdFromCache(@NonNull Long id);

	/**
	 * Returns the {@link Location} object associated with {@code code} in the
	 * cache. This method provides a simple substitute for the conventional "if
	 * cached, return; otherwise create, cache and return" pattern.
	 *
	 * @param code Globally unique location code
	 * @return Cached {@code Location} object with the given code or
	 * {@link Optional#empty()} if none found
	 */
	Optional<Location> getLocationByCodeFromCache(@NonNull String code);

	/**
	 * Rerurns a list of {@link Location} objects from the cache according to
	 * the listing query. This method provides a simple substitute for the
	 * conventional "if cached, return; otherwise create, cache and return"
	 * pattern.
	 *
	 * @param query Listing query
	 * @return Cached list of {@code Location} objects
	 */
	List<Location> getLocationListing(@NonNull LocationListingQuerySpi query);

	/**
	 * Discards the cached {@link Location} object if it is present in the
	 * cache.
	 *
	 * @param id Persisted {@code Location} entity ID
	 */
	void removeLocationFromCache(@NonNull Long id);
}
