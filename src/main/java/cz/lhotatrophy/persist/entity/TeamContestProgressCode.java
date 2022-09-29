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
	 * Accepted solution.
	 */
	private String solution;
	/**
	 * Indicates that the solution hint was used.
	 */
	private boolean hintRevealed;
	/**
	 * Indicates that the solution procedure was used.
	 */
	private boolean procedureRevealed;
	/**
	 * Indicates that the right solution was revealed.
	 */
	private boolean solutionRevealed;
	/**
	 * Time of acceptance.
	 */
	private long ts;

	/**
	 * Indicates whether the code has been accepted.
	 *
	 * @return {@code true} if accepted
	 */
	public boolean accepted() {
		return ts > 0l && solution != null;
	}

	/**
	 * Checks whether the hint, procedure or solution has been revealed. Exactly
	 * one of the boolean arguments must be set to {@code true}.
	 *
	 * @param hint Check solution hint has been revealed
	 * @param procedure Check solution procedure has been revealed
	 * @param solution Check solution has been revealed
	 * @return {@code true} if revealed
	 */
	public boolean revealed(final boolean hint, final boolean procedure, final boolean solution) {
		// validate that exactly one of booleans is true
		if ((hint ? 1 : 0) + (procedure ? 1 : 0) + (solution ? 1 : 0) == 1) {
			throw new IllegalArgumentException("Only one of the arguments can be true.");
		}
		return (hint && hintRevealed) || (procedure && procedureRevealed) || (solution && solutionRevealed);
	}
}
