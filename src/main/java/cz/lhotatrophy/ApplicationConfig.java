package cz.lhotatrophy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Petr Vogl
 */
@Getter
@Log4j2
@Configuration
@EnableTransactionManagement
@PropertySources({
	@PropertySource("classpath:application.properties"),
	@PropertySource("file:${WEB_LOCAL_CONFIG}")})
public class ApplicationConfig {

	/**
	 * Default application {@link ZoneId}.
	 */
	public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Prague");
	/**
	 * Default application {@link TimeZone}.
	 */
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(DEFAULT_ZONE_ID);
	/**
	 * Total length of ideal route from the start to the finish in kilometers.
	 */
	@Nonnull
	@Value("${contest.mileage.idealRouteLength:170}")
	private transient Integer idealRouteLength;
	/**
	 * The exact timestamp of the game completion limit.
	 */
	@Nonnull
	@Value("${contest.timeLimit.epochMilli:1665250200000}")
	private transient Long completionLimitEpochMilli;
	private transient Instant completionLimitInstant;
	/**
	 * Penalty (given in kilometers) for every minute that exceeds the limit if
	 * a team has finished the game after the time limit has expired.
	 */
	@Nonnull
	@Value("${contest.timeLimit.penaltyPerMinute:2}")
	private transient Long penaltyPerMinute;
	/**
	 * The maximum number of seconds by which the time limit can be exceeded.
	 * After that, the team is disqualified.
	 */
	@Nonnull
	@Value("${contest.timeLimit.maxOverLimitSeconds:1800}")
	private transient Long maxOverLimitSeconds;
	/**
	 * Price of the insurance against winning (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.price.insurance:50}")
	private transient Integer insurancePrice;
	/**
	 * Bonus for acquiring the contest code (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.bonus.cCodeAcquired:10}")
	private transient Integer cCodeAcquiredBonus;
	/**
	 * Penalty for not entering the contest code (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.aCodeMissing:75}")
	private transient Integer aCodeMissingPenalty;
	/**
	 * Penalty for not entering the contest code (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.bCodeMissing:50}")
	private transient Integer bCodeMissingPenalty;
	/**
	 * Penalty for using solution hint (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.hintRevealed:25}")
	private transient Integer hintRevealedPenalty;
	/**
	 * Penalty for using solution procedure (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.procedureRevealed:50}")
	private transient Integer procedureRevealedPenalty;
	/**
	 * Penalty for revealing the solution (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.solutionRevealed:50}")
	private transient Integer solutionRevealedPenalty;

	@PostConstruct
	public void init() {
		// TimeZone config
		TimeZone.setDefault(DEFAULT_TIME_ZONE);
		log.info("TimeZone set to {}: {}", DEFAULT_ZONE_ID.getId(), new Date().toString());
		// Contest config
		final LocalDateTime completionLimit = LocalDateTime.ofInstant(getTimeLimitInstant(), DEFAULT_ZONE_ID);
		final String completionLimitStr = DateTimeFormatter.ofPattern("d. M. yyyy HH:mm:ss.SSS").format(completionLimit);
		log.info("Contest settings\n\n"
				+ "General:\n"
				+ "  completionLimit\t{} ms ({})\n"
				+ "  penaltyPerMinute\t{} km\n"
				+ "  maxOverLimitSeconds\t{} sec\n"
				+ "  idealRouteLength\t{} km\n"
				+ "  insurancePrice\t{} km\n"
				+ "Codes:\n"
				+ "  cCodeAcquiredBonus\t{} km\n"
				+ "  aCodeMissingPenalty\t{} km\n"
				+ "  bCodeMissingPenalty\t{} km\n"
				+ "Hints:\n"
				+ "  hintRevealedPenalty\t\t{} km\n"
				+ "  procedureRevealedPenalty\t{} km\n"
				+ "  solutionRevealedPenalty\t{} km\n",
				completionLimitEpochMilli, completionLimitStr,
				penaltyPerMinute,
				maxOverLimitSeconds,
				idealRouteLength,
				insurancePrice,
				cCodeAcquiredBonus,
				aCodeMissingPenalty,
				bCodeMissingPenalty,
				hintRevealedPenalty,
				procedureRevealedPenalty,
				solutionRevealedPenalty
		);
	}

	@Bean
	@Autowired
	public JpaTransactionManager transactionManager(final SessionFactory sessionFactory) {
		final JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(sessionFactory);
		return txManager;
	}

	/**
	 * Gets the exact instant of the game completion limit.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getTimeLimitInstant() {
		if (completionLimitInstant == null) {
			synchronized (this) {
				if (completionLimitInstant == null) {
					completionLimitInstant = Instant
							.ofEpochMilli(completionLimitEpochMilli)
							.atZone(DEFAULT_ZONE_ID)
							.toInstant();
				}
			}
		}
		return completionLimitInstant;
	}
}
