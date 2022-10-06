package cz.lhotatrophy.web.service;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.core.service.ContestService;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.persist.entity.Clue;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code ${service.contest.method()}}
 *
 * @author Petr Vogl
 */
@Component
@Log4j2
public final class ContestViewService {

	@Autowired
	private transient ApplicationConfig appConfig;
	@Autowired
	private transient ContestService contestService;
	@Autowired
	private transient TeamService teamService;

	/**
	 * {@code ${service.contest.checkTeamIsInPlay()}}
	 */
	public boolean checkTeamIsInPlay() {
		// check in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.checkTeamIsInPlay(team))
				.orElse(false);
	}

	/**
	 * {@code ${service.contest.checkTaskIsCompleted(task)}}
	 */
	public boolean checkTaskIsCompleted(final Task task) {
		if (task == null) {
			// null-safe
			return false;
		}
		final MutableBoolean completed = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			completed.setValue(contestService.checkTaskIsCompleted(task, team));
		});
		return completed.booleanValue();
	}

	/**
	 * {@code ${service.contest.checkLocationIsDiscovered(location)}}
	 */
	public boolean checkLocationIsDiscovered(final Location location) {
		if (location == null) {
			// null-safe
			return false;
		}
		final MutableBoolean discovered = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			discovered.setValue(contestService.checkLocationIsDiscovered(location, team));
		});
		return discovered.booleanValue();
	}

	/**
	 * {@code ${service.contest.checkHasInsurance()}}
	 */
	public boolean checkHasInsurance() {
		final MutableBoolean insurance = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			insurance.setValue(contestService.checkHasInsurance(team));
		});
		return insurance.booleanValue();
	}

	/**
	 * {@code ${service.contest.isInsuranceAvailable()}}
	 */
	public boolean isInsuranceAvailable() {
		final MutableBoolean insurance = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			final int c = contestService.getCodesAcquired(team, TaskTypeEnum.C_CODE).size();
			insurance.setValue(c >= appConfig.getInsuranceCCodeLimit());
		});
		return insurance.booleanValue();
	}

	/**
	 * {@code ${service.contest.aCodesAcquiredCount()}}
	 */
	public int aCodesAcquiredCount() {
		// get it in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.getTasksCompleted(team, TaskTypeEnum.A_CODE))
				.map(List::size)
				.orElse(0);
	}

	public int aCodesAcquiredCount(final Team team) {
		return contestService.getTasksCompleted(team, TaskTypeEnum.A_CODE).size();
	}

	/**
	 * {@code ${service.contest.bCodesAcquiredCount()}}
	 */
	public int bCodesAcquiredCount() {
		// get it in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.getTasksCompleted(team, TaskTypeEnum.B_CODE))
				.map(List::size)
				.orElse(0);
	}

	public int bCodesAcquiredCount(final Team team) {
		return contestService.getTasksCompleted(team, TaskTypeEnum.B_CODE).size();
	}

	/**
	 * {@code ${service.contest.cCodesAcquiredCount()}}
	 */
	public int cCodesAcquiredCount() {
		// get it in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.getTasksCompleted(team, TaskTypeEnum.C_CODE))
				.map(List::size)
				.orElse(0);
	}

	public int cCodesAcquiredCount(final Team team) {
		return contestService.getTasksCompleted(team, TaskTypeEnum.C_CODE).size();
	}

	/**
	 * {@code ${service.contest.cCodesAcquired()}}
	 */
	public List<TeamContestProgressCode> cCodesAcquired() {
		// get it in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.getCodesAcquired(team, TaskTypeEnum.C_CODE))
				.orElse(Collections.emptyList());
	}

	/**
	 * {@code ${service.contest.getCluesDiscovered()}}
	 */
	public List<Clue> getCluesDiscovered() {
		// get it in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.getCluesDiscovered(team))
				.orElse(Collections.emptyList());
	}

	/**
	 * {@code ${service.contest.checkDestinationRevealed()}}
	 */
	public boolean checkDestinationRevealed() {
		final MutableBoolean revealed = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			revealed.setValue(contestService.checkDestinationRevealed(team));
		});
		return revealed.booleanValue();
	}

	/**
	 * {@code ${service.contest.checkStartImageUploaded()}}
	 */
	public boolean checkStartImageUploaded() {
		final MutableBoolean uploaded = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			uploaded.setValue(contestService.checkStartImageUploaded(team));
		});
		return uploaded.booleanValue();
	}

	/**
	 * {@code ${service.contest.checkFinishImageUploaded()}}
	 */
	public boolean checkFinishImageUploaded() {
		final MutableBoolean uploaded = new MutableBoolean(false);
		// check in the context of the effective user
		teamService.getEffectiveTeam().ifPresent(team -> {
			uploaded.setValue(contestService.checkFinishImageUploaded(team));
		});
		return uploaded.booleanValue();
	}

	/**
	 * {@code ${service.contest.getTeamScore(team)}}
	 */
	public Integer getTeamScore(final Team team) {
		final int score = contestService.getTeamScore(team);
		if (score == Integer.MAX_VALUE) {
			// team is disqualified
			return null;
		}
		return score;
	}

	/**
	 * {@code ${service.contest.getTeamTimeExceededPenalty(team)}}
	 */
	public Integer getTeamTimeExceededPenalty(final Team team) {
		final Integer penalty = contestService.getTeamTimeExceededPenalty(team);
		if (penalty == Integer.MAX_VALUE) {
			// team is disqualified
			return null;
		}
		return penalty;
	}

	/**
	 * {@code ${service.contest.getTeamStandings()}}
	 */
	public List<Team> getTeamStandings() {
		return contestService.getTeamStandings();
	}

	/**
	 * {@code ${service.contest.getTaskStatistics(task)}}
	 */
	public Map<String, Integer> getTaskStatistics(final Task task) {
		return contestService.getTaskStatistics(task);
	}
}
