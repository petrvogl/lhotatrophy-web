package cz.lhotatrophy.web.form;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Petr Vogl
 */
@Data
public class HintRevealForm {

	private String taskCode;
	private String type;

	public String getTaskCode() {
		return StringUtils.trimToNull(taskCode);
	}

	public String getType() {
		return StringUtils.trimToNull(type);
	}
}
