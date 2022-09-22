package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.LocationDao;
import cz.lhotatrophy.persist.entity.Location;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class LocationServiceImpl extends AbstractService implements LocationService {

	@Autowired
	private transient LocationDao locationDao;
	@Autowired
	private transient TaskService taskService;
	@Autowired
	private transient EntityCacheService cacheService;

	@NonNull
	public Location createLocation(@NonNull final UnaryOperator<Location> initializer) {
		return runInTransaction(() -> {
			return locationDao.save(
					// custom init
					initializer.apply(new Location())
			);
		});
	}

	@Override
	public Optional<Location> getLocationById(@NonNull final Long id) {
		return locationDao.findById(id);
	}

	@Override
	public Optional<Location> getLocationByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, Location.class);
	}

	/**
	 * Retuns default loader of IDs.
	 *
	 * @return IDs loader
	 */
	private Function<LocationListingQuerySpi, List<Long>> getDefaultIdsLoader() {
		return (listingQuery) -> {
			// The base listing query always targets all saved locations;
			// no filtration is needed
			return locationDao.findAllIds();
		};
	}

	@Override
	public List<Location> getLocationListing(@NonNull final LocationListingQuerySpi query) {
		return cacheService.getEntityListing(query, getDefaultIdsLoader());
	}

	@Override
	public void removeLocationFromCache(@NonNull final Long id) {
		cacheService.removeFromCache(id, Location.class);
	}

	@Override
	public Location registerNewLocation(
			@NonNull final String code,
			@NonNull final String name,
			final String description
	) {
		// globally unique code check relies on up-to-date register
		if (cacheService.getEntityByCode(code).isPresent()) {
			throw new RuntimeException("Entita s tímto kódem už existuje.");
		}
		if (locationDao.findByName(name).isPresent()) {
			throw new RuntimeException("Stanoviště s tímto názvem už existuje.");
		}
		final Location newLocation = runInTransaction(() -> {
			final Location location = createLocation(l -> {
				l.setActive(true);
				l.setCode(code);
				l.setName(name);
				l.setDescription(description);
				return l;
			});
			// logging
			log.info("New Location has been registered: [ {} / {} ]", location.getCode(), location.getName());
			return location;
		});
		// keep GlobalCodeRegister up-to-date
		cacheService.resetGlobalCodeRegister();
		// discard all cached relationships
		taskService.invalidateTaskRelationshipsCache();
		return newLocation;
	}

	@Override
	public void updateLocation(@NonNull final Location location) {
		final Long locationId = location.getId();
		Objects.requireNonNull(locationId, "Stanoviště nelze aktualizovat (v databazi neexistuje).");
		Objects.requireNonNull(location.getCode());
		Objects.requireNonNull(location.getName());
		// save updates
		final MutableBoolean codeHasChanged = new MutableBoolean(false);
		final Location l = runInTransaction(() -> {
			final Location originalLocation = locationDao.findById(locationId).orElse(null);
			if (originalLocation == null) {
				throw new RuntimeException("Stanoviště nelze aktualizovat (v databazi neexistuje).");
			}
			if (originalLocation.equalsAllProperties(location)) {
				// no changes to the location
				return location;
			}
			final String newCode = location.getCode();
			if (!newCode.equals(originalLocation.getCode())) {
				// globally unique code check relies on up-to-date register
				if (cacheService.getEntityByCode(newCode).isPresent()) {
					throw new RuntimeException("Entita s tímto kódem už existuje.");
				}
				// code has changed
				codeHasChanged.setTrue();
				originalLocation.setCode(newCode);
			}
			final String newName = location.getName();
			if (!newName.equals(originalLocation.getName())) {
				// name has changed
				if (locationDao.findByName(newName).isPresent()) {
					throw new RuntimeException("Stanoviště s tímto názvem už existuje.");
				}
				originalLocation.setName(newName);
			}
			originalLocation.setActive(location.getActive());
			originalLocation.setDescription(location.getDescription());
			return locationDao.save(originalLocation);
		});
		if (l != location) {
			// changes have been made
			removeLocationFromCache(l.getId());
			// keep GlobalCodeRegister up-to-date
			if (codeHasChanged.isTrue()) {
				cacheService.resetGlobalCodeRegister();
			}
			// discard all cached relationships
			taskService.invalidateTaskRelationshipsCache();
		}
	}
}
