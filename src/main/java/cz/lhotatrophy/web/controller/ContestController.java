package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.service.ContestService;
import cz.lhotatrophy.core.service.EntityCacheService;
import cz.lhotatrophy.core.service.FileStoreService;
import cz.lhotatrophy.core.service.LocationListingQuerySpi;
import cz.lhotatrophy.core.service.LocationService;
import cz.lhotatrophy.core.service.TaskService;
import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.TaskTypeEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamContestProgress;
import cz.lhotatrophy.persist.entity.TeamContestProgressCode;
import cz.lhotatrophy.persist.filestore.FileStoreEnum;
import cz.lhotatrophy.web.form.ConfirmationForm;
import cz.lhotatrophy.web.form.FileUploadForm;
import cz.lhotatrophy.web.form.HintRevealForm;
import cz.lhotatrophy.web.form.SubmitCodeForm;
import cz.lhotatrophy.web.form.SubmitMileageForm;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

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
	@Autowired
	private transient FileStoreService fileStoreService;

	private static final String START_MILEAGE_FORM_TYPE = "start";
	private static final String FINISH_MILEAGE_FORM_TYPE = "finish";
	private static final String HINT_REVEAL_FORM_TYPE = "hint";
	private static final String PROCEDURE_REVEAL_FORM_TYPE = "procedure";
	private static final String SOLUTION_REVEAL_FORM_TYPE = "solution";

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
			@ModelAttribute("hintRevealForm") final HintRevealForm hintRevealForm,
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
		// NOTE: HintRevealForm is initialized in template
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
			final ConfirmationForm confirmationForm,
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
			final ConfirmationForm confirmationForm,
			final SubmitMileageForm submitMileageForm,
			final FileUploadForm fileUploadForm,
			final Model model
	) {
		if (!checkContestIsOn()) {
			return "redirect:/v-terenu";
		}
		log.info("DESTINATION");
		final Optional<Location> optLocation = locationService.getLocationByCodeFromCache("DEST");
		if (optLocation.isEmpty()) {
			// location does not exists
			return "redirect:/v-terenu";
		}
		submitMileageForm.setType(FINISH_MILEAGE_FORM_TYPE);
		initModel(model);
		model.addAttribute("location", optLocation.get());
		return "public/contest-destination";
	}

	/**
	 * Submit contest code
	 */
	@PostMapping("/submitCode")
	public String submitCode(
			final SubmitCodeForm submitCodeForm
	) {
		if (!checkContestIsOn() || !checkTeamIsInPlay()) {
			return "redirect:/v-terenu";
		}
		log.info("SUBMIT CODE");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		final TaskTypeEnum taskType = submitCodeForm.getType();
		final Optional<Task> optTask = Optional.ofNullable(submitCodeForm.getTaskCode())
				.flatMap(code -> taskService.getTaskByCodeFromCache(code));
		final String solution = submitCodeForm.getSolution();
		if (optTeam.isEmpty() || taskType == null || (taskType != TaskTypeEnum.C_CODE && optTask.isEmpty())) {
			// not valid
			return "redirect:/v-terenu";
		}
		final Team team = optTeam.get();
		// verify C code
		if (taskType == TaskTypeEnum.C_CODE) {
			if (solution != null) {

				contestService.acceptSolution(solution, taskType, team);
				//
				// TODO - log if accepted
				//
			}
			return "redirect:/v-terenu/c-kody";
		}
		// verify A/B code
		final Task task = optTask.get();
		if (solution != null) {
			if (!contestService.checkTaskIsCompleted(task, team)) {
				contestService.acceptSolution(solution, task, team);
				//
				// TODO - log if accepted
				//
			}
		}
		final Location location = taskService.getLocationRelatedToTask(task).orElse(null);
		return "redirect:/v-terenu/stanoviste-" + location.getCode();
	}

	/**
	 * Submit contest code
	 */
	@PostMapping("/submitMileage")
	public String submitMileage(
			@Valid final SubmitMileageForm submitMileageForm,
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
			try {
				final MultipartFile file = submitMileageForm.getFile();
				if (file == null || StringUtils.isEmpty(file.getOriginalFilename()) || file.getInputStream() == null) {
					bindingResult.rejectValue("file", "NotFound", "Nahraj fotografii tachometru");
				}
				if (!bindingResult.hasErrors()) {
					// save image
					fileStoreService.store(FileStoreEnum.USER_UPLOAD, file.getInputStream(), team.getStartPhotoName());
					// save mileage
					contestService.setMileageAtStart(team, submitMileageForm.getMileage());
					teamService.removeTeamFromCache(team.getId());
				}
			} catch (final IOException ex) {
				log.error("Error saving image uploaded by team [{}]: \n  [{}]\n  [{}]",
						team.getId(), ex.getMessage(), Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElse(""));
				bindingResult.rejectValue("file", "StorageError", "Fotografii se nepodařilo uložit, zkuste to znovu");
			}
			if (bindingResult.hasErrors()) {
				initModel(model);
				return "public/contest";
			}
		} else if (FINISH_MILEAGE_FORM_TYPE.equals(submitMileageForm.getType()) && team.getContestProgress().getMileageAtFinish() == null) {
			if (contestService.checkTeamIsInPlay(team)) {
				final TeamContestProgress contestProgress = team.getContestProgress();
				final String solution = StringUtils.trimToNull(submitMileageForm.getDestCode());
				final Integer mileage = submitMileageForm.getMileage();
				if (solution == null) {
					bindingResult.rejectValue("destCode", "Invalid", "Musí být zadán cílový kód");
				}
				if (mileage == null || mileage < contestProgress.getMileageAtStart()) {
					bindingResult.rejectValue("mileage", "Invalid", "Stav tachometru musí být vyšší než na startu");
				}
				if (!bindingResult.hasErrors() && contestProgress.getDestinationSolution() == null) {
					final boolean accepted = contestService.acceptDestinationSolution(solution, team);
					if (!accepted) {
						bindingResult.rejectValue("destCode", "Invalid", "Cílový kód není správný");
					}
				}
				if (!bindingResult.hasErrors()) {
					final boolean updated = contestService.setMileageAtFinish(team, mileage);
					if (!updated) {
						bindingResult.rejectValue("mileage", "Invalid", "Stav tachometru není validní");
					}
				}
				if (bindingResult.hasErrors()) {
					initModel(model);
					final Optional<Location> optLocation = locationService.getLocationByCodeFromCache("DEST");
					model.addAttribute("location", optLocation.orElse(null));
					model.addAttribute("fileUploadForm", new FileUploadForm());
					return "public/contest-destination";
				}
			}
			return "redirect:/v-terenu/cil";
		} else {
			// ignoring invalid form and invalid states
		}
		return "redirect:/v-terenu";
	}

	/**
	 * Reveal hint, procedure or solution
	 */
	@PostMapping("/submitDestinationPhoto")
	public String submitDestinationPhoto(
			@Valid final FileUploadForm fileUploadForm,
			final BindingResult bindingResult,
			final Model model
	) {
		if (!checkContestIsOpen()) {
			return "redirect:/v-terenu";
		}
		log.info("SUBMIT DESTINATION PHOTO");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		if (optTeam.isEmpty()) {
			// logged in user has no team so cannot compete
			return "redirect:/";
		}
		final Team team = optTeam.get();
		try {
			final MultipartFile file = fileUploadForm.getFile();
			if (file == null || StringUtils.isEmpty(file.getOriginalFilename()) || file.getInputStream() == null) {
				bindingResult.rejectValue("file", "NotFound", "Nahraj fotografii tachometru");
			}
			if (!bindingResult.hasErrors()) {
				// save image
				fileStoreService.store(FileStoreEnum.USER_UPLOAD, file.getInputStream(), team.getFinishPhotoName());
				teamService.removeTeamFromCache(team.getId());
			}
		} catch (final IOException ex) {
			log.error("Error saving image uploaded by team [{}]: \n  [{}]\n  [{}]",
					team.getId(), ex.getMessage(), Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElse(""));
			bindingResult.rejectValue("file", "StorageError", "Fotografii se nepodařilo uložit, zkuste to znovu");
		}
		if (bindingResult.hasErrors()) {
			initModel(model);
			final Optional<Location> optLocation = locationService.getLocationByCodeFromCache("DEST");
			model.addAttribute("location", optLocation.orElse(null));
			model.addAttribute("submitMileageForm", new SubmitMileageForm());
			return "public/contest-destination";
		}
		return "redirect:/v-terenu/cil";
	}

	/**
	 * Reveal hint, procedure or solution
	 */
	@PostMapping("/revealHint")
	public String revealHint(
			final HintRevealForm hintRevealForm
	) {
		if (!checkContestIsOn() || !checkTeamIsInPlay()) {
			return "redirect:/v-terenu";
		}
		log.info("REVEAL HINT");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		final Optional<Task> optTask = Optional.ofNullable(hintRevealForm.getTaskCode())
				.flatMap(code -> taskService.getTaskByCodeFromCache(code));
		if (optTeam.isEmpty() || optTask.isEmpty() || hintRevealForm.getType() == null) {
			// task does not exist or logged in user has no team so cannot compete
			return "redirect:/v-terenu";
		}
		final Team team = optTeam.get();
		final Task task = optTask.get();
		final TeamContestProgress contestProgress = team.getContestProgress();
		final TeamContestProgressCode contestCode = contestProgress.getContestCode(task.getCode());
		switch (hintRevealForm.getType()) {
			case HINT_REVEAL_FORM_TYPE:
				if ((contestCode == null || !contestCode.revealed(true, false, false))
						&& contestService.revealSolutionHint(task, team)) {
					//
					// TODO - log
					//
				}
				break;
			case PROCEDURE_REVEAL_FORM_TYPE:
				if ((contestCode == null || !contestCode.revealed(false, true, false))
						&& contestService.revealSolutionProcedure(task, team)) {
					//
					// TODO - log
					//
				}
				break;
			case SOLUTION_REVEAL_FORM_TYPE:
				if ((contestCode == null || !contestCode.revealed(false, false, true))
						&& contestService.revealSolution(task, team)) {
					//
					// TODO - log
					//
				}
				break;
			default:
				// ignoring invalid form and invalid states
				return "redirect:/v-terenu";
		}
		final Location location = taskService.getLocationRelatedToTask(task).orElse(null);
		return "redirect:/v-terenu/stanoviste-" + location.getCode();
	}

	/**
	 * Buy insurance
	 */
	@PostMapping("/buyInsurance")
	public String buyInsurance(
			final ConfirmationForm confirmationForm
	) {
		if (!checkContestIsOn() || !checkTeamIsInPlay()) {
			return "redirect:/v-terenu";
		}
		log.info("BUY INSURANCE");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		if (optTeam.isEmpty()) {
			// logged in user has no team so cannot compete
			return "redirect:/v-terenu";
		}
		final Team team = optTeam.get();
		if (!contestService.checkHasInsurance(team)) {
			// insurance not obtained yet
			if (contestService.buyInsurance(team)) {
				//
				// TODO - log
				//
			}
		}
		return "redirect:/v-terenu/c-kody";
	}

	/**
	 * Reveal destination
	 */
	@PostMapping("/revealDestination")
	public String revealDestination(
			final ConfirmationForm confirmationForm
	) {
		if (!checkContestIsOn() || !checkTeamIsInPlay()) {
			return "redirect:/v-terenu";
		}
		log.info("REVEAL DESTINATION");
		final Optional<Team> optTeam = teamService.getEffectiveTeam();
		if (optTeam.isEmpty()) {
			// logged in user has no team so cannot compete
			return "redirect:/v-terenu";
		}
		final Team team = optTeam.get();
		if (!contestService.checkDestinationRevealed(team)) {
			// insurance not obtained yet
			if (contestService.revealDestination(team)) {
				//
				// TODO - log
				//
			}
		}
		return "redirect:/v-terenu/cil";
	}

	private boolean checkTeamIsInPlay() {
		// check in the context of the effective user
		return teamService.getEffectiveTeam()
				.map(team -> contestService.checkTeamIsInPlay(team))
				.orElse(false);
	}
}
