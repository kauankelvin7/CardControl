package com.cardcontrol.app.ui.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsScreenCopyTest {
    @Test
    fun cardCountLabel_usesPortugueseSingularAndPlural() {
        assertEquals("1 cartão", cardCountLabel(1))
        assertEquals("2 cartões", cardCountLabel(2))
    }

    @Test
    fun holderCountLabel_usesPortugueseSingularAndPlural() {
        assertEquals("1 pessoa", holderCountLabel(1))
        assertEquals("2 pessoas", holderCountLabel(2))
    }

    @Test
    fun walletCapacityPercent_preservesValuesAboveLimitAndHandlesZeroLimit() {
        assertEquals(50L, walletCapacityUsagePercent(5_000L, 10_000L))
        assertEquals(100L, walletCapacityUsagePercent(10_000L, 10_000L))
        assertEquals(150L, walletCapacityUsagePercent(15_000L, 10_000L))
        assertEquals(0L, walletCapacityUsagePercent(15_000L, 0L))
    }

    @Test
    fun purchaseCountLabel_usesSingularWhenThereIsOnePurchase() {
        assertEquals("1 compra", purchaseCountLabel(1))
        assertEquals("2 compras", purchaseCountLabel(2))
    }
}
