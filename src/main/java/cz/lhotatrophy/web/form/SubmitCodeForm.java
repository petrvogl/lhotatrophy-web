package cz.lhotatrophy.web.form;

import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Petr Vogl
 */
@Data
public class SubmitCodeForm {

	private String taskType;
	private String taskCode;
	private String solution;

	public char getTypeMark() {
		if (taskType == null || taskType.length() != 1) {
			// invalid type mark
			return '\0';
		}
		return taskType.charAt(0);
	}

	public TaskTypeEnum getType() {
		return TaskTypeEnum.valueOf(getTypeMark());
	}

	public String getSolution() {
		return StringUtils.trimToNull(solution);
	}

	public String getTaskCode() {
		return StringUtils.trimToNull(taskCode);
	}

	public void setFrom(@NonNull final Task task) {
		taskType = String.valueOf(task.getType().getMark());
		taskCode = task.getCode();
	}
}
