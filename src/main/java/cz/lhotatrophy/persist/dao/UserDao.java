package cz.lhotatrophy.persist.dao;

import cz.lhotatrophy.persist.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * User account entity DAO.
 *
 * @author Petr Vogl
 */
@Repository
public interface UserDao extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.email = ?1")
	Optional<User> findByEmail(String email);
}
