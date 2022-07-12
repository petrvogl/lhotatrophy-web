package cz.lhotatrophy.persist.entity;

/**
 *
 * @author Petr Vogl
 */
public enum FridayOfferEnum {
	NONE {
		@Override
		public int getPrice() {
			return 0;
		}
	},
	RIZOTO,
	VRABEC,
	KURE;

	public int getPrice() {
		return 870;
	}
}
