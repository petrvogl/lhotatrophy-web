package cz.lhotatrophy.persist.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * Type of the task.
 *
 * @author Petr Vogl
 */
@Getter
public enum TaskTypeEnum {

	A_CODE("A-kód", 'A'),
	B_CODE("B-kód", 'B'),
	C_CODE("C-kód", 'C');

	private static final Map<Character, TaskTypeEnum> constantByMark = new HashMap<>(3);
	/**
	 * Title of the task type.
	 */
	private final String title;
	/**
	 * Unique mark of the task type.
	 */
	private final char mark;

	static {
		// register construction
		Stream.of(values()).forEach(type -> constantByMark.put(type.mark, type));
	}

	/**
	 * Constructor.
	 *
	 * @param title Title of task type
	 * @param mark Unique mark of task type
	 */
	private TaskTypeEnum(final String title, final char mark) {
		this.title = title;
		this.mark = mark;
	}

	/**
	 * Returns the enum constant of the specified task type mark (case
	 * insensitive). Returns {@code null} if the specified enum type has no
	 * constant with the specified mark.
	 *
	 * @param mark The mark of the constant to return
	 * @return The enum constant of the specified task type mark
	 */
	public static TaskTypeEnum valueOf(final char mark) {
		return constantByMark.get(Character.toUpperCase(mark));
	}
}
