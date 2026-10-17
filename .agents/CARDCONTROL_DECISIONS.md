# Registros de Decisões Arquiteturais (ADRs)

## ADR-001: Persistência Local Estrita com Room SQLite
- **Status:** Aceito
- **Contexto:** Necessidade de garantir privacidade financeira absoluta, sem custos de servidor e funcionamento sem internet.
- **Decisão:** Utilizar SQLite local gerenciado pela biblioteca Jetpack Room com migration destrutiva em desenvolvimento e chaves estrangeiras com `CASCADE`.

---

## ADR-002: Captura Passiva de Notificações via `NotificationListenerService`
- **Status:** Aceito
- **Contexto:** O Android proíbe leitura direta de SMS bancários em versões recentes e apps bancários não fornecem APIs locais públicas gratuitas.
- **Decisão:** Implementar `NotificationListenerService` oficial do Android. O app reage a eventos emitidos pelos bancos, filtrando palavras-chave de aprovação e estorno, sem manter loops ou polling que drenem bateria.

---

## ADR-003: Criptografia de Credenciais no Android Keystore
- **Status:** Aceito
- **Contexto:** Se o usuário optar por alertas de e-mail instantâneos via SMTP, a senha de aplicativo não pode ficar legível no disco.
- **Decisão:** Usar `EncryptedSharedPreferences` com cifra `AES256-GCM` baseada em hardware através do `MasterKey`.

---

## ADR-004: Design System Próprio em Jetpack Compose ("Liquid Glass")
- **Status:** Aceito
- **Contexto:** O visual padrão do Material Design 3 é genérico. O usuário solicitou estética visual de alto nível.
- **Decisão:** Criar um tema personalizado baseado em vidro líquido (*Liquid Glass*), com paleta *Obsidian/DeepSlate*, gradientes suaves, ambient glow ciano/índigo e componentes sob medida (`GlassTextField`, `LiquidPillSelector`, `LiquidGlassBar`).

---

## ADR-005: Certificado de Assinatura Local Próprio (Keystore Dedicado)
- **Status:** Suspenso por incidente de segurança (15/10/2026)
- **Contexto:** O keystore de release e as senhas associadas foram versionados em um repositório público. O material deve ser tratado como comprometido.
- **Decisão anterior:** Um keystore dedicado era usado para assinar APKs distribuídos diretamente e permitir atualizações com a mesma assinatura.
- **Decisão atual:** Não reutilizar a chave nem as senhas expostas. A assinatura de release está desativada até que o caminho de atualização das instalações existentes seja avaliado, uma nova chave seja gerada e a assinatura passe a usar configuração externa ao Git. Uma nova chave pode impedir a atualização direta de instalações assinadas com a chave anterior; não publicar uma versão até validar a estratégia de migração.

---

## ADR-006: Representação Canônica de Moeda em Centavos Inteiros (`Long`)
- **Status:** Aceito
- **Contexto:** Cálculos com ponto flutuante (`Double`) geravam inconsistências de arredondamento em faturas e rateio de parcelas.
- **Decisão:** Todas as grandezas monetárias no banco SQLite (`subLimit`, `totalLimit`, `totalAmount`, `amount`, `amountPaid`) utilizam representação inteira canônica em centavos (`Long`). Conversões e formatações são centralizadas em `MoneyUtils`, com distribuição do resto na primeira parcela.

---

## ADR-007: Identificação Explícita de Banco Emissor (`SupportedBank`) e Room v4
- **Status:** Aceito
- **Contexto:** A inferência do banco pelo nome de exibição (`name`) quebrava a captura automática quando o usuário renomeava o cartão para um apelido customizado (ex: "Viagem", "Pessoal").
- **Decisão:** Adicionar coluna explícita `bank: String` à `CreditCardEntity` mapeada ao enum `SupportedBank`, evoluindo o Room para versão 4 (`MIGRATION_3_4`) com backfill retroativo. O `resolveCardForNotification` passa a filtrar prioritariamente por `bank`, preservando o apelido para visualização e desambiguando cartões com os mesmos 4 dígitos.

---

## ADR-008: Unificação Canônica de Governança em `.agents` e Integração de Ferramentas
- **Status:** Aceito
- **Contexto:** A existência simultânea das pastas `.agent` e `.agents` gerava duplicidade de skills e ambiguidade no fluxo dos agentes do Antigravity IDE.
- **Decisão:** Eliminar a pasta legada `.agent` e unificar toda a governança e skills na raiz oficial `.agents`. Integrar as skills oficiais `graphify` (Graphify-Labs), `context-mode` (v1.0.169) e `ponytail`. Fortalecer o script de validação `check_state.ps1` com o padrão Leve de rastreabilidade e integridade estrita.

