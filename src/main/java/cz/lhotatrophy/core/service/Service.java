package cz.lhotatrophy.core.service;

import java.util.concurrent.Callable;

/**
 *
 * @author Petr Vogl
 */
public interface Service {

	/**
	 * Flush current Hibernate session.
	 */
	void flush();

	/**
	 * Run command in transaction.
	 */
	<T> T runInTransaction(final Callable<T> command);

	/**
	 * Run command in transaction.
	 */
	void runInTransaction(final Runnable command);
}
