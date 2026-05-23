package com.cyberbits.defcon.pipeline

object Triggers {

    /** Explicit precedence order — matches docker `PRECEDENCE`. Highest-severity first. */
    private val precedence: List<TriggerType> = listOf(
        TriggerType.ACTIVE_EXPLOITATION,
        TriggerType.KEV_ADDITION,
        TriggerType.CEILING_CVSS,
        TriggerType.SUPPLY_CHAIN_COMPROMISE,
        TriggerType.CRITICAL_SCOPE_VULN,
        TriggerType.CONFIRMED_BREACH,
        TriggerType.APT_CAMPAIGN,
        TriggerType.MALWARE_CAMPAIGN,
    )

    /**
     * Detect the highest-precedence trigger in `text`. Returns null if none match.
     * `text` must be pre-lowercased. Contest/bug-bounty contexts (Pwn2Own, CTF,
     * HackerOne) exclude active_exploitation, kev_addition, critical_scope_vuln.
     */
    fun detect(text: String): TriggerMatch? {
        val contest = isContestContext(text)
        for (trigger in precedence) {
            if (contest && (trigger == TriggerType.ACTIVE_EXPLOITATION ||
                            trigger == TriggerType.KEV_ADDITION ||
                            trigger == TriggerType.CEILING_CVSS ||
                            trigger == TriggerType.CRITICAL_SCOPE_VULN)) {
                continue
            }
            val matched = match(trigger, text)
            if (matched != null) {
                return TriggerMatch(trigger, matched)
            }
        }
        return null
    }

    fun isContestContext(text: String): Boolean =
        cachedRegex(contestPattern).containsMatchIn(text)

    fun hasScopeAmplifier(text: String): Boolean =
        cachedRegex(scopeAmplifierPattern).containsMatchIn(text)

    fun isNewsletterTitle(title: String): Boolean =
        cachedRegex("""\bnewsletter\b|\bround\s+\d+\b|\bweekly\s+(roundup|recap|digest)\b|\bweek in (review|security)\b""")
            .containsMatchIn(title)

    // MARK: - Regex cache

    private val cache: MutableMap<String, Regex> = mutableMapOf()

    private fun cachedRegex(pattern: String): Regex {
        cache[pattern]?.let { return it }
        val r = Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        cache[pattern] = r
        return r
    }

    // MARK: - Patterns

    private val activeExploitationPatterns: List<String> = listOf(
        """\bactively exploited\b""",
        """\bactive exploitation\b""",
        """\bexploited in (the wild|attacks?|the open)\b""",
        """\bin[- ]the[- ]wild\b""",
        """\bunder (active )?attack\b""",
        """\bunder exploitation\b""",
        """\b(zero[- ]day|0[- ]day) exploited\b""",
        """\bexploits?\s+(cve|known|critical|vuln|flaw|bug|zero[- ]?day)""",
        """\bexploited\s+as\s+a?\s*(zero|0)[- ]?day\b""",
        """\bweaponized\b""",
        """\bactively\s+exploiting\b""",
        // Bare "exploiting" must reference a vuln-like indicator within ~80 chars to
        // avoid false positives like "exploiting human trust" / "exploiting confusion".
        """\bexploiting\b[^.\n]{0,80}(cve|flaw|bug|vuln|zero[- ]?day|0[- ]?day|rce|remote code execution|patch|advisory|exploit)""",
        """\bcashing in on\s+(?:fresh|new|unpatched|critical)?\s*(?:\w+\s+){0,3}(flaw|bug|vuln|vulnerability|cve)""",
        """\bwave of attacks?\b""",
        """\bin active use\s+by\b""",
        """\bpreying on\b""",
    )

    private val kevPatterns: List<String> = listOf(
        """\bcisa\b.{0,50}\b(kev|known exploited)\b.{0,50}\b(adds?|added)\b""",
        """\bcisa\b.{0,50}\b(adds?|added)\b.{0,50}\b(kev|known exploited)\b""",
        """\bkev catalog\b""",
    )

    private val breachVerbPattern: String =
        """\b(confirms? breach|breached|data breach|data leak|data dump|dump puts|leaked database|claims dump|""" +
        """exposed (personal information|data of|records of|user data|customer data|credentials|emails|accounts)|""" +
        """(stolen|stole|theft of|claimed theft of|stealing) .{0,40}(records|users|emails|customers|accounts|tokens|credentials))\b"""

    private val breachScaleMillionsPattern = """\bmillions? of\b"""
    private val breachScaleNumericPattern = """\b\d+[kKmM]\+?\s*(users|records|emails|accounts|customers|repos|repositories|packages|codebases?)\b"""
    private val breachScaleCommaPattern =
        """\b\d{1,3}(?:,\d{3})+\s+(users|records|emails|accounts|customers|people|schools|patients|students|employees|individuals|organizations|repos|repositories|packages|codebases?)\b"""

    private val criticalSectorPattern: String
        get() {
            val esc = Vocabulary.criticalSectors.joinToString("|") { Regex.escape(it) }
            return """\b(""" + esc + """)\b"""
        }

    private val threatActorPattern: String
        get() {
            val esc = Vocabulary.threatActors.joinToString("|") { Regex.escape(it) }
            return """\b(""" + esc + """)\b"""
        }

    private val aptHintsPattern = """\b(apt|nation[- ]state|state[- ]sponsored)\b"""
    private val aptVerbPattern = """\b(campaign|targets|targeting|espionage|breach)\b"""
    private val aptBreachVerbPattern = """\b(breach|breached|theft|stole|stolen|leak|leaked|hack|hacked|compromise|compromised|exfiltrat\w+)\b"""

