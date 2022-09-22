package cz.lhotatrophy.web.form;

import cz.lhotatrophy.persist.entity.TeamContestProgress;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Data;

/**
 *
 * @author Petr Vogl
 */
@Data
public class ContestProgressForm {

	/**
	 * Informs whether the team has been disqualified.
	 */
	private Boolean disqualified;
	/**
	 * Informs whether the team has insurance which protects from winning the
	 * first prize.
	 */
	private Boolean insuranceAgainstWinning;
	/**
	 * The mileage state on the car when the game has started (km unit).
	 */
	private Integer mileageAtStart;
	/**
	 * The mileage state on the car when the team finished the game (km unit).
	 */
	private Integer mileageAtFinish;
	/**
	 * Penalty for breaking the rules of the game (km unit).
	 */
	private Integer extraMileagePenalty;
	/**
	 * Bonus as a compensation from the organizer (km unit).
	 */
	private Integer extraMileageBonus;
	/**
	 * The exact timestamp the team finished the game.
	 */
	private Long timestampAtFinish;

	public void setFrom(@Nullable final TeamContestProgress progress) {
		if (progress == null) {
			// no progress yet
			return;
		}
		disqualified = progress.isDisqualified();
		insuranceAgainstWinning = progress.isInsuranceAgainstWinning();
		mileageAtStart = progress.getMileageAtStart();
		mileageAtFinish = progress.getMileageAtFinish();
		extraMileagePenalty = progress.getExtraMileagePenalty();
		extraMileageBonus = progress.getExtraMileageBonus();
		timestampAtFinish = progress.getTimestampAtFinish();
	}

	@Nonnull
	public TeamContestProgress toContestProgress() {
		final TeamContestProgress progress = new TeamContestProgress();
		progress.setDisqualified(Boolean.TRUE.equals(disqualified));
		progress.setInsuranceAgainstWinning(Boolean.TRUE.equals(insuranceAgainstWinning));
		progress.setMileageAtStart(mileageAtStart);
		progress.setMileageAtFinish(mileageAtFinish);
		progress.setExtraMileagePenalty(extraMileagePenalty);
		progress.setExtraMileageBonus(extraMileageBonus);
		progress.setTimestampAtFinish(timestampAtFinish);
		return progress;
	}
}
