package cz.lhotatrophy.web.controller;

import cz.lhotatrophy.core.service.UserService;
import cz.lhotatrophy.persist.entity.User;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
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

	@PostMapping("/setUserProperties/{userId}")
	public Object setUserProperties(
			@PathVariable final Long userId,
			@RequestBody final Map<String, Object> properties
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
					return user;
				})
				.orElse(null);
		return Map.of("success", u != null);
	}

	@PostMapping("/getPasswdRecoveryLink/{userId}")
	public Object getPasswdRecoveryLink(
			@PathVariable final Long userId
	) {
		log.info("REST ADMIN (getPasswdRecoveryLink)");

		final Mutable<String> token = new MutableObject<>();
		final User u = userService.getUserById(userId)
				.map(user -> {
					token.setValue(RandomStringUtils.randomAlphanumeric(10));
					user.addProperty("passwdRecoveryToken", token.getValue());
					userService.updateUser(user);
					return user;
				})
				.orElse(null);
		return Map.of("success", u != null, "token", token.getValue());
	}
}
