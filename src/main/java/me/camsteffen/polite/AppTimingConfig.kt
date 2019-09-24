package me.camsteffen.polite

import org.threeten.bp.Duration

val defaultAppTimingConfig = AppTimingConfig(
    lookahead = Duration.ofHours(30),
    maxRingerRestoreDelay = Duration.ofHours(4),
    refreshWindowDelay = Duration.ofHours(4),
    refreshWindowLength = Duration.ofHours(25),
    ruleEventBoundaryTolerance = Duration.ofMinutes(1)
)

class AppTimingConfig(

    /**
     * How far to look into the future for upcoming calendar events or schedule events.
     * This determines how often Polite will need to refresh.
     */
    val lookahead: Duration,

    /**
     * If Polite Mode is deactivated more than this duration after the rule event is supposed to
     * end, the ringer mode will not be restored.
     */
    val maxRingerRestoreDelay: Duration,

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
