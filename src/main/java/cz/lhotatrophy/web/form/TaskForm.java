package cz.lhotatrophy.web.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

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
}
