package cz.lhotatrophy.core.exceptions;

/**
 *
 * @author Petr Vogl
 */
public class UsernameOrEmailIsTakenException extends Exception {

	public UsernameOrEmailIsTakenException() {
		super("Tým s touto e-mailovou adresou je už registrován.");
	}

	public UsernameOrEmailIsTakenException(final String message) {
		super(message);
	}
}
