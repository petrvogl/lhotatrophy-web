package cz.lhotatrophy.web.form;

import cz.lhotatrophy.persist.entity.Clue;
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
public class ClueForm {

	@NotBlank(message = "Vyplň kódové označení")
	@Size(max = 32, message = "Maximální délka kódu je 32 znaků")
	private String code;

	@Size(max = 512, message = "Maximální popisu vodítka je 512 znaků")
	private String description;

	private Boolean active;

	public void setFrom(@NonNull final Clue clue) {
		code = clue.getCode();
		description = clue.getDescription();
		active = clue.getActive();
	}

	@Nonnull
	public Clue toClue() {
		final Clue clue = new Clue();
		clue.setActive(active);
		clue.setCode(code);
		clue.setDescription(description);
		return clue;
	}
}
