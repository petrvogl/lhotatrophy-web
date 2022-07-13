package cz.lhotatrophy.core.service;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author Petr Vogl
 */
@Data
@ToString
public final class TeamListingQuery implements Serializable {

	private Boolean active;
}
