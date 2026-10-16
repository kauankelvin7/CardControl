package com.cardcontrol.app

import com.cardcontrol.app.data.local.AppDao
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.TransactionEntity
import com.cardcontrol.app.data.repository.CardRepository
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.TimeUnit

class CardRepositoryRecurringSyncTest {

    @Test
    fun `sync nao duplica recorrencia em variacoes de caixa do mesmo comercio`() = runBlocking {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            TransactionEntity(
                id = 1L, cardId = 10L, holderId = 100L, merchant = "NETFLIX.COM",
                totalAmount = 5590L, purchaseTimestamp = now - TimeUnit.DAYS.toMillis(30)
            ),
            TransactionEntity(
                id = 2L, cardId = 10L, holderId = 100L, merchant = "Netflix.com",
                totalAmount = 5590L, purchaseTimestamp = now
            )
        )
        val persisted = mutableListOf(
            RecurringChargeEntity(
                id = 7L, cardId = 10L, holderId = 100L, merchant = "Netflix.com",
                expectedAmount = 5590L, lastAmount = 5590L,
                lastChargedTimestamp = now - TimeUnit.DAYS.toMillis(30)
            )
        )
        var nextId = 8L

        val handler = InvocationHandler { _: Any, method: Method, args: Array<out Any?>? ->
            when (method.name) {
                "observeHolders", "observeCards", "observeAllInstallments", "observeAllPayments",
                "observeActiveRecurringCharges", "observeAllRecurringCharges" -> emptyFlow<Any>()
                "getAllTransactionsSnapshot" -> transactions
                "getAllRecurringChargesSnapshot" -> persisted.toList()
                "insertRecurringCharge" -> {
                    val charge = args!![0] as RecurringChargeEntity
                    val index = persisted.indexOfFirst { it.id == charge.id && charge.id != 0L }
                    if (index >= 0) {
                        persisted[index] = charge
                        charge.id
                    } else {
                        val id = nextId++
                        persisted.add(charge.copy(id = id))
                        id
                    }
                }
                "getRecurringChargeByMerchantAndCard" -> null
                else -> null
            }
        }
        val dao = Proxy.newProxyInstance(
            AppDao::class.java.classLoader,
            arrayOf(AppDao::class.java),
            handler
        ) as AppDao

        CardRepository(dao).syncRecurringCharges()

        assertEquals("Variações de caixa não podem criar outro registro", 1, persisted.size)
        assertEquals(7L, persisted.single().id)
        assertEquals("Netflix.com", persisted.single().merchant)
        assertEquals(now, persisted.single().lastChargedTimestamp)
    }
}
