package cz.lhotatrophy.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Objects;
import lombok.SneakyThrows;

/**
 * Comparator for collection of Czech {@code String}s.
 *
 * @see https://www.java.cz/article/ceskerazeni
 * @see https://nb.vse.cz/~jjkastl/iksy/kap12i.htm
 * @see https://cs.wikipedia.org/wiki/Abecedn%C3%AD_%C5%99azen%C3%AD
 */
public class CzechComparator implements Comparator<String>, Serializable {

	/**
	 * Orderings of characters.
	 */
	private static final String RULES
			= "< ' ' < A,a;Á,á;À,à;Â,â;Ä,ä;Ą,ą < B,b < C,c;Ç,ç < Č,č < D,d;Ď,ď < E,e;É,é;È,è;Ê,ê;Ě,ě"
			+ "< F,f < G,g < H,h < CH,Ch,cH,ch < I,i;Í,í < J,j < K,k < L,l;Ľ,ľ;Ł,ł < M,m < N,n;Ň,ň"
			+ "< O,o;Ó,ó;Ô,ô;Ö,ö < P,p < Q,q < R,r;Ŕ,ŕ < Ř,ř < S,s < Š,š < T,t;Ť,ť"
			+ "< U,u;Ú,ú;Ů,ů;Ü,ü < V,v < W,w < X,x < Y,y;Ý,ý < Z,z;Ż,ż < Ž,ž"
			+ "< 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9"
			+ "< '.' < ',' < ';' < '?' < '¿' < '!' < '¡' < ':' < '\"' < '\'' < '«' < '»'"
			+ "< '-' < '|' < '/' < '\\' < '(' < ')' < '[' < ']' < '<' < '>' < '{' < '}'"
			+ "< '&' < '¢' < '£' < '¤' < '¥' < '§' < '©' < '®' < '%' < '‰' < '$'"
			+ "< '=' < '+' < '×' < '*' < '÷' < '~'";

	/**
	 * Singleton.
	 */
	private static final CzechComparator INSTANCE = new CzechComparator();

	/**
	 * Returns an instance.
	 *
	 * @return an instance
	 */
	public static CzechComparator instance() {
		return INSTANCE;
	}

	/**
	 * Comparator that uses the orderings of characters defined in the RULES
	 * string.
	 */
	private final RuleBasedCollator comparator;
	/**
	 * HashCode value of this instance.
	 */
	private transient Integer hashCode;

	/**
	 * Constructor.
	 *
	 * @throws ParseException if there is an error in the rules string
	 */
	@SneakyThrows
	public CzechComparator() {
		this.comparator = new RuleBasedCollator(RULES);
	}

	/**
	 * Compares two strings. We just call the compare method on the comparator
	 * variable.
	 *
	 * @param s1 the first string to be compared
	 * @param s2 the second string to be compared
	 * @return a negative integer, zero, or a positive integer as the first
	 * string is less than, equal to, or greater than the second
	 */
	@Override
	public int compare(final String s1, final String s2) {
		return comparator.compare(s1, s2);
	}

	/**
	 * Returns a comparator that imposes the reverse ordering of this
	 * comparator.
	 *
	 * @return a comparator that imposes the reverse ordering of this
	 * comparator.
	 */
	@Override
	public Comparator<String> reversed() {
		final Comparator reversed = comparator.reversed();
		return reversed;
	}

	@Override
	public int hashCode() {
		if (hashCode == null) {
			// cache the hashCode as this comparator is immutable
			hashCode = 877 + Objects.hashCode(this.comparator);
		}
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CzechComparator other = (CzechComparator) obj;
		return Objects.equals(this.comparator, other.comparator);
	}
}
