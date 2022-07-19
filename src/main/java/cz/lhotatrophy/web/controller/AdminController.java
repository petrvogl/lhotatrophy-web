package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.exceptions.UsernameOrEmailIsTakenException;
import cz.lhotatrophy.core.exceptions.WeakPasswordException;
import cz.lhotatrophy.core.service.TeamListingQuery;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.web.form.UserRegistrationForm;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Petr Vogl
 */
@Controller
@RequestMapping("/admin")
@Log4j2
public class AdminController {

	@Autowired
	private UserService userService;
	@Autowired
	private TeamService teamService;

	/**
	 * Admin homepage
	 */
	@GetMapping()
	public String index(final Model model) {
		log.info("ADMIN");
		model.addAttribute("teamListing", teamService.getTeamListing(new TeamListingQuery()));
		return "admin/index";
	}

	/**
	 * Registration
	 */
	@GetMapping("/user-info/{userId}")
	public String userInfo(
			@PathVariable Long userId,
			final Model model
	) {
		log.info("USER INFO [" + userId + "]");

		// logged in user and team
		final Optional<User> optUser = userService.getUserByIdFromCache(userId);
		final Optional<Team> optTeam = optUser
				.map(User::getTeam)
				.map(Team::getId)
				.flatMap(teamId -> teamService.getTeamByIdFromCache(teamId));
		// set data
		model.addAttribute("user", optUser.orElse(null));
		model.addAttribute("team", optTeam.orElse(null));
		// render template
		return "admin/user-info";
	}

	/**
	 * Registration
	 */
	@GetMapping("/register")
	public String getRegistration(
			final UserRegistrationForm userRegistrationForm
	) {
		log.info("ADMIN REGISTRATION (GET)");
		return "admin/register";
	}

	/**
	 * Registration
	 */
	@PostMapping("/register")
	public String postRegistration(
			@Valid final UserRegistrationForm userRegistrationForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("ADMIN REGISTRATION (POST)");
		final String email = userRegistrationForm.getEmail().trim().toLowerCase();
		final String passwd = userRegistrationForm.getPassword().trim();
		// user credentials validation
		if (userService.getUserByEmail(email).isPresent()) {
			bindingResult.rejectValue(
					"email",
					"Exists",
					"Uživatel s touto e-mailovou adresou je už registrován.");
		}
		// Password lenght is validated by form-validation
		//	if (!userService.checkPasswordStrength(passwd)) {
		//		bindingResult.rejectValue(
		//				"password",
		//				"Strength",
		//				"Heslo musí mít nejméně 6 znaků.");
		//	}
		if (bindingResult.hasErrors()) {
			return "admin/register";
		}
		// save new user
		final User user;
		try {
			// registration by superadmin is authorized; self registration is not
			final boolean authorized = userService.isLoggedInUserSuperadmin();
			user = userService.registerNewUser(email, passwd, true, authorized);
		} catch (final UsernameOrEmailIsTakenException | WeakPasswordException ex) {
			bindingResult.reject(
					"GlobalError",
					ex.getMessage());
			return "admin/register";
		} catch (final Exception ex) {
			// something went terribly wrong
			log.error("User registration failed.", ex);
			bindingResult.reject(
					"GlobalError",
					"Něco se pokazilo, zkus se registrovat znovu.");
			return "admin/register";
		}
		// success message
		model.addAttribute("user", user);
		return "admin/register";
	}
}
