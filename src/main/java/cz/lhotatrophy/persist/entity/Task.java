package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.SchemaConstants;
import java.util.Arrays;
import java.util.Collections;
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
			@Index(name = "code_idx", columnList = "code")}
)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Task extends AbstractEntity<Long, Task> implements EntityLongId<Task> {

	/**
	 * Defines a separator regular expression that separates valid solutions of
	 * the task.
	 */
	private static final String SOLUTIONS_SEPARATOR_REGEXP = "\\s*;\\s*";

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
	 * All valid solutions of this task. Transient calculated value.
	 */
	@Transient
	@ToString.Exclude
	@Setter(AccessLevel.NONE)
	private Set<String> solutions;

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
	 * Gets all valid solutions to this task.
	 *
	 * @return All valid solutions
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Set<String> getSolutions() {
		if (solutions == null) {
			synchronized (this) {
				if (solutions == null) {
					solutions = StringUtils.isEmpty(solutionsString)
							? Collections.emptySet()
							: Arrays.stream(solutionsString.split(SOLUTIONS_SEPARATOR_REGEXP))
									.filter(StringUtils::isNotEmpty)
									.collect(Collectors.toUnmodifiableSet());
				}
			}
		}
		return solutions;
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
}
