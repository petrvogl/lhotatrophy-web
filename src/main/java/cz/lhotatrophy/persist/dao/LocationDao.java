package cz.lhotatrophy.persist.dao;

import cz.lhotatrophy.persist.entity.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Location entity DAO.
 *
 * @author Petr Vogl
 */
@Repository
public interface LocationDao extends JpaRepository<Location, Long> {

	@Query("SELECT l FROM Location l WHERE l.code = ?1")
	Optional<Location> findByCode(String code);

	@Query("SELECT l FROM Location l WHERE l.name = ?1")
	Optional<Location> findByName(String name);

	@Query("SELECT l.id FROM Location l ORDER BY l.id ASC")
	List<Long> findAllIds();
}
