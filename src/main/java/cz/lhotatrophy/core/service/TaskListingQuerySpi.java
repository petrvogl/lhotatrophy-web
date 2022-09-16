package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;

/**
 * Listing Query API for defining a query to list {@link Task} entities.
 *
 * @author Petr Vogl
 */
public interface TaskListingQuerySpi extends EntityListingQuerySpi<Task, TaskListingQuerySpi> {

	/**
	 * This method is the factory method for instances of this interface default
	 * implementation.
	 *
	 * @return New instance of this interface default implementation
	 */
	static TaskListingQuerySpi create() {
		return new TaskListingQuery();
	}

	/**
	 * Returns the criterion value for the activity status. If it returns
	 * {@code null}, the criterion is not set.
	 */
	Boolean getActive();

	/**
	 * Sets the criterion for the activity status.
	 *
	 * @param active criterion value
	 * @return Listing query
	 */
	TaskListingQuerySpi setActive(Boolean active);

	/**
	 * Returns the criterion value for the task type. If it returns
	 * {@code null}, the criterion is not set.
	 */
	TaskTypeEnum getType();

	/**
	 * Sets the criterion for the task type.
	 *
	 * @param active criterion value
	 * @return Listing query
	 */
	TaskListingQuerySpi setType(TaskTypeEnum type);
}
