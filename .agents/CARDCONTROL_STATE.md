# CardControl — Ponto de Retomada e Estado Canônico

- Data da Última Auditoria: 17 de Outubro de 2026
- Branch ativa: `main` (Sincronizada em `15b1e70`)
- HEAD SHA: `15b1e70`
- Versão Atual: `v2.3.0` (versionCode 5) — Liquid Glass Edition
- Versão do banco Room: `v5` (`MIGRATION_4_5`)
- Release no GitHub: Atualizada em `v2.3.0` com novo APK `CardControl-v2.3.0.apk` assinado e verificado.
  - APK SHA-256: `5c396a933ef1e29869084716ad478c05bca7ef9dc9f0070e2fb5f69139aebaeb`
  - Assinatura: Esquema v2 com certificado oficial `A5:24:7A:59:4D:30:C8:FA:F4:6C:8B:30:06:AB:09:27:35:CC:DE:FD:67:81:1B:41:96:5E:01:87:28:64:93:28`
- Feature Integrada: Expansão do Motor de Notificações Bancárias (Gastos de Cartão, Aproximação NFC, Carteiras Digitais, Pix Enviado/Recebido, TED e Boletos)

---

## 1. Resumo Executivo das Entregas Validadas

1. **Harmonização de Bancos e Parser (PR #7 integrado via commit 7ff84ba):**
   - Unificação de parsers em `BankNotificationParser.kt` com suporte aos maiores bancos do Brasil.
   - `BankNotificationProcessor.kt` conectando captura, resolução segura, concorrência e deduplicação de 120s.
   - Enum canônico `SupportedBank` com sanitização, normalização sem acentos, tokenização e tratamento rigoroso de ambiguidade.
   - Migração Room v3 -> v4 com backfill inteligente retroativo e validação em SQLite real (100% PASS).
   - Suíte de 74 testes aprovada com louvor no CI do GitHub Actions (`Android CI`, `CodeQL`, `Dependency Review`).

2. **Gerenciamento Completo de Cartões e UX Liquid Glass (PR #13 integrado via commit 65d46a7):**
   - Edição e exclusão segura de cartões com diálogos modais dedicados e validação estrita.
   - Bloqueio de exclusão quando há histórico financeiro (transações, parcelas ou pagamentos).
   - Vinculação de pessoa/titular padrão e seletor visual de bancos no modal.
   - Suíte consolidada de 82 testes unitários aprovados com 100% de sucesso.

3. **Design System Overhaul e Release v2.3.0 Liquid Glass Edition:**
   - Overhaul estético padrão fintech premium (Linear / Raycast) com fundo `#060913`, vidro translúcido `#0F172A` (65%) e tipografia tabular com `CurrencyText`.
   - Compilação e assinatura local com o par de chaves oficial persistente (`A5:24:7A:59:4D:30:C8:FA:F4:6C:8B:30:06:AB:09:27:35:CC:DE:FD:67:81:1B:41:96:5E:01:87:28:64:93:28`).
   - Release `v2.3.0` no GitHub atualizada com o novo APK `CardControl-v2.3.0.apk` e o novo `SHA256SUMS.txt`.
   - Branch `main` e tag `v2.3.0` sincronizadas no repositório remoto.

4. **Saneamento e Limpeza Estrita de Binários:**
   - Remoção de binários compilados fora do canal oficial de Releases do GitHub.
   - Atualização de `dist/README.md` e regras de integridade do `.gitignore`.

5. **Clean Code, Clean Architecture, Comentários Naturais e Compose Previews:**
   - Modularização da interface Jetpack Compose: extração de diálogos modais (`ui/dialogs/`) e componentes reutilizáveis (`ui/components/`), enxugando `MainScreen.kt` em mais de 600 linhas líquidas.
   - Suporte completo a `@Preview` com mock data e tooling preview integrado em `app/build.gradle.kts` e `LiquidGlass.kt`.
   - Desacoplamento de `MainViewModel` com injeção de dependências no construtor para testabilidade sem dependência de singleton do SQLite.
   - Unificação do pipeline de notificações em `CardRepository.processBankNotification` delegando para `BankNotificationProcessor` canônico com trava atômica.
   - Eliminação de redundâncias em `BankNotificationParser.issuerFromCardName` com delegação direta a `SupportedBank.inferFromCardName`.
   - Comentários naturais e explicativos em português brasileiro documentando decisões, concorrência e regras contábeis.

6. **Expansão do Motor de Notificações Bancárias (Gastos, Aproximação, Pix, TED e Boletos):**
   - Removidos falsos positivos de rejeição (`pix`, `transferencia`, `boleto`) de `IGNORED_CONTEXT_REGEX`, preservando bloqueio seguro de tokens, senhas, faturas e estornos.
   - Suporte a compras com cartões por aproximação NFC ("por aproximação", "contactless"), carteiras digitais (Apple Pay / Samsung Pay) e confirmações ("Você pagou", "Transação realizada").
   - Detecção especializada de Pix Enviado (`PIX_SENT`), Pix Recebido (`PIX_RECEIVED`), Transferências bancárias (`TRANSFER_SENT`) e Boletos (`BILL_PAID`).
   - Extração inteligente de contrapartes (destinatário ou remetente) com lookahead para pontuações e sanitização de sufixos de cartão.
   - Deduplicação SHA-256 aprimorada incorporando a categoria da notificação para prevenir colisões entre operações distintas de mesmo valor.
   - 19 novos testes unitários adicionados no `BankNotificationParserTest`, elevando a suíte para 52 testes específicos do parser e 116 testes no total geral do projeto.

---

## 2. Evidências Empíricas dos Gates de Engenharia

- **Testes Unitários:** 116 testes executados, 116 aprovados, 0 falhas, 0 erros, 0 ignorados (`BUILD SUCCESSFUL`).
- **Validação em SQLite Real (`scripts/test_room_v3_to_v4_real_sqlite.py`):** 100% aprovado com tratamento de bancos ambíguos.
- **Canários de Empacotamento Seguro (`scripts/test_bundle_canary.py`):** 28 arquivos-canário sensíveis bloqueados com sucesso.
- **Compilação e Linting:** `assembleDebug` gerado com sucesso (APK 16,46 MB em `app/build/outputs/apk/debug/app-debug.apk`).
- **Integridade Git:** `git diff --check` aprovado sem inconsistências.
- **ConfiguraÃ§Ã£o Remota:** GitHub CLI confirmou os 4 secrets e as 2 variables no repositÃ³rio `kauankelvin7/CardControl`.

---

## 3. InventÃ¡rio de ConfiguraÃ§Ã£o Remota

| Item | Tipo | Valor / Status |
| :--- | :--- | :--- |
| `CARDCONTROL_KEYSTORE_BASE64` | Secret | Confirmado via GitHub API |
| `CARDCONTROL_KEYSTORE_PASSWORD` | Secret | Confirmado via GitHub API |
| `CARDCONTROL_KEY_ALIAS` | Secret | Confirmado via GitHub API |
| `CARDCONTROL_KEY_PASSWORD` | Secret | Confirmado via GitHub API |
| `CARDCONTROL_CERT_SHA256` | Variable | `A5:24:7A:59:4D:30:C8:FA:F4:6C:8B:30:06:AB:09:27:35:CC:DE:FD:67:81:1B:41:96:5E:01:87:28:64:93:28` |
| `CARDCONTROL_RELEASE_COMPATIBILITY_APPROVED` | Variable | `true` |