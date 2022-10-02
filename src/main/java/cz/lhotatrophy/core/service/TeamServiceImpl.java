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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.Frequency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@Override
	public Optional<Team> getTeamByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, Team.class);
	}

	/**
	 * Retuns default loader of IDs.
	 *
	 * @return IDs loader
	 */
	private Function<TeamListingQuerySpi, List<Long>> getDefaultIdsLoader() {
		return (listingQuery) -> {
			// The base listing query always targets all saved teams;
			// no filtration is needed
			return teamDao.findAllIds();
		};
	}

	@Override
	public List<Team> getTeamListing(@NonNull final TeamListingQuerySpi query) {
		return cacheService.getEntityListing(query, getDefaultIdsLoader());
	}

	@Override
	public Stream<Team> getTeamListingStream(@NonNull final TeamListingQuerySpi query) {
		return cacheService.getEntityListingStream(query, getDefaultIdsLoader());
	}

	@Override
	public void removeTeamFromCache(@NonNull final Long id) {
		cacheService.removeFromCache(id, Team.class);
		//
		//teamOrdersCache.invalidateAll();
		//teamOrdersCache.cleanUp();
	}

	@Override
	public Optional<Team> getEffectiveTeam() {
		return userService.getEffectiveUser()
				.map(User::getTeam);
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
		if (team.getId() == null || isManagedInPersistenceContext(team)) {
			// team instance is not persisted yet or it is managed in persistence context
			final Team t = teamDao.save(team);
			removeTeamFromCache(t.getId());
			return;
		}
		// team instance is plain data transfer object
		runInTransaction(() -> {
			final Long teamId = team.getId();
			final Team _team = teamDao.findById(teamId).get();
			_team.merge(team);
			teamDao.save(_team);
			removeTeamFromCache(teamId);
		});
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
}
