package cz.lhotatrophy.web.service;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.core.service.ContestService;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.persist.entity.Clue;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import java.util.Collections;
import java.util.List;
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
}
