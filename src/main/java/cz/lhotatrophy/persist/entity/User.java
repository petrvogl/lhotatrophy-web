package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.SchemaConstants;
import cz.lhotatrophy.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.function.BiFunction;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User account entity.
 *
 * @author Petr Vogl
 */
@Entity
@Table(name = SchemaConstants.User.TABLE_NAME)
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class User extends AbstractEntityWithSimpleProperties<Long, User> implements EntityLongId<User>, EntityWithCacheAccess {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = SchemaConstants.PRIMARY_KEY, unique = true, nullable = false)
	private Long id;

	@Column(name = "active", unique = false, nullable = false)
	private Boolean active = true;

	@Column(name = "created", updatable = false, nullable = false)
	private Long created;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "password", nullable = false)
	@ToString.Exclude
	private String password;

	@Column(name = "privileged", unique = false, nullable = false)
	private Boolean privileged = false;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "owner")
	@ToString.Exclude
	private Team team;

	/**
	 * Transient calculated value.
	 */
	@Transient
	private transient LocalDateTime createdDateTime;

	@Transient
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	private transient BiFunction<Long, Class<? extends EntityLongId>, ? extends EntityLongId> cachedEntityGetter;

	public boolean isActive() {
		return Boolean.TRUE.equals(getActive());
	}

	public boolean isPrivileged() {
		return Boolean.TRUE.equals(getPrivileged());
	}

	public void setCreated(final Long created) {
		this.created = created;
		// reset calculated value
		this.createdDateTime = null;
	}

	public LocalDateTime getCreatedDateTime() {
		if (createdDateTime == null && created != null) {
			createdDateTime = DateTimeUtils.createLocalDateTime(created);
		}
		return createdDateTime;
	}

	public void setCreatedDateTime(final LocalDateTime createdDateTime) {
		if (createdDateTime == null) {
			setCreated(null);

		} else {
			setCreated(DateTimeUtils.toEpochMilli(createdDateTime));
			this.createdDateTime = createdDateTime;
		}
	}

	public Team getTeam() {
		if (cachedEntityGetter == null || team == null) {
			return team;
		}
		// if cache access is available, then is used
		return (Team) cachedEntityGetter.apply(team.getId(), Team.class);
	}
}
