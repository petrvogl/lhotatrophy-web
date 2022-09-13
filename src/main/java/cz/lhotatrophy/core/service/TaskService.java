package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
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
}
