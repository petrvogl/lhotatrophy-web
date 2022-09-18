package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Clue;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface ClueService extends Service {

	/**
	 * Creates and save a new clue.
	 *
	 * @param code Clue unique code
	 * @param description Description of a clue
	 * @return New clue instance
	 */
	Clue registerNewClue(@NonNull String code, String description);

	/**
	 * Saves updated state of a clue.
	 *
	 * @param clue Updated {@code Clue} entity
	 */
	void updateClue(@NonNull final Clue clue);

	/**
	 * Retrieves an entity by its ID from a persistent context.
	 *
	 * @param id Persisted {@code Clue} entity ID
	 * @return {@code Clue} object with the given ID or {@link  Optional#empty()}
	 * if none found
	 */
	Optional<Clue> getClueById(@NonNull Long id);

	/**
	 * Returns the {@link Clue} object associated with {@code id} in the cache.
	 * This method provides a simple substitute for the conventional "if cached,
	 * return; otherwise create, cache and return" pattern.
	 *
	 * @param id Persisted {@code Clue} entity ID
	 * @return Cached {@code Clue} object with the given ID or
	 * {@link Optional#empty()} if none found
	 */
	public Optional<Clue> getClueByIdFromCache(@NonNull Long id);

	/**
	 * Rerurns a list of {@link Clue} objects from the cache according to the
	 * listing query. This method provides a simple substitute for the
	 * conventional "if cached, return; otherwise create, cache and return"
	 * pattern.
	 *
	 * @param query Listing query
	 * @return Cached list of {@code Clue} objects
	 */
	List<Clue> getClueListing(@NonNull ClueListingQuerySpi query);

	/**
	 * Discards the cached {@link Clue} object if it is present in the cache.
	 *
	 * @param id Persisted {@code Clue} entity ID
	 */
	void removeClueFromCache(@NonNull Long id);
}
