package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.core.security.UserDetails;
import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.web.service.ContestViewServices;
import cz.lhotatrophy.web.service.LocationViewServices;
import cz.lhotatrophy.web.service.TaskViewServices;
import cz.lhotatrophy.web.service.ViewServices;
import java.util.Optional;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

/**
 *
 * @author Petr Vogl
 */
public abstract class AbstractController {

	private static final ViewServices services = ViewServices.instance();
	private static ApplicationConfig appConfig;

	@Autowired
	private transient ApplicationContext applicationContext;
	@Autowired
	protected transient UserService userService;

	private <T> T getBean(final Class<T> cls) {
		return (T) applicationContext.getBean(cls);
	}

	private ViewServices getViewServices() {
		if (services.isInitialized()) {
			return services;
		}
		services.setContest(getBean(ContestViewServices.class));
		services.setLocation(getBean(LocationViewServices.class));
		services.setTask(getBean(TaskViewServices.class));
		services.setInitialized();
		return services;
	}

	protected ApplicationConfig getApplicationConfig() {
		if (appConfig == null) {
			appConfig = getBean(ApplicationConfig.class);
		}
		return appConfig;
	}

	/**
	 * Performs web page model initialization.
	 *
	 * @param model Model attributes holder
	 */
	protected void initModel(final Model model) {
		// view services
		model.addAttribute("service", getViewServices());
		// global configuration
		model.addAttribute("appConfig", getApplicationConfig());
		// effective user and team
		final Optional<User> optUser = userService.getEffectiveUser();
		final Optional<Team> optTeam = optUser.map(User::getTeam);
		model.addAttribute("user", optUser.orElse(null));
		model.addAttribute("team", optTeam.orElse(null));
		model.addAttribute("userDetails", getUserDetails().orElse(null));
	}

	/**
	 * Performs web page model initialization.
	 *
	 * @param model Model attributes holder
	 * @param initializer Model initializer
	 */
	protected void initModel(final Model model, final UnaryOperator<Model> initializer) {
		// basic initialization
		initModel(model);
		// custom initialization
		if (initializer != null) {
			initializer.apply(model);
		}
	}

	/**
	 * Provides information of the logged-in user from HTTP session.
	 *
	 * @return User information
	 */
	@Nonnull
	protected Optional<UserDetails> getUserDetails() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.map(Authentication::getPrincipal)
				.filter(UserDetails.class::isInstance)
				.map(UserDetails.class::cast);
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
