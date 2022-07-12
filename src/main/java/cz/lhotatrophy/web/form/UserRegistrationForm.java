package cz.lhotatrophy.web.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author Petr Vogl
 */
@Data
public class UserRegistrationForm {

	@NotBlank(message = "E-mail je třeba vyplnit.")
	@Email(message = "E-mail nemá správný tvar.")
	@Size(max = 255, message = "E-mail smí mít nejvýše 255 znaků.")
	private String email;

	@NotBlank(message = "Heslo je třeba vyplnit.")
	@Size(min = 6, max = 255, message = "Heslo musí mít nejméně 6 znaků.")
	private String password;
}
