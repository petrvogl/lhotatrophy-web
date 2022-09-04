package cz.lhotatrophy.persist.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * This class represents team's progress in the competition. It collects all the
 * data needed to calculate the position on the scoreboard. It can also be used
 * as a key to unlock some game elements or items.
 *
 * @author Petr Vogl
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TeamContestProgress implements Serializable {

	/**
	 * Informs whether the team has been disqualified.
	 */
	private boolean disqualified;
	/**
	 * Informs whether the team has insurance which protects from winning the
	 * first prize.
	 */
	private boolean insuranceAgainstWinning;
	/**
	 * The mileage state on the car when the game has started (km unit).
	 */
	private Integer mileageAtStart;
	/**
	 * The mileage state on the car when the team finished the game (km unit).
	 */
	private Integer mileageAtFinish;
	/**
	 * Penalty for breaking the rules of the game (km unit).
	 */
	private Integer extraMileagePenalty;
	/**
	 * Bonus as a compensation from the organizer (km unit).
	 */
	private Integer extraMileageBonus;
	/**
	 * The exact timestamp the team finished the game.
	 */
	private Long timestampAtFinish;
	/**
	 * Records of accepted contest codes. The key of a map is the accepted code
	 * and the mapped value is the detailed information about the acceptance.
	 */
	private final Map<String, TeamContestProgressCode> contestCodes = new HashMap<>();

	/**
	 * Checks if the code was acquired by the team.
	 *
	 * @param code Contest code to check
	 * @return {@code true} if code was acquired; {@code false} otherwise
	 */
	public boolean containsContestCode(final String code) {
		return contestCodes.containsKey(code);
	}

	/**
	 * Gets the code acquired by the team.
	 *
	 * @param code Contest code
	 * @return Code if acquired; {@code null} otherwise
	 */
	public TeamContestProgressCode getContestCode(final String code) {
		return contestCodes.get(code);
	}

	/**
	 * It records that the code has been accepted.
	 *
	 * @param code Contest code
	 * @param group Group the code belongs to
	 * @param timestamp Time of acceptance
	 */
	public void addContestCode(
			@NonNull final String code,
			@NonNull final String group,
			long timestamp
	) {
		if (timestamp <= 0l) {
			throw new IllegalArgumentException("Time of code acceptance must be positive.");
		}
		final TeamContestProgressCode c = new TeamContestProgressCode(code, group, timestamp);
		contestCodes.put(code, c);
	}
}
