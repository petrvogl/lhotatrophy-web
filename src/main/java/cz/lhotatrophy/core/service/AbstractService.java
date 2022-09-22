package cz.lhotatrophy.core.service;

import cz.lhotatrophy.core.security.UserDetails;
import cz.lhotatrophy.persist.SessionHelper;
import cz.lhotatrophy.persist.entity.Entity;
import cz.lhotatrophy.persist.entity.User;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Petr Vogl
 */
@Log4j2
public abstract class AbstractService implements Service {

	@Autowired
	private transient SessionHelper sessionHelper;

	/**
	 * Create an instance of {@link TypedQuery} for executing a criteria query.
	 *
	 * @param <T> a criteria query type
	 * @param criteriaQuery a criteria query object
	 * @return the new query instance
	 */
	protected <T extends Entity> TypedQuery<T> createQuery(final CriteriaQuery<T> query) {
		return sessionHelper.createQuery(query);
	}

	/**
	 * Return an instance of {@link CriteriaBuilder} for the creation of
	 * {@link CriteriaQuery} objects.
	 *
	 * @return CriteriaBuilder instance
	 * @throws IllegalStateException if the entity manager has been closed
	 */
	protected CriteriaBuilder getCriteriaBuilder() {
		return sessionHelper.getCriteriaBuilder();
	}

	/**
	 * Flush current Hibernate session.
	 */
	protected void flush() {
		sessionHelper.flush();
	}

	/**
	 * Remove the given entity from the persistence context, causing a managed
	 * entity to become detached. Unflushed changes made to the entity if any
	 * (including removal of the entity), will not be synchronized to the
	 * database. Entities which previously referenced the detached entity will
	 * continue to reference it.
	 *
	 * @param <T> Entity type
	 * @param entity Entity instance
	 * @return Deatached entity instance
	 */
	protected <T extends Entity> T detach(@NonNull final T entity) {
		sessionHelper.detach(entity);
		return entity;
	}

	/**
	 * If the given object is not a proxy, return it. But, if it is a Hibernate
	 * proxy, ensure that the proxy is initialized, and return a direct
	 * reference to its proxied entity object.
	 *
	 * @param <T> Entity type
	 * @param entity an object which might be a proxy for an entity
	 * @return Unproxied entity instance
	 */
	protected <T extends Entity> T unproxy(@NonNull final T entity) {
		final T unproxied = (T) Hibernate.unproxy(entity);
		if (log.isDebugEnabled()) {
			final Class clsU = unproxied.getClass();
			final Class clsE = entity.getClass();
			if (clsE.equals(clsU)) {
				log.debug("Unproxy entity: \"{}\" is not Hibernate proxy", clsE.getSimpleName());
			} else {
				log.debug("Unproxy entity: \"{}\" >> \"{}\"", clsE.getSimpleName(), clsU.getSimpleName());
			}
		}
		return unproxied;
		//	if (entity instanceof HibernateProxy) {
		//		final LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
		//		if (lazyInitializer != null) {
		//			final T _entity = (T) lazyInitializer.getImplementation();
		//			log.info("Unproxy entity: \"{}\" >> \"{}\"", entity.getClass().getSimpleName(), _entity.getClass().getSimpleName());
		//			return _entity;
		//		}
		//	}
		//	log.info("Unproxy entity: \"{}\" is not HibernateProxy", entity.getClass().getSimpleName());
		//	return entity;
	}

	/**
	 * Run command in transaction.
	 */
	@Override
	public <T> T runInTransaction(final Callable<T> command) {
		return sessionHelper.runInTransaction(command);
	}

	/**
	 * Run command in transaction.
	 */
	@Override
	public void runInTransaction(final Runnable command) {
		sessionHelper.runInTransaction(command);
	}

	/**
	 * Provides information of the logged-in user from HTTP session.
	 *
	 * @return User information
	 */
	@NonNull
	protected Optional<UserDetails> getUserDetails() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.map(Authentication::getPrincipal)
				.filter(UserDetails.class::isInstance)
				.map(UserDetails.class::cast);
	}

	/**
	 * Provides logged-in user from HTTP session.
	 *
	 * @return User entity
	 */
	@NonNull
	protected Optional<User> getLoggedInUser() {
		return getUserDetails().map(UserDetails::getLoggedInUser);
	}

	/**
	 * Returns {@code true} if logged-in user has superadmin role.
	 *
	 * @return User entity
	 */
	protected boolean isLoggedInUserSuperadmin() {
		return getUserDetails()
				.map(d -> d.hasAuthority(UserDetails.SUPERADMIN_ROLE))
				.orElse(Boolean.FALSE);
	}
}
