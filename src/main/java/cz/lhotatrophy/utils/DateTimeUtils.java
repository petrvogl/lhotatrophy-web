package cz.lhotatrophy.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

/**
 * Utility methods.
 *
 * @author Petr Vogl
 */
public final class DateTimeUtils {

	/**
	 * DateTime format pattern {@code yyyy-MM-dd HH:mm}.
	 */
	public static final String YYYY_MM_DD__HH_MM = "yyyy-MM-dd HH:mm";
	/**
	 * DateTime format pattern {@code d. M. yyyy HH:mm:ss.SSS}.
	 */
	public static final String D_M_YYYY__HH_MM_SS_SSS = "d. M. yyyy HH:mm:ss.SSS";
	/**
	 * Local cache of parsed LocalDateTime instances.
	 */
	private static final Map<String, LocalDateTime> cache = new HashMap<>(1_000);

	public static Instant createTodayInstatnt() {
		return Instant.now()
				.atZone(ZoneId.systemDefault())
				.truncatedTo(ChronoUnit.DAYS)
				.toInstant();
	}

	public static LocalDateTime toLocalDateTime(final long epochMilli) {
		return LocalDateTime.ofInstant(
				Instant.ofEpochMilli(epochMilli),
				ZoneId.systemDefault()
		);
	}

	public static LocalDateTime toLocalDateTime(@NonNull final Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	public static Instant toInstant(final long epochMilli) {
		return Instant.ofEpochMilli(epochMilli)
				.atZone(ZoneId.systemDefault())
				.toInstant();
	}

	public static Instant toInstant(@NonNull final LocalDateTime localDateTime) {
		return ZonedDateTime
				.of(localDateTime, ZoneId.systemDefault())
				.toInstant();
	}

	public static long toEpochMilli(@NonNull final LocalDateTime localDateTime) {
		return toInstant(localDateTime).toEpochMilli();
	}

	public static LocalDateTime parse(final DateTimeFormatter formater, final String dateTimeString, final boolean useCache) {
		if (useCache) {
			final LocalDateTime resultCached = cache.get(dateTimeString);
			if (resultCached != null) {
				return resultCached;
			}
		}
		try {
			final LocalDateTime result = formater.parse(dateTimeString, LocalDateTime::from);
			if (useCache) {
				cache.put(dateTimeString, result);
			}
			return result;
		} catch (final DateTimeParseException ignored) {
			// noop
		}
		return null;
	}

	public static LocalDateTime parse(final String dateTimeString, @NonNull final String formatPattern) {
		if (dateTimeString == null) {
			// null-safe
			return null;
		}
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
		return parse(formatter, dateTimeString, true);
	}

	public static String format(final LocalDateTime dateTime, @NonNull final String formatPattern) {
		if (dateTime == null) {
			// null-safe
			return null;
		}
		return DateTimeFormatter.ofPattern(formatPattern)
				.format(dateTime);
	}

	public static String format(final Instant instant, @NonNull final String formatPattern) {
		if (instant == null) {
			// null-safe
			return null;
		}
		return DateTimeFormatter.ofPattern(formatPattern)
				.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
	}
}
