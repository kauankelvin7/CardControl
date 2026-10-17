# CardControl — Prompt de Execução + Design System "Vitrine"

> **Versão 2.1 · Executor: Antigravity · Material 3 Expressive · Design System Vitrine como fonte única de cores · Tema claro Petróleo & Cobre · Tema escuro Grafite**
>
> **Objetivo:** redesenhar e implementar diretamente no repositório Android do CardControl uma experiência de cartões com a qualidade visual e o movimento do Google Wallet, mantendo identidade própria: carrossel interativo, flip 3D opcional, ações reais e refatoração global de cores para que o design system Vitrine controle todas as cores da interface do aplicativo. O sistema Android determina apenas claro/escuro quando o modo Sistema estiver selecionado; cores de papel de parede/Dynamic Color não podem substituir a paleta Vitrine.
>
> **Ferramenta de execução:** Antigravity. **Não usar Claude Design nem depender de qualquer entregável externo de design.** O próprio agente pesquisa o projeto, aplica o design system descrito aqui, implementa, executa e valida no aplicativo real.
>
> **O que mudou nesta versão:** o prompt original (diretrizes de produto, segurança e aceitação) foi preservado e ampliado com (1) um **design system completo e numérico** — tokens de cor calculados e verificados por contraste, tipografia, formas, elevação, movimento e acessibilidade; (2) **especificações de cada componente e estado**; (3) **decisões de ambiguidade já resolvidas**; (4) **protocolo em fases com gates**; (5) **esqueletos de código** e **plano de verificação** mais concretos.

---

## Parte A — Direção e protocolo de execução

### A0. Como ler este documento

**Estrutura.** Parte A = direção, decisões e protocolo (obrigatória). Parte B = design system (tokens). Parte C = componentes e telas. Parte D = engenharia e código. Parte E = verificação, aceitação e relatório.

**Etiquetas usadas ao longo do texto**

| Etiqueta | Significado |
|---|---|
| `[INEGOCIÁVEL]` | Regra de segurança, honestidade funcional ou produto. Não relaxar sem evidência técnica registrada. |
| `[DECISÃO]` | Escolha de design já tomada. **Não perguntar ao usuário.** Só mudar se o código/testes provarem inviabilidade, e então registrar o motivo. |
| `[ALVO]` | Valor numérico de partida. Calibrar visualmente no app executado, respeitando a intenção e as faixas indicadas. |
| `[VERIFICAR]` | Depende do código real (modelo, navegação, versões). Confirmar antes de implementar. |
| `[CONDICIONAL]` | Só implementar se a capacidade correspondente **já existir** no domínio. |

**Ordem de precedência quando houver conflito**

1. Segurança do repositório e governança do projeto (`AGENTS.md`, `.agents/`, convenções reais).
2. Código real, testes e restrições de versão das dependências.
3. Regras `[INEGOCIÁVEL]` e honestidade funcional (nada fictício, nada de pagamento).
4. Decisões `[DECISÃO]` e intenção visual deste documento.
5. Valores `[ALVO]` (os mais flexíveis).

**Notas específicas do Antigravity**

- Use o modo de planejamento e gere os artefatos nativos: `task.md` (fases abaixo), `implementation_plan.md` (inventário, decisões D-xx, riscos) e, ao final, `walkthrough.md` com **evidências reais** (capturas/gravações). Se o ambiente exigir aprovação do plano, prossiga logo após a aprovação e **não reabra decisões já fechadas aqui**.
- Para a revisão independente (seção E3), use um segundo agente **somente leitura** no Agent Manager, quando disponível.
- Este arquivo é um **prompt de tarefa**. Não o cole inteiro em uma regra de workspace (`.agents/rules` tem limite por arquivo). Se, depois, o time quiser perenizar o design system, extraia a Parte B para uma skill ou documento de conhecimento do projeto — **não faça isso nesta tarefa** sem pedido explícito.

### A1. Missão, produto e princípios

**Propósito em uma frase:** ao abrir a tela de cartões, a pessoa entende em um segundo *quais cartões tem, qual está selecionado, de qual banco é, de quem é* e consegue chegar à ação certa em um ou dois toques.

**O que o CardControl é:** um app que **organiza e controla cartões** (banco, pessoa associada, registros relacionados). **O que ele não é:** carteira de pagamentos. Nenhum elemento visual, texto ou fluxo pode sugerir pagamento por aproximação, transferência, QR Code de pagamento ou acesso à conta bancária. `[INEGOCIÁVEL]`

**Referência conceitual:** o Google Wallet ao exibir um cartão — cartão principal dominante, vizinhos aparecendo de leve, superfícies que harmonizam com a cor do cartão, ações bem espaçadas abaixo, transições previsíveis. Reproduza a **qualidade de composição e interação**, não a identidade visual, os textos nem os recursos de pagamento.

**Conceito do design system — "Vitrine".** O cartão é um objeto exposto em uma vitrine: a tela é a luz e a superfície onde ele repousa; a cor do cartão tinge sutilmente o ambiente; o movimento descreve o que está acontecendo (selecionar, virar, remover) e nunca decora. O resultado deve parecer calmo, tátil e confiável — não "fintech agressiva".

**Princípios (usar como critério de desempate em qualquer dúvida de design)**

1. **O cartão é o herói.** Tudo o mais é coadjuvante. Se um elemento disputa atenção com o cartão selecionado, reduza-o.
2. **Honestidade funcional.** Se a capacidade não existe, o botão não existe. Dado ausente não é inventado nem substituído por exemplo.
3. **Calma financeira.** Poucos elementos, bom espaçamento, cor com propósito, ações destrutivas sempre atrás de confirmação e sem alarmismo.
4. **Legibilidade por construção.** O texto sobre o cartão tem contraste garantido pelo algoritmo de cor (B4), não por sorte do papel de parede ou da cor escolhida.
5. **Movimento que explica.** Molas curtas, reversíveis, interrompíveis; reduzidas ou trocadas por fade quando o sistema pedir.
6. **Identidade própria e fonte única de cores.** Grafite é a assinatura escura; o tema claro fixo tem personalidade própria (petróleo + cobre). A paleta Vitrine substitui as cores dinâmicas do sistema dentro do aplicativo; o papel de parede e o destaque do Android nunca redefinem as cores dos componentes.
7. **Para todos.** Acessibilidade (TalkBack, fonte ampliada, contraste, movimento reduzido, alvos de toque) é requisito de componente, não etapa final.

### A2. Decisões fechadas

**Herdadas do prompt original** `[DECISÃO]`

- Adicionar tema claro fixo **Petróleo & Cobre** e **preservar o Grafite** como opção de primeira classe. O design system Vitrine deve sobrescrever as cores que a interface do aplicativo receberia do tema/papel de parede do Android.
- Preferência **Sistema / Claro / Escuro**, persistida pelo mecanismo que o projeto já usa.
- **Carrossel** com vizinhos parcialmente visíveis, seleção sincronizada e animação contida.
- **Flip 3D opcional** com informação real e segura no verso.
- **Ações contextuais** e composição **"Ver mais" + "+"** conforme as regras deste documento.
- A **opção B já foi escolhida**: não peça ao usuário para escolher de novo entre opções de tema ou fluxo.

**Resolvidas nesta versão (eram ambíguas)** `[DECISÃO]`

| ID | Pergunta | Decisão | Por quê |
|---|---|---|---|
| D-01 | O app usa Dynamic Color do Android? | **Não.** A paleta Vitrine é a fonte única de cores no app, tanto no tema claro quanto no Grafite. O esquema dinâmico do papel de parede não é consumido nem mesclado com os tokens. | Garante identidade consistente e impede que as cores do sistema substituam o design system. |
| D-02 | Padrões de fábrica | Modo = **Sistema**. O modo Sistema acompanha somente a luminosidade claro/escuro do Android; cores Vitrine permanecem fixas em todas as versões e papéis de parede. Não mostrar controle de Dynamic Color. | Separa a preferência de luminosidade da identidade cromática do app. |
| D-03 | Usuários existentes (app era só escuro) | Se houver forma barata e confiável de detectar instalação anterior (chave de preferências preexistente, banco com dados), **migrar uma única vez para Escuro**; senão, Sistema. **Documentar a escolha.** | Evita trocar o tema de quem já usa. |
| D-04 | Dependência de APIs Expressive | A camada `Cc*` do design system **não depende** de APIs experimentais. Ver B2.3 (Nível A × Nível B). | Em `material3 1.4.0` estável as APIs Expressive ainda são internas; só a linha `1.5.0` (alpha/beta, na data deste documento) as expõe. |
| D-05 | Atualizar Compose/Material3? | **Não nesta tarefa.** Só se o projeto já estiver na linha 1.5.x. | Escopo isolado e revisável. |
| D-06 | Toque no cartão | Selecionado → vira (flip). Vizinho → rola até ele. | Evita virar um cartão que o usuário não está olhando. |
| D-07 | Flip ao trocar de cartão | O cartão que perde a seleção volta à frente (sem animação longa). | Estado previsível. |
| D-08 | Indicador de posição | Pílulas até 7 cartões; texto "n de N" acima disso; nenhum com 0 ou 1 cartão. | Orientação sem ruído. |
| D-09 | Onde ficam excluir/editar | Excluir e ações infrequentes ficam no **menu do cabeçalho do cartão**, nunca na linha de ações principais. | Hierarquia: 1–2 ações principais. |
| D-10 | Número do cartão | Só **últimos 4 dígitos**, mascarados. Nunca número completo, CVV, validade ou senha. | Privacidade e segurança. |
| D-11 | Cor do cartão | 12 predefinições + "Automática" + (se já existir seletor livre) cor personalizada, sempre passando pelo algoritmo de contraste (B4). | Legibilidade garantida. |
| D-12 | Seleção persistida? | Por **ID estável** em `SavedStateHandle`/`rememberSaveable`; **não** criar nova chave em DataStore. | Sem nova fonte de verdade. |
| D-13 | Foto/logo de banco | Nunca. Nome textual + monograma. | Marca e licenciamento. |
| D-14 | Texto da interface | Tudo em `strings.xml` (pt-BR como base; manter paridade com outros idiomas já existentes). | Padrão Android. |
| D-15 | Haptics | Opcionais e discretos; somente via `LocalHapticFeedback`, respeitando o sistema. | Não impor sensação. |
| D-16 | Mock e exemplos | Somente em `@Preview` e testes, sempre rotulados como fictícios. | Nunca dado falso em produção. |

### A3. Anti-objetivos (o que NÃO fazer)

- Entregar apenas plano, descrição, mockup isolado ou tela estática.
- Reduzir a tarefa a uma troca de cores ou a um carrossel cosmético.
- Inventar saldo, limite total, estatísticas, gráficos, "score", cashback ou qualquer seção financeira sem dados reais.
- Criar botão que pareça funcional e não faça nada; `TODO` silencioso; ação "em breve".
- Simular chip, NFC, tarja magnética, área de CVV, assinatura ou "aproximar para pagar".
- Usar logos, marcas, cartões bancários reais, nomes de bandeira com marca registrada em formato de logotipo.
- Desfoque (`blur`), vidro fosco, neon, brilho exagerado, sombras pesadas, gradientes arco-íris, texto espelhado, texto distorcido por perspectiva.
- Criar segunda implementação de `CardTile`, segunda lista de bancos, segundo mecanismo de preferências ou arquitetura paralela.
- Não refatorar lógica de negócio ou módulos sem relação com a aparência; porém, a migração das cores é global e obrigatória em toda a interface do aplicativo: todas as telas, componentes e fluxos visuais devem passar a consumir os tokens Vitrine. Não atualizar toda a pilha Compose nem mexer em CI/CD, assinatura, keystores, secrets ou release.
- Declarar teste, build, captura ou revisão que não foram realmente executados.

### A4. Protocolo de execução em fases com gates

Trabalhe como engenheiro Android sênior, designer de produto e responsável por qualidade. Cada fase termina em um **gate**: não avance sem cumpri-lo; se houver bloqueio real, registre evidências e prossiga com o que for viável.

| Fase | Entrega | Gate (evidência) |
|---|---|---|
| **F0 Descoberta** | Inventário (A5) + `implementation_plan.md` com decisões D-xx, riscos e lacunas reais | Estado do Git registrado; versões confirmadas; nenhuma edição antes disso |
| **F1 Fundação do tema** | `ThemeMode`, preferência persistida, resolução de esquema (B2), tokens `Cc*`, barras do sistema, sem flash | Testes de resolução de tema passando; app compila; tema alterna sem recriar estado indevido |
| **F2 Cartão** | `CardFace` (frente), paleta de cartão (B4), estados de dado ausente, previews | Testes de contraste das 12 paletas × 2 temas; previews claro/escuro/dinâmico/fonte 2.0 |
| **F3 Carrossel e seleção** | `CardCarousel` por ID estável, peek, indicador, cabeçalho contextual, backdrop tingido | Testes de seleção (0, 1, N; exclusão; atualização); rotação sem perder seleção |
| **F4 Ações** | Linha de ações reais, menu do cabeçalho, grupo "Ver mais" + "+" com destinos reais | Tabela de ações preenchida só com destinos existentes; nenhuma ação fictícia |
| **F5 Gerenciamento** | Lacunas reais de cadastro/edição/exclusão, formulário com pré-visualização ao vivo | Testes de CRUD e de exclusão com vínculos; migração testada se houve mudança de modelo |
| **F6 Flip e polimento de movimento** | `FlippableCard`, movimento reduzido, microinterações | Testes de flip; verificação com animações desativadas |
| **F7 Testes** | Plano E1 completo | Resultados reais registrados |
| **F8 Verificação visual** | Matriz E2 em emulador/dispositivo | Capturas reais inspecionadas; defeitos corrigidos |
| **F9 Revisão independente** | Seção E3 | Sem bloqueadores pendentes |
| **F10 Fechamento** | Seções E4–E6 | Critérios de aceitação marcados com evidência; relatório final |

