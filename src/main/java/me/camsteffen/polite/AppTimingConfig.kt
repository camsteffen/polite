package me.camsteffen.polite

import org.threeten.bp.Duration

val defaultAppTimingConfig = AppTimingConfig(
    alarmTolerance = Duration.ofMinutes(1),
    lookahead = Duration.ofHours(30),
    refreshWindowDelay = Duration.ofHours(4),
    refreshWindowLength = Duration.ofHours(25),
    ruleEventBoundaryTolerance = Duration.ofMinutes(1)
)

class AppTimingConfig(

    /**
     * How close the current time must be to a previously recorded "next alarm clock" in order for
     * a cancel on alarm to be triggered.
     */
    val alarmTolerance: Duration,

    /**
     * How far to look into the future for upcoming calendar events or schedule events.
     * This determines how often Polite will need to refresh.
     */
    val lookahead: Duration,

    /**
     * If no rule event begins or ends in the near future, the next refresh time is determined by
     * the OS within a specified window that begins after [refreshWindowDelay] and lasts for
     * [refreshWindowLength].
     */
    val refreshWindowDelay: Duration,

    /** @see refreshWindowDelay */
    val refreshWindowLength: Duration,

    /**
     * A rule event may be considered to be started or ended this duration before its actual time.
     * This helps to avoid excessive refresh scheduling.
     *
     * Note: This is a small duration (seconds) which is separate from the user-configured
     * activation/deactivation offsets.
     */
    val ruleEventBoundaryTolerance: Duration
) {

    init {
        require(lookahead >= (refreshWindowDelay + refreshWindowLength)) {
            "lookahead must be >= (refreshWindowDelay + refreshWindowLength)"
        }
    }
}
