package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.TaskDao;
import cz.lhotatrophy.persist.entity.EntityLongId;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class TaskServiceImpl extends AbstractService implements TaskService {

	@Autowired
	private transient TaskDao taskDao;
	@Autowired
	private transient EntityCacheService cacheService;

	/**
	 * Reference to the register of relationships between {@link Task} objects
	 * and other objects.
	 */
	private static final AtomicReference<TaskRelationships> taskRelationshipsRef = new AtomicReference<>();

	@NonNull
	public Task createTask(@NonNull final UnaryOperator<Task> initializer) {
		return runInTransaction(() -> {
			return taskDao.save(
					// custom init
					initializer.apply(new Task())
			);
		});
	}

	@Override
	public Optional<Task> getTaskById(@NonNull final Long id) {
		return taskDao.findById(id);
	}

	@Override
	public Optional<Task> getTaskByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, Task.class);
	}

	@Override
	public void invalidateTaskRelationshipsCache() {
		// volatile access synchronization
		taskRelationshipsRef.set(null);
	}

	/**
	 * Returns the in-memory registry of task relationships.
	 */
	@Nonnull
	private TaskRelationships getTaskRelationships() {
		{
			// volatile access synchronization
			final TaskRelationships register = taskRelationshipsRef.get();
			if (register != null) {
				return register;
			}
		}
		// Register construction is synchronized on register reference.
		// It prevents threads blocking on #resetRelationshipsRegister().
		synchronized (taskRelationshipsRef) {
			// double check
			final TaskRelationships register = taskRelationshipsRef.get();
			if (register != null) {
				return register;
			}
			// load the current state from the database
			final TaskListingQuerySpi query = TaskListingQuerySpi.create()
					.setUseQueryCache(false);
			final TaskRelationships newRegister = runInTransaction(() -> {
				final Stream<Task> tasks = getTaskListingStream(query);
				return TaskRelationships.create(tasks, cacheService::getEntityByCode);
			});
			// volatile access synchronization
			taskRelationshipsRef.set(newRegister);
			return newRegister;
		}
	}

	@Nonnull
	@Override
	public Optional<Location> getLocationRelatedToTask(@NonNull final Task task) {
		return getTaskRelationships().getLocationIdRelatedToTask(task)
				.flatMap(id -> cacheService.getEntityById(id, Location.class));
	}

	@Nonnull
	@Override
	public Optional<Task> getTaskRelatedToLocation(@NonNull final Location location, @NonNull final TaskTypeEnum type) {
		// Here should be only one task of each type present
		// Inconsistencies are ignored
		return getTasksRelatedToLocationStream(location, type).findFirst();
	}

	@Nonnull
	@Override
	public Stream<Task> getTasksRelatedToLocationStream(@NonNull final Location location, final TaskTypeEnum type) {
		return getTaskRelationships().getTaskIdsRelatedToLocation(location, type).stream()
				.map(id -> cacheService.getEntityById(id, Task.class))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	@Nonnull
	@Override
	public Stream<EntityLongId> getTaskRewardsStream(@NonNull final Task task) {
		return task.getRewardCodes().stream()
				.map(code -> cacheService.getEntityByCode(code))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(EntityLongId.class::cast);
	}

	/**
	 * Retuns default loader of IDs.
	 *
	 * @return IDs loader
	 */
	private Function<TaskListingQuerySpi, List<Long>> getDefaultIdsLoader() {
		return (listingQuery) -> {
			// The base listing query always targets all saved tasks;
			// no filtration is needed
			return taskDao.findAllIds();
		};
	}

	@Override
	public List<Task> getTaskListing(@NonNull final TaskListingQuerySpi query) {
		return cacheService.getEntityListing(query, getDefaultIdsLoader());
	}

	public Stream<Task> getTaskListingStream(@NonNull final TaskListingQuerySpi query) {
		return cacheService.getEntityListingStream(query, getDefaultIdsLoader());
	}

	@Override
	public void removeTaskFromCache(@NonNull final Long id) {
		cacheService.removeFromCache(id, Task.class);
	}

	@Override
	public Task registerNewTask(
			@NonNull final TaskTypeEnum type,
			@NonNull final String code,
			@NonNull final String name,
			@NonNull final String solutions,
			final String solutionHint,
			final String solutionProcedure,
			final boolean revealSolutionAllowed
	) {
		// globally unique code check relies on up-to-date register
		if (cacheService.getEntityByCode(code).isPresent()) {
			throw new RuntimeException("Entita s tímto kódem už existuje.");
		}
		if (taskDao.findByName(name).isPresent()) {
			throw new RuntimeException("Úkol s tímto názvem už existuje.");
		}
		final Task newTask = runInTransaction(() -> {
			final Task task = createTask(t -> {
				t.setActive(true);
				t.setType(type);
				t.setCode(code);
				t.setName(name);
				t.setSolutionsString(solutions);
				t.setSolutionHint(solutionHint);
				t.setSolutionProcedure(solutionProcedure);
				t.setRevealSolutionAllowed(revealSolutionAllowed);
				return t;
			});
			// logging
			log.info("New task has been registered: [ {} / {} ]", task.getCode(), task.getName());
			return task;
		});
		// keep GlobalCodeRegister up-to-date
		cacheService.resetGlobalCodeRegister();
		// discard all cached relationships
		invalidateTaskRelationshipsCache();
		return newTask;
	}

	@Override
	public void updateTask(@NonNull final Task task) {
		final Long taskId = task.getId();
		Objects.requireNonNull(taskId, "Úkol nelze aktualizovat (v databazi neexistuje).");
		Objects.requireNonNull(task.getCode());
		Objects.requireNonNull(task.getName());
		// save updates
		final MutableBoolean codeHasChanged = new MutableBoolean(false);
		final Task t = runInTransaction(() -> {
			final Task originalTask = taskDao.findById(taskId).orElse(null);
			if (originalTask == null) {
				throw new RuntimeException("Úkol nelze aktualizovat (v databazi neexistuje).");
			}
			if (originalTask.equalsAllProperties(task)) {
				// no changes to the task
				return task;
			}
			final String newCode = task.getCode();
			if (!newCode.equals(originalTask.getCode())) {
				// globally unique code check relies on up-to-date register
				if (cacheService.getEntityByCode(newCode).isPresent()) {
					throw new RuntimeException("Entita s tímto kódem už existuje.");
				}
				// code has changed
				codeHasChanged.setTrue();
				originalTask.setCode(newCode);
			}
			final String newName = task.getName();
			if (!newName.equals(originalTask.getName())) {
				// name has changed
				if (taskDao.findByName(newName).isPresent()) {
					throw new RuntimeException("Úkol s tímto názvem už existuje.");
				}
				originalTask.setName(newName);
			}
			originalTask.setActive(task.getActive());
			originalTask.setType(task.getType());
			originalTask.setSolutionHint(task.getSolutionHint());
			originalTask.setSolutionProcedure(task.getSolutionProcedure());
			originalTask.setSolutionsString(task.getSolutionsString());
			originalTask.setRevealSolutionAllowed(task.getRevealSolutionAllowed());
			originalTask.setLocationCode(task.getLocationCode());
			originalTask.setRewardCodesString(task.getRewardCodesString());
			return taskDao.save(originalTask);
		});
		if (t != task) {
			// changes have been made
			removeTaskFromCache(t.getId());
			// keep GlobalCodeRegister up-to-date
			if (codeHasChanged.isTrue()) {
				cacheService.resetGlobalCodeRegister();
			}
			// discard all cached relationships
			invalidateTaskRelationshipsCache();
		}
	}
}
