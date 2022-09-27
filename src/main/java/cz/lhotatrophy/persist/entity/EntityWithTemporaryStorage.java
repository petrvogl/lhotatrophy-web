package cz.lhotatrophy.persist.entity;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.NonNull;

/**
 * This interface defines an entity with the ability to temporarily store data.
 * The entity object itself acts as a temporary data store (cache). The
 * expiration of a cache entry is linked to the expiration of this entity in the
 * "global" cache.
 *
 * @author Petr Vogl
 */
public interface EntityWithTemporaryStorage {

	<T> T getTemporary(@NonNull Object key);

	<T> T getTemporaryOrDefault(@NonNull Object key, @Nullable T defaultValue);

	<T> T getTemporary(@NonNull Object key, @NonNull Supplier<T> valueLoader);

	<T> void setTemporary(@NonNull Object key, @Nullable T data);

	void invalidateTemporary(Object key);
}
