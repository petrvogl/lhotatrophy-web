package cz.lhotatrophy.persist;

import cz.lhotatrophy.utils.JsonUtils;
import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.collections4.MapUtils;

/**
 * Converters {@code Map<String, Object>} data (entity attribute) into JSON
 * formatted string (database column) and back again.
 *
 * @author Igor Hrazdil
 */
@Converter
public class MapToJsonStringConverter implements AttributeConverter<Map<String, Object>, String> {

	/**
	 * Converts the value stored in the entity attribute into the data
	 * representation to be stored in the database.
	 *
	 * @param attribute the entity attribute value to be converted
	 * @return the converted data to be stored in the database column
	 */
	@Override
	public String convertToDatabaseColumn(final Map<String, Object> attribute) {
		return (MapUtils.isEmpty(attribute) ? null : JsonUtils.objectToString(attribute));
	}

	/**
	 * Converts the data stored in the database column into the value to be
	 * stored in the entity attribute. Note that it is the responsibility of the
	 * converter writer to specify the correct <code>dbData</code> type for the
	 * corresponding column for use by the JDBC driver: i.e., persistence
	 * providers are not expected to do such type conversion.
	 *
	 * @param dbData the data from the database column to be converted
	 * @return the converted value to be stored in the entity attribute
	 */
	@Override
	public Map<String, Object> convertToEntityAttribute(final String dbData) {
		return (dbData == null ? null : JsonUtils.stringToObject(dbData));
	}
}