**Regras de condução**

- Não pare em um plano e não peça confirmação de decisões já fechadas. Pergunte ao usuário **somente** se houver bloqueio real que o código e este documento não resolvam.
- Se houver divergência entre este prompt e o código/testes/governança, **o código vence**; adapte sem abandonar a intenção visual e funcional.
- Commits locais só se a governança do projeto os permitir ou exigir; siga as convenções reais de mensagem. **Nunca** faça push, merge, release ou altere configurações remotas sem autorização explícita.
- Registre cada decisão relevante com ID (D-xx) para o relatório final.

### A5. Descoberta e proteção do repositório — obrigatória antes de editar

Antes de escrever ou modificar código:

1. Confirme o caminho e o repositório em que está trabalhando.
2. Leia `AGENTS.md`, instruções locais e o estado canônico em `.agents/` (se presentes). **Não recrie estrutura paralela de governança** quando já existir fonte canônica.
3. Verifique branch, `HEAD`, `git status`, alterações staged/unstaged, arquivos não rastreados e commits recentes.
4. Inspecione a estrutura real: versão de Kotlin/Gradle/AGP/Compose/Material 3, catálogo de dependências, tema, navegação, tela de cartões, `CardTile`, ViewModels, modelos, repositórios e Room.
5. Examine testes existentes, problemas conhecidos, critérios de release e CI relevantes.
6. Determine o que existe e o que falta: edição/exclusão de cartões, banco, titular/pessoa associada, identificação parcial, limites e datas da fatura.
7. Registre internamente o plano e critérios objetivos e **prossiga com a execução na mesma tarefa**.

**Comandos de apoio (adapte ao projeto; prefira os documentados no repositório)**

```bash
git rev-parse --show-toplevel && git branch --show-current && git rev-parse HEAD
git status --porcelain=v1 -b && git stash list && git log --oneline -n 15
cat gradle/libs.versions.toml            # versões efetivas (BOM do Compose, material3, foundation, lifecycle)
./gradlew -q projects && ./gradlew tasks --all | head -n 80
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -Ei "material3|compose.foundation|datastore|room|splashscreen|core-ktx"
```

**Inventário obrigatório (preencha no `implementation_plan.md`)**

| # | Item | Onde procurar | Registrar |
|---|---|---|---|
| 1 | Versão efetiva de `material3`, Compose BOM, `foundation` | `libs.versions.toml`, saída do Gradle | Define o Nível A ou B (B2.3) e se `HorizontalPager` é estável |
| 2 | Tema atual e preferência de tema | pacote `ui/theme`, DataStore/prefs | Fonte de verdade existente e chaves; há flag de tema antigo? |
| 3 | `CardTile` e onde é usado | busca por `CardTile` | Reaproveitar/refatorar; nunca duplicar |
| 4 | Modelo de cartão | entidades Room/domínio | Campos reais: banco, titular, final, apelido, bandeira, cor, status, limite, fechamento, vencimento |
| 5 | Lista de bancos / seletor | enum/tabela/asset | Reutilizar; nunca criar lista divergente |
| 6 | Pessoa/titular | modelo e relacionamentos | Como associar; obrigatoriedade |
| 7 | Navegação | `NavHost`/rotas | Destinos reais para "Ver mais", detalhes, edição, cadastro |
| 8 | Formulário de cadastro/edição | telas/ViewModels | O que já existe e o que falta |
| 9 | Exclusão e relações | DAO, `@ForeignKey(onDelete=…)` | Cascata, restrição ou nulificação; impacto em compras/registros |
| 10 | Ações candidatas | fluxos de compra, pagamento, rateio | Quais existem de fato (preencher tabela C7) |
| 11 | Barras do sistema/edge-to-edge | `MainActivity`, `themes.xml` | Estado atual e `windowBackground` |
| 12 | Splash e inicialização | `installSplashScreen`? | Estratégia anti-flash possível (B2.5) |
| 13 | Testes e ferramentas | `src/test`, `src/androidTest`, screenshot tests, lint/detekt/ktlint | Comandos reais de validação |
| 14 | Integrações de notificação | parsers, filtro por pacote, deduplicação | Riscos de regressão ao mudar seleção/UI |
| 15 | Recursos de ícones | `material-icons-extended`? drawables | Ícones disponíveis (C7) |

**Segurança do trabalho** `[INEGOCIÁVEL]`

- Preserve todas as mudanças locais e arquivos não rastreados. **Nunca** use `reset --hard`, `clean`, force-push ou comandos destrutivos.
- Não sobrescreva mudanças de outra tarefa.
- Não altere assinatura de release, keystores, secrets, workflow de release ou código não relacionado.
- Se a branch atual for dedicada a release signing, hardening ou outro objetivo independente, **não misture o redesign nela**: crie/use uma branch de funcionalidade seguindo as convenções reais, sem perder alterações locais.
- Não faça merge, push, release nem mude configurações remotas sem autorização explícita e adequada.

### A6. Prioridades e cortes permitidos

Se tempo, contexto ou riscos forçarem cortes, corte **de baixo para cima**; nunca comprometa o que está em "Essencial".

| Nível | Itens |
|---|---|
| **Essencial** | Refatoração global das cores de toda a interface para tokens Vitrine; tema Sistema/Claro/Escuro + persistência; tema claro Petróleo & Cobre e Grafite preservado; barras do sistema enquanto o app está aberto coerentes com o tema ativo; carrossel com seleção por ID; cartão legível com banco/titular; ações reais; "Ver mais" + "+"; estados vazio/erro; CRUD sem lacunas fictícias; acessibilidade; testes pertinentes; verificação visual real |
| **Importante** | Backdrop tingido pela cor do cartão; indicador; pré-visualização ao vivo no formulário; flip acessível; movimento reduzido; layout adaptativo (largura/fonte) |
| **Desejável** | Paralaxe do brilho; haptics; morph de forma ao pressionar; painel duplo em telas largas; paridade total de strings em outros idiomas |

O flip nunca pode bloquear o carrossel: implemente-o isolado e testável (ver C3).


---

## Parte B — Design System "Vitrine"

> Todos os tokens vivem em **um único pacote de design system** (sugestão: `…/ui/designsystem` ou o pacote de tema já existente), com prefixo `Cc` (CardControl). Nenhuma tela usa cor, raio, duração ou espaçamento "solto": ou é um token, ou é derivado dele. Cores literais (`Color(0xFF…)`) só existem nos arquivos de tokens.

### B1. Camadas da Vitrine (vocabulário compartilhado)

| Camada | Papel | Material 3 |
|---|---|---|
| **Palco** | Fundo da tela; recebe o tingimento sutil da cor do cartão | `surface` (+ tinta B4.6) |
| **Bancada** | Agrupamentos de conteúdo (cabeçalho, listas, formulários) | `surfaceContainerLow` / `surfaceContainer` |
| **Objeto** | O cartão (frente/verso) | Paleta de cartão (B4) |
| **Controles** | Botões, chips, campos, indicador | `primary*`, `secondaryContainer`, `surfaceContainerHigh` |
| **Sobreposições** | Diálogos, menus, bottom sheets | `surfaceContainerHigh/Highest` + `scrim` |

### B2. Arquitetura de tema

#### B2.1 Matriz de modo × esquema Vitrine `[DECISÃO]`

| Modo resolvido | Esquema efetivo |
|---|---|
| Claro | `CcLightFallback`: paleta fixa **Petróleo & Cobre** (B3.1) + cores semânticas fixas (B3.3) |
| Escuro | `CcGrafite`: paleta fixa **Grafite** (B3.2) + cores semânticas fixas (B3.3) |

Regras:

- "Modo resolvido" = `ThemeMode.SYSTEM` → `isSystemInDarkTheme()`; `LIGHT` → claro; `DARK` → escuro. O sistema só decide a luminosidade quando a pessoa escolhe Sistema.
- **Nunca** usar `dynamicLightColorScheme` ou `dynamicDarkColorScheme` para compor as cores do app. A paleta do papel de parede, o destaque do sistema e preferências de Dynamic Color devem ser ignorados pela interface CardControl.
- Nunca "inverter RGB" para obter um tema do outro. Cada tema tem hierarquia própria de camadas (B6.3).
- Todas as cores usadas em componentes do app, inclusive barras de status/navegação enquanto a janela do app está ativa, devem ser resolvidas pelo design system Vitrine. Isso não altera o launcher nem outros aplicativos Android.
- Os papéis de erro (`error`, `onError`, `errorContainer`, `onErrorContainer`), os semânticos (sucesso/aviso/info) e os estados desabilitados são **fixos** em ambos os temas.

#### B2.2 Preferência e persistência

- Modelo sugerido: `ThemeMode { SYSTEM, LIGHT, DARK }` — armazenado **no mecanismo que o projeto já usa** (ex.: DataStore/preferences). **Uma única fonte de verdade**; nenhuma cópia em SharedPreferences, banco ou `rememberSaveable`. Não introduzir preferência ou toggle de Dynamic Color. Se já existir uma preferência antiga desse tipo, deixar de aplicá-la às cores e remover/migrar apenas o controle visual necessário sem apagar outras preferências.
- Se já existir flag de tema (ex.: booleano "escuro"), **migre-a** para `ThemeMode` sem perder a escolha atual do usuário (decisão D-03).
- A leitura é um `Flow` coletado no topo da árvore (Activity/raiz do app); o tema é aplicado por um único `CardControlTheme(prefs) { … }`.

#### B2.3 Nível A × Nível B (dependência de APIs Expressive) `[VERIFICAR]` `[DECISÃO]`

Confirme a versão efetiva de `androidx.compose.material3` (A5, item 1):

- **Nível A — `material3 ≤ 1.4.x` estável (caso mais provável):** as APIs Expressive (`MaterialExpressiveTheme`, `MotionScheme`, tipos "emphasized", `MaterialShapes`, `ButtonGroup`, `SplitButtonLayout`, etc.) **não estão públicas**. Implemente a **linguagem Expressive** (formas generosas, molas, hierarquia de tamanho, morph por pressão) com **a camada própria `Cc*`** sobre `MaterialTheme`, `Surface`, `Button`, `IconButton` estáveis. Não use `@OptIn(ExperimentalMaterial3ExpressiveApi)` nem reflexão para "forçar" APIs internas.
- **Nível B — projeto já na linha `1.5.x`:** pode usar `MaterialExpressiveTheme`, `MaterialTheme.motionScheme` e componentes Expressive já estáveis **dentro do pacote do design system**, mapeando-os para os tokens `Cc*`. APIs que ainda exijam opt-in (por exemplo `MaterialShapes`/`LoadingIndicator`, conforme a versão) só entram se a manutenção justificar — registre a decisão.
- **Em ambos os níveis:** nenhuma tela chama diretamente API experimental; elas consomem `CcMotion`, `CcShapes`, `CcType`, `CcSpace`. Assim, migrar para a API oficial depois é uma troca de **um** arquivo.
- **Não atualizar** a pilha Compose/Material3 para obter forma decorativa (D-05).

#### B2.4 Composição do tema

`CardControlTheme(prefs)` resolve o esquema (B2.1) e provê:

- `MaterialTheme(colorScheme, typography = CcTypography, shapes = CcShapes)` (ou `MaterialExpressiveTheme` no Nível B).
- `LocalCcExtras` — cores extras: `success/warning/info` (+`on`, `container`, `onContainer`), `cardSheen`, `cardOutline`, `scrimSoft`.
- `LocalCcMotion` — molas e durações (B7).
- `LocalCcReduceMotion` — booleano de movimento reduzido (B2.6).
- `LocalCcSpacing` (ou constantes) — escala de espaçamento (B6.1).

Recalcule o esquema conforme o modo resolvido (`claro`/`escuro`) e as mudanças de configuração que o projeto realmente precisar. Mudanças de papel de parede, cor de destaque ou Dynamic Color do sistema **não podem alterar nenhum token Vitrine**.

#### B2.5 Sem flash de tema e barras do sistema `[INEGOCIÁVEL]`

