# Plano de Execução e Arquitetura Global do CardControl (ExecPlan)

## 1. Objetivo Global

Fornecer uma experiência fintech 100% offline-first de padrão Google Wallet / Vitrine Petróleo & Cobre (Claro) e Grafite (Escuro) com:
1. **Carteira de Cartões 3D Personalizada (`TASK-018`)**: Modelos visuais originais por banco (`SupportedBank`), chip EMV metálico, ondas Contactless em `Canvas`, proporção ISO/IEC 7810 ID-1 (`1.586:1`), flip 3D no eixo Y e seleção sincronizada com faturas e lançamentos.
2. **Área de Análises e Inteligência Financeira Offline (`TASK-019`)**: Motor determinístico de agregação em centavos (`Long` + `BigDecimal` `RoundingMode.HALF_UP`, zero `Double`/`Float` para somas monetárias), gráficos interativos em `Canvas` nativo (`CcCharts.kt`), seletor de período, KPIs reconciliáveis, categorias inferidas com `NAO_CATEGORIZADO` explícito, modalidade (`RECORRENTE` / `PARCELADO` / `A_VISTA`), distribuição por cartão/titular, maiores estabelecimentos e drilldown clicável.
3. **Arquitetura de Informação, Configurações na Engrenagem e Marca Vetorial (`TASK-020`)**: Destinos primários da barra inferior (`Faturas`, `Cartões`, `Análises`), tela dedicada de `Configurações` acessível pelo ícone de engrenagem na TopBar (agrupando Notificações de compras, Aparência e Alertas de e-mail/SMTP com `BackHandler` limpo), auditoria de conexões ponta a ponta e novo logotipo vetorial / Adaptive Icon original (`ic_launcher_background.xml` e `ic_launcher_foreground.xml`).

---

## 2. Contexto Atual, Restrições e Avaliação de Bibliotecas (17/10/2026)

- **Linguagem / SDK:** Kotlin `1.9.24`, Compose Compiler `1.5.14`, Compose BOM `2024.06.00`, `compileSdk = 34`, `minSdk = 26`, Java 17, Room `2.6.1` SQLite (`v5`, `MIGRATION_4_5`).
- **Dispensa de Hardware USB / Emulador:** O usuário validará manualmente o APK final diretamente em seu smartphone. Os agentes validam compilação, testes unitários (`testDebugUnitTest --rerun-tasks`), `lintDebug`, `assembleDebug`, `git diff --check` e canários, registrando validação visual manual em aparelho como etapa do usuário (não bloqueante para entrega e push).

### 2.1 Comparativo Técnico de Componentes de Cartão e Gráficos

| Candidato / Repositório | Licença | Versão / Requisitos Inspecionados | Decisão Arquitetural e Justificativa |
| :--- | :--- | :--- | :--- |
| **Pedro-kt/compose-components** (`github.com/Pedro-kt/compose-components`) | MIT | App-portfólio (`minSdk = 24`, `agp = 9.0.0`, `kotlin = 2.2.10`); não publicado no Maven Central. | **Estudo Seletivo / Adaptação Nativa sob MIT:** Conceitos de EMV chip, ondas Contactless e pilha de cartões adaptados em código 100% nativo (`CardElements.kt`, `CardVisuals.kt`, `CardFace.kt`) compatível com Kotlin 1.9.24 e API 26+. |
| **makzimi/SwipingCards** (`github.com/makzimi/SwipingCards`) | MIT | Release `v0.1.0` exige `minSdk = 33` (README) e compila com `kotlin = 2.2.0` / `coreKtx = 1.18.0`. | **Rejeitado como Dependência Binária:** Exigiria elevar `minSdk` de 26 para 33 e causaria incompatibilidade de metadata Kotlin 2.2 vs 1.9.24. |
| **wajahatkarim3/Flippable** (`github.com/wajahatkarim3/Flippable`) | Apache-2.0 | Versão `1.5.4` (2023, Compose 1.x legado). | **Substituído por Nativo (`FlippableCard.kt`):** O componente próprio já implementa rotação 3D no eixo Y (`cameraDistance = 16f * density`), contrarrotação de 180° e fallback `Crossfade` para `LocalCcReduceMotion`. |
| **patrykandpatrick/vico** (`github.com/patrykandpatrick/vico`) vs **Compose Canvas Nativo (`CcCharts.kt`)** | Apache-2.0 (Vico) / Nativo | Guia oficial Vico declara `minSdk >= 23` e módulos `compose-m3` (release recente `v3.3.1` / `2.x`), porém releases `2.x/3.x` compilam contra Kotlin `2.0+` e não incluem gráfico Donut/Rosca nativo para fatias de categoria/cartão. | **Escolha: Compose `Canvas` Nativo (`ui/analytics/CcCharts.kt`)**: Garante compatibilidade zero-atrito com Kotlin `1.9.24`, zero aumento de APK, suporte nativo tanto a gráfico de barras interativo (Tendência Mensal) quanto a gráfico Donut/barras horizontais (Categorias/Modalidades/Cartões), hit-testing de toque (`pointerInput`), semântica TalkBack e respeito a `LocalCcReduceMotion`. |

