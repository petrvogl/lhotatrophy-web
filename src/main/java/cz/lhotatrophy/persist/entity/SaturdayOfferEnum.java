package cz.lhotatrophy.persist.entity;

/**
 *
 * @author Petr Vogl
 */
public enum SaturdayOfferEnum {
	NONE,
	VECERE {
		@Override
		public int getPrice() {
			return 260;
		}
	},
	ALL_INCLUSIVE {
		@Override
		public int getPrice() {
			return 970;
		}
	};

	public int getPrice() {
		return 0;
	}
}
