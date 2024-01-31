package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.exceptions.UsernameOrEmailIsTakenException;
import cz.lhotatrophy.core.exceptions.WeakPasswordException;
import cz.lhotatrophy.core.service.TeamListingQuerySpi;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.web.form.TeamRegistrationForm;
import cz.lhotatrophy.web.form.TeamSettingsForm;
import cz.lhotatrophy.web.form.UserPasswordRecoveryForm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author Petr Vogl
 */
@Controller
@Log4j2
public class MainController extends AbstractController {

	@Autowired
	private transient TeamService teamService;

	/**
	 * Homepage
	 */
	@GetMapping("/")
	public String index(
			final TeamRegistrationForm teamRegistrationForm,
			final Model model
	) {
		log.info("HOMEPAGE");
		initModel(model);
		return "public/index";
	}

	/**
	 * Login
	 */
	@GetMapping("/login")
	public String login(
			final Model model
	) {
		log.info("LOGIN");
		initModel(model);
		return "public/login";
	}

	/**
	 * Registration
	 */
	@GetMapping("/register")
	public String getRegistration(
			final TeamRegistrationForm teamRegistrationForm,
			final Model model
	) {
		log.info("TEAM REGISTRATION (GET)");
		initModel(model);
		if (!isLoggedInUserSuperadmin()) {
			// not allowed now
			return "redirect:/";
		}
		return "public/register";
	}

	/**
	 * Registration
	 */
	@PostMapping("/register")
	public String postRegistration(
			@Valid final TeamRegistrationForm teamRegistrationForm,
			final BindingResult bindingResult,
			final HttpServletRequest request,
			final Model model
	) {
		log.info("TEAM REGISTRATION (POST)");
		if (!isLoggedInUserSuperadmin()) {
			// not allowed now
			return "redirect:/";
		}
		final String email = teamRegistrationForm.getEmail().trim().toLowerCase();
		final String passwd = teamRegistrationForm.getPassword().trim();
		final String teamName = teamRegistrationForm.getTeamName().trim();
		// user credentials validation
		if (userService.getUserByEmail(email).isPresent()) {
			bindingResult.rejectValue(
					"email",
					"Exists",
					"Tým s touto e-mailovou adresou je už registrován.");
		}
		// Password lenght is validated by form-validation
		//	if (!userService.checkPasswordStrength(passwd)) {
		//		bindingResult.rejectValue(
		//				"password",
		//				"Strength",
		//				"Heslo musí mít nejméně 6 znaků.");
		//	}
		// team name validation
		if (teamService.getTeamByName(teamName).isPresent()) {
			bindingResult.rejectValue(
					"teamName",
					"Exists",
					"Tým s tímto názvem je už registrován.");
		}
		if (bindingResult.hasErrors()) {
			// log errors
			//	final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
			//	if (fieldErrors != null) {
			//		fieldErrors.stream()
			//				.forEach((error) -> {
			//					log.info("FieldError: {} [ {} \"{}\" ]", error.getField(), error.getCode(), error.getDefaultMessage());
			//				});
			//	}
			initModel(model);
			return "public/register";
		}
		// save new team
		final Team team;
		try {
			team = userService.runInTransaction(() -> {
				final User user_;
				{
					user_ = userService.registerNewUser(email, passwd);
					user_.addProperty("zaplaceno", false);
				}
				return teamService.registerNewTeam(teamName, user_);
			});
		} catch (final Exception ex) {
			if (ex instanceof UsernameOrEmailIsTakenException || ex instanceof WeakPasswordException) {
				bindingResult.reject(
						"GlobalError",
						ex.getMessage());
			} else {
				// something went terribly wrong
				log.error("User registration failed.", ex);
				bindingResult.reject(
						"GlobalError",
						"Něco se pokazilo, zkus se registrovat znovu.");
			}
			initModel(model);
			return "public/register";
		}
		// autologin and redirect
		userService.autologin(team.getOwner());
		request.getSession().setAttribute("TeamRegistrationSuccess", true);
		return "redirect:/muj-tym";
	}

	/**
	 * Password recovery
	 */
	@GetMapping("/zmena-hesla/{userId}/{token}")
	public String passwordRecovery(
			@PathVariable final Long userId,
			@PathVariable final String token,
			final UserPasswordRecoveryForm userPasswordRecoveryForm,
			final Model model
	) {
		log.info("PASSWORD RECOVERY (GET)");
		initModel(model);
		// user must exist
		final User user = userService.getUserByIdFromCache(userId).get();
		// token must be valid
		final boolean isTokenValid = user.getProperty("passwdRecoveryToken")
				.map(Object::toString)
				.filter(t -> t.equals(token))
				.isPresent();
		if (!isTokenValid) {
			throw new RuntimeException("Invalid password recovery token.");
		}
		userPasswordRecoveryForm.setId(userId);
		userPasswordRecoveryForm.setToken(token);
		return "public/password-recovery";
	}

