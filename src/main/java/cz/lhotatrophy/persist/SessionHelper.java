package cz.lhotatrophy.persist;

import cz.lhotatrophy.persist.entity.Entity;
import java.util.concurrent.Callable;
import lombok.NonNull;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helpful methods to deal with Hibernate session.
 *
 * @author Petr Vogl
 */
public interface SessionHelper {

	/**
	 * Get current Hibernate session.
	 *
	 * @return session
	 */
	Session getSession();

	/**
	 * Flush current Hibernate session.
	 */
	void flush();

	/**
	 * Remove entity instance from the session cache. Changes to the instance
	 * will not be synchronized with the database.
	 *
	 * @param entity The entity to detach
	 */
	void detach(@NonNull Entity entity);

	/**
	 * Run command in transaction.
	 */
	@Transactional
	<T> T runInTransaction(Callable<T> command);

	/**
	 * Run command in transaction.
	 */
	@Transactional
	void runInTransaction(Runnable command);

	/**
	 * Rollback transaction.
	 */
	@Transactional
	void transactionRollback();
}
