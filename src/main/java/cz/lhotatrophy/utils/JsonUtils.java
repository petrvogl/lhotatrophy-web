package cz.lhotatrophy.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods.
 *
 * @author Igor Hrazdil
 */
@Log4j2
public final class JsonUtils {

	/**
	 * Serialize object to JSON.
	 */
	public static String objectToString(final Object o) {
		return objectToString(o, false);
	}

	/**
	 * Serialize object to JSON.
	 */
	public static String objectToString(final Object o, final boolean prettify) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return (prettify
					? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o)
					: mapper.writeValueAsString(o));
		} catch (final JsonProcessingException e) {
			log.error("Serialization of object to string error:", e);
			return null;
		}
	}
	
	/**
	 * Deserialize object from JSON.
	 */
	public static <T> T stringToObject(final String str) {
		return (T) stringToObject(str, Object.class);
	}

	/**
	 * Deserialize object from JSON.
	 */
	public static <T> T stringToObject(final String str, final Class<T> cls) {
		if (StringUtils.isNotEmpty(str)) {
			final ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.readValue(str, cls);
			} catch (final IOException e) {
				log.error("Deserialization of object from string error:", e);
			}
		}
		return null;
	}
}
