package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.EntityLongId;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * This class acts as a runtime in-memory registry of relationships between {@link Task}
 * objects and {@link Location} objects.
 *
 * @author Petr Vogl
 */
@Log4j2
public final class TaskRelationships {

	/**
	 * Creates a runtime registry of relationships. The mapper
	 * {@code codeMapper} is used only at the time of registry construction.
	 *
	 * @param tasks Tasks stream
	 * @param codeMapper Function of mapping codes to data
	 * @return register instance
	 */
	static TaskRelationships create(
			@NonNull final Stream<Task> tasks,
			@NonNull final Function<String, Optional<EntityLongId>> codeMapper
	) {
		return new TaskRelationships(tasks, codeMapper);
	}

	private static <T extends EntityLongId> T getByCodeCached(
			final String code,
			final Class<T> expectedType,
			final Map<String, EntityLongId> localCache,
			final Function<String, Optional<EntityLongId>> valueLoader
	) {
		// null-safe
		if (code == null) {
			return null;
		}
		// first try the local cache
		final EntityLongId resultCached = localCache.get(code);
		if (resultCached != null) {
			// class check
			return expectedType.isInstance(resultCached)
					? (T) resultCached
					: null;
		}
		// if not cached then load, cache and finally return
		final Optional<EntityLongId> optResult = valueLoader.apply(code);
		if (optResult.isPresent()) {
			final EntityLongId resultLoaded = optResult.get();
			localCache.put(code, resultLoaded);
			// class check
			return expectedType.isInstance(resultLoaded)
					? (T) resultLoaded
					: null;
		}
		// not found
		return null;
	}

	private final Map<Long, Long> taskToLocation = new HashMap<>(32);
	private final Map<Long, Map<TaskTypeEnum, Set<Long>>> locationToTasks = new HashMap<>(32);

	private TaskRelationships(
			final Stream<Task> tasks,
			final Function<String, Optional<EntityLongId>> codeMapper
	) {
		final Map<String, EntityLongId> tempLocalCache = new HashMap<>(64);

		tasks.forEachOrdered(task -> {
			// main location relationships
			final Location mainLoc = getByCodeCached(task.getLocationCode(), Location.class, tempLocalCache, codeMapper);
			if (mainLoc != null) {
				// task to location many-to-one
				taskToLocation.put(task.getId(), mainLoc.getId());
				// location to task one-to-many
				final Map<TaskTypeEnum, Set<Long>> taskByType = Optional.ofNullable(locationToTasks.get(mainLoc.getId()))
						.or(() -> {
							// create and set
							final Map<TaskTypeEnum, Set<Long>> byType = new HashMap<>(3);
							locationToTasks.put(mainLoc.getId(), byType);
							return Optional.of(byType);
						}).get();
				Optional.ofNullable(taskByType.get(task.getType()))
						.or(() -> {
							// create and set
							final Set<Long> ids = new HashSet<>(2);
							taskByType.put(task.getType(), ids);
							return Optional.of(ids);
						})
						.get()
						.add(task.getId());
			}
		});
	}

	public Optional<Long> getLocationIdRelatedToTask(@NonNull final Task task) {
		return Optional.ofNullable(task.getId())
				.map(taskToLocation::get);
	}

	@Nonnull
	public Set<Long> getTaskIdsRelatedToLocation(@NonNull final Location location) {
		final Map<TaskTypeEnum, Set<Long>> map = locationToTasks.get(location.getId());
		if (map == null) {
			return Collections.emptySet();
		}
		final Set<Long> result = new HashSet<>(3);
		map.forEach((type, ids) -> result.addAll(ids));
		return result;
	}

	@Nonnull
	public Set<Long> getTaskIdsRelatedToLocation(@NonNull final Location location, final TaskTypeEnum type) {
		if (type == null) {
			return getTaskIdsRelatedToLocation(location);
		}
		final Map<TaskTypeEnum, Set<Long>> map = locationToTasks.get(location.getId());
		if (map == null) {
			return Collections.emptySet();
		}
		final Set<Long> result = map.get(type);
		return result != null ? result : Collections.emptySet();
	}
}
