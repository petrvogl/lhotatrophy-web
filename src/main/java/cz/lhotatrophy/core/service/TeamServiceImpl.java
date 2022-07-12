package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.TeamDao;
import cz.lhotatrophy.persist.dao.TeamMemberDao;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class TeamServiceImpl extends AbstractService implements TeamService {

	@Autowired
	private transient TeamDao teamDao;
	@Autowired
	private transient TeamMemberDao teamMemberDao;
	@Autowired
	private transient UserService userService;

	@NonNull
	public Team createTeam(@NonNull final UnaryOperator<Team> initializer) {
		return runInTransaction(() -> {
			return teamDao.save(
					// custom init
					initializer.apply(new Team())
			);
		});
	}

	@NonNull
	public TeamMember createTeamMember(@NonNull final UnaryOperator<TeamMember> initializer) {
		return runInTransaction(() -> {
			return teamMemberDao.save(
					// custom init
					initializer.apply(new TeamMember())
			);
		});
	}

	@Override
	public Optional<Team> getTeamById(@NonNull final Long id) {
		return teamDao.findById(id);
	}

	@Override
	public Optional<Team> getTeamByName(@NonNull final String name) {
		return teamDao.findByName(name);
	}

	@NonNull
	@Override
	public List<Team> getAllTeams() {
		final List<Team> all = teamDao.findAll(Sort.by(Sort.Direction.ASC, "id"));
		return all == null ? Collections.emptyList() : all;
	}

	public Optional<TeamMember> getTeamMemberById(@NonNull final Long id) {
		return teamMemberDao.findById(id);
	}

	public List<TeamMember> getAllTeamMembers(@NonNull final Team team) {
		return teamMemberDao.findAllByTeamId(team.getId());
	}

	@Override
	public Team registerNewTeam(@NonNull final String name, @NonNull final User owner) {
		if (teamDao.findByName(name).isPresent()) {
			throw new RuntimeException("Tým s tímto názvem už existuje.");
		}
		if (owner.getTeam() != null) {
			throw new RuntimeException("Tento uživatel už má svůj tým.");
		}
		return runInTransaction(() -> {
			final Team team = createTeam(t -> {
				t.setName(name);
				t.setOwner(owner);
				return t;
			});
			// just for consistency
			owner.setTeam(team);
			// logging
			log.info("New team has been registered: [ {} / {} ]", team.getId(), team.getName());
			log.info("\nLhotaTrophy:\n    CREATED {}", team.toString());
			return team;
		});
	}

	@Override
	public void updateTeam(@NonNull final Team team) {
		teamDao.save(team);
	}

	public void updateTeamMembers(@NonNull final Long teamId, final Set<TeamMember> members) {
		// TODO
	}
}
