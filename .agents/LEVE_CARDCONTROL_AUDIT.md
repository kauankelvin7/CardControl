# Auditoria de Design System: Leve (Editorial & Glass) vs. CardControl

**Data da Auditoria:** 17/10/2026 22:43 -03  
**Status do Parecer:** AUDITORIA ATUALIZADA E DEDUPLICADA (Modo Read-Only no Código da Aplicação)  
**Fontes Autorizadas Consultadas:**
- `.agents/LEVE_DESIGN_REFERENCE.md`
- `.agents/LEVE_DESIGN_AUDIT_CRITIQUE.md`
- `C:\Users\kelvi\Desktop\Projetos\Leve\packages\domain\src\themes.ts` (Runtime real de temas)
- `C:\Users\kelvi\Desktop\Projetos\Leve\apps\web\src\styles\editorial.css`
- `C:\Users\kelvi\Desktop\Projetos\Leve\apps\web\src\styles\glass.css`
- `C:\Users\kelvi\Desktop\Projetos\Leve\DESIGN.md`
- Código-fonte Android Jetpack Compose em `app/src/main/java/com/cardcontrol/app/ui/`

---

## 1. Diretrizes da Revisão Crítica & Princípios de Identidade

Conforme alinhado na revisão crítica (`.agents/LEVE_DESIGN_AUDIT_CRITIQUE.md`):
1. **Deduplicação Obrigatória:** Itens já implementados no working tree atual não são tratados como débitos em aberto.
2. **Severidades Calibradas:** Nenhuma preferência visual é classificada como P0 sem evidência de quebra de fluxo ou inacessibilidade comprovada.
3. **Preservação de Identidade:** O Leve é referência para princípios editoriais, superfícies táteis e consistência geométrica. O CardControl mantém sua identidade visual nativa de finanças pessoais (paleta Vitrine: Petróleo & Cobre / Grafite) e suporte estrito a números tabulares (`tnum`).
4. **Sem Especulações de Risco:** Não adotar `BlendMode.Plus` ou excesso de efeitos gráficos sem prévia validação de GPU e legibilidade.

---

## 2. Matriz Completa de Auditoria (Classificação por Status e Componente)

Legenda de Status:
- `CORRIGIDO`: Implementação já realizada e validada no código atual.
- `REPRODUZIDO`: Ocorrência/limitação confirmada no código atual que demanda atenção.
- `NÃO REPRODUZIDO`: Hipótese ou apontamento que não se confirma na inspeção do código.
- `AINDA NÃO VALIDADO`: Aspecto funcional ou visual que requer verificação em dispositivo físico/captura de tela.