    private val scopeAmplifierPattern: String = listOf(
        """\b(all|most)\s+major\s+(linux|windows|macos|android|ios|distros?|distributions?|systems?|platforms?|versions?|releases?)\b""",
        """\bevery\s+(version|release)\b""",
        """\bbillions?\s+of\s+(devices|users)\b""",
        """\bany\s+(linux|windows|macos|android|ios)\b""",
    ).joinToString("|")

    private val negationPattern = """\b(no|never|not|without|unsuccessful|fails?|failed|prevent(?:ed|s)?|denied|blocked)\b"""

    private val vulnKeywordPattern = """\b(zero[- ]?day|0[- ]?day|RCE|remote code execution|privilege escalation|unauthenticated)\b"""

    private val malwareNounPattern = """\b(stealer|infostealer|info-stealer|loader|dropper|keylogger|rat|malware)\b"""

    private val knownStealerPattern: String
        get() {
            val esc = Vocabulary.knownStealers.joinToString("|") { Regex.escape(it) }
            return """\b(""" + esc + """)\b"""
        }

    private val campaignVerbPattern = """\b(targeting|deploys?|distributes?|spreads?|drops?|delivers?|campaign|infects?|infecting)\b"""

    // ceiling_cvss: explicit CVSS 10.0 / 9.8 / 9.9
    private val ceilingCvssPattern = """\bcvss[^\n]{0,20}?(10(?:\.0)?|9\.[89])\b"""

    // supply_chain_compromise: artifact + badness + scale
    private val supplyChainArtifactPattern =
        """\b(repos|repositories|packages|codebases?|workflows|ci/cd|registry|""" +
        """npm|pypi|rubygems|crates\.io|docker images?|container images?|maven|nuget)\b"""
    private val supplyChainBadnessPattern =
        """\b(malicious|compromised?|poisoned|backdoor(?:ed)?|typosquat\w*|trojanized|infected|hijacked)\b"""
    private val supplyChainScalePattern =
        """\b(\d{1,3}(?:,\d{3})+|\d{2,}|hundreds|thousands|dozens)\b"""

    private val contestPattern =
        """\bpwn2own\b|\bcapture[- ]the[- ]flag\b|\bctf\b|\bhack(?:athon|fest|the box)\b|""" +
        """\bbug bounty\b|\bdef ?con (?:ctf|village|finals?)\b|\bhackone\b|\bhackerone\b|""" +
        """\bzero day initiative\b|\bzdi\b"""

    // MARK: - Detection

    private fun match(trigger: TriggerType, text: String): String? {
        when (trigger) {
            TriggerType.ACTIVE_EXPLOITATION -> {
                for (pattern in activeExploitationPatterns) {
                    val matches = cachedRegex(pattern).findAll(text)
                    for (m in matches) {
                        if (!isNegated(text, m.range.first, m.range.last + 1)) {
                            return m.value
                        }
                    }
                }
                return null
            }

            TriggerType.KEV_ADDITION -> {
                for (pattern in kevPatterns) {
                    val m = cachedRegex(pattern).find(text)
                    if (m != null) return m.value
                }
                return null
            }

            TriggerType.CONFIRMED_BREACH -> {
                val verb = cachedRegex(breachVerbPattern).find(text) ?: return null
                val hasScale =
                    cachedRegex(breachScaleMillionsPattern).containsMatchIn(text) ||
                    cachedRegex(breachScaleNumericPattern).containsMatchIn(text) ||
                    cachedRegex(breachScaleCommaPattern).containsMatchIn(text) ||
                    cachedRegex(criticalSectorPattern).containsMatchIn(text)
                if (!hasScale) return null
                return verb.value
            }

            TriggerType.APT_CAMPAIGN -> {
                val hint = cachedRegex(aptHintsPattern).find(text)
                    ?: cachedRegex(threatActorPattern).find(text)
                    ?: return null
                if (!cachedRegex(aptVerbPattern).containsMatchIn(text)) return null
                val hasTarget =
                    cachedRegex(criticalSectorPattern).containsMatchIn(text) ||
                    cachedRegex(aptBreachVerbPattern).containsMatchIn(text)
                if (!hasTarget) return null
                return hint.value
            }

            TriggerType.CRITICAL_SCOPE_VULN -> {
                val vuln = cachedRegex(vulnKeywordPattern).find(text) ?: return null
                if (!hasScopeAmplifier(text)) return null
                return vuln.value
            }

            TriggerType.CEILING_CVSS -> {
                val m = cachedRegex(ceilingCvssPattern).find(text) ?: return null
                return m.value
            }

            TriggerType.SUPPLY_CHAIN_COMPROMISE -> {
                val artifact = cachedRegex(supplyChainArtifactPattern).find(text) ?: return null
                if (!cachedRegex(supplyChainBadnessPattern).containsMatchIn(text)) return null
                if (!cachedRegex(supplyChainScalePattern).containsMatchIn(text)) return null
                return artifact.value
            }

            TriggerType.MALWARE_CAMPAIGN -> {
                val noun = cachedRegex(malwareNounPattern).find(text)
                    ?: cachedRegex(knownStealerPattern).find(text)
                    ?: return null
                if (!cachedRegex(campaignVerbPattern).containsMatchIn(text)) return null
                return noun.value
            }
        }
    }

    private fun isNegated(text: String, start: Int, end: Int): Boolean {
        val pre = text.substring(maxOf(0, start - 30), start)
        if (cachedRegex(negationPattern).containsMatchIn(pre)) return true
        val post = text.substring(end, minOf(text.length, end + 60))
        return cachedRegex(negationPattern).containsMatchIn(post)
    }
}
