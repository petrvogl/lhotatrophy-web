package cz.lhotatrophy.web.service;

import cz.lhotatrophy.core.service.TaskService;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.utils.EnumUtils;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Petr Vogl
 */
@Component
@Log4j2
public final class TaskViewServices {

	@Autowired
	private transient TaskService taskService;

	public Task getTaskRelatedToLocation(@NonNull final Location location, @NonNull final String taskType) {
		final Optional<TaskTypeEnum> optType = EnumUtils.decodeEnum(TaskTypeEnum.class, taskType);
		if (optType.isEmpty()) {
			return null;
		}
		return taskService.getTaskRelatedToLocation(location, optType.get()).orElse(null);
	}

	public Location getLocationRelatedToTask(@NonNull final Task task) {
		return taskService.getLocationRelatedToTask(task).orElse(null);
	}
}
