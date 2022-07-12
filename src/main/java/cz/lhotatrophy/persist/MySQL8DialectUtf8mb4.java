package cz.lhotatrophy.persist;

import lombok.extern.log4j.Log4j2;
import org.hibernate.dialect.MySQL8Dialect;

/**
 * Set the encoding and sorting rules of the table when JPA builds the table.
 *
 * @author Petr Vogl
 */
@Log4j2
public class MySQL8DialectUtf8mb4 extends MySQL8Dialect {

	/**
	 * Set the default charset to utf-8 for CREATE TABLE when using hibernate
	 * with java persistence annotations.
	 *
	 * @return Table type string
	 */
	@Override
	public String getTableTypeString() {
		return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci";
	}
}
