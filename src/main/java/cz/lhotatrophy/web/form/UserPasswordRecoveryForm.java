package cz.lhotatrophy.web.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author Petr Vogl
 */
@Data
public class UserPasswordRecoveryForm {

	@NotBlank(message = "Heslo je třeba vyplnit.")
	@Size(min = 6, max = 255, message = "Heslo musí mít nejméně 6 znaků.")
	private String password;

	private Long id;
	private String token;
}
