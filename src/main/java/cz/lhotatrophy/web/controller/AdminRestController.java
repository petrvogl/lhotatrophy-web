package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.service.TeamService;
import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.User;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Petr Vogl
 */
@RestController
@RequestMapping("/rest/admin")
@Log4j2
public class AdminRestController {

	@Autowired
	private UserService userService;
	@Autowired
	private TeamService teamService;
	
	@PostMapping("/setUserProperties/{userId}")
	public Object setUserProperty(
			@PathVariable Long userId,
			@RequestBody Map<String, Object> properties
	) {
		log.info("REST ADMIN (setUserProperties)");
		if (properties == null) {
			return null;
		}
		final User u = userService.getUserById(userId)
				.map(user -> {
					properties.entrySet().stream().forEach(entry -> {
						user.addProperty(entry.getKey(), entry.getValue());
						log.info("user.addProperty(\"{}\", {})", entry.getKey(), entry.getValue());
					});
					userService.updateUser(user);
					teamService.removeTeamFromCache(user.getTeam().getId());
					return user;
				})
				.orElse(null);
		return Map.of("success", u != null);
	}
}
