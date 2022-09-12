package cz.lhotatrophy.core.service;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgress;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class ContestServiceImpl extends AbstractService implements ContestService {

	// TODO - get this from a service
	private static final int A_CODES_COUNT = 9;
	private static final int B_CODES_COUNT = 9;

	@Autowired
	private transient ApplicationConfig appConfig;

	/**
	 * Gets the current team's score. The score is calculated and measured in
	 * kilometers driven from the start to the finish - the lower the mileage
	 * the better.
	 *
	 * @param contestProgress Results achieved in the game
	 * @return current team's score
	 */
	public int calculateScore(final TeamContestProgress contestProgress) {
		if (contestProgress == null || contestProgress.isDisqualified()) {
			// no progress yet or disqualified
			return Integer.MAX_VALUE;
		}
		// constants
		final Instant completionLimitInstant = appConfig.getCompletionLimitInstant();
		final long maxOverLimitSeconds = appConfig.getMaxOverLimitSeconds();
		final Instant timeAtFinishInstant = (contestProgress.getTimestampAtFinish() == null)
				? null
				: Instant.ofEpochMilli(contestProgress.getTimestampAtFinish())
						.atZone(ZoneId.systemDefault())
						.toInstant();
		final Duration timeLimitExceededDuration;
		if (timeAtFinishInstant != null && timeAtFinishInstant.isAfter(completionLimitInstant)) {
			// the team has finished the game after time limit
			timeLimitExceededDuration = Duration.ofMillis(ChronoUnit.MILLIS.between(completionLimitInstant, timeAtFinishInstant));
			if (timeLimitExceededDuration.getSeconds() >= maxOverLimitSeconds) {
				// team is disqualified
				return Integer.MAX_VALUE;
			}
		} else {
			if (timeAtFinishInstant != null) {
				// the team has finished the game before time limit
				timeLimitExceededDuration = Duration.ZERO;
			} else {
				// the team is still in the game
				final Instant now = Instant.now()
						.atZone(ZoneId.systemDefault())
						.toInstant();
				if (now.isAfter(completionLimitInstant)) {
					timeLimitExceededDuration = Duration.ofMillis(ChronoUnit.MILLIS.between(completionLimitInstant, now));
					if (timeLimitExceededDuration.getSeconds() >= maxOverLimitSeconds) {
						// team is disqualified
						return Integer.MAX_VALUE;
					}
				} else {
					timeLimitExceededDuration = Duration.ZERO;
				}
			}
		}
		// score accumulator variable
		int totalMileage;
		if (contestProgress.getMileageAtStart() != null && contestProgress.getMileageAtFinish() != null) {
			// the team has finished the game
			totalMileage = contestProgress.getMileageAtFinish() - contestProgress.getMileageAtStart();
		} else {
			// the team is still in the game
			totalMileage = appConfig.getIdealRouteLength();
		}
		// include extra penalty
		if (contestProgress.getExtraMileagePenalty() != null) {
			totalMileage += contestProgress.getExtraMileagePenalty();
		}
		// include extra bonus
		if (contestProgress.getExtraMileageBonus() != null) {
			totalMileage -= contestProgress.getExtraMileageBonus();
		}
		// include the cost of insurance
		if (contestProgress.isInsuranceAgainstWinning()) {
			totalMileage += appConfig.getInsurancePrice();
		}
		// include the results achieved in the game
		int ACodesAcquiredCount = 0;
		int BCodesAcquiredCount = 0;
		for (final TeamContestProgressCode code : contestProgress.getContestCodes().values()) {
			switch (code.getGroup()) {
				case "A":
					// penalty for hints
					totalMileage += code.isHintRevealed() ? appConfig.getHintRevealedPenalty() : 0;
					totalMileage += code.isProcedureRevealed() ? appConfig.getProcedureRevealedPenalty() : 0;
					totalMileage += code.isSolutionRevealed() ? appConfig.getSolutionRevealedPenalty() : 0;
					if (code.getTs() > 0) {
						ACodesAcquiredCount++;
					}
				case "B":
					if (code.getTs() > 0) {
						BCodesAcquiredCount++;
					}
					break;
				case "C":
					// bonus for C-code
					totalMileage -= appConfig.getCCodeAcquiredBonus();
					break;
			}
		}
		// penalty for missing codes
		totalMileage += (A_CODES_COUNT - ACodesAcquiredCount) * appConfig.getACodeMissingPenalty();
		totalMileage += (B_CODES_COUNT - BCodesAcquiredCount) * appConfig.getBCodeMissingPenalty();
		// penalty for exceeding the time limit
		if (!timeLimitExceededDuration.isZero()) {
			final long penaltyPerMinute = appConfig.getPenaltyPerMinute();
			totalMileage += (timeLimitExceededDuration.getSeconds() / 60) * penaltyPerMinute + penaltyPerMinute;
		}
		// total mileage
		return totalMileage;
	}

	/**
	 * Gets the current team's position on the leaderboard.
	 *
	 * @param team Competing team
	 * @return Team's position on the leaderboard
	 */
	public int getTeamPlacement(@NonNull final Team team) {
		return 0;
	}

	/**
	 * Gets the current team standings on the leaderboard.
	 *
	 * @return Current team standings
	 */
	public List<Team> getTeamStandings() {
		return new LinkedList<>();
	}
}
