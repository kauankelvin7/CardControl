# CardControl — Ponto de Retomada e Estado Canônico

- Data da Última Auditoria/Atualização: 18 de Outubro de 2026, 01:50 -03
- Branch ativa: `main`
- HEAD SHA: `cc7a0fb` (base das alterações locais para v2.4.0)
- Versão Atual: `v2.4.0` (versionCode 6) — Vitrine & Leve Editorial Edition
- Versão do banco Room: `v5` (`MIGRATION_4_5`) com atomicidade @Transaction e proteção estrita contra deleção em cascata
- Estado do Repositório: Limpo e saneado (sem logs residuais, pastas redundantes de tooling removidas, APKs soltos excluídos)
- Gates Comprovados: 172/172 unit tests PASS (0 falhas, 0 erros), lintDebug PASS (0 erros), assembleDebug PASS, git diff --check PASS
- Status de Entrega: Pronto para push na `main` e publicação da release oficial `v2.4.0`.
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

7. **Design System Vitrine v2.1 (Claro: Petróleo & Cobre, Escuro: Grafite, Sobrescrita Global e Experiência de Cartões):**
   - Sobrescrita global do sistema cromático em todo o aplicativo sem dependência de cores dinâmicas do papel de parede Android.
   - Tema claro Petróleo & Cobre (`CcLightPetroleo`) e tema escuro Grafite (`CcGrafiteDark`) com semânticos e extras calibrados por contraste WCAG 2.1.
   - Tipografia M3 com fonte Inter e algarismos tabulares (`tnum`). Formas, espaçamento base 4dp e molas animadas (`CcMotion`).
   - Carrossel de cartões interativo tipo Google Wallet com `HorizontalPager`, peek equilibrado, z-index adaptativo e flip 3D no eixo Y (180° com contrarrotação e dados reais de limite, fechamento e vencimento).
   - Tingimento animado do terço superior do palco com proteção contra ofuscamento.
   - Resolução de paletas de cartões com 12 predefinições e algoritmo determinístico puro sRGB <-> CIELAB.
   - Suíte de 10 novos testes unitários cobrindo contrastes WCAG, invariância de cores, algoritmos e 250 sementes aleatórias em `VitrineThemeAndContrastTest.kt`.
   - Suíte geral de testes unitários expandida para 126 testes (100% PASS).

8. **Redesign Visual, UX e Vitrine (Google Wallet Style & Limpeza Global):**
   - Top Bar compacta e contextual por aba ("Faturas", "Cartões", "Automação"), eliminando a cápsula de cabeçalho "CardControl".
   - Tela Cartões: Palco contínuo suave (sem corte abrupto), cabeçalho contextual sem repetir banco/dígitos, menu ⋮ para editar/excluir, duas ações primárias equilibradas ("Lançar compra" e "Ver faturas"), e eliminação de comandos duplicados de cadastro (`SeeMoreAndAdd` e FAB redundante).
   - Tela Faturas: Resumo mensal em `surfaceContainer` com contorno sutil, barra de progresso horizontal de quitação da fatura, FAB "Nova compra" em cor primária com alto contraste, e estados vazios específicos e contextuais.
   - Tela Automação: Removidos todos os jargões internos ("Vitrine", "paleta sob medida", "imune a papel de parede", "Cofre Keystore"), agrupada em 3 seções compactas (Notificações, Aparência, E-mail) com formulário SMTP recolhível via `AnimatedVisibility`.
    - Suíte de 139 testes unitários aprovados (100% PASS) e novo APK de debug gerado com sucesso com a Carteira 3D, Análises Financeiras Offline e Nova Navegação (18.285.866 bytes, SHA-256 53E98029BB057C8B537F4386C780D71E9483992CA133DBECD1F328F07BF8D65A).

9. **Área de Análises Financeiras Offline e Gráficos Canvas Nativos (TASK-019):**
   - Motor `FinancialAnalyticsEngine.kt` 100% determinístico e local sobre entidades Room v5 (`InstallmentEntity`, `PaymentEntity`, `CreditCardEntity`, `HolderEntity`, `RecurringChargeEntity`).
   - Integridade contábil estrita: proibido `Double`/`Float` em valores monetários; utilização exclusiva de centavos inteiros (`Long`) e `BigDecimal` com `RoundingMode.HALF_UP`. Variações período-sobre-período expressas em basis points (`10000 = 100.00%`).
   - Gráficos nativos em Compose Canvas sem bibliotecas externas: `CcBarChart` (barras mensais com linha de base), `CcDonutChart` (distribuição por categoria), `CcModalityBar` (modalidade) e `CcMerchantRanking` (top 5 estabelecimentos).
   - Drilldown interativo com subtotal reconciliado em tempo real e filtros bidirecionais sincronizados.
   - Suíte de 6 testes matemáticos rigorosos em `FinancialAnalyticsTest.kt` (100% PASS).

