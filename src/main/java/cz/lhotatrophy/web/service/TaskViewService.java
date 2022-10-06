package cz.lhotatrophy.web.service;

import cz.lhotatrophy.core.service.EntityCacheService;
import cz.lhotatrophy.core.service.TaskListingQuerySpi;
import cz.lhotatrophy.core.service.TaskService;
import cz.lhotatrophy.persist.entity.EntityLongId;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.utils.EnumUtils;
import java.util.Iterator;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code ${service.task.method()}}
 *
 * @author Petr Vogl
 */
@Component
@Log4j2
public final class TaskViewService {

	@Autowired
	private transient EntityCacheService cacheService;
	@Autowired
	private transient TaskService taskService;

	/**
	 * {@code ${service.task.getTaskRelatedToLocation(location, taskType)}}
	 */
	public Task getTaskRelatedToLocation(@NonNull final Location location, @NonNull final String taskType) {
		final Optional<TaskTypeEnum> optType = EnumUtils.decodeEnum(TaskTypeEnum.class, taskType);
		if (optType.isEmpty()) {
			return null;
		}
		return taskService.getTaskRelatedToLocation(location, optType.get()).orElse(null);
	}

	/**
	 * {@code ${service.task.getLocationRelatedToTask(task)}}
	 */
	public Location getLocationRelatedToTask(@NonNull final Task task) {
		return taskService.getLocationRelatedToTask(task).orElse(null);
	}

	/**
	 * {@code ${service.task.getTaskRewards(task)}}
	 */
	public Iterator<EntityLongId> getTaskRewards(@NonNull final Task task) {
		return task.getRewardCodesStream()
				.map(cacheService::getEntityByCode)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.iterator();
	}

	/**
	 * {@code ${service.task.getDestinationTask()}}
	 */
	public Task getDestinationTask() {
		return taskService.getTaskByCodeFromCache("D0").get();
	}

	/**
	 * {@code ${service.task.getAllActiveTasks()}}
	 */
	public Iterator<Task> getAllActiveTasks() {
		final TaskListingQuerySpi query = TaskListingQuerySpi.create()
				.setActive(Boolean.TRUE)
				.setSorting(Task.orderByCode());
		return taskService.getTaskListingStream(query).iterator();
	}

	/**
	 * {@code ${service.task.getAllActiveTasks(taskType)}}
	 */
	public Iterator<Task> getAllActiveTasks(@NonNull final String taskType) {
		final Optional<TaskTypeEnum> optType = EnumUtils.decodeEnum(TaskTypeEnum.class, taskType);
		if (optType.isEmpty()) {
			return null;
		}
		final TaskListingQuerySpi query = TaskListingQuerySpi.create()
				.setActive(Boolean.TRUE)
				.setType(optType.get())
				.setSorting(Task.orderByCode());
		return taskService.getTaskListingStream(query).iterator();
	}
}
