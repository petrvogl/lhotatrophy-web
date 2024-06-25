package cz.lhotatrophy.persist.entity;

/**
 *
 * @author Petr Vogl
 */
public enum AccommodationOfferEnum {

	NO("bez ubytování", 0),
	YES("s ubytováním", 2040);

	private final String description;
	private final int price;

	private AccommodationOfferEnum(final String description, final int price) {
		this.description = description;
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}
}
