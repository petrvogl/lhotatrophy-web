package cz.lhotatrophy.core.service;

import cz.lhotatrophy.core.security.UserDetails;
import cz.lhotatrophy.persist.SessionHelper;
import cz.lhotatrophy.persist.entity.User;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
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
	 * Flush current Hibernate session.
	 */
	@Override
	public void flush() {
		sessionHelper.flush();
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
	public Optional<UserDetails> getUserDetails() {
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
	public Optional<User> getLoggedInUser() {
		return getUserDetails().map(UserDetails::getLoggedInUser);
	}
	
	/**
	 * Returns {@code true} if logged-in user has superadmin role.
	 *
	 * @return User entity
	 */
	public boolean isLoggedInUserSuperadmin() {
		return getUserDetails()
				.map(d -> d.hasAuthority(UserDetails.SUPERADMIN_ROLE))
				.orElse(Boolean.FALSE);
	}
}
