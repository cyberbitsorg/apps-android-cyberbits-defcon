package com.cyberbits.defcon.model

/**
 * Source colors — muted slate palette, deliberately distinct from the DEFCON warning
 * palette (white/red/yellow/green/blue) so source badges don't read as severity.
 * Matches docker `services/frontend/src/lib/constants.ts` SOURCE_COLORS / SOURCE_LABELS.
 */
enum class FeedSource(
    val displayName: String,
    val abbreviation: String,
    val feedUrl: String,
    val accentHex: String,
    val format: Format,
) {
    BLEEPING_COMPUTER("Bleeping Computer", "BC", "https://www.bleepingcomputer.com/feed/", "#cbd5e1", Format.RSS),
    HACKER_NEWS("Hacker News", "HN", "https://thehackernews.com/feeds/posts/default", "#94a3b8", Format.ATOM),
    HACKREAD("HackRead", "HR", "https://hackread.com/feed/", "#64748b", Format.RSS),
    SECURITY_AFFAIRS("Security Affairs", "SA", "https://securityaffairs.com/wp-json/wp/v2/posts?per_page=100", "#475569", Format.WORDPRESS_JSON),
    THE_REGISTER("The Register", "TR", "https://www.theregister.com/security/headlines.atom", "#334155", Format.ATOM);

    enum class Format { RSS, ATOM, WORDPRESS_JSON }
}
