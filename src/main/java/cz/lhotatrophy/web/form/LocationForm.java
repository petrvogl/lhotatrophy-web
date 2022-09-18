package cz.lhotatrophy.web.form;

import cz.lhotatrophy.persist.entity.Location;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
@Data
public class LocationForm {

	@NotBlank(message = "Vyplň kódové označení")
	@Size(max = 32, message = "Maximální délka kódu je 32 znaků")
	private String code;

	@NotBlank(message = "Vyplň název")
	@Size(max = 255, message = "Maximální délka názvu je 255 znaků")
	private String name;

	@Size(max = 512, message = "Maximální popisu polohy je 512 znaků")
	private String description;

	private Boolean active;

	public void setFrom(@NonNull final Location location) {
		code = location.getCode();
		name = location.getName();
		description = location.getDescription();
		active = location.getActive();
	}

	@Nonnull
	public Location toLocation() {
		final Location location = new Location();
		location.setActive(active);
		location.setCode(code);
		location.setName(name);
		location.setDescription(description);
		return location;
	}
}
