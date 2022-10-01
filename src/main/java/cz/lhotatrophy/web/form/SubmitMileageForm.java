package cz.lhotatrophy.web.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Data;

/**
 *
 * @author Petr Vogl
 */
@Data
public class SubmitMileageForm {

	@NotBlank(message = "Vyplň stav tachometru")
	@Positive(message = "Vyplň kladné číslo")
	private Integer mileage;

	private String destCode;

	private String type;
}
