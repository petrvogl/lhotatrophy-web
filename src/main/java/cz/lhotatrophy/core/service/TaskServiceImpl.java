package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.TaskDao;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
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
		if (taskDao.findByCode(code).isPresent()) {
			throw new RuntimeException("Úkol s tímto kódem už existuje.");
		}
		if (taskDao.findByName(name).isPresent()) {
			throw new RuntimeException("Úkol s tímto názvem už existuje.");
		}
		return runInTransaction(() -> {
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
	}

	@Override
	public void updateTask(@NonNull final Task task) {
		final Long taskId = task.getId();
		Objects.requireNonNull(taskId, "Úkol nelze aktualizovat (v databazi neexistuje).");
		Objects.requireNonNull(task.getCode());
		Objects.requireNonNull(task.getName());
		// save updates
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
				// code has changed
				if (taskDao.findByCode(newCode).isPresent()) {
					throw new RuntimeException("Úkol s tímto kódem už existuje.");
				}
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
		}
	}
}
