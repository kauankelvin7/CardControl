# CardControl v3 — Reformulação & Design System

> Documento de trabalho para alimentar o Claude Design e guiar a refatoração em Jetpack Compose.
> Base: 4 prints do app (Faturas, Cartões, Automação ×2) + README do repositório `kauankelvin7/CardControl` (Android nativo, Kotlin, Compose + Material 3, Room, offline-first, minSdk 26).
> Data: 17/10/2026.

---

## 0. Como ler este documento

**Este documento não decide por você.** Onde existe uma escolha real (cor, glass, navegação, escopo), ela está marcada como **[D#]** na Etapa 4, com opções, custo de cada uma e um jeito de testar. Onde eu precisei assumir um valor para o design system ficar utilizável, está escrito **"default provisório"** — é para ser contestado.

Ordem de leitura sugerida: Etapa 1 (diagnóstico) → Etapa 4 (decisões) → decida → o resto fica coerente com o que você escolher.

| Etapa | Conteúdo |
|---|---|
| 1 | Diagnóstico do app atual |
| 2 | Norte do produto (princípios e anti-metas) |
| 3 | Referências cruzadas |
| 4 | **Decisões em aberto (suas)** |
| 5 | Design System v1 (tokens → componentes) |
| 6 | Arquitetura de informação e telas (wireframes) |
| 7 | Novas funcionalidades e priorização |
| 8 | Microcopy e tom de voz |
| 9 | Plano de implementação em Compose (fases + código) |
| 10 | QA, acessibilidade e métricas |
| 11 | Stress test do próprio plano |
| A | Prompts prontos para o Claude Design |
| B | Cheat sheet de tokens (CSS vars para protótipo) |

---

## Etapa 1 — Diagnóstico do app atual

### 1.1 O que já está bom (preservar)

- **Proposta de valor forte e rara:** 100% offline, sem telemetria, centavos exatos (`BigDecimal`/`Long`), captura passiva por notificação. Isso é identidade — o design deve *mostrar* isso, não só o README.
- **Fundo escuro com glow ambiente**: dá profundidade sem peso.
- **Hierarquia da hero de fatura** (valor grande + bruto/abatido) é a ideia certa.
- **Barra de uso do limite** com cor semântica (âmbar em 86%).
- **Bottom nav** com indicador em pílula, labels sempre visíveis.

### 1.2 Problemas observados nos prints

| # | Onde | Problema | Gravidade |
|---|---|---|---|
| P1 | Faturas › Assinaturas | Os rótulos **"COMPROMETIMENTO MENSAL"** e **"IMPACTO NO LIMITE"** colidem (leem-se como "MENSALLIMITE"). Caixa-alta com tracking largo + duas colunas sem largura mínima. | Alta (bug) |
| P2 | Faturas | O FAB **"Lançar Compra" cobre o valor "A Pagar no Mês"** do card da pessoa. Falta `contentPadding` inferior ≥ altura do FAB + margem. | Alta (bug) |
| P3 | Faturas | **Três CTAs competindo** na primeira tela: Nova Pessoa, Abater/Pix e Lançar Compra, todos com peso visual parecido. | Média |
| P4 | Todas | Header "CardControl" dentro de uma pílula **repete em todas as abas** e depois vem outro título grande ("Seus cartões"). Duas barras de topo dizendo coisas diferentes. | Média |
| P5 | Automação | **Duas cores primárias**: botão índigo ("Conceder Permissão") e botão ciano ("Criptografar…"). Sem regra do que é primário. | Média |
| P6 | Automação | Campos com **rótulo só como placeholder** ("Gmail Remetente", "Senha de App Google" quebrando em 2 linhas). Some ao digitar → usuário perde o contexto. | Média |
| P7 | Automação | **Jargão técnico na superfície**: "Cofre Keystore", "SMTP TLS", "Criptografar no Android Keystore". Correto, mas é detalhe de implementação, não benefício. | Média |
| P8 | Automação | Rodapé **"CardControl • Motor Offline • Liquid Glass"** vaza nome de design interno na UI. | Baixa |
| P9 | Automação | Sem **estado visível** da permissão (concedida / pendente) nem "enviar e-mail de teste". A tela é uma lista de campos, não um fluxo de configuração. | Média |
| P10 | Cartões | Muito espaço morto; o cartão é uma **linha de texto**, não um objeto. Editar e **excluir lado a lado** (risco de toque errado numa ação destrutiva). | Média |
| P11 | Cartões | Linha "Utilizado: 86% • Limite total: R$ 4.850,00" comprimida com tracking largo. | Baixa |
| P12 | Cartões | **Percentual parece truncado:** (4.850,00 − 636,13) / 4.850,00 = **86,9%**, exibido como 86%. Num app cujo diferencial é "centavo exato", o arredondamento do percentual precisa ser uma regra explícita. *Confirme no código.* | Baixa (verificar) |
| P13 | Todas | Texto secundário azul-acinzentado sobre azul-marinho: contraste provavelmente **no limite de 4,5:1** em tamanhos pequenos; legenda com tracking largo agrava. *Medir.* | Média |
| P14 | Todas | Estado vazio é só texto ("Nenhuma assinatura detectada ainda…"); a hero mostra "R$ 0,00" gigante sem explicar o que fazer. | Média |
| P15 | Visual | O "vidro" atual é basicamente **card semitransparente sobre gradiente chapado**. Sem conteúdo rolando por baixo, não há o que desfocar — o efeito não se paga. | Informativa |

### 1.3 Diagnóstico estrutural (do README)

- Só **3 abas** e uma delas (Automação) é tela de **configuração**, usada poucas vezes — ocupa lugar nobre da navegação.
- **Pessoas/titulares** são o coração do rateio, mas só aparecem como cards dentro de Faturas.
- Sem **revisão de capturas**: o parser por Regex vai errar de vez em quando (loja, parcelas). Hoje o erro entra direto no banco.
- `allowBackup="false"` + zero nuvem = **perder o aparelho é perder tudo**. Não há exportação/backup mencionado.
- Sem **categorias**, **projeção de parcelas**, **lembretes de fechamento/vencimento** nem **widgets** — as coisas que fazem um app de cartão ser aberto todo dia.

---

## Etapa 2 — Norte do produto

### 2.1 Frase-norte (default provisório)

> **"Controle de cartão que você pode conferir centavo por centavo — e que nunca sai do seu celular."**

### 2.2 Princípios de design

1. **Confiança tangível.** Privacidade e precisão aparecem na interface: selo "100% no aparelho", valores exatos, cálculos explicáveis ("por que esse valor?").
2. **Cor é significado.** Cor forte só para dados (limite, status, pessoa). Marca = tipografia, forma e movimento. *(Ver [D1].)*
3. **Calma financeira.** Nenhum alarme visual sem causa; vermelho só quando há risco real. Ansiedade não é feature.
4. **Um primário por tela.** Uma ação principal clara; o resto é secundário/terciário.
5. **O valor primeiro.** O número que importa (a fatura, o limite livre, quanto a pessoa deve) é sempre o maior elemento.
6. **Rápido para lançar.** Lançar uma compra manual em ≤ 5 segundos / ≤ 4 toques.
7. **Glass com propósito.** Transparência só onde há conteúdo passando por baixo. *(Ver [D2].)*
8. **Acessível por padrão.** Contraste, escala de fonte 200%, TalkBack, estado nunca só por cor.

### 2.3 Anti-metas (o que NÃO queremos ser)

- Não ser um "banco clone" (sem logos de banco, sem copiar identidade de Nubank/Inter/etc.).
- Não virar dashboard poluído de planilha.
- Não gamificar dinheiro com confete/streaks agressivos.
- Não depender de rede para nada essencial.

### 2.4 Personalidade

Precisa, discreta, levemente técnica, mas humana. Fala **"você"**, frases curtas, sem jargão na superfície. Referência de tom: um contador gentil, não um app de investimentos.

---

## Etapa 3 — Referências cruzadas

> Análise a partir do que se conhece publicamente desses produtos, **não de capturas atuais**. Antes de fechar decisões, abra cada app e tire 3–5 screenshots de cada tela citada.

| Referência | O que estudar | Adotar | Evitar |
|---|---|---|---|
| **Nubank (app)** | Fatura como objeto central; status aberta/fechada; tom de voz; estados vazios com personalidade | Status da fatura (aberta · fecha em X dias · fechada · paga); linguagem conversacional | Copiar identidade visual/cor de marca |
| **Copilot Money** | Tipografia numérica limpa; categorias com ícone; fila de **"para revisar"** de transações | Fila de revisão de capturas; gráficos sem ruído; hierarquia de números | Densidade de iOS que não traduz bem p/ Android |
| **Splitwise** | Quem deve quanto a quem; fluxo de "quitar"; saldo por pessoa | Tela de pessoa como "conta corrente" (compras − abatimentos = saldo); status pendente/parcial/pago | Excesso de telas para registrar um pagamento |
| **Revolut** | Cartões como objetos; analytics por categoria; números que animam | Contagem animada de valores; visual de cartão; gráfico por categoria | Quantidade de módulos/promoções |
| **Apple Card / Wallet** | Cartão como "peça"; resumo mensal com cores por categoria; clareza em juros/parcelas | Visual de cartão com gradiente próprio; resumo mensal em barra segmentada | Dependência de skeuomorfismo pesado |
| **Mobills / Organizze** | Convenções de finanças em PT-BR: categorias, fatura por cartão, vencimentos | Vocabulário familiar ao usuário BR; categorias padrão | Interface carregada, muito anúncio/upsell |
| **Wise** | Transparência: mostrar o cálculo e o que muda antes de confirmar | Simulador "e se eu parcelar em 6x?" com efeito no limite e nos meses futuros | — |
| **Cash App** | Tipografia ousada, preto/branco, botão "tinta" | Botão primário em "tinta" (alto contraste) como alternativa a cor de marca | Humor excessivo |
| **Linear / Stripe Dashboard** | Hierarquia tipográfica; tabelas alinhadas; cor restrita; microinterações | `tabular-nums`, alinhamento à direita de valores, bordas finas | Densidade desktop em tela de 360dp |
| **Material 3 Expressive (Google)** | Formas, movimento em mola, componentes de lista/menu, FAB menu | Springs, morph de formas, FAB menu, tokens de movimento | Seguir o padrão cegamente sem identidade própria |

**Síntese:** a combinação mais promissora para o seu caso é **Splitwise (modelo mental de pessoas/saldo) + Copilot (revisão e tipografia numérica) + Apple Card (cartão como objeto) + M3 Expressive (motion/forma)**, sobre uma identidade sóbria própria.

---

## Etapa 4 — Decisões em aberto (suas)

> Cada decisão: opções → o que ganha/perde → como testar. O **default provisório** é só para o resto do documento fazer sentido; troque à vontade.

### [D1] Direção de cor/identidade

| Opção | Descrição | Ganha | Perde / Risco |
|---|---|---|---|
| **A · Ember** | Preto quente + laranja (`#E5591D`) | Identidade forte e quente, diferente de "fintech azul" | Laranja fica **entre o vermelho e o âmbar**, as cores de alerta do app → ambiguidade semântica. Também lembra marca de banco (o único cartão nos prints é de um banco de marca laranja). |
| **B · Obsidian Azul** | Evolução do atual: navy + ciano (`#38BDF8`), **remover o índigo** | Menor esforço; continuidade | Visual genérico "fintech dark"; ciano em tudo compete com `info` |
| **C · Grafite** | Neutros quase monocromáticos; botão primário "tinta" (off-white); cor só em dados; **acento dinâmico** = cor do cartão/pessoa selecionado | Cor = significado; sem colisão com semânticas; escala para qualquer cartão | Exige mais disciplina de tipografia/forma; "sem cor de marca" pode parecer frio se mal executado |

- **Minha inclinação (default provisório): C**, porque o produto vive de dados com semântica de risco, e uma cor de marca saturada disputa atenção com isso. **É inclinação, não veredito.**
- **Como testar:** peça ao Claude Design a **mesma Home** nas 3 direções; teste (a) sob sol/brilho baixo no seu aparelho, (b) "teste dos 5 segundos" com 3 pessoas ("qual o valor da fatura? tem algo preocupante?"), (c) simule daltonismo (protanopia/deuteranopia) no Figma/Chrome DevTools.

### [D2] Quanto "glass"

| Opção | O que é | Custo |
|---|---|---|
| **2.1 Glass seletivo** (default provisório) | Blur real **só** em camadas flutuantes sobre conteúdo que rola: bottom nav, FAB/menu, bottom sheets, snackbar. Cards = sólidos com borda fina | Baixo; melhor contraste; poupa GPU |
| **2.2 Glass total** | Tudo translúcido (como hoje, porém com blur real) | Alto: `Modifier.blur` do Compose desfoca o *próprio* conteúdo, não o fundo — backdrop blur exige lib como **Haze** (Chris Banes), que adiciona custo de frame mensurável; contraste variável conforme o que está atrás |
| **2.3 Sem glass** | Superfícies sólidas + bordas | Mais simples/acessível; perde a assinatura visual |

- **Teste:** Macrobenchmark/JankStats no *seu* aparelho mais fraco + opção do usuário "Reduzir transparência".

### [D3] Tipografia

| Opção | Fontes | Observação |
|---|---|---|
| **3.1 Trio** (default provisório) | **Sora** (display: títulos e hero) + **Inter** (UI e números, com `tnum`) + **JetBrains Mono** (só texto técnico: final do cartão, hash) | Sora dá personalidade; Inter dá legibilidade numérica |
| **3.2 Inter única** | Inter com pesos/escala | Mais coeso e leve (1 arquivo variável); menos personalidade |
| **3.3 Outra família geométrica** | p.ex. Manrope / Plus Jakarta Sans | Alternativa a Sora; **verifique** se tem figuras tabulares |

- **Regras que valem em qualquer opção:** (1) **embutir as fontes no APK** (`res/font`), não usar "Downloadable Fonts" — o app é offline e não deve depender de Google Play Services/rede; (2) **checar `tnum`** (números tabulares) — sem isso, colunas de valores não alinham; (3) licenças OFL: incluir os arquivos de licença.

### [D4] Navegação

| Opção | Estrutura |
|---|---|
| **4.1 Manter 3 abas** | Faturas · Cartões · Automação |
| **4.2 4 abas** (default provisório) | **Início · Pessoas · Cartões · Insights**, engrenagem no topo → Ajustes (onde mora "Captura automática") |
| **4.3 5 abas** | + Assinaturas ou + Captura |

- **Argumento para 4.2:** Automação é configuração (usada poucas vezes) e hoje ocupa uma aba; Pessoas é o centro do rateio e não tem aba. **Contra-argumento a pesar:** mudar navegação quebra memória muscular de quem já usa.

### [D5] Ações primárias

- **5.1 FAB estendido único** "Lançar compra" (hoje) com padding correto.
- **5.2 FAB Menu (M3 Expressive)** (default provisório): um FAB que abre *Lançar compra · Nova pessoa · Abater/Pix*. Remove os 2 botões do corpo da Home.
- **5.3 Ação central na bottom bar.**

### [D6] Escopo de funcionalidades

Ver Etapa 7. **Você define a ordem das ondas.** Há uma sugestão, mas é o seu tempo e o seu uso real que decidem.

### [D7] Nome do estilo

"Liquid Glass" é o nome comercial do design language da Apple (iOS 26). Usar o termo como nome do **seu** sistema (inclusive visível na UI, como no rodapé hoje) acopla você à marca de outra empresa. Sugestões de nome próprio (apenas pontos de partida): **"Obsidian Glass"**, **"Cofre"**, **"Prisma"** *(já usado em outro projeto seu — evite confusão)*. Decida o nome; o documento usa **"CC Design System"** como placeholder.

### [D8] Temas

- **8.1 Dark-only agora** (default provisório): metade do QA; coerente com o visual atual.
- **8.2 Dark + Light** desde o início: melhor uso em sol forte e para quem prefere claro; dobra o trabalho de contraste/QA.
- **8.3 + Dynamic Color (Material You):** opcional; **conflita com a Opção C/acento dinâmico** se não for desenhado junto.

---

## Etapa 5 — Design System v1

### 5.1 Arquitetura de tokens (3 camadas)

```
Primitivos (valores brutos)      →  gray-900 = #0B0B0C, space-4 = 16dp ...
Semânticos (intenção)            →  bg.base, text.secondary, status.warning ...
Componentes (uso específico)     →  card.background, meter.fill.warning ...
```

Regra de ouro: **nenhuma tela usa hex, `dp` ou `sp` crus** — só tokens semânticos. Primitivos só existem dentro do módulo do design system.

### 5.2 Cor

#### 5.2.1 Tokens dependentes da direção [D1] (dark)

| Token | **A · Ember** | **B · Obsidian Azul** | **C · Grafite** (default prov.) |
|---|---|---|---|
| `bg.base` | `#0D0B0A` | `#080C16` | `#0B0B0C` |
| `surface.1` (cards) | `#151210` | `#0F1524` | `#141416` |
| `surface.2` (selecionado/elevado) | `#1C1815` | `#151C30` | `#1B1B1E` |
| `surface.3` (sheets/menus) | `#26201C` | `#1D2742` | `#242428` |
| `text.primary` | `#F6F1EC` | `#EEF2FA` | `#F4F4F5` |
| `text.secondary` | `#BDB2A8` | `#A9B4CB` | `#B4B4BB` |
| `text.tertiary` *(≥ ~5:1 sobre `bg.base`)* | `#8F857C` | `#7C89A5` | `#8A8A93` |
| `accent` | `#E5591D` | `#38BDF8` | `#F4F4F5` (tinta) + **acento dinâmico** |
| `on-accent` | `#1A0B04` | `#04121C` | `#0B0B0C` |
| `accent.container` | accent @ 16% | accent @ 16% | acento dinâmico @ 14% |
| Glow ambiente (topo-esq.) | accent @ 12% | accent @ 14% | acento dinâmico @ 10% (ou neutro) |

> **Contraste do laranja:** `#E5591D` como **texto/ícone sobre `bg.base`** ≈ 5,3:1 (ok). **Texto branco sobre botão laranja ≈ 3,6:1 (reprova para texto normal)** → por isso `on-accent` é escuro. Valores de contraste são estimativas minhas — **confirme com uma ferramenta** (ex.: Figma plugin/Contrast checker) antes de fechar.

#### 5.2.2 Tokens semânticos (iguais nas 3 direções)

| Token | Dark | Uso |
|---|---|---|
| `status.success` | `#3DDC97` | Disponível, pago, abatido |
| `status.warning` | `#F5B93D` | Limite 70–89%, fatura fecha em ≤ 3 dias |
| `status.danger` | `#FF6B6B` | Limite ≥ 90%, vencida, erro, ação destrutiva |
| `status.info` | `#6CB6FF` | Dicas, captura ativa |
| `status.*.container` | cor @ 16% | Fundo de chips/banners |
| `outline.subtle` | branco @ 8% | Bordas de card |
| `outline.strong` | branco @ 16% | Foco, campos, divisores fortes |
| `scrim` | preto @ 60% | Fundo de sheets/dialogs |

**Regras de uso de cor**
- **Semântica nunca sozinha:** sempre ícone **ou** texto junto (daltonismo).
- **Limiares do medidor de limite** *(parâmetro de produto, ajuste)*: 0–69% neutro/ok · 70–89% `warning` · ≥ 90% `danger`.
- **Percentual:** **arredondar (half-up)** na lista; **1 casa decimal** no detalhe (ex.: 86,9%). *(Corrige P12.)*
- Valores positivos de abatimento: `+` e `success`. Valores negativos: sinal **−** (U+2212, não hífen).

#### 5.2.3 Paleta categórica (pessoas e categorias) — daltonismo-safe

Baseada em Okabe-Ito, ajustada para fundo escuro. Sempre acompanhada de **inicial/ícone** (não depender só da cor).

`#E69F00` · `#56B4E9` · `#00B386` · `#F0E442` · `#6E8BFF` · `#E8743B` · `#CC79A7` · `#A3A3AD`

Avatar de pessoa: círculo 40dp, fundo = cor @ 18%, inicial = cor @ 100%, borda 1dp cor @ 40%.

### 5.3 Tipografia

**Default provisório [D3.1]:** Display = Sora · UI/números = Inter (variável, `tnum`) · Técnico = JetBrains Mono.

| Token | Fonte | Tamanho/Linha (sp) | Peso | Tracking | Uso |
|---|---|---|---|---|---|
| `display.hero` | Sora | 44 / 48 | 600 | −1% | Valor da fatura (Home) |
| `display.lg` | Sora | 32 / 36 | 600 | −0,5% | Totais por pessoa/cartão |
| `title.lg` | Sora | 22 / 28 | 600 | 0 | Título de tela |
| `title.md` | Inter | 18 / 24 | 600 | 0 | Título de seção/card |
| `title.sm` | Inter | 16 / 22 | 600 | 0 | Nome de pessoa/cartão |
| `body.lg` | Inter | 16 / 24 | 400 | 0 | Texto corrido |
| `body.md` | Inter | 14 / 20 | 400 | 0 | Padrão de listas |
| `body.sm` | Inter | 13 / 18 | 400 | 0 | Auxiliar |
| `label.lg` | Inter | 14 / 20 | 600 | +0,1% | Botões |
| `label.md` | Inter | 12 / 16 | 600 | +2% | Chips, legendas |
| `label.caps` | Inter | 12 / 16 | 600 | **+6% (máx.)** | Sobretítulos — **no máx. 1 por seção** |
| `num.lg` | Inter `tnum` | 20 / 24 | 600 | 0 | Valores em cards |
| `num.md` | Inter `tnum` | 16 / 22 | 500 | 0 | Valores em linhas |
| `num.sm` | Inter `tnum` | 14 / 20 | 500 | 0 | Valores secundários |
| `mono.sm` | JetBrains Mono | 13 / 18 | 400 | 0 | "•••• 0359", hashes |

**Regras**
- **Mínimo 12sp.** Nada de legenda de 10–11sp.
- **Caixa-alta com tracking largo** (hoje usado em quase todo rótulo) fica restrito a `label.caps` — corrige P1/P11.
- **Dinheiro (`MoneyText`):** símbolo "R$" a ~55% do tamanho, cor `text.secondary`; parte inteira no peso do token; centavos no mesmo tamanho porém **um passo mais leve** (o app vende precisão — não encolher os centavos).
- **Alinhamento:** colunas de valores sempre **alinhadas à direita**, com `tnum`.
- **Escala de fonte do sistema:** todo layout precisa suportar **200%** sem corte (usar `weight`/`FlowRow`/quebra, nunca altura fixa em texto).
- `maxLines` + `overflow = Ellipsis` em nomes; **nunca** em valores monetários.

### 5.4 Espaçamento e grid

- **Base 4dp.** Escala: `0 · 4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48 · 64`.
- **Margem lateral:** 20dp (compacta), 24dp (média), conteúdo centralizado com **largura máx. 640dp** em telas grandes.
- **Entre seções:** 24dp · **Padding de card:** 20dp · **Item de lista:** 16dp horizontal × 12dp vertical.
- **Alvo de toque mínimo:** 48 × 48dp.
- **Insets:** edge-to-edge; `WindowInsets.systemBars` + `ime`; **conteúdo rolável com padding inferior = nav bar + FAB + 16dp** (corrige P2).

### 5.5 Formas (raios)

| Token | dp | Uso |
|---|---|---|
| `shape.xs` | 6 | Tags pequenas |
| `shape.sm` | 10 | Chips retangulares, tooltips |
| `shape.md` | 16 | Inputs, itens de lista selecionáveis |
| `shape.lg` | 24 | **Cards** |
| `shape.xl` | 32 | Bottom sheets (cantos superiores), hero |
| `shape.full` | 999 | Botões, chips, avatares, nav indicator |

Opcional (M3 Expressive): **morph de forma** em botões ao pressionar (pílula → retângulo arredondado).

### 5.6 Superfícies, elevação e glass

Em tema escuro, **sombra não comunica elevação**; usamos degraus de superfície + borda.

| Camada | Token | Receita |
|---|---|---|
| L0 | Fundo | `bg.base` + glow ambiente (radial, topo-esq.) |
| L1 | Card | `surface.1` + borda `outline.subtle` 1dp |
| L2 | Elevado/selecionado | `surface.2` |
| L3 | Sheet/menu/dialog | `surface.3` + scrim |
| L4 | **Glass flutuante** | blur + tint (abaixo) — nav bar, FAB menu, snackbar |

**Receita de glass (L4) — default provisório [D2.1]**

| Parâmetro | Valor de partida |
|---|---|
| Blur | 20–24dp |
| Tint | `surface.1` @ 60–70% |
| Borda | 1dp, gradiente 135°: branco 18% → 4% |
| Brilho superior (sheen) | linha de 1dp branco @ 10% no topo |
| Ruído/grão | opcional, 3–4% (testar custo) |
| **Fallback** | "Reduzir transparência" ligado, economia de energia, ou aparelho fraco → `surface.2` @ 96% + borda, **sem blur** |

Notas técnicas: `Modifier.blur` do Compose desfoca o conteúdo do próprio composable; **backdrop blur** (desfocar o que está *atrás*) precisa de uma lib como **Haze** (`hazeSource` no conteúdo, `hazeEffect` na camada de vidro — a API muda entre versões, confira a doc da versão que usar). A própria documentação do Haze avisa que blur tem custo de frame. **Medir no pior aparelho que você tem.**

### 5.7 Iconografia e imagens

- **Material Symbols Rounded**, peso 400, grade 0, tamanho óptico 24; preenchido (`FILL=1`) só para estado selecionado.
- **Exportar como vector drawables** e embutir só os usados — evitar `material-icons-extended` inteiro (pesa no APK).
- Tamanhos: 16 (inline), 20 (chips), 24 (padrão), 32 (empty state).
- **Bancos:** não usar logos; usar **monograma** (inicial + cor do cartão). Evita problema de marca e de manutenção.
- **Ilustração de empty states:** formas geométricas simples (SVG/vector), 1–2 cores, sem biblioteca externa.

### 5.8 Movimento

| Token | Valor |
|---|---|
| `dur.short` | 100–150ms (press, toggle) |
| `dur.medium` | 250–350ms (entrada de item, sheet) |
| `dur.long` | 500–600ms (contagem de valores, transições de tela) |
| `ease.standard` | cubic-bezier(0.2, 0, 0, 1) |
| `ease.emphasized.decel` | cubic-bezier(0.05, 0.7, 0.1, 1) |
| `ease.emphasized.accel` | cubic-bezier(0.3, 0, 0.8, 0.15) |
| `spring.spatial` | amortecimento ~0,8 · rigidez ~380 *(ponto de partida; ajuste no "feeling")* |
| `spring.effects` | amortecimento 1,0 · rigidez ~1600 (sem "pulo") |

**Padrões de movimento**
- **Contagem animada** do valor da hero ao trocar de mês/fatura (≤ 600ms). Tabular-nums evita "tremedeira".
- **Barra de limite** anima do valor anterior ao novo (não reinicia do zero).
- **Entrada de lista:** stagger de 30ms, máx. 6 itens.
- **Card → detalhe:** *shared element transition* (cartão/pessoa).
- **Pressionar:** escala 0,98 em 100ms.
- **Háptico:** confirmar ao salvar, rejeitar em erro (tipos `Confirm`/`Reject` do Compose existem em versões recentes — **confirme na sua versão**).
- **Reduzir movimento:** se a escala de animação do sistema = 0 ou opção do app ligada → sem mola/contagem; só fade curto.

### 5.9 Acessibilidade (tokens e regras)

- **Contraste:** texto ≥ 4,5:1; texto grande (≥ 18sp/14sp bold) e componentes de UI ≥ 3:1. Sobre **glass**, medir com o *pior caso* de conteúdo atrás.
- **Estado por mais de cor:** ícone + texto em status.
- **TalkBack:** `MoneyText` lê "seiscentos e trinta e seis reais e treze centavos" (ou, mínimo, "R$ 636,13"); medidor lê "86,9% do limite usado, restam R$ 636,13"; botões com `contentDescription`.
- **Foco/teclado:** anel de foco 2dp `outline.strong`; ordem lógica de foco.
- **Modo discreto:** oculta todos os valores (`R$ ••••`), útil em público; opcional `FLAG_SECURE` para ocultar na tela de recentes.

### 5.10 Catálogo de componentes

Cada componente deve existir no **catálogo de componentes** (tela debug) com todos os estados.

**Botões**
| Variante | Visual | Uso |
|---|---|---|
| `Primary` | Fundo `accent` (C: tinta), texto `on-accent`, altura 52dp, `shape.full` | **1 por tela** |
| `Tonal` | `surface.2` + texto `text.primary` | Ações secundárias fortes |
| `Outline` | Borda `outline.strong` | Secundárias |
| `Text` | Sem fundo, `accent`/`text.primary` | Terciárias |
| `Destructive` | `danger.container` + texto `danger` | **Sempre com confirmação** |
Estados: default · pressed · focused · disabled (38% opacidade) · loading (spinner substitui rótulo, largura fixa).

**Campos de texto**
- **Rótulo flutuante** (não só placeholder) → corrige P6. Altura 56dp, `shape.md`, borda `outline.subtle`, foco `outline.strong` + 2dp.
- Texto de ajuda abaixo; erro com ícone + texto `danger`.
- Variantes: texto, **moeda** (teclado numérico, máscara em centavos), senha (olho com label "mostrar/ocultar"), data.
- Rótulos longos quebram **abaixo** do campo, nunca dentro dele.

**HeroBalanceCard (fatura)**
- Anatomia: sobretítulo (`label.caps`) · **valor `display.hero`** · linha de status (chip: Aberta/Fecha em X dias/Fechada/Paga) · medidor · 2 ações secundárias.
- Variações: normal · vazio (CTA "Lançar primeira compra") · modo discreto · fatura paga (estado "tudo certo").

**LimitMeter**
- Barra 8dp, `shape.full`, trilho `surface.3`, preenchimento por limiar (5.2.2). Rótulos **fora** da barra: esquerda "Usado X%" / direita "Livre R$ …". Animação 5.8.

**PersonCard / PersonRow**
- Avatar + nome + **saldo** (compras − abatimentos) + mini-medidor do sublimite + chip de status (pendente/parcial/quitado). Toque → detalhe. Ação rápida (swipe ou ⋮): "Cobrar no WhatsApp", "Abater".

**CardTile (cartão como objeto)**
- Proporção 1,586:1 (cartão real), `shape.lg`, gradiente próprio derivado da **cor do cartão** (2 tons, 6% de ruído), monograma do banco, "•••• 0359" em `mono.sm`, "Fecha dia 9 · Vence dia 15", e o **LimitMeter** na base. Carrossel horizontal com *peek*; toque abre detalhe. Editar/Excluir ficam no detalhe (⋮), **não** na lista → corrige P10.

**TransactionRow**
- Ícone de categoria (40dp) · loja (`body.md`, 1 linha) · pessoa + parcela ("3/6 · Jheymison") em `body.sm`/`text.secondary` · valor à direita (`num.md`). Selo "capturado" (ícone de sino) vs "manual". Swipe: editar / excluir (com *undo* em snackbar).

**ReviewBanner / ReviewCard**
- Banner "N compras para revisar" (`status.info.container`). Card de revisão: loja detectada (editável), valor, parcelas, cartão sugerido, pessoa sugerida; ações **Confirmar** (Primary) · **Editar** · **Descartar**.

**Chip**
- Altura 32dp, `shape.full`; tipos: filtro, status, categoria. Ícone opcional 16dp.

**BottomNav (glass L4)**
- 4 destinos, ícone 24 + label `label.md`; indicador em pílula `surface.3`; badge numérico (revisões pendentes) no ícone de Início/Revisão.

**FAB Menu**
- FAB principal (`+`) → expande 3 ações (Lançar compra · Nova pessoa · Abater/Pix) em glass; scrim leve; fecha com tap fora/voltar.

**BottomSheet "Lançar compra"**
- Altura ~90%, arrasta para fechar. Ordem de campos: **valor** (foco automático, teclado numérico, fonte `display.lg`) → loja → cartão (chips) → pessoa (avatares) → parcelas (stepper 1–24 com **prévia: "6 × R$ 33,34 / 33,33…"**) → data → categoria → botão **Salvar** fixo no rodapé.

**Dialog de confirmação (destrutivo)**
- Título direto ("Excluir cartão Inter?"), consequência explícita ("12 compras serão removidas"), botões `Cancelar` (Text) + `Excluir` (Destructive).

**Snackbar com Desfazer**
- Glass L4, 4s; **toda exclusão** é desfazível.

**EmptyState**
- Ilustração geométrica 96dp · título curto · 1 linha de explicação · **1 CTA**. Texto específico por tela (ver Etapa 8).

**PermissionStep (Captura automática)**
- Stepper de 3 passos com estado: ○ pendente · ● ativo · ✓ concluído. Cada passo tem título, 1 linha de explicação, botão e **status em tempo real** (permissão concedida?).

**Selo "No aparelho"**
- Chip discreto (ícone de cadeado + "100% no aparelho") na Home e Ajustes; toque abre explicação "Seus dados nunca saem daqui" + o que o app faz/não faz. É a identidade tornada visível.

---

## Etapa 6 — Arquitetura de informação e telas

### 6.1 Mapa (default provisório [D4.2])

```
Início ─┬─ Detalhe da fatura/mês
        ├─ Revisão de capturas
        └─ Lançar compra (sheet)
Pessoas ─┬─ Detalhe da pessoa (conta corrente) ─ Compartilhar rateio (PDF/WhatsApp)
         └─ Nova pessoa
Cartões ─┬─ Detalhe do cartão ─ Editar/Excluir
         └─ Novo cartão
Insights ─ Por categoria · Por pessoa · Parcelas futuras · Assinaturas
Ajustes (engrenagem) ─ Captura automática · Alertas por e-mail · Privacidade/Segurança ·
                       Backup · Aparência (tema, reduzir transparência/movimento) · Sobre
```

### 6.2 Wireframes (360 × 800 dp — estrutura, não estilo final)

**S1 · Início**
```
┌────────────────────────────────┐
│ ◂  Outubro 2026  ▸     👁   ⚙  │  month switcher + modo discreto + ajustes
├────────────────────────────────┤
│ FATURA DE OUTUBRO    [ Aberta ]│
│ R$ 2.340,18                    │  display.hero
│ Fecha em 4 dias · vence dia 15 │
│ ▓▓▓▓▓▓▓▓▓░░░  Usado 72% · Livre│
│                      R$ 1.360  │
│ [Abater/Pix]   [Compartilhar]  │  secundários (Tonal)
├────────────────────────────────┤
│ 🔔 3 compras para revisar   ›  │  ReviewBanner
├────────────────────────────────┤
│ Pessoas                Ver todas│
│ (J) Jheymison   R$ 1.210,40  ▓▓│
│ (M) Maria       R$   640,10  ▓ │
├────────────────────────────────┤
│ Próximos 6 meses (parcelas)    │  mini barras
│ ▂ ▅ ▇ ▅ ▃ ▁                    │
├────────────────────────────────┤
│ Assinaturas   R$ 189/mês · 4%  │
│ Últimas compras …              │
└────────────────────────────────┘
   [ Início  Pessoas  Cartões  Insights ]   +  (FAB menu)
```
- Estado **vazio:** hero vira "Comece lançando sua primeira compra" com CTA Primary + selo "No aparelho".
- Estado **fatura paga:** hero em `success.container` com "Tudo certo em outubro ✓".

**S2 · Lançar compra (sheet)**
```
│ ───                             │
│ Nova compra                     │
│ R$ 0,00                         │  display.lg, foco automático
│ Loja  [________________]        │
│ Cartão  (Inter ••0359) (+)      │  chips
│ Quem   (J) (M) (+)              │  avatares
│ Parcelas  [ − ]  6x  [ + ]      │
│  → 6 × R$ 33,34/33,33 (soma ✓)  │  prévia exata
│ Data  Hoje ▾    Categoria ▾     │
│ [         Salvar compra       ] │  Primary fixo
```

**S3 · Detalhe da pessoa (conta corrente)**
```
│ ◂  Jheymison           ⋮        │
│ Deve este mês                   │
│ R$ 1.210,40   [ Parcial ]       │
│ ▓▓▓▓▓▓░░  sublimite R$ 2.000    │
│ [Cobrar no WhatsApp] [Abater]   │
│ ── Timeline ──                  │
│ 12 out  Mercado X      3/6  −… │
│ 10 out  Pix recebido        +… │
```

**S4 · Cartões**
```
│ Cartões                    (+)  │
│ ┌───────────────────────────┐   │
│ │ [IN]  Inter     •••• 0359 │   │ CardTile (peek do próximo)
│ │ Fecha 9 · Vence 15        │   │
│ │ ▓▓▓▓▓▓▓▓▓░ 86,9%          │   │
│ │ Livre R$ 636,13           │   │
│ └───────────────────────────┘   │
│ Resumo do cartão (toque abre)   │
```

**S5 · Revisão de capturas**
```
│ ◂ Revisar (3)                   │
│ ┌ Compra capturada ──────────┐  │
│ │ "IFOOD *RESTAURANTE"       │  │ origem (texto bruto, colapsável)
│ │ Loja: [iFood Restaurante]  │  │ editável
│ │ R$ 54,90 · 1x · Inter ••0359│ │
│ │ Pessoa sugerida: (J)       │  │
│ │ [Descartar] [Editar] [Confirmar]│
```

**S6 · Insights** — abas segmentadas: *Categorias* (rosca/barra segmentada + lista) · *Pessoas* · *Parcelas futuras* (linha do tempo de 12 meses: "o limite libera R$ X em março") · *Assinaturas*.

**S7 · Ajustes › Captura automática** (substitui a aba "Automação")
```
│ ◂ Captura automática            │
│ ● Ativa — 14 compras capturadas │  status em tempo real
│ 1 ✓ Permissão de notificações   │
│ 2 ● Escolha os apps de banco    │  checklist (Nubank, Inter, …)
│ 3 ○ Alertas por e-mail (opcional)│
│   Gmail remetente  [__________] │  rótulo flutuante
│   Senha de app     [••••••] 👁  │  + "Como gerar?" (link)
│   [ Enviar e-mail de teste ]    │
│ ▸ Detalhes técnicos (Keystore, TLS)│  colapsável — corrige P7
```

**S8 · Onboarding (3 telas)**
1. "Seus cartões, só seus." — selo No aparelho + 3 bullets (sem nuvem · centavos exatos · captura automática).
2. Adicionar 1º cartão (campos mínimos).
3. Captura automática (opcional, pode pular).

**S9 · Compartilhar rateio** — pré-visualização do PDF/mensagem do WhatsApp (fatura formatada), toggles do que incluir, botão "Compartilhar".

---

## Etapa 7 — Novas funcionalidades e priorização

> **[D6]: a ordem é sua.** A coluna "Valor" é uma estimativa minha de impacto no uso diário; "Esforço" (P/M/G) assume você sozinho. Marque o que entra em cada onda.

| # | Funcionalidade | Por quê | Valor | Esforço | Risco/Obs. |
|---|---|---|---|---|---|
| F1 | **Fila de revisão de capturas** | Parser por Regex errará; sem revisão, erro vira dado errado silencioso | Alto | M | Nova tabela Room + migração |
| F2 | **Status de fatura** (aberta · fecha em X dias · fechada · paga) + lembretes locais de fechamento/vencimento | É a 1ª pergunta do usuário todo mês | Alto | M | WorkManager; permissão POST_NOTIFICATIONS (Android 13+) |
| F3 | **Pessoa como conta corrente** (timeline, saldo, status pendente/parcial/quitado) | Rateio é o diferencial | Alto | M | Reaproveita dados atuais |
| F4 | **Modo discreto + bloqueio biométrico** (BiometricPrompt) + `FLAG_SECURE` opcional | Combina com a promessa de privacidade | Alto | P | Definir fallback sem biometria |
| F5 | **Backup/Exportação criptografada** (arquivo escolhido via SAF; senha do usuário; AES-GCM) + importação | `allowBackup=false` + offline = risco de perda total | **Crítico** | M | Cuidado com derivação de chave (KDF); testar restore |
| F6 | **Projeção de parcelas** ("quando o limite libera", 12 meses) | Responde "posso comprar isso?" | Alto | M | Cálculo já existe no repositório |
| F7 | **Simulador "e se eu parcelar em N×?"** | Mostra o diferencial de centavos exatos | Médio | P | Somente leitura |
| F8 | **Categorias** (padrão + editáveis) com auto-categorização local por palavras-chave | Habilita Insights | Alto | M | Regras locais, sem IA/nuvem |
| F9 | **Insights** (categoria, pessoa, evolução) | Retém uso | Médio | M | Depende de F8 |
| F10 | **Widget (Glance)**: fatura do mês + limite livre; atalho "Lançar compra" | Abertura diária sem abrir o app | Médio | M | Respeitar modo discreto |
| F11 | **Quick Settings tile / App Shortcut** "Lançar compra" | Velocidade | Médio | P | — |
| F12 | **Assinaturas**: próxima cobrança, total mensal, marcar "vou cancelar" | Já detecta; falta ação | Médio | P | Ajustar detecção ~30 dias |
| F13 | **Layout adaptativo** (rail em tablets/dobráveis) | Cobertura | Baixo | M | Só depois do core |
| F14 | **Importar fatura (CSV/PDF)** para conferência | Reconciliar captura vs. fatura real | Médio | G | Complexidade de parsing |

**Sugestão de ondas (default provisório — troque):**
- **Onda 1 (fundação de confiança):** F5 (backup), F1 (revisão), F2 (status/lembretes), F4 (discreto).
- **Onda 2 (rateio):** F3, F6, F7.
- **Onda 3 (dados):** F8, F9, F12.
- **Onda 4 (alcance):** F10, F11, F13, F14.

> Cada onda precisa deixar o app **utilizável e estável** — sem "meio-feito" no meio.

---

## Etapa 8 — Microcopy e tom de voz

**Princípios:** "você", frases curtas, verbo no começo do botão, benefício antes do mecanismo, jargão só em "Detalhes técnicos".

| Hoje | Proposta |
|---|---|
| "Captura em Tempo Real & Cofre Keystore" | "Captura automática de compras" |
| "O CardControl escuta compras emitidas pelos apps bancários…" | "Quando seu banco avisar de uma compra, o CardControl registra para você. Nada sai do seu celular." |
| "Conceder Permissão às Notificações" | "Permitir leitura de notificações" |
| "Criptografar no Android Keystore" | "Guardar com segurança" *(+ colapsável "Detalhes técnicos")* |
| "Gmail Remetente" | "Seu Gmail (quem envia o alerta)" |
| "Senha de App Google" | "Senha de app do Google" + link "Como gerar?" |
| "Código de 16 caracteres gerado na sua Conta Google" | "São 16 letras, geradas na sua Conta Google. Não é sua senha normal." |
| "Sem fatura pendente" | "Nenhuma fatura em aberto este mês" |
| "Nenhuma assinatura detectada ainda…" | "Ainda não vimos nenhuma cobrança recorrente. Quando algo se repetir a cada ~30 dias, aparece aqui." |
| "Abater / Pix" | "Registrar pagamento" |
| Rodapé "Motor Offline • Liquid Glass" | **Remover**; trocar por selo "100% no aparelho" |

**Erros e confirmações**
- Erro: causa + o que fazer ("Não foi possível enviar o e-mail. Verifique a senha de app e tente de novo.").
- Destrutivo: nomeia o objeto e a consequência ("Excluir o cartão Inter? As 12 compras ligadas a ele também serão removidas.").
- Sucesso: curto e silencioso (snackbar + háptico), sem confete.

---

## Etapa 9 — Plano de implementação em Compose

### 9.1 Fases

| Fase | Objetivo | Entregáveis | Definição de pronto (DoD) |
|---|---|---|---|
| **0 · Rede de segurança** | Não quebrar o que funciona | Tag `v2.x-pre-redesign`; branch `redesign/v3`; **testes de screenshot** (Roborazzi ou Paparazzi) das telas atuais; auditoria `grep` de `Color(0x`, `fontSize`, `.dp` soltos; lista de aparelhos de teste (incluir o mais fraco) | Golden images commitadas; lista de hardcodes |
| **1 · Fundação** | Tokens + tema + fontes | Pacote/módulo `designsystem`: `CCColors`, `CCType`, `CCSpace`, `CCShapes`, `CCMotion`; fontes em `res/font`; `CCTheme` mapeando para `MaterialTheme`; `MoneyText` | Nenhuma tela nova usa hex/dp cru; previews em fonte 100/200% |
| **2 · Componentes** | Biblioteca viva | Todos os componentes da 5.10 + tela **Catálogo** (debug) com estados | Cada componente tem preview, estados e teste de screenshot |
| **3 · Telas** | Migrar tela por tela | Ordem sugerida: Início → Lançar compra → Cartões → Pessoa → Ajustes/Captura | Telas antigas removidas só quando a nova passar QA |
| **4 · Funcionalidades** | Ondas (Etapa 7) | Migrações Room v5+ **com testes de migração** (`MigrationTestHelper`) | Migração testada com dados reais anonimizados |
| **5 · Polimento** | Motion, háptico, a11y, performance | Springs, contagem, shared element; TalkBack; **Macrobenchmark + JankStats**; Baseline Profile | p95 de frame dentro da meta (Etapa 10) |
| **6 · Release** | Publicar com segurança | CHANGELOG; notas de versão; versionamento (sugestão: **3.0.0**); checar plano de assinatura/atualização já descrito no README | Release em rascunho, revisada |

**Boas práticas:** um PR por componente/tela; `@Preview` com `@PreviewLightDark`, `@PreviewFontScale`, `@PreviewScreenSizes`; **nunca** misturar refator visual com mudança de regra de negócio no mesmo PR.

### 9.2 Esqueleto de tema (Kotlin)

```kotlin
@Immutable
class CCColors(
    val bgBase: Color, val surface1: Color, val surface2: Color, val surface3: Color,
    val outlineSubtle: Color, val outlineStrong: Color,
    val textPrimary: Color, val textSecondary: Color, val textTertiary: Color,
    val accent: Color, val onAccent: Color, val accentContainer: Color,
    val success: Color, val warning: Color, val danger: Color, val info: Color,
)

val LocalCCColors = staticCompositionLocalOf<CCColors> { error("CCTheme ausente") }

object CCTheme {
    val colors: CCColors
        @Composable @ReadOnlyComposable get() = LocalCCColors.current
    // val type, val space, val shapes, val motion — mesmo padrão
}

@Composable
fun CCTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colors = if (darkTheme) GrafiteDark /* ou EmberDark / AzulDark [D1] */ else GrafiteLight
    CompositionLocalProvider(LocalCCColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(), // componentes M3 herdam os tokens
            typography = ccMaterialTypography(),
            shapes = ccMaterialShapes(),
            content = content,
        )
    }
}
```

### 9.3 Fontes e números tabulares

```kotlin
val Inter = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter_variable, FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

val NumMd = TextStyle(
    fontFamily = Inter, fontSize = 16.sp, lineHeight = 22.sp,
    fontWeight = FontWeight.Medium,
    fontFeatureSettings = "tnum",
)
```
*(Fontes variáveis exigem API 26+, que é o seu minSdk.)*

### 9.4 `MoneyText` (centavos como `Long`)

```kotlin
private val brInt = NumberFormat.getIntegerInstance(Locale("pt", "BR"))

@Composable
fun MoneyText(cents: Long, style: TextStyle, modifier: Modifier = Modifier, hidden: Boolean = false) {
    val abs = kotlin.math.abs(cents)
    val text = buildAnnotatedString {
        if (hidden) { append("R$ ••••"); return@buildAnnotatedString }
        if (cents < 0) append("−")                       // U+2212
        withStyle(SpanStyle(fontSize = style.fontSize * 0.55f, color = CCTheme.colors.textSecondary)) { append("R$ ") }
        append(brInt.format(abs / 100)); append(",")
        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append((abs % 100).toString().padStart(2, '0')) }
    }
    Text(text, style = style, modifier = modifier.semantics { /* contentDescription legível */ })
}
```
> Regra: **a UI nunca recebe `Double`/`Float` para dinheiro.** Formatação só na borda.

### 9.5 Glass com Haze (conceito)

```kotlin
val haze = rememberHazeState()
Box {
    LazyColumn(Modifier.hazeSource(haze)) { /* conteúdo que rola */ }
    GlassNavBar(Modifier.align(Alignment.BottomCenter).ccGlass(haze, reduceTransparency))
}

fun Modifier.ccGlass(state: HazeState, reduce: Boolean) =
    if (reduce) this.background(CCTheme.colors.surface2.copy(alpha = .96f)).border(1.dp, outline)
    else this.hazeEffect(state) { blurEffect { blurRadius = 22.dp } } /* + tint + borda gradiente */
```
> Confirme os nomes na versão do Haze que adotar (a API mudou entre 1.x e 2.x).

### 9.6 Cuidados de refatoração específicos do app

- **Parser/valores:** nenhuma mudança visual pode tocar `BankNotificationParser`, `CardRepository` ou cálculo de parcelas; testes existentes (`SecurityAndDomainTestHarness`) rodam em todo PR.
- **Percentual do limite:** extrair para função de domínio testada (half-up; 1 casa no detalhe) → corrige P12.
- **Room:** qualquer tabela nova (categorias, fila de revisão, preferências) → **versão do schema + migração + teste**. Schemas já são exportados em `app/schemas/`.
- **Estado de UI:** ViewModel expõe `UiState` imutável por tela; sem lógica de formatação dentro de Composable de tela.
- **Performance:** `LazyColumn` com `key` estável; evitar recomposição por `Modifier` instável; medir antes/depois.

---

## Etapa 10 — QA, acessibilidade e métricas

### 10.1 Critérios de aceite do redesign

| Área | Critério |
|---|---|
| Bugs do diagnóstico | P1, P2, P12 **corrigidos e cobertos por teste de screenshot/unidade** |
| Contraste | Todo texto ≥ 4,5:1; UI ≥ 3:1 (medido, inclusive sobre glass) |
| Escala de fonte | 200% sem corte/sobreposição nas 5 telas principais |
| Toque | Todo alvo ≥ 48dp |
| TalkBack | Fluxos "ver fatura" e "lançar compra" completos só com leitor de tela |
| Daltonismo | Status compreensível em simulação de protanopia/deuteranopia |
| Performance | Sem *jank* perceptível em rolagem da Home; **meta inicial** p95 de frame < 16,6ms no aparelho de referência *(defina o aparelho)* |
| Bateria/GPU | Glass desligável; sem blur contínuo fora de tela |
| Offline | Todas as telas funcionam em modo avião |
| Privacidade | Nenhuma permissão nova sem justificativa na UI; nenhum SDK de analytics/anúncio |

### 10.2 Métricas de produto *(medidas por você, localmente — sem telemetria)*

- Tempo para lançar uma compra manual: **≤ 5s**.
- Taxa de capturas confirmadas sem edição (qualidade do parser) — contador local na tela de revisão.
- Nº de compras descartadas na revisão (sinal de regex fraca por banco).

### 10.3 Matriz de testes

Aparelhos: 1 fraco (≤ 3GB RAM), 1 médio, 1 emulador grande/dobrável · Android 8 (API 26) · 12 · 14/15 · fonte 100/150/200% · tema (se D8≠8.1) · reduzir movimento · reduzir transparência · idioma pt-BR.

---

## Etapa 11 — Stress test do próprio plano

Pontos fracos que eu apontaria se este plano fosse de outra pessoa — **confira cada um antes de começar**:

1. **Redesenhar antes de ter rede de segurança visual.** Sem screenshot tests (Fase 0), você não sabe se quebrou algo. É a fase mais "chata" e a mais importante.
2. **Glass é a decisão mais cara com menor retorno comprovado.** Em aparelhos intermediários, blur custa frame; em tela com fundo chapado, não há o que desfocar. Se o teste de performance reprovar, **[D2.3] sem glass** ainda é um bom produto.
3. **Acento laranja (Ember) colide com a semântica de risco e com a cor de um banco real.** Se escolher A, defina regra explícita de separação (accent só em elementos interativos; status sempre com ícone + texto).
4. **Mais pixels não consertam parser errado.** O que realmente faz o usuário abandonar app de captura é dado errado. Por isso F1 (revisão) vem antes de qualquer gráfico bonito.
5. **Offline-first sem backup é uma aposta.** O diferencial de privacidade só vale se perder o celular não destruir anos de histórico. F5 não é "nice to have".
6. **Escopo para uma pessoa só.** 14 funcionalidades + redesign total é meses. Se travar, o que você corta primeiro? Defina isso **antes**, não no meio.
7. **Mudar navegação [D4.2] afeta quem já usa.** Se hoje o público é você + poucas pessoas, tudo bem; se há outros usuários, considere manter atalhos/rotas equivalentes e comunicar na versão.
8. **Fontes:** Sora + Inter + JetBrains Mono somam peso de APK e três arquivos para manter. Se o objetivo é leveza, **[D3.2] Inter única** entrega 90% do resultado.
9. **Contraste sobre glass é imprevisível.** O pior caso é texto terciário sobre conteúdo claro passando por baixo. Teste com conteúdo "ruim" de propósito.
10. **Distribuição:** hoje via GitHub Releases. Se um dia for à Play Store, revise as políticas aplicáveis (formulário de segurança de dados, uso de leitura de notificações e apps financeiros) **antes** de investir em polimento — não afirmo regras específicas aqui; verifique na documentação atual do Google Play.
11. **Percentual 86% vs 86,9%:** pequeno, mas num produto cujo discurso é "centavo exato" pequenas inconsistências corroem a confiança mais que bugs grandes.
12. **O que eu não vi:** só tive 4 telas e o README. Não li o código Compose. Há telas (lançar compra, pessoa, PDF/WhatsApp) que desenhei **sem ver a versão atual** — compare com o que você já tem.

---

## Anexo A — Prompts prontos para o Claude Design

> Dica: rode **um prompt por tela** e peça sempre as 3 direções de cor [D1] lado a lado antes de aprofundar.

### A.1 Prompt mestre (cole primeiro; reutilize como contexto)

```
Você é designer de produto sênior. Projete o app Android "CardControl": controle de
faturas de cartão de crédito e rateio entre pessoas, 100% offline, sem nuvem, com captura
automática de compras via notificações dos apps de banco. Usuário: brasileiro (pt-BR),
quer precisão de centavos e privacidade.

PLATAFORMA: Android, Jetpack Compose + Material 3 (Expressive). Frame 360×800dp.
Tudo que você projetar precisa ser implementável em Compose (blur de fundo = Haze; sem
mix-blend-mode, sem filtros SVG exóticos).

NORTE: "Controle de cartão que você pode conferir centavo por centavo — e que nunca sai do seu celular."
PRINCÍPIOS: cor é significado; um primário por tela; valor primeiro; calma financeira;
glass só em camadas flutuantes sobre conteúdo que rola; acessível (contraste 4.5:1,
fonte 200%, estado nunca só por cor).

DESIGN SYSTEM: use exatamente os tokens do anexo B (cores, tipografia, espaçamento,
formas, movimento). Direção de cor: [A Ember | B Obsidian Azul | C Grafite].
Tipografia: Sora (display), Inter (UI/números, tabular-nums), JetBrains Mono (técnico).
Dinheiro: "R$" a 55% do tamanho, centavos mais leves, alinhado à direita, tabular.

ENTREGUE: frames Home (normal, vazio, fatura paga, modo discreto), componentes com
estados, e notas de interação/movimento. Liste dúvidas no final em vez de inventar.
NÃO: logos de banco, confete, gradientes arco-íris, texto <12sp, alertas vermelhos sem causa.
```

### A.2 Prompts por tela (anexar ao mestre)

- **Home:** "Projete a tela Início conforme wireframe S1: top bar com seletor de mês, modo discreto e ajustes; hero da fatura com status (aberta/fecha em X dias/paga), medidor de limite e 2 ações secundárias; banner de revisão; lista de pessoas; mini-gráfico de parcelas dos próximos 6 meses; assinaturas; últimas compras. Bottom nav em glass com 4 destinos e FAB menu."
- **Lançar compra:** "Bottom sheet 90% conforme S2. Foco no valor com teclado numérico; prévia exata de parcelas ('6 × R$ 33,34 / 33,33…, soma confere'). Botão Salvar fixo."
- **Pessoa:** "Detalhe da pessoa como conta corrente (S3) com timeline de compras e abatimentos, saldo, status e ações 'Cobrar no WhatsApp' e 'Abater'."
- **Cartões:** "Galeria de CardTile (S4), proporção de cartão real, gradiente derivado da cor do cartão, monograma (sem logo), medidor de limite. Editar/excluir só no detalhe."
- **Revisão:** "Fila de capturas (S5) com texto bruto colapsável, campos editáveis e ações Confirmar/Editar/Descartar; estados vazio e 'tudo revisado'."
- **Captura automática:** "Fluxo em 3 passos (S7) com status ao vivo da permissão, seleção de apps de banco, e-mail opcional com rótulos flutuantes e botão 'Enviar e-mail de teste'; detalhes técnicos em seção colapsável."
- **Insights:** "Abas Categorias/Pessoas/Parcelas futuras/Assinaturas. Gráficos sem eixo desnecessário, valores tabulares, cores da paleta categórica com ícone."
- **Onboarding:** "3 telas (S8) com o selo 'No aparelho' e tom humano."

### A.3 Perguntas para o Claude Design te devolver

1. Em qual das 3 direções a Home comunica melhor "calma + precisão"?
2. Onde o contraste do texto terciário fica mais fraco?
3. Que elementos você cortaria da Home para ter **um** primário claro?
4. O que no protótipo **não** é implementável em Compose sem custo alto?

---

## Anexo B — Cheat sheet de tokens (CSS vars para protótipo)

> Para o Claude Design/HTML. Em Compose os nomes viram campos de `CCColors`. Troque o atributo `data-direction` para alternar a direção [D1].

```css
:root{
  /* semânticos (iguais nas 3 direções) */
  --success:#3DDC97; --warning:#F5B93D; --danger:#FF6B6B; --info:#6CB6FF;
  --outline-subtle:rgba(255,255,255,.08); --outline-strong:rgba(255,255,255,.16);
  --scrim:rgba(0,0,0,.6);

  /* espaço (4dp) */
  --s1:4px; --s2:8px; --s3:12px; --s4:16px; --s5:20px; --s6:24px; --s8:32px; --s10:40px; --s12:48px;

  /* formas */
  --r-xs:6px; --r-sm:10px; --r-md:16px; --r-lg:24px; --r-xl:32px; --r-full:999px;

  /* tipografia */
  --font-display:'Sora',system-ui,sans-serif;
  --font-ui:'Inter',system-ui,sans-serif;
  --font-mono:'JetBrains Mono',ui-monospace,monospace;

  /* movimento */
  --ease-standard:cubic-bezier(.2,0,0,1);
  --ease-emph-decel:cubic-bezier(.05,.7,.1,1);
  --ease-emph-accel:cubic-bezier(.3,0,.8,.15);
  --dur-short:150ms; --dur-medium:300ms; --dur-long:550ms;

  /* categórica (pessoas/categorias) */
  --cat-1:#E69F00; --cat-2:#56B4E9; --cat-3:#00B386; --cat-4:#F0E442;
  --cat-5:#6E8BFF; --cat-6:#E8743B; --cat-7:#CC79A7; --cat-8:#A3A3AD;
}

/* números sempre tabulares */
.num{ font-family:var(--font-ui); font-feature-settings:"tnum"; font-variant-numeric:tabular-nums; }

/* Direção A — Ember */
[data-direction="ember"]{
  --bg-base:#0D0B0A; --surface-1:#151210; --surface-2:#1C1815; --surface-3:#26201C;
  --text-1:#F6F1EC; --text-2:#BDB2A8; --text-3:#8F857C;
  --accent:#E5591D; --on-accent:#1A0B04;
}
/* Direção B — Obsidian Azul */
[data-direction="azul"]{
  --bg-base:#080C16; --surface-1:#0F1524; --surface-2:#151C30; --surface-3:#1D2742;
  --text-1:#EEF2FA; --text-2:#A9B4CB; --text-3:#7C89A5;
  --accent:#38BDF8; --on-accent:#04121C;
}
/* Direção C — Grafite (default provisório) */
[data-direction="grafite"]{
  --bg-base:#0B0B0C; --surface-1:#141416; --surface-2:#1B1B1E; --surface-3:#242428;
  --text-1:#F4F4F5; --text-2:#B4B4BB; --text-3:#8A8A93;
  --accent:#F4F4F5; --on-accent:#0B0B0C;
  --accent-dynamic:var(--cat-2); /* troca pela cor do cartão/pessoa selecionado */
}

/* glass (L4) — usar só em nav/FAB/sheet/snackbar */
.glass{
  background:color-mix(in srgb,var(--surface-1) 65%,transparent);
  backdrop-filter:blur(22px) saturate(1.1);
  border:1px solid transparent;
  background-clip:padding-box;
  box-shadow:inset 0 1px 0 rgba(255,255,255,.10);
}
@media (prefers-reduced-transparency:reduce){
  .glass{ background:color-mix(in srgb,var(--surface-2) 96%,transparent); backdrop-filter:none; }
}
```

---

### Próximo passo sugerido (sem pressa)

1. Responda **D1–D5, D7, D8** (ao menos "default" ou "troco por…").
2. Marque as ondas de funcionalidades (Etapa 7).
3. Rode o **prompt mestre + Home** no Claude Design nas 3 direções e traga os resultados para stress-testarmos juntos.
