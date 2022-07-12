package cz.lhotatrophy.persist.dao;

import cz.lhotatrophy.persist.entity.TeamMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * TeamMember entity DAO.
 *
 * @author Petr Vogl
 */
@Repository
public interface TeamMemberDao extends JpaRepository<TeamMember, Long> {

	@Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = ?1")
	List<TeamMember> findAllByTeamId(Long teamId);
}
