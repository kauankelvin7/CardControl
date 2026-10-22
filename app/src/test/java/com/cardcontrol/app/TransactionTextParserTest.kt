package com.cardcontrol.app

import com.cardcontrol.app.util.TransactionTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionTextParserTest {
    @Test
    fun textoCompartilhadoPreencheCompraSemPermissaoDeNotificacao() {
        val result = TransactionTextParser.parse(
            "Compra de R$ 1.250,90 aprovada em MERCADO CENTRAL, cartão final 1234, 10x de R$ 125,09"
        )!!
        assertEquals(125090L, result.amountCents)
        assertEquals("MERCADO CENTRAL", result.merchant)
        assertEquals(10, result.installments)
        assertEquals("1234", result.lastFourDigits)
    }

    @Test
    fun totalExplicitoVemAntesDaParcela() {
        val result = TransactionTextParser.parse(
            "Parcela: R$ 33,33\nValor total: R$ 100,00\nEstabelecimento: FARMACIA"
        )!!
        assertEquals(10000L, result.amountCents)
        assertEquals("FARMACIA", result.merchant)
    }

    @Test
    fun textoSemValorNaoGeraCompra() {
        assertNull(TransactionTextParser.parse("Compra aprovada: sem valor"))
    }

    @Test
    fun estornoNaoPodeVirarDespesa() {
        assertNull(TransactionTextParser.parse("Estorno de R$ 50,00 em MERCADO"))
    }

    @Test
    fun pixRecebidoNaoPodeVirarCompraNoCredito() {
        assertNull(TransactionTextParser.parse("Pix recebido R$ 25,00"))
    }

    @Test
    fun textoExcessivoEhLimitado() {
        val result = TransactionTextParser.parse("A".repeat(5_000) + " Compra R$ 200,00")
        assertNull(result)
    }

    @Test
    fun entradaInteiraEhConvertidaParaCentavos() {
        assertEquals(3500L, TransactionTextParser.parse("Valor total: R$ 35")?.amountCents)
    }
}
