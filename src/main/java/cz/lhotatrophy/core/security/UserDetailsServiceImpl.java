package cz.lhotatrophy.core.security;

import cz.lhotatrophy.persist.dao.UserDao;
import cz.lhotatrophy.persist.entity.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service("userDetailsService")
@Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {

	/**
	 * Preconfigured superadmin username
	 */
	@Value("${user.authentication.authorized-superadmin:c7e546!bx18d#fe@}")
	private transient String authorizedSuperadmin;
	/**
	 * String mapping of all available authorities.
	 */
	private static final Map<String, GrantedAuthority> authoritiesLookup = new HashMap<>(3);

	@Autowired
	private transient UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		try {
			return userDao.findByEmail(username.toLowerCase())
					.map(UserDetailsImpl::new)
					.orElseThrow(() -> new UsernameNotFoundException("User by email not found."));
		} catch (final Exception e) {
			throw new UsernameNotFoundException("USER LOGIN FAILED: Unable to load/verify user's identity.", e);
		}
	}

	@SuppressWarnings("DoubleCheckedLocking")
	private static GrantedAuthority mapAuthority(final String authority) {
		if (StringUtils.isEmpty(authority)) {
			return null;
		}
		if (authoritiesLookup.isEmpty()) {
			synchronized (authoritiesLookup) {
				if (authoritiesLookup.isEmpty()) {
					authoritiesLookup.put(UserDetails.SUPERADMIN_ROLE.getAuthority(), UserDetails.SUPERADMIN_ROLE);
					authoritiesLookup.put(UserDetails.ADMIN_ROLE.getAuthority(), UserDetails.ADMIN_ROLE);
					authoritiesLookup.put(UserDetails.USER_ROLE.getAuthority(), UserDetails.USER_ROLE);
				}
			}
		}
		return authoritiesLookup.get(authority);
	}

	private class UserDetailsImpl implements UserDetails {

		private static final long serialVersionUID = 1L;
		private User loggedInUser;
		private User impersonatedUser;
		private final Set<GrantedAuthority> authorities;

		public UserDetailsImpl(@NonNull final User loggedInUser) {
			this.loggedInUser = loggedInUser;
			this.authorities = new LinkedHashSet();
			final GrantedAuthority ga = loggedInUser.isPrivileged()
					? ADMIN_ROLE
					: USER_ROLE;
			if (ga == ADMIN_ROLE && isAuthorizedSuperadmin()) {
				// username match with preconfigured superadmin
				this.authorities.add(SUPERADMIN_ROLE);
			}
			this.authorities.add(ga);
		}

		private boolean isAuthorizedSuperadmin() {
			return Objects.equals(loggedInUser.getEmail(), authorizedSuperadmin);
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return authorities;
		}

		@Override
		public boolean hasAuthority(final GrantedAuthority authority) {
			if (authority == null) {
				return false;
			}
			return authorities.contains(authority);
		}

		@Override
		public boolean hasAuthority(final String authority) {
			return hasAuthority(mapAuthority(authority));
		}

		@Override
		public String getPassword() {
			return loggedInUser.getPassword();
		}

		@Override
		public String getUsername() {
			return loggedInUser.getEmail();
		}

		@Override
		public boolean isAccountNonExpired() {
			return loggedInUser.isActive();
		}

		@Override
		public boolean isAccountNonLocked() {
			return loggedInUser.isActive();
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return loggedInUser.isActive();
		}

		@Override
		public boolean isEnabled() {
			return loggedInUser.isActive();
		}

		@Override
		public boolean isSwitched() {
			return impersonatedUser != null;
		}

		@Override
		public User getLoggedInUser() {
			return loggedInUser;
		}

		@Override
		public User getEffectiveUser() {
			return isSwitched() ? impersonatedUser : loggedInUser;
		}

		@Override
		public void switchUser(final User user) {
			if (user == null) {
				resetSwitch();
				return;
			}
			if (!loggedInUser.isPrivileged() || (user.isPrivileged() && !isAuthorizedSuperadmin())) {
				throw new RuntimeException("Impersonation is denied.");
			}
			impersonatedUser = user;
		}

		@Override
		public void resetSwitch() {
			impersonatedUser = null;
		}
	}
}
