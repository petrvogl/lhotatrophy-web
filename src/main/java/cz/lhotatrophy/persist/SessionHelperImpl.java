package cz.lhotatrophy.persist;

import cz.lhotatrophy.persist.entity.Entity;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helpful methods to deal with Hibernate session.
 *
 * @author Petr Vogl
 */
@Component
@Log4j2
public class SessionHelperImpl implements SessionHelper {

	@Autowired
	private transient SessionFactory sessionFactory;

	/**
	 * Get current Hibernate session.
	 *
	 * @return session
	 */
	@Override
	public Session getSession() {
		final Session currentSession = Objects.requireNonNull(sessionFactory, "sessionFactory must not be null").getCurrentSession();
		if (log.isTraceEnabled()) {
			final Transaction transaction = currentSession.getTransaction();
			log.trace("TRANSACTION >>> session={}/{} transaction={}/{}", currentSession.isOpen(), currentSession.hashCode(), transaction.isActive(), transaction.hashCode());
		}
		return currentSession;
	}

	/**
	 * Flush current Hibernate session.
	 */
	@Override
	public void flush() {
		getSession().flush();
	}

	/**
	 * Remove entity instance from the session cache.
	 */
	@Override
	public void detach(@NonNull final Entity entity) {
		getSession().evict(entity);
	}

	/**
	 * Run command in transaction.
	 */
	@Override
	@Transactional
	public <T> T runInTransaction(final Callable<T> command) {
		try {
			return command.call();
		} catch (final Exception e) {
			log.error("runInTransaction:\n", e);
			Optional.ofNullable(getSession().getTransaction())
					.map(Transaction::isActive)
					.filter(Boolean.TRUE::equals)
					.ifPresent(isActive -> {
						getSession().clear();
					});
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Run command in transaction.
	 */
	@Override
	@Transactional
	public void runInTransaction(final Runnable command) {
		try {
			command.run();
		} catch (final Exception e) {
			log.error("runInTransaction:\n", e);
			Optional.ofNullable(getSession().getTransaction())
					.map(Transaction::isActive)
					.filter(Boolean.TRUE::equals)
					.ifPresent(isActive -> {
						getSession().clear();
					});
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Rollback transaction.
	 */
	@Override
	@Transactional
	public void transactionRollback() {
		getSession().getTransaction().rollback();
	}
}