10. **Nova IA de Navegação, Configurações na TopBar e Adaptive Icon Vetorial (TASK-020):**
    - BottomBar atualizada para 3 abas essenciais: "Faturas" (0), "Cartões" (1), "Análises" (2).
    - Ícone de engrenagem na TopBar abrindo tela dedicada de Configurações (`SettingsScreenContent`) com seta de retorno e `BackHandler` de sistema.
    - Preservação integral de todas as funções de automação: escuta de notificações bancárias, alternância de tema claro/escuro (`AppearanceSection`), credenciais SMTP Google e gestão rápida de pessoas.
    - Sincronização do filtro de cartão entre Cartões, Faturas e Análises.
    - Novo Adaptive Icon original em `ic_launcher_background.xml` (Petróleo Vitrine `#00383B` / `#004E52` com iluminação ambiente) e `ic_launcher_foreground.xml` (duplo cartão 3D em perspectiva, chip EMV Cobre `#F2A365`/`#C76A2B`, ondas NFC e barras de crescimento financeiro rigorosamente dentro do safe-zone de 66dp).

---

## 2. Evidências Empíricas dos Gates de Engenharia e Checkpoint Físico (18:04:06)

- **Checkpoint Verificado em Dispositivo Físico Real (realme C71 / RMX5303, serial `007448c90e35c79b`):**
  - **APK Instalado:** `app/build/outputs/apk/debug/app-debug.apk` (18.302.555 bytes, mtime 17:59:30).
  - **SHA-256 do APK:** `aeb46c26cc3f838adaf94ed4e636f85c44219677129ac020c61eaaa776bc891f`.
  - **Instalação:** `adb install -r` executado às 18:04:06 (versão 2.3.0, app debuggable, dados do usuário preservados).
  - **Suíte de Testes Unitários:** XMLs das 17:58:36 reportam **145 testes, 0 falhas, 0 erros (100% PASS)**.
  - **Linting & Compilação:** `lintDebug` HTML mtime 18:01:00; `assembleDebug` mtime 17:59:30; `git diff --check` PASS (zero erros de whitespace).
  - **Preservação e Integridade de Dados:** Nenhuma compra ou pagamento real foi submetido na base viva do usuário (persistência coberta por testes unitários/integração em memória/fake). Backup original protegido em `scratch/db_backup/`.
  - **Matriz de Testes Reais no Hardware (Resultados Comprovados):**
    - `[PASS]` Aba Faturas -> próximo mês exibe fatura aberta de Nov/2026 com total de R$ 300,00 e itens detalhados.
    - `[PASS]` Cartões -> 'Ver faturas' navega corretamente para a aba Faturas.
    - `[PASS]` Após inicialização do novo build, Faturas abre diretamente no ciclo aberto de Nov/2026.
    - `[PASS]` Registros duplicados de titular foram saneados para um único titular Titular Principal.
    - `[PASS]` Diálogo '+ Compra' tem como destino 11/2026 (dia de fechamento 9) e exibe apenas uma opção de titular.
    - `[PASS]` Submissão vazia exibe validação visível: *"Informe o estabelecimento e um valor maior que R$ 0,00."* mantendo o diálogo aberto.
    - `[PASS]` Botão 'Cancelar' fecha o diálogo de compra.
    - `[PASS]` Botão 'Abater' abre diálogo 'Registrar Abatimento / Pix' e 'Cancelar' fecha sem registrar pagamento.
    - `[PASS]` Engrenagem abre Configurações, alternância Claro/Escuro funciona perfeitamente e botão Voltar retorna para Análises.
  - **Falhas / Quebras Visuais Observadas no Hardware (Escopo Ativo do STEP B - Codex):**
    - `[FAIL/OPEN]` Faturas: valor de titular R$ 300,00 quebra verticalmente para "R$ 300,0" + "0".
    - `[FAIL/OPEN]` Cartões: botão CTA trunca "Lançar compra" para "Lançar com...".
    - `[FAIL/OPEN]` Cartões: linha CardMetrics colide e trunca "Comprometido: R$300,00Total: R$4.8…".
    - `[FAIL/OPEN]` Análises: badge de 6% sobrepõe "CAPACIDADE DA CARTEIRA", quebra palavra e exibe plural "1 cartão(ões)".
    - `[FAIL/OPEN]` Configurações: descritores "Titulares e dependentes…" e "Comprovantes opcionais via Gm…" truncam.
  - **Evidências Visuais Locais (TEMP do PC autorizado - sem inclusão no Git):**
    `cc-latest-build-settled.png`, `cc-invoices-next-month.png`, `cc-new-expense-dialog.png`, `cc-new-expense-validation.png`, `cc-cards-tab.png`, `cc-analytics-current.png`, `cc-settings-current.png`, `cc-settings-light.png`.
  - **Skills de Engenharia Instaladas no Repositório:** `mobile-design`, `ux-audit`, `frontend-design-review`, `frontend-design` em `.agents/skills`; Codex global `mobile-design`/`ux-audit` (registradas para padronização; sem alegação de terem sido testadas em fontes).
  - **Status das Tarefas Ativas:**
    - `TASK-021`: code/gates checkpoint completed, E2E purchase success on live DB intentionally not attempted to protect data.
    - `TASK-022`: IN_PROGRESS — ajustes visuais do Step B implementados e capturas revisadas; validação de fatura de R$300 e diálogo de compra pendentes porque o estado visível no aparelho está sem compras/titulares; Flash read-only pendente.
    - `TASK-023`: IN_PROGRESS — análise passa a contar `TransactionEntity` por `purchaseTimestamp`, testes e gates concluídos; Flash read-only pendente.


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

