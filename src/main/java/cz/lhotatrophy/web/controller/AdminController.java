package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.exceptions.UsernameOrEmailIsTakenException;
import cz.lhotatrophy.core.exceptions.WeakPasswordException;
import cz.lhotatrophy.core.service.ClueListingQuerySpi;
import cz.lhotatrophy.core.service.ClueService;
import cz.lhotatrophy.core.service.EntityCacheService;
import cz.lhotatrophy.core.service.LocationListingQuerySpi;
import cz.lhotatrophy.core.service.LocationService;
import cz.lhotatrophy.core.service.TaskListingQuerySpi;
import cz.lhotatrophy.core.service.TaskService;
import cz.lhotatrophy.core.service.TeamListingQuerySpi;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.Clue;
import cz.lhotatrophy.persist.entity.FridayOfferEnum;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.SaturdayOfferEnum;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TshirtOfferEnum;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.web.form.ClueForm;
import cz.lhotatrophy.web.form.LocationForm;
import cz.lhotatrophy.web.form.TaskForm;
import cz.lhotatrophy.web.form.UserRegistrationForm;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableInt;
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
	private transient EntityCacheService cacheService;
	@Autowired
	private transient UserService userService;
	@Autowired
	private transient TeamService teamService;
	@Autowired
	private transient TaskService taskService;
	@Autowired
	private transient LocationService locationService;
	@Autowired
	private transient ClueService clueService;

	/**
	 * Admin homepage
	 */
	@GetMapping()
	public String index(final Model model) {
		log.info("ADMIN");
		model.addAttribute("teamListing", teamService.getTeamListing(TeamListingQuerySpi.create()));
		model.addAttribute("fridayTotal", new MutableInt(0));
		model.addAttribute("saturdayTotal", new MutableInt(0));
		return "admin/index";
	}

	/**
	 * Orders
	 */
	@GetMapping("/orders")
	public String orders(final Model model) {
		log.info("ORDERS");
		model.addAttribute("fridayOrders", teamService.getTeamOrdersFrequency(FridayOfferEnum.class));
		model.addAttribute("saturdayOrders", teamService.getTeamOrdersFrequency(SaturdayOfferEnum.class));
		model.addAttribute("tshirtOrders", teamService.getTeamOrdersFrequency(TshirtOfferEnum.class));
		return "admin/orders";
	}

	/**
	 * Detailed user information page
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
	 * Contest tasks listing
	 */
	@GetMapping("/tasks")
	public String getTasks(
			final TaskForm taskForm,
			final Model model
	) {
		log.info("TASKS");
		final TaskListingQuerySpi query = TaskListingQuerySpi.create().setSorting(Task.orderByCode());
		final List<Task> taskListing = taskService.getTaskListing(query);
		model.addAttribute("taskListing", taskListing);
		// render template
		return "admin/tasks";
	}

	/**
	 * Contest tasks editor
	 */
	@GetMapping("/tasks/task-{taskId}")
	public String getTask(
			@PathVariable Long taskId,
			final TaskForm taskForm,
			final Model model
	) {
		log.info("TASK [{}]", taskId);
		// verification of the task existence
		final Optional<Task> optTask = taskService.getTaskByIdFromCache(taskId);
		if (optTask.isEmpty()) {
			// task not found
			return "redirect:/admin/tasks";
		}
		// set model
		final Task task = optTask.get();
		taskForm.setFrom(task);
		model.addAttribute("task", task);
		// render template
		return "admin/task";
	}

	/**
	 * New contest task registration
	 */
	@PostMapping("/tasks")
	@SuppressWarnings("null")
	public String postNewTask(
			@Valid final TaskForm taskForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("NEW TASK");
		// task type validation
		final TaskTypeEnum type = TaskTypeEnum.valueOf(taskForm.getTypeMark());
		if (type == null) {
			bindingResult.rejectValue(
					"type",
					"Unknown",
					"Tento typ není platný");
		}
		if (bindingResult.hasErrors()) {
			final TaskListingQuerySpi query = TaskListingQuerySpi.create().setSorting(Task.orderByCode());
			final List<Task> taskListing = taskService.getTaskListing(query);
			model.addAttribute("taskListing", taskListing);
			return "admin/tasks";
		}
		// save new task
		try {
			taskService.registerNewTask(
					type,
					taskForm.getCode(),
					taskForm.getName(),
					taskForm.getSolutions(),
					taskForm.getSolutionHint(),
					taskForm.getSolutionProcedure(),
					Boolean.TRUE.equals(taskForm.getRevealSolutionAllowed()));
		} catch (final Exception ex) {
			// something went wrong
			log.error("Task registration failed.", ex);
			bindingResult.reject("GlobalError", ex.getMessage());
			final TaskListingQuerySpi query = TaskListingQuerySpi.create().setSorting(Task.orderByCode());
			final List<Task> taskListing = taskService.getTaskListing(query);
			model.addAttribute("taskListing", taskListing);
			return "admin/tasks";
		}
		// FIXME - clean only tasks listings
		cacheService.cleanEntityListingCache();
		// list all tasks
		return "redirect:/admin/tasks";
	}

	/**
	 * Update contest task
	 */
	@PostMapping("/tasks/task-{taskId}")
	public String postUpdatedTask(
			@PathVariable Long taskId,
			@Valid final TaskForm taskForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("EDIT TASK [{}]", taskId);
		// verification of the task existence
		final Optional<Task> optTask = taskService.getTaskByIdFromCache(taskId);
		if (optTask.isEmpty()) {
			// task not found
			return "redirect:/admin/tasks";
		}
		model.addAttribute("task", optTask.get());
		// task type validation
		final TaskTypeEnum type = TaskTypeEnum.valueOf(taskForm.getTypeMark());
		if (type == null) {
			bindingResult.rejectValue("type", "Unknown", "Tento typ není platný");
		}
		if (bindingResult.hasErrors()) {
			return "admin/task";
		}
		// update task
		try {
			final Task task = taskForm.toTask();
			task.setId(taskId);
			taskService.updateTask(task);
		} catch (final Exception ex) {
			// something went wrong
			log.error("Task update failed.", ex);
			bindingResult.reject("GlobalError", ex.getMessage());
			return "admin/task";
		}
		return "redirect:/admin/tasks";
	}

	/**
	 * Contest location listing
	 */
	@GetMapping("/locations")
	public String getLocations(
			final LocationForm locationForm,
			final Model model
	) {
		log.info("LOCATIONS");
		final LocationListingQuerySpi query = LocationListingQuerySpi.create().setSorting(Location.orderByCode());
		final List<Location> locationListing = locationService.getLocationListing(query);
		model.addAttribute("locationListing", locationListing);
		// render template
		return "admin/locations";
	}

	/**
	 * Contest locations editor
	 */
	@GetMapping("/locations/location-{locationId}")
	public String getLocation(
			@PathVariable Long locationId,
			final LocationForm locationForm,
			final Model model
	) {
		log.info("LOCATION [{}]", locationId);
		// verification of the location existence
		final Optional<Location> optLocation = locationService.getLocationByIdFromCache(locationId);
		if (optLocation.isEmpty()) {
			// location not found
			return "redirect:/admin/locations";
		}
		// set model
		final Location location = optLocation.get();
		locationForm.setFrom(location);
		model.addAttribute("location", location);
		// render template
		return "admin/location";
	}

	/**
	 * New contest location registration
	 */
	@PostMapping("/locations")
	public String postNewLocation(
			@Valid final LocationForm locationForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("NEW LOCATION");
		if (bindingResult.hasErrors()) {
			final LocationListingQuerySpi query = LocationListingQuerySpi.create().setSorting(Location.orderByCode());
			final List<Location> locationListing = locationService.getLocationListing(query);
			model.addAttribute("locationListing", locationListing);
			return "admin/locations";
		}
		// save new location
		try {
			locationService.registerNewLocation(
					locationForm.getCode(),
					locationForm.getName(),
					locationForm.getDescription());
		} catch (final Exception ex) {
			// something went wrong
			log.error("Location registration failed.", ex);
			bindingResult.reject("GlobalError", ex.getMessage());
			final LocationListingQuerySpi query = LocationListingQuerySpi.create().setSorting(Location.orderByCode());
			final List<Location> locationListing = locationService.getLocationListing(query);
			model.addAttribute("locationListing", locationListing);
			return "admin/locations";
		}
		// FIXME - clean only locations listings
		cacheService.cleanEntityListingCache();
		// list all locations
		return "redirect:/admin/locations";
	}

	/**
	 * Update contest location
	 */
	@PostMapping("/locations/location-{locationId}")
	public String postUpdatedLocation(
			@PathVariable Long locationId,
			@Valid final LocationForm locationForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("EDIT LOCATION [{}]", locationId);
		// verification of the location existence
		final Optional<Location> optLocation = locationService.getLocationByIdFromCache(locationId);
		if (optLocation.isEmpty()) {
			// location not found
			return "redirect:/admin/locations";
		}
		model.addAttribute("location", optLocation.get());
		if (bindingResult.hasErrors()) {
			return "admin/location";
		}
		// update location
		try {
			final Location location = locationForm.toLocation();
			location.setId(locationId);
			locationService.updateLocation(location);
		} catch (final Exception ex) {
			// something went wrong
			log.error("Location update failed.", ex);
			bindingResult.reject("GlobalError", ex.getMessage());
			return "admin/location";
		}
		return "redirect:/admin/locations";
	}

	/**
	 * Clue listing
	 */
	@GetMapping("/clues")
	public String getClues(
			final ClueForm clueForm,
			final Model model
	) {
		log.info("CLUES");
		final ClueListingQuerySpi query = ClueListingQuerySpi.create().setSorting(Clue.orderByCode());
		final List<Clue> clueListing = clueService.getClueListing(query);
		model.addAttribute("clueListing", clueListing);
		// render template
		return "admin/clues";
	}

	/**
	 * Clues editor
	 */
	@GetMapping("/clues/clue-{clueId}")
	public String getClue(
			@PathVariable Long clueId,
			final ClueForm clueForm,
			final Model model
	) {
		log.info("CLUE [{}]", clueId);
		// verification of the clue existence
		final Optional<Clue> optClue = clueService.getClueByIdFromCache(clueId);
		if (optClue.isEmpty()) {
			// clue not found
			return "redirect:/admin/clues";
		}
		// set model
		final Clue clue = optClue.get();
		clueForm.setFrom(clue);
		model.addAttribute("clue", clue);
		// render template
		return "admin/clue";
	}

	/**
	 * New clue registration
	 */
	@PostMapping("/clues")
	public String postNewClue(
			@Valid final ClueForm clueForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("NEW CLUE");
		if (bindingResult.hasErrors()) {
			final ClueListingQuerySpi query = ClueListingQuerySpi.create().setSorting(Clue.orderByCode());
			final List<Clue> clueListing = clueService.getClueListing(query);
			model.addAttribute("clueListing", clueListing);
			return "admin/clues";
		}
		// save new clue
		try {
			clueService.registerNewClue(
					clueForm.getCode(),
					clueForm.getDescription());
		} catch (final Exception ex) {
			// something went wrong
			log.error("Clue registration failed.", ex);
			bindingResult.reject("GlobalError", ex.getMessage());
			final ClueListingQuerySpi query = ClueListingQuerySpi.create().setSorting(Clue.orderByCode());
			final List<Clue> clueListing = clueService.getClueListing(query);
			model.addAttribute("clueListing", clueListing);
			return "admin/clues";
		}
		// FIXME - clean only clues listings
		cacheService.cleanEntityListingCache();
		// list all clues
		return "redirect:/admin/clues";
	}

	/**
	 * Update contest clue
	 */
	@PostMapping("/clues/clue-{clueId}")
	public String postUpdatedClue(
			@PathVariable Long clueId,
			@Valid final ClueForm clueForm,
			final BindingResult bindingResult,
			final Model model
	) {
		log.info("EDIT CLUE [{}]", clueId);
		// verification of the clue existence
		final Optional<Clue> optClue = clueService.getClueByIdFromCache(clueId);
		if (optClue.isEmpty()) {
			// clue not found
			return "redirect:/admin/clues";
		}
		model.addAttribute("clue", optClue.get());
		if (bindingResult.hasErrors()) {
			return "admin/clue";
		}
		// update clue
		try {
			final Clue clue = clueForm.toClue();
			clue.setId(clueId);
			clueService.updateClue(clue);
		} catch (final Exception ex) {
			// something went wrong
			log.error("Clue update failed.", ex);
			bindingResult.reject("GlobalError", ex.getMessage());
			return "admin/clue";
		}
		return "redirect:/admin/clues";
	}

	/**
	 * Admin user registration page
	 */
	@GetMapping("/register")
	public String getRegistration(
			final UserRegistrationForm userRegistrationForm
	) {
		log.info("ADMIN REGISTRATION (GET)");
		return "admin/register";
	}

	/**
	 * Admin user registration
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