---

## 3. Arquitetura de Dados e Motor Financeiro (`TASK-019`)

### 3.1 Fontes Reais no Room v5 (Sem Migração Desnecessária)
- `TransactionEntity`: `id`, `cardId`, `holderId`, `merchant`, `totalAmount: Long` (centavos), `installmentsCount`, `purchaseTimestamp`, `isRecurring`. Fonte canônica de compras para Análises.
- `InstallmentEntity`: `id`, `transactionId`, `holderId`, `cardId`, `merchantSnapshot`, `installmentNumber`, `totalInstallments`, `amount: Long` (centavos), `refMonth: Int`, `refYear: Int`, `purchaseTimestamp: Long`.
- `PaymentEntity`: `id`, `holderId`, `cardId: Long?`, `amountPaid: Long` (centavos), `refMonth: Int`, `refYear: Int`, `paymentTimestamp: Long`, `notes: String`.
- `CreditCardEntity`: `id`, `name`, `bank: String`, `lastFourDigits`, `totalLimit: Long`, `closingDay: Int`, `dueDay: Int`, `defaultHolderId: Long?`.
- `HolderEntity`: `id`, `name`, `whatsapp`, `email`, `subLimit: Long`, `notifyByEmailOnPurchase: Boolean`.
- `RecurringChargeEntity`: `id`, `cardId`, `holderId`, `merchant`, `expectedAmount: Long`, `lastAmount: Long`, `previousAmount: Long?`, `billingCycleDays: Int`, `lastChargedTimestamp: Long`, `isActive: Boolean`, `isUserConfirmed: Boolean`.

### 3.2 Regras Matemáticas e Contábeis Estritas (`FinancialAnalyticsEngine.kt`)
1. **Precisão Financeira:** Valores monetários ficam em `Long` centavos; médias e percentuais/basis points usam `BigDecimal` + `RoundingMode.HALF_UP`. `Float` é reservado à geometria dos gráficos.
2. **Janelas de Período (`AnalyticsPeriod`) por data da compra:** Filtros usam `purchaseTimestamp` convertido no fuso local e intervalos `[início do primeiro mês, início do mês seguinte)`. `LAST_3_MONTHS` inclui o mês de referência e os dois anteriores.
3. **Compra, parcela e pagamento têm papéis separados:** `TransactionEntity.totalAmount` e cada transação contam uma vez para gasto bruto, quantidade e ticket médio. `InstallmentEntity` continua sendo a fonte das faturas. Pagamentos ficam em métricas próprias e não reduzem o gasto bruto. O período equivalente anterior usa a mesma quantidade de meses imediatamente anterior à janela selecionada.
4. **Agregados por compra:** Categoria, modalidade, participação por cartão, ranking de estabelecimentos e tendência mensal usam as transações e seus valores totais; recorrência usa `isRecurring`/recorrências ativas. Contagens do ranking contam compras.
5. **Drilldown reconciliado:** A lista de Análises apresenta compras com valor total e data de compra; detalhes de fatura seguem baseados nas parcelas.

