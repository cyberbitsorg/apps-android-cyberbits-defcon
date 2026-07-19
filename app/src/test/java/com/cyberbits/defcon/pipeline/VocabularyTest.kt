package com.cyberbits.defcon.pipeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyTest {

    @Test fun tier1ContainsExpectedTerms() {
        assertTrue(Vocabulary.tier1.isNotEmpty())
        assertEquals(20, Vocabulary.tier1.size)
        assertTrue("zero-day" in Vocabulary.tier1)
        assertTrue("nation-state" in Vocabulary.tier1)
        assertTrue("supply chain attack" in Vocabulary.tier1)
        assertEquals("zero-day", Vocabulary.tier1.first())
        assertEquals("malicious dependency", Vocabulary.tier1.last())
    }

    @Test fun tier2ContainsExpectedTerms() {
        assertTrue(Vocabulary.tier2.isNotEmpty())
        assertEquals(44, Vocabulary.tier2.size)
        assertTrue("ransomware" in Vocabulary.tier2)
        assertTrue("rce" in Vocabulary.tier2)
        assertTrue("lateral movement" in Vocabulary.tier2)
        assertEquals("ransomware", Vocabulary.tier2.first())
        assertEquals("infecting", Vocabulary.tier2.last())
    }

    @Test fun tier3ContainsExpectedTerms() {
        assertTrue(Vocabulary.tier3.isNotEmpty())
        assertEquals(21, Vocabulary.tier3.size)
        assertTrue("vulnerability" in Vocabulary.tier3)
        assertTrue("cve" in Vocabulary.tier3)
        assertTrue("payload" in Vocabulary.tier3)
    }

    @Test fun wordBoundarySetIsNonEmpty() {
        assertTrue(Vocabulary.wordBoundaryRequired.isNotEmpty())
        assertEquals(setOf("rce", "apt", "rat", "0-day", "0day", "0 day"), Vocabulary.wordBoundaryRequired)
    }

    @Test fun tier1ContainsNumericZeroDaySpellings() {
        assertTrue("0-day" in Vocabulary.tier1)
        assertTrue("0day" in Vocabulary.tier1)
        assertTrue("0 day" in Vocabulary.tier1)
    }

    @Test fun wordBoundaryRequiredContainsNumericZeroDaySpellings() {
        assertTrue("0-day" in Vocabulary.wordBoundaryRequired)
        assertTrue("0day" in Vocabulary.wordBoundaryRequired)
        assertTrue("0 day" in Vocabulary.wordBoundaryRequired)
    }

    @Test fun criticalSectorsContainCoreEntries() {
        assertTrue(Vocabulary.criticalSectors.isNotEmpty())
        assertEquals(26, Vocabulary.criticalSectors.size)
        assertTrue("power grid" in Vocabulary.criticalSectors)
        assertTrue("hospital" in Vocabulary.criticalSectors)
        assertTrue("k-12" in Vocabulary.criticalSectors)
    }

    @Test fun threatActorsContainCoreEntries() {
        assertTrue(Vocabulary.threatActors.isNotEmpty())
        assertEquals(16, Vocabulary.threatActors.size)
        assertTrue("lockbit" in Vocabulary.threatActors)
        assertTrue("lazarus" in Vocabulary.threatActors)
        assertTrue("volt typhoon" in Vocabulary.threatActors)
    }

    @Test fun knownStealersContainCoreEntries() {
        assertTrue(Vocabulary.knownStealers.isNotEmpty())
        assertEquals(9, Vocabulary.knownStealers.size)
        assertTrue("redline" in Vocabulary.knownStealers)
        assertTrue("lumma" in Vocabulary.knownStealers)
        assertTrue("raccoon" in Vocabulary.knownStealers)
    }
}
