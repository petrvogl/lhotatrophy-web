package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.service.ContestService;
import cz.lhotatrophy.core.service.EntityCacheService;
import cz.lhotatrophy.core.service.LocationListingQuerySpi;
import cz.lhotatrophy.core.service.LocationService;
import cz.lhotatrophy.core.service.TaskService;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.web.form.SubmitCodeForm;
import cz.lhotatrophy.web.form.SubmitMileageForm;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Petr Vogl
 */
@Controller
@RequestMapping("/v-terenu")
@Log4j2
public class ContestController extends AbstractController {

	@Autowired
	private transient EntityCacheService cacheService;
	@Autowired
	private transient ContestService contestService;
	@Autowired
	private transient TeamService teamService;
	@Autowired
	private transient LocationService locationService;
	@Autowired
	private transient TaskService taskService;

	private static final String START_MILEAGE_FORM_TYPE = "start";
	private static final String FINISH_MILEAGE_FORM_TYPE = "finish";

	/**
	 * Contest dashboard
	 */
	@GetMapping()
	public String index(
			final SubmitMileageForm submitMileageForm,
			final Model model
	) {
		log.info("CONTEST INDEX");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		if (optTeam.isEmpty()) {
			// logged in user has no team so cannot compete
			return "redirect:/";
		}
		final Team team = optTeam.get();
		if (team.getContestProgress().getMileageAtStart() == null) {
			// team is at start
			submitMileageForm.setType(START_MILEAGE_FORM_TYPE);
		} else {
			// team is in play or finished
			final LocationListingQuerySpi query = LocationListingQuerySpi.create()
					.setActive(Boolean.TRUE)
					.setDestination(Boolean.FALSE)
					.setSorting(Location.orderByCode());
			final List<Location> locations = locationService.getLocationListing(query);
			model.addAttribute("locations", locations);
		}
		initModel(model);
		return "public/contest";
	}

	/**
	 * Location detail page
	 */
	@GetMapping("/stanoviste-{locationCode}")
	public String locationPage(
			@PathVariable String locationCode,
			@ModelAttribute("submitACodeForm") final SubmitCodeForm submitACodeForm,
			@ModelAttribute("submitBCodeForm") final SubmitCodeForm submitBCodeForm,
			final Model model
	) {
		if (!checkContestIsOn()) {
			return "redirect:/v-terenu";
		}
		log.info("LOCATION {}", locationCode);
		final Optional<Location> optLocation = locationService.getLocationByCodeFromCache(locationCode);
		if (optLocation.isEmpty()) {
			// location does not exists
			return "redirect:/v-terenu";
		}
		initModel(model);
		model.addAttribute("location", optLocation.get());
		return "public/contest-location";
	}

	/**
	 * C-codes page
	 */
	@GetMapping("/c-kody")
	public String cCodesPage(
			final SubmitCodeForm submitCodeForm,
			final Model model
	) {
		if (!checkContestIsOn()) {
			return "redirect:/v-terenu";
		}
		log.info("C-CODES");
		submitCodeForm.setTaskType("C");
		initModel(model);
		return "public/contest-c-codes";
	}

	/**
	 * Destination detail page
	 */
	@GetMapping("/cil")
	public String destinationPage(
			final Model model
	) {
		if (!checkContestIsOn()) {
			return "redirect:/v-terenu";
		}
		log.info("DESTINATION");
		initModel(model);
		return "public/contest-destination";
	}

	/**
	 * Submit contest code
	 */
	@PostMapping("/submitCode")
	public String submitCode(
			final SubmitCodeForm submitCodeForm,
			final Model model
	) {
		if (!checkContestIsOn()) {
			return "redirect:/v-terenu";
		}
		log.info("SUBMIT CODE");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		final TaskTypeEnum taskType = submitCodeForm.getType();
		final Optional<Task> optTask = Optional.ofNullable(submitCodeForm.getTaskCode())
				.flatMap(code -> taskService.getTaskByCodeFromCache(code));
		final String solution = submitCodeForm.getSolution();
		if (optTeam.isEmpty() || taskType == null || solution == null || (taskType != TaskTypeEnum.C_CODE && optTask.isEmpty())) {
			// not valid
			return "redirect:/v-terenu";
		}
		final Team team = optTeam.get();
		// verify C code
		if (taskType == TaskTypeEnum.C_CODE) {
			contestService.acceptSolution(solution, taskType, team);
			// TODO - log if accepted
			return "redirect:/v-terenu/c-kody";
		}
		// verify A/B code
		final Task task = optTask.get();
		if (!contestService.checkTaskIsCompleted(task, team)) {
			contestService.acceptSolution(solution, task, team);
			// TODO - log if accepted
		}
		final Location location = taskService.getLocationRelatedToTask(task).orElse(null);
		return "redirect:/v-terenu/stanoviste-" + location.getCode();
	}

	/**
	 * Submit contest code
	 */
	@PostMapping("/submitMileage")
	public String submitMileage(
			final SubmitMileageForm submitMileageForm,
			final BindingResult bindingResult,
			final Model model
	) {
		if (!checkContestIsOpen()) {
			return "redirect:/v-terenu";
		}
		log.info("SUBMIT MILEAGE");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		if (optTeam.isEmpty()) {
			// logged in user has no team so cannot compete
			return "redirect:/";
		}
		final Team team = optTeam.get();
		if (!team.isActive() || !team.getOwner().isActive() || team.isDisqualified()) {
			// not active or disqualified
			return "redirect:/v-terenu";
		}
		if (START_MILEAGE_FORM_TYPE.equals(submitMileageForm.getType()) && team.getContestProgress().getMileageAtStart() == null) {
			if (bindingResult.hasErrors()) {
				initModel(model);
				return "public/contest";
			}
			contestService.setMileageAtStart(team, submitMileageForm.getMileage());
			//
			// TODO - save image
			//
		} else if (FINISH_MILEAGE_FORM_TYPE.equals(submitMileageForm.getType()) && team.getContestProgress().getMileageAtFinish() == null) {
			if (bindingResult.hasErrors()) {
				initModel(model);
				return "public/contest-destination";
			}
			contestService.setMileageAtFinish(team, submitMileageForm.getMileage());
			//
			// TODO - save image
			//
			return "redirect:/v-terenu/cil";
		} else {
			// ignoring invalid form and invalid states
		}
		return "redirect:/v-terenu";
	}
}
