package cz.lhotatrophy.web.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Petr Vogl
 */
@Data
public class SubmitMileageForm {

	@NotNull(message = "Vyplň stav tachometru")
	@Positive(message = "Vyplň kladné číslo")
	private Integer mileage;

	private MultipartFile file;

	private String destCode;

	private String type;
}
