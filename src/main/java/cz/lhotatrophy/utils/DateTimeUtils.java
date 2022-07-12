package cz.lhotatrophy.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;

/**
 * Utility methods.
 *
 * @author Petr Vogl
 */
public final class DateTimeUtils {

	public static Instant createTodayInstatnt() {
		return Instant.now()
				.atZone(ZoneId.systemDefault())
				.truncatedTo(ChronoUnit.DAYS)
				.toInstant();
	}

	public static LocalDateTime createLocalDateTime(final long epochMilli) {
		return LocalDateTime.ofInstant(
				Instant.ofEpochMilli(epochMilli),
				ZoneId.systemDefault()
		);
	}

	public static long toEpochMilli(@NonNull final LocalDateTime localDateTime) {
		return ZonedDateTime
				.of(localDateTime, ZoneId.systemDefault())
				.toInstant()
				.toEpochMilli();
	}
}
