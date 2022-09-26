package cz.lhotatrophy.web.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Petr Vogl
 */
@Getter
@Setter
public final class ViewServices {

	/**
	 * Singleton.
	 */
	private static final ViewServices INSTANCE = new ViewServices();

	/**
	 * Returns an instance.
	 *
	 * @return an instance
	 */
	public static ViewServices instance() {
		return INSTANCE;
	}

	private ContestViewServices contest;
	private LocationViewServices location;
	private TaskViewServices task;

	@Setter(AccessLevel.NONE)
	private boolean initialized;

	private ViewServices() {
		initialized = false;
	}

	public void setInitialized() {
		this.initialized = true;
	}
}
