package com.cardcontrol.app

import com.cardcontrol.app.service.BankNotificationParser
import com.cardcontrol.app.service.NotificationCategory
import com.cardcontrol.app.service.ParsedBankExpense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regressão do parser único de notificações bancárias.
 *
 * Os textos abaixo reproduzem a estrutura de notificações públicas desses bancos
 * (valor, estabelecimento, final do cartão, parcelas); não são capturas de dispositivos reais.
 */
class BankNotificationParserTest {

    private fun parseOk(pkg: String, title: String?, text: String?): ParsedBankExpense {
        val result = BankNotificationParser.parse(pkg, title, text)
        assertNotNull("Esperava compra reconhecida para: $title | $text", result)
        return result!!
    }

    // ---------- Formatos por banco suportado ----------

    @Test
    fun `nubank - compra a vista com final do cartao`() {
        val r = parseOk(
            "com.nu.production",
            "Compra aprovada",
            "Compra de R$ 45,90 APROVADA em PADARIA CENTRAL para o cartão com final 4321."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(4590L, r.amountCents)
        assertEquals("PADARIA CENTRAL", r.merchant)
        assertEquals(1, r.installments)
        assertEquals("4321", r.lastFourDigits)
    }

    @Test
    fun `itau - compra parcelada em milhar ignora valor da parcela`() {
        val r = parseOk(
            "com.itau",
            "Itaú",
            "Compra aprovada no valor de R$ 1.850,00 em AMAZON BR em 10x de R$ 185,00 cartão final 9988."
        )
        assertEquals("Itaú", r.bankName)
        assertEquals(185_000L, r.amountCents)
        assertEquals("AMAZON BR", r.merchant)
        assertEquals(10, r.installments)
        assertEquals("9988", r.lastFourDigits)
    }

    @Test
    fun `inter - compra no credito com valor antes do estabelecimento`() {
        val r = parseOk(
            "br.com.intermedium",
            "Compra aprovada",
            "Compra no crédito aprovada no cartão final 1234 no valor de R$ 50,00 em LOJAS RENNER."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(5_000L, r.amountCents)
        assertEquals("LOJAS RENNER", r.merchant)
        assertEquals("1234", r.lastFourDigits)
    }

    @Test
    fun `c6 - estabelecimento delimitado por virgula`() {
        val r = parseOk(
            "com.c6bank.app",
            null,
            "Compra no crédito aprovada: R$ 89,90 em POSTO SHELL, cartão final 5678."
        )
        assertEquals("C6 Bank", r.bankName)
        assertEquals(8_990L, r.amountCents)
        assertEquals("POSTO SHELL", r.merchant)
        assertEquals("5678", r.lastFourDigits)
    }

    @Test
    fun `santander - data e hora nao viram parcelas`() {
        val r = parseOk(
            "com.santander.app",
            "Santander",
            "Compra aprovada no cartao final 1234 de R$ 120,00 em DROGASIL em 15/10 as 10:00"
        )
        assertEquals("Santander", r.bankName)
        assertEquals(12_000L, r.amountCents)
        assertEquals("DROGASIL", r.merchant)
        assertEquals(1, r.installments)
        assertEquals("1234", r.lastFourDigits)
    }

    @Test
    fun `bradesco cartoes - compra com data completa`() {
        val r = parseOk(
            "br.com.bradesco.cartoes",
            "Bradesco Cartões",
            "Compra aprovada no cartão final 4321 de R$ 35,50 em RESTAURANTE SABOR em 15/10/2026 às 12:30."
        )
        assertEquals("Bradesco Cartões", r.bankName)
        assertEquals(3_550L, r.amountCents)
        assertEquals("RESTAURANTE SABOR", r.merchant)
        assertEquals("4321", r.lastFourDigits)
    }

    @Test
    fun `bradesco - compra no debito`() {
        val r = parseOk(
            "com.bradesco",
            "Bradesco",
            "Compra no débito aprovada de R$ 22,00 em PADARIA PAO QUENTE, cartão final 0011."
        )
        assertEquals("Bradesco", r.bankName)
        assertEquals(2_200L, r.amountCents)
        assertEquals("PADARIA PAO QUENTE", r.merchant)
        assertEquals("0011", r.lastFourDigits)
    }

    @Test
    fun `caixa - estabelecimento antes do valor`() {
        val r = parseOk(
            "br.gov.caixa.cartoes",
            "CAIXA",
            "Compra aprovada em SUPERMERCADO BOM PRECO R$ 210,35 em 15/10 as 18:40, cartao final 1122."
        )
        assertEquals("Caixa", r.bankName)
        assertEquals(21_035L, r.amountCents)
        assertEquals("SUPERMERCADO BOM PRECO", r.merchant)
        assertEquals("1122", r.lastFourDigits)
    }

    @Test
    fun `picpay - final seguido de dois pontos`() {
        val r = parseOk(
            "com.picpay",
            "PicPay",
            "Compra aprovada no cartão PicPay final 3344: R$ 59,90 em IFOOD."
        )
        assertEquals("PicPay", r.bankName)
        assertEquals(5_990L, r.amountCents)
        assertEquals("IFOOD", r.merchant)
        assertEquals("3344", r.lastFourDigits)
    }

    @Test
    fun `mercado pago - estabelecimento delimitado por com seu cartao`() {
        val r = parseOk(
            "com.mercadopago.wallet",
            "Mercado Pago",
            "Compra aprovada de R$ 150,00 em MERCADOLIVRE com seu cartão final 7788."
        )
        assertEquals("Mercado Pago", r.bankName)
        assertEquals(15_000L, r.amountCents)
        assertEquals("MERCADOLIVRE", r.merchant)
        assertEquals("7788", r.lastFourDigits)
    }

    // ---------- Valores monetários ----------

    @Test
    fun `converte formatos monetarios sem perder centavos`() {
        assertEquals(185_000L, BankNotificationParser.parseAmountToCents("1.850,00"))
        assertEquals(185_000L, BankNotificationParser.parseAmountToCents("1850,00"))
        assertEquals(185_000L, BankNotificationParser.parseAmountToCents("1850.00"))
        assertEquals(185_000L, BankNotificationParser.parseAmountToCents("1,850.00"))
        assertEquals(10L, BankNotificationParser.parseAmountToCents("0,10"))
        assertEquals(123_457L, BankNotificationParser.parseAmountToCents("1.234,57"))
        assertEquals(99_999_999L, BankNotificationParser.parseAmountToCents("999.999,99"))
    }

    @Test
    fun `aceita BRL 1850 ponto 00 e BRL com separador de milhar`() {
        val dotDecimal = parseOk(
            "com.nu.production", "Compra aprovada",
            "Compra aprovada de BRL 1850.00 em APPLE.COM/BILL cartão final 4321."
        )
        assertEquals(185_000L, dotDecimal.amountCents)
        assertEquals("APPLE.COM/BILL", dotDecimal.merchant)

        val withThousands = parseOk(
            "com.nu.production", "Compra aprovada",
            "Compra aprovada de BRL 1,850.00 em STEAM GAMES cartão final 4321."
        )
        assertEquals(185_000L, withThousands.amountCents)
    }

    @Test
    fun `preserva centavos impares exatos`() {
        val r = parseOk(
            "com.nu.production", null,
            "Compra aprovada de R$ 1.234,57 em LOJA CENTRO cartão final 4321."
        )
        assertEquals(123_457L, r.amountCents)
    }

    @Test
    fun `rejeita valor sem centavos, zero ou acima do limite`() {
        assertNull(BankNotificationParser.parse("com.nu.production", null, "Compra aprovada de R$ 1.850 em LOJA X cartão final 4321."))
        assertNull(BankNotificationParser.parse("com.nu.production", null, "Compra aprovada de R$ 0,00 em LOJA X cartão final 4321."))
        assertNull(BankNotificationParser.parse("com.nu.production", null, "Compra aprovada de R$ 1.000.000,01 em LOJA X cartão final 4321."))
    }

    @Test
    fun `ignora limite disponivel ao extrair o valor da compra`() {
        val r = parseOk(
            "com.nu.production", "Compra aprovada",
            "Compra aprovada de R$ 80,00 em FARMACIA SAO JOAO, cartão final 4321. Limite disponível: R$ 1.920,00"
        )
        assertEquals(8_000L, r.amountCents)
        assertEquals("FARMACIA SAO JOAO", r.merchant)
    }

    @Test
    fun `valores distintos ambiguos nao geram lancamento`() {
        assertNull(
            BankNotificationParser.parse(
                "com.nu.production", "Compra aprovada",
                "Compra aprovada de R$ 100,00 em LOJA X cartão final 4321, ganhe R$ 10,00 de volta."
            )
        )
    }

    @Test
    fun `somente valor da parcela sem total nao gera lancamento`() {
        assertNull(
            BankNotificationParser.parse(
                "com.itau", null,
                "Compra aprovada em AMAZON BR em 10x de R$ 185,00 cartão final 9988."
            )
        )
    }

    // ---------- Título/texto nulos ou vazios ----------

    @Test
    fun `titulo e texto nulos ou vazios sao ignorados`() {
        assertNull(BankNotificationParser.parse("com.nu.production", null, null))
        assertNull(BankNotificationParser.parse("com.nu.production", "", ""))
        assertNull(BankNotificationParser.parse("com.nu.production", "   ", "\n\t"))
    }

    @Test
    fun `compra apenas no titulo com texto nulo e reconhecida`() {
        val r = parseOk("com.nu.production", "Compra aprovada de R$ 10,00 em LOJA ABC", null)
        assertEquals(1_000L, r.amountCents)
        assertEquals("LOJA ABC", r.merchant)
        assertNull(r.lastFourDigits)
    }

    // ---------- Mensagens que não são compra ----------

    @Test
    fun `mensagens nao transacionais sao ignoradas`() {
        val pkg = "com.nu.production"
        assertNull(BankNotificationParser.parse(pkg, "Seu cartão chegou", "Desbloqueie agora pelo app."))
        assertNull(BankNotificationParser.parse(pkg, "Código", "Seu código de verificação é 123456."))
        assertNull(BankNotificationParser.parse(pkg, "Token", "Seu token de segurança para acesso é 998877."))
        assertNull(BankNotificationParser.parse(pkg, "Segurança", "Alteração de senha confirmada com sucesso."))
    }

    @Test
    fun `fatura, cashback e ofertas sao ignorados mesmo com valor`() {
        val pkg = "com.nu.production"
        assertNull(BankNotificationParser.parse(pkg, "Sua fatura fechou", "Sua fatura fechou no valor de R$ 2.340,10 e vence dia 10."))
        assertNull(BankNotificationParser.parse("br.com.intermedium", "Oferta especial!", "Você ganhou aumento de limite de R$ 500,00 no seu cartão."))
        assertNull(BankNotificationParser.parse(pkg, "Cashback", "Compra aprovada em LOJA X rendeu cashback de R$ 5,00."))
        assertNull(BankNotificationParser.parse(pkg, "Promoção", "Compra aprovada com 10% de desconto em LOJA X: R$ 90,00."))
        assertNull(BankNotificationParser.parse(pkg, "Estorno", "Compra aprovada de R$ 90,00 em LOJA X foi estornada."))
        assertNull(BankNotificationParser.parse(pkg, "Compra negada", "Compra de R$ 90,00 em LOJA X negada no cartão final 4321."))
    }

    // ---------- Estabelecimento ----------

    @Test
    fun `estabelecimento ausente nao gera lancamento`() {
        assertNull(BankNotificationParser.parse("com.nu.production", "Compra aprovada", "Compra aprovada de R$ 30,00 no cartão final 4321."))
        assertNull(BankNotificationParser.parse("com.nu.production", "Compra aprovada", "Compra aprovada em cartão final 4321 de R$ 30,00."))
    }

    @Test
    fun `estabelecimento nao absorve palavras como cartao ou compra`() {
        val withCard = parseOk("com.nu.production", null, "Compra aprovada de R$ 45,90 em PADARIA CENTRAL cartão final 4321")
        assertEquals("PADARIA CENTRAL", withCard.merchant)

        val withPurchase = parseOk("com.nu.production", null, "Compra aprovada de R$ 45,90 em PADARIA CENTRAL compra no crédito final 4321")
        assertEquals("PADARIA CENTRAL", withPurchase.merchant)

        assertFalse(withCard.merchant.contains("cart", ignoreCase = true))
        assertFalse(withPurchase.merchant.contains("compra", ignoreCase = true))
    }

    // ---------- Parcelas ----------

    @Test
    fun `parcelas invalidas ou fora do limite nao geram lancamento`() {
        val pkg = "com.itau"
        assertNull(BankNotificationParser.parse(pkg, null, "Compra aprovada de R$ 100,00 em LOJA X em 0x cartão final 9988."))
        assertNull(BankNotificationParser.parse(pkg, null, "Compra aprovada de R$ 100,00 em LOJA X em 99x cartão final 9988."))
        assertNull(BankNotificationParser.parse(pkg, null, "Compra aprovada de R$ 100,00 em LOJA X em 150x cartão final 9988."))
        assertNull(BankNotificationParser.parse(pkg, null, "Compra aprovada de R$ 100,00 em LOJA X em 3x, cartão final 9988, parcelado em 4x."))
    }

    @Test
    fun `parcelas dentro do limite sao aceitas`() {
        val twelve = parseOk("com.itau", null, "Compra aprovada de R$ 1.200,00 em LOJA X em 12 vezes, cartão final 9988.")
        assertEquals(12, twelve.installments)
        val max = parseOk("com.itau", null, "Compra aprovada de R$ 7.200,00 em LOJA X em ${BankNotificationParser.MAX_INSTALLMENTS}x cartão final 9988.")
        assertEquals(BankNotificationParser.MAX_INSTALLMENTS, max.installments)
    }

    // ---------- Pacotes ----------

    @Test
    fun `pacotes nao suportados ou nao confirmados sao ignorados`() {
        val text = "Compra aprovada de R$ 500,00 em MERCADO LIVRE no cartao final 4321"
        listOf(
            "com.whatsapp",
            "br.com.xp.cartoes",
            "com.itaucard.activity",
            "br.com.santander.way",
            "com.google.android.apps.walletnfcrel",
            "com.samsung.android.spay",
            ""
        ).forEach { pkg ->
            assertFalse(pkg, BankNotificationParser.isSupportedPackage(pkg))
            assertNull(pkg, BankNotificationParser.parse(pkg, "Compra aprovada", text))
        }
        assertFalse(BankNotificationParser.isSupportedPackage(null))
        assertNull(BankNotificationParser.parse(null, "Compra aprovada", text))
    }

    @Test
    fun `lista de pacotes suportados e exatamente a confirmada`() {
        assertEquals(
            setOf(
                "com.nu.production", "br.com.intermedium", "com.itau", "com.c6bank.app",
                "com.santander.app", "com.bradesco", "br.com.bradesco.cartoes",
                "br.gov.caixa.cartoes", "com.picpay", "com.mercadopago.wallet"
            ),
            BankNotificationParser.SUPPORTED_PACKAGES
        )
    }

    // ---------- Final do cartão ----------

    @Test
    fun `cartao sem final e reconhecido com lastFour nulo`() {
        val r = parseOk("com.nu.production", "Compra aprovada", "Compra aprovada de R$ 25,00 em CAFE DO PONTO.")
        assertNull(r.lastFourDigits)
        assertEquals("CAFE DO PONTO", r.merchant)
    }

    @Test
    fun `cartao mascarado com asteriscos e reconhecido`() {
        val r = parseOk("com.nu.production", null, "Compra aprovada de R$ 25,00 em CAFE DO PONTO no cartão ****6543.")
        assertEquals("6543", r.lastFourDigits)
    }

    @Test
    fun `finais de cartao conflitantes nao geram lancamento`() {
        assertNull(
            BankNotificationParser.parse(
                "com.nu.production", null,
                "Compra aprovada de R$ 25,00 em CAFE DO PONTO cartão final 1111, adicional final 2222."
            )
        )
    }

    // ---------- Notificações repetidas ----------

    @Test
    fun `notificacoes repetidas geram o mesmo hash de deduplicacao`() {
        val text = "Compra de R$ 45,90 APROVADA em PADARIA CENTRAL para o cartão com final 4321."
        val first = parseOk("com.nu.production", "Compra aprovada", text)
        val second = parseOk("com.nu.production", "Compra aprovada", text)
        assertEquals(first.dedupHash, second.dedupHash)
        assertEquals(24, first.dedupHash.length)

        val otherAmount = parseOk("com.nu.production", "Compra aprovada", text.replace("45,90", "45,91"))
        assertNotEquals(first.dedupHash, otherAmount.dedupHash)
        val otherBank = parseOk("com.itau", "Compra aprovada", text)
        assertNotEquals(first.dedupHash, otherBank.dedupHash)
    }

    @Test
    fun `entrada gigante e limitada sem quebrar o parser`() {
        val noisy = "Compra aprovada de R$ 10,00 em LOJA ABC. " + "A".repeat(50_000)
        val r = BankNotificationParser.parse("com.nu.production", null, noisy)
        assertTrue(r == null || r.amountCents == 1_000L)
    }

    @Test
    fun `issuerFromCardName infere emissor canonico e rejeita ambiguidades`() {
        assertEquals("Nubank", BankNotificationParser.issuerFromCardName("Nubank Ultravioleta"))
        assertEquals("Banco Inter", BankNotificationParser.issuerFromCardName("Cartão Inter Black"))
        assertEquals("Itaú", BankNotificationParser.issuerFromCardName("Meu Itaú Click"))
        assertEquals("Bradesco", BankNotificationParser.issuerFromCardName("Bradesco Elo Nanquim"))
        assertNull(BankNotificationParser.issuerFromCardName("Cartão Genérico"))
        assertNull(BankNotificationParser.issuerFromCardName("Nubank e Itaú juntos"))
    }

    // ---------- Compras por Aproximação (NFC) e Carteiras Digitais ----------

    @Test
    fun `nubank - compra por aproximacao contactless`() {
        val r = parseOk(
            "com.nu.production",
            "Compra por aproximação",
            "Compra de R$ 32,50 por aproximação aprovada em SUPERMERCADO DIA no cartão final 1234."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.CARD_EXPENSE, r.category)
        assertEquals(3_250L, r.amountCents)
        assertEquals("SUPERMERCADO DIA", r.merchant)
        assertEquals("1234", r.lastFourDigits)
        assertEquals(1, r.installments)
    }

    @Test
    fun `inter - pagamento aprovado via carteira digital apple pay`() {
        val r = parseOk(
            "br.com.intermedium",
            "Apple Pay",
            "Pagamento aprovado de R$ 89,00 em RESTAURANTE JAPONES com cartão final 5678."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(NotificationCategory.CARD_EXPENSE, r.category)
        assertEquals(8_900L, r.amountCents)
        assertEquals("RESTAURANTE JAPONES", r.merchant)
        assertEquals("5678", r.lastFourDigits)
    }

    @Test
    fun `itau - transacao por aproximacao com valor antes da aprovacao`() {
        val r = parseOk(
            "com.itau",
            "Itaú Cartões",
            "Transação por aproximação no valor de R$ 15,00 aprovada em PADARIA ESTRELA final 4433."
        )
        assertEquals("Itaú", r.bankName)
        assertEquals(NotificationCategory.CARD_EXPENSE, r.category)
        assertEquals(1_500L, r.amountCents)
        assertEquals("PADARIA ESTRELA", r.merchant)
        assertEquals("4433", r.lastFourDigits)
    }

    @Test
    fun `nubank - compra confirmada voce pagou sem palavra aprovada`() {
        val r = parseOk(
            "com.nu.production",
            "Pagamento confirmado",
            "Você pagou R$ 99,00 em UBER TRIP com cartão final 1234."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.CARD_EXPENSE, r.category)
        assertEquals(9_900L, r.amountCents)
        assertEquals("UBER TRIP", r.merchant)
        assertEquals("1234", r.lastFourDigits)
    }

    @Test
    fun `c6 - transacao realizada no valor de`() {
        val r = parseOk(
            "com.c6bank.app",
            "C6 Bank",
            "Transação realizada de R$ 45,00 em FARMACIA RAIA com final 9988."
        )
        assertEquals("C6 Bank", r.bankName)
        assertEquals(NotificationCategory.CARD_EXPENSE, r.category)
        assertEquals(4_500L, r.amountCents)
        assertEquals("FARMACIA RAIA", r.merchant)
        assertEquals("9988", r.lastFourDigits)
    }

    // ---------- Pix Enviado ----------

    @Test
    fun `nubank - pix enviado com destinatario extraido com sucesso`() {
        val r = parseOk(
            "com.nu.production",
            "Transferência enviada",
            "Você transferiu R$ 150,00 para Marcos Souza via Pix."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(15_000L, r.amountCents)
        assertEquals("Marcos Souza", r.counterpart)
        assertNull(r.lastFourDigits)
    }

    @Test
    fun `nubank - pix enviado com titulo explicito`() {
        val r = parseOk(
            "com.nu.production",
            "Pix enviado",
            "Pix enviado no valor de R$ 85,00 para Dra Camila."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(8_500L, r.amountCents)
        assertEquals("Dra Camila", r.counterpart)
    }

    @Test
    fun `inter - pix enviado realizado com sucesso`() {
        val r = parseOk(
            "br.com.intermedium",
            "Pix enviado",
            "Você realizou um Pix de R$ 200,00 para Carlos Alberto."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(20_000L, r.amountCents)
        assertEquals("Carlos Alberto", r.counterpart)
    }

    @Test
    fun `itau - pix realizado enviado com sucesso`() {
        val r = parseOk(
            "com.itau",
            "Pix realizado",
            "Pix enviado com sucesso no valor de R$ 350,00 para Maria Oliveira."
        )
        assertEquals("Itaú", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(35_000L, r.amountCents)
        assertEquals("Maria Oliveira", r.counterpart)
    }

    @Test
    fun `bradesco - pix enviado`() {
        val r = parseOk(
            "com.bradesco",
            "Bradesco",
            "Transferência Pix de R$ 120,00 enviada para Joao Silva."
        )
        assertEquals("Bradesco", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(12_000L, r.amountCents)
        assertEquals("Joao Silva", r.counterpart)
    }

    @Test
    fun `santander - pix enviado`() {
        val r = parseOk(
            "com.santander.app",
            "Santander",
            "Pix enviado de R$ 45,00 para Padaria Central."
        )
        assertEquals("Santander", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(4_500L, r.amountCents)
        assertEquals("Padaria Central", r.counterpart)
    }

    @Test
    fun `c6 - pix enviado`() {
        val r = parseOk(
            "com.c6bank.app",
            "C6 Bank",
            "Pix de R$ 60,00 enviado com sucesso para Ana Paula."
        )
        assertEquals("C6 Bank", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(6_000L, r.amountCents)
        assertEquals("Ana Paula", r.counterpart)
    }

    @Test
    fun `mercado pago - pix enviado`() {
        val r = parseOk(
            "com.mercadopago.wallet",
            "Pix enviado",
            "Você transferiu R$ 40,00 para Lucas Santos via Pix."
        )
        assertEquals("Mercado Pago", r.bankName)
        assertEquals(NotificationCategory.PIX_SENT, r.category)
        assertEquals(4_000L, r.amountCents)
        assertEquals("Lucas Santos", r.counterpart)
    }

    // ---------- Pix Recebido ----------

    @Test
    fun `nubank - pix recebido com remetente extraido`() {
        val r = parseOk(
            "com.nu.production",
            "Transferência recebida",
            "Você recebeu uma transferência de R$ 50,00 de João Silva."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.PIX_RECEIVED, r.category)
        assertEquals(5_000L, r.amountCents)
        assertEquals("João Silva", r.counterpart)
    }

    @Test
    fun `inter - pix recebido`() {
        val r = parseOk(
            "br.com.intermedium",
            "Pix Recebido",
            "Você recebeu um Pix de R$ 300,00 de Pedro Alvares."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(NotificationCategory.PIX_RECEIVED, r.category)
        assertEquals(30_000L, r.amountCents)
        assertEquals("Pedro Alvares", r.counterpart)
    }

    @Test
    fun `itau - pix recebido`() {
        val r = parseOk(
            "com.itau",
            "Pix recebido",
            "Você recebeu um Pix de R$ 500,00 de Jose Pereira."
        )
        assertEquals("Itaú", r.bankName)
        assertEquals(NotificationCategory.PIX_RECEIVED, r.category)
        assertEquals(50_000L, r.amountCents)
        assertEquals("Jose Pereira", r.counterpart)
    }

    // ---------- Transferências TED e Boletos ----------

    @Test
    fun `nubank - ted enviada`() {
        val r = parseOk(
            "com.nu.production",
            "Transferência enviada",
            "Você transferiu R$ 1.000,00 para Pedro Alcantara via TED."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.TRANSFER_SENT, r.category)
        assertEquals(100_000L, r.amountCents)
        assertEquals("Pedro Alcantara", r.counterpart)
    }

    @Test
    fun `nubank - boleto pago`() {
        val r = parseOk(
            "com.nu.production",
            "Boleto pago",
            "Pagamento de R$ 250,00 do boleto CONDOMINIO realizado com sucesso."
        )
        assertEquals("Nubank", r.bankName)
        assertEquals(NotificationCategory.BILL_PAID, r.category)
        assertEquals(25_000L, r.amountCents)
    }

    @Test
    fun `inter - boleto liquidado`() {
        val r = parseOk(
            "br.com.intermedium",
            "Boleto liquidado",
            "Boleto de R$ 180,00 pago com sucesso em ENEL DISTRIBUICAO."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(NotificationCategory.BILL_PAID, r.category)
        assertEquals(18_000L, r.amountCents)
        assertEquals("ENEL DISTRIBUICAO", r.merchant)
    }

    // ---------- Regressão Inter: Compra no crédito sem símbolo monetário ----------

    @Test
    fun `inter - compra no credito sem simbolo monetario extrai valor estabelecimento e final`() {
        val r = parseOk(
            "br.com.intermedium",
            "Compra no credito.",
            "Ola, TITULAR TESTE. Voce acaba de comprar 112,86 em MERCADO TESTE. A compra foi no credito nacional, com cartao final 1234."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(NotificationCategory.CARD_EXPENSE, r.category)
        assertEquals(11_286L, r.amountCents)
        assertEquals("MERCADO TESTE", r.merchant)
        assertNotEquals("TITULAR TESTE", r.merchant)
        assertEquals("1234", r.lastFourDigits)
        assertEquals(1, r.installments)
    }

    @Test
    fun `inter - compra contextual com pontuacao mantem deduplicacao estavel`() {
        val text = "Você acaba de comprar 112,86 em MERCADO TESTE, a compra foi no crédito nacional, com cartão final 1234."
        val first = parseOk("br.com.intermedium", "Compra no crédito", text)
        val repeated = parseOk("br.com.intermedium", "Compra no crédito", text)
        assertEquals("MERCADO TESTE", first.merchant)
        assertEquals(first.dedupHash, repeated.dedupHash)
    }

    @Test
    fun `inter - compra contextual rejeita sinais ausentes ou ambiguos`() {
        val validShape = "Você acaba de comprar 112,86 em MERCADO TESTE. A compra foi no crédito nacional, com cartão final 1234."
        val invalidNotifications = listOf(
            "br.com.intermedium" to "Você acaba de comprar 112,86 em MERCADO TESTE. Compra no débito, cartão final 1234.",
            "br.com.intermedium" to "Você acaba de comprar 112,86 em MERCADO TESTE. Crédito nacional.",
            "br.com.intermedium" to "Você acaba de comprar 112,86 em MERCADO TESTE. Crédito nacional, cartão final 1234 e final 5678.",
            "br.com.intermedium" to "Você acaba de comprar 112,86 em. Crédito nacional, cartão final 1234.",
            "br.com.intermedium" to "Você acaba de comprar 112,86 em MERCADO TESTE. Crédito nacional; outro valor 5,00; cartão final 1234.",
            "br.com.intermedium" to "Você acaba de comprar 112,86 em MERCADO TESTE. R$ 5,00, crédito nacional, cartão final 1234.",
            "br.com.intermedium" to "Você acaba de comprar 112,86 em MERCADO TESTE. Compra estornada no crédito nacional, cartão final 1234.",
            "br.com.intermedium" to "Fatura: você acaba de comprar 112,86 em MERCADO TESTE. Crédito nacional, cartão final 1234.",
            "br.com.intermedium" to "Promoção: você acaba de comprar 112,86 em MERCADO TESTE. Crédito nacional, cartão final 1234.",
            "com.nu.production" to validShape,
            "br.com.pacote.nao.suportado" to validShape
        )

        invalidNotifications.forEach { (pkg, text) ->
            val title = if (text.contains("R$ 5,00")) "Compra aprovada" else "Compra no crédito"
            assertNull("Deveria rejeitar $pkg: $text", BankNotificationParser.parse(pkg, title, text))
        }
    }

    @Test
    fun `inter - compra no credito com colchetes ou pontuacao no final do cartao`() {
        val r = parseOk(
            "br.com.intermedium",
            "Compra no crédito",
            "Olá, TITULAR TESTE. Você acaba de comprar 112,86 em MERCADO TESTE. A compra foi no crédito nacional, com cartão final [1234]."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(11_286L, r.amountCents)
        assertEquals("MERCADO TESTE", r.merchant)
        assertEquals("1234", r.lastFourDigits)
        assertEquals(1, r.installments)
    }

    @Test
    fun `inter - compra no credito sanitiza pontuacao e caracteres especiais no estabelecimento`() {
        val cases = listOf(
            "Você acaba de comprar 50,00 em PADARIA 24H.. A compra foi no crédito nacional, com cartão final 1234." to "PADARIA 24H",
            "Você acaba de comprar 80,50 em FARMÁCIA & DROGARIA! A compra foi no crédito nacional, com cartão final 1234." to "FARMÁCIA & DROGARIA",
            "Você acaba de comprar 199,99 em POSTO BR-101; a compra foi no crédito nacional, com cartão final 1234." to "POSTO BR-101",
            "Você acaba de comprar 320,00 em @LOJA_OFICIAL. A compra foi no crédito nacional, com cartão final 1234." to "@LOJA_OFICIAL"
        )
        for ((text, expectedMerchant) in cases) {
            val r = parseOk("br.com.intermedium", "Compra no crédito", text)
            assertEquals(expectedMerchant, r.merchant)
            assertEquals("1234", r.lastFourDigits)
            assertEquals(1, r.installments)
        }
    }

    @Test
    fun `inter - compra no credito internacional e aceita`() {
        val r = parseOk(
            "br.com.intermedium",
            "Compra no crédito",
            "Olá, TITULAR TESTE. Você acaba de comprar 45,00 em DIGITAL OCEAN NY. A compra foi no crédito internacional, com cartão final 1234."
        )
        assertEquals("Inter", r.bankName)
        assertEquals(4_500L, r.amountCents)
        assertEquals("DIGITAL OCEAN NY", r.merchant)
        assertEquals("1234", r.lastFourDigits)
    }

    @Test
    fun `inter - compra com data e horario nao e rejeitada por valor conflitante`() {
        val r = parseOk(
            "br.com.intermedium",
            "Compra no crédito",
            "Olá, TITULAR TESTE. Você acaba de comprar 112,86 em MERCADO TESTE às 14:30 em 18/10. A compra foi no crédito nacional, com cartão final 1234."
        )
        assertEquals(11_286L, r.amountCents)
        assertEquals("MERCADO TESTE", r.merchant)
        assertEquals("1234", r.lastFourDigits)
    }
}