---

## 4. Arquitetura de Informação, Configurações e Marca Vetorial (`TASK-020`)

### 4.1 Navegação Principal e Configurações na Engrenagem
- **Bottom Navigation Bar (3 destinos primários):**
  - Aba `0`: **Faturas** (`Icons.Default.Receipt`) — gestão de faturas do mês, progresso de quitação, despesas por titular, recorrências e lançamentos.
  - Aba `1`: **Cartões** (`Icons.Default.CreditCard`) — vitrine 3D de cartões, flip frente/verso, ações rápidas (`"Lançar compra"`, `"Ver faturas"`) e gestão de cartões.
  - Aba `2`: **Análises** (`Icons.Default.QueryStats` / `InsertChartOutlined`) — painel analítico offline com gráficos interativos, KPIs, filtros de período/cartão e drilldown.
- **Ícone de Engrenagem na TopBar (`Icons.Default.Settings`):**
  - Disponível no canto superior direito da TopBar (`contentDescription = "Configurações"`).
  - Abre a tela dedicada **Configurações** com botão Voltar na TopBar (`Icons.AutoMirrored.Filled.ArrowBack`) e `BackHandler` nativo que retorna à aba ativa anterior (`0`, `1` ou `2`).
  - Agrupa sem jargão técnico: (1) **Captura automática de compras** (status da permissão do Android + botão de abertura + refresh em `Lifecycle.Event.ON_RESUME`), (2) **Aparência** (`ThemeMode.LIGHT`, `DARK`, `SYSTEM`), (3) **Alertas por e-mail** (configuração SMTP recolhível com `AnimatedVisibility`, persistência em `EncryptedSharedPreferences` e feedback de salvamento), e (4) **Titulares e limites** (acesso rápido ao gerenciamento/cadastro de titulares).

### 4.2 Identidade Visual e Logotipo Vetorial (`ic_launcher_*`)
- **Conceito Original ("Vitrine Matrix & Cobre"):** Dois cartões em perspectiva isométrica/3D com acabamento Petróleo profundo (`#00383B` -> `#004E52`) e destaque em Cobre acetinado (`#C76A2B` -> `#F2A365`), chip EMV geométrico e barras ascendentes de controle financeiro no quadrante central (safe-zone de `66dp` dentro do grid de `108dp` do Adaptive Icon).
- **Arquivos Vetoriais:** `app/src/main/res/drawable/ic_launcher_background.xml` e `app/src/main/res/drawable/ic_launcher_foreground.xml`, referenciados por `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml`, além de `CcBrandLogo` reutilizável na UI interna.

### 4.3 Matriz Funcional Ponta a Ponta (Seção → Ação → Origem → Persistência → Retorno)

