package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.EntityLongId;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface TaskService extends Service {

	/**
	 * Creates and save a new contest task.
	 *
	 * @param type Task type
	 * @param code Task unique code
	 * @param name Task unique name
	 * @param solutions All task solutions
	 * @param solutionHint Solution hint
	 * @param solutionProcedure Solution procedure
	 * @param revealSolutionAllowed Whether the solution can be revealed
	 * @return New task instance
	 */
	Task registerNewTask(
			@NonNull TaskTypeEnum type,
			@NonNull String code,
			@NonNull String name,
			@NonNull String solutions,
			String solutionHint,
			String solutionProcedure,
			boolean revealSolutionAllowed
	);

	/**
	 * Saves updated state of contest task.
	 *
	 * @param task Updated {@code Task} entity
	 */
	void updateTask(@NonNull final Task task);

	/**
	 * Retrieves an entity by its ID from a persistent context.
	 *
	 * @param id Persisted {@code Task} entity ID
	 * @return {@code Task} object with the given ID or {@link  Optional#empty()}
	 * if none found
	 */
	Optional<Task> getTaskById(@NonNull Long id);

	/**
	 * Returns the {@link Task} object associated with {@code id} in the cache.
	 * This method provides a simple substitute for the conventional "if cached,
	 * return; otherwise create, cache and return" pattern.
	 *
	 * @param id Persisted {@code Task} entity ID
	 * @return Cached {@code Task} object with the given ID or
	 * {@link Optional#empty()} if none found
	 */
	public Optional<Task> getTaskByIdFromCache(@NonNull Long id);

	/**
	 * Returns the {@link Task} object associated with {@code code} in the
	 * cache. This method provides a simple substitute for the conventional "if
	 * cached, return; otherwise create, cache and return" pattern.
	 *
	 * @param code Globally unique task code
	 * @return Cached {@code Task} object with the given code or
	 * {@link Optional#empty()} if none found
	 */
	Optional<Task> getTaskByCodeFromCache(@NonNull String code);

	/**
	 * Rerurns a list of {@link Task} objects from the cache according to the
	 * listing query. This method provides a simple substitute for the
	 * conventional "if cached, return; otherwise create, cache and return"
	 * pattern.
	 *
	 * @param query Listing query
	 * @return Cached list of {@code Task} objects
	 */
	List<Task> getTaskListing(@NonNull TaskListingQuerySpi query);

	/**
	 * Rerurns a stream of {@link Task} objects from the cache according to the
	 * listing query. This method provides a simple substitute for the
	 * conventional "if cached, return; otherwise create, cache and return"
	 * pattern.
	 *
	 * @param query Listing query
	 * @return Cached stream of {@code Task} objects
	 */
	Stream<Task> getTaskListingStream(@NonNull TaskListingQuerySpi query);

	/**
	 * Discards the cached {@link Task} object if it is present in the cache.
	 *
	 * @param id Persisted {@code Task} entity ID
	 */
	void removeTaskFromCache(@NonNull Long id);

	/**
	 * Invalidates the in-memory registry of task relationships.
	 */
	void invalidateTaskRelationshipsCache();

	@Nonnull
	Optional<Location> getLocationRelatedToTask(@NonNull Task task);

	@Nonnull
	Optional<Task> getTaskRelatedToLocation(@NonNull Location location, @NonNull TaskTypeEnum type);

	@Nonnull
	Stream<Task> getTasksRelatedToLocationStream(@NonNull Location location, TaskTypeEnum type);

	@Nonnull
	Stream<EntityLongId> getTaskRewardsStream(@NonNull Task task);
}