## Checkpoint de Análises e UI — 17/10/2026 19:12 -03

- Branch/HEAD: `main` / `9e38e72abe8286c08a6e198a44d14cd6271e56bb`; working tree preservado, sem stage/commit/push.
- Analytics: fonte de gasto bruto, contagem, ticket, categoria, modalidade, participação por cartão, ranking e tendência é `TransactionEntity`; período é filtrado por `purchaseTimestamp` no fuso local `[início, próximo mês)`. Parcelas seguem como fonte das faturas e pagamentos permanecem em KPI separado. Sem migração Room.
- Provas finais sequenciais: `testDebugUnitTest --rerun-tasks` PASS (155 testes; zero falhas/erros/skips; 19:04:38–19:06:54); `lintDebug` PASS (19:07:51–19:08:53); `assembleDebug` PASS (19:09:02–19:09:15); `git diff --check` PASS.
- APK `app/build/outputs/apk/debug/app-debug.apk`: 18.306.733 bytes, SHA-256 `0D9188465C43B12CAB175B2B3640D4080DDA8E3F1DFC65BA33431BDBB82981ED`; instalado no RMX5303 por `adb install -r`, preservando dados do app.
- Capturas do checkpoint inicial mostraram estado vazio. Follow-up posterior, após reinstalar o mesmo APK, exibiu titular e fatura Nov/2026 R$300,00; Análises Mês atual/3 meses mostraram R$300, ticket R$300 e uma compra. Confirmar formulário vazio mostrou validação, Cancelar fechou o diálogo e a fatura permaneceu R$300/pago R$0. Nenhuma compra/pagamento foi submetido nem banco acessado diretamente.
- Capturas atualizadas: `%TEMP%\cardcontrol-final-shots` (Faturas, Cartões, métricas, Análises atual/3 meses, Configurações, diálogo/validação/cancelamento e fatura pós-cancelamento). O helper secundário do cartão “Melhor compra apó...” permanece truncado como P2 visual.
- Auditoria Flash somente leitura concluída e aprovada; ver parecer abaixo.

## Resultado da auditoria Flash — 17/10/2026 19:26 -03

- Parecer read-only para TASK-022/023: **APROVADO, P0=0, P1=0, sem bloqueadores**. Revisor confirmou agregação por compra, filtros e limites locais, pagamentos separados, copy/callbacks visuais e gates de build/testes.
- À época da revisão, o aparelho parecia vazio pelas capturas então disponíveis. A validação física posterior confirmou R$300 e o fluxo vazio/Cancelar sem alteração. Não há bloqueio arquitetural que justifique acionar Argon.

## Follow-up de build e dispositivo — 17/10/2026 19:27–19:30 -03