| Seção / Tela | Ação do Usuário | Origem dos Dados | Efeito / Persistência | Retorno / Feedback Visual |
| :--- | :--- | :--- | :--- | :--- |
| **TopBar Global** | Alternar mês `< Mês/Ano >` (Faturas/Análises) | `MainViewModel.currentMonth/Year` | Atualiza `StateFlow` de mês/ano | Recalcula faturas e janela de análises instantaneamente |
| **TopBar Global** | Tocar em `+` (Novo titular / Novo cartão) | Aba ativa (`Faturas` ou `Cartões`) | Abre `HolderDialog` ou `CardDialog` | Salva no Room (`holders` / `credit_cards`) e fecha modal |
| **TopBar Global** | Tocar na Engrenagem (`Configurações`) | Estado `showSettingsScreen` | Navega para tela `Configurações` | TopBar exibe seta Voltar; `BackHandler` retorna à aba anterior |
| **Aba Cartões** | Deslizar carrossel / Tocar em Virar | `CreditCardEntity` + `BankCardVisuals` | Atualiza `selectedCardId` / `isFlipped` | Animação 3D Y-axis (ou `Crossfade` com Reduce Motion) |
| **Aba Cartões** | `"Lançar compra"` / `"Ver faturas"` | `selectedCard.id` | Abre `ExpenseDialog(initialCardId)` ou vai para Aba `Faturas` | Compra rateada via `splitInstallmentAmounts` e gravada no Room |
| **Aba Cartões** | Menu `⋮` -> Editar / Excluir cartão | `selectedCard` | `viewModel.saveCard` / `deleteCard` | Bloqueia exclusão se houver histórico financeiro com aviso claro |
| **Aba Faturas** | `"Nova compra"` (FAB) / `"Registrar pagamento"` | `holders`, `cards`, `installments` | `addExpense` / `registerPayment` no Room | Barra de progresso de quitação e saldos por titular atualizam |
| **Aba Faturas** | Confirmar / Dispensar recorrência | `RecurringChargeEntity` | `confirmRecurringCharge` / `dismissRecurringCharge` | Atualiza card de assinaturas e refletirá na modalidade `RECORRENTE` |
| **Aba Análises** | Selecionar Período (Mês, 3M, 6M, 12M) ou Cartão | `InstallmentEntity`, `PaymentEntity`, `CreditCardEntity` | Recalcula `AnalyticsReport` em `FinancialAnalyticsEngine` | Atualiza KPIs, gráficos de barras/rosca, rankings e lista |
| **Aba Análises** | Tocar em barra/fatia/categoria/estabelecimento | `AnalyticsDrilldownFilter` | Aplica filtro de drilldown (ou limpa no chip) | Lista inferior exibe apenas os lançamentos reconciliados com subtotal |
| **Configurações** | Ativar notificações / Tema / Salvar SMTP | `NotificationListener`, `ThemePreferenceStore`, `SecurePrefs` | Abre settings do Android / persiste tema / grava no Keystore | Atualiza no `ON_RESUME`, aplica tema imediato e exibe confirmação |

---

## 5. Marcos de Entrega e Critérios de Aceite

- [x] **TASK-017 — Redesign Visual, UX e Vitrine Base (F1–F9):** Tema Petróleo & Cobre / Grafite, TopBar contextual, diálogos, acessibilidade e revisão independente concluídos.
- [x] **TASK-018 — Carteira de Cartões 3D Personalizada:** `CardVisuals.kt`, `CardElements.kt`, `CardFace.kt`, `CardBack.kt`, `CardCarousel.kt`, 131/131 testes unitários PASS, `lintDebug` PASS, `assembleDebug` PASS.
- [x] **TASK-019 — Nova Área de Análises Financeiras Offline:** `FinancialAnalyticsEngine.kt`, `CcCharts.kt`, `AnalyticsScreen.kt`; agregados baseados em `TransactionEntity`, janelas locais com limite final exclusivo, categorias/modalidades/cartões/estabelecimentos/tendência sem duplicação por parcela; 155/155 testes, lint/build e diff-check PASS em 2026-10-17.
- [ ] **TASK-020 — Nova IA de Navegação, Configurações na Engrenagem, Logotipo Vetorial e Auditoria End-to-End:** BottomBar com `Faturas`, `Cartões` e `Análises`, `Configurações` via engrenagem na TopBar, novo `ic_launcher_background.xml` e `ic_launcher_foreground.xml` na identidade Vitrine, todos os gates verdes (`testDebugUnitTest --rerun-tasks`, `lintDebug`, `assembleDebug`, `git diff --check`, `test_bundle_canary.py`), revisão read-only do Flash, geração do novo APK debug final com SHA-256 e integração/push seguro em `main`.
- [ ] **TASK-022/023 — Revisão visual e auditoria independente:** APK `0D918846…B82981ED` instalado e telas capturadas em 720×1600. Flash read-only pendente. No aparelho, faturas/análises exibem zero compras e zero titulares, então valor Nov/2026 R$300 e diálogo de validação/cancelamento não puderam ser confirmados sem inserir dados no perfil.


