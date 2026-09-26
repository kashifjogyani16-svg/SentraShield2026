package com.sentra.shield.data.model

/**
 * Result of running [com.sentra.shield.util.ThreatAnalyzer] against an installed app.
 *
 * @param score 0-100 heuristic risk score (higher = riskier)
 * @param reasons human-readable list of every rule that fired
 */
data class ThreatScore(
    val score: Int,
    val reasons: List<String>
) {
    val level: ThreatLevel
        get() = when {
            score >= 70 -> ThreatLevel.DANGER
            score >= 40 -> ThreatLevel.WARNING
            score >= 15 -> ThreatLevel.INFO
            else -> ThreatLevel.SAFE
        }
}

enum class ThreatLevel {
    SAFE, INFO, WARNING, DANGER
}
