package cz.lhotatrophy.web.form;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Petr Vogl
 */
@Data
public class FileUploadForm {

	@NotNull(message = "Nahraj fotografii tachometru")
	private MultipartFile file;
}
