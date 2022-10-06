package cz.lhotatrophy.web.service;

import cz.lhotatrophy.core.service.LocationListingQuerySpi;
import cz.lhotatrophy.core.service.LocationService;
import cz.lhotatrophy.persist.entity.Location;
import java.util.Iterator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code ${service.location.method()}}
 *
 * @author Petr Vogl
 */
@Component
@Log4j2
public final class LocationViewService {

	@Autowired
	private transient LocationService locationService;

	/**
	 * {@code ${service.location.getAllActiveLocations()}}
	 */
	public Iterator<Location> getAllActiveLocations() {
		final LocationListingQuerySpi query = LocationListingQuerySpi.create()
				.setActive(Boolean.TRUE)
				.setSorting(Location.orderByCode());
		return locationService.getLocationListingStream(query).iterator();
	}
}
