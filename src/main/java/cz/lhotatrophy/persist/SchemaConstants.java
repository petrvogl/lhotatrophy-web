package cz.lhotatrophy.persist;

/**
 * Set of useful constants for the schema definition.
 *
 * @author Petr Vogl
 */
public final class SchemaConstants {

	/**
	 * Standard name for the primary key column.
	 */
	public static final String PRIMARY_KEY = "id";
	/**
	 * Standard postfix for the name of the foreign key column.
	 */
	public static final String FOREIGN_KEY_POSTFIX = "_" + PRIMARY_KEY;

	/**
	 * Constants relative to user accounts.
	 */
	public static final class User {

		public static final String TABLE_NAME = "user";
		public static final String FK_NAME = TABLE_NAME + FOREIGN_KEY_POSTFIX;
		// just for the semantics
		public static final String FK_OWNER = "owner" + FOREIGN_KEY_POSTFIX;
	}

	/**
	 * Constants relative to teams of contestants.
	 */
	public static final class Team {

		public static final String TABLE_NAME = "team";
		public static final String FK_NAME = TABLE_NAME + FOREIGN_KEY_POSTFIX;

		public static final class TeamMember {

			public static final String TABLE_NAME = "team_member";
		}
	}

	/**
	 * Constants relative to contest tasks.
	 */
	public static final class Task {

		public static final String TABLE_NAME = "task";
		public static final String FK_NAME = TABLE_NAME + FOREIGN_KEY_POSTFIX;
	}

	/**
	 * Constants relative to contest locations.
	 */
	public static final class Location {

		public static final String TABLE_NAME = "location";
		public static final String FK_NAME = TABLE_NAME + FOREIGN_KEY_POSTFIX;
	}
}
