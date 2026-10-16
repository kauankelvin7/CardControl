# CardControl — Ponto de Retomada e Estado Canônico

- Data da Última Auditoria: 16 de Outubro de 2026
- Branch ativa: `main` (Sincronizada com `origin/main` no commit `9290989`)
- HEAD SHA: `9290989`
- Versão Atual: `v2.3.0` (versionCode 5) — Liquid Glass Edition
- Versão do banco Room: `v5` (`MIGRATION_4_5`)
- Release no GitHub: Atualizada em `v2.3.0` com novo APK `CardControl-v2.3.0.apk` assinado e verificado.
  - APK SHA-256: `a3cadd0bca5005d91f31dbe85f7d24171819f5052756a77f6679c8233a6503e5`
  - Assinatura: Esquema v2 com certificado oficial `A5:24:7A:59:4D:30:C8:FA:F4:6C:8B:30:06:AB:09:27:35:CC:DE:FD:67:81:1B:41:96:5E:01:87:28:64:93:28`
- Feature Integrada: UI/UX & Design System Overhaul — Liquid Glass Edition (Color tokens, Tabular Figures `tnum`, CurrencyText, OverlineText, TopBar contextual, BottomBar estilizada, FAB unificado e resolução de bugs de corte de inputs)

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

---

## 2. EvidÃªncias EmpÃ­ricas dos Gates de Engenharia

- **Testes UnitÃ¡rios:** 82 testes executados, 82 aprovados, 0 falhas, 0 erros, 0 ignorados (`BUILD SUCCESSFUL`).
- **ValidaÃ§Ã£o em SQLite Real (`scripts/test_room_v3_to_v4_real_sqlite.py`):** 100% aprovado com tratamento de bancos ambÃ­guos.
- **CanÃ¡rios de Empacotamento Seguro (`scripts/test_bundle_canary.py`):** 28 arquivos-canÃ¡rio sensÃ­veis bloqueados com sucesso.
- **CompilaÃ§Ã£o e Linting:** `lintDebug` aprovado com 0 erros, `assembleDebug` gerado com sucesso.
- **Integridade Git:** `git diff --check` aprovado sem inconsistÃªncias.
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