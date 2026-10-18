package com.cardcontrol.app.ui.cards

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardPrimaryActionsLayoutTest {
    @Test
    fun compactWidthStacksActionsToKeepLabelsReadable() {
        assertTrue(shouldStackCardPrimaryActions(359f, 1f))
        assertFalse(shouldStackCardPrimaryActions(400f, 1f))
    }

    @Test
    fun increasedFontScaleStacksActionsEvenOnWideScreens() {
        assertTrue(shouldStackCardPrimaryActions(480f, 1.2f))
    }
}
