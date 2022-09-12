package cz.lhotatrophy.core.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.lhotatrophy.persist.dao.TaskDao;
import cz.lhotatrophy.persist.dao.TeamDao;
import cz.lhotatrophy.persist.dao.UserDao;
import cz.lhotatrophy.persist.entity.Entity;
import cz.lhotatrophy.persist.entity.EntityLongId;
import cz.lhotatrophy.persist.entity.Task;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nonnull;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.util.Pair;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
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
	public void invalidateCacheEntry(@NonNull final EntityLongId entity) {
		_invalidate(entity.getId(), entity.getClass());
	}

	@Override
	public <T extends EntityLongId> void invalidateCacheEntry(@NonNull final Long id, @NonNull final Class<T> cls) {
		_invalidate(id, cls);
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
					.map(e -> detachEntity(e))
					.map(e -> unproxyEntity(e))
					.map(e -> initializeEntity(e))
					.map(EntityLongId.class::cast)
					// this will save the "not found" information in the cache
					.orElse(NULL_ENTITY);
		}
	}

	/**
	 * Detach entity.
	 *
	 * @param <T> Entity type
	 * @param entity Entity
	 * @return Entity
	 */
	private <T extends Entity> T detachEntity(final T entity) {
		detach(entity);
		return entity;
	}

	/**
	 * Unproxy entity.
	 *
	 * @param <T> Entity type
	 * @param entity Entity
	 * @return Entity
	 */
	private <T extends Entity> T unproxyEntity(final T entity) {
		if (entity instanceof HibernateProxy) {
			final LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
			if (lazyInitializer != null) {
				final T _entity = (T) lazyInitializer.getImplementation();

				log.info("Unproxy entity: \"{}\" >> \"{}\"", entity.getClass().getSimpleName(), _entity.getClass().getSimpleName());
				return _entity;
			}
		}

		log.info("Unproxy entity: \"{}\" is not HibernateProxy", entity.getClass().getSimpleName());
		return entity;
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
					final TeamMember m = unproxyEntity(detachEntity(member));
					m.setTeam(team);
					detachedMembers.add(m);
				});
				team.setMembers(detachedMembers);
			}
		}
		return entity;
	}
}
