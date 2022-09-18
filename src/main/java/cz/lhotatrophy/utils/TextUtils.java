package cz.lhotatrophy.utils;

import java.text.Normalizer;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

/**
 * Text utils
 *
 * @author Petr Vogl
 */
public final class TextUtils {

	/**
	 * The default delimiter used between words in a slug.
	 */
	public static final String DEFAULT_SLUG_DELIMITER = "-";

	/**
	 * Replaces non-ASCII characters with latin characters. Returns {@code null}
	 * only if {@code text} is {@code null}.
	 *
	 * @param text Text input
	 * @return Transformed text or {@code null}
	 */
	public static String toAscii(final String text) {
		return (text == null ? null : Normalizer
				.normalize(text.replace("ß", "ss"), Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", ""));
	}

	/**
	 * Transforms all characters to lowercase, replaces non-ASCII characters
	 * with latin characters, removes punctuation and spaces are replaced by
	 * single hypen {@code '-'}.
	 *
	 * @param text Text input
	 * @return Transformed text
	 */
	@Nonnull
	public static String slugify(final String text) {
		return slugifyInternal(text, true, null);
	}

	/**
	 * Replaces non-ASCII characters with latin characters, removes punctuation
	 * and spaces are replaced by single hypen {@code '-'}.
	 *
	 * @param text Text input
	 * @param toLowercase if {@code true} then transforms all characters to
	 * lowercase; otherwise, it preserves the upper and lower case characters in
	 * the text
	 * @return Transformed text
	 */
	@Nonnull
	public static String slugify(final String text, final boolean toLowercase) {
		return slugifyInternal(text, toLowercase, null);
	}

	@Nonnull
	private static String slugifyInternal(final String text, final boolean toLower, final String delim) {
		if (StringUtils.isBlank(text)) {
			return StringUtils.EMPTY;
		}
		final String slug = toAscii(text)
				.replaceAll("[^a-zA-Z0-9 ]", " ")
				.trim()
				.replaceAll("\\s+", (delim != null ? delim : DEFAULT_SLUG_DELIMITER));
		return (toLower ? slug.toLowerCase() : slug);
	}
}
