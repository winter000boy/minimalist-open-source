package io.github.winter000boy.minimalist

import io.github.winter000boy.minimalist.data.indexLetterOf
import io.github.winter000boy.minimalist.data.matchRank
import io.github.winter000boy.minimalist.data.normalizedForSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchTest {

    @Test
    fun normalizesCaseAccentsAndWhitespace() {
        assertEquals("cafe", " Café ".normalizedForSearch())
    }

    @Test
    fun ranksPrefixThenWordStartThenAnywhere() {
        assertEquals(0, matchRank("instagram", "ins"))
        assertEquals(1, matchRank("google maps", "map"))
        assertEquals(2, matchRank("whatsapp", "app"))
        assertNull(matchRank("whatsapp", "xyz"))
    }

    @Test
    fun groupsNonLettersUnderHash() {
        assertEquals('I', indexLetterOf("instagram"))
        assertEquals('#', indexLetterOf("1password"))
        assertEquals('#', indexLetterOf(""))
    }
}
