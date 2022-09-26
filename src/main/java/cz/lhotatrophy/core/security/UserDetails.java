package cz.lhotatrophy.core.security;

import cz.lhotatrophy.persist.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Provides core user information.
 *
 * @author Petr Vogl
 * @see UserDetailsServiceImpl
 */
public interface UserDetails extends org.springframework.security.core.userdetails.UserDetails {

	public static final GrantedAuthority SUPERADMIN_ROLE = new SimpleGrantedAuthority("SUPERADMIN");
	public static final GrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ADMIN");
	public static final GrantedAuthority USER_ROLE = new SimpleGrantedAuthority("USER");

	/**
	 * Returns {@code true} if the user has been granted the specified
	 * authority.
	 *
	 * @param authority authority
	 * @return Whether the user has been granted authority
	 */
	boolean hasAuthority(final GrantedAuthority authority);

	/**
	 * Returns {@code true} if the user has been granted the specified
	 * authority.
	 *
	 * @param authority authority
	 * @return Whether the user has been granted authority
	 */
	boolean hasAuthority(final String authority);

	/**
	 * Provides the logged in user.
	 *
	 * @return User entity
	 */
	User getLoggedInUser();

	/**
	 * Provides the impersonated user if the user is switched, otherwise returns
	 * the logged in user.
	 *
	 * @return User entity
	 */
	User getEffectiveUser();

	/**
	 * Allows administrators to impersonate any other specific user. This allows
	 * actions to be taken on behalf of a specific user (but with the
	 * permissions of the currently authenticated user).
	 *
	 * @param user User entity
	 */
	void switchUser(User user);

	/**
	 * Allows switch back to the originally logged in user.
	 */
	void resetSwitch();

	/**
	 * Indicates whether the user is switched (impersonated).
	 *
	 * @return {@code true} if switched
	 */
	boolean isSwitched();
}