- **Objetivo:** nenhum frame com tema errado na abertura nem após recriar a Activity.
- **Estratégia preferida:** manter a splash até a **primeira emissão** da preferência (ex.: `installSplashScreen().setKeepOnScreenCondition { !themeReady }`), com salvaguarda de tempo máximo curto (≈ 400 ms) para nunca travar a abertura. `[VERIFICAR]` se `core-splashscreen` já está no projeto; **não adicione dependência** apenas para isso se houver alternativa já existente.
- **Alternativa aceitável:** leitura síncrona única e curta da preferência antes de `setContent`, seguindo o padrão já usado no projeto.
- **Janela:** defina o fundo da janela conforme o tema resolvido (evita lampejo claro/escuro antes da primeira composição). Atenção: o qualificador XML `-night` segue o **sistema**, não a preferência do app.
- **Edge-to-edge real, tela inteira do dispositivo `[INEGOCIÁVEL]`:** a interface deve ocupar visualmente toda a janela, de uma borda física à outra, incluindo a área atrás da barra de status/notificações e atrás da barra de navegação. Na captura atual, a barra de status permanece preta enquanto o conteúdo é azul-marinho; isso é um defeito a corrigir, não uma escolha de design.
  - Configure edge-to-edge na `MainActivity` com a API compatível com as versões e dependências reais do projeto (preferir `enableEdgeToEdge` do AndroidX Activity quando disponível; alternativamente, configurar `WindowCompat.setDecorFitsSystemWindows(window, false)` corretamente).
  - A barra de status deve ser **transparente** em edge-to-edge, e o fundo do próprio app deve ser desenhado continuamente por trás dela. A cor visível atrás do relógio, ícones e recortes deve ser a cor `Cc` do fundo da tela atual (por exemplo, o fundo Grafite no tema escuro), nunca uma faixa preta padrão ou uma cor independente do tema.
  - Não basta definir `window.statusBarColor` ou alterar somente o `MaterialTheme`: verificar o desenho real atrás dos insets. O contêiner raiz/fundo da tela deve alcançar as bordas da janela; o conteúdo interativo, por outro lado, deve respeitar `WindowInsets` para não ficar escondido sob relógio, recortes, câmera ou gestos.
  - Configurar a aparência dos ícones da barra de status conforme o tema **resolvido do CardControl**: ícones claros no tema escuro e ícones escuros no tema claro, independentemente do modo de aparência configurado no Android quando o app força outro tema. Preservar contraste e legibilidade de relógio, notificações e indicadores do sistema.
  - Aplicar o mesmo princípio à barra de navegação inferior: a superfície atrás dela deve integrar-se ao fundo do app e os ícones/gestos precisam continuar legíveis. Evitar uma faixa preta ou clara inesperada; respeitar o comportamento de navegação por gestos e por três botões nas versões Android suportadas.
  - Reaplicar estilos de barras quando o modo resolvido do app mudar (`DisposableEffect(modoResolvido)` ou mecanismo equivalente no nível correto), sem recriar a Activity desnecessariamente e sem flashes de cor.
  - Inspecionar e corrigir, se necessário, o tema da janela no `AndroidManifest.xml`/recursos XML (`windowBackground`, flags e atributos de barras), evitando que o tema nativo `Theme.Material.NoActionBar` introduza uma faixa preta antes da composição. Não adicionar paddings superiores fixos para esconder o problema.
  - Validar visualmente em dispositivo/emulador com captura de tela: fundo contínuo do topo até o conteúdo, ícones de status legíveis, conteúdo sem sobreposição com barras/recortes e comportamento correto em tema claro e Grafite. Um teste de compilação isolado não comprova este requisito.
- Depois de rotação/recriação, a seleção do cartão, a face (frente/verso) e a tela de preferências permanecem coerentes (ver C4.3).

#### B2.6 Movimento reduzido `[INEGOCIÁVEL]`

- Considere "movimento reduzido" quando as animações do sistema estiverem desativadas (`Settings.Global.ANIMATOR_DURATION_SCALE == 0f`, lido via `ContentObserver` e exposto como `State<Boolean>`), e/ou quando houver sinal de acessibilidade equivalente na versão do Android suportada.
- Efeito: **flip → fade cruzado de ~150 ms**; carrossel mantém rolagem (é entrada direta do usuário) mas **sem** escala/translação entre páginas; backdrop e cabeçalho trocam **sem** deslocamento; indicador troca sem mola; nenhuma animação decorativa (paralaxe do brilho, morph de forma). Funcionalidade e informação permanecem idênticas.

### B3. Cor

> **Origem dos valores:** paletas tonais geradas por script (CIELAB, matiz petróleo 210°, croma limitado ao gamut sRGB) e **contraste WCAG verificado** para os pares `on*` principais. São o ponto de partida; se houver Material Theme Builder disponível, pode refinar mantendo os contrastes. **Se o projeto já define valores do tema escuro Grafite, eles prevalecem** — a coluna Grafite abaixo preenche papéis ausentes e define a hierarquia mínima entre camadas (≈ 3–5 pontos de L* entre uma camada e a seguinte).

#### B3.1–B3.2 Esquemas Fallback Claro e Grafite

| Papel | Claro (fallback "Petróleo & Cobre") | Escuro **Grafite** |
|---|---|---|
| `primary` | `#006972` | `#72D5E1` |
| `onPrimary` | `#FFFFFF` | `#00363C` |
| `primaryContainer` | `#8FF2FE` | `#004F56` |
| `onPrimaryContainer` | `#001F23` | `#8FF2FE` |
| `secondary` | `#416468` | `#A8CDD2` |
| `onSecondary` | `#FFFFFF` | `#113539` |
| `secondaryContainer` | `#C3E9EE` | `#294C50` |
| `onSecondaryContainer` | `#001F23` | `#C3E9EE` |
| `tertiary` (cobre) | `#83532D` | `#F5BA8F` |
| `onTertiary` | `#FFFFFF` | `#4D2600` |
| `tertiaryContainer` | `#FFDCC4` | `#683C17` |
| `onTertiaryContainer` | `#2C1700` | `#FFDCC4` |
| `background` / `surface` | `#F4FBFB` | `#111717` |
| `onBackground` / `onSurface` | `#181C1D` | `#DDE4E5` |
| `surfaceDim` | `#D5DBDC` | `#0E1415` |
| `surfaceBright` | `#F4FBFB` | `#343A3B` |
| `surfaceContainerLowest` | `#FFFFFF` | `#071010` |
| `surfaceContainerLow` | `#EEF5F6` | `#171C1D` |
| `surfaceContainer` | `#E8EFF0` | `#1C2222` |
| `surfaceContainerHigh` | `#E3E9EA` | `#242A2B` |
| `surfaceContainerHighest` | `#DDE4E5` | `#2E3434` |
| `surfaceVariant` | `#D6E5E7` | `#384446` |
| `onSurfaceVariant` | `#3C494B` | `#B5C4C6` |
| `outline` | `#6C7A7B` | `#808E90` |
| `outlineVariant` | `#BAC9CB` | `#3C494B` |
| `inverseSurface` | `#2C3132` | `#DDE4E5` |
| `inverseOnSurface` | `#EBF2F3` | `#2C3132` |
| `inversePrimary` | `#72D5E1` | `#006972` |
| `scrim` | `#000000` | `#000000` |

Contrastes medidos (mínimos exigidos entre parênteses): `onPrimary/primary` 6,4:1 claro · 7,7:1 Grafite (4,5) — `onPrimaryContainer/primaryContainer` 13,4:1 · 7,2:1 (4,5) — `onSurface/surface` ≈ 16:1 · 14,1:1 (4,5) — `onSurfaceVariant/surfaceVariant` 7,2:1 (4,5) — `outline/surface` 4,3:1 · 5,3:1 (3,0) — `outlineVariant` ≈ 1,6–1,9:1 ⇒ **somente divisores decorativos**, nunca borda de campo ou de componente interativo.

**Personalidade do fallback claro:** petróleo (primário, calmo, nem rosa nem roxo) + cobre (terciário, para pequenos acentos quentes: selo de seleção, destaque de ação principal secundária). Não use cobre como cor de estado (aviso/erro).

#### B3.3 Cores semânticas fixas nos temas Vitrine `[DECISÃO]`

| Papel | Claro | Escuro |
|---|---|---|
| `error` / `onError` | `#B3261E` / `#FFFFFF` | `#F2B8B5` / `#601410` |
| `errorContainer` / `onErrorContainer` | `#F9DEDC` / `#410E0B` | `#8C1D18` / `#F9DEDC` |
| `success` / `onSuccess` | `#246B3A` / `#FFFFFF` | `#8FD79E` / `#003916` |
| `successContainer` / `onSuccessContainer` | `#ABF3B9` / `#002107` | `#015224` / `#ABF3B9` |
| `warning` / `onWarning` | `#73550F` / `#FFFFFF` | `#EAC078` / `#402D00` |
| `warningContainer` / `onWarningContainer` | `#FFDEAA` / `#251A00` | `#5D4200` / `#FFDEAA` |
| `info` / `onInfo` | `#006493` / `#FFFFFF` | `#90CDFF` / `#00344E` |
| `infoContainer` / `onInfoContainer` | `#CCE5FF` / `#001E30` | `#004B70` / `#CCE5FF` |

Contraste `on*/cor` ≥ 6,4:1 e `onContainer/container` ≥ 7,1:1 em todos os pares. **Regra:** significado nunca só por cor — sempre ícone ou texto junto (ex.: ícone de alerta + "Falha ao salvar").

#### B3.4 Estados e camadas de interação (M3)

- Camadas de estado sobre o conteúdo: **hover 8% · foco 10% · pressionado 10% · arrastado 16%**.
- Desabilitado: conteúdo a **38%**, container a **12%** da cor `onSurface`.
- Foco por teclado/D-pad/Switch Access: anel de **3 dp** em `primary` com afastamento de **2 dp**, visível em ambos os temas (≥ 3:1 contra o fundo).

#### B3.5 Sobrescrita das cores do sistema e aplicação global dos tokens Vitrine `[DECISÃO]`

- O design system Vitrine é a **fonte única de verdade para cores** da interface do CardControl. Não consumir nem mesclar `dynamicLightColorScheme`/`dynamicDarkColorScheme`, cores do papel de parede, `colorAccent` do tema do sistema ou cores herdadas de temas antigos.
- Refatorar todos os pontos de entrada de cor existentes no aplicativo para usarem tokens centralizados `Cc*` ou cores derivadas desses tokens: telas, componentes, formulários, botões, campos, menus, diálogos, bottom sheets, snackbars, estados de erro/sucesso, placeholders, ícones, divisores, cards, top/bottom app bars, navegação e preferências. Incluir componentes legados e caminhos secundários, não apenas a tela de cartões.
- Cores literais só podem permanecer nos arquivos centrais de tokens (ou nos pontos técnicos em que Android exige recurso de janela, alimentados pelos tokens). Eliminar cores hardcoded dispersas, aliases contraditórios e cópias de paleta. Não substituir uma paleta hardcoded por outra paleta hardcoded em cada tela.
- Aplicar as cores de status bar e navigation bar da janela ativa a partir do tema Vitrine; respeitar a API de edge-to-edge existente e garantir ícones legíveis nos dois modos. Isso afeta somente a janela do app, não muda a aparência global do Android ou de outros aplicativos.
- **Teste de invariância obrigatório:** em Android 12+, alterar papel de parede e cores de destaque para pelo menos 6 variações extremas (amarelo-claro, azul saturado, verde-limão, magenta saturado, laranja queimado e cinza quase neutro). As telas e os componentes do CardControl devem manter exatamente a paleta Vitrine correspondente ao modo claro/escuro, sem mudança de cores. Contraste e semântica devem continuar corretos.

### B4. Cor do cartão (a assinatura visual)

#### B4.1 Algoritmo `[DECISÃO]`

Entrada: uma **semente** (matiz *h* e croma *c*, em CIELAB LCh) vinda de uma predefinição ou da cor personalizada do usuário, caso o seletor livre já exista. Nunca derivar sementes das cores dinâmicas do Android; para a cor "Automática", aplicar as regras determinísticas de B4.2.

```
c'    = clamp(c, 8, 56)                       # evita cinza morto e neon
start = cor(h, c',        L* = 38)            # topo-esquerda
end   = cor(h, 0,85·c',   L* = 22)            # base-direita
brilho (sheen) = branco 8% em gradiente radial no canto superior-esquerdo (raio ≈ 0,9 × largura)
gradiente = linear 135° de start → end, com o brilho por cima
```

Fixar o **L\*** (luminosidade percebida) é o que **garante o contraste por construção**, qualquer que seja o matiz escolhido.

| Texto sobre o cartão | Cor | Contraste mínimo medido (12 predefinições, sobre `start` + brilho) | Uso |
|---|---|---|---|
| Primário | branco 100% | ≥ 5,6:1 | banco, final, valores |
| Secundário | branco 88% | ≥ 4,8:1 | titular, apelido, rótulos |
| Decorativo | branco 80% | ≥ 4,3:1 | **somente** ícones/rótulos decorativos ≥ 14 sp; nunca dado essencial |

Para cores personalizadas, a mesma função é aplicada; o teste de contraste (E1) roda também sobre sementes aleatórias.

#### B4.2 Predefinições (12) `[DECISÃO]`

Cores de cartão **não são a cor da marca**: rosa e roxo existem como opções do usuário, mas nunca como tema do app.

