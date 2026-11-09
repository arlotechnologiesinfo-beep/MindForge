package com.mindforge101.app

import com.mindforge101.app.games.Catalog
import com.mindforge101.app.ui.GameIcons
import com.mindforge101.app.ui.GameInstructions
import org.junit.Assert.assertEquals
import org.junit.Test

class GameIconCoverageTest {
    @Test
    fun everyGameHasItsOwnIcon() {
        val missing = Catalog.games.map { it.id }.filter { it !in GameIcons.ids }
        assertEquals("games without an icon: $missing", emptyList<String>(), missing)
    }

    @Test
    fun everyGameHasInstructions() {
        val missing = Catalog.games.map { it.id }.filter { it !in GameInstructions.ids }
        assertEquals("games without instructions: $missing", emptyList<String>(), missing)
    }

    @Test
    fun noOrphanIcons() {
        val ids = Catalog.games.map { it.id }.toSet()
        assertEquals(emptyList<String>(), GameIcons.ids.filter { it !in ids })
    }
}
