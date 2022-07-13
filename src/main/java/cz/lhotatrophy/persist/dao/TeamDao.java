package cz.lhotatrophy.persist.dao;

import cz.lhotatrophy.persist.entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Team entity DAO.
 *
 * @author Petr Vogl
 */
@Repository
public interface TeamDao extends JpaRepository<Team, Long> {
	
	@Query("SELECT t FROM Team t WHERE t.name = ?1")
	Optional<Team> findByName(String name);
	
	@Query("SELECT t.id FROM Team t ORDER BY t.id ASC")
	List<Long> findAllIds();
}
