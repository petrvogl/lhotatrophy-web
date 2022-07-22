package cz.lhotatrophy.persist.entity;

import cz.lhotatrophy.persist.SchemaConstants;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Team member entity.
 *
 * @author Petr Vogl
 */
@Entity
@Table(name = SchemaConstants.Team.TeamMember.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TeamMember extends AbstractEntityWithSimpleProperties<Long, TeamMember> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = SchemaConstants.PRIMARY_KEY, unique = true, nullable = false)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = SchemaConstants.Team.FK_NAME, nullable = false)
	@ToString.Exclude
	private Team team;

	public TeamMember(final String name, final Team team) {
		this.name = name;
		this.team = team;
	}

	@ToString.Include(name = "teamId")
	Long getTeamId() {
		return team == null ? null : team.getId();
	}
}
