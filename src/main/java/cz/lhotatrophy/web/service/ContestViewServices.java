package cz.lhotatrophy.web.service;

import cz.lhotatrophy.core.service.ContestService;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.Team;
import lombok.NonNull;
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
public final class ContestViewServices {

	@Autowired
	private transient ContestService contestService;
	@Autowired
	private transient TeamService teamService;

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
}
