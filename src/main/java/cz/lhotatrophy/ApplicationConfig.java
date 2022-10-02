package cz.lhotatrophy;

import cz.lhotatrophy.utils.DateTimeUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 *
 * @author Petr Vogl
 */
@Getter
@Log4j2
@Configuration
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
	 * Maximum number of teams.
	 */
	@Nonnull
	@Value("${contest.teamRegistration.limit:50}")
	private transient Integer teamRegistrationLimit;
	/**
	 * The exact opening time of registration.
	 */
	@Nonnull
	@Value("${contest.teamRegistration.open:2022-07-04 18:00}")
	private transient String teamRegistrationOpenString;
	private transient Instant teamRegistrationOpenInstant;
	/**
	 * The exact closing time of registration.
	 */
	@Nonnull
	@Value("${contest.teamRegistration.closed:2022-07-16 00:00}")
	private transient String teamRegistrationClosedString;
	private transient Instant teamRegistrationClosedInstant;
	/**
	 * Total length of ideal route from the start to the finish in kilometers.
	 */
	@Nonnull
	@Value("${contest.mileage.idealRouteLength:170}")
	private transient Integer idealRouteLength;
	/**
	 * The exact timestamp of opening the game for starting mileage inputs.
	 */
	@Nonnull
	@Value("${contest.timeLimit.gameOpen:2022-10-07 16:00}")
	private transient String gameOpenTimeString;
	private transient Instant gameOpenInstant;
	/**
	 * The exact timestamp of starting the game.
	 */
	@Nonnull
	@Value("${contest.timeLimit.gameStart:2022-10-08 06:00}")
	private transient String gameStartTimeString;
	private transient Instant gameStartInstant;
	/**
	 * The exact timestamp of the game completion limit.
	 */
	@Nonnull
	@Value("${contest.timeLimit.gameEnd:2022-10-08 19:30}")
	private transient String gameEndTimeString;
	private transient Instant gameEndInstant;
	/**
	 * The exact timestamp of publishing game results.
	 */
	@Nonnull
	@Value("${contest.timeLimit.gameResults:2022-10-09 00:00}")
	private transient String gameResultsTimeString;
	private transient Instant gameResultsInstant;
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
	@Value("${contest.insurance.price:50}")
	private transient Integer insurancePrice;
	/**
	 * Condition for making insurance available (given in number of C-codes).
	 */
	@Nonnull
	@Value("${contest.insurance.cCodeLimit:5}")
	private transient Integer insuranceCCodeLimit;
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
	@Value("${contest.penalty.procedureRevealed:10}")
	private transient Integer procedureRevealedPenalty;
	/**
	 * Penalty for revealing the solution (given in kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.solutionRevealed:25}")
	private transient Integer solutionRevealedPenalty;
	/**
	 * Penalty for revealing the exact destination location (kilometers).
	 */
	@Nonnull
	@Value("${contest.penalty.destinationRevealed:150}")
	private transient Integer destinationRevealedPenalty;

	@PostConstruct
	public void init() {
		// TimeZone config
		TimeZone.setDefault(DEFAULT_TIME_ZONE);
		log.info("TimeZone set to {}: {}", DEFAULT_ZONE_ID.getId(), new Date().toString());
		// Contest config
		log.info("Contest settings\n\n"
				+ "General:\n"
				+ "  registrationsOpen\t{}\n"
				+ "  registrationsClosed\t{}\n"
				+ "  gameOpen\t\t{}\n"
				+ "  gameStart\t\t{}\n"
				+ "  gameEnd\t\t{}\n"
				+ "  penaltyPerMinute\t{} km\n"
				+ "  maxOverLimitSeconds\t{} sec\n"
				+ "  idealRouteLength\t{} km\n"
				+ "  insurancePrice\t{} km\n"
				+ "  insuranceUnlocked\t{} C-codes\n"
				+ "Codes:\n"
				+ "  cCodeAcquiredBonus\t{} km\n"
				+ "  aCodeMissingPenalty\t{} km\n"
				+ "  bCodeMissingPenalty\t{} km\n"
				+ "Hints:\n"
				+ "  hintRevealedPenalty\t\t{} km\n"
				+ "  procedureRevealedPenalty\t{} km\n"
				+ "  solutionRevealedPenalty\t{} km\n"
				+ "  destinationRevealedPenalty\t{} km\n",
				teamRegistrationOpenString,
				teamRegistrationClosedString,
				gameOpenTimeString,
				gameStartTimeString,
				gameEndTimeString,
				penaltyPerMinute,
				maxOverLimitSeconds,
				idealRouteLength,
				insurancePrice,
				insuranceCCodeLimit,
				cCodeAcquiredBonus,
				aCodeMissingPenalty,
				bCodeMissingPenalty,
				hintRevealedPenalty,
				procedureRevealedPenalty,
				solutionRevealedPenalty,
				destinationRevealedPenalty
		);
	}

	/**
	 * Return the exact opening time of registration.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getTeamRegistrationOpenInstant() {
		if (teamRegistrationOpenInstant == null) {
			synchronized (this) {
				if (teamRegistrationOpenInstant == null) {
					final LocalDateTime dateTime = DateTimeUtils.parse(teamRegistrationOpenString, DateTimeUtils.YYYY_MM_DD__HH_MM);
					teamRegistrationOpenInstant = DateTimeUtils.toInstant(dateTime);
				}
			}
		}
		return teamRegistrationOpenInstant;
	}

	/**
	 * Return the exact closing time of registration.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getTeamRegistrationClosedInstant() {
		if (teamRegistrationClosedInstant == null) {
			synchronized (this) {
				if (teamRegistrationClosedInstant == null) {
					final LocalDateTime dateTime = DateTimeUtils.parse(teamRegistrationClosedString, DateTimeUtils.YYYY_MM_DD__HH_MM);
					teamRegistrationClosedInstant = DateTimeUtils.toInstant(dateTime);
				}
			}
		}
		return teamRegistrationClosedInstant;
	}

	/**
	 * Return the exact instant of the game completion limit.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getGameOpenInstant() {
		if (gameOpenInstant == null) {
			synchronized (this) {
				if (gameOpenInstant == null) {
					final LocalDateTime dateTime = DateTimeUtils.parse(gameOpenTimeString, DateTimeUtils.YYYY_MM_DD__HH_MM);
					gameOpenInstant = DateTimeUtils.toInstant(dateTime);
				}
			}
		}
		return gameOpenInstant;
	}

	/**
	 * Return the exact instant of the game completion limit.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getGameStartInstant() {
		if (gameStartInstant == null) {
			synchronized (this) {
				if (gameStartInstant == null) {
					final LocalDateTime dateTime = DateTimeUtils.parse(gameStartTimeString, DateTimeUtils.YYYY_MM_DD__HH_MM);
					gameStartInstant = DateTimeUtils.toInstant(dateTime);
				}
			}
		}
		return gameStartInstant;
	}

	/**
	 * Return the exact instant of the game completion limit.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getGameEndInstant() {
		if (gameEndInstant == null) {
			synchronized (this) {
				if (gameEndInstant == null) {
					final LocalDateTime dateTime = DateTimeUtils.parse(gameEndTimeString, DateTimeUtils.YYYY_MM_DD__HH_MM);
					gameEndInstant = DateTimeUtils.toInstant(dateTime);
				}
			}
		}
		return gameEndInstant;
	}

	/**
	 * Return the exact instant of publishing game results.
	 */
	@Nonnull
	@SuppressWarnings("DoubleCheckedLocking")
	public Instant getGameResultsInstant() {
		if (gameResultsInstant == null) {
			synchronized (this) {
				if (gameResultsInstant == null) {
					final LocalDateTime dateTime = DateTimeUtils.parse(gameResultsTimeString, DateTimeUtils.YYYY_MM_DD__HH_MM);
					gameResultsInstant = DateTimeUtils.toInstant(dateTime);
				}
			}
		}
		return gameResultsInstant;
	}
}
