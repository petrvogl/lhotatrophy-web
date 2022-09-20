package cz.lhotatrophy.persist;

import cz.lhotatrophy.persist.entity.Entity;
import java.util.concurrent.Callable;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helpful methods to deal with Hibernate session.
 *
 * @author Petr Vogl
 */
public interface SessionHelper {

	/**
	 * Create an instance of {@link TypedQuery} for executing a criteria query.
	 *
	 * @param <T> a criteria query type
	 * @param criteriaQuery a criteria query object
	 * @return the new query instance
	 */
	<T extends Entity> TypedQuery<T> createQuery(@NonNull CriteriaQuery<T> query);

	/**
	 * Return an instance of {@link CriteriaBuilder} for the creation of
	 * {@link CriteriaQuery} objects.
	 *
	 * @return CriteriaBuilder instance
	 * @throws IllegalStateException if the entity manager has been closed
	 */
	CriteriaBuilder getCriteriaBuilder();

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
