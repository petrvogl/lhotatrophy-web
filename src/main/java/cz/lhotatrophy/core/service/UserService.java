package cz.lhotatrophy.core.service;

import cz.lhotatrophy.core.exceptions.UsernameOrEmailIsTakenException;
import cz.lhotatrophy.core.exceptions.WeakPasswordException;
import cz.lhotatrophy.core.security.UserDetails;
import cz.lhotatrophy.persist.entity.User;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface UserService extends Service {

	/**
	 * Provides {@code User} entity from persistence context.
	 *
	 * @param id User id
	 * @return Optional User entity
	 */
	Optional<User> getUserById(Long id);

	/**
	 * Provides {@code User} entity from cache.
	 *
	 * @param id User id
	 * @return Optional User entity
	 */
	Optional<User> getUserByIdFromCache(@NonNull Long id);

	/**
	 * Removes {@code User} entity from cache.
	 *
	 * @param id User id
	 */
	void removeUserFromCache(@NonNull final Long id);

	/**
	 * Provides {@code User} entity from persistence context.
	 *
	 * @param email User email address
	 * @return User entity
	 */
	Optional<User> getUserByEmail(String email);

	/**
	 * Returns {@code true} if password match security criteria; {@code false}
	 * otherwise.
	 *
	 * @param password User password
	 * @return True if passes the test
	 */
	boolean checkPasswordStrength(String password);

	/**
	 * Process (non admin) user registration.
	 *
	 * @param email User email address
	 * @param password User password
	 * @return User entity
	 */
	default User registerNewUser(
			String email,
			String password
	) throws WeakPasswordException, UsernameOrEmailIsTakenException {
		// Common user accounts are automatically activated
		return this.registerNewUser(email, password, false, true);
	}

	/**
	 * Process user registration. If this operation is not authorized, user
	 * account would not be automatically activated.
	 *
	 * @param email User email address
	 * @param password User password
	 * @param setAdminAuthority Whether to grant admin authority
	 * @param registrationAuthorized Signals whether the registration is
	 * authorized
	 * @return User entity
	 */
	User registerNewUser(
			String email,
			String password,
			boolean setAdminAuthority,
			boolean registrationAuthorized
	) throws WeakPasswordException, UsernameOrEmailIsTakenException;

	/**
	 *
	 * @param user
	 */
	void updateUser(@NonNull User user);

	/**
	 * Saves user properties.
	 *
	 * @param id User ID
	 * @param properties User properties
	 */
	void updateUserProperties(Long id, Map<String, Object> properties);

	/**
	 * Perform user login in current HTTP session.
	 *
	 * @param user User to be logged
	 */
	void autologin(User user);

	/**
	 * Perform user logout in current HTTP session.
	 *
	 * @param request Request from which to obtain a HTTP session
	 */
	void autologout(HttpServletRequest request);

	/**
	 * Provides information of the logged-in user from HTTP session.
	 *
	 * @return User information
	 */
	Optional<UserDetails> getUserDetails();

	/**
	 * Provides logged-in user from HTTP session.
	 *
	 * @return User entity
	 */
	Optional<User> getLoggedInUser();

	/**
	 * Returns {@code true} if logged-in user has superadmin role.
	 *
	 * @return User entity
	 */
	boolean isLoggedInUserSuperadmin();
}
