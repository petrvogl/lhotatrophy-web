package cz.lhotatrophy.core.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.lhotatrophy.persist.dao.TeamDao;
import cz.lhotatrophy.persist.dao.TeamMemberDao;
import cz.lhotatrophy.persist.entity.FridayOfferEnum;
import cz.lhotatrophy.persist.entity.SaturdayOfferEnum;
import cz.lhotatrophy.persist.entity.Team;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.TshirtOfferEnum;
import cz.lhotatrophy.persist.entity.User;
import cz.lhotatrophy.utils.EnumUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.Frequency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class TeamServiceImpl extends AbstractService implements TeamService {

	@Autowired
	private transient TeamDao teamDao;
	@Autowired
	private transient TeamMemberDao teamMemberDao;
	@Autowired
	private transient UserService userService;
	@Autowired
	private transient EntityCacheService cacheService;

	/**
	 * Cache storage to temporarily store frequently used data lists to avoid
	 * redundant/unnecessary accessing a database.
	 */
	private static final Cache<TeamListingQuery, List<Team>> teamListingCache = CacheBuilder
			.newBuilder()
			.maximumSize(10)
			.expireAfterWrite(2, TimeUnit.MINUTES)
			.build();

	/**
	 * Cache storage to temporarily store histogram of teams orders.
	 */
	private static final Cache<String, Frequency> teamOrdersCache = CacheBuilder
			.newBuilder()
			.maximumSize(10)
			.expireAfterWrite(60, TimeUnit.MINUTES)
			.build();

	@NonNull
	public Team createTeam(@NonNull final UnaryOperator<Team> initializer) {
		return runInTransaction(() -> {
			return teamDao.save(
					// custom init
					initializer.apply(new Team())
			);
		});
	}

	@NonNull
	public TeamMember createTeamMember(@NonNull final UnaryOperator<TeamMember> initializer) {
		return runInTransaction(() -> {
			return teamMemberDao.save(
					// custom init
					initializer.apply(new TeamMember())
			);
		});
	}

	@Override
	public Optional<Team> getTeamById(@NonNull final Long id) {
		return teamDao.findById(id);
	}

	@Override
	public Optional<Team> getTeamByName(@NonNull final String name) {
		return teamDao.findByName(name);
	}

	@NonNull
	@Override
	public List<Team> getAllTeams() {
		final List<Team> all = teamDao.findAll(Sort.by(Sort.Direction.ASC, "id"));
		return all == null ? Collections.emptyList() : all;
	}

	@Override
	public Optional<Team> getTeamByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, Team.class);
	}

	@Override
	public List<Team> getTeamListing(@NonNull final TeamListingQuery query) {
		try {
			final List<Team> listing = teamListingCache.get(query, () -> {
				final List<Long> allIds = teamDao.findAllIds();
				return allIds.stream()
						.map(id -> getTeamByIdFromCache(id))
						.filter(Optional::isPresent)
						.map(Optional::get)
						// filter by query.active
						.filter(t -> query.getActive() == null || Objects.equals(query.getActive(), t.getActive()))
						.collect(Collectors.toList());
			});
			return listing;
		} catch (final Exception ex) {
			final String err = String.format("Can't load team listing from cache by query [%s].", query.toString());
			log.error(err, ex);
			return Collections.emptyList();
		}
	}

	@Override
	public Frequency getTeamOrdersFrequency(@NonNull final Class<? extends Enum> enumClass) {
		final String cacheKey = enumClass.getSimpleName();
		final String propertyKey;
		if (enumClass.isAssignableFrom(FridayOfferEnum.class)) {
			propertyKey = "friday";
		} else if (enumClass.isAssignableFrom(SaturdayOfferEnum.class)) {
			propertyKey = "saturday";
		} else if (enumClass.isAssignableFrom(TshirtOfferEnum.class)) {
			propertyKey = "tshirtCode";
		} else {
			throw new IllegalArgumentException();
		}

		try {
			return teamOrdersCache.get(cacheKey, () -> {
				// for all active teams
				final TeamListingQuery teamListingQuery = new TeamListingQuery();
				teamListingQuery.setActive(Boolean.TRUE);
				final List<Team> allTeams = getTeamListing(teamListingQuery);
				// construct histogram
				final Frequency frequency = new Frequency();
				allTeams.stream()
						.filter(Team::hasMembers)
						.flatMap(t -> t.getMembers().stream())
						.forEach(member -> {
							member.getProperty(propertyKey)
									.map(Object::toString)
									.flatMap(val -> EnumUtils.decodeEnum(enumClass, val))
									.ifPresent(e -> frequency.addValue((Enum) e));
						});
				return frequency;
			});
		} catch (final Exception ex) {
			final String err = String.format("Can't load team orders from cache by key [%s].", cacheKey);
			log.error(err, ex);
			return null;
		}
	}

	@Override
	public void removeTeamFromCache(@NonNull final Long id) {
		cacheService.invalidateCacheEntry(id, Team.class);
		//
		teamListingCache.invalidateAll();
		teamListingCache.cleanUp();
		teamOrdersCache.invalidateAll();
		teamOrdersCache.cleanUp();
	}

	@Override
	public Team registerNewTeam(@NonNull final String name, @NonNull final User owner) {
		if (getTeamByName(name).isPresent()) {
			throw new RuntimeException("Tým s tímto názvem už existuje.");
		}
		if (owner.getTeam() != null) {
			throw new RuntimeException("Tento uživatel už má svůj tým.");
		}
		return runInTransaction(() -> {
			final Team team = createTeam(t -> {
				t.setName(name);
				t.setOwner(owner);
				return t;
			});
			// just for consistency
			owner.setTeam(team);
			userService.removeUserFromCache(owner.getId());
			// logging
			log.info("New team has been registered: [ {} / {} ]", team.getId(), team.getName());
			log.info("\nLhotaTrophy:\n    CREATED {}", team.toString());
			return team;
		});
	}

	@Override
	public void updateTeam(@NonNull final Team team) {
		final Team t = teamDao.save(team);
		removeTeamFromCache(t.getId());
	}
}
