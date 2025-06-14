package cz.lhotatrophy.persist.entity;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import cz.lhotatrophy.persist.ContestProgressToJsonStringConverter;
import cz.lhotatrophy.persist.SchemaConstants;
import cz.lhotatrophy.utils.EnumUtils;
import cz.lhotatrophy.utils.TemporaryStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.util.Pair;

/**
 * Team entity.
 *
 * @author Petr Vogl
 */
@Entity
@Table(
		name = SchemaConstants.Team.TABLE_NAME,
		indexes = {
			@Index(name = "name_idx", columnList = "name")}
)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Team extends AbstractEntity<Long, Team> implements EntityLongId<Team>, EntityWithCacheAccess, EntityWithTemporaryStorage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = SchemaConstants.PRIMARY_KEY, unique = true, nullable = false)
	private Long id;

	@Column(name = "active", unique = false, nullable = false)
	private Boolean active = true;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
	@JoinColumn(name = SchemaConstants.User.FK_OWNER, nullable = false, unique = true)
	@ToString.Exclude
	private User owner;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "team")
	@ToString.Exclude
	private Set<TeamMember> members;

	@Column(name = "contest_progress", nullable = true, columnDefinition = "TEXT")
	@Convert(converter = ContestProgressToJsonStringConverter.class)
	@ToString.Exclude
	private TeamContestProgress contestProgress;

	@Transient
	@ToString.Exclude
	@Setter(AccessLevel.NONE)
	private transient Supplier<Map<String, Frequency>> frequencyCacheSupplier;

	@Transient
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	private transient BiFunction<Long, Class<? extends EntityLongId>, ? extends EntityLongId> cachedEntityGetter;

	@Transient
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private transient TemporaryStorage temporaryStorage = TemporaryStorage.create();

	@PrePersist
	@PreUpdate
	void prePersist() {
		if (CollectionUtils.isEmpty(members)) {
			members = null;
		}
	}

	/**
	 * Indicates whether this team is active.
	 */
	public boolean isActive() {
		return Boolean.TRUE.equals(active);
	}

	public User getOwner() {
		if (cachedEntityGetter == null || owner == null) {
			return owner;
		}
		// if cache access is available, then is used
		return (User) cachedEntityGetter.apply(owner.getId(), User.class);
	}

	@Nonnull
	public TeamContestProgress getContestProgress() {
		if (contestProgress == null) {
			synchronized (this) {
				if (contestProgress == null) {
					contestProgress = new TeamContestProgress();
				}
			}
		}
		return contestProgress;
	}

	/**
	 * Informs whether the team has been disqualified.
	 */
	public boolean isDisqualified() {
		return contestProgress != null && contestProgress.isDisqualified();
	}

	/**
	 * Sets all attributes of {@code this} object by values of corresponding
	 * attributes of {@code other} object except the primary and foreign key
	 * attributes.
	 *
	 * @param other Other object
	 */
	public void merge(final Team other) {
		if (other == null) {
			// null-safe
			return;
		}
		active = other.active;
		name = other.name;
		contestProgress = other.contestProgress;
	}

	private synchronized Set<TeamMember> getMembersSync(final boolean createIfNotSet) {
		if (createIfNotSet && members == null) {
			members = new LinkedHashSet<>();
		}
		return members;
	}

	public Set<TeamMember> getMembers() {
		return getMembersSync(false);
	}

	public List<TeamMember> getMembersOrdered() {
		return getMembersSync(true).stream()
				.sorted()
				.collect(Collectors.toList());
	}

	public void addMember(@NonNull final TeamMember member) {
		member.setTeam(this);
		getMembersSync(true).add(member);
	}

	public void updateMembers(@NonNull final Set<TeamMember> members) {
		final Set<TeamMember> thisMembers = getMembersSync(true);
		if (thisMembers.isEmpty()) {
			// insert all
			members.forEach(m -> addMember(m));
		} else {
			// merge
			final Iterator<TeamMember> iterator = members.iterator();
			thisMembers.stream()
					.sorted()
					.forEach(m -> {
						if (iterator.hasNext()) {
							final TeamMember other = iterator.next();
							m.setName(other.getName());
							m.setProperties(other.getProperties());
						}
					});
		}
	}

	public boolean hasMembers() {
		return members != null && !members.isEmpty();
	}

	@ToString.Include(name = "expenses")
	public int getExpenses() {
		if (members == null || members.isEmpty()) {
			return 2000;
		}
		final MutableInt totalPrice = new MutableInt(2000);
		members.stream()
				.forEach(m -> {
					totalPrice.add(m.getProperty("friday")
							.flatMap(val -> EnumUtils.decodeEnum(FridayOfferEnum.class, (String) val))
							.map(FridayOfferEnum::getPrice)
							.orElse(0));
					totalPrice.add(m.getProperty("saturday")
							.flatMap(val -> EnumUtils.decodeEnum(SaturdayOfferEnum.class, (String) val))
							.map(SaturdayOfferEnum::getPrice)
							.orElse(0));
					totalPrice.add(m.getProperty("tshirtCode")
							.flatMap(val -> EnumUtils.decodeEnum(TshirtOfferEnum.class, (String) val))
							.map(TshirtOfferEnum::getPrice)
							.orElse(0));
					totalPrice.add(m.getProperty("accommodation")
							.flatMap(val -> EnumUtils.decodeEnum(AccommodationOfferEnum.class, (String) val))
							.map(AccommodationOfferEnum::getPrice)
							.orElse(0));
				});
		return totalPrice.getValue();
	}

	public long getCount(@NonNull final Enum propertyValue) {
		if (!hasMembers()) {
			return 0L;
		}
		final String cacheKey;
		if (FridayOfferEnum.class.isInstance(propertyValue)) {
			cacheKey = FridayOfferEnum.class.getSimpleName();
		} else if (SaturdayOfferEnum.class.isInstance(propertyValue)) {
			cacheKey = SaturdayOfferEnum.class.getSimpleName();
		} else if (TshirtOfferEnum.class.isInstance(propertyValue)) {
			cacheKey = TshirtOfferEnum.class.getSimpleName();
		} else if (AccommodationOfferEnum.class.isInstance(propertyValue)) {
			cacheKey = AccommodationOfferEnum.class.getSimpleName();
		} else {
			throw new IllegalArgumentException();
		}
		final Frequency frequency = getFrequencyCache().get(cacheKey);
		return frequency == null ? 0L : frequency.getCount(propertyValue);
	}

	@SuppressWarnings("DoubleCheckedLocking")
	private Map<String, Frequency> getFrequencyCache() {
		if (frequencyCacheSupplier == null) {
			synchronized (Team.class) {
				if (frequencyCacheSupplier == null) {
					final com.google.common.base.Supplier<Map<String, Frequency>> memoizeWithExpiration = Suppliers.memoizeWithExpiration(() -> {
						final Map<String, Frequency> cachedMap = new HashMap<>(4);

						final ArrayList<Pair<String, Class>> propertiesDef = Lists.newArrayList(
								new Pair("friday", FridayOfferEnum.class),
								new Pair("saturday", SaturdayOfferEnum.class),
								new Pair("tshirtCode", TshirtOfferEnum.class),
								new Pair("accommodation", AccommodationOfferEnum.class)
						);
						propertiesDef.forEach(def -> {
							final String propertyKey = def.getKey();
							final Class propertyValueClass = def.getValue();
							final Frequency frequency = new Frequency();
							getMembers().stream().forEach(member -> {
								member.getProperty(propertyKey)
										.map(Object::toString)
										.flatMap(val -> EnumUtils.decodeEnum(propertyValueClass, val))
										.ifPresent(e -> frequency.addValue((Enum) e));
							});
							cachedMap.put(propertyValueClass.getSimpleName(), frequency);
						});

						return cachedMap;
					}, 10, TimeUnit.MINUTES);
					//
					frequencyCacheSupplier = () -> memoizeWithExpiration.get();
				}
			}
		}
		return frequencyCacheSupplier.get();
	}

	@ToString.Include(name = "ownerId")
	Long getOwnerId() {
		return owner == null ? null : owner.getId();
	}

	public String getStartPhotoName() {
		return id + "_startPhoto.jpg";
	}

	public String getFinishPhotoName() {
		return id + "_finishPhoto.jpg";
	}

	@Override
	public <T> T getTemporary(@NonNull final Object key) {
		return (T) temporaryStorage.get(key);
	}

	@Override
	public <T> T getTemporaryOrDefault(@NonNull final Object key, @Nullable final T defaultValue) {
		return (T) temporaryStorage.getOrDefault(key, defaultValue);
	}

	@Override
	public <T> T getTemporary(@NonNull final Object key, @NonNull final Supplier<T> valueLoader) {
		return temporaryStorage.get(key, valueLoader);
	}

	@Override
	public <T> void setTemporary(@NonNull final Object key, @Nullable final T data) {
		temporaryStorage.put(key, data);
	}

	@Override
	public void invalidateTemporary(final Object key) {
		temporaryStorage.invalidate(key);
	}
}