| Nome | Semente h / C | Frente: início → fim | Verso: início → fim |
|---|---|---|---|
| Grafite | 250 / 6 | `#515B62` → `#2E363C` | `#2E363B` → `#22292D` |
| Petróleo | 205 / 46 | `#006469` → `#003B3F` | `#003B3F` → `#002D30` |
| Safira | 266 / 52 | `#005E93` → `#00375A` | `#00375A` → `#002A46` |
| Índigo | 292 / 50 | `#4153A5` → `#1A3070` | `#1F306C` → `#142456` |
| Ametista | 318 / 46 | `#77458C` → `#4B245C` | `#4A255A` → `#391B47` |
| Rosé | 352 / 48 | `#993366` → `#65103E` | `#63153E` → `#4E0C2F` |
| Rubi | 28 / 56 | `#A52C32` → `#6E0716` | `#6B0E18` → `#55060F` |
| Coral | 46 / 54 | `#993C1B` → `#641B00` | `#621D03` → `#4B1700` |
| Cobre | 62 / 48 | `#884910` → `#542A00` | `#542A00` → `#402000` |
| Âmbar | 80 / 46 | `#765303` → `#473100` | `#473100` → `#362500` |
| Oliva | 116 / 44 | `#4D6111` → `#2C3A00` | `#2C3A00` → `#222B00` |
| Esmeralda | 150 / 46 | `#006833` → `#003E1C` | `#003E1C` → `#002F14` |

**Cor "Automática":** com banco conhecido → predefinição **determinística** (hash estável do ID do banco módulo as 11 predefinições coloridas, excluindo Grafite) — o mesmo banco tem sempre a mesma cor; sem banco → Grafite. Nunca usar `scheme.primary` de um esquema dinâmico do Android. **Não use `hashCode()` de objetos instáveis**; use o ID persistente.

**Cor personalizada** (apenas se o formulário atual já oferece seletor livre): converter para (h, c) e passar por B4.1.

#### B4.3 Borda, sombra e profundidade

| Tema | Cartão selecionado | Cartão vizinho |
|---|---|---|
| **Claro** | Sombra `~10 dp` com `spotColor = start@45%` e `ambientColor = preto@16%` (sombra tingida pela cor do cartão) | Sombra `~3 dp` |
| **Escuro (Grafite)** | **Sem sombra** (invisível no escuro). Borda de **1 dp** branco@10% + brilho superior-esquerdo; sutil halo de cor `start@18%` por baixo (opcional) | Mesma borda, sem halo |

Nunca use `blur`/vidro. Sombra e borda são parte do cartão (`graphicsLayer`/`drawBehind`), não camadas extras.

#### B4.4 Estados do cartão `[CONDICIONAL]`

- **Bloqueado/Inativo** — somente se forem estados reais do domínio: croma da paleta reduzido a **30%**, selo textual ("Bloqueado"/"Inativo", container branco@16%, ícone + texto). Nunca só cor.
- **Dados ausentes** — omitir a linha correspondente (não escrever "—" decorativo nem valor de exemplo); manter o alinhamento do restante.

#### B4.5 Verso

Gradiente de **verso** (tabela B4.2), mais escuro que a frente para reforçar a sensação de "outra face". Textos em branco 100%/88% (contraste ≥ 12:1).

#### B4.6 Tingimento do palco (harmonia com o cartão selecionado)

- O palco recebe um gradiente vertical no terço superior: `lerp(surface, start, α)` no topo → `surface` a ~55% da altura da tela. **α = 0,14 (claro) · 0,20 (escuro)** `[ALVO]`.
- Transição ao trocar de cartão: `animateColorAsState` com mola de efeitos lenta (B7) ou `tween(350 ms)`.
- **Guarda de contraste:** `onSurface` sobre o topo tingido mantém ≥ 4,5:1 para as 12 predefinições nos dois temas (testar). Barras do sistema continuam calculadas contra `surface`, não contra o tingimento.
- Sem cartões ou com movimento reduzido: sem animação de cor (troca direta).

### B5. Tipografia

- **Família:** a já usada pelo app (normalmente a padrão do sistema). **Não adicione fonte nova.** Se o projeto já usa fonte variável, pode usar o eixo de peso.
- **Unidades:** `sp` para texto; escala do usuário respeitada até **2,0×**.
- **Ênfase estilo Expressive:** no Nível A, simule os estilos "emphasized" com `FontWeight.SemiBold` via helper `CcType.emphasized(style)`.
- **Números:** valores monetários, finais e datas com `fontFeatureSettings = "tnum"` (algarismos tabulares), formatação por `Locale("pt","BR")`/`NumberFormat` — nunca concatenação manual.

| Token `CcType.*` | Base M3 | Tamanho/linha | Peso | Detalhes | Uso |
|---|---|---|---|---|---|
| `cardBank` | `titleMedium` | 16/22 sp | 600 | `letterSpacing 0.15 sp`, 1 linha, elipse | Nome do banco no cartão |
| `cardHolder` | `labelLarge` | 14/20 sp | 500 | cor 88%, 1 linha | Titular/pessoa |
| `cardMask` | `titleLarge` | 22/28 sp | 500 | `tnum`, `letterSpacing 1.5 sp`, "•••• 1234" | Final mascarado |
| `cardMeta` | `labelMedium` | 12/16 sp | 500 | `letterSpacing 0.5 sp`, cor 88% | Apelido, tipo, bandeira |
| `screenHeader` | `headlineSmall` | 24/32 sp | 600 | emphasized | Cabeçalho do cartão selecionado |
| `screenSub` | `bodyMedium` | 14/20 sp | 400 | `onSurfaceVariant` | Linha de apoio |
| `actionLabel` | `labelMedium` | 12/16 sp | 500 | até 2 linhas, centralizado | Rótulo das ações |
| `buttonLabel` | `labelLarge` | 14/20 sp | 500 | — | "Ver mais", diálogos |
| `dialogTitle` | `headlineSmall` | 24/32 sp | 400–600 | — | Títulos de diálogo |

**Fonte ampliada (cartão)** `[DECISÃO]`: o texto do cartão acompanha a escala até 1,3× dentro da proporção fixa 1,586:1. Acima de 1,3×, a **altura do cartão cresce** (até +30%, igual para todas as páginas do carrossel) e, se ainda faltar espaço, as linhas de menor prioridade somem **da frente** (ordem de prioridade: banco › titular › final › apelido/tipo › bandeira), permanecendo no verso e nos detalhes. **Nunca** cortar texto no meio nem sobrepor.

### B6. Formas, espaçamento e camadas

#### B6.1 Espaçamento (grade de 4 dp) — `CcSpace`

| Token | dp | Token | dp |
|---|---|---|---|
| `s1` | 4 | `s6` | 24 |
| `s2` | 8 | `s8` | 32 |
| `s3` | 12 | `s10` | 40 |
| `s4` | 16 | `s12` | 48 |
| `s5` | 20 | | |

Margem horizontal da tela: **16 dp** (compacta) · 24 dp (média) · 32 dp (expandida). O carrossel é a exceção: usa o *peek* (C4.1).

#### B6.2 Formas — `CcShapes`

| Token | Valor | Uso |
|---|---|---|
| `card` | **24 dp** | Cartão (frente e verso) |
| `actionContainer` | 20 dp (repouso) → círculo ao pressionar | Contêiner do ícone de ação |
| `pill` | 28 dp (altura 56) | Grupo "Ver mais" + "+" |
| `pillInner` | 6 dp | Cantos internos dos dois segmentos |
| `monogram` | 12 dp (contêiner 40 dp) | Monograma do banco |
| `chip` | 8 dp | Selos de estado/bandeira |
| `dialog` / `sheet` | 28 dp | Diálogos / bottom sheets |
| `field` | 12–16 dp | Campos do formulário |

**Morph ao pressionar (Expressive, sem API experimental):** animar o raio de canto de 28 → 16 dp (pílula) e de 20 dp → círculo (ação) com mola espacial rápida (B7); voltar ao soltar. Com movimento reduzido: sem morph (apenas camada de estado).

#### B6.3 Camadas de superfície por tema

| Nível | Claro | Grafite | Uso |
|---|---|---|---|
| L0 Palco | `surface` | `surface` | Fundo |
| L1 Bancada | `surfaceContainerLow` | `surfaceContainerLow` | Grupos de conteúdo |
| L2 Controles | `surfaceContainerHigh` | `surfaceContainerHigh` | Contêineres de ação, campos |
| L3 Sobreposição | `surfaceContainerHigh/Highest` | `surfaceContainerHigh/Highest` | Diálogo, menu, sheet |

Em tema escuro a separação vem de **luminosidade + borda sutil**, não de sombra. No claro, sombras suaves e tingidas só no cartão; demais componentes planos (estilo Expressive).

### B7. Movimento

#### B7.1 Tokens — `CcMotion`

Molas inspiradas nos tokens do esquema Expressive (amortecimento / rigidez). `[VERIFICAR]` contra `MaterialTheme.motionScheme` quando o projeto estiver no Nível B e **prefira os valores do tema** nesse caso.

| Token | Amortecimento | Rigidez | Natureza |
|---|---|---|---|
| `spatialFast` | 0,6 | 800 | Posição/tamanho/forma, pequeno e rápido (leve overshoot) |
| `spatialDefault` | 0,8 | 380 | Posição/tamanho/rotação padrão |
| `spatialSlow` | 0,8 | 200 | Movimentos amplos |
| `effectsFast` | 1,0 | 3800 | Opacidade/cor, sem overshoot |
| `effectsDefault` | 1,0 | 1600 | Opacidade/cor padrão |
| `effectsSlow` | 1,0 | 800 | Cor ampla (tingimento do palco) |

**Regra de ouro:** *espacial* (posição, escala, rotação, forma) pode ter leve oscilação; *efeitos* (cor, opacidade) **nunca** oscilam.

#### B7.2 Mapa de movimento

| Evento | Animação | Token / valor |
|---|---|---|
| Rolagem do carrossel | Nativa do pager + encaixe (`atMost(1)`) | Fling do `HorizontalPager` |
| Vizinhos | Escala 1 → **0,92**, opacidade 1 → **0,72**, translação Y 0 → 6 dp, em função do deslocamento da página (sem animação própria) | Derivado de `pageOffset` |
| Brilho (sheen) do cartão | Paralaxe horizontal de até ±24 dp (opcional) | Derivado de `pageOffset` |
| Troca de cabeçalho | Saída 90 ms; entrada com leve subida de 8 dp | `effectsFast` / `effectsDefault` + `spatialFast` |
| Tingimento do palco | Cor | `effectsSlow` |
| **Flip** | Rotação Y 0 ↔ 180° (+ escala 1 → 0,96 → 1) | `spatialDefault` |
| Indicador | Largura da pílula ativa | `spatialFast` |
| Pressionar botão/ação | Morph de canto | `spatialFast` |
| Diálogo/sheet | Entrada escala 0,9 → 1 + fade | `spatialDefault` + `effectsDefault` |
| Estado vazio | Entrada com fade e subida de 12 dp | `effectsDefault` + `spatialDefault` |
| Cartão removido | Fade-out do cartão e reposicionamento do pager sem salto | `effectsFast` |

**Todas** as animações são interrompíveis e reversíveis: um segundo toque durante o flip reverte a partir do ponto atual, sem salto.

#### B7.3 Haptics `[ALVO]` (opcionais)

Somente via `LocalHapticFeedback`: leve ao assentar em novo cartão; leve ao virar; confirmação ao concluir exclusão. Use os tipos disponíveis na versão instalada; se o tipo desejado não existir, **omita** (não improvisar com vibração própria).

### B8. Iconografia, monograma e imagem

- **Ícones:** use somente os já disponíveis no projeto (família *rounded/filled* consistente). Se faltarem, adicione poucos vetores de **Material Symbols (Apache-2.0)** como drawables, mantendo peso e estilo. Tamanho **24 dp** em botões; **20 dp** dentro de selos; contêiner mínimo **48 dp** para toque.
- **Monograma do banco** (sempre que não houver ativo oficial licenciado): contêiner **40 dp** com forma `monogram`, fundo `branco@16%` sobre o cartão, texto `titleMedium` branco. Regra de iniciais: ignorar conectivos (*de, da, do, das, dos, e*); com 2+ palavras, a **inicial das duas primeiras**; com 1 palavra, **as duas primeiras letras** com a segunda minúscula. Exemplos: "Banco do Brasil" → **BB**; "Nubank" → **Nu**; "Mercado Pago" → **MP**; "Itaú" → **It**.
- **Titular/pessoa:** avatar de iniciais **20 dp** (círculo, `branco@16%`) + nome. Sem foto.
- **Bandeira** (Visa, Mastercard, Elo etc., só se o modelo guardar): **texto** em selo `chip` ("VISA" em `cardMeta`, caixa alta), nunca logotipo. Banco e bandeira ocupam posições e estilos diferentes para não se confundirem.
- **Ilustrações:** nenhuma. O estado vazio usa composição de formas simples (cartão-fantasma tracejado com o mesmo raio do cartão real).


---

## Parte C — Componentes e telas

> Antes de criar qualquer componente abaixo, **inspecione o equivalente existente** (principalmente `CardTile`). Reutilize ou refatore; nunca mantenha duas implementações concorrentes do mesmo conceito. Não invente seções de resumo financeiro, saldos, limites totais ou estatísticas; use conteúdo real e estados vazios reais.

### C1. Tela de cartões — anatomia, ritmo e responsividade

#### C1.1 Hierarquia (de cima para baixo)

```
┌────────────────────────────────────────┐
│ Top bar existente (sem duplicar nav)   │
├────────────────────────────────────────┤
│  ░░ tingimento do palco (B4.6) ░░░░░   │
│ ┌╌╌╌┐ ┌──────────────────┐ ┌╌╌╌┐       │
│ │   │ │   CARTÃO ATIVO   │ │   │ ← peek│
│ └╌╌╌┘ └──────────────────┘ └╌╌╌┘       │
│              ▬ ● ● ●                   │ ← indicador (≥ 2 cartões)
│  Nubank                            ⋮   │ ← cabeçalho contextual + menu
│  Maria · final 1234                    │
│  (Detalhes) (Editar) (Lançar compra)   │ ← ações reais (máx. 4)
│                                        │
│     [ Ver mais  ▸ ][ + ]               │ ← grupo de destinos
└────────────────────────────────────────┘
```

