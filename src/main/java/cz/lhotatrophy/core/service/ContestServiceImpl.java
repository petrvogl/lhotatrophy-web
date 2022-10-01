package cz.lhotatrophy.core.service;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.persist.entity.Clue;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgress;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import cz.lhotatrophy.utils.DateTimeUtils;
import cz.lhotatrophy.utils.TextUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
	private transient EntityCacheService cacheService;
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
		final Instant completionLimitInstant = appConfig.getGameEndInstant();
		final long maxOverLimitSeconds = appConfig.getMaxOverLimitSeconds();
		final Instant timeAtFinishInstant = (contestProgress.getTimestampAtFinish() == null)
				? null
				: DateTimeUtils.toInstant(contestProgress.getTimestampAtFinish());
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
				final Instant now = Instant.now();
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
		// include the cost of destination location revealing
		if (contestProgress.isDestinationRevealed()) {
			totalMileage += appConfig.getDestinationRevealedPenalty();
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
					if (code.accepted()) {
						ACodesAcquiredCount++;
					}
				case "B":
					if (code.accepted()) {
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

	@Override
	public boolean checkTeamIsInPlay(final Team team) {
		return team != null
				&& team.isActive()
				&& team.getOwner().isActive()
				&& !team.isDisqualified()
				&& team.getContestProgress().getMileageAtStart() != null
				&& team.getContestProgress().getMileageAtFinish() == null;
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
	 * Verifies the solution and records if the solution to the task was
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
		if (!checkTeamIsInPlay(team) || !task.isActive()) {
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
	public boolean acceptDestinationSolution(
			@NonNull final String solution,
			@NonNull final Team team
	) {
		if (!checkTeamIsInPlay(team)) {
			return false;
		}
		final String _solution = normalizeSolution(solution);
		final String rightSolution = taskService.getTaskByCodeFromCache("D0")
				.map(Task::getAnySolution)
				.orElse("");
		if (!rightSolution.equals(_solution)) {
			// incorrect solution
			log.info("Solution \'{}\' NOT accepted: team=[{}] task=[DEST]", solution, team.getId());
			return false;
		}
		// update contest progress
		final Boolean updated = runInTransaction(() -> {
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			if (contestProgress.getDestinationSolution() != null) {
				// already set
				return false;
			}
			contestProgress.setDestinationSolution(solution);
			teamService.updateTeam(_team);
			return true;
		});
		if (updated) {
			log.warn("Solution \'{}\' already accepted: team=[{}] task=[DEST]", solution, team.getId());
		} else {
			log.info("Solution \'{}\' accepted: team=[{}] task=[DEST]", solution, team.getId());
		}
		return updated;
	}

	@Override
	public boolean revealSolutionHint(
			@NonNull final Task task,
			@NonNull final Team team
	) {
		return revealSolutionHintInternal(team, task, true, false, false);
	}

	@Override
	public boolean revealSolutionProcedure(
			@NonNull final Task task,
			@NonNull final Team team
	) {
		return revealSolutionHintInternal(team, task, false, true, false);
	}

	@Override
	public boolean revealSolution(
			@NonNull final Task task,
			@NonNull final Team team
	) {
		return revealSolutionHintInternal(team, task, false, false, true);
	}

	/**
	 * Records whether the hint, procedure or solution has been revealed.
	 * Exactly one of the boolean arguments must be set to {@code true}.
	 *
	 * @param team Team
	 * @param task Task
	 * @param hint Record that solution hint has been revealed
	 * @param procedure Record that solution procedure has been revealed
	 * @param solution Record that solution has been revealed
	 * @return {@code true} if revealed
	 * @throws IllegalArgumentException If none or more than one boolean
	 * arguments are {@code true}
	 */
	private boolean revealSolutionHintInternal(
			final Team team,
			final Task task,
			final boolean hint,
			final boolean procedure,
			final boolean solution
	) {
		if (!checkTeamIsInPlay(team) || !task.isActive()) {
			return false;
		}
		// update contest progress
		final TeamContestProgressCode c = runInTransaction(() -> {
			final boolean hintRevealed = (hint || procedure || solution) && task.hasSolutionHint();
			final boolean procedureRevealed = (procedure || solution) && task.hasSolutionProcedure();
			final boolean solutionRevealed = solution;
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			TeamContestProgressCode contestCode = contestProgress.getContestCode(task.getCode());
			if (contestCode != null) {
				if (contestCode.revealed(hint, procedure, solution)) {
					// hint already revealed
					return null;
				}
				contestCode.setHintRevealed(hintRevealed);
				contestCode.setProcedureRevealed(procedureRevealed);
				contestCode.setSolutionRevealed(solutionRevealed);
			} else {
				contestCode = contestProgress.addContestCode(
						task.getCode(),
						String.valueOf(task.getType().getMark()),
						hintRevealed,
						procedureRevealed,
						solutionRevealed
				);
			}
			teamService.updateTeam(_team);
			return contestCode;
		});
		if (c == null) {
			log.warn("Solution hint already revealed: team=[{}] task=[{}]", team.getId(), task.getCode());
		} else {
			log.info("Solution hint revealed: team=[{}] task=[{}]", team.getId(), task.getCode());
		}
		return true;
	}

	@Override
	public boolean revealDestination(@NonNull final Team team) {
		if (!checkTeamIsInPlay(team)) {
			return false;
		}
		// update contest progress
		final Boolean revealed = runInTransaction(() -> {
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			if (contestProgress.isDestinationRevealed()) {
				// destination has been revealed
				return null;
			}
			contestProgress.setDestinationRevealed(true);
			teamService.updateTeam(_team);
			return true;
		});
		if (revealed == null) {
			log.warn("Destination location already revealed: team=[{}]", team.getId());
		} else {
			log.info("Destination location revealed: team=[{}]", team.getId());
		}
		return true;
	}

	@Override
	public boolean buyInsurance(@NonNull final Team team) {
		if (!checkTeamIsInPlay(team)) {
			return false;
		}
		// update contest progress
		final Boolean bought = runInTransaction(() -> {
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			if (contestProgress.isInsuranceAgainstWinning()) {
				// team already has insurance
				return null;
			}
			final long CCodesAcquiredCount = contestProgress.getAllAcquiredCodes()
					.filter(contestCode -> "C".equals(contestCode.getGroup()))
					.count();
			if (CCodesAcquiredCount < appConfig.getInsuranceCCodeLimit()) {
				// the conditions for obtaining insurance are not met
				return false;
			}
			contestProgress.setInsuranceAgainstWinning(true);
			teamService.updateTeam(_team);
			return true;
		});
		if (bought == null) {
			log.warn("Team already has insurance against winning: team=[{}]", team.getId());
			return true;
		}
		if (bought) {
			log.info("Insurance against winning was bought: team=[{}]", team.getId());
		}
		return bought;
	}

	@Override
	public boolean setMileageAtStart(
			@NonNull final Team team,
			@NonNull final Integer mileage
	) {
		if (!team.isActive() || !team.getOwner().isActive() || team.isDisqualified() || mileage < 0) {
			return false;
		}
		// update contest progress
		final Boolean updated = runInTransaction(() -> {
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			if (contestProgress.getMileageAtStart() != null) {
				// already set
				return false;
			}
			contestProgress.setMileageAtStart(mileage);
			teamService.updateTeam(_team);
			return true;
		});
		if (updated) {
			log.info("Mileage at start has been set: team=[{}]", team.getId());
		} else {
			log.warn("Team has already set the mileage at start: team=[{}]", team.getId());
		}
		return updated;
	}

	@Override
	public boolean setMileageAtFinish(
			@NonNull final Team team,
			@NonNull final Integer mileage
	) {
		if (!checkTeamIsInPlay(team) || mileage < 0) {
			return false;
		}
		// update contest progress
		final Boolean updated = runInTransaction(() -> {
			final Team _team = teamService.getTeamById(team.getId()).get();
			final TeamContestProgress contestProgress = _team.getContestProgress();
			if (contestProgress.getMileageAtFinish() != null) {
				// already set
				return false;
			}
			contestProgress.setMileageAtFinish(mileage);
			contestProgress.setTimestampAtFinish(Instant.now().toEpochMilli());
			teamService.updateTeam(_team);
			return true;
		});
		if (updated) {
			log.info("Mileage at finish has been set: team=[{}]", team.getId());
		} else {
			log.warn("Team has already set the mileage at finish: team=[{}]", team.getId());
		}
		return updated;
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
		final Boolean resultCached = team.getTemporary(cacheKey, () -> {
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
			return discovered;
		});
		return resultCached;
	}

	@Override
	public boolean checkHasInsurance(@NonNull final Team team) {
		final TeamContestProgress contestProgress = team.getContestProgress();
		return contestProgress.isInsuranceAgainstWinning();
	}

	@Override
	public List<Clue> getCluesDiscovered(@NonNull final Team team) {
		final String cacheKey = "CluesDiscovered";
		final List<Clue> resultCached = team.getTemporary(cacheKey, () -> {
			final TeamContestProgress contestProgress = team.getContestProgress();
			final List<Clue> clues = contestProgress.getAllAcquiredCodes()
					// get rewards of all acquired codes
					.map(TeamContestProgressCode::getCode)
					.map(taskService::getTaskByCodeFromCache)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.flatMap(Task::getRewardCodesStream)
					// map codes of rewards to entities
					.map(cacheService::getEntityByCode)
					.filter(Optional::isPresent)
					.map(Optional::get)
					// filter and deduplicate clues 
					.filter(Clue.class::isInstance)
					.map(Clue.class::cast)
					.distinct()
					// collect sorted
					.sorted(Clue.orderByCode())
					.collect(Collectors.toList());
			return clues;
		});
		return resultCached;
	}

	@Override
	public List<Task> getTasksCompleted(@NonNull final Team team, @NonNull final TaskTypeEnum taskType) {
		final String cacheKey = "TasksCompleted." + taskType.name();
		final List<Task> resultCached = team.getTemporary(cacheKey, () -> {
			final List<Task> tasks = getCodesAcquired(team, taskType).stream()
					.map(TeamContestProgressCode::getCode)
					.map(taskService::getTaskByCodeFromCache)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList());
			return tasks;
		});
		return resultCached;
	}

	@Override
	public List<TeamContestProgressCode> getCodesAcquired(@NonNull final Team team, @NonNull final TaskTypeEnum taskType) {
		final String cacheKey = "CodesAcquired." + taskType.name();
		final List<TeamContestProgressCode> resultCached = team.getTemporary(cacheKey, () -> {
			final String typeMark = String.valueOf(taskType.getMark());
			final TeamContestProgress contestProgress = team.getContestProgress();
			return contestProgress.getAllAcquiredCodes()
					.filter(contestCode -> typeMark.equals(contestCode.getGroup()))
					.collect(Collectors.toList());
		});
		return resultCached;
	}

	@Override
	public boolean checkDestinationRevealed(@NonNull final Team team) {
		final TeamContestProgress contestProgress = team.getContestProgress();
		return contestProgress.isDestinationRevealed();
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
