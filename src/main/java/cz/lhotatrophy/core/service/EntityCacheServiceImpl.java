package cz.lhotatrophy.core.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.lhotatrophy.persist.SchemaConstants;
import cz.lhotatrophy.persist.dao.ClueDao;
import cz.lhotatrophy.persist.dao.LocationDao;
import cz.lhotatrophy.persist.dao.TaskDao;
import cz.lhotatrophy.persist.dao.TeamDao;
import cz.lhotatrophy.persist.dao.UserDao;
import cz.lhotatrophy.persist.entity.Clue;
import cz.lhotatrophy.persist.entity.EntityLongId;
import cz.lhotatrophy.persist.entity.Location;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provides support for individual entity caching as well as entity
 * list caching.
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class EntityCacheServiceImpl extends AbstractService implements EntityCacheService {

	@Autowired
	private transient UserDao userDao;
	@Autowired
	private transient TeamDao teamDao;
	@Autowired
	private transient TaskDao taskDao;
	@Autowired
	private transient ClueDao clueDao;
	@Autowired
	private transient LocationDao locationDao;

	/**
	 * Maximum listing size.
	 */
	private static final int MAXIMUM_LISTING_SIZE = Integer.MAX_VALUE;
	/**
	 * Capacity of a register of global code mappings to an entity ids.
	 */
	private static final int GLOBAL_CODE_REGISTER_CAPACITY = 100;

	/**
	 * Cache storage to temporarily store frequently used entities to avoid
	 * redundant/unnecessary accessing a database.
	 */
	private static final Cache<Pair<Long, String>, EntityLongId> entityCache = CacheBuilder
			.newBuilder()
			.maximumSize(10_000)
			//.expireAfterWrite(60, TimeUnit.MINUTES)
			.expireAfterWrite(3, TimeUnit.MINUTES)
			.build();

	/**
	 * Cache storage to temporarily store frequently used listings to minimize
	 * accessing a database.
	 */
	private static final Cache<EntityListingQuerySpi, List<Long>> idsListingCache = CacheBuilder
			.newBuilder()
			.maximumSize(100)
			//.expireAfterWrite(2, TimeUnit.MINUTES)
			.expireAfterWrite(1, TimeUnit.MINUTES)
			.build();

	/**
	 * Reference to the register of entities identified by a globally unique
	 * code.
	 */
	private static final AtomicReference<Map<String, Pair<Long, Class>>> entityCodeToIdMapRef = new AtomicReference();

	/**
	 * Entity instance (constant) to indicate "no entry found" in the cache or
	 * persistence context.
	 */
	private static final EntityLongId NULL_ENTITY = new EntityLongId() {
		@Override
		public Long getId() {
			return null;
		}

		@Override
		public void setId(final Serializable id) {
			throw new UnsupportedOperationException("Not supported.");
		}

		@Override
		public int compareTo(final Object arg0) {
			throw new UnsupportedOperationException("Not supported.");
		}
	};

	/**
	 * Contains functions for loading entities by ID from a persistent context.
	 */
	private static final Map<Class<? extends EntityLongId>, Function<Long, Optional<? extends EntityLongId>>> loadFunctions = new HashMap<>();

	/**
	 * Returns a function for loading entities of the specified class.
	 *
	 * @param cls Entity class
	 * @return Function for loading entities
	 */
	private Function<Long, Optional<? extends EntityLongId>> getLoadFunction(@NonNull final Class<? extends EntityLongId> cls) {
		if (loadFunctions.isEmpty()) {
			// register all supported loaders
			loadFunctions.put(User.class, id -> userDao.findById(id));
			loadFunctions.put(Team.class, id -> teamDao.findById(id));
			loadFunctions.put(Task.class, id -> taskDao.findById(id));
			loadFunctions.put(Clue.class, id -> clueDao.findById(id));
			loadFunctions.put(Location.class, id -> locationDao.findById(id));
		}
		return loadFunctions.get(cls);
	}

	/**
	 * Constructs and returns a key to the entity cache.
	 *
	 * @param id Entity ID
	 * @param cls Entity class
	 * @return Cache key
	 */
	@Nonnull
	private Pair<Long, String> createCacheKey(final Long id, final Class cls) {
		return new Pair(id, cls.getSimpleName());
	}

	/**
	 * Discards the cached entity if it is present in the cache.
	 *
	 * @param id Entity ID
	 * @param cls Entity class
	 */
	private void _invalidate(final Long id, final Class cls) {
		final Pair<Long, String> cacheKey = createCacheKey(id, cls);
		entityCache.invalidate(cacheKey);
	}

	@Override
	public void removeFromCache(@NonNull final EntityLongId entity) {
		_invalidate(entity.getId(), entity.getClass());
	}

	@Override
	public <T extends EntityLongId> void removeFromCache(@NonNull final Long id, @NonNull final Class<T> cls) {
		_invalidate(id, cls);
	}

	@Override
	public void removeFromCache(@NonNull final EntityListingQuerySpi listingQuery) {
		idsListingCache.invalidate(listingQuery);
		final EntityListingQuerySpi baseListingQuery = listingQuery.createBaseListingQuery();
		if (baseListingQuery != null) {
			idsListingCache.invalidate(baseListingQuery);
		}
	}

	@Override
	public void cleanEntityCache() {
		entityCache.invalidateAll();
		entityCache.cleanUp();
	}

	@Override
	public void cleanEntityListingCache() {
		idsListingCache.invalidateAll();
		idsListingCache.cleanUp();
	}

	@Override
	public void resetGlobalCodeRegister() {
		// volatile access synchronization
		entityCodeToIdMapRef.set(null);
	}

	private Pair<Long, Class> getEntityIdByCode(@NonNull final String code) {
		{
			// volatile access synchronization
			final Map<String, Pair<Long, Class>> register = entityCodeToIdMapRef.get();
			if (register != null) {
				return register.get(code);
			}
		}
		// Register construction is synchronized on register reference.
		// It prevents threads blocking on #resetGlobalCodeRegister().
		synchronized (entityCodeToIdMapRef) {
			// double check
			final Map<String, Pair<Long, Class>> register = entityCodeToIdMapRef.get();
			if (register != null) {
				return register.get(code);
			}
			// load the current state from the database
			final Map<String, Pair<Long, Class>> newRegister = new HashMap<>(GLOBAL_CODE_REGISTER_CAPACITY);
			runInTransaction(() -> {
				final String errMessage = "Globally unique entity code constraint violation: Duplicate code '{}'";
				getEntitiesFromDatabase(Task.class)
						.forEach(t -> {
							if (newRegister.put(t.getCode(), Pair.create(t.getId(), Task.class)) != null) {
								log.warn(errMessage, t.getCode());
							}
						});
				getEntitiesFromDatabase(Location.class)
						.forEach(l -> {
							if (newRegister.put(l.getCode(), Pair.create(l.getId(), Location.class)) != null) {
								log.warn(errMessage, l.getCode());
							}
						});
				getEntitiesFromDatabase(Clue.class)
						.forEach(c -> {
							if (newRegister.put(c.getCode(), Pair.create(c.getId(), Clue.class)) != null) {
								log.warn(errMessage, c.getCode());
							}
						});
			});
			// volatile access synchronization
			entityCodeToIdMapRef.set(newRegister);
			return newRegister.get(code);
		}
	}

	@Nonnull
	@Override
	public <T extends EntityLongId> Optional<T> getEntityByCode(@NonNull final String code) {
		return Optional.ofNullable(getEntityIdByCode(code))
				.flatMap(entry -> getEntityById(entry.getFirst(), (Class<T>) entry.getSecond()));
	}

	@Nonnull
	@Override
	public <T extends EntityLongId> Optional<T> getEntityById(@NonNull final Long id, @NonNull final Class<T> cls) {
		try {
			final EntityLoader loader = new EntityLoader(id, cls);
			final EntityLongId result = entityCache.get(createCacheKey(id, cls), loader);
			if (result == NULL_ENTITY) {
				// entity by ID not found
				return Optional.empty();
			}
			// check class
			if (!cls.isInstance(result)) {
				log.warn("The cached entity instance class conflicts with the class contained in the key: Entity \"{}\" vs. key \"{}\"",
						result.getClass().getSimpleName(), cls.getSimpleName());
				return Optional.empty();
			}
			final Optional<T> optEntity = Optional.of((T) result);
			return optEntity;
		} catch (final Exception ex) {
			final String err = String.format("Can't load entity from cache by ID [%d].", id);
			log.error(err, ex);
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public <T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> List<Long> getEntityIdsListing(
			@NonNull final Q listingQuery,
			@NonNull final Function<Q, List<Long>> idsLoader
	) {
		return getEntityIdsListingByFullQuery(listingQuery, idsLoader);
	}

	/**
	 * Rerurns a list of entity IDs from the cache according to the listing
	 * query, obtaining that list from IdsLoader if necessary. This method
	 * provides a simple substitute for the conventional "if cached, return;
	 * otherwise create, cache and return" pattern.
	 *
	 * If the given listing query {@code listingQuery} differs from the base
	 * query, then the base query is processed and cached first. Then a given
	 * fully specific query is applied on the resulting data of the base query.
	 * Processing the fully specific query does not require access to the
	 * database, it just calculates the result in memory.
	 *
	 * @param <T> Entity type
	 * @param <Q> Query type
	 * @param listingQuery Listing query
	 * @param idsLoader Loader of IDs
	 * @return List of entity IDs
	 */
	@Nonnull
	private <T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> List<Long> getEntityIdsListingByFullQuery(
			final Q listingQuery,
			final Function<Q, List<Long>> idsLoader
	) {
		if (listingQuery.useQueryCache()) {
			final List<Long> resultCached = idsListingCache.getIfPresent(listingQuery);
			if (resultCached != null) {
				// listing is present in the cache
				return resultCached;
			}
		}
		// get the base query
		final Q baseListingQuery = listingQuery.createBaseListingQuery();
		if (baseListingQuery == null) {
			// no further filtration is needed if the given listing query is already in base form
			return getEntityIdsListingByBaseQuery(listingQuery, idsLoader);
		}
		if (!listingQuery.useEntityCache()) {
			// if "no-cache" option is enabled, than listing query must be in the base form
			throw new IllegalArgumentException("Entity listing with \"no-cache\" option can not be processed becauce the listing query is not in the base form.");
		}
		// If the base query differs from the given listing query, then the base query is processed and cached first.
		// Then a fully specific query is applied on the resulting data of the base query.
		// Processing the fully specific query does not require access to the database, it just calculates the result in memory.
		final List<Long> resultOfBaseQuery = getEntityIdsListingByBaseQuery(baseListingQuery, idsLoader);
		if (resultOfBaseQuery.isEmpty()) {
			// no entities match the query criteria
			return resultOfBaseQuery;
		}
		// process the fully specific query
		try {
			// this callable applies a fully specific query on the resulting data of the base query
			final Callable<? extends List<Long>> valueLoader = () -> {
				final int maxSize = Optional.ofNullable(listingQuery.getMaxSize())
						// request for empty listing is valid
						.filter(max -> max >= 0)
						.orElse(MAXIMUM_LISTING_SIZE);
				// filter the result of the base query
				final Class<T> cls = listingQuery.getObjectClass();
				final List<Long> result = new ArrayList<>(resultOfBaseQuery.size() < maxSize ? resultOfBaseQuery.size() : maxSize);
				for (final Object obj : resultOfBaseQuery) {
					// check listing size limit
					if (result.size() >= maxSize) {
						break;
					}
					// check all the criteria of a fully specific query
					final Long id = (Long) obj;
					getEntityById(id, cls)
							.filter(listingQuery::check)
							.ifPresent(e -> result.add(id));
				}
				// shrink the allocated capacity to the real size of result
				final List<Long> resultShrunken = Arrays.asList(result.toArray(Long[]::new));
				return result.isEmpty()
						? Collections.emptyList()
						: Collections.unmodifiableList(resultShrunken);
			};
			// either cached
			if (listingQuery.useQueryCache()) {
				final List<Long> result = idsListingCache.get(listingQuery, valueLoader);
				return result != null ? result : Collections.emptyList();
			}
			// or not
			try {
				return valueLoader.call();
			} catch (final Exception e) {
				log.error("An error occurred while loading entity listing:", e);
			}
		} catch (final ExecutionException e) {
			final String errMsg = String.format("Problem with loading the listing from/into the cache according to the query [%s]:\n{}", listingQuery);
			log.error(errMsg, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Rerurns a list of entity IDs from the cache according to the listing
	 * query, obtaining that list from IdsLoader if necessary. This method
	 * provides a simple substitute for the conventional "if cached, return;
	 * otherwise create, cache and return" pattern.
	 *
	 * This method does not perform entity checking
	 * {@link EntityListingQuerySpi#check(EntityLongId)} because the listing
	 * query is supposed to be in the base form. Data filtering according to the
	 * listing query is supposed to be handled by the IdsLoader.
	 *
	 * @param <T> Entity type
	 * @param <Q> Query type
	 * @param baseListingQuery Listing query in the base form
	 * @param idsLoader Loader of IDs
	 * @return List of entity IDs
	 */
	@Nonnull
	private <T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> List<Long> getEntityIdsListingByBaseQuery(
			@Nonnull final Q baseListingQuery,
			@Nonnull final Function<Q, List<Long>> idsLoader) {
		try {
			final Callable<? extends List<Long>> valueLoader = () -> {
				final List<Long> idsLoaded = idsLoader.apply(baseListingQuery);
				if (idsLoaded == null || idsLoaded.isEmpty()) {
					// no entities match the query criteria
					return Collections.emptyList();
				}
				if (baseListingQuery.getSorting() == null) {
					// get the immutable copy of the list and keep the order given by the loader
					final List<Long> result = new ArrayList<>(idsLoaded);
					return Collections.unmodifiableList(result);
				}
				// result post-sorting
				final List<Long> resultSorted = new ArrayList<>(idsLoaded.size());
				final Class<T> cls = baseListingQuery.getObjectClass();
				idsLoaded.stream()
						.map(id -> (T) getEntityById(id, cls).orElse(null))
						.filter(Objects::nonNull)
						.sorted(baseListingQuery.getSorting())
						.map(EntityLongId::getId)
						.forEachOrdered(resultSorted::add);
				return resultSorted.isEmpty()
						? Collections.emptyList()
						: Collections.unmodifiableList(resultSorted);
			};
			// either cached
			if (baseListingQuery.useQueryCache()) {
				return idsListingCache.get(baseListingQuery, valueLoader);
			}
			// or not
			try {
				return valueLoader.call();
			} catch (final Exception e) {
				log.error("An error occurred while loading entity listing:", e);
			}
		} catch (final ExecutionException e) {
			final String errMsg = String.format("Problem with loading the listing from/into the cache according to the base query [%s]:\n{}", baseListingQuery);
			log.error(errMsg, e);
		}
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public <T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> Stream<T> getEntityListingStream(
			@NonNull final Q listingQuery,
			@NonNull final Function<Q, List<Long>> idsLoader
	) {
		// get IDs from the cache
		final List<Long> ids = getEntityIdsListingByFullQuery(listingQuery, idsLoader);
		if (ids.isEmpty()) {
			// no entities match the query criteria
			return Stream.<T>empty();
		}
		// map enity IDs to entity instances using cache
		final Class<T> cls = listingQuery.getObjectClass();
		if (listingQuery.useEntityCache()) {
			return ids.stream()
					.map(id -> getEntityById(id, cls).orElse(null))
					.filter(Objects::nonNull);
		}
		// or load entity instances directly from database
		return getEntitiesFromDatabase(cls, ids).stream();
	}

	@Nonnull
	@Override
	public <T extends EntityLongId, Q extends EntityListingQuerySpi<T, Q>> List<T> getEntityListing(
			@Nonnull final Q listingQuery,
			@Nonnull final Function<Q, List<Long>> idsLoader
	) {
		// get entities from the cache
		if (listingQuery.useEntityCache()) {
			return getEntityListingStream(listingQuery, idsLoader)
					.collect(Collectors.toList());
		}
		// or load entities directly from database
		final List<Long> ids = getEntityIdsListingByFullQuery(listingQuery, idsLoader);
		return ids.isEmpty()
				? Collections.emptyList()
				: getEntitiesFromDatabase(listingQuery.getObjectClass(), ids);
	}

	/**
	 * Retrieves all the rows of the table associated with the entity class
	 * {@code cls} from the database.
	 *
	 * <pre>SELECT * FROM cls</pre>
	 *
	 * @param <T> Entity type
	 * @param cls Entity class
	 * @return List of entities
	 */
	@Nonnull
	private <T extends EntityLongId> List<T> getEntitiesFromDatabase(final Class<T> cls) {
		return runInTransaction(() -> {
			final CriteriaBuilder builder = getCriteriaBuilder();
			final CriteriaQuery<T> criteria = builder.createQuery(cls);
			final Root<T> root = criteria.from(cls);
			// SELECT * FROM cls
			criteria.select(root);
			// get results
			final TypedQuery<T> createQuery = createQuery(criteria);
			final List<T> result = createQuery.getResultList();
			return (result == null || result.isEmpty())
					? Collections.emptyList()
					: result;
		});
	}

	/**
	 * Retrieves the rows of the table associated with the entity class
	 * {@code cls} and with specified ids from the database.
	 *
	 * <pre>SELECT * FROM cls WHRE id IN (ids)</pre>
	 *
	 * @param <T> Entity type
	 * @param cls Entity class
	 * @param ids List of entity ids
	 * @return List of entities
	 */
	@Nonnull
	private <T extends EntityLongId> List<T> getEntitiesFromDatabase(final Class<T> cls, final List<Long> ids) {
		return runInTransaction(() -> {
			final CriteriaBuilder builder = getCriteriaBuilder();
			final CriteriaQuery<T> criteria = builder.createQuery(cls);
			final Root<T> root = criteria.from(cls);
			// SELECT * FROM cls WHRE id IN (ids)
			criteria.select(root).where(root.get(SchemaConstants.PRIMARY_KEY).in(ids));
			// get results
			final TypedQuery<T> createQuery = createQuery(criteria);
			final List<T> result = createQuery.getResultList();
			return (result == null || result.isEmpty())
					? Collections.emptyList()
					: result;
		});
	}

	/**
	 * Entity loader for the cache.
	 */
	private class EntityLoader implements Callable<EntityLongId> {

		final Long id;
		final Class<? extends EntityLongId> cls;

		public EntityLoader(@NonNull final Long id, @NonNull final Class<? extends EntityLongId> cls) {
			this.id = id;
			this.cls = cls;
		}

		@Override
		public EntityLongId call() throws Exception {
			return getLoadFunction(cls)
					.apply(id)
					.map(e -> detach(e))
					.map(e -> unproxy(e))
					.map(e -> initializeEntity(e))
					.map(EntityLongId.class::cast)
					// this will save the "not found" information in the cache
					.orElse(NULL_ENTITY);
		}
	}

	/**
	 * Initialize entity.
	 *
	 * @param <T> Entity type
	 * @param entity Entity
	 * @return Entity
	 */
	private <T extends EntityLongId> T initializeEntity(final T entity) {
		if (entity instanceof Team) {
			final Team team = (Team) entity;
			// FIXME - remove this code
			// init team owner eagerly
			final Long userId = team.getOwner().getId();
			getEntityById(userId, User.class).ifPresent(user -> {
				team.setOwner(user);
				user.setTeam(team);
			});
			// detach, unproxy and initialize set of team members
			final Set<TeamMember> members = team.getMembers();
			if (members != null && !members.isEmpty()) {
				final Set<TeamMember> detachedMembers = new LinkedHashSet<>();
				members.forEach(member -> {
					final TeamMember m = unproxy(detach(member));
					m.setTeam(team);
					detachedMembers.add(m);
				});
				team.setMembers(detachedMembers);
			}
		}
		return entity;
	}
}
