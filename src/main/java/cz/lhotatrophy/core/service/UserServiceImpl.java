package cz.lhotatrophy.core.service;

import cz.lhotatrophy.core.exceptions.UsernameOrEmailIsTakenException;
import cz.lhotatrophy.core.exceptions.WeakPasswordException;
import cz.lhotatrophy.persist.dao.UserDao;
import cz.lhotatrophy.persist.entity.User;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class UserServiceImpl extends AbstractService implements UserService {

	@Autowired
	private transient UserDao userDao;
	@Autowired
	private transient PasswordEncoder passwordEncoder;
	@Autowired
	private transient UserDetailsService userDetailsService;
	@Autowired
	private transient EntityCacheService cacheService;

	@NonNull
	public User createUser(@NonNull final UnaryOperator<User> initializer) {
		return runInTransaction(() -> {
			// custom init
			final User user = initializer.apply(new User());
			// system init
			user.setCreated(System.currentTimeMillis());
			if (user.getPassword() == null) {
				// no password
				user.setPassword("*** NO PASSWORD ***");
			} else {
				// encrypted password
				encodeAndSetUserPassword(user, user.getPassword());
			}
			// save
			return userDao.save(user);
		});
	}

	@Override
	public void encodeAndSetUserPassword(
			@NonNull final User user,
			@NonNull final String password
	) {
		final String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);
	}

	@Override
	public Optional<User> getUserById(@NonNull final Long id) {
		return userDao.findById(id);
	}

	@Override
	public Optional<User> getUserByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, User.class);
	}

	@Override
	public void removeUserFromCache(@NonNull final Long id) {
		cacheService.removeFromCache(id, User.class);
	}

	/**
	 * @deprecated TODO - checkEmailIsFree(...)
	 */
	@Deprecated
	@Override
	public Optional<User> getUserByEmail(@NonNull final String email) {
		return userDao.findByEmail(email);
	}

	@Override
	public boolean checkPasswordStrength(final String password) {
		return (password != null) && (password.length() >= 6);
	}

	@NonNull
	@Override
	public User registerNewUser(
			@NonNull final String email,
			@NonNull final String password,
			final boolean setAdminAuthority,
			final boolean registrationAuthorized
	) throws WeakPasswordException, UsernameOrEmailIsTakenException {
		if (!checkPasswordStrength(password)) {
			throw new WeakPasswordException();
		}
		if (userDao.findByEmail(email).isPresent()) {
			throw new UsernameOrEmailIsTakenException();
		}
		return runInTransaction(() -> {
			final User user = createUser(u -> {
				u.setEmail(email);
				u.setPassword(password);
				u.setPrivileged(setAdminAuthority);
				u.setActive(registrationAuthorized);
				return u;
			});
			log.info("New user account has been created: [ {} / {} ]", user.getId(), user.getEmail());
			log.info("\nLhotaTrophy:\n    CREATED {}", user.toString());
			return user;
		});
	}

	@Override
	public void updateUser(@NonNull final User user) {
		final User u = userDao.save(user);
		removeUserFromCache(u.getId());
	}

	@Override
	public void updateUserProperties(@NonNull final Long id, final Map<String, Object> properties) {
		final User user = userDao.getReferenceById(id);
		user.setProperties(properties);
		final User u = userDao.save(user);
		removeUserFromCache(u.getId());
	}

	@Override
	public void autologin(@NonNull final User user) {
		final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
		final Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Override
	public void autologout(@NonNull final HttpServletRequest request) {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, null, auth);
		}
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
