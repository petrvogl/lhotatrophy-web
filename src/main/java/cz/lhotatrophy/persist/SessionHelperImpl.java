package cz.lhotatrophy.persist;

import cz.lhotatrophy.persist.entity.Entity;
import java.util.concurrent.Callable;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helpful methods to deal with Hibernate session.
 *
 * @author Petr Vogl
 */
@Log4j2
@Component
public class SessionHelperImpl implements SessionHelper {

	@Autowired
	private transient EntityManager entityManager;

	/**
	 * Create an instance of {@link TypedQuery} for executing a criteria query.
	 */
	@Override
	public <T extends Entity> TypedQuery<T> createQuery(@NonNull final CriteriaQuery<T> query) {
		return entityManager.createQuery(query);
	}

	/**
	 * Return an instance of {@link CriteriaBuilder} for the creation of
	 * {@link CriteriaQuery} objects.
	 */
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return entityManager.getCriteriaBuilder();
	}

	/**
	 * Flush current Hibernate session.
	 */
	@Override
	public void flush() {
		entityManager.flush();
	}

	/**
	 * Remove entity instance from the session cache.
	 */
	@Override
	public void detach(@NonNull final Entity entity) {
		if (entityManager.contains(entity)) {
			entityManager.detach(entity);
		}
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
			//entityManager.clear();
			log.error("runInTransaction:\n", e);
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
			//entityManager.clear();
			log.error("runInTransaction:\n", e);
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
		entityManager.getTransaction().rollback();
	}
}
