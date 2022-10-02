package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.math3.stat.Frequency;

/**
 *
 * @author Petr Vogl
 */
public interface TeamService extends Service {

	Optional<Team> getTeamById(@NonNull Long id);

	Optional<Team> getTeamByName(@NonNull String name);

	Optional<Team> getTeamByIdFromCache(@NonNull Long id);

	List<Team> getTeamListing(@NonNull TeamListingQuerySpi query);

	Stream<Team> getTeamListingStream(@NonNull TeamListingQuerySpi query);

	Frequency getTeamOrdersFrequency(@NonNull Class<? extends Enum> enumClass);

	void removeTeamFromCache(@NonNull Long id);

	Optional<Team> getEffectiveTeam();

	Team registerNewTeam(@NonNull String name, @NonNull User owner);

	void updateTeam(@NonNull Team team);
}