---

## ADR-009: Detecção Offline de Gastos Recorrentes e Assinaturas (Subscription Tracker)
- **Status:** Aceito
- **Contexto:** Necessidade de identificar assinaturas e despesas fixas recorrentes (streaming, academias, planos de assinatura) de forma 100% offline, alertando sobre variações de preço ("price creep") e mensurando o impacto percentual no limite total dos cartões.
- **Decisão:** Criar a entidade `RecurringChargeEntity` e schema Room v5 com migration determinística `MIGRATION_4_5`. O motor `SubscriptionTracker` só detecta recorrência automática quando o intervalo entre as duas cobranças mais recentes do mesmo merchant normalizado e cartão estiver entre 28 e 32 dias; a confirmação manual só é válida para cobranças únicas; transações parceladas são excluídas da detecção de assinaturas. A sincronização reutiliza registros pela identidade normalizada do merchant, e o impacto percentual no limite preserva valores acima de 100% para não ocultar ultrapassagens. A interface `LiquidGlass` exibe o comprometimento mensal e alerta visual sobre aumento de valor.

---

## ADR-010: Motor de Captura Multicategoria de Notificações Bancárias (Gastos, Aproximação NFC, Carteiras Digitais, Pix, TED e Boletos)
- **Status:** Aceito
- **Contexto:** O parser anterior restringia-se a compras com cartões e descartava sumariamente notificações que contivessem palavras como `pix`, `transferencia` ou `boleto`, além de não reconhecer transações por aproximação (*contactless* NFC) ou confirmações de carteiras digitais (Apple Pay / Samsung Pay).
- **Decisão:** Introduzir o enum `NotificationCategory` (`CARD_EXPENSE`, `PIX_SENT`, `PIX_RECEIVED`, `TRANSFER_SENT`, `BILL_PAID`), enriquecer o `BankNotificationParser` com extração de contrapartes (`counterpart`) e estabelecimentos, lookaheads seguros para pontuação final (`(?=\s+(?:via\b|com\b...)\b|\s*[.,:;!]|\s*$)`), desambiguação da deduplicação SHA-256 por categoria e preservação estrita do cálculo financeiro canônico em centavos (`Long`). Rejeições legítimas de segurança (tokens 2FA, senhas provisórias, códigos de autenticação) e sanitização contra ReDoS e CRLF continuam rigorosamente ativas.

---

## ADR-011: Design System Vitrine v2.1 (Claro: Petróleo & Cobre, Escuro: Grafite, Sobrescrita Global e Experiência de Cartões)
- **Status:** Aceito
- **Contexto:** O app demandava uma renovação profunda do sistema visual e da experiência de cartões inspirada no Google Wallet, com contraste garantido, acessibilidade nativa e total independência de cores dinâmicas de papel de parede do Android (Dynamic Color / Monet).
- **Decisão:**
  1. Sobrescrever globalmente as cores do app com o Design System Vitrine v2.1: Claro = Petróleo & Cobre (`CcLightPetroleo`), Escuro = Grafite (`CcGrafiteDark`).
  2. Dynamic Color desativado por design (D-01): o sistema operacional define apenas a luminosidade (claro/escuro no modo `ThemeMode.SYSTEM`); a paleta de cores permanece 100% controlada pelo app.
  3. Tipografia Material 3 com fonte física Inter e algarismos tabulares (`tnum`).
  4. Experiência de cartões com carrossel interativo (`HorizontalPager` com ID estável e z-index adaptativo), flip 3D no eixo Y (180° com contrarrotação para evitar espelhamento e fade fallback quando movimento reduzido estiver ativado), tingimento animado do terço superior do palco, e ações reais (Lançar compra, Editar, Excluir) sem simulação de cartões de crédito físicos, chips ou pagamentos NFC.
  5. Resolução determinística e autônoma de paletas em espaço de cores CIELAB (D65) sem chamadas a métodos não mockados do Android SDK na JVM.
  6. Todas as decisões D-01 a D-16 de `CARDCONTROL_PROMPT_ANTIGRAVITY_VITRINE_COLOR_OVERRIDE.md` consolidadas e validadas por 126 testes automatizados e compilação do APK de debug.
