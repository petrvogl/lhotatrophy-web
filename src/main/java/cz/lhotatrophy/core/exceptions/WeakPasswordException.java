package cz.lhotatrophy.core.exceptions;

/**
 *
 * @author Petr Vogl
 */
public class WeakPasswordException extends Exception {

	public WeakPasswordException() {
		super("Heslo musí mít nejméně 6 znaků.");
	}

	public WeakPasswordException(final String message) {
		super(message);
	}
}