- `assembleDebug` executado isoladamente com JDK configurado; `BUILD SUCCESSFUL`, exit code 0, 13 s. Log: `%TEMP%\cardcontrol-assembleDebug-followup-20261017.log`. A tentativa anterior com caminho JDK 17 inexistente encerrou antes de iniciar Gradle.
- `git diff --check` exit 0; APK SHA-256 `0D9188465C43B12CAB175B2B3640D4080DDA8E3F1DFC65BA33431BDBB82981ED`; instalado com `adb install -r` no RMX5303, sem limpar dados.
- Sem ações financeiras persistidas. Validação vazia e cancelamento confirmados por capturas; fatura posterior continua R$300,00 e pago R$0,00.

## Investigação da discrepância de capturas — 17/10/2026 19:35 -03

- `home.png` e `faturas-current.png` têm `LastWriteTime` 18:59:45 e 19:00:06, anteriores ao `lastUpdateTime` 19:27:48 do pacote instalado conferido. Aplicação instalada: `com.cardcontrol.app`, versionCode 5/versionName 2.3.0; usuário ativo 0; `firstInstallTime` permaneceu 12:19:58. `adb install -r` retornou `Success`; APK instalado foi puxado do caminho de código e tem SHA-256 idêntico ao local `0D9188465C43B12CAB175B2B3640D4080DDA8E3F1DFC65BA33431BDBB82981ED`.
- Room v5 abre `card_control_offline.db`, com migrações 2→3, 3→4 e 4→5 explícitas e sem fallback destrutivo. Não houve mudança de schema. Nenhum dado/arquivo do banco foi acessado. Logcat filtrado não mostrou erro de migração, SQLite ou crash; não há log persistente separado de instalação, apenas a saída do comando (`Success`).
- Em cold launch, 1 s mostrou splash; aos 3 s a UI mostra registro chamado `teste`, fatura Nov/2026 R$300,00 em uma parcela e R$0,00 pago. Análises Mês atual/3 meses mostram esse estado como gasto/ticket R$300 e 1 compra; isso não é o dado Step A esperado de Titular Principal em três parcelas e não prova o KPI esperado no aparelho.
- As capturas citadas pelo revisor (`home.png`, `faturas-current.png`, `analytics-current.png`, `analytics-3-months.png`, `purchase-dialog.png`) foram gravadas entre 18:59:45 e 19:01:21, antes de `lastUpdateTime=19:27:48` do pacote atual. As imagens analytics mostram vazio e a de `purchase-dialog.png` é a tela simples de Cartões. Mantidas como estavam. Capturas novas com prefixo `audit-` documentam estado atual e modal real.
- Modal foi aberto explicitamente; Confirmar vazio manteve modal aberto com mensagem de validação; Cancelar fechou; captura posterior manteve R$300 e R$0 pago. Nenhuma compra/pagamento foi submetido.
- **Bloqueio de evidência:** identidade do titular e fatura de três parcelas esperados não são visíveis. Não é possível afirmar preservação integral, perda ou recuperação do registro Titular Principal sem ler o banco ou uma fonte de backup confiável. Banco não acessado; não inventar/recriar dados. Causa do estado vazio nas imagens antigas não determinada; atraso de carregamento é possível, mas não provado. Tarefas de UX/validação permanecem abertas; solicitar revisão Flash read-only após fim das alterações.
- `run-as com.cardcontrol.app sqlite3 -version` retornou `No such file or directory`; não há cliente SQLite no aparelho. Não puxei banco/WAL/SHM para o host. Preservação e identidade de Titular Principal/3x permanecem sem confirmação direta.

## Fechamento da revisão Flash e estado de evidência — 17/10/2026

- Flash read-only concluiu com **APROVADO COM RESSALVAS**, P0=0: P1 é a ausência do dataset físico esperado para verificar analytics em 3 parcelas; P2 é a hipótese de janela visual de cold-start. Não adotar a afirmação do revisor de que o banco contém especificamente `teste` ou de que não houve perda: não foi feita consulta ao banco, e a UI não prova identidade nem preservação integral.
- Evidência segura disponível: UI após carregamento exibe `teste`, fatura Nov/2026 R$300,00 em uma parcela e pago R$0,00. O cenário Titular Principal/R$300 em 3 parcelas e a preservação do estado anterior permanecem **indeterminados**; `run-as ... sqlite3` não encontrou executável. Nenhuma cópia do banco/WAL/SHM foi feita, nenhuma escrita financeira foi executada.
- Modal real aberto; submissão vazia manteve o modal e mostrou validação; Cancelar fechou. Captura posterior manteve R$300,00/pago R$0,00.
- Capturas vazias de 18:59–19:01 precedem `lastUpdateTime` 19:27:48. Isso não determina a causa do conteúdo vazio. Splash a 1 s e tela carregada a 3 s tornam atraso plausível, não comprovado.
- Nesta checagem não apareceu cliente Gradle de build/teste; há um processo residente `GradleDaemon 8.7` (PID 15748), cuja ociosidade não foi inspecionada nem alterada. Não afirmar “nenhum processo Gradle ativo”.
- TASK-022/023 permanecem `IN_PROGRESS` pela ressalva de validação física/dataset. Gates de código previamente registrados (155 testes, lintDebug, assembleDebug, diff-check) valem para o SHA citado, mas não substituem validação com os dados esperados. Nenhuma alteração de código nesta retomada.

