# Regressão de Notificação Inter: Compra no Crédito
Data: 2026-10-18
Status: GATES_APROVADOS_AGUARDANDO_DISPOSITIVO_E_NOTIFICACAO_REAL

## Evidência Fornecida pelo Usuário
- Emissor aparente: Banco Inter
- Título: `Compra no crédito`
- Fixture anônimo do corpo: `Olá, [TITULAR]. Você acaba de comprar 112,86 em [ESTABELECIMENTO]. A compra foi no crédito nacional, com cartão final [1234].`
- Regra de privacidade: Nenhum dado real de titular, estabelecimento ou final de cartão registrado em código ou testes.

## Diagnóstico e Causa Raiz
1. **Ausência de símbolo monetário:** O padrão `AMOUNT_REGEX` e o parser anterior exigiam prefixo monetário (`R$`, `BRL`, `US$`). A notificação do Inter envia apenas o decimal puro `112,86`.
2. **Gatilho de compra:** O padrão anterior não capturava a locução contextual *"Você acaba de comprar [VALOR] em [ESTABELECIMENTO]"*.
3. **Colisão de datas/horários:** Um padrão genérico de valor sem moeda colidia com carimbos de data/hora (ex.: `às 14:30`, `18/10`), interpretando-os como valores conflitantes e rejeitando a notificação.
4. **Delimitação de estabelecimento e sufixo:** O nome do estabelecimento frequentemente continha pontuações ou era seguido por ponto final (`.` antes de *"A compra foi..."*), e o sufixo do cartão continha colchetes (`[1234]`).

## Alterações Implementadas (Causa Raiz)
- **`app/src/main/java/com/cardcontrol/app/service/BankNotificationParser.kt`**:
  - `INTER_BARE_PURCHASE_REGEX`: captura estrita e contextualizada de `voc[eê]\s+acaba\s+de\s+comprar\s+$AMOUNT_NUMBER\s+em\s+([\p{L}\p{N}*&'@#_./\- ]{2,40}?)(?=\s*(?:[àa]s\s+[0-9]|[.,;!?]|\s*\.\s+a\s+compra|\s+a\s+compra\s+foi\b|$))`
  - `INTER_NATIONAL_CREDIT_REGEX`: suporta `credito\s+(?:nacional|internacional)`
  - `CARD_DIGITS_REGEX`: aceita colchetes/hífens/dois-pontos `(?:\bfinal|\bterminad[oa]\s+em|\*{2,})\s*[:#\-\[\(]?\s*([0-9]{4})(?![0-9])`
  - Sanitização de carimbos temporais (`às HH:mm` e `dd/MM`) antes da validação de valores conflitantes em `extractInterBarePurchase`.
  - Remoção de pontuação residual de estabelecimentos (`.` , `-` , `/` , `!` , `?` , `;` , `:`).
  - Bloqueio estrito de casos não-transacionais: fatura, estorno, cancelamento, promoção, compra negada e valores ambíguos.
  - Fixação de `installments = 1` para compras no crédito nacional à vista.

- **`app/src/test/java/com/cardcontrol/app/BankNotificationParserTest.kt`**:
  - Testes positivos: valor `112,86` sem moeda, estabelecimento com pontuação e caracteres especiais (`PADARIA 24H..`, `FARMÁCIA & DROGARIA!`, `POSTO BR-101;`, `@LOJA_OFICIAL`), sufixo com colchetes `[1234]`, crédito internacional e timestamps.
  - Testes negativos: valor sem moeda fora do Inter (rejeitado), sem crédito nacional (rejeitado), fatura/estorno/cancelamento/promoção/compra negada (rejeitados), valores conflitantes (rejeitados) e garantia de que o titular nunca é extraído como estabelecimento.

- **`app/src/test/java/com/cardcontrol/app/BankNotificationProcessorTest.kt`**:
  - 5 testes de integração end-to-end do fluxo completo (`parser → processor → associação emissor/final → titular padrão → registerExpense`):
    1. Registro de ponta a ponta de despesa Inter com valor sem moeda (11.286 centavos Long, 1 parcela, titular padrão associado, transação e parcela salvas no DAO).
    2. Isolamento de emissor: compra Inter com final 1234 não associa a cartão Nubank de mesmo final (falha com `AMBIGUOUS_CARD`).
    3. Ambiguidade: dois cartões Inter com mesmo final 1234 falham com segurança (`AMBIGUOUS_CARD`).
    4. Titular padrão ausente: cartão Inter sem titular padrão é ignorado com segurança (`NO_HOLDER`).
    5. Deduplicação: compras repetidas dentro de 120s são rejeitadas como `DUPLICATE`.

## Resultados dos Gates de Validação (Executados em Série)
1. **Testes Unitários (`testDebugUnitTest`)**:
   - Status: **APROVADO (BUILD SUCCESSFUL)**
   - Cobertura: 172 testes executados, 0 falhas, 0 erros, 0 ignorados (100% PASS).
2. **Linting (`lintDebug`)**:
   - Status: **APROVADO (BUILD SUCCESSFUL in 1m 17s)**
   - 0 erros de linting.
3. **Compilação Debug (`assembleDebug`)**:
   - Status: **APROVADO (BUILD SUCCESSFUL in 12s)**
   - Artefato gerado: `app/build/outputs/apk/debug/app-debug.apk`
   - Tamanho: 18.483.502 bytes
   - SHA-256: `CFC7270AE9652361C739FE02FEF1B9211D7634811AA442AD3DF6CF8FBF7C92E5`
4. **Verificação Git Whitespace (`git diff --check`)**:
   - Status: **APROVADO (Exit code 0)**

## Estado no Dispositivo e Captura Real
- **Dispositivo USB:** Diagnosticado via `adb devices -l` e `mobile_diagnose`. O aparelho físico realme C71 (serial `007448c90e35c79b`) encontra-se desconectado no momento (nenhum dispositivo USB detectado).
- **Proteção do Banco de Dados:** Nenhuma transação sintética ou alteração de dados foi realizada em banco de produção.
- **Captura Real / PackageName:** O pacote `br.com.intermedium` consta na allowlist de pacotes suportados do Inter. No entanto, conforme exigido pelas regras de aceitação, a captura em tempo real **não é declarada como empiricamente confirmada** até que uma notificação push real do Banco Inter seja recebida no dispositivo conectado e seu `packageName` efetivo seja observado no logcat/StatusBarNotification.
