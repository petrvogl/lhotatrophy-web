package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgress;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface ContestService extends Service {

	/**
	 * Verifies the solution and records if the solution to the task is
	 * accepted.
	 *
	 * @param solution Solution
	 * @param taskType Task type
	 * @param team Team
	 * @return {@code true} if solution is accepted
	 */
	boolean acceptSolution(@NonNull String solution, @NonNull TaskTypeEnum taskType, @NonNull Team team);

	/**
	 * Verifies the solution and records if the solution to the task is
	 * accepted.
	 *
	 * @param solution Solution
	 * @param task Task
	 * @param team Team
	 * @return {@code true} if solution is accepted
	 */
	boolean acceptSolution(@NonNull String solution, @NonNull Task task, @NonNull Team team);

	/**
	 * Checks if the task has been already completed.
	 *
	 * @param task Task
	 * @param team Team
	 * @return {@code true} if task has been completed
	 */
	boolean checkTaskIsCompleted(@NonNull Task task, @NonNull Team team);
	
	/**
	 * Checks if the location has been already discovered.
	 *
	 * @param location Location
	 * @param team Team
	 * @return {@code true} if task has been completed
	 */
	boolean checkLocationIsDiscovered(@NonNull Location location, @NonNull Team team);

	/**
	 * Gets the current team's score. The score is calculated and measured in
	 * kilometers driven from the start to the finish - the lower the mileage
	 * the better.
	 *
	 * @param contestProgress Results achieved in the game
	 * @return current team's score
	 */
	int calculateScore(TeamContestProgress contestProgress);
}