Itens 1–5 da hierarquia pretendida no original (top bar → carrossel → indicador → ações do cartão selecionado → "Ver mais" + "+") foram mantidos. A top bar e a navegação existentes são preservadas.

#### C1.2 Ritmo vertical `[ALVO]`

| Entre | Espaço |
|---|---|
| Top bar → carrossel | 8 dp |
| Carrossel (altura do cartão + 16 dp de respiro vertical p/ sombra) | — |
| Carrossel → indicador | 8 dp |
| Indicador (altura reservada 16 dp, mesmo se oculto com 1 cartão, para não "pular") | — |
| Indicador → cabeçalho | 16 dp |
| Cabeçalho → ações | 20 dp |
| Ações → grupo "Ver mais" + "+" | 24 dp |
| Grupo → borda inferior útil | 16 dp + inset de navegação |

> O respiro vertical do carrossel evita que a **sombra do cartão seja cortada** pelo viewport do pager.

#### C1.3 Classes de tamanho `[DECISÃO]`

| Classe | Largura | Layout |
|---|---|---|
| **Compacta** | < 600 dp | Coluna única conforme C1.1; *peek* 20–28 dp |
| **Média** | 600–839 dp | Coluna única centralizada; cartão limitado a **400 dp**; margens 24 dp |
| **Expandida** ou celular em **paisagem** (altura < 480 dp) | ≥ 840 dp ou paisagem | **Dois painéis:** esquerdo ≈ 45% com o carrossel (altura do cartão ≤ 70% da altura útil); direito com cabeçalho, ações e grupo "Ver mais" + "+" (rolável) |

Use as APIs de janela **já disponíveis** (`BoxWithConstraints`, `LocalConfiguration` ou, se já houver, `currentWindowAdaptiveInfo`). **Não adicione dependência** só para classificar largura.

#### C1.4 Insets e barras

- Conteúdo respeita `WindowInsets.systemBars`, `displayCutout` e gestos de borda; o grupo inferior usa `navigationBars` + 16 dp.
- O carrossel usa a área total horizontal (os vizinhos "vazam" até a borda), mas **o primeiro e o último cartão** nunca ficam atrás de gestos de voltar do sistema: mantenha pelo menos 16 dp de área livre quando o cartão estiver centralizado.

### C2. `CardFace` — frente do cartão

**Proporção** 1,586:1 (largura/altura) `[ALVO]`, com ajuste por fonte ampliada (B5). **Raio** `card` 24 dp. **Padding interno** 20 dp (16 dp se a largura do cartão < 300 dp).

```
┌──────────────────────────────────────┐
│ [Nu]  Nubank                    (↻)  │  monograma 40 · banco · botão virar (cartão selecionado)
│       (M) Maria                      │  avatar 20 + titular
│                                      │
│                                      │
│  Platinum · Crédito          [VISA]  │  apelido/tipo (cardMeta)  ·  bandeira (selo de texto)
│  •••• 1234                           │  cardMask
└──────────────────────────────────────┘
```

| Elemento | Origem (modelo real) | Se ausente |
|---|---|---|
| Monograma + nome do banco | seletor/lista de bancos já existente | Nunca ausente; se o banco for desconhecido, mostrar "Banco não informado" (sem monograma, ícone genérico de cartão) |
| Titular/pessoa | pessoa associada | Omitir a linha; o bloco do banco centraliza verticalmente |
| Final mascarado | **somente** se o modelo guarda os 4 últimos dígitos | Omitir; não mostrar "••••" vazio |
| Apelido/tipo | campos existentes | Omitir |
| Bandeira | **somente** se o modelo guarda | Omitir |
| Selo de estado | **somente** estados reais | Omitir |

**Estados e interação**

| Estado | Comportamento |
|---|---|
| Selecionado | Escala 1,0; sombra/borda de destaque (B4.3); botão virar visível; toque → flip |
| Vizinho | Escala 0,92 / opacidade 0,72; **sem** botão virar; toque → rola até ele |
| Pressionado | Escala 0,98 com `spatialFast`; sem alteração de layout |
| Foco (teclado/D-pad) | Anel 3 dp (B3.4) |
| Dado secundário ausente | Layout se reorganiza sem buracos |

**Acessibilidade do cartão (frente):** um único nó com `contentDescription` composta — "Cartão {banco}, titular {nome}, final {1234}, {n} de {total}" — omitindo partes ausentes, `stateDescription` ("Mostrando a frente"), `Role.Button` com `onClickLabel` "Virar cartão" apenas no selecionado, e **ações de acessibilidade** "Cartão anterior"/"Próximo cartão" para TalkBack. Elementos puramente decorativos (brilho, gradiente, monograma) ficam fora da árvore semântica.

**Regras de privacidade:** nenhum dado além dos 4 últimos dígitos; nada disso em logs, `toString()` ou analytics.

### C3. Verso do cartão (flip) `[CONDICIONAL]` no conteúdo, obrigatório no mecanismo

**Quando tocar:** cartão **selecionado**, por toque no corpo ou no botão "Virar" (alvo ≥ 48 dp). **Cartão que perde a seleção volta à frente** (D-07).

**Conteúdo do verso — apenas dados reais e adequados:**

| Campo | Exibir se… | Formato |
|---|---|---|
| Banco | sempre | texto |
| Titular | associada | texto |
| Final | modelo guarda | `•••• 1234` |
| Limite | campo existe **e** preenchido | `NumberFormat` pt-BR |
| Fecha dia / Vence dia | campos existem **e** preenchidos | "Fecha dia 10", "Vence dia 17" |

Organização: título "Detalhes" (`labelLarge`) + botão "Voltar à frente"; até **4 linhas em grade 2×2** (rótulo `cardMeta` + valor `titleMedium`); se houver mais, o restante vive na tela de detalhes. **Sem conteúdo secundário suficiente:** verso simples com o texto "Verso do cartão. Toque para voltar à frente." (e, **somente se** o formulário já permite cadastrar limite/datas, a dica "Adicione limite e datas da fatura ao editar o cartão.").

**Proibido no verso:** CVV, número completo, validade, senha, credenciais, **tarja magnética, área de assinatura, chip, símbolo de aproximação** — mesmo decorativos (induzem a ler o cartão como instrumento de pagamento).

**Mecânica (ver D2.3)**

- Rotação em Y de 0° → 180° com `spatialDefault`; **a face só é trocada em 90°**; o verso recebe contrarrotação de 180° para **nunca** aparecer espelhado; só **uma** face existe na composição por vez (derivar `mostrarVerso` de `rotation > 90f`).
- `cameraDistance ≈ 16 × density` (perspectiva discreta); leve escala 1 → 0,96 → 1 e sombra acompanhando.
- Estado `flipped` guardado **por ID** do cartão selecionado e restaurado com `rememberSaveable`; nunca dispara por recomposição.
- Acessibilidade: somente a face visível é acessível; `stateDescription` "Mostrando a frente/o verso"; ação acessível "Virar cartão"; **não depender de gesto**.
- Movimento reduzido: fade cruzado ~150 ms, sem rotação.
- O efeito é **apenas visual** — não simula chip, NFC, pagamento ou leitura do cartão.
- Se o flip dificultar demais a arquitetura atual, **isole-o e testá-lo**, sem bloquear o carrossel (A6).

### C4. `CardCarousel`

#### C4.1 Geometria do *peek* `[ALVO]`

| Largura útil | `contentPadding` horizontal | `pageSpacing` | *Peek* visível | Largura do cartão |
|---|---|---|---|---|
| < 360 dp | 32 dp | 12 dp | **20 dp** | largura − 64 dp |
| 360–599 dp | **40 dp** | 12 dp | **28 dp** | largura − 80 dp |
| ≥ 600 dp | `(largura − 400 dp) / 2` | 16 dp | o que sobrar (mostrar vizinho só se ≥ 16 dp) | **400 dp** (máx.) |

*Peek = `contentPadding − pageSpacing`.* Com **um** cartão: centralizado, sem padding assimétrico e sem indicador. Padding **vertical** de 16 dp no pager para não cortar a sombra.

#### C4.2 Transformações por página `[ALVO]`

Com `offset = |(currentPage − page) + currentPageOffsetFraction|` limitado a 0..1, aplicar em `graphicsLayer` (lido na fase de desenho, **sem recomposição**):

- `scale = lerp(1, 0.92, offset)`, `alpha = lerp(1, 0.72, offset)`, `translationY = lerp(0, 6 dp, offset)`.
- `zIndex = 1 − offset` (o selecionado fica por cima).
- **Nunca** aplicar rotação/inclinação nas páginas (texto não pode distorcer) e nunca cortar informação necessária.
- Encaixe: `PagerSnapDistance.atMost(1)` (previsível); `beyondViewportPageCount = 1`.
- `HorizontalPager` do Compose Foundation se compatível com a versão do projeto `[VERIFICAR]`; usar `key = { cards[it].id }`.

#### C4.3 Contrato de seleção `[INEGOCIÁVEL]`

- **Fonte de verdade:** `selectedCardId` (ID estável) mantido no ViewModel/`SavedStateHandle` — **ou** onde a arquitetura existente já mantiver estado de tela. O pager **reflete** o ID; não existe um segundo "índice selecionado" persistente.
- **Pager → modelo:** reporta seleção **apenas ao assentar** (`settledPage`), não durante o arrasto.
- **Modelo → pager:** quando o ID selecionado mudar por causa de dado (exclusão, edição, reordenação, nova lista do Room), reposicionar com `scrollToPage` **sem animação**, para evitar loop e piscadas.
- **Cabeçalho, ações, menu, tingimento e flip** derivam **do mesmo ID** — nunca de `currentPage` cru (evita ação apontando para outro cartão durante a transição).
- **Lista vazia:** `selectedCardId = null`; nada de índice inválido.

#### C4.4 Política após mudanças na lista `[DECISÃO]`

| Evento | Novo selecionado |
|---|---|
| Exclusão do selecionado (meio) | O cartão que passa a ocupar o **mesmo índice** (o seguinte) |
| Exclusão do selecionado (último) | O **anterior** |
| Exclusão do único | Nenhum → estado vazio |
| Cadastro de novo cartão | O **novo** cartão (rolar até ele ao voltar à tela) |
| Edição | Mesmo ID; atualizar conteúdo sem mexer na posição |
| Exclusão de cartão **não** selecionado | Mantém o selecionado (reposiciona o pager para o mesmo ID) |
| Nova emissão do Room durante o arrasto | Não interromper o gesto; reconciliar ao assentar |
| Rotação / morte de processo | Restaura pelo ID; se o ID não existir mais, cai na regra de exclusão |

#### C4.5 Interação e acessibilidade

- TalkBack lê "Cartão … n de N" e oferece "Cartão anterior/Próximo cartão".
- Toque em vizinho → `animateScrollToPage` com mola `spatialDefault`.
- O carrossel **não** captura gestos verticais da página (rolagem vertical do conteúdo continua funcionando).

### C5. Indicador de posição

| Cartões | Indicador |
|---|---|
| 0 ou 1 | Nenhum (espaço reservado, sem "pulo") |
| 2–7 | Pílulas: inativas círculo **6 dp** `onSurfaceVariant@38%`; ativa **18×6 dp** `primary`; espaçamento 6 dp; largura animada com `spatialFast` |
| ≥ 8 | Texto "3 de 12" (`labelMedium`, `onSurfaceVariant`) |

Não é interativo e **não é focável** (a posição já é anunciada pelo cartão).

### C6. Cabeçalho contextual

- **Linha 1:** nome do banco (`screenHeader`, 1 linha, elipse) + **menu do cartão** (`⋮`, 48 dp, "Mais opções do cartão {banco}").
- **Linha 2:** "{titular} · final {1234}" (`screenSub`), omitindo partes ausentes.
- Troca por `AnimatedContent` chaveado pelo **ID** (B7.2). Com movimento reduzido, troca direta.
- **Menu do cartão** — itens reais: *Editar* (se a edição não estiver já na linha de ações) e *Excluir* (`error`, com confirmação C10). Capacidades infrequentes ou destrutivas moram aqui (D-09).

### C7. Ações contextuais — somente capacidades reais

**Preencha esta tabela no `implementation_plan.md` com o que existe de fato.** Cada capacidade aparece **uma vez** (ou na linha de ações, ou no menu).

| Ação | Só existe se… | Destino/operação | Prioridade | Ícone sugerido |
|---|---|---|---|---|
| **Detalhes** | existe tela de detalhes do cartão (ou o *Ver mais* a substitui) | rota de detalhes com `cardId` | **Principal** (`primaryContainer`) | info/cartão |
| **Editar** | existe (ou será completado em F5) formulário de edição | formulário em modo edição com `cardId` | Secundária | editar |
| **Lançar compra** | existe fluxo de lançar/registrar compra | fluxo existente pré-filtrado por `cardId` | Secundária | adicionar recibo/+ |
| **Registrar pagamento** | **já há** fluxo real de registro de pagamento | fluxo existente com `cardId` | Secundária | check/pagamento registrado |
| **Compartilhar rateio** | **já há** fluxo de rateio | fluxo existente com `cardId` | Secundária | pessoas/dividir |
| **Excluir** | sempre (via repositório oficial) | diálogo C10 | Destrutiva → **menu** | lixeira |

