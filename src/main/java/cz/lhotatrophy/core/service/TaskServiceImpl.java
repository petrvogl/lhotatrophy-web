package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.TaskDao;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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

	public Optional<Task> getTaskById(@NonNull final Long id) {
		return taskDao.findById(id);
	}

	public Optional<Task> getTaskByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, Task.class);
	}
	
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
				t.setSolutionHint(StringUtils.trimToNull(solutionHint));
				t.setSolutionProcedure(StringUtils.trimToNull(solutionProcedure));
				t.setRevealSolutionAllowed(revealSolutionAllowed);
				return t;
			});
			// logging
			log.info("New task has been registered: [ {} / {} ]", task.getCode(), task.getName());
			return task;
		});
	}

	public void updateTask(@NonNull final Task task) {
		final Task t = taskDao.save(task);
		removeTaskFromCache(t.getId());
	}
}
