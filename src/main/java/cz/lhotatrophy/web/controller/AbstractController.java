package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.ApplicationConfig;
import cz.lhotatrophy.core.security.UserDetails;
import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.utils.DateTimeUtils;
import cz.lhotatrophy.web.service.ContestViewService;
import cz.lhotatrophy.web.service.LocationViewService;
import cz.lhotatrophy.web.service.TaskViewService;
import cz.lhotatrophy.web.service.ViewServices;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
		services.setContest(getBean(ContestViewService.class));
		services.setLocation(getBean(LocationViewService.class));
		services.setTask(getBean(TaskViewService.class));
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
		// view services and utils
		model.addAttribute("service", getViewServices());
		model.addAttribute("now", NowInstatntUtils.INSTANCE);
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

	protected boolean checkContestIsOn() {
		final Long limitSeconds = appConfig.getMaxOverLimitSeconds() + 1l;
		final Instant limit = appConfig.getGameStartInstant().plus(limitSeconds, ChronoUnit.SECONDS);
		return NowInstatntUtils.INSTANCE.isAfter(appConfig.getGameStartInstant())
				&& NowInstatntUtils.INSTANCE.isBefore(limit);
	}

	protected boolean checkContestIsOpen() {
		final Long limitSeconds = appConfig.getMaxOverLimitSeconds() + 1l;
		final Instant limit = appConfig.getGameStartInstant().plus(limitSeconds, ChronoUnit.SECONDS);
		return NowInstatntUtils.INSTANCE.isAfter(appConfig.getGameOpenInstant())
				&& NowInstatntUtils.INSTANCE.isBefore(limit);
	}

	/**
	 * Date utils object.
	 * <ul>
	 * <li>{@code now.isAfter('yyyy-MM-dd HH:mm')}</li>
	 * <li>{@code now.isBefore('yyyy-MM-dd HH:mm')}</li>
	 * <li>{@code now.isAfter(Instant)}</li>
	 * <li>{@code now.isBefore(Instant)}</li>
	 * <ul/>
	 */
	private static class NowInstatntUtils {

		static final NowInstatntUtils INSTANCE = new NowInstatntUtils();

		public boolean isAfter(final String date) {
			if (date == null) {
				return false;
			}
			final LocalDateTime _date = DateTimeUtils.parse(date, DateTimeUtils.YYYY_MM_DD__HH_MM);
			if (_date == null) {
				return false;
			}
			final Instant instant = DateTimeUtils.toInstant(_date);
			return Instant.now().isAfter(instant);
		}

		public boolean isAfter(final Instant instant) {
			if (instant == null) {
				return false;
			}
			return Instant.now().isAfter(instant);
		}

		public boolean isBefore(final String date) {
			if (date == null) {
				return false;
			}
			final LocalDateTime _date = DateTimeUtils.parse(date, DateTimeUtils.YYYY_MM_DD__HH_MM);
			if (_date == null) {
				return false;
			}
			final Instant instant = DateTimeUtils.toInstant(_date);
			return Instant.now().isBefore(instant);
		}

		public boolean isBefore(final Instant instant) {
			if (instant == null) {
				return false;
			}
			return Instant.now().isBefore(instant);
		}
	}
}
