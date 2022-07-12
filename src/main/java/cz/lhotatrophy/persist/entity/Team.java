package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.SchemaConstants;
import cz.lhotatrophy.utils.EnumUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.mutable.MutableInt;

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
public class Team extends AbstractEntity<Long> {

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

	@Transient
	@ToString.Exclude
	private Boolean hasBeenEdited;

	public void addMember(@NonNull final TeamMember member) {
		getMembersSync(true).add(member);
		member.setTeam(this);
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
					.sorted(Comparator.naturalOrder())
					.forEach(m -> {
						if (iterator.hasNext()) {
							final TeamMember other = iterator.next();
							m.setName(other.getName());
							m.setProperties(other.getProperties());
						}
					});
		}
	}

	public List<TeamMember> getMembersOrdered() {
		return getMembersSync(true).stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());
	}

	private synchronized Set<TeamMember> getMembersSync(final boolean createIfNotSet) {
		if (createIfNotSet && members == null) {
			members = new LinkedHashSet<>();
		}
		return members;
	}

	public boolean hasBeenEdited() {
		if (hasBeenEdited == null) {
			hasBeenEdited = members != null && !members.isEmpty();
		}
		return hasBeenEdited;
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
				});
		return totalPrice.getValue();
	}

	@ToString.Include(name = "ownerId")
	Long getOwnerId() {
		return owner == null ? null : owner.getId();
	}

	@ToString.Include(name = "membersIds")
	List<Long> getMembersIds() {
		if (members == null || members.isEmpty()) {
			return Collections.emptyList();
		}
		return members.stream()
				.map(TeamMember::getId)
				.collect(Collectors.toList());
	}
}