**Layout:** linha de até **4** ações visíveis; as demais capacidades reais vão ao menu `⋮`. Cada ação: contêiner **56×56 dp** (forma `actionContainer`) + rótulo `actionLabel` até 2 linhas; largura mínima **72 dp**, toque ≥ 48 dp. A ação principal usa `primaryContainer/onPrimaryContainer`; as demais, `surfaceContainerHigh/onSurface`. Com fonte ≥ 1,3× ou largura estreita: grade 2×2 (`FlowRow`) em vez de linha única.

**Regras por ação implementada** `[INEGOCIÁVEL]`

1. Usar o **ID estável** do cartão selecionado.
2. Navegar para o fluxo real existente ou executar a operação no domínio correspondente.
3. Exibir loading/erro/sucesso quando houver operação assíncrona.
4. Impedir **duplo envio** em mutações (desabilitar enquanto em andamento).
5. Respeitar autenticação, autorização e propriedade dos dados.
6. Rótulo semântico para leitores de tela (`contentDescription` com o nome do cartão quando a ação for ambígua: "Editar cartão Nubank final 1234").

**Se menos de 2 ações reais existirem**, renderize **um único** botão largo (`FilledTonalButton`, 56 dp) — nunca preencha a linha com atalhos falsos nem crie fluxos financeiros novos para "completar a tela".

### C8. Grupo "Ver mais" + "+"

**Composição** (nativa, sem `SplitButtonLayout` nem API experimental — B2.3):

```
[   Ver mais  ▸   ][ + ]        altura 56 dp · espaço entre segmentos 2 dp
 ╰─ tonal, cantos: 28 externo / 6 interno ─╯  ╰─ primário, 6 interno / 28 externo ─╯
```

| Segmento | Visual | Destino | Rótulo de acessibilidade |
|---|---|---|---|
| **Ver mais** | `secondaryContainer`, rótulo `buttonLabel` + chevron 20 dp; largura flexível (mín. 140 dp) | Ver árvore abaixo | "Ver mais sobre meus cartões" (ajustar ao destino) |
| **+** | `primary`/`onPrimary`, ícone 24 dp, **56×56 dp** | Fluxo de **cadastrar cartão** | "Adicionar cartão" |

**Destino de "Ver mais" — árvore de decisão `[VERIFICAR]`:**

1. Existe **lista/gestão de cartões** → abra-a.
2. Senão, existe **detalhes do cartão** e *Detalhes* **não** está na linha de ações → abra-a (e remova *Detalhes* da linha).
3. Senão, abra um *bottom sheet* "Mais opções" com **apenas** ações reais ainda não presentes na tela.
4. Se nada disso for real, **omita** "Ver mais" e mantenha apenas o "+" como botão de ação; registre no relatório.

**Estados:** repouso; pressionado (morph de canto 28 → 16 dp e camada 10%); foco (anel 3 dp); desabilitado (38%/12%). Os dois segmentos têm **destinos distintos** e nomes acessíveis distintos.

**Posicionamento** `[VERIFICAR]`:

- **Preferência (telas compactas):** fixo na base da área útil (`navigationBars` + 16 dp), com degradê `surface → transparente` de 24 dp acima para o conteúdo "entrar por baixo"; o conteúdo da tela recebe *padding* inferior suficiente para **nunca** ficar coberto.
- **Se já existir `NavigationBar`/FAB:** respeite o padrão existente — coloque o grupo no fluxo (logo abaixo das ações) em vez de sobrepor. **Não mantenha dois "+" concorrentes** na mesma tela: o "+" deste grupo é exclusivo para cadastrar cartão; um FAB existente com outra função deve mudar de ícone/rótulo ou migrar para uma ação contextual — documente a decisão.
- Em telas expandidas/paisagem: grupo no painel direito, alinhado ao início, largura máx. 360 dp.

### C9. Cadastro e edição (formulário)

> Reaproveite formulário e validações existentes se adequados. Complete somente lacunas reais (F5).

**Estrutura**

1. **Pré-visualização ao vivo** no topo: o **mesmo** `CardFace` com os dados em edição (banco, titular, final, apelido, cor) atualizando em tempo real — é o que torna a tela de formulário "do mesmo universo" do carrossel. Em modo de criação, com campos vazios, o cartão mostra rótulos de preenchimento genéricos (**"Seu banco", "Titular"** em 60% de opacidade), claramente marcados como pré-visualização e fora da árvore semântica de dados.
2. **Banco** — seletor existente (reutilize a lista; busca se a lista for longa).
3. **Pessoa associada** — seletor existente; permitir "nenhuma" se o domínio permitir.
4. **Final do cartão** — campo numérico, **máximo 4 dígitos**, ajuda "Somente os 4 últimos dígitos". Se o formulário atual aceitar mais dígitos, **não aceite nem armazene o número completo**; truncar/validar e relatar `[INEGOCIÁVEL]`.
5. **Apelido/tipo/bandeira** — somente campos que já existem.
6. **Cor** — linha rolável: chip "Automática" + 12 amostras circulares de 48 dp (gradiente diagonal, anel de 3 dp e ✓ na selecionada; `contentDescription` "Cor Petróleo, selecionada").
7. **Limite, fechamento, vencimento** — `[CONDICIONAL]`.
8. **Ações:** "Cadastrar" / "Salvar alterações" (primário, desabilitado enquanto inválido ou salvando) e "Cancelar". Em edição, "Excluir cartão" (texto `error`, abre C10).

**Modo criação × edição** (diferença visual **e** funcional): título "Novo cartão" × "Editar cartão"; botão "Cadastrar" × "Salvar alterações"; edição preserva banco, pessoa, cor e demais campos suportados e **atualiza o cartão existente sem duplicar** (mesmo ID); salvar atualiza carrossel e detalhes **sem reiniciar o app**.

**Validação:** erros em linha com `isError` + `supportingText`, anunciados por leitor de tela; validar ao perder o foco e ao enviar. Mensagens em C11. Descartar alterações com tela suja pede confirmação ("Descartar alterações?").

**Falha do repositório:** manter dados digitados, exibir mensagem útil e permitir tentar de novo (nunca fechar a tela perdendo o preenchimento).

### C10. Exclusão

1. Confirmação clara antes de exclusão permanente (diálogo `AlertDialog`, forma `dialog`), explicando **o que será removido** sem alarmismo.
2. **Descubra as relações reais** (A5, item 9) e escolha **uma** variante — nunca apague histórico silenciosamente:

| Situação real (Room/regras) | Variante do diálogo | Comportamento |
|---|---|---|
| Sem registros associados | Simples | Exclui |
| Registros associados que **permanecem** (FK nula/SET_NULL/regra atual) | Informa quantos permanecem "sem cartão" | Exclui só o cartão |
| Registros que **impedem** a exclusão (regra atual) | Mensagem de bloqueio com caminho ("Ver registros" só se o destino existir) | Não exclui |
| Cascata já existente e aceita pela governança | Informa **quantidade** que também será removida; botão com ênfase destrutiva | Só prossegue se a regra atual for essa; **nunca introduzir cascata nova** |

3. Após confirmar: operação pelo repositório oficial; estado de "excluindo" (botão desabilitado, sem duplo toque); depois aplicar C4.4 e mostrar *snackbar* "Cartão excluído".
4. Nenhuma ação, seleção ou estado pode apontar para o cartão excluído.
5. Testar remoção do **único**, do **primeiro**, do **último**, de um **intermediário** e do **atualmente selecionado**.

Se a operação exigir mudança de domínio ou migração: faça a **alteração mínima correta**, documente o motivo e adicione testes de migração.

### C11. Estados obrigatórios e microcopy (pt-BR)

> Textos sugeridos — ajuste ao que o produto realmente faz; **todos em `strings.xml`**.

| Estado | Composição | Texto sugerido |
|---|---|---|
| **Nenhum cartão** | Cartão-fantasma tracejado (raio `card`) + título + texto + CTA primário | "Nenhum cartão cadastrado" · "Cadastre seus cartões para acompanhá-los por banco e por pessoa." · **Cadastrar cartão** · rodapé `bodySmall`: "O CardControl organiza seus cartões. Ele não faz pagamentos nem acessa sua conta bancária." |
| **Um cartão** | Cartão centralizado; sem indicador; ações normais | — |
| **Vários cartões** | Carrossel + indicador | — |
| **Carregando** | Cartão-esqueleto (forma `card`, cintilação suave; parado com movimento reduzido) | — |
| **Falha de leitura** | Mensagem + ação | "Não foi possível carregar seus cartões." · **Tentar novamente** |
| **Falha de operação** | *Snackbar* com ação | "Não foi possível salvar. Tente novamente." |
| **Sucesso** | *Snackbar* | "Cartão cadastrado" · "Cartão atualizado" · "Cartão excluído" |
| **Validação** | Erro em linha | "Escolha o banco" · "Informe os 4 últimos dígitos" · "Use exatamente 4 dígitos" · "Escolha a pessoa" (se obrigatória) |
| **Confirmar exclusão** | Diálogo | Título "Excluir cartão?" · "O cartão {banco} final {1234} será removido do CardControl. Isso não afeta seu cartão real nem sua conta no banco." · **Cancelar** / **Excluir** |
| **Exclusão bloqueada** | Diálogo | "Não é possível excluir agora" · "Este cartão tem {n} registros associados. Resolva-os antes de excluir." · **Entendi** |
| **Verso sem dados** | Verso simples | "Verso do cartão. Toque para voltar à frente." |
| **Selecionado removido/atualizado** | Reconciliação silenciosa (C4.4) | — |
| **Campos ausentes** | Linhas omitidas, layout reorganizado | — |
| **Tema claro Vitrine** | Petróleo & Cobre fixo + semânticos fixos | — |
| **Tema escuro Grafite** | Grafite fixo + semânticos fixos | — |
| **Seguir o sistema** | Resolve ao modo do sistema em tempo real | — |
| **Fonte ampliada / tela estreita** | Regras de B5 e C1 | — |

### C12. Preferências de aparência

- Seção **"Aparência"** nas configurações existentes (ou local equivalente na navegação atual).
- **Tema:** `SingleChoiceSegmentedButtonRow` (ou componente equivalente já estável) com 3 opções — **Sistema · Claro · Escuro** — cada uma com ícone e rótulo; estado selecionado também comunicado por semântica (`selected`).
- **Paleta de cores:** não criar switch de Dynamic Color. Explicar, se houver texto de apoio para aparência, que o CardControl usa a paleta fixa Vitrine independentemente do papel de parede e das cores do Android.
- **Pré-visualização** (opcional): miniatura de cartão **rotulada "Exemplo"** refletindo a escolha atual.
- A mudança é **imediata** (sem reiniciar) e persiste após recriar a Activity. A aba/tela de configurações permanece onde estava.


---

## Parte D — Engenharia

### D1. Arquitetura e qualidade do código

Respeite as fronteiras de UI, domínio e persistência existentes. **Não crie arquitetura paralela.**

- Estado de **negócio** no ViewModel/camada definida pelo projeto; estado **puramente visual** (flip, rotação, escala) no componente.
- **Derive** valores em vez de armazená-los duplicados; sem estado duplicado entre tela, ViewModel e componente.
- Seleção por **identidade** (ID) e `key` estável em listas.
- **Nenhum I/O, consulta de banco ou trabalho pesado na composição.**
- Falhas tratadas de forma explícita, conforme os padrões do projeto.
- Não duplicar `CardTile`, modelos, enums de bancos, repositórios ou a fonte de verdade de tema.
- Modelo de UI sugerido (criado na camada de apresentação, mapeado do domínio — **todos os campos opcionais refletem o que o modelo realmente tem**): `CardUi(id, bankId, bankName, holderName?, last4?, nickname?, kind?, flag?, colorSeed?, status?, limit?, closingDay?, dueDay?)`.
- Nomes claros, responsabilidades pequenas, Kotlin idiomático. Comentários explicam **motivo, invariante ou limitação** — nunca paráfrase óbvia.
- Sem `TODO`, sem botões simulados como substitutos silenciosos de funcionalidade necessária.
- Sem refatorações amplas fora do escopo; toda alteração adjacente precisa de justificativa técnica demonstrável.
- Textos em `strings.xml`; sem texto literal em composables.
- Dados de exemplo **somente** em `@Preview`/testes, identificados como fictícios.

**Estrutura sugerida (adapte aos módulos/pacotes reais)**

```
ui/designsystem/   CcColors.kt  CcMotion.kt  CcShapes.kt  CcType.kt  CcSpace.kt  CardControlTheme.kt  CcReduceMotion.kt
ui/cards/          CardFace.kt  CardBack.kt  FlippableCard.kt  CardCarousel.kt  CardPaletteResolver.kt
                   CardActionsRow.kt  CardsHomeScreen.kt  CardForm.kt  DeleteCardDialog.kt  PagerIndicator.kt
settings/          AppearanceSection.kt  (+ repositório de preferências já existente)
```

### D2. Esqueletos de código (ilustrativos — adapte às APIs e versões reais)

