package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.entity.EntityLongId;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.extern.log4j.Log4j2;

/**
 * Abstract query for entities listing.
 *
 * @param <T> Entity type
 * @param <Q> Listing query type
 * @author Petr Vogl
 */
@Log4j2
public abstract class EntityListingQuery<T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>>
		implements EntityListingQuerySpi<T, Q> {

	/**
	 * Listed entity class and also the type of query.
	 */
	private Class<T> cls;
	/**
	 * If true then entities are taken from cache.
	 */
	private boolean useEntityCache = true;
	/**
	 * If true then listing is taken from cache.
	 */
	private boolean useQueryCache = true;
	/**
	 * Maximum listing size.
	 */
	private Integer maxSize;
	/**
	 * Comparator to determine the order of the listing.
	 */
	private Comparator<T> sorting;
	/**
	 * Transient calculated value.
	 */
	private transient int hashCode;

	/**
	 * Constructor.
	 *
	 * @param cls Entity class
	 */
	protected EntityListingQuery(final Class<T> cls) {
		this.cls = cls;
	}

	/**
	 * Copy constructor.
	 *
	 * @param other Listing query
	 */
	public EntityListingQuery(final Q other) {
		final EntityListingQuery o = (EntityListingQuery) other;
		this.cls = o.cls;
		this.useEntityCache = o.useEntityCache;
		this.useQueryCache = o.useQueryCache;
		this.maxSize = o.maxSize;
		this.sorting = o.sorting;
	}

	@Override
	public boolean check(@Nullable final T entity) {
		if (entity == null) {
			log.warn("Entity checked by listing query is NULL.");
			return false;
		}
		// If the type of query is EntityLongId, checks if the entity is assignment-compatible
		if (Objects.equals(cls, EntityLongId.class)) {
			return cls.isInstance(entity);
		}
		// Other types must be equal
		return Objects.equals(cls, entity.getClass());
	}

	@Override
	public Q createBaseListingQuery() {
		// NULL means that all listing queries are base queries by default
		// Overide this in subclasses
		return null;
	}

	@Override
	public Class<T> getObjectClass() {
		return cls;
	}

	protected Q setObjectClass(final Class<T> cls) {
		this.cls = cls;
		resetHashCode();
		return (Q) this;
	}

	@Override
	public boolean useEntityCache() {
		return useEntityCache;
	}

	@Override
	public Q setUseEntityCache(final boolean useEntityCache) {
		this.useEntityCache = useEntityCache;
		return (Q) this;
	}

	@Override
	public boolean useQueryCache() {
		return this.useQueryCache;
	}

	@Override
	public Q setUseQueryCache(final boolean useQueryCache) {
		this.useQueryCache = useQueryCache;
		return (Q) this;
	}

	@Override
	public Integer getMaxSize() {
		return this.maxSize;
	}

	@Override
	public Q setMaxSize(final Integer maxSize) {
		this.maxSize = maxSize;
		resetHashCode();
		return (Q) this;
	}

	@Override
	public Comparator<T> getSorting() {
		return sorting;
	}

	@Override
	public Q setSorting(final Comparator<T> sorting) {
		this.sorting = sorting;
		resetHashCode();
		return (Q) this;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EntityListingQuery<?, ?> other = (EntityListingQuery<?, ?>) obj;
		if (!Objects.equals(this.cls, other.cls)) {
			return false;
		}
		if (!Objects.equals(this.sorting, other.sorting)) {
			return false;
		}
		return Objects.equals(this.maxSize, other.maxSize);
	}

	@Override
	public int hashCode() {
		if (hashCode != 0) {
			return hashCode;
		}
		return hashCode = hashCodeInternal();
	}

	protected void resetHashCode() {
		hashCode = 0;
	}

	protected int hashCodeInternal() {
		int hash = 7;
		hash = 73 * hash + Objects.hashCode(this.cls);
		hash = 73 * hash + Objects.hashCode(this.sorting);
		hash = 73 * hash + Objects.hashCode(this.maxSize);
		return hash;
	}
}
