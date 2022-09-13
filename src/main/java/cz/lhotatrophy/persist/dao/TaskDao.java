package cz.lhotatrophy.persist.dao;

import cz.lhotatrophy.persist.entity.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Task entity DAO.
 * 
 * @author Petr Vogl
 */
@Repository
public interface TaskDao extends JpaRepository<Task, Long> {

	@Query("SELECT t FROM Task t WHERE t.code = ?1")
	Optional<Task> findByCode(String code);
	
	@Query("SELECT t FROM Task t WHERE t.name = ?1")
	Optional<Task> findByName(String name);
	
	@Query("SELECT t.id FROM Task t ORDER BY t.id ASC")
	List<Long> findAllIds();
}
