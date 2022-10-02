package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Clue;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgress;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import java.util.List;
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

	boolean acceptDestinationSolution(@NonNull String solution, @NonNull Team team);

	/**
	 * Records that the solution hint has been revealed.
	 *
	 * @param task Task
	 * @param team Team
	 * @return {@code true} if revealed
	 */
	boolean revealSolutionHint(@NonNull Task task, @NonNull Team team);

	/**
	 * Records that the solution procedure has been revealed.
	 *
	 * @param task Task
	 * @param team Team
	 * @return {@code true} if revealed
	 */
	boolean revealSolutionProcedure(@NonNull Task task, @NonNull Team team);

	/**
	 * Records that the solution has been revealed.
	 *
	 * @param task Task
	 * @param team Team
	 * @return {@code true} if revealed
	 */
	boolean revealSolution(@NonNull Task task, @NonNull Team team);

	/**
	 * Records that the exact destination location has been revealed.
	 *
	 * @param team Team
	 * @return {@code true} if revealed
	 */
	boolean revealDestination(@NonNull Team team);

	/**
	 * Verifies the conditions for obtaining insurance and records its
	 * obtaining.
	 *
	 * @param team Team
	 * @return {@code true} if obtained
	 */
	boolean buyInsurance(@NonNull Team team);

	/**
	 * Sets the mileage state on the car when the game has started (km unit).
	 *
	 * @param team Team
	 * @param mileage Mileage
	 * @return {@code true} if state has been updated
	 */
	boolean setMileageAtStart(@NonNull Team team, @NonNull Integer mileage);

	/**
	 * Sets the mileage state on the car when the team finished the game (km
	 * unit). Also sets the exact timestamp the team finished the game.
	 *
	 * @param team Team
	 * @param mileage Mileage
	 * @return {@code true} if state has been updated
	 */
	boolean setMileageAtFinish(@NonNull Team team, @NonNull Integer mileage);

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
	 * Checks whether the team has insurance which protects from winning the
	 * first prize.
	 *
	 * @param team Team
	 * @return {@code true} if insurance has been obtained
	 */
	boolean checkHasInsurance(@NonNull Team team);

	boolean checkTeamIsInPlay(Team team);

	/**
	 * Returns the list of all discovered clues.
	 *
	 * @param team Team
	 * @return List of clues
	 */
	List<Clue> getCluesDiscovered(@NonNull Team team);

	List<Task> getTasksCompleted(@NonNull Team team, @NonNull TaskTypeEnum taskType);

	List<TeamContestProgressCode> getCodesAcquired(@NonNull Team team, @NonNull TaskTypeEnum taskType);

	boolean checkDestinationRevealed(@NonNull Team team);

	boolean checkStartImageUploaded(@NonNull Team team);

	boolean checkFinishImageUploaded(@NonNull Team team);

	/**
	 * Returns the current team's score. The score is calculated and measured in
	 * kilometers driven from the start to the finish - the lower the mileage
	 * the better.
	 *
	 * @param contestProgress Results achieved in the game
	 * @return current team's score
	 */
	int calculateScore(TeamContestProgress contestProgress);

	/**
	 * Returns the current score of the team. Returns {@code Integer.MAX_VALUE}
	 * if team is disqualified.
	 *
	 * @param team Team
	 * @return Score
	 */
	int getTeamScore(@NonNull Team team);

	/**
	 * Returns penalty for exceeding the time limit. Returns
	 * {@code Integer.MAX_VALUE} if team is disqualified.
	 *
	 * @param team Team
	 * @return Penalty
	 */
	int getTeamTimeExceededPenalty(@NonNull Team team);

	/**
	 * Returns the current team standings on the leaderboard.
	 *
	 * @return Current team standings
	 */
	List<Team> getTeamStandings();
}