> Valide cada API na versão instalada antes de usar. Os trechos mostram **estrutura e invariantes**, não são para colar às cegas.

#### D2.1 Resolução de tema (função pura e testável)

```kotlin
enum class ThemeMode { SYSTEM, LIGHT, DARK }
data class AppearancePrefs(val mode: ThemeMode = ThemeMode.SYSTEM)

fun resolveDark(mode: ThemeMode, systemDark: Boolean) = when (mode) {
    ThemeMode.SYSTEM -> systemDark; ThemeMode.LIGHT -> false; ThemeMode.DARK -> true
}

/** O esquema é sempre Vitrine; o sistema decide somente a luminosidade em modo SYSTEM. */
fun resolveScheme(dark: Boolean): ColorScheme = if (dark) CcGrafite else CcLightFallback

@Composable
fun CardControlTheme(prefs: AppearancePrefs, content: @Composable () -> Unit) {
    val dark = resolveDark(prefs.mode, isSystemInDarkTheme())
    val scheme = remember(dark) { resolveScheme(dark) }
    val extras = remember(dark) { if (dark) CcExtrasDark else CcExtrasLight }
    val reduceMotion by rememberReduceMotion()
    CompositionLocalProvider(LocalCcExtras provides extras, LocalCcReduceMotion provides reduceMotion) {
        MaterialTheme(colorScheme = scheme, typography = CcTypography, shapes = CcShapes, content = content)
        // Nível B: MaterialExpressiveTheme(colorScheme = scheme, motionScheme = MotionScheme.expressive(), …) — somente se já disponível.
    }
}
```

#### D2.2 Movimento e movimento reduzido

```kotlin
object CcMotion {
    fun <T> spatialFast()    = spring<T>(dampingRatio = 0.6f, stiffness = 800f)
    fun <T> spatialDefault() = spring<T>(dampingRatio = 0.8f, stiffness = 380f)
    fun <T> spatialSlow()    = spring<T>(dampingRatio = 0.8f, stiffness = 200f)
    fun <T> effectsFast()    = spring<T>(dampingRatio = 1f,   stiffness = 3800f)
    fun <T> effectsDefault() = spring<T>(dampingRatio = 1f,   stiffness = 1600f)
    fun <T> effectsSlow()    = spring<T>(dampingRatio = 1f,   stiffness = 800f)
}

@Composable
fun rememberReduceMotion(): State<Boolean> {
    val resolver = LocalContext.current.contentResolver
    fun read() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    val state = remember { mutableStateOf(read()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { state.value = read() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return state
}
```

#### D2.3 Carrossel por ID estável + flip

```kotlin
@Composable
fun CardCarousel(
    cards: List<CardUi>,
    selectedId: Long?,                   // fonte de verdade: ViewModel
    onSelect: (Long) -> Unit,
    flipped: Boolean, onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pager = rememberPagerState(initialPage = cards.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)) { cards.size }
    val currentCards by rememberUpdatedState(cards)
    val currentSelected by rememberUpdatedState(selectedId)
    val scope = rememberCoroutineScope()

    // modelo -> pager (exclusão/edição/nova lista): sem animação para não gerar loop nem piscada
    LaunchedEffect(selectedId, cards) {
        val target = cards.indexOfFirst { it.id == selectedId }
        if (target >= 0 && target != pager.currentPage) pager.scrollToPage(target)
    }
    // pager -> modelo: somente quando a página ASSENTA
    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.collect { page ->
            currentCards.getOrNull(page)?.takeIf { it.id != currentSelected }?.let { onSelect(it.id) }
        }
    }

    HorizontalPager(
        state = pager, modifier = modifier,
        contentPadding = PaddingValues(horizontal = peekPadding, vertical = 16.dp),
        pageSpacing = 12.dp, beyondViewportPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(pager, pagerSnapDistance = PagerSnapDistance.atMost(1)),
        key = { cards[it].id },
    ) { page ->
        val card = cards[page]
        val offset = ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
        Box(
            Modifier
                .graphicsLayer { // lido na fase de desenho: sem recomposição por frame
                    val s = lerp(1f, 0.92f, offset); scaleX = s; scaleY = s
                    alpha = lerp(1f, 0.72f, offset); translationY = lerp(0f, 6.dp.toPx(), offset)
                }
                .zIndex(1f - offset),
        ) {
            val isSelected = card.id == selectedId
            FlippableCard(
                card = card, isSelected = isSelected, flipped = flipped && isSelected,
                onClick = { if (isSelected) onFlip() else scope.launch { pager.animateScrollToPage(page) } },
            )
        }
    }
}

@Composable
fun FlippableCard(card: CardUi, isSelected: Boolean, flipped: Boolean, onClick: () -> Unit) {
    val reduce = LocalCcReduceMotion.current
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, if (reduce) snap() else CcMotion.spatialDefault(), label = "flip")
    val showBack by remember { derivedStateOf { rotation > 90f } } // só UMA face existe na composição

    Box(
        Modifier
            .graphicsLayer { rotationY = rotation; cameraDistance = 16f * density }
            .clickable(role = Role.Button, onClickLabel = if (isSelected) stringResource(R.string.card_flip) else null, onClick = onClick)
            .semantics { stateDescription = if (showBack) backLabel else frontLabel }
    ) {
        if (!showBack) CardFace(card, isSelected)
        else Box(Modifier.graphicsLayer { rotationY = 180f }) { CardBack(card) } // contrarrotação: nunca espelhado
        // Com movimento reduzido: substituir a rotação por Crossfade(showBack) de ~150 ms e manter rotationY = 0.
    }
}
```

#### D2.4 Paleta de cartão (tom fixo ⇒ contraste garantido)

```kotlin
@Immutable data class CardPalette(val start: Color, val end: Color, val back0: Color, val back1: Color)

/** Presets: valores fixos da tabela B4.2 (testáveis). Semente livre: converter para LCh e fixar L*. */
fun cardPalette(seed: CardColorSeed): CardPalette = when (seed) {
    is CardColorSeed.Preset -> CcCardPresets.getValue(seed.id)
    is CardColorSeed.Argb   -> derive(seed.argb)            // h, c' = clamp(c, 8, 56); start L*38; end L*22 (0,85·c'); verso L*22→16
    CardColorSeed.Auto      -> error("resolva Auto -> Preset antes (hash estável do ID do banco)")
}
// Cálculo: androidx.core.graphics.ColorUtils (colorToLAB / LABToColor) basta; não adicione biblioteca de cor.
// Conversões que saírem do gamut: reduzir o croma (não o L*), pois o L* é o que sustenta o contraste.
```

#### D2.5 Grupo "Ver mais" + "+" (composição nativa)

```kotlin
@Composable
fun SeeMoreAndAdd(onSeeMore: () -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.heightIn(min = 56.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        PressMorphButton(onClick = onSeeMore, outer = 28.dp, inner = 6.dp, startSide = true,
            container = MaterialTheme.colorScheme.secondaryContainer, content = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f).widthIn(min = 140.dp)) { Text(stringResource(R.string.see_more)); Icon(chevron, null) }
        PressMorphButton(onClick = onAdd, outer = 28.dp, inner = 6.dp, startSide = false,
            container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(56.dp).semantics { contentDescription = addCardLabel }) { Icon(addIcon, null) }
    }
}
// PressMorphButton: Surface/Button com interactionSource; raio anima 28 -> 16 dp ao pressionar via CcMotion.spatialFast().
```

### D3. Desempenho

- Alvo: carrossel fluido (sem *jank* perceptível) em um aparelho de entrada. Se o projeto já tiver Macrobenchmark/Perfetto, use; **não crie infraestrutura nova** só para isso.
- `graphicsLayer { … }` com lambda (leitura na fase de desenho) para escala/alpha/rotação/translação; **proibido** ler `pageOffset` na composição para esses efeitos.
- `Brush` do gradiente e `Shape`s em `remember(card.id, seed, isDark)`; **sem alocação** dentro de `graphicsLayer`/`drawBehind` a cada frame.
- `derivedStateOf` para `mostrarVerso`, `peek`, `offset`; **sem** `Modifier.blur`; sombra apenas no cartão selecionado e vizinhos imediatos.
- `key` estável no pager; `beyondViewportPageCount = 1`; evitar recomposição de todas as páginas ao trocar a seleção (passar `isSelected` como parâmetro estável; modelos `@Immutable`).
- Verifique recomposições no Layout Inspector (contagem) e corrija contagens anormais.

### D4. Acessibilidade — checklist de componente

- [ ] Alvos de toque **≥ 48 dp** (botões principais 56 dp); espaçamento mínimo de 8 dp entre alvos.
- [ ] Contraste: texto **≥ 4,5:1**; texto grande (≥ 18 sp ou 14 sp negrito) e ícones/limites de componentes **≥ 3:1** — inclusive sobre o cartão e sobre o palco tingido, nos 2 temas e com sementes dinâmicas extremas.
- [ ] Informação nunca só por cor (estado do cartão, erro, sucesso, seleção).
- [ ] TalkBack: ordem lógica (top bar → carrossel → cabeçalho → ações → grupo "Ver mais"/"+"); carrossel com posição "n de N" e ações "anterior/próximo"; apenas a face visível do cartão acessível; `stateDescription` do flip; diálogos com foco inicial e retorno de foco; erros de campo anunciados.
- [ ] Fonte **1,0×, 1,3×, 2,0×** sem corte, sobreposição ou perda de ação (C1/B5).
- [ ] Movimento reduzido (B2.6) e animações interrompíveis.
- [ ] Foco por teclado/D-pad/Switch Access visível (3 dp) e ordem coerente; sem armadilhas de foco no pager.
- [ ] Rótulos que identificam o cartão em ações ambíguas ("Editar cartão Nubank final 1234").
- [ ] Respeitar contraste/“texto em negrito”/tamanho de exibição do sistema.

### D5. Privacidade e segurança do que é exibido

- Mostrar **no máximo os 4 últimos dígitos**; nunca número completo, CVV, validade, senha ou credenciais, nem como "exemplo".
- Não registrar (log/analytics/crash) dados do cartão, titular ou valores; `toString()` de modelos sem campos sensíveis.
- Se o modelo atual armazenar mais do que os 4 últimos dígitos, **não migre nem exiba**; relate como risco com próximo passo concreto.
- Nenhuma permissão nova, nenhuma chamada de rede nova, nenhuma dependência nova sem justificativa técnica demonstrável.
- Não alterar assinatura, keystores, secrets, workflows de release.


---

## Parte E — Verificação, aceitação e relatório

### E1. Plano de testes obrigatório

Ajuste às ferramentas **já presentes** (JUnit, Compose UI Test, Room in-memory, `MigrationTestHelper`, screenshot tests **se já existirem**). Testes **determinísticos**: sem rede real, sem credenciais privadas, sem relógio real. Não reduza testes de integração existentes a testes isolados.

#### Tema e cor

- **Resolução de tema:** testar `resolveDark` para os 3 modos (`SYSTEM`, `LIGHT`, `DARK`) em sistema claro e escuro; confirmar que o esquema é sempre `CcLightFallback` ou `CcGrafite` e não depende do SDK, papel de parede ou Dynamic Color.
- Persistência e restauração da preferência; **migração** de flag antigo (D-03), se existir.
- Invariância de cores: alterações de papel de parede, `colorAccent` e Dynamic Color do Android não alteram os tokens, as telas ou as barras do CardControl.
- Recriação de Activity sem flash e sem perda de preferência (teste instrumentado quando viável).
- **Contraste (JVM, puro):** (a) pares `on*/*` dos esquemas Claro e Grafite (≥ 4,5:1; `outline` ≥ 3:1); (b) semânticos B3.3 (≥ 4,5:1); (c) **12 predefinições × 3 níveis de texto** sobre `start`+brilho (≥ 5,6 / 4,8 / 4,3:1, conforme B4.1); (d) função `derive` com **≥ 200 sementes aleatórias** (incluindo cinzas, amarelos e neons) mantendo os mesmos mínimos; (e) `onSurface` sobre o palco tingido (B4.6) para as 12 predefinições nos 2 temas (≥ 4,5:1).
- Testes de **previews/galeria**: `@PreviewLightDark`, `@PreviewFontScale`, `@PreviewScreenSizes` para `CardFace`, tela completa, formulário e diálogos — usados na inspeção visual (E2). As previews devem aplicar os esquemas Vitrine fixos, não cores dinâmicas do Android.

#### Carrossel e seleção

- Lista vazia, **um** cartão e **vários**.
- Troca de página reporta o ID correto **apenas ao assentar**.
- Identidade da seleção após atualização da lista (reordenar, inserir antes/depois, editar).
- Exclusão do selecionado, do primeiro, do último e do único (política C4.4).
- **Ações sempre vinculadas ao ID certo**, inclusive durante a transição entre páginas.
- Recomposição/rotação/morte de processo sem mudança indevida de seleção.
- Duas emissões seguidas do repositório durante arrasto não quebram o gesto nem a seleção.

#### Flip

- Alternar frente/verso; segundo toque durante a animação reverte sem salto.
- Dados secundários ausentes (verso simples).
- **Apenas a face visível** acessível (árvore semântica); `stateDescription` correto.
- Movimento reduzido: sem rotação, com fade.
- **Nenhuma exposição de dados proibidos** (teste que varre o texto renderizado de frente e verso).
- Troca de seleção volta o cartão anterior à frente (D-07).

