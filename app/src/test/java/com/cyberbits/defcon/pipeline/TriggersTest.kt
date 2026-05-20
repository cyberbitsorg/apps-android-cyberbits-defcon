package com.cyberbits.defcon.pipeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TriggersTest {

    @Test fun activeExploitationDetected() {
        val m = Triggers.detect("attackers are actively exploiting cve-2024-1234".lowercase())
        // Bare "exploiting" only matches with a vuln-like indicator within ~80 chars — cve-id qualifies.
        // The exact matcher in iOS is the activelyExploiting pattern OR the cve-context exploiting pattern.
        assertNotNull(m)
        assertEquals(TriggerType.ACTIVE_EXPLOITATION, m?.trigger)
    }

    @Test fun activeExploitationNegatedNotDetected() {
        val text = "vendor confirms the flaw was not actively exploited in attacks".lowercase()
        val m = Triggers.detect(text)
        // isNegated should suppress this match. Other triggers shouldn't fire either on this text.
        assertNull(m)
    }

    @Test fun kevAdditionDetected() {
        val text = "cisa adds new flaw to its known exploited vulnerabilities catalog"
        assertNotNull(Triggers.detect(text))
    }

    @Test fun confirmedBreachRequiresScale() {
        // "small site breached by intruder" lacks scale words → null.
        assertNull(Triggers.detect("small site breached by intruder"))
        val big = "data breach exposes millions of records at major retailer"
        assertEquals(TriggerType.CONFIRMED_BREACH, Triggers.detect(big)?.trigger)
    }

    @Test fun contestContextSuppressesHighSeverityTriggers() {
        val text = "researchers actively exploited a chrome bug during pwn2own"
        val m = Triggers.detect(text)
        // active_exploitation must NOT fire (contest context suppresses it). m may be null or a lower-severity match.
        assertNotEquals(TriggerType.ACTIVE_EXPLOITATION, m?.trigger)
    }

    @Test fun criticalScopeVulnNeedsAmplifier() {
        assertNull(Triggers.detect("zero-day found in obscure plugin"))
        val ample = "zero-day affects every version of windows server"
        assertEquals(TriggerType.CRITICAL_SCOPE_VULN, Triggers.detect(ample)?.trigger)
    }

    @Test fun newsletterTitleDetected() {
        assertEquals(true, Triggers.isNewsletterTitle("Weekly Roundup: top stories"))
        assertEquals(false, Triggers.isNewsletterTitle("Patch released for critical bug"))
    }

    // confirmed_breach: repos/packages as scale entities

    @Test fun confirmedBreach_githubRepos_commaScale() {
        val text = "github confirms breach of 3,800 repos via malicious vscode extension"
        val m = Triggers.detect(text.lowercase())
        assertNotNull(m)
        assertEquals(TriggerType.CONFIRMED_BREACH, m?.trigger)
    }

    @Test fun confirmedBreach_npmPackages_commaScale() {
        val text = "npm registry data breach exposes source code of 15,000 packages"
        val m = Triggers.detect(text.lowercase())
        assertNotNull(m)
        assertEquals(TriggerType.CONFIRMED_BREACH, m?.trigger)
    }

    @Test fun confirmedBreach_packageRegistry_sectorScale() {
        val text = "npm package registry confirms breach of developer credentials"
        val m = Triggers.detect(text.lowercase())
        assertNotNull(m)
        assertEquals(TriggerType.CONFIRMED_BREACH, m?.trigger)
    }

    @Test fun noFP_repos_withoutBreachVerb() {
        val text = "developer shares 3,800 repos on github under open source license"
        val m = Triggers.detect(text.lowercase())
        assertNotEquals(TriggerType.CONFIRMED_BREACH, m?.trigger)
    }

    @Test fun noFP_packages_withoutBreachVerb() {
        val text = "npm publishes 15,000 packages per day as ecosystem grows"
        val m = Triggers.detect(text.lowercase())
        assertNotEquals(TriggerType.CONFIRMED_BREACH, m?.trigger)
    }

    // Vocabulary: supply chain terms in tier1

    @Test fun tier1_containsSupplyChainTerms() {
        assert(Vocabulary.tier1.contains("malicious extension"))
        assert(Vocabulary.tier1.contains("malicious plugin"))
        assert(Vocabulary.tier1.contains("malicious package"))
        assert(Vocabulary.tier1.contains("malicious dependency"))
    }

    @Test fun criticalSectors_containsPackageRegistry() {
        assert(Vocabulary.criticalSectors.contains("package registry"))
    }
}
