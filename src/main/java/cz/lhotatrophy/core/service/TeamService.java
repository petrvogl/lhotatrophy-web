package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.User;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface TeamService extends Service {

	Optional<Team> getTeamById(@NonNull Long id);
	
	Optional<Team> getTeamByName(@NonNull String name);
	
	@NonNull
	List<Team> getAllTeams();
	
	Team registerNewTeam(@NonNull String name, @NonNull User owner);
	
	void updateTeam(@NonNull Team team);
}
