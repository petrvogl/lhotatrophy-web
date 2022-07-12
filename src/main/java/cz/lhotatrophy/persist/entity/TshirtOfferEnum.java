package cz.lhotatrophy.persist.entity;

/**
 *
 * @author Petr Vogl
 */
public enum TshirtOfferEnum {
	NONE("") {
		@Override
		public int getPrice() {
			return 0;
		}
	},
	P_M("pánské M"),
	P_L("pánské L"),
	P_XL("pánské XL"),
	P_XXL("pánské XXL"),
	D_S("dámské S"),
	D_M("dámské M"),
	D_L("dámské L"),
	D_XL("dámské XL");

	private final String description;

	private TshirtOfferEnum(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return 350;
	}
}
