# Plano de Engenharia — Design System Vitrine v2.1 no CardControl

## 1. Contexto e Objetivo
Implementar fielmente a especificação `CARDCONTROL_PROMPT_ANTIGRAVITY_VITRINE_COLOR_OVERRIDE.md` no CardControl:
- **Sobrescrita global de cores:** O app controla toda a sua paleta. O sistema Android decide apenas luminosidade claro/escuro quando em `ThemeMode.SYSTEM`. Dynamic Color do Android 12+ nunca altera a paleta do app.
- **Dois temas Vitrine:** Tema Claro (**Petróleo & Cobre**) e Tema Escuro (**Grafite**).
- **Experiência de Cartões Vitrine:**
  - `CardFace` (frente com proporção 1,586:1, monograma 40dp com regras de iniciais, avatar titular 20dp, final mascarado).
  - `CardBack` (verso com dados reais de fechamento, vencimento, limite e banco, sem dados fictícios nem recursos de pagamento/NFC/chip).
  - `FlippableCard` (flip 3D no eixo Y, sem distorção e com fallback de fade para movimento reduzido).
  - `CardCarousel` (`HorizontalPager` por ID estável, peek responsivo, escala 0.92, opacidade 0.72, translação Y 6dp nos vizinhos, tingimento sutil do palco no terço superior).
  - `PagerIndicator` (pílulas animadas 2-7, texto "n de N" para 8+, oculto para 0 ou 1).
  - Cabeçalho contextual com menu ⋮ (Editar, Excluir).
  - Ações contextuais reais vinculadas ao ID estável do cartão selecionado.
  - Grupo "Ver mais" + "+" com cantos morfológicos (28dp externo / 6dp interno).
  - Estado vazio com cartão-fantasma tracejado.
- **Testes automatizados:**
  - Testes unitários puros de contraste (W3C WCAG 2.1) para todas as combinações de cores e 12 presets + 200 sementes aleatórias.
  - Teste unitário de resolução de tema (`resolveDark`).
  - Preservação total dos 116 testes existentes no harness.

---

## 2. Fases de Execução

### Fase F1: Fundação de Tokens Vitrine (Nível A)
- Criar/atualizar tokens em `ui/designsystem/`:
  - `CcColors.kt`: Paletas `CcLightPetroleo` e `CcGrafiteDark`, cores semânticas B3.3, extras `CcExtras` (`cardSheen`, `cardOutline`, `scrimSoft`).
  - `CcType.kt` / `CCTypography.kt`: Inter variável com `tnum`, estilos de texto para o cartão (`cardBank`, `cardMeta`, `cardMask`) e cabeçalho.
  - `CcShapes.kt`: Formas card (24dp), dialog (24dp), actionContainer (16dp), monogram (12dp), pill (100%), chip (8dp).
  - `CcSpace.kt`: Escala base 4dp.
  - `CcMotion.kt` e `CcReduceMotion.kt`: Molas espaciais e de efeitos, detecção de escala de animação do sistema.
  - `CCTheme.kt`: Provider com `ThemeMode` (`SYSTEM`, `LIGHT`, `DARK`), `resolveDark`, e `CardControlTheme` unificado.
  - Atualizar `ui/theme/Theme.kt` e `ui/theme/Color.kt` para delegar aos tokens Vitrine, garantindo que toda tela existente use as cores do Vitrine.

### Fase F2: Paleta de Cartões & Algoritmo de Cores
- Criar `ui/cards/CardPalette.kt` e `CardPaletteResolver.kt`:
  - Algoritmo de derivação L* 38 -> 22, sheen branco 8% topo-esquerdo.
  - 12 predefinições fixas testadas (Grafite, Petróleo, Safira, Índigo, Ametista, Rosé, Rubi, Coral, Cobre, Âmbar, Oliva, Esmeralda).
  - Resolução estável de cor automática por hash do banco.
  - Cálculo de tingimento do palco (`lerp(surface, start, alpha)`).

### Fase F3: Componentes de Cartão e Carrossel
- Criar em `ui/cards/`:
  - `CardUi.kt`: Modelo de apresentação desacoplado mapeando `CreditCardEntity` + titular associado + fatura.
  - `CardFace.kt`: Frente com monograma (regras de iniciais), avatar do titular, apelido, final mascarado, botão virar.
  - `CardBack.kt`: Verso com dados reais de fatura/limite, sem dados proibidos.
  - `FlippableCard.kt`: Transição 3D em Y com contrarrotação e suporte a `CcReduceMotion`.
  - `PagerIndicator.kt`: Indicador de pílulas animadas ou contador textual.
  - `CardCarousel.kt`: `HorizontalPager` por ID estável, peek responsivo, escala/opacidade dos vizinhos e tingimento do palco.
  - `CardActionsRow.kt`: Botões de ação contextual real (Detalhes, Editar, Lançar compra, Excluir).
  - `SeeMoreAndAdd.kt`: Botão composto tonal/primário com cantos 28dp/6dp.
  - `CardsScreen.kt`: Tela integrada da aba "Cartões" ou seção do carrossel, com estado vazio tracejado, cabeçalho contextual e diálogo de exclusão integrado.

### Fase F4: Integração com MainScreen e Diálogos
- Substituir a visualização simples de cartões na aba 1 de `MainScreen.kt` pelo carrossel Vitrine.
- Vincular ações do carrossel aos fluxos reais existentes:
  - Editar -> `onUpdateCard` via `CardDialog`
  - Lançar compra -> `onAddExpense` via `ExpenseDialog`
  - Excluir -> `DeleteCardDialog`
  - Adicionar cartão ("+") -> `onAddCard` via `CardDialog`
- Atualizar diálogos para usar os tokens e formas Vitrine.

### Fase F5: Preferências de Aparência (Tema Vitrine)
- Adicionar opção de seleção de tema (Sistema, Claro, Escuro) em memória/DataStore para controle dinâmico da paleta Vitrine.

### Fase F6: Testes Automatizados (Contraste, Resolução de Tema e Regressão)
- Criar `VitrineThemeAndContrastTest.kt` em `app/src/test/java/com/cardcontrol/app/`:
  - Teste de `resolveDark` para os 3 modos.
  - Teste de contraste (W3C WCAG 2.1) de pares `on*/*` nos temas Claro e Escuro.
  - Teste de contraste dos 12 presets e das 200 sementes aleatórias na função `derive`.
  - Teste de invariância: nenhuma dependência de Dynamic Color.
- Executar `testDebugUnitTest` (todos os 116 testes existentes + novos testes).
- Executar `assembleDebug`.
- Inspecionar `git diff --check`.