## Follow-up ADB do modal e inspeção de causa — 17/10/2026 19:43 -03

- RMX5303 (`007448c90e35c79b`) conectado, resolução 720×1604. Toque aplicado ao pai clicável em (360,938); captura `%TEMP%\cardcontrol-final-shots\followup-purchase-dialog.png` confirma o modal real. Toque em Confirmar vazio produziu `%TEMP%\cardcontrol-final-shots\followup-purchase-validation.png`, com erro visível e modal aberto; Cancelar produziu `followup-purchase-cancel.png`, modal fechado. Captura `followup-invoices-after-cancel.png` mostra Nov/2026 R$300,00, pago R$0,00, uma pessoa `teste` e uma parcela. Nenhuma compra/pagamento persistido.
- Pacote permanece `com.cardcontrol.app`, versionCode 5/versionName 2.3.0; firstInstallTime 12:19:58, lastUpdateTime 19:27:48. Isso é compatível com atualização do mesmo pacote; não prova conteúdo/preservação completa da base. Código Room está em v5, registra migrações explícitas 2→3→4→5 e não usa fallback destrutivo; não há mudança de schema registrada para este APK. MainViewModel executa `sanitizeDuplicateHolders`; essa rotina só mescla titulares de mesmo nome normalizado. Não encontrei evidência de seed de titular em runtime (Titular Principal aparece apenas em HomeScreenV3/previews). Estas observações não determinam por que o estado Step A difere.
- Sem leitura/cópia da base, continuam indistinguíveis: titular renomeado, outro estado persistido ou divergência anterior à atualização. A UI prova somente o estado que apresenta; a causa de ausência de Titular Principal e de duas parcelas continua **não determinada**. Logs atuais filtrados não forneceram evento Room/SQLite/instalação que explique a diferença.
- Follow-up solicitado: o pacote tem `dataDir=/data/user/0/com.cardcontrol.app`; usuário ativo é 0. `pm list packages --user 10` também lista o pacote, mas `pm list users` não lista o usuário 10 e o registro do pacote para user 10 tem `ceDataInode=0`, `deDataInode=0`, `stopped=true`, `notLaunched=true`; a UI/ADB da sessão permanece no user 0. Em `run-as ... ls -l databases` às 19:43, DB principal tinha 114.688 bytes (mtime 19:13), WAL 440.872 bytes (mtime 19:13), SHM 32.768 bytes (mtime 19:34). Isso confirma presença de arquivos, não o conteúdo nem a integridade lógica. O snapshot anterior fornecido pelo usuário (main 4 KB/WAL 391 KB; package lastUpdateTime 18:59:35) diverge da consulta direta posterior (lastUpdateTime 19:27:48); cronologia/metadados não demonstram perda causada por instalação.
- Caminho plausível, sem prova de ocorrência: `deleteHolder` confirmado apaga a pessoa; as FKs de transações, parcelas e pagamentos usam CASCADE, enquanto o cartão desvincula defaultHolder via SET_NULL. Assim, a exclusão confirmada poderia remover histórico e manter o cartão. Não há trilha de auditoria local para provar se isso ocorreu. O fluxo de leitura não filtra titulares únicos; `DashboardUiState()` inicial vazio pode aparecer antes das emissões Room, mas isso também não explica comprovadamente a diferença persistente.
- Modal revalidado às 19:44–19:45 com tap (360,938): `recheck-purchase-modal.png`, `recheck-purchase-validation.png`, `recheck-purchase-cancel.png`, `recheck-invoice-after-cancel.png`. Vazio exibiu erro; Cancelar fechou; fatura manteve R$300,00/pago R$0,00. Nenhuma operação financeira submetida.