#### Cadastro, edição e exclusão

- Validação de campos (final com exatamente 4 dígitos; banco obrigatório etc.).
- Edição atualiza o cartão existente **sem duplicar**; banco e pessoa persistem.
- Exclusão exige confirmação e mantém consistência das relações (C10: cada variante).
- Tratamento de falha do repositório (dados digitados preservados; nova tentativa).
- **Migração Room** testada se o modelo foi alterado.

#### UI e integração

- Tema claro, escuro e fallback; larguras diferentes; fonte ampliada (1,3× e 2,0×).
- Estados vazio, carregando e erro; semântica e navegação por leitor de tela onde a infraestrutura permitir.
- **Integração:** se mudanças de UI/seleção afetarem integrações de notificação ou registros associados, testar o fluxo **interface → filtro por pacote (quando relevante) → resolução do cartão → persistência → deduplicação**. Não substituir por testes isolados de parser/repositório.

### E2. Verificação visual real — não concluir apenas pela compilação

Execute o app em emulador ou dispositivo de teste (se o ambiente permitir), **inspecione as capturas** e corrija os problemas de layout encontrados. **Não afirme revisão visual se só houve compilação ou teste unitário.** Sem emulador: registre a limitação, conclua o que for viável, **sem inventar evidências**.

**Matriz mínima de capturas** (nomear `NN_tema_estado_dispositivo.png`)

| # | Estado | Tema | Dispositivo/condição |
|---|---|---|---|
| 01 | Carrossel com vários cartões, central selecionado | Claro Vitrine (Petróleo & Cobre) | Celular padrão |
| 02 | Idem, com papel de parede/cor de destaque do Android diferente | Claro Vitrine (paleta deve permanecer idêntica) | Android 12+ |
| 03 | Idem | **Grafite** fixo | Celular padrão |
| 04 | Idem, com papel de parede/cor de destaque do Android diferente | **Grafite** (paleta deve permanecer idêntica) | Android 12+ |
| 05 | Barras de status/navegação e tela completa | Cores das barras fornecidas pelo tema Vitrine | Celular padrão |
| 06 | Verso do cartão | Claro e Grafite | Celular padrão |
| 07 | Um único cartão | Claro e Grafite | Celular padrão |
| 08 | **Sem cartões** (vazio) | Claro e Grafite | Celular padrão |
| 09 | Cadastro (com pré-visualização) | Claro | Celular padrão |
| 10 | Edição | Grafite | Celular padrão |
| 11 | Diálogo de exclusão (e variante de bloqueio, se aplicável) | Claro e Grafite | Celular padrão |
| 12 | Tela estreita (≈ 320–360 dp) | Claro | Peek de 20 dp |
| 13 | **Fonte 1,3× e 2,0×** | Claro e Grafite | Celular padrão |
| 14 | Paisagem / tablet (dois painéis) | Claro | Se suportado |
| 15 | Preferências de aparência | Claro e Grafite | Celular padrão |
| 16 | Movimento reduzido (flip por fade) | Claro | Gravação curta |

**Comandos de apoio (adapte ao ambiente)**

```bash
adb devices
adb shell cmd uimode night yes   # no | auto
adb shell settings put system font_scale 1.3        # 1.0 | 2.0
adb shell wm size 720x1280 && adb shell wm density 320     # reset: wm size reset; wm density reset
adb shell settings put global animator_duration_scale 0    # movimento reduzido; restaurar para 1
mkdir -p build/reports/visual-qa
adb exec-out screencap -p > build/reports/visual-qa/01_claro-vitrine_carrossel_celular.png
adb shell screenrecord /sdcard/flip.mp4   # depois: adb pull /sdcard/flip.mp4 build/reports/visual-qa/
```

Para testar a invariância da paleta: em Android ≥ 12, troque temporariamente o papel de parede/cor de destaque do sistema (amarelo-claro, azul saturado, verde-limão, magenta, laranja queimado, cinza) e capture o app após cada alteração. As cores do CardControl devem permanecer idênticas para o mesmo modo Vitrine. Ao final, **restaure** as configurações do dispositivo (`font_scale`, `wm`, animações, modo noturno e papel de parede/cor de destaque, se alterados).

**Checklist de inspeção por captura:** sombra não cortada · *peek* equilibrado dos dois lados · texto sem corte/sobreposição · contraste legível sobre cartão e palco · alinhamento do cartão no centro · nada coberto por barras do sistema/grupo inferior · estados (selecionado × vizinho) distintos · cantos e proporção do cartão · aparência intencional (não "cor aplicada por cima") · nenhum dado fictício visível.

**Evidências:** salvar em `build/reports/visual-qa/` (fora do controle de versão) ou anexar ao `walkthrough.md`; **não** commitar capturas sem convenção do repositório que o peça. Registrar cada defeito encontrado e sua correção no relatório.

### E3. Revisão independente e correções

Após implementar, conduza uma segunda revisão, **preferencialmente com um agente somente leitura**, com acesso ao diff e aos critérios deste prompt. O revisor **não faz mudanças paralelas** sem coordenação; entrega achados com **severidade** (Bloqueador / Importante / Menor), arquivo e linha, evidência e correção sugerida.

O revisor verifica:

- Fidelidade à direção Vitrine e à referência descrita.
- Tema claro Petróleo & Cobre e tema escuro Grafite realmente aplicados de forma fixa, mesmo quando o papel de parede ou o Dynamic Color do Android mudam.
- *Peek* equilibrado e responsivo; proporção e legibilidade do cartão.
- ID do cartão selecionado correto em **todas** as ações, no menu, no diálogo e no formulário.
- Bugs de estado/recomposição e navegação (loops pager↔modelo, índice inválido, flip disparando por recomposição).
- Acessibilidade, contraste e movimento reduzido (D4).
- Compatibilidade de APIs e dependências; **nenhuma API experimental fora do pacote de design system**.
- **Uso de tokens:** nenhuma cor/raio/duração literal fora dos arquivos de tokens.
- Funcionalidade real de **todos** os botões (nada fictício); "Ver mais" e "+" com destinos reais e distintos.
- Riscos de exclusão de dados e regressões de Room; nenhuma cascata nova.
- Privacidade (D5): nada além dos 4 últimos dígitos, nada em logs.
- Código duplicado (`CardTile`, lista de bancos), abstrações excessivas e alterações fora do escopo.

Corrija bloqueadores e problemas relevantes e **repita os testes afetados**.

### E4. Validação final e controle de escopo

Execute os comandos **documentados pelo projeto**. Quando disponíveis, execute pelo menos:

- Testes unitários pertinentes; testes de UI/instrumentados pertinentes.
- Lint e verificações estáticas (detekt/ktlint se existirem); compilação Kotlin.
- Build Android **debug**.
- Testes de migração/segurança se Room ou persistência foram alterados.

Inspecione `git diff --check`, o diff completo e `git status`. Confirme que **nenhum** arquivo sensível, keystore, credencial ou artefato de build entrou por acidente (por exemplo, `git diff --name-only | grep -Ei "keystore|\.jks|\.p12|secret|\.env|google-services"` deve ser vazio). Não remova alterações preexistentes de outros trabalhos.

Atualize documentação e estado canônico **apenas** conforme a governança real do repositório. **Não alegue** testes, captura, revisão, build ou validação que não foram executados. Não publique release nem altere CI/CD sem que a análise prove necessidade no escopo. O objetivo é uma mudança de produto **isolada, revisável e segura**.

### E5. Critérios de aceitação — todos obrigatórios

**Produto e comportamento**

- [ ] A solução foi implementada no aplicativo Android real, e não apenas descrita.
- [ ] Tema Sistema, Claro e Escuro funcionais e persistidos; sem flash na abertura/recriação.
- [ ] A paleta Vitrine sobrescreve as cores dinâmicas do Android em todas as telas e componentes do aplicativo; Claro = Petróleo & Cobre e Escuro = Grafite, sem acento dinâmico herdado.
- [ ] O carrossel funciona com zero, um e vários cartões e mostra vizinhos parcialmente quando adequado.
- [ ] O cartão selecionado e as ações relacionadas **nunca divergem** (por ID).
- [ ] O flip funciona de modo acessível, respeita movimento reduzido e não expõe dados sensíveis.
- [ ] Banco e pessoa associada identificados corretamente, conforme o modelo real.
- [ ] Cadastro, edição e exclusão funcionam — ou as lacunas existentes foram documentadas com precisão, **sem botões falsos**.
- [ ] "Ver mais" e "+" têm destinos reais, distintos e acessíveis.
- [ ] Nenhuma interface sugere pagamento, transferência ou NFC não suportados.
- [ ] Estados vazios, de falha e diferentes tamanhos de tela/fonte tratados.

**Design system**

- [ ] Tokens `Cc*` centralizados; nenhuma cor/raio/duração literal em telas.
- [ ] Contraste verificado por testes automatizados (B3, B4) e por inspeção visual nos 2 temas.
- [ ] Paleta de cartão pelo algoritmo B4.1; 12 predefinições; cor "Automática" determinística.
- [ ] Movimento conforme B7, interrompível, com alternativa para movimento reduzido.
- [ ] Nenhuma API experimental fora do pacote de design system; nenhuma atualização ampla de dependências.

**Qualidade e processo**

- [ ] Testes pertinentes e build executados, com resultados **verdadeiros** documentados.
- [ ] Verificação visual real executada (ou limitação do ambiente registrada).
- [ ] Revisão independente sem bloqueadores pendentes.
- [ ] Diff limitado ao escopo, sem arquivos sensíveis ou mudanças incidentais.
- [ ] Estado do Git e limitações da validação claros no relatório final.

### E6. Formato obrigatório do relatório final

Ao concluir, apresente (também como `walkthrough.md`, se o ambiente gerar artefatos):

1. **Estado inicial:** branch, HEAD e existência de alterações prévias relevantes.
2. **Implementação:** o que mudou por componente/fluxo.
3. **Tema e refatoração de cores:** comportamento testado de Sistema/Claro/Escuro; inventário das cores antigas substituídas; confirmação de que as cores do sistema/papel de parede não alteram a paleta Vitrine; barras da janela do app alinhadas ao tema.
4. **Funcionalidades:** o que está realmente pronto e o que permanece pendente.
5. **Testes:** comandos executados e resultados reais; declarar **separadamente** o que não foi possível executar.
6. **Inspeção visual:** telas verificadas (referenciando a matriz E2) e evidências geradas, ou limitações do ambiente.
7. **Revisão:** achados independentes (com severidade) e correções realizadas.
8. **Git:** branch, resumo do diff, arquivos sensíveis verificados e estado final.
9. **Riscos/restantes:** somente problemas reais, com próximo passo concreto.
10. **Decisões e desvios:** lista das decisões D-xx tomadas (e dos desvios deste prompt, com o motivo técnico).
11. **Rastreabilidade:** tabela *critério de aceitação → teste/evidência*.

### Regra final

**Não reduza esta tarefa a uma troca de cores pontual ou a um carrossel cosmético.** Entregue uma refatoração global e consistente do sistema cromático: todas as telas e componentes do aplicativo devem usar os tokens Vitrine; o tema claro Petróleo & Cobre e o Grafite escuro devem prevalecer sobre as cores dinâmicas do Android; barras de status/navegação da janela do app devem respeitar o tema ativo. Preserve o restante da experiência: carrossel responsivo, cartão com identidade legível, interações acessíveis e ações verdadeiras. Não invente capacidades financeiras nem refatore lógica de negócio sem relação com tema/cores. A qualidade deve ser demonstrada pelo aplicativo executado, pelos testes, pela revisão e pelo diff final.

---

## Apêndice — Referência rápida da Vitrine

| Tema | Valor |
|---|---|
| Proporção do cartão | 1,586 : 1 · raio 24 dp · padding 20 dp |
| Gradiente do cartão | linear 135°, `L* 38 → 22`, croma `c' → 0,85·c'`, brilho branco 8% |
| Texto no cartão | branco 100% / 88% / 80% (decorativo) |
| *Peek* | 28 dp (360–599 dp) · 20 dp (< 360 dp) · espaçamento entre páginas 12 dp |
| Vizinhos | escala 0,92 · opacidade 0,72 · Y +6 dp |
| Flip | rotação Y 180° · `spatialDefault` · câmera 16×density · contrarrotação no verso |
| Grupo "Ver mais" + "+" | altura 56 dp · segmentos separados por 2 dp · cantos 28 externo / 6 interno · "+" 56×56 |
| Ações | contêiner 56 dp · largura mín. 72 dp · máx. 4 visíveis |
| Molas | `spatialFast 0,6/800` · `spatialDefault 0,8/380` · `spatialSlow 0,8/200` · `effectsFast 1/3800` · `effectsDefault 1/1600` · `effectsSlow 1/800` |
| Tingimento do palco | α 0,14 (claro) · 0,20 (escuro) |
| Alvos de toque | ≥ 48 dp (principais 56 dp) |
| Contraste | texto ≥ 4,5:1 · grande/ícones/limites ≥ 3:1 |
| Fallback claro | primário `#006972` · terciário cobre `#83532D` · fundo `#F4FBFB` |
| Grafite | fundo `#111717` · container `#1C2222` · primário `#72D5E1` (se o projeto já define o Grafite, **o do projeto prevalece**) |
