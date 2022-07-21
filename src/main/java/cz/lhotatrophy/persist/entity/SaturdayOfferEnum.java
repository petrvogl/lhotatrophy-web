package cz.lhotatrophy.persist.entity;

/**
 *
 * @author Petr Vogl
 */
public enum SaturdayOfferEnum {
	NONE(""),
	VECERE("pouze večeří") {
		@Override
		public int getPrice() {
			return 260;
		}
	},
	ALL_INCLUSIVE("ubytování + večeře") {
		@Override
		public int getPrice() {
			return 970;
		}
	};

	private final String description;

	private SaturdayOfferEnum(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return 0;
	}
}
