package cz.lhotatrophy.core.service;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgress;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import cz.lhotatrophy.utils.TextUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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

	@Autowired
	private transient ApplicationConfig appConfig;
	@Autowired
	private transient TaskService taskService;
	@Autowired
	private transient TeamService teamService;

	private int getContestTaskCount(final TaskTypeEnum type) {
		final TaskListingQuerySpi query = TaskListingQuerySpi.create()
				.setActive(Boolean.TRUE)
				.setType(type);
		return taskService.getTaskListing(query).size();
	}

	@Override
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
		totalMileage += (getContestTaskCount(TaskTypeEnum.A_CODE) - ACodesAcquiredCount) * appConfig.getACodeMissingPenalty();
		totalMileage += (getContestTaskCount(TaskTypeEnum.B_CODE) - BCodesAcquiredCount) * appConfig.getBCodeMissingPenalty();
		// penalty for exceeding the time limit
		if (!timeLimitExceededDuration.isZero()) {
			final long penaltyPerMinute = appConfig.getPenaltyPerMinute();
			totalMileage += (timeLimitExceededDuration.getSeconds() / 60) * penaltyPerMinute + penaltyPerMinute;
		}
		// total mileage
		return totalMileage;
	}

	private String normalizeSolution(final String solution) {
		return TextUtils.slugify(solution);
	}

	@Override
	public boolean acceptSolution(
			@NonNull final String solution,
			@NonNull final TaskTypeEnum taskType,
			@NonNull final Team team
	) {
		final TaskListingQuerySpi query = TaskListingQuerySpi.create()
				.setActive(Boolean.TRUE)
				.setType(taskType);
		final Task task = taskService.getTaskListingStream(query)
				.filter(t -> t.getSolutionsNormalized().contains(normalizeSolution(solution)))
				.findFirst()
				.orElse(null);
		if (task == null) {
			// incorrect solution
			log.info("Solution \'{}\' NOT accepted: team=[{}] taskType=[{}]", solution, team.getId(), taskType.name());
			return false;
		}
		return acceptSolutionInternal(solution, task, team);
	}

	@Override
	public boolean acceptSolution(
			@NonNull final String solution,
			@NonNull final Task task,
			@NonNull final Team team
	) {
		return acceptSolutionInternal(solution, task, team);
	}

	/**
	 * Verifies the solution and records if the solution to the task is
	 * accepted.
	 *
	 * @param solution Solution
	 * @param task Task
	 * @param team Team
	 * @return {@code true} if solution is accepted
	 */
	private boolean acceptSolutionInternal(
			final String solution,
			final Task task,
			final Team team
	) {
		if (team.isDisqualified()) {
			return false;
		}
		final String _solution = normalizeSolution(solution);
		if (!task.getSolutionsNormalized().contains(_solution)) {
			// incorrect solution
			log.info("Solution \'{}\' NOT accepted: team=[{}] task=[{}]", solution, team.getId(), task.getCode());
			return false;
		}
		// update contest progress
		final TeamContestProgressCode c = runInTransaction(() -> {
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			TeamContestProgressCode contestCode = contestProgress.getContestCode(task.getCode());
			if (contestCode != null) {
				if (contestCode.getSolution() != null) {
					// solution already accepted
					return null;
				}
				contestCode.setSolution(_solution);
				contestCode.setTs(Instant.now().toEpochMilli());
			} else {
				contestCode = contestProgress.addContestCode(
						task.getCode(),
						String.valueOf(task.getType().getMark()),
						solution,
						Instant.now().toEpochMilli()
				);
			}
			teamService.updateTeam(_team);
			return contestCode;
		});
		if (c == null) {
			log.warn("Solution \'{}\' already accepted: team=[{}] task=[{}]", solution, team.getId(), task.getCode());
		} else {
			log.info("Solution \'{}\' accepted: team=[{}] task=[{}]", solution, team.getId(), task.getCode());
		}
		return true;
	}

	@Override
	public boolean checkTaskIsCompleted(@NonNull final Task task, @NonNull final Team team) {
		final TeamContestProgress contestProgress = team.getContestProgress();
		final TeamContestProgressCode contestCode = contestProgress.getContestCode(task.getCode());
		return (contestCode != null && contestCode.getSolution() != null);
	}
	
	@Override
	public boolean checkLocationIsDiscovered(@NonNull final Location location, @NonNull final Team team) {
		final String cacheKey = "LocationDiscovered." + location.getCode();
		final Boolean resultCached = team.getData(cacheKey);
		if (resultCached != null) {
			return resultCached;
		}
		final TeamContestProgress contestProgress = team.getContestProgress();
		final boolean discovered = contestProgress.getAllAcquiredCodes()
				.filter(progressCode -> "A".equals(progressCode.getGroup()))
				.map(TeamContestProgressCode::getCode)
				.map(taskService::getTaskByCodeFromCache)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(task -> task.getRewardCodes().contains(location.getCode()))
				.findAny()
				.isPresent();
		// cache positive result (can't be changed)
		if (discovered) {
			team.setData(cacheKey, discovered);
		}
		return discovered;
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
