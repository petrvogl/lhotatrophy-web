package cz.lhotatrophy.web.form;

import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
@Data
public class TaskForm {

	@NotBlank(message = "Vyber typ")
	@Size(max = 1, message = "Typ smí mít pouze jeden znak")
	private String type;

	@NotBlank(message = "Vyplň kódové označení")
	@Size(max = 32, message = "Maximální délka kódu je 32 znaků")
	private String code;

	@NotBlank(message = "Vyplň název")
	@Size(max = 255, message = "Maximální délka názvu je 255 znaků")
	private String name;

	@Size(max = 1024, message = "Maximální délka textu je 1000 znaků")
	private String solutions;

	@Size(max = 2048, message = "Maximální délka textu je 2000 znaků")
	private String solutionHint;

	@Size(max = 2048, message = "Maximální délka textu je 2000 znaků")
	private String solutionProcedure;

	private Boolean active;

	private Boolean revealSolutionAllowed;

	public char getTypeMark() {
		if (type == null || type.length() != 1) {
			// invalid type mark
			return '\0';
		}
		return type.charAt(0);
	}

	public void setFrom(@NonNull final Task task) {
		type = Optional.ofNullable(task.getType())
				.map(TaskTypeEnum::getMark)
				.map(String::valueOf)
				.orElse(null);
		code = task.getCode();
		name = task.getName();
		solutions = task.getSolutionsString();
		solutionHint = task.getSolutionHint();
		solutionProcedure = task.getSolutionProcedure();
		active = task.getActive();
		revealSolutionAllowed = task.getRevealSolutionAllowed();
	}

	@Nonnull
	public Task toTask() {
		final Task task = new Task();
		task.setType(TaskTypeEnum.valueOf(getTypeMark()));
		task.setActive(active);
		task.setCode(code);
		task.setName(name);
		task.setSolutionHint(solutionHint);
		task.setSolutionProcedure(solutionProcedure);
		task.setSolutionsString(solutions);
		task.setRevealSolutionAllowed(revealSolutionAllowed);
		return task;
	}
}
