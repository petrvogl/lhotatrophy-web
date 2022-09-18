package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.ClueDao;
import cz.lhotatrophy.persist.entity.Clue;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class ClueServiceImpl extends AbstractService implements ClueService {

	@Autowired
	private transient ClueDao clueDao;
	@Autowired
	private transient EntityCacheService cacheService;

	@NonNull
	public Clue createClue(@NonNull final UnaryOperator<Clue> initializer) {
		return runInTransaction(() -> {
			return clueDao.save(
					// custom init
					initializer.apply(new Clue())
			);
		});
	}

	@Override
	public Optional<Clue> getClueById(@NonNull final Long id) {
		return clueDao.findById(id);
	}

	@Override
	public Optional<Clue> getClueByIdFromCache(@NonNull final Long id) {
		return cacheService.getEntityById(id, Clue.class);
	}

	/**
	 * Retuns default loader of IDs.
	 *
	 * @return IDs loader
	 */
	private Function<ClueListingQuerySpi, List<Long>> getDefaultIdsLoader() {
		return (listingQuery) -> {
			// The base listing query always targets all saved clues;
			// no filtration is needed
			return clueDao.findAllIds();
		};
	}

	@Override
	public List<Clue> getClueListing(@NonNull final ClueListingQuerySpi query) {
		return cacheService.getEntityListing(query, getDefaultIdsLoader());
	}

	@Override
	public void removeClueFromCache(@NonNull final Long id) {
		cacheService.removeFromCache(id, Clue.class);
	}

	@Override
	public Clue registerNewClue(
			@NonNull final String code,
			final String description
	) {
		if (clueDao.findByCode(code).isPresent()) {
			throw new RuntimeException("Indicie s tímto kódem už existuje.");
		}
		return runInTransaction(() -> {
			final Clue clue = createClue(c -> {
				c.setActive(true);
				c.setCode(code);
				c.setDescription(description);
				return c;
			});
			// logging
			log.info("New Clue has been registered: [ {} ]", clue.getCode());
			return clue;
		});
	}

	@Override
	public void updateClue(@NonNull final Clue clue) {
		final Long clueId = clue.getId();
		Objects.requireNonNull(clueId, "Indicie nelze aktualizovat (v databazi neexistuje).");
		Objects.requireNonNull(clue.getCode());
		// save updates
		final Clue c = runInTransaction(() -> {
			final Clue originalClue = clueDao.findById(clueId).orElse(null);
			if (originalClue == null) {
				throw new RuntimeException("Indicie nelze aktualizovat (v databazi neexistuje).");
			}
			if (originalClue.equalsAllProperties(clue)) {
				// no changes to the clue
				return clue;
			}
			final String newCode = clue.getCode();
			if (!newCode.equals(originalClue.getCode())) {
				// code has changed
				if (clueDao.findByCode(newCode).isPresent()) {
					throw new RuntimeException("Indicie s tímto kódem už existuje.");
				}
				originalClue.setCode(newCode);
			}
			originalClue.setActive(clue.getActive());
			originalClue.setDescription(clue.getDescription());
			return clueDao.save(originalClue);
		});
		if (c != clue) {
			// changes have been made
			removeClueFromCache(c.getId());
		}
	}
}
