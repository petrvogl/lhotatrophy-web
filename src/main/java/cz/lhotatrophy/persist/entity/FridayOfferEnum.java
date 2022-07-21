package cz.lhotatrophy.persist.entity;

/**
 *
 * @author Petr Vogl
 */
public enum FridayOfferEnum {
	NONE("") {
		@Override
		public int getPrice() {
			return 0;
		}
	},
	RIZOTO("ubytování + bulgurové rizoto"),
	VRABEC("ubytování + moravský vrabec"),
	KURE("ubytování + kuřecí prsíčko");

	private final String description;

	private FridayOfferEnum(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return 870;
	}
}
