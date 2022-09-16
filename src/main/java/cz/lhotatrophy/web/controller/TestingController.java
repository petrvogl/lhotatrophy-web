package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.exceptions.UsernameOrEmailIsTakenException;
import cz.lhotatrophy.core.exceptions.WeakPasswordException;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.web.form.TeamRegistrationForm;
import cz.lhotatrophy.web.form.TeamSettingsForm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Petr Vogl
 */
@Controller
@RequestMapping("/testing/v2")
@Log4j2
public class TestingController {

	@Autowired
	private transient UserService userService;
	@Autowired
	private transient TeamService teamService;

	/**
	 * Homepage
	 */
	@GetMapping
	public String index(
			final TeamRegistrationForm teamRegistrationForm,
			final Model model
	) {
		log.info("HOMEPAGE");

		initModel(model);
		return "public-v2/index";
	}

	/**
	 * Registration
	 */
	@GetMapping("/register")
	public String register(
			final TeamRegistrationForm teamRegistrationForm
	) {
		log.info("TEAM REGISTRATION (GET)");
		return "public-v2/register";
	}

	/**
	 * Registration
	 */
	//@PostMapping
	@PostMapping("/register")
	public String registerPost(
			@Valid final TeamRegistrationForm teamRegistrationForm,
			final BindingResult bindingResult,
			final HttpServletRequest request,
			final Model model
	) {
		log.info("TEAM REGISTRATION (POST)");
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
			return "public-v2/register";
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
			return "public-v2/register";
		}
		// autologin and redirect
		userService.autologin(team.getOwner());
		request.getSession().setAttribute("TeamRegistrationSuccess", true);
		return "redirect:/testing/v2/muj-tym";
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

		final Mutable<List<TeamMember>> members = new MutableObject();

		userService.getLoggedInUser().ifPresent(user -> {
			if (user.getTeam() != null) {
				final Long teamId = user.getTeam().getId();
				teamService.getTeamById(teamId).ifPresent(team -> {
					List<TeamMember> members_ = team.getMembersOrdered();
					if (members_ == null || members_.isEmpty()) {
						members_ = new ArrayList(5);
						for (int i = 1; i <= 5; i++) {
							members_.add(new TeamMember());
						}
					}
					members.setValue(members_);
				});
			}
		});

		// data to fill the form
		model.addAttribute("teamSettings", members.getValue());

		initModel(model);
		return "public-v2/my-team";
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

		final Mutable<Set<TeamMember>> members = new MutableObject();
		userService.getLoggedInUser().ifPresent(user -> {
			userService.runInTransaction(() -> {
				final Long teamId = user.getTeam().getId();
				teamService.getTeamById(teamId).ifPresent(team -> {
					final Set<TeamMember> members_ = teamSettingsForm.getTeamMembers();
					members.setValue(members_);
					team.updateMembers(members_);
					teamService.updateTeam(team);
				});
			});
		});

		// data to fill the form
		model.addAttribute("teamSettings", members.getValue());
		request.getSession().setAttribute("TeamUpdateSuccess", true);
		initModel(model);
		return "public-v2/my-team";
	}

	private void initModel(final Model model) {

		// FIXME - udelat lepe
		final Map<String, Object> appConfig;
		{
			appConfig = new HashMap<>();
			// TOTO - system property
			appConfig.put("teamRegistrationLimit", 50L);
			model.addAttribute("appConfig", appConfig);
		}
		
		final Function<Team, User> getOwner = (t) -> {
			final Long userId = t.getOwner().getId();
			return userService.getUserById(userId).orElse(null);
		};

		// logged in user and team
		final Optional<User> optUser = userService.getLoggedInUser()
				.map(User::getId)
				.flatMap(userId -> userService.getUserById(userId));
		final Optional<Team> optTeam = optUser
				.map(User::getTeam)
				.map(Team::getId)
				.flatMap(teamId -> teamService.getTeamById(teamId));
		// set model
		model.addAttribute("user", optUser.orElse(null));
		model.addAttribute("team", optTeam.orElse(null));
		model.addAttribute("teamMembers", optTeam.map(Team::getMembers).orElse(Collections.emptySet()));
		model.addAttribute("getOwner", getOwner);
	}
}