## Snapshot de retomada — 17/10/2026 22:35 -03

- Pedido mais recente: usar o Leve local como referência real de design do CardControl. Plano e fontes examinadas: `.agents/LEVE_DESIGN_REFERENCE.md`.
- Estado Git verificado antes da atualização de governança: branch `main`, HEAD `9e38e72abe8286c08a6e198a44d14cd6271e56bb`; 32 arquivos rastreados modificados e arquivos não rastreados já existiam. Os diffs locais estão preservados e não podem ser descartados. O terminal Gemini - Continuidade exibia trabalho em `AnalyticsScreen.kt`; concluir e registrar handoff antes de iniciar outra escrita.
- As alegações antigas de 155 testes/lint/build e os hashes de APK referem-se aos respectivos snapshots registrados, não provam o estado atual do working tree modificado. Reexecutar gates apenas após a consolidação final do código.
- Leve: fontes verificadas foram `DESIGN.md`, `design-tokens.json`, CSS editorial/glass/base, `platform/theme.ts` e `packages/domain/src/themes.ts`. O JSON de tokens diverge do runtime CSS/domínio em alguns valores; a fonte de verdade precisa ser reconciliada antes de portar cores. Nenhum segredo ou `.env.local` foi lido.
- Orquestração identificada: Design Review e Data/Room aguardavam escopo/decisão. A partir do pedido de autonomia, Maestro toma decisões técnicas; revisão visual começa read-only, correções de dados ficam em onda separada e segura, e implementação visual aguarda handoff do escritor atual. Sem commit/push/merge ou mutação de banco real.


## Reavaliação do parecer de design — 17/10/2026 22:37 -03

- Relatório do especialista: `.agents/LEVE_CARDCONTROL_AUDIT.md`; contraponto crítico: `.agents/LEVE_DESIGN_AUDIT_CRITIQUE.md`.
- Alguns achados da auditoria são já implementados no estado local (dock 32dp em MainScreen, tokens CcShapes 18/14dp, gradiente/cantos em CcCharts, rótulo OUTROS “Outros”). O Maestro deve deduplicar antes de atribuir trabalho; P0 visual exige reprodução objetiva. Haptics TextHandleMove e BlendMode.Plus continuam opcionais/especulativos.
- Status TASK-025 permanece IN_PROGRESS. Nenhum código da aplicação mudou por esta revisão; capturas, testes e gates atuais ainda precisam ser verificados após a implementação consolidada.


## Addendum: TASK-025 audit crítica e riscos funcionais — 17/10/2026 22:40 -03

- O parecer de design inicial está em `.agents/LEVE_CARDCONTROL_AUDIT.md`. Há falsos positivos contra o código atual: dock de 32dp já existe em `MainScreen.kt`; tokens `CcShapes.panel/card` 18/14dp existem; `CcCharts.kt` já possui gradiente vertical e cantos arredondados; a categoria já está nomeada `OUTROS("Outros")`. Consulte `.agents/LEVE_DESIGN_AUDIT_CRITIQUE.md` antes de implementar. Classificação P0 requer reprodução/evidência; haptics TextHandleMove e BlendMode.Plus são propostas opcionais e com risco, não aplicar cegamente.
- TASK-025 continua em progresso read-only/design. O revisor recebeu nova instrução de ampliar auditoria de tipografia, todas as telas e estados, font scale, acessibilidade e evidência; não editar código. O Maestro está processando a crítica e a decisão de handoff.
- TASK-026 PENDING: proteger exclusão de titular com histórico por política segura no DAO/Room (bloquear hard-delete se houver transações/parcelas/pagamentos; UI clara), teste transacional/atomicidade e manter exclusão sem histórico. TASK-027 PENDING: corrigir período/flicker no cold start por fonte temporal determinística, sem sleeps. Ambos aguardam handoff do escritor atual e devem ser implementados em sequência com um único escritor; nunca tocar o banco real.
- Um APK debug foi recompilado e instalado por processo Gemini preexistente por volta de 22:39 (18.483.539 bytes); isso não é validação final do redesign Leve nem prova que os novos gates estão todos verdes. O processo de agente mostrou lançamento da MainActivity e continuou ativo. Não iniciar outro Gradle/ADB enquanto esse fluxo estiver em andamento.
- Git continua em `main`, HEAD anterior `9e38e72...`, sem commit/push do trabalho local. `git status --short` contabilizou 51 entradas locais; os diffs preexistentes e os novos documentos de governança devem ser preservados.
