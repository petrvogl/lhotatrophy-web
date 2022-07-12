package cz.lhotatrophy.web.form;

import cz.lhotatrophy.persist.entity.FridayOfferEnum;
import cz.lhotatrophy.persist.entity.SaturdayOfferEnum;
import cz.lhotatrophy.persist.entity.TeamMember;
import cz.lhotatrophy.persist.entity.TshirtOfferEnum;
import cz.lhotatrophy.utils.EnumUtils;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Petr Vogl
 */
@Data
@NoArgsConstructor
public class TeamSettingsForm {

	private String name_1;
	private String friday_1;
	private String saturday_1;
	private String tshirt_1;
	//
	private String name_2;
	private String friday_2;
	private String saturday_2;
	private String tshirt_2;
	//
	private String name_3;
	private String friday_3;
	private String saturday_3;
	private String tshirt_3;
	//
	private String name_4;
	private String friday_4;
	private String saturday_4;
	private String tshirt_4;
	//
	private String name_5;
	private String friday_5;
	private String saturday_5;
	private String tshirt_5;

	private TeamMember getTeamMember(
			final String name,
			final String friday,
			final String saturday,
			final String tshirt
	) {
		final TeamMember member;
		final FridayOfferEnum fridayEnum = EnumUtils.decodeEnum(FridayOfferEnum.class, friday).orElse(FridayOfferEnum.NONE);
		final SaturdayOfferEnum saturdayEnum = EnumUtils.decodeEnum(SaturdayOfferEnum.class, saturday).orElse(SaturdayOfferEnum.NONE);
		final TshirtOfferEnum tshirtEnum = EnumUtils.decodeEnum(TshirtOfferEnum.class, tshirt).orElse(TshirtOfferEnum.NONE);
		member = new TeamMember(Optional.ofNullable(name).orElse(""), null);
		member.addProperty("friday", fridayEnum.name());
		member.addProperty("saturday", saturdayEnum.name());
		member.addProperty("tshirtCode", tshirtEnum.name());
		member.addProperty("tshirt", tshirtEnum.getDescription());
		return member;
	}

	private TeamMember getTeamMember(final int number) {
		final TeamMember member;
		switch (number) {
			case 1: {
				member = getTeamMember(name_1, friday_1, saturday_1, tshirt_1);
				break;
			}
			case 2: {
				member = getTeamMember(name_2, friday_2, saturday_2, tshirt_2);
				break;
			}
			case 3: {
				member = getTeamMember(name_3, friday_3, saturday_3, tshirt_3);
				break;
			}
			case 4: {
				member = getTeamMember(name_4, friday_4, saturday_4, tshirt_4);
				break;
			}
			case 5: {
				member = getTeamMember(name_5, friday_5, saturday_5, tshirt_5);
				break;
			}
			default:
				throw new IllegalArgumentException("Number must be between 1 and 5 innclusive.");
		}
		return member;
	}
	
	public Set<TeamMember> getTeamMembers() {
		final Set<TeamMember> members = new LinkedHashSet<>(5);
		for (int i = 1; i <= 5; i++) {
			members.add(getTeamMember(i));
		}
		return members;
	}
	
	public static TeamSettingsForm fromTeamMembers(final Set<TeamMember> members) {
		if (members == null || members.isEmpty()) {
			return new TeamSettingsForm();
		}
		// TODO
		return null;
	}
}
