package com.cyberbits.defcon.model

enum class FeedSource(
    val displayName: String,
    val feedUrl: String,
    val accentHex: String,
    val format: Format,
) {
    BLEEPING_COMPUTER("Bleeping Computer", "https://www.bleepingcomputer.com/feed/", "#f97316", Format.RSS),
    HACKER_NEWS("Hacker News", "https://thehackernews.com/feeds/posts/default", "#8b5cf6", Format.ATOM),
    HACKREAD("HackRead", "https://hackread.com/feed/", "#ec4899", Format.RSS),
    SECURITY_AFFAIRS("Security Affairs", "https://securityaffairs.com/wp-json/wp/v2/posts?per_page=100", "#e11d48", Format.WORDPRESS_JSON),
    THE_REGISTER("The Register", "https://www.theregister.com/security/headlines.atom", "#06b6d4", Format.ATOM);

    enum class Format { RSS, ATOM, WORDPRESS_JSON }
}
