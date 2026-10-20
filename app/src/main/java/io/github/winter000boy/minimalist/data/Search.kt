package io.github.winter000boy.minimalist.data

import java.text.Normalizer
import java.util.Locale

private val combiningMarks = Regex("\\p{Mn}+")
private val wordSeparators = charArrayOf(' ', '-', '_', '.', '&')

/** Lowercase, trimmed, accents removed: "Café " -> "cafe". */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(combiningMarks, "")
        .lowercase(Locale.ROOT)
        .trim()

/**
 * Lower is better: 0 = label starts with the query, 1 = a later word does,
 * 2 = it appears anywhere. Null means no match. Both inputs must be normalized.
 */
fun matchRank(searchKey: String, query: String): Int? = when {
    searchKey.startsWith(query) -> 0
    searchKey.split(*wordSeparators).any { it.startsWith(query) } -> 1
    searchKey.contains(query) -> 2
    else -> null
}

/** The A–Z sidebar letter for a normalized label; '#' for digits and symbols. */
fun indexLetterOf(searchKey: String): Char {
    val first = searchKey.firstOrNull() ?: return '#'
    return if (first.isLetter()) first.uppercaseChar() else '#'
}
