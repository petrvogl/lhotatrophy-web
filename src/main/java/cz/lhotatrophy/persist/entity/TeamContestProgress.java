package cz.lhotatrophy.persist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
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
	 * Indicates that the exact destination location was revealed.
	 */
	private boolean destinationRevealed;
	/**
	 * Solution at destination location.
	 */
	private String destinationSolution;
	/**
	 * Records of accepted contest codes. The key of a map is the accepted code
	 * and the mapped value is the detailed information about the acceptance.
	 */
	private final Map<String, TeamContestProgressCode> contestCodes = new HashMap<>();
	/**
	 * Records of NOT accepted contest codes (wrong attempts). The key of a map
	 * is a task code (or group) and the mapped value is the list of unaccepted
	 * solutions to the task.
	 */
	private final Map<String, List<String>> invalidCodes = new HashMap<>();

	/**
	 * The mileage driven from the start to the finish (km unit).
	 */
	@JsonIgnore
	public int getMileage() {
		if (mileageAtStart == null || mileageAtFinish == null) {
			return 0;
		}
		return mileageAtFinish - mileageAtStart;
	}

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
	 * Checks if the solution was accepted in the specified group.
	 *
	 * @param solution Solution to check
	 * @param group Group the solution belongs to
	 * @return {@code true} if solution was accepted; {@code false} otherwise
	 */
	public boolean containsSolution(final String solution, final String group) {
		if (contestCodes.isEmpty()) {
			return false;
		}
		return contestCodes.values().stream()
				.filter(progressCode -> progressCode.getGroup().equals(group))
				.filter(progressCode -> progressCode.getSolution().equals(solution))
				.findAny()
				.isPresent();
	}

	/**
	 * Returns the code acquired by the team.
	 *
	 * @param code Contest code
	 * @return Code if acquired; {@code null} otherwise
	 */
	public TeamContestProgressCode getContestCode(final String code) {
		return contestCodes.get(code);
	}

	/**
	 * Returns all codes acquired by the team.
	 *
	 * @return All acquired codes
	 */
	@JsonIgnore
	public Stream<TeamContestProgressCode> getAllAcquiredCodes() {
		return contestCodes.values().stream()
				.filter(TeamContestProgressCode::accepted);
	}

	/**
	 * It records that the code has been accepted and indicates that none of the
	 * available hints were used.
	 *
	 * @param code Contest code
	 * @param group Group the code belongs to
	 * @param solution Accepted solution
	 * @param timestamp Time of acceptance
	 * @return the new record
	 */
	public TeamContestProgressCode addContestCode(
			@NonNull final String code,
			@NonNull final String group,
			@NonNull final String solution,
			final long timestamp
	) {
		return addContestCode(code, group, solution, false, false, false, timestamp);
	}

	/**
	 * It records that some progress has been made in acquiring the code.
	 *
	 * @param code Contest code
	 * @param group Group the code belongs to
	 * @param hintRevealed Indicates that the solution hint was used
	 * @param procedureRevealed Indicates that the solution procedure was used
	 * @param solutionRevealed Indicates that the right solution was revealed
	 * @return the new record
	 */
	public TeamContestProgressCode addContestCode(
			@NonNull final String code,
			@NonNull final String group,
			final boolean hintRevealed,
			final boolean procedureRevealed,
			final boolean solutionRevealed
	) {
		return addContestCode(code, group, null, hintRevealed, procedureRevealed, solutionRevealed, 0l);
	}

	/**
	 * It records that some progress has been made in acquiring the code.
	 *
	 * @param code Contest code
	 * @param group Group the code belongs to
	 * @param solution Accepted solution
	 * @param hintRevealed Indicates that the solution hint was used
	 * @param procedureRevealed Indicates that the solution procedure was used
	 * @param solutionRevealed Indicates that the right solution was revealed
	 * @param timestamp Time of acceptance
	 * @return the new record
	 */
	public TeamContestProgressCode addContestCode(
			@NonNull final String code,
			@NonNull final String group,
			final String solution,
			final boolean hintRevealed,
			final boolean procedureRevealed,
			final boolean solutionRevealed,
			final long timestamp
	) {
		if (timestamp < 0l) {
			throw new IllegalArgumentException("Time of code acceptance must not be negative.");
		}
		final TeamContestProgressCode c = new TeamContestProgressCode(code, group, solution, hintRevealed, procedureRevealed, solutionRevealed, timestamp);
		contestCodes.put(code, c);
		return c;
	}

	/**
	 * Returns the list of all invalid solutions related to the specified
	 * task/code.
	 *
	 * @param code Contest code
	 * @return the list of all invalid solutions
	 */
	@Nonnull
	public List<String> getInvalidCodes(final String code) {
		final List<String> invalid = invalidCodes.get(code);
		return invalid == null ? Collections.emptyList() : invalid;
	}

	/**
	 * It records that the solution has NOT been accepted.
	 *
	 * @param code Contest code
	 * @param solution Invalid solution
	 * @return the list of all invalid solutions related to the specified code
	 */
	public List<String> addInvalidCode(
			@NonNull final String code,
			@NonNull final String solution
	) {
		List<String> invalid = invalidCodes.get(code);
		if (invalid == null) {
			invalid = new LinkedList<>();
			invalidCodes.put(code, invalid);
		}
		invalid.add(solution);
		return invalid;
	}
}