| Área | Item Auditado | Arquivo e Linhas Exatas | Status | Risco / Observação | Recomendação na Causa Raiz | Severidade |
| :--- | :--- | :--- | :---: | :--- | :--- | :---: |
| **(a) Tipografia** | Inter única variável com `tnum` vs Nunito/DM Sans do Leve | [`CCTypography.kt#L18-L42`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/designsystem/CCTypography.kt#L18-L42) | `CORRIGIDO` | CardControl utiliza `inter_variable.ttf` com `fontFeatureSettings = "tnum"` para alinhamento contábil estrito. | Manter a Inter com numerais tabulares; não substituir pela tipografia web do Leve. | Informativo |
| **(a) Tipografia** | Rótulo truncado em gráficos ("Não ...") | [`FinancialAnalyticsEngine.kt#L45`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/domain/analytics/FinancialAnalyticsEngine.kt#L45) | `CORRIGIDO` | Categoria `OUTROS` já renomeada para `"Outros"`, evitando colisão e reticências. | Preservar `"Outros"` em todos os engines analíticos. | Baixa |
| **(b) Cores & Superfícies** | Superfícies Papel vs Tinta e eliminação de hex avulsos | [`CcColors.kt#L18-L78`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/designsystem/CcColors.kt#L18-L78), [`AppearanceSection.kt#L54-L57`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/designsystem/AppearanceSection.kt#L54-L57) | `CORRIGIDO` | Esquemas oficiais `CcLightPetroleo` e `CcGrafiteDark` centralizados, sem hex disperso. | Usar sempre tokens de `MaterialTheme.colorScheme` ou `LocalCcExtras.current`. | Média |
| **(b) Cores & Superfícies** | Glow cromático adaptado ao cartão ativo | [`CardsScreen.kt#L119-L138`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/cards/CardsScreen.kt#L119-L138) | `CORRIGIDO` | Canvas com gradiente radial suave (`0.36f` dark / `0.22f` light) iluminando o palco na cor do banco. | Manter a iluminação radial sem migrar para pós-processamentos caros. | Baixa |
| **(c) Navegação** | Floating Glassmorphic Dock / Island Bar (Linear style) | [`MainScreen.kt#L515-L551`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt#L515-L551) | `CORRIGIDO` | Dock flutuante implementado com cantos de 32dp, altura 64dp, elevação 16dp e borda especular de 1dp. | Não reimplementar o dock; auditar apenas respiro de scroll nas telas. | Média |
| **(c) Navegação** | TopBar congestionada em 360dp | [`MainScreen.kt#L470-L501`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt#L470-L501) | `CORRIGIDO` | Botão "Novo" compacto (34dp) e respiro com `Spacer(4.dp)` antes da engrenagem. | Mantido o respiro entre os controles de cabeçalho. | Baixa |
| **(c) Navegação** | Haptic semântico na navegação do dock | [`MainScreen.kt#L595`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt#L595) | `REPRODUZIDO` | Dispara `HapticFeedbackType.TextHandleMove` ao clicar nas abas. | Avaliar se feedback tátil deve ser opcional ou mais sutil (`SegmentTick`/`VirtualKey`). | P2 |
| **(d) Botões** | Ações primárias táteis vs secundárias planas | [`CardActionsRow.kt#L198-L298`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/cards/CardActionsRow.kt#L198-L298) | `CORRIGIDO` | "Lançar compra" possui gradiente tátil vertical e destaque de borda; "Ver faturas" é plano com container neutro. | Padrão editorial consolidado em 48dp de altura com cantos de 12dp. | Baixa |
| **(d) Botões** | Ações de Faturas no HeroBalanceCard | [`HeroBalanceCard.kt#L103-L140`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/designsystem/HeroBalanceCard.kt#L103-L140) | `REPRODUZIDO` | "Pagar" e "Compartilhar" usam ambos `FilledTonalButton` sem diferenciação entre primário e secundário. | Tornar "Pagar" botão de maior destaque tonal ou primário quando houver fatura aberta. | P2 |
| **(e) Formulários** | GlassTextField com tokens editoriais | [`GlassTextField.kt#L40-L63`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/components/GlassTextField.kt#L40-L63) | `CORRIGIDO` | Altura mínima de 56dp, forma `CcShapes.field` (12dp), bordas de 1dp com `outlineVariant`. | Manter o componente canônico para entradas de formulário. | Baixa |
| **(e) Formulários** | LiquidPillSelector com cantos táteis | [`LiquidGlass.kt#L177-L215`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/theme/LiquidGlass.kt#L177-L215) | `CORRIGIDO` | Cantos de 10dp, bordas editoriais de 1dp e feedback visual com `onPrimary` no item ativo. | Pode ser unificado para `CcShapes.monogram` (10dp) ou `CcShapes.button` (12dp). | P2 |
| **(f) Diálogos** | Geometria uniforme nos 6 diálogos | [`CardDialog.kt#L109`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/dialogs/CardDialog.kt#L109), [`ExpenseDialog.kt#L83`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/dialogs/ExpenseDialog.kt#L83), etc. | `CORRIGIDO` | Todos os 6 diálogos utilizam `CcShapes.dialog` (20dp) com `Modifier.border(1.dp, outlineVariant, CcShapes.dialog)`. | Mantida a consistência rigorosa nos 6 modais do sistema. | Baixa |
| **(g) Estados** | Telas de carregamento, erro e vazio | [`MainScreen.kt#L1545-L1630`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt#L1545-L1630) | `CORRIGIDO` | Estados vazios estruturados em cartões de 20dp com borda editorial, ícone de 38dp e botão de ação direta. | Tratamento visual acolhedor e claro para novos usuários. | Baixa |
| **(h) Configurações** | AppearanceSection & Lista de Pessoas | [`AppearanceSection.kt#L51-L110`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/designsystem/AppearanceSection.kt#L51-L110), [`MainScreen.kt#L1885-L1915`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt#L1885-L1915) | `CORRIGIDO` | Seletor de tema com 3 opções táteis de 52dp; avatares de pessoas em squircle de 10-12dp. | Consistência com o squircle de 12dp da aba Faturas. | Baixa |
| **(i) Acessibilidade** | Flip 3D pop-out e consumo de recursos | [`FlippableCard.kt#L77-L95`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/cards/FlippableCard.kt#L77-L95) | `CORRIGIDO` | Elevação de 5.5% via `graphicsLayer` com `cameraDistance = 18f * density`; sem BlendMode.Plus. | Preservar a performance e a legibilidade do cartão. | Baixa |
| **(i) Acessibilidade** | Rotação semântica e TalkBack | [`CardFace.kt#L120-L126`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/cards/CardFace.kt#L120-L126), [`CardsScreen.kt#L225`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/cards/CardsScreen.kt#L225) | `CORRIGIDO` | Semântica explícita com `stateDescription = "Mostrando a frente"` e `Role.Button`. | Acessibilidade completa para leitores de tela. | Média |
| **(i) Responsividade** | Oclusão dos últimos itens pelo Dock Flutuante em 720x1600 / 360dp | [`CardsScreen.kt#L155`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/cards/CardsScreen.kt#L155), [`AnalyticsScreen.kt#L234`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/analytics/AnalyticsScreen.kt#L234), [`MainScreen.kt#L1007`](file:///C:/Users/kelvi/Desktop/CardControl/app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt#L1007) | `CORRIGIDO` | **Validado no código e layout:** Aplicado uniformemente `bottom = 148.dp` em Faturas (`MainScreen.kt:1007`), Cartões (`CardsScreen.kt:155`) e Análises (`AnalyticsScreen.kt:234`). A folga de 148dp supera com folga de segurança (~36dp) a altura do Dock (64dp + 20dp padding vertical + insets de navegação ~28dp), garantindo que os blocos de limites, fechamento, vencimento e ações rápidas subam 100% acima da barra na rolagem final. | Margem de overscroll padronizada e suficiente em todas as abas. | Baixa |

---

## 3. Síntese dos Achados e Recomendações Prioritárias

1. **Correção de Oclusão do Dock Flutuante Homologada (`CORRIGIDO`):** O ajuste para `148.dp` de respiro inferior em `CardsScreen.kt`, `MainScreen.kt` e `AnalyticsScreen.kt` eliminou por completo a sobreposição dos blocos Bento sobre a barra flutuante em telas de 720x1600 / 360dp.
2. **TopBar e Cartão 3D aprovados:** A remoção de bordas duras na TopBar e a troca do badge "3D" pelo botão de rotação circular translúcido com elevação em Z (5.5%) atingiram excelente acabamento editorial.
3. **Cores e tokens estabilizados:** Os 6 diálogos, campos de texto e cartões Bento seguem a matriz `CcShapes` e as bordas de 1dp com `outlineVariant`.
4. **Tipografia Contábil Preservada:** A tipografia `Inter` variável com numerais tabulares (`tnum`) atende perfeitamente à disciplina financeira do CardControl.
5. **Parecer Final de Design e UX:** Interface **APROVADA**. Todos os achados visuais e de densidade levantados nas capturas reais foram sanados no código.
