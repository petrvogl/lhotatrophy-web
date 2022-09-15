package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.EntityLongId;
import java.io.Serializable;
import java.util.Comparator;

/**
 * API for defining a query to list entities.
 *
 * @param <T> Entity type
 * @param <Q> Listing query type
 * @author Petr Vogl
 */
public interface EntityListingQuerySpi<T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> extends Serializable {

	/**
	 * Returns the class of the listed entities.
	 */
	Class<T> getObjectClass();

	/**
	 * Returns {@code true} if the entities are taken from cache, otherwise they
	 * are taken from the database.
	 */
	boolean useEntityCache();

	/**
	 * Sets the cache as the primary entity source. If set to {@code true} then
	 * entities are taken from cache, otherwise they are loaded from the
	 * database. The default value is {@code true}.
	 *
	 * @param useEntityCache If {@code true} then entities are taken from cache
	 * @return Listing query
	 */
	Q setUseEntityCache(boolean useEntityCache);

	/**
	 * Indicates the use of a cache when evaluating the query. Returns
	 * {@code true} if the listing (sequence of entities) is taken from cache,
	 * otherwise it is loaded from the database.
	 */
	boolean useQueryCache();

	/**
	 * Sets the cache as the primary listing source. If set to {@code true} then
	 * listing (sequence of entities) is taken from cache, otherwise it is
	 * loaded from the database. The default value is {@code true}.
	 *
	 * @param useQueryCache If {@code true} then listing is taken from cache
	 * @return Listing query
	 */
	Q setUseQueryCache(boolean useQueryCache);

	/**
	 * Returns less specific general query (so called "base query") with more
	 * general criteria to get the "base listing". If this method returns
	 * {@code null}, it means this guery is already in base form. The goal of
	 * dividing the query into the base and the fully specific is to reduce the
	 * number of queries to the database.
	 *
	 * @return Base query or {@code null}
	 */
	Q createBaseListingQuery();

	/**
	 * Maximum listing size.
	 */
	Integer getMaxSize();

	/**
	 * Sets the maximum listing size.
	 *
	 * @param maxSize Maximum listing size
	 * @return Listing query
	 */
	Q setMaxSize(Integer maxSize);

	/**
	 * Returns the {@link Comparator} to determine the order of the listing.
	 */
	Comparator<T> getSorting();

	/**
	 * Sets the {@link Comparator} to determine the order of the listing.
	 *
	 * @implNote Comparator is used for post-sorting.
	 * @param comparator The comparator to determine the order of the listing
	 * @return Listing query
	 */
	Q setSorting(Comparator<T> comparator);

	/**
	 * Performs a detailed check to see if the entity meets the criteria in the
	 * query. If the given entity is {@code null} then it should return
	 * {@code false}.
	 *
	 * @param entity Entity being checked
	 *
	 * @return Returns {@code true} if the entity meets the query criteria
	 */
	boolean check(T entity);
}
