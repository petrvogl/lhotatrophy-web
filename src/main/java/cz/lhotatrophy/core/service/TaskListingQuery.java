package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.Objects;
import lombok.NonNull;

/**
 * The implementation of the Listing Query API for {@link Task} entities.
 *
 * @author Petr Vogl
 */
@SuppressWarnings("EqualsAndHashcode")
public class TaskListingQuery
		extends EntityListingQuery<Task, TaskListingQuerySpi>
		implements TaskListingQuerySpi {

	/**
	 * The criterion for the activity status.
	 */
	private Boolean active;
	/**
	 * The criterion for the task type.
	 */
	private TaskTypeEnum type;

	/**
	 * Constructor.
	 */
	public TaskListingQuery() {
		super(Task.class);
	}

	/**
	 * Copy constructor.
	 *
	 * @param other Listing query
	 */
	public TaskListingQuery(@NonNull final TaskListingQuerySpi other) {
		super(other);
		this.active = other.getActive();
		this.type = other.getType();
	}

	@Override
	public TaskListingQuerySpi createBaseListingQuery() {
		final TaskListingQuery baseQuery = new TaskListingQuery(this);
		// reset all criteria except of those which are the base
		baseQuery.active = null;
		baseQuery.type = null;
		// return base query
		return equals(baseQuery) ? null : baseQuery;
	}

	@Override
	public boolean check(final Task task) {
		if (!super.check(task)) {
			return false;
		}
		if (active != null && !active.equals(task.getActive())) {
			return false;
		}
		if (type != null && !type.equals(task.getType())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCodeInternal() {
		int hash = super.hashCodeInternal();
		hash = 89 * hash + Objects.hashCode(this.active);
		hash = 89 * hash + Objects.hashCode(this.type);
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		final boolean equals = super.equals(obj);
		if (!equals) {
			return false;
		}
		final TaskListingQuery other = (TaskListingQuery) obj;
		if (!Objects.equals(this.active, other.active)) {
			return false;
		}
		if (!Objects.equals(this.type, other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public Boolean getActive() {
		return active;
	}

	@Override
	public TaskListingQuerySpi setActive(final Boolean active) {
		this.active = active;
		resetHashCode();
		return this;
	}

	@Override
	public TaskTypeEnum getType() {
		return type;
	}

	@Override
	public TaskListingQuerySpi setType(final TaskTypeEnum type) {
		this.type = type;
		resetHashCode();
		return this;
	}
}