	/**
	 * Password recovery
	 */
	@PostMapping("/zmena-hesla")
	public String postPasswordRecovery(
			@Valid final UserPasswordRecoveryForm userPasswordRecoveryForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("PASSWORD RECOVERY (POST)");
		final Long userId = userPasswordRecoveryForm.getId();
		final String token = userPasswordRecoveryForm.getToken();
		// user must exist
		final Optional<User> optUser = userService.getUserByIdFromCache(userId);
		// token must be valid
		final boolean isTokenValid = optUser
				.flatMap(user -> user.getProperty("passwdRecoveryToken"))
				.map(Object::toString)
				.filter(t -> t.equals(token))
				.isPresent();
		if (optUser.isEmpty() || !isTokenValid) {
			bindingResult.reject(
					"GlobalError",
					"Odkaz pro změnu hesla už není platný.");
		}
		if (bindingResult.hasErrors()) {
			initModel(model);
			return "public/password-recovery";
		}
		// set new password
		userService.getUserById(userId)
				.ifPresent(u -> {
					userService.encodeAndSetUserPassword(u, userPasswordRecoveryForm.getPassword());
					u.removeProperty("passwdRecoveryToken");
					userService.updateUser(u);
				});
		return "redirect:/muj-tym";
	}

	/**
	 * Team
	 */
	@GetMapping("/muj-tym")
	public String myTeam(
			final TeamSettingsForm teamSettingsForm,
			final Model model
	) {
		log.info("TEAM");
		final Optional<User> optUser = userService.getEffectiveUser();
		final Optional<Team> optTeam = optUser.map(User::getTeam);

		final Mutable<List<TeamMember>> members = new MutableObject();
		optTeam.ifPresent(team -> {
			List<TeamMember> members_ = team.getMembersOrdered();
			if (members_ == null || members_.isEmpty()) {
				members_ = new ArrayList(5);
				for (int i = 1; i <= 5; i++) {
					members_.add(new TeamMember());
				}
			}
			members.setValue(members_);
		});

		initModel(model);
		// data to fill the form
		model.addAttribute("teamSettings", members.getValue());
		model.addAttribute("teamMembers", teamService.getEffectiveTeam().map(Team::getMembers).orElse(Collections.emptySet()));
		return "public/my-team";
	}

	/**
	 * Team settings edit
	 */
	@PostMapping("/muj-tym")
	public String editMyTeam(
			final TeamSettingsForm teamSettingsForm,
			final HttpServletRequest request,
			final Model model
	) {
		log.info("TEAM EDIT (POST)");
		if (true) {
			// not allowed now
			return "redirect:/muj-tym";
		}
		final Optional<User> optUser = userService.getEffectiveUser();
		final Optional<Team> optTeam = optUser.map(User::getTeam);

		final Mutable<Set<TeamMember>> members = new MutableObject();
		optTeam.map(Team::getId).ifPresent(teamId -> {
			userService.runInTransaction(() -> {
				teamService.getTeamById(teamId).ifPresent(team -> {
					final Set<TeamMember> members_ = teamSettingsForm.getTeamMembers();
					members.setValue(members_);
					team.updateMembers(members_);
					teamService.updateTeam(team);
				});
			});
		});

		initModel(model);
		// data to fill the form
		model.addAttribute("teamSettings", members.getValue());
		request.getSession().setAttribute("TeamUpdateSuccess", true);
		model.addAttribute("teamMembers", teamService.getEffectiveTeam().map(Team::getMembers).orElse(Collections.emptySet()));
		return "public/my-team";
	}

	/**
	 * Team listing
	 */
	@GetMapping("/prihlasene-tymy")
	public String teamList(final Model model) {
		log.info("TEAM LISTING");
		initModel(model);
		model.addAttribute("teamListing", teamService.getTeamListing(TeamListingQuerySpi.create()));
		return "public/team-list";
	}

	/**
	 * Team standings
	 */
	@GetMapping("/vysledky")
	public String teamStandings(final Model model) {
		log.info("TEAM STANDINGS");
		initModel(model);
		return "public/team-standings";
	}
}