## Onda adicional — TASK-025: Design CardControl inspirado no Leve (17/10/2026)

### Objetivo
Usar o Leve local como referência para a identidade visual inteira, com adaptação nativa a Jetpack Compose/Material 3 Expressive. Brief canônico: `.agents/LEVE_DESIGN_REFERENCE.md`.

### Sequência de execução (não aguardar aprovação para decisões técnicas normais)
1. Design System & Layout Review faz auditoria read-only do Leve e do CardControl e entrega mapeamento evidenciado, P0-P2 e propostas; pode trabalhar enquanto o escritor atual termina.
2. Gemini - Continuidade conclui o escopo já iniciado e registra handoff antes de receber outro arquivo/onda.
3. Maestro escolhe o design (reconciliando cores reais do Leve), define tokens Compose e libera escopo de arquivos exclusivo para implementação incremental.
4. Data/Room mantém sua análise de integridade em escopo separado; qualquer correção de exclusão/cold-start exige testes de regressão e não pode misturar mudanças contábeis com estilo visual.
5. APK Validator revisa o SHA e os gates do estado final somente depois que os escritores encerram.
6. Capturar e inspecionar telas renderizadas em 360dp, escala de fonte ampliada, claro/escuro e estados vazios/erro/loading. Em seguida executar testes, lint, build e diff-check sequencialmente.

### Critério de encerramento
O sistema visual foi aplicado de forma consistente às telas principais; decisões de fonte/cor derivam de código real e são documentadas; acessibilidade e adaptação móvel foram verificadas; nenhum dado/fluxo financeiro foi alterado por motivo puramente estético; o APK testado corresponde ao código revisado; pendências e evidências ficam registradas. Sem publicação/commit/push não autorizados.


## TASK-026 e TASK-027 — Dados financeiros antes de redesign adicional

- **TASK-026 (P0, segura por padrão):** apurar chamadas de exclusão de titular e relações FK. Adicionar uma operação Room/DAO transacional que só exclua titular sem histórico financeiro vinculado. Se houver transações/parcelas/pagamentos, bloquear hard-delete e retornar resultado de domínio explícito para UI apresentar razão e caminho seguro. Não confiar só no ViewModel/diálogo; cobrir race/atomicidade e chamadas diretas do DAO/repository. Não implementar reatribuição/arquivamento sem desenhar semântica consistente.
- **TASK-027 (P1):** corrigir o período inicial de Análises para que primeira UI/render e consulta Room usem a mesma fonte de mês/ano/fuso. Não adicionar sleeps/delay cosmético. Criar teste determinístico com relógio/fuso e emissões iniciais.
- **Ordem:** escritor atual conclui handoff e gates da tarefa atual; depois Data/Core Data recebe exclusividade de escrita para TASK-026 e TASK-027, com testes específicos e handoff; somente então iniciar onda de implementação visual. Revisor de design pode seguir read-only em paralelo. Nunca rodar Gradle paralelo nem acessar DB real.


## Clarificação de escopo TASK-026 (22:52 -03)

A proteção contra exclusão em cascata deve abranger **titulares e cartões**, pois ambos são pais da entidade recurring_charges por FK ON DELETE CASCADE. Em ambos os guards Room, recusar hard-delete se houver qualquer referência em transactions/installments/payments/recurring_charges; retirar/restringir APIs raw sem guarda. Encapsular a exclusão autorizada e a limpeza de defaults em transação. Testar recorrência isolada em ambos os pais, casos com compras/pagamentos/parcelas, casos sem vínculos, preservação integral dos filhos e feedback humano para a pessoa usuária. FakeAppDao deve espelhar a regra e testes DAO real precisam cobrir o contrato de FK/atomicidade.
