package cz.lhotatrophy.persist.dao;

import cz.lhotatrophy.persist.entity.Clue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Clue entity DAO.
 *
 * @author Petr Vogl
 */
@Repository
public interface ClueDao extends JpaRepository<Clue, Long> {

	@Query("SELECT c FROM Clue c WHERE c.code = ?1")
	Optional<Clue> findByCode(String code);

	@Query("SELECT c.id FROM Clue c ORDER BY c.id ASC")
	List<Long> findAllIds();
}
