package com.cyberbits.defcon.pipeline

import org.junit.Assert.assertEquals
import org.junit.Test

/** Ports of the docker news-aggregator tagger tests (test_tagger.py). */
class TaggerTest {

    @Test fun ransomwareHeadline() {
        val tags = Tagger.classifyTags(
            "Qilin ransomware claims attack on US healthcare organization",
            "The gang listed the victim on its leak site and is demanding a ransom.",
        )
        assertEquals(listOf("ransomware", "attacks"), tags)
    }

    @Test fun vulnerabilityHeadline() {
        val tags = Tagger.classifyTags(
            "CVE-2025-12345: critical RCE flaw in Cisco ASA firewall",
            "A remote code execution vulnerability patched in the latest advisory.",
        )
        assertEquals(listOf("vulnerabilities"), tags)
    }

    @Test fun malwareHeadline() {
        val tags = Tagger.classifyTags(
            "New Amos infostealer targets macOS users",
            "The stealer harvests credentials and crypto wallets from browsers.",
        )
        assertEquals(listOf("malware"), tags)
    }

    @Test fun breachHeadline() {
        val tags = Tagger.classifyTags(
            "Data breach at retailer exposes 3 million customer records",
            "The company confirmed leaked payment data in a security notice.",
        )
        assertEquals(listOf("breaches"), tags)
    }

    @Test fun aptHeadline() {
        val tags = Tagger.classifyTags(
            "Lazarus hackers target nuclear researchers in espionage campaign",
            "The state-sponsored group used custom malware against the defense sector.",
        )
        assertEquals("apt", tags[0])
    }

    @Test fun fraudHeadline() {
        val tags = Tagger.classifyTags(
            "Pig butchering scams drain victims of millions",
            "Fraud networks launder stolen funds through shell companies.",
        )
        assertEquals("fraud", tags[0])
    }

    @Test fun privacyHeadline() {
        val tags = Tagger.classifyTags(
            "GDPR ruling fines company over illegal tracking of personal data",
            "The regulator found surveillance practices violated data protection law.",
        )
        assertEquals(listOf("privacy"), tags)
    }

    @Test fun aiHeadlineWithFraudOverlap() {
        val tags = Tagger.classifyTags(
            "Hackers use AI-generated deepfakes in BEC campaign",
            "Artificial intelligence tools cloned an executive's voice to authorize transfers.",
        )
        assertEquals(listOf("ai", "fraud"), tags)
    }

    @Test fun supplyChainHeadline() {
        val tags = Tagger.classifyTags(
            "Malicious npm package steals secrets in supply chain attack",
            "The malicious dependency was downloaded thousands of times per week.",
        )
        assertEquals(listOf("supply-chain", "attacks"), tags)
    }

    @Test fun cloudHeadline() {
        val tags = Tagger.classifyTags(
            "Azure cloud storage buckets leave data publicly accessible",
            "Misconfigured S3 buckets exposed internal documents online.",
        )
        assertEquals("cloud", tags[0])
    }

    @Test fun icsOtHeadline() {
        val tags = Tagger.classifyTags(
            "Hackers breach water treatment facility SCADA systems",
            "Critical infrastructure operators warned utilities about the intrusion.",
        )
        assertEquals(listOf("ics-ot", "breaches"), tags)
    }

    @Test fun maxThreeTags() {
        val tags = Tagger.classifyTags(
            "Ransomware attack on water utility exploits CVE pitched by AI chatbot",
            "Extortion leak site posting follows phishing wave against cloud tenants.",
        )
        assertEquals(Tagger.maxTags, tags.size)
    }

    @Test fun titleMatchOutranksSummaryMatch() {
        val tags = Tagger.classifyTags("Data breach at retailer", "GDPR discussion follows.")
        assertEquals(listOf("breaches", "privacy"), tags)
    }

    @Test fun noMatchReturnsFallbackTag() {
        val tags = Tagger.classifyTags("Tech stocks rally on earnings", "Markets closed higher.")
        assertEquals(listOf(Vocabulary.fallbackTag), tags)
    }

    @Test fun shortTokensUseWordBoundaries() {
        // "ai" must match "AI" but not inside "said"; "apt" not inside "capture".
        assertEquals(listOf("ai"), Tagger.classifyTags("He said AI models were compared", ""))
        assertEquals(listOf(Vocabulary.fallbackTag), Tagger.classifyTags("The capture tool ran overnight", ""))
    }

    @Test fun emptyInputReturnsFallbackTag() {
        assertEquals(listOf(Vocabulary.fallbackTag), Tagger.classifyTags("", ""))
    }
}
