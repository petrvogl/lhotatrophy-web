package cz.lhotatrophy.utils;

import java.util.Optional;

/**
 * Enum utils
 * 
 * @author Petr Vogl
 */
public final class EnumUtils {

	/**
	 * Provide enum constant of the stecified enum type and constant name.
	 * 
	 * @param <T> Enum type
	 * @param enumClass Enum type class
	 * @param enumConstantName Name of the enum constant
	 * @return Enum constant
	 */
	public static <T extends Enum<T>> Optional<T> decodeEnum(final Class<T> enumClass, final String enumConstantName) {
		if (enumConstantName == null) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(Enum.valueOf(enumClass, enumConstantName));
		} catch (final Exception ignored) {
			return Optional.empty();
		}
	}
}
