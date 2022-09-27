package cz.lhotatrophy.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Petr Vogl
 */
public interface TemporaryStorage {

	static TemporaryStorage create() {
		return new DefaultTemporaryStorage();
	}

	<T> T get(@Nonnull Object key);

	<T> T getOrDefault(@Nonnull Object key, @Nullable T defaultValue);

	<T> T get(@Nonnull Object key, @Nonnull Supplier<T> valueLoader);

	<T> void put(@Nonnull Object key, @Nullable T data);

	void invalidate(Object key);

	final class DefaultTemporaryStorage implements TemporaryStorage {

		private transient Map<Object, Object> dataStore;

		private DefaultTemporaryStorage() {
		}

		@Nonnull
		private Map<Object, Object> getOrCreateStore() {
			if (dataStore != null) {
				return dataStore;
			}
			synchronized (this) {
				if (dataStore == null) {
					dataStore = new HashMap<>();
				}
			}
			return dataStore;
		}

		private <T> Object putInternal(final Object key, final T data) {
			final Map<Object, Object> cache = getOrCreateStore();
			// the previous value may be of a different type
			return cache.put(key, data);
		}

		private <T> T getOrDefaultInternal(final Object key, final T defaultValue) {
			if (dataStore == null) {
				return defaultValue;
			}
			final Object cachedValue = dataStore.get(key);
			return (cachedValue == null ? defaultValue : (T) cachedValue);
		}

		@Override
		public <T> T get(@Nonnull final Object key) {
			return (T) getOrDefaultInternal(key, null);
		}

		@Override
		public <T> T getOrDefault(@Nonnull final Object key, @Nullable final T defaultValue) {
			return (T) getOrDefaultInternal(key, defaultValue);
		}

		@Override
		public <T> T get(@Nonnull final Object key, @Nonnull final Supplier<T> valueLoader) {
			T cachedValue = getOrDefaultInternal(key, null);
			if (cachedValue == null) {
				cachedValue = Objects.requireNonNull(valueLoader.get(), "ValueLoader must not supply null");
				putInternal(key, cachedValue);
			}
			return cachedValue;
		}

		@Override
		public <T> void put(@Nonnull final Object key, @Nullable final T data) {
			if (data == null) {
				// null value is considered a removal request
				invalidate(key);
			} else {
				putInternal(key, data);
			}
		}

		@Override
		public void invalidate(final Object key) {
			if (key != null) {
				if (dataStore != null) {
					dataStore.remove(key);
				}
			}
		}
	}
}
