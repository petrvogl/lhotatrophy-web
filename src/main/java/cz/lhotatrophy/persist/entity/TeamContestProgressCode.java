package cz.lhotatrophy.persist.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The record of contest code acceptance. It reports on the completion of the
 * task.
 * 
 * @author Petr Vogl
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TeamContestProgressCode implements Serializable {

	/**
	 * Identifier of the contest code.
	 */
	private String code;
	/**
	 * Identifier of the group the code belongs to.
	 */
	private String group;
	/**
	 * Time of acceptance.
	 */
	private long ts;
}