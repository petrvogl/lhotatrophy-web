package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.SchemaConstants;
import cz.lhotatrophy.utils.CzechComparator;
import java.util.Comparator;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * This entity class represents a clue to find a location or solve a puzzle.
 *
 * @author Petr Vogl
 */
@Entity
@Table(
		name = SchemaConstants.Clue.TABLE_NAME,
		indexes = {
			@Index(name = "code_idx", columnList = "code")
		}
)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Clue extends AbstractEntity<Long, Clue> implements EntityLongId<Clue> {

	/**
	 * Lexicographically compares clues by {@code code} property
	 */
	private static final Comparator<Clue> comparatorByCode = Comparator
			.comparing(Clue::getCode, Comparator.nullsLast(CzechComparator.instance()));

	/**
	 * Returns a comparator that compares {@link Clue} objects by {@code code}
	 * property in natural order.
	 *
	 * The returned comparator is serializable and throws {@link
	 * NullPointerException} when comparing {@code null}.
	 *
	 * @return a comparator that imposes the <i>natural ordering</i> on
	 * {@code Clue} objects.
	 */
	public static Comparator<Clue> orderByCode() {
		return comparatorByCode;
	}

	/**
	 * The persisted entity ID.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = SchemaConstants.PRIMARY_KEY, unique = true, nullable = false)
	private Long id;
	/**
	 * Indicates whether this clue is active.
	 */
	@Column(name = "active", unique = false, nullable = false)
	private Boolean active = true;
	/**
	 * Unique identification code of a clue.
	 */
	@Column(name = "code", unique = true, nullable = false, length = 32)
	private String code;
	/**
	 * Description of a clue.
	 */
	@Column(name = "description", unique = false, nullable = true, length = 512)
	private String description;

	/**
	 * Description of a clue.
	 */
	public void setDescription(final String description) {
		this.description = StringUtils.trimToNull(description);
	}

	/**
	 * Indicates whether some other object is "equal to" this one comparing all
	 * non-transient properties.
	 *
	 * @param obj the reference object with which to compare
	 * @return {@code true} if this object is the same as the {@code obj}
	 * argument; {@code false} otherwise.
	 */
	public boolean equalsAllProperties(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		// handles Hibernate proxies
		if (!getClass().isInstance(obj)) {
			return false;
		}
		final Clue other = (Clue) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		if (!Objects.equals(this.active, other.active)) {
			return false;
		}
		if (!Objects.equals(this.code, other.code)) {
			return false;
		}
		return Objects.equals(this.description, other.description);
	}
}
