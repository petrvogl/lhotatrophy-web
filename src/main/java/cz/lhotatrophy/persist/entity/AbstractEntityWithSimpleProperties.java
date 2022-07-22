package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.MapToJsonStringConverter;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;

/**
 * Entity with the simple key-value properties serialized in a single column.
 *
 * @author Petr Vogl
 * @param <I> Type of entity identifier (the primary key)
 * @param <E> Type of entity
 */
@MappedSuperclass
@Getter
@Setter
@ToString
@NoArgsConstructor
public abstract class AbstractEntityWithSimpleProperties<I extends Serializable, E extends Entity<I, E>>
		extends AbstractEntity<I, E> {

	@Column(name = "properties", nullable = true, columnDefinition = "TEXT")
	@Convert(converter = MapToJsonStringConverter.class)
	private Map<String, Object> properties;

	@PrePersist
	@PreUpdate
	void prePersist() {
		if (MapUtils.isEmpty(properties)) {
			properties = null;
		}
	}

	/**
	 * Provides value of the specified property.
	 *
	 * @param <T> Type of the value provided
	 * @param name Name of the property
	 * @return Property value or empty {@link Optional} if not present
	 */
	public <T> Optional<T> getProperty(@NonNull final String name) {
		return properties == null
				? Optional.empty()
				: Optional.ofNullable((T) properties.get(name));
	}

	/**
	 * Sets a value of the specified property.
	 *
	 * @param name Name of the property
	 * @param value New value of the property
	 */
	public void addProperty(@NonNull final String name, @NonNull final Object value) {
		getPropertiesSync(true).put(name, value);
	}

	/**
	 * Removes the specified property if present.
	 *
	 * @param name Name of the property
	 */
	public void removeProperty(@NonNull final String name) {
		if (properties != null) {
			properties.remove(name);
		}
	}

	/**
	 * Provides the map of properties.
	 *
	 * @param createIfNotSet If set to {@code true}, the {@link Map} will be
	 * created in case not present
	 * @return Map of properties
	 */
	protected synchronized Map<String, Object> getPropertiesSync(final boolean createIfNotSet) {
		if (createIfNotSet && properties == null) {
			properties = new LinkedHashMap<>();
		}
		return properties;
	}
}
