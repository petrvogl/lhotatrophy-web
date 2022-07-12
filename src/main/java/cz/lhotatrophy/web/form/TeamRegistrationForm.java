package cz.lhotatrophy.web.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Petr Vogl
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamRegistrationForm extends UserRegistrationForm {

	@NotBlank(message = "Jméno týmu je třeba vyplnit.")
	@Size(max = 40, message = "Jméno týmu může mít nejvýše 40 znaků.")
	private String teamName;

	@NotNull(message = "S podmínkami účasti je třeba souhlasit.")
	@AssertTrue(message = "S podmínkami účasti je třeba souhlasit.")
	private Boolean termsAgreement;
}
