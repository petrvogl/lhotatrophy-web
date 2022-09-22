package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.SchemaConstants;
import cz.lhotatrophy.utils.CzechComparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * This entity class represents contest task.
 *
 * @author Petr Vogl
 */
@Entity
@Table(
		name = SchemaConstants.Task.TABLE_NAME,
		indexes = {
			@Index(name = "code_idx", columnList = "code"),
			@Index(name = "name_idx", columnList = "name")
		}
)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Task extends AbstractEntity<Long, Task> implements EntityLongId<Task> {

	/**
	 * Defines a separator regular expression that separates individual values
	 * of multi-value properties.
	 */
	private static final String MULTIPLE_VALUES_SEPARATOR_REGEXP = "\\s*;\\s*";

	/**
	 * Lexicographically compares tasks by {@code code} property
	 */
	private static final Comparator<Task> comparatorByCode = Comparator
			.comparing(Task::getCode, Comparator.nullsLast(CzechComparator.instance()));

	/**
	 * Returns a comparator that compares {@link Task} objects by {@code code}
	 * property in natural order.
	 *
	 * The returned comparator is serializable and throws {@link
	 * NullPointerException} when comparing {@code null}.
	 *
	 * @return a comparator that imposes the <i>natural ordering</i> on
	 * {@code Task} objects.
	 */
	public static Comparator<Task> orderByCode() {
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
	 * Indicates whether this task is active.
	 */
	@Column(name = "active", unique = false, nullable = false)
	private Boolean active = true;
	/**
	 * Specifies the task type.
	 */
	@Enumerated(value = EnumType.STRING)
	@Column(name = "type", unique = false, nullable = false, length = 32)
	private TaskTypeEnum type;
	/**
	 * Unique identification code of this task.
	 */
	@Column(name = "code", unique = true, nullable = false, length = 32)
	private String code;
	/**
	 * Unique name of this task.
	 */
	@Column(name = "name", unique = true, nullable = false)
	private String name;
	/**
	 * Solution hint.
	 */
	@Column(name = "solution_hint", unique = false, nullable = true, length = 2048)
	private String solutionHint;
	/**
	 * Solution procedure.
	 */
	@Column(name = "solution_procedure", unique = false, nullable = true, length = 2048)
	private String solutionProcedure;
	/**
	 * All valid solutions of this task.
	 */
	@Column(name = "solutions", unique = false, nullable = false, length = 1024)
	private String solutionsString;
	/**
	 * Indicates whether the solution of this task can be revealed.
	 */
	@Column(name = "reveal_solution", unique = false, nullable = false)
	private Boolean revealSolutionAllowed = false;
	/**
	 * Identification code of the main associated location. This property
	 * provides a weak relation to the relationship entity.
	 */
	@Column(name = "location_code", unique = false, nullable = true)
	private String locationCode;
	/**
	 * Identification codes of objects intended to be rewards for completing
	 * this task. This property provides a weak relationship to multiple entity
	 * types and instances.
	 */
	@Column(name = "reward_codes", unique = false, nullable = true)
	private String rewardCodesString;

	/**
	 * All valid solutions of this task. Transient calculated value.
	 */
	@Transient
	@ToString.Exclude
	@Setter(AccessLevel.NONE)
	private Set<String> solutions;

	/**
	 * Identification codes of objects intended to be rewards for completing
	 * this task. Transient calculated value.
	 */
	@Transient
	@ToString.Exclude
	@Setter(AccessLevel.NONE)
	private Set<String> rewardCodes;

	/**
	 * Sets the all valid solutions to this task. Solutions must be separated by
	 * semicolon character {@code ';'}.
	 *
	 * @param solutionsString Valid solutions to this task
	 */
	public void setSolutionsString(final String solutionsString) {
		synchronized (this) {
			this.solutionsString = StringUtils.trimToNull(solutionsString);
			// reset calculated value
			this.solutions = null;
		}
	}

	/**
	 * Returns all valid solutions to this task.
	 *
	 * @return All valid solutions
	 */
	@Nonnull
	public Set<String> getSolutions() {
		if (solutions != null) {
			return solutions;
		}
		synchronized (this) {
			if (solutions == null) {
				solutions = StringUtils.isEmpty(solutionsString)
						? Collections.emptySet()
						: Arrays.stream(solutionsString.split(MULTIPLE_VALUES_SEPARATOR_REGEXP))
								.filter(StringUtils::isNotEmpty)
								.collect(Collectors.toUnmodifiableSet());
			}
		}
		return solutions;
	}

	/**
	 * Solution hint.
	 */
	public void setSolutionHint(final String solutionHint) {
		this.solutionHint = StringUtils.trimToNull(solutionHint);
	}

	/**
	 * Solution procedure.
	 */
	public void setSolutionProcedure(final String solutionProcedure) {
		this.solutionProcedure = StringUtils.trimToNull(solutionProcedure);
	}

	/**
	 * Indicates whether the solution hint is available for this task.
	 */
	public boolean hasSolutionHint() {
		return StringUtils.isNotEmpty(solutionHint);
	}

	/**
	 * Indicates whether the solution procedure is available for this task.
	 */
	public boolean hasSolutionProcedure() {
		return StringUtils.isNotEmpty(solutionProcedure);
	}

	/**
	 * Sets the identification code of the main associated location.
	 *
	 * @param locationCode Main location code
	 */
	public void setLocationCode(final String locationCode) {
		this.locationCode = StringUtils.trimToNull(locationCode);
	}

	/**
	 * Sets the identification codes of objects intended to be rewards for
	 * completing this task. Codes must be separated by semicolon character
	 * {@code ';'}.
	 *
	 * @param rewardCodesString Identification codes of rewards
	 */
	public void setRewardCodesString(final String rewardCodesString) {
		synchronized (this) {
			this.rewardCodesString = StringUtils.trimToNull(rewardCodesString);
			// reset calculated value
			this.rewardCodes = null;
		}
	}

	/**
	 * Returns all identification codes of objects intended to be rewards for
	 * completing this task.
	 *
	 * @return identification codes of rewards
	 */
	@Nonnull
	public Set<String> getRewardCodes() {
		if (rewardCodes != null) {
			return rewardCodes;
		}
		synchronized (this) {
			if (rewardCodes == null) {
				rewardCodes = StringUtils.isEmpty(rewardCodesString)
						? Collections.emptySet()
						: Arrays.stream(rewardCodesString.split(MULTIPLE_VALUES_SEPARATOR_REGEXP))
								.filter(StringUtils::isNotEmpty)
								.collect(Collectors.toCollection(LinkedHashSet::new));
				rewardCodes = Collections.unmodifiableSet(rewardCodes);
			}
		}
		return rewardCodes;
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
		final Task other = (Task) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		if (!Objects.equals(this.active, other.active)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		if (!Objects.equals(this.code, other.code)) {
			return false;
		}
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (!Objects.equals(this.solutionHint, other.solutionHint)) {
			return false;
		}
		if (!Objects.equals(this.solutionProcedure, other.solutionProcedure)) {
			return false;
		}
		if (!Objects.equals(this.solutionsString, other.solutionsString)) {
			return false;
		}
		if (!Objects.equals(this.revealSolutionAllowed, other.revealSolutionAllowed)) {
			return false;
		}
		if (!Objects.equals(this.locationCode, other.locationCode)) {
			return false;
		}
		return Objects.equals(this.rewardCodesString, other.rewardCodesString);
	}
}
