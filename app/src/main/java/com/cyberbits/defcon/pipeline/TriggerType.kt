package com.cyberbits.defcon.pipeline

enum class TriggerType(val rawValue: String, val baseScore: Double, val label: String) {
    ACTIVE_EXPLOITATION("active_exploitation", 70.0, "Active Exploitation"),
    KEV_ADDITION("kev_addition", 70.0, "KEV Addition"),
    CRITICAL_SCOPE_VULN("critical_scope_vuln", 65.0, "Critical Scope Vuln"),
    CONFIRMED_BREACH("confirmed_breach", 65.0, "Confirmed Breach"),
    APT_CAMPAIGN("apt_campaign", 60.0, "APT Campaign"),
    MALWARE_CAMPAIGN("malware_campaign", 55.0, "Malware Campaign");

    companion object {
        fun fromRaw(raw: String?): TriggerType? = entries.firstOrNull { it.rawValue == raw }
    }
}

data class TriggerMatch(val trigger: TriggerType, val matchedText: String)
