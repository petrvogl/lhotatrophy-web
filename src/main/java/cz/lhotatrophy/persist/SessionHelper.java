package cz.lhotatrophy.persist;

import java.util.concurrent.Callable;
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
	 * Run command in transaction.
	 */
	@Transactional
	<T> T runInTransaction(final Callable<T> command);

	/**
	 * Run command in transaction.
	 */
	@Transactional
	void runInTransaction(final Runnable command);

	/**
	 * Rollback transaction.
	 */
	@Transactional
	void transactionRollback();
}
