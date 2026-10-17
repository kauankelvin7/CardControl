# CardControl — Prompt mestre de redesign visual, UX e validação em dispositivo

> **Executor:** Antigravity, com orquestração de agentes e subagentes quando disponível
> **Projeto:** `kauankelvin7/CardControl`
> **Missão:** transformar a interface atual em um produto Android coeso, leve, premium e fácil de entender, inspirado na clareza, composição e sensação tátil do Google Wallet — sem copiar marcas, logotipos ou funções de pagamento.
> **Modo de trabalho:** descobrir → planejar → implementar → instalar no telefone conectado → capturar → inspecionar visualmente → corrigir → repetir → testar → revisão independente → entrega documentada.
> **Não basta compilar. A validação visual real é obrigatória.**

---

## 0. Instrução principal: execute até a entrega, não pare no plano

Você recebeu autorização para conduzir a tarefa de ponta a ponta no repositório CardControl, usando o telefone Android conectado ao ambiente do Antigravity para testar a aplicação real.

Não entregue apenas uma análise, um plano, previews estáticos ou uma implementação que apenas compila. Faça as mudanças necessárias, instale a versão de debug no dispositivo conectado, abra todas as telas relevantes, examine capturas reais, corrija os problemas encontrados e repita o ciclo até os critérios de aceitação serem atingidos.

**Não peça ao usuário que escolha novamente direções já fechadas neste documento.** Tome decisões de produto coerentes com as regras abaixo. Pergunte somente se encontrar um bloqueio genuíno que não possa ser resolvido com o código, os dados disponíveis, a governança ou estas instruções.

O redesign deve ser deliberado: repense a hierarquia e a organização das telas, não apenas cores, raios ou ícones. Use as capturas fornecidas na conversa como evidências do estado atual e como referência visual. A imagem de referência do Google Wallet define o nível de simplicidade e acabamento desejado, não uma cópia literal de sua identidade.

## 1. Resultado de produto desejado

O CardControl deve parecer **um aplicativo único, desenhado com intenção**, e não uma coleção de painéis e componentes de bibliotecas diferentes.

A experiência desejada combina:

- a superfície clara e calma, a hierarquia e a apresentação de cartões do Google Wallet;
- a identidade fixa do CardControl, baseada no Design System Vitrine;
- o tema escuro Grafite já existente;
- cartões como objetos visuais protagonistas, com cor própria, gradiente controlado e profundidade elegante;
- ícones expressivos e dimensionalidade sutil, com uma linguagem consistente;
- navegação simples e previsível;
- texto curto, humano e orientado à tarefa;
- ações reais, sem botões decorativos nem promessas de pagamentos que o produto não oferece.

O aplicativo é para organizar cartões, pessoas/titulares, faturas, compras, pagamentos registrados e automações realmente implementadas. **Não é uma carteira de pagamento e não move dinheiro.**

### Princípios de UX inegociáveis

1. Uma tela deve ter um objetivo principal fácil de reconhecer.
2. Menos containers grandes, menos texto explicativo e menos elementos concorrendo por atenção.
3. A informação não deve ser repetida sem necessidade.
4. A ação principal aparece perto do conteúdo ao qual se refere.
5. Edição e exclusão são secundárias; exclusão requer confirmação e respeita vínculos contábeis.
6. Estados vazios devem explicar o próximo passo em uma frase e oferecer uma ação real.
7. Uma função não implementada não deve aparecer como se estivesse pronta.
8. Acessibilidade, contraste, fonte ampliada, alvos de toque e movimento reduzido são parte do design, não tarefas posteriores.
9. A composição deve funcionar em telefone compacto, fonte ampliada, orientação paisagem e larguras maiores.
10. O visual final deve ser julgado com o app rodando no dispositivo real, não só em `@Preview`.

---

## 2. Direção visual fechada

### 2.1 Design System Vitrine como fonte única de verdade

Preserve e consolide o Design System Vitrine que já foi iniciado. Não crie outro design system paralelo nem reintroduza Liquid Glass.

- **Tema claro:** base fixa Vitrine, com superfícies em marfim/tons claros neutros, petróleo como acento principal e cobre em uso secundário e controlado. A aparência deve ser próxima da limpeza e serenidade da referência, sem ser uma cópia.
- **Tema escuro:** Grafite, superfícies profundas, ciano/turquesa controlado e cobre claro apenas quando apropriado.
- **Cores de cartão:** independentes do tema global e definidas pelos dados/preferência de cada cartão. Um cartão laranja pode ser laranja, mas não pode criar um retângulo laranja recortado no fundo da tela.
- **Paleta fixa:** a interface não deve ser recolorida pelo wallpaper, Monet/Dynamic Color ou cor de destaque do sistema. `Sistema` escolhe apenas claro/escuro a partir do sistema, mantendo a paleta fixa Vitrine.
- **Preferência do usuário:** conservar Sistema/Claro/Escuro e sua persistência existente. Não sobrescrever a preferência de instalações existentes. Para instalação realmente nova, avalie tema claro como experiência inicial, mas confirme a fonte real da preferência e documente a decisão sem criar uma segunda fonte de verdade.
- **Barras do sistema:** status bar, navegação, cutout e edge-to-edge precisam compor com o tema ativo. Não pode haver faixa preta indevida nem texto atrás de relógio/ícones.

### 2.2 Linguagem de superfícies

- Preferir **um palco contínuo** a uma pilha de retângulos com fundos contrastantes.
- Remover fundos retangulares auxiliares que aparecem atrás dos cartões sem função semântica clara.
- Palco: uma superfície global Vitrine. Pode haver tingimento amplo, muito sutil e gradativo pela cor do cartão selecionado; nunca um bloco marrom com limites abruptos.
- Grupos de configurações: lista simples com divisores discretos ou grupos tonais leves, não grandes cartões dentro de cartões dentro de outra superfície.
- Containers de ação: cor tonal e dimensão moderada, sem cada ação virar um bloco enorme.
- Profundidade: elevação ou borda sutil, coerente com o tema. Sem vidro fosco, blur pesado, neon, brilho excessivo ou sombra preta exagerada.
- Raios, espaços, tipografia, elevação, estados e durações devem vir dos tokens centralizados do Vitrine.
- Elimine cores literais espalhadas nos composables. Cores hardcoded podem existir nos arquivos canônicos de tokens e nas paletas próprias dos cartões, não em telas aleatórias.

### 2.3 Ícones expressivos e 3D

Quero uma linguagem visual mais rica que ícones lineares genéricos, mas não uma mistura inconsistente de emojis, Material Symbols, desenhos de estilos diferentes e imagens aleatórias.

Defina uma linguagem de ícone única:

- ícones essenciais de navegação e controles permanecem legíveis, limpos e consistentes;
- ações principais podem usar ícones preenchidos e containers tonais com profundidade sutil, highlights e sombra coerentes;
- ilustrações 3D suaves são reservadas a poucos pontos com benefício real, como estado vazio de cartões ou onboarding;
- ícones dimensionais devem compartilhar perspectiva, iluminação, paleta, nível de detalhe e escala aparente;
- manter contraste e reconhecer o significado em tamanhos pequenos;
- não usar emojis como substituto de ícones de produto;
- não instalar bibliotecas enormes de ícones só por decoração;
- se forem criados assets próprios, usar recursos locais licenciáveis (VectorDrawable/SVG convertido ou raster otimizado), documentar a origem/licença e incluir fallback acessível.

**Não implemente ícones 3D pesados em todos os controles.** O resultado desejado é uma interface premium e coesa, não visualmente barulhenta. Mostre o efeito dimensional onde reforça a hierarquia; controles secundários continuam discretos.

### 2.4 Tipografia e escrita da interface

- Use a tipografia canônica já decidida no projeto; se Inter variável já estiver integrada, valide-a e reutilize-a. Não adicione outra família sem motivo comprovado.
- Hierarquia compacta: título de tela claro, título de seção menor, texto de apoio curto e rótulos consistentes.
- Números monetários alinhados e tabulares quando possível.
- Não usar texto inteiro em maiúsculas, salvo pequenos rótulos necessários e consistentes.
- Evitar títulos longos, descrições técnicas e repetição do nome da marca.
- Textos da UI em `strings.xml`, seguindo o padrão de idiomas já existente.
- Comentários no código só para explicar decisões/invariantes não óbvios. **Comentários de arquitetura, explicações do design system, notas de implementação e marketing não devem aparecer na interface para o usuário.**

---

## 3. Direção específica por tela

### 3.1 Estrutura global e cabeçalho

O atual bloco gigante arredondado que contém apenas **“CardControl”** ocupa espaço vertical e compete com o conteúdo. Remova essa cápsula como padrão de cabeçalho.

Crie uma top app bar compacta e nativa:

- símbolo/marca CardControl discreto à esquerda, junto de texto compacto quando ajudar a identidade;
- sem repetir um painel grande com o nome inteiro em todas as telas;
- o título/contexto da tela deve ser a principal hierarquia visual;
- ações contextuais no lado direito, quando existirem (ex.: mês anterior/próximo em Faturas; adicionar cartão em Cartões; nenhuma ação inútil na tela Automação);
- alturas, insets e comportamento consistentes entre abas;
- manter as funções de navegação reais existentes.

A navegação inferior deve ser simples, consistente e de altura adequada. O item selecionado usa indicador tonal refinado. Não alterar destinos ou regras de navegação inadvertidamente. Respeitar os insets inferiores e nunca deixar botões flutuantes encobrirem os rótulos/ícones.

### 3.2 Tela Cartões — prioridade visual máxima

A tela atual apresenta: cartão, repetição do nome do banco e final logo abaixo, três grandes ações coloridas, o botão “Ver faturas e limites”, um “+” separado e o botão flutuante “Adicionar cartão” que compete/sobrepõe a composição. O fundo atrás do cartão também cria um retângulo de cor com limites abruptos.

Redesenhe a hierarquia na seguinte ordem:

1. Top bar compacta com título “Cartões” e acesso claro para adicionar.
2. Carrossel horizontal como protagonista, com o cartão selecionado dominante e parte dos adjacentes visível quando existirem.
3. O cartão mantém aparência de objeto com gradiente, monograma, banco, titular se houver e somente os quatro últimos dígitos se os dados existirem.
4. Flip 3D opcional no toque do cartão selecionado, com verso contendo apenas dados reais e permitidos.
5. Um cabeçalho contextual compacto abaixo do carrossel. Não repetir informações que já estão perfeitamente legíveis no cartão. Usar essa região para dados que agregam valor (ex.: titular/apelido) e menu secundário.
6. Duas ações primárias visualmente equilibradas: **Ver fatura** e **Lançar compra**, se os destinos reais e contexto do domínio permitirem.
7. Ações secundárias como editar e excluir dentro de um menu contextual ou detalhe, não em três grandes tiles igualmente proeminentes. Excluir sempre pede confirmação e respeita bloqueios por histórico financeiro.
8. “Ver faturas e limites” somente se levar para destino real e necessário. Consolidar com “Ver fatura” se for a mesma finalidade.
9. Um único comando de cadastro de cartão. **Eliminar a duplicidade entre o botão `+`, “Ver mais” e um FAB “Adicionar cartão” se eles realizarem a mesma ação.** Escolher uma única ação canônica, preferencialmente ícone `+` na top bar ou um FAB pequeno/estendido que nunca cubra conteúdo.

Comportamento do carrossel:

- manter `HorizontalPager`/implementação existente quando compatível;
- usar ID estável de cartão como chave da seleção;
- vizinhos menores e ligeiramente menos proeminentes, sem rotacionar/distocer texto;
- indicador discreto para várias páginas;
- trocar de cartão atualiza cabeçalho e ações de maneira consistente;
- exclusão do cartão selecionado não pode deixar índice inválido nem conteúdo de outro cartão;
- sem cartão: estado vazio limpo, uma ilustração/ícone dimensional consistente e ação principal “Cadastrar cartão”;
- um cartão só: centralizado, sem indicador enganoso;
- vários cartões: permitir descobrir os adjacentes pelo peek e gesto.

O fundo da página deve continuar uniforme. Se a cor do cartão tingir o palco, a transição deve ser ampla e suave, sem um retângulo com corte visível. O cartão pode ter sua própria cor chamativa; o ambiente de fundo não pode competir com ele.

**Dados:** nunca inventar banco, titular, limite, datas, final ou estado. Previews podem usar dados claramente fictícios; o app instalado deve mostrar somente dados reais existentes.

### 3.3 Tela Faturas — conteúdo primeiro, não painéis redundantes

Na captura atual, o cartão “Fatura consolidada do mês”, um controle “Nova” de contraste fraco e o bloco “Primeiros Passos” consomem grande altura. Simplifique sem perder informação contábil.

Estrutura desejada:

1. Top bar compacta com “Faturas” e seleção do período integrada de forma discreta.
2. Um resumo do mês que destaque o total relevante, usando a hierarquia tipográfica correta e informação bruta/abatida apenas quando útil e sustentada pelos dados.
3. Uma barra/progresso somente se sua semântica e o valor forem reais e compreensíveis. Sem gráficos decorativos ou estatísticas inventadas.
4. Lista de faturas/lançamentos do período, agrupada com separação clara, se houver conteúdo.
5. Estados vazios diferentes conforme o estado real:
   - nenhum cartão cadastrado: orientar cadastro de cartão;
   - cartão existe, sem compras registradas no mês: “Nenhuma compra registrada neste mês” e ação real “Lançar compra”;
   - dados indisponíveis/erro de carga: mensagem curta e opção de tentar novamente quando houver suporte.
6. Remover o painel “Primeiros Passos” genérico quando o usuário já tiver concluído a configuração inicial. Quando realmente necessário, transformá-lo em um onboarding compacto e contextual com as etapas faltantes, sem parágrafos longos.
7. O botão “Nova” precisa ter rótulo claro (ex.: “Nova compra” se esse for o fluxo real), contraste adequado e estado de habilitação honesto. Não parecer desabilitado quando ativo.

Não exibir blocos de R$ 0,00 repetidamente se não ajudarem o usuário a compreender o estado. Não esconder valores reais relevantes para conseguir uma tela “limpa”. O conteúdo contábil tem prioridade sobre a estética.

### 3.4 Tela Automação — converter em configurações simples

A tela atual exibe “Automação & Ajustes”, um painel grande “Aparência & Tema Vitrine”, o subtítulo “Design System Vitrine com paleta sob medida” e um parágrafo sobre a imunidade a cores do wallpaper. Isso é vocabulário de implementação, não conteúdo útil para a pessoa.

A tela deve ser chamada **“Automação”**. Subtítulo opcional e curto: “Notificações e preferências do aplicativo.” Remova das strings visíveis nomes internos como “Design System Vitrine”, “paleta sob medida”, “imune ao papel de parede” e explicações arquiteturais.

Organize como listas compactas agrupadas:

**Grupo: Captura automática**
- ícone coerente;
- título “Captura de notificações” ou a denominação mais natural após verificar a função real;
- resumo de uma linha;
- estado de permissão claramente indicado;
- toque abre o fluxo real de concessão/gestão de permissão;
- quando permitido, apresentar estado ativo sem exagero visual; quando negado, CTA claro e específico.

**Grupo: E-mails**
- linha “Envio por e-mail” com resumo curto;
- indicar opcionalidade somente se for verdade;
- deixar campos de configuração de SMTP recolhidos em um fluxo/detalhe, se isso combinar com o estado atual;
- não expor campos de senha constantemente ao abrir a tela;
- senha de aplicativo deve continuar protegida, nunca logada ou exibida sem necessidade;
- mensagens de salvamento devem ser curtas e sem alegações de segurança não comprovadas.

**Grupo: Aparência**
- título “Aparência”;
- resumo “Tema claro, escuro ou do sistema”;
- permitir Sistema, Claro e Escuro num seletor compacto e acessível; não transformar o seletor em outro painel gigante;
- manter a preferência Vitrine fixa e não mencionar detalhes de Dynamic Color ao usuário.

Se os formulários precisarem continuar na mesma tela por motivos arquiteturais, organize-os em seções simples expansíveis. Não faça uma refatoração apenas cosmética enquanto preserva o excesso de texto e o tamanho desproporcional dos containers.

“Cofre Keystore” é termo técnico. Use uma expressão útil e verdadeira para o recurso apresentado; não prometa cofre de credenciais ou segurança além do que a implementação garante.

### 3.5 Formulários, modais e menus

Revise todos os formulários e diálogos alcançáveis por estas telas:

- cadastro/edição de cartão;
- exclusão;
- lançamento de compra;
- registro de pagamento;
- cadastro/seleção de pessoa/titular;
- configuração de e-mail;
- menus contextuais e seleção de período.

Aplicar a mesma tipografia, espaçamento, tokens, botões e campos. Evitar modal dentro de modal, excesso de contorno, duplicação de título e botões com a mesma importância visual.

No formulário de cartão, manter pré-visualização ao vivo se já implementada, mas garantir que ela não domine o formulário nem reduza excessivamente o espaço para campos. Validar que edição preserva os dados omitidos corretamente e que exclusão continua respeitando integridade de histórico financeiro.

---

## 4. Edge-to-edge e barras do sistema — requisito obrigatório

A barra de status e a navegação do Android devem se integrar ao fundo da tela. O problema original era uma faixa preta na parte superior quando a interface usava outro fundo.

- Auditar `MainActivity`, `AndroidManifest.xml`, tema XML e root composable.
- Implementar edge-to-edge de acordo com as versões Android/AndroidX reais do projeto.
- Fundo do aplicativo desenhado atrás das barras; conteúdos e controles continuam respeitando `WindowInsets`.
- Cor/contraste dos ícones da status bar e navigation bar coerentes com tema claro ou Grafite ativo.
- A preferência de aparência do app, e não apenas o modo noturno do Android, deve orientar ícones e fundo de janela quando o usuário seleciona tema diferente do sistema.
- Evitar duplo padding: conferir uso de `Scaffold` insets + `systemBarsPadding` + `navigationBarsPadding`.
- Nenhum título deve encostar no relógio, nenhum botão pode ficar atrás do recorte/ícones e a barra inferior do app deve ficar acima da navegação do sistema.
- Não usar apenas `statusBarColor` como tentativa superficial. Considerar scrims do fabricante, contrast enforcement, `WindowCompat`, `enableEdgeToEdge` e comportamento por versão, após inspecionar a implementação existente.
- Não alterar o Android globalmente, apenas a janela da Activity do CardControl.
- Validar em captura física que o fundo é contínuo, sem uma faixa preta artificial.

---

## 5. Descoberta obrigatória e cuidados com o repositório

Antes de editar:

1. Confirme root do repo, branch atual, HEAD completo, `git status`, mudanças staged/unstaged e arquivos não rastreados.
2. Leia `AGENTS.md`, `.agents/CARDCONTROL_STATE.md`, `CARDCONTROL_TASKS.yaml`, decisões/ADRs relevantes e skills locais de verificação/design. O estado canônico pode estar desatualizado; compare com o Git real.
3. Inspecione versões reais de Kotlin, Gradle, Compose BOM, Material 3, Room e API mínima/alvo.
4. Examine pelo menos `MainActivity.kt`, `MainScreen.kt`, `ui/theme/Theme.kt`, `ui/designsystem/CCTheme.kt`, tokens `CCColors/CCTokens`, componentes `CardTile`/cartões, diálogos, models, ViewModels, repositórios e testes.
5. Identifique componentes legados ainda ativos: `LiquidAmbientBackground`, `liquidGlassCard`, cores globais antigas, `TextPrimary` etc. Determine quais são realmente usados antes de apagar/substituir.
6. Verifique se coexistem duas fontes de tema (por exemplo `CardControlTheme` e `CCTheme`). Consolidar para **um caminho canônico** sem quebrar previews/testes nem introduzir regressões.
7. Confirme se a nova implementação Vitrine já existe localmente, quais commits estão locais e quais chegaram ao remoto. Não presuma que um relatório de uma execução anterior prova o estado atual.
8. Não faça checkout destrutivo, reset, clean, remoção de dados, limpeza de diretórios desconhecidos nem sobrescrita de trabalho local.
9. Não modifique keystores, assinatura, secrets, release workflows ou funcionalidades do parser de notificações fora do escopo.

**Contexto preliminar a revalidar:** no estado remoto que foi inspecionado, `design/v3-home-comparison` apontava para `d67b85e` e `MainScreen.kt` ainda continha referências a `LiquidAmbientBackground` e uma top bar em `liquidGlassCard`; o relatório local de execução anterior cita commits `ed1a563` e `8a47b12` que não estavam presentes no remoto consultado. Isso pode significar que a implementação está local e ainda não enviada. **Use o estado real da máquina atual como fonte de verdade; não descarte commits ou mudanças locais para reconciliar essa divergência.**

### Proteção do trabalho local

- Não execute `git reset --hard`, `git clean`, `git checkout --` sobre alterações do usuário, force-push nem remoção de dados de aplicação.
- Não misture redesign com branch dedicada a assinatura/release/hardening independente.
- Se necessário criar branch, primeiro entenda o estado local e crie-a sem perder mudanças.
- Não faça push, merge ou release sem verificar a autorização aplicável e as regras do projeto. A entrega local validada é obrigatória; publicar remotamente é etapa separada e não deve ser presumida.

---

## 6. Orquestração de agentes e subagentes

Use agentes paralelos para reduzir pontos cegos, com fronteiras explícitas para evitar edição simultânea dos mesmos arquivos.

### Agente 1 — Coordenador / Arquiteto (você)

Responsável pelo estado canônico, plano, escolhas de produto, resolução de conflitos e integração final. Só ele decide a fonte de verdade do tema e aprova a composição final. Não permita que subagentes definam direções visuais incompatíveis.

### Agente 2 — Auditor UX/UI, somente leitura

Analisa as capturas fornecidas e as telas atuais no código. Entrega uma lista priorizada de problemas de hierarquia, redundância, contraste, fluxo, densidade, textos e inconsistências. Deve diferenciar bloqueadores de preferências estéticas. Não edita arquivos.

### Agente 3 — Auditor da arquitetura Compose/tema, somente leitura

Investiga a coexistência de `CardControlTheme`/`CCTheme`, tokens Vitrine, valores hardcoded, `LiquidGlass`, insets e barras do sistema. Entrega um mapa da fonte atual de cores e riscos técnicos. Não edita arquivos.

### Agente 4 — Implementação de layout/telas

Implementa o layout e a organização das telas de acordo com esta direção aprovada. Dono dos arquivos de apresentação que o coordenador designar. Não altera modelo de dados, persistência, parsing de notificações ou assinatura.

### Agente 5 — Implementação de tokens/edge-to-edge

Trabalha nos arquivos de tema, tokens e configuração de janela/insets designados. Não edita simultaneamente `MainScreen.kt` com o Agente 4; coordenar contratos antes das edições.

### Agente 6 — Validação visual em dispositivo, preferencialmente somente leitura

Inspeciona dispositivo conectado, resolução/densidade, navegação real, capturas e defeitos visuais. Emite relatórios com evidência por tela/tema. Não modifica código enquanto a implementação estiver ativa; depois pode revalidar correções.

### Agente 7 — Revisor independente, somente leitura

Depois de integrar, revisa diff, arquitetura, honestidade funcional, segurança, acessibilidade, regressões e a evidência visual. Não deve revisar apenas documentação escrita pelo implementador; verificar o código e os resultados reais.

Se a plataforma não permitir 7 agentes, manter ao menos: (1) coordenador, (2) auditor UX/read-only, (3) implementação com dono de arquivos definido, (4) revisor/testador independente. Nunca pedir a dois agentes para editar em paralelo o mesmo arquivo ou a mesma decisão de tema.

### Contratos para subagentes

Cada subagente deve devolver:
- arquivos inspecionados;
- achados com gravidade e evidência;
- decisões ainda pendentes;
- arquivos que pretende alterar (se autorizado);
- critérios para declarar seu trabalho concluído.

Não aceite afirmações vagas como “ficou mais moderno”. Use mudanças rastreáveis e capturas reais.

---

## 7. Plano de execução em fases com gates obrigatórios

### F0 — Descoberta e baseline

- Registrar branch, HEAD, estado de trabalho e comandos de validação oficiais.
- Rodar a suite de testes e build de baseline, se viável, antes das mudanças.
- Confirmar dispositivo conectado via ADB (`adb devices -l`). Registrar modelo, versão Android, resolução e density quando disponível.
- Instalar/abrir a versão atual apenas se isso não sobrescrever trabalho não salvo nem afetar dados. Capturar baseline real das três abas e do estado de cartão vazio/cheio.
- Inspecionar e registrar quais telas/funcionalidades existem hoje.

**Gate F0:** plano com arquivos/risco, linha de base e dispositivo identificado; nenhuma mudança antes de proteger o estado local.

### F1 — Consolidar fundação visual

- Definir uma fonte canônica de tokens e um único tema raiz.
- Mapear e remover referências de cores legadas/estilo Liquid Glass que ainda controlem as telas, sem apagar código necessário antes de migração validada.
- Estabelecer tokens de superfície, foreground, contorno, semânticos, espaçamento, raios, tipografia, elevação, interação e movimento.
- Resolver a configuração edge-to-edge e o tratamento de insets.
- Atualizar estado/documentação de arquitetura conforme requerido.

**Gate F1:** app compila e muda entre tema claro/escuro sem flashes, superfícies conflitantes nem faixa indevida; testes de resolução de tema passam.

### F2 — Cabeçalho, palco e navegação compartilhados

- Remover a cápsula grande de “CardControl” usada como cabeçalho genérico.
- Implementar top bar compacta, título contextual e ações corretas por aba.
- Unificar plano de fundo e navegação inferior.
- Corrigir insets superior/inferior, gesto de retorno e edge-to-edge.

**Gate F2:** capturas das três abas mostram a mesma linguagem, altura superior consistente, e zero sobreposição com as barras do sistema.

### F3 — Cartões

- Refinar carrossel, card face/back, seleção estável, ações contextuais e estado vazio.
- Corrigir o retângulo colorido atrás do cartão e eliminar repetição de banco/final onde não agrega.
- Consolidar as ações primárias e remover comandos duplicados de adicionar.
- Preservar editar, excluir e integridade do histórico.

**Gate F3:** zero dado falso, zero ação duplicada, seleção correta e captura em tema claro/escuro com cartão real e com o estado vazio quando puder ser validado sem apagar dados do usuário.

### F4 — Faturas

- Reorganizar resumo mensal e lista para que o valor e o conteúdo real tenham precedência.
- Tornar período/meses compacto.
- Criar estados vazios verdadeiramente contextuais.
- Corrigir o contraste e o rótulo dos botões.
- Não simular lançamento, limite, total ou gráfico.

**Gate F4:** tela legível sem redundância e com os valores existentes preservados.

### F5 — Automação e Aparência

- Remover menção visual ao nome técnico “Vitrine” e à explicação do wallpaper da interface de usuário.
- Organizar captura de notificações, e-mail e aparência em grupos/listas compactos.
- Revelar os campos de SMTP quando necessários, não dominar toda a tela por padrão.
- Permissão, envio e estado de configuração devem refletir a realidade.

**Gate F5:** configuração mais curta e compreensível; permissões e credenciais continuam corretas e seguras.

### F6 — Ícones expressivos e acabamento

- Aplicar a linguagem visual unificada de ícones com dimensão seletiva.
- Criar/otimizar assets necessários e licenciáveis, sem misturar estilos.
- Refinar animações do carrossel, flip, seleção, diálogos e feedback.
- Implementar/rever redução de movimento e TalkBack.

**Gate F6:** efeitos ajudam a hierarquia; nenhum ícone, sombra ou animação rouba atenção do dado principal.

### F7 — QA real em telefone com loop visual

Este gate é central. Não o substitua por previews ou testes unitários.

1. Verifique novamente o dispositivo via `adb devices -l` e confirme que o serial está `device`, não `offline`/`unauthorized`.
2. Obtenha aplicação ID e comando de build a partir dos arquivos reais do projeto.
3. Gere APK de debug com os comandos oficiais e instale com `adb install -r`, se seguro.
4. Abra o app sem limpar armazenamento nem remover os dados atuais.
5. Navegue e capture: Faturas, Cartões, Automação, aparência/configurações, cartão selecionado/frente, verso/flip e os principais diálogos sem salvar dados artificiais.
6. Para cada estado, produza uma screenshot em arquivo e inspecione a própria imagem visualmente. Não conclua pelo fato de o comando ter sucesso.
7. Compare com a referência de UX e esta especificação, identifique os 3–5 defeitos visuais mais graves, corrija-os e repita.
8. Fazer **pelo menos três ciclos reais** de captura → inspeção → correção, a menos que todos os critérios estejam atingidos antes e isso seja demonstrado com evidências adequadas. Não inventar ciclos ou resultados.
9. Fazer a validação no tema claro e escuro. Quando uma preferência for alterada para teste, restaurar a preferência original ao final ou registrar explicitamente a situação.
10. Não criar, editar, pagar ou excluir registros reais só para testar. Não limpar o banco nem restaurar dados de fábrica. Se um estado exigir dados ausentes e não puder ser produzido com segurança no dispositivo, cobri-lo com Preview/UI test determinístico e documentar a limitação.
11. Validar rotação, fonte ampliada (no mínimo 1,3× e o máximo suportado relevante), navegação inferior e scroll sem conteúdo cortado.
12. Se o telefone ficar indisponível, corrigir problemas de conexão possíveis sem usar ações destrutivas. Caso permaneça indisponível, executar a validação alternativa em emulador e registrar com clareza que o gate físico não passou; não alegar validação física concluída.

Comandos ADB são exemplos, não presuma package name ou activity:

```bash
adb devices -l
adb shell wm size
adb shell wm density
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
# Depois de identificar package/activity reais:
adb install -r <caminho-do-apk-debug>
adb shell am start -n <applicationId>/<ActivityLauncher>
adb shell screencap -p /sdcard/cardcontrol-screen.png
adb pull /sdcard/cardcontrol-screen.png <diretorio-de-evidencias>
```

Use equivalentes PowerShell/Windows quando aplicável. Capture screenshots num diretório de evidências não rastreado ou caminho temporário permitido pelo projeto; não adicione dumps enormes ao Git.

**Gate F7:** imagens inspecionadas; problemas corrigidos; três ciclos de validação real realizados ou limitações documentadas honestamente; tema claro/escuro verificados.

### F8 — Revisão independente e regressão

- Um revisor somente leitura revisa o diff completo e as capturas.
- Verificar tema duplicado, códigos de cor espalhados, comentários visíveis indevidos, botões sem destino, semântica de acessibilidade, erros de contraste e mudanças fora do escopo.
- Testar seleção de cartão, CRUD, integridade de registros, navegação, configuração de aparência e permissões.
- Testar o parser/serviço de notificação apenas para comprovar que a refatoração não o regrediu, sem alterar a lógica dele.
- Corrigir achados bloqueadores e repetir os testes afetados e a inspeção visual.

**Gate F8:** nenhum bloqueador aberto; bugs não bloqueadores documentados e de baixo risco.

### F9 — Entrega

- Rodar testes unitários, testes instrumentados/UI disponíveis, lint, compilação e verificações de segurança/governança apropriadas.
- Rodar `git diff --check` e auditar o diff em busca de segredos, keystores, assets não licenciados e alterações alheias.
- Atualizar documentação, estado canônico, tasks/decisions conforme regras do projeto, sem inventar evidências.
- Commit local somente seguindo a governança e convenções existentes. Não publicar release e não fazer push/merge sem a autorização necessária.

**Gate F9:** código, testes, capturas, estado do Git e relatório final coerentes entre si.

---

## 8. Critérios mensuráveis de aceitação

### Layout e informação

- [ ] A cápsula gigante “CardControl” não ocupa mais o espaço de título comum em todas as telas.
- [ ] Cada aba tem título inequívoco: “Faturas”, “Cartões” e “Automação”.
- [ ] Não há texto visível que explique arquitetura/implementação em vez de ajudar o usuário (ex.: “Design System Vitrine com paleta sob medida”).
- [ ] Não há cartões/painéis aninhados desnecessários nem grandes fundos retangulares abruptos atrás do cartão.
- [ ] Não há repetição de banco/final ou resumo financeiro sem benefício claro.
- [ ] A tela Cartões tem uma hierarquia evidente: cartão → ações principais → ações secundárias.
- [ ] Existe somente uma ação canônica para adicionar cartão por contexto.
- [ ] A tela Faturas diferencia estados vazios com base nos dados reais e não mostra zeros redundantes.
- [ ] A tela Automação usa grupos compactos, rótulos naturais e estados reais das permissões.
- [ ] Nenhum conteúdo é encoberto por barra de status, navegação do sistema, bottom navigation ou FAB.

### Consistência visual

- [ ] Uma única fonte de verdade de cores/tokens e um único tema efetivo.
- [ ] O tema claro utiliza uma base leve e calorosa, com petróleo e cobre controlados.
- [ ] O tema escuro mantém Grafite e o acento ciano/turquesa sem excesso de brilho.
- [ ] Wallpaper/Dynamic Color não altera a paleta Vitrine.
- [ ] Cores do cartão não contaminam o fundo com blocos recortados.
- [ ] Ícones simples e dimensionais seguem uma mesma linguagem.
- [ ] Botões, campos, diálogos, navegação e textos compartilham a mesma escala de tokens.
- [ ] Contraste de texto/interativos validado; não usar outline decorativo fraco em campos importantes.

### Funcionalidade e acessibilidade

- [ ] Nenhum dado fictício em produção e nenhum dado sensível exposto.
- [ ] Seleção do carrossel permanece correta ao mudar/editar/excluir cartões.
- [ ] Flip é opcional, reversível, compatível com leitores de tela e movimento reduzido.
- [ ] Permissões e configuração de e-mail refletem o estado real; segredo não é exibido em logs.
- [ ] TalkBack, alvos de toque, teclado/foco e fonte ampliada verificados.
- [ ] Integridade do histórico financeiro preservada.

### Validação real

- [ ] APK instalado e aberto no telefone conectado, se o dispositivo estiver disponível.
- [ ] Capturas reais de Faturas, Cartões e Automação em tema claro e escuro.
- [ ] Captura do carrossel com cartão real disponível e flip, sem dados inventados.
- [ ] Capturas inspecionadas após as mudanças, não apenas geradas.
- [ ] Três ciclos reais de captura → inspeção → correção ou explicação documentada do porquê algum ciclo foi desnecessário.
- [ ] Testes, build, lint e revisões listados com resultados reais.
- [ ] Nenhuma afirmação de validação física se ocorreu somente Preview/emulador.

---

## 9. Como decidir quando houver dúvida

Use esta ordem:

1. Segurança, integridade de dados e governança do repositório.
2. Funcionalidade real e fonte de dados existente.
3. Acessibilidade, legibilidade e previsibilidade.
4. Direção visual e hierarquia descritas neste prompt.
5. Animações, 3D e ornamentos.

Se uma ideia 3D deixar um ícone ilegível, reduza o efeito. Se uma tela limpa exigir esconder um dado contábil importante, preserve o dado e reorganize o layout. Se uma ação parece atraente mas não tem destino funcional real, não mostre a ação. Se duas implementações de tema competirem, consolide a fonte canônica ao invés de adicionar outra camada.

Não refatore o domínio, Room, parser, credenciais, notificações ou release mais do que for necessário para a interface. Não atualize toda a pilha Compose para obter um efeito decorativo. Não inclua bibliotecas novas sem justificar necessidade, compatibilidade, tamanho e manutenção.

---

## 10. Relatório final obrigatório

Ao concluir, entregue um `walkthrough.md` com evidências reais e um resumo na conversa contendo:

1. **Estado inicial:** branch, HEAD, estado de Git e dispositivo detectado.
2. **Direção aplicada:** breve explicação da hierarquia visual escolhida.
3. **Mudanças por tela:** Faturas, Cartões, Automação, formulários/modais, top/bottom bars e tema.
4. **Arquitetura:** como as fontes duplicadas de tema foram consolidadas e quais tokens passaram a ser a fonte única de verdade.
5. **Ícones/ilustrações:** assets usados/criados, licença/origem e por que os elementos dimensionais foram aplicados nesses pontos.
6. **Capturas antes/depois:** caminhos das evidências e descrição concreta do que foi corrigido em cada ciclo.
7. **Dispositivo:** modelo, API Android, densidade/resolução e se o gate físico foi realmente realizado.
8. **Testes:** comandos exatos, resultados, build/APK gerado, lint e verificações. Diferenciar passou, falhou, não executado e bloqueado.
9. **Revisão independente:** achados e resolução.
10. **Git:** arquivos alterados, commits locais, status final, estado remoto e o que não foi enviado.
11. **Riscos remanescentes:** limitações honestas, sem disfarçar como concluído algo que não foi verificado.

**Objetivo final:** o CardControl deve ficar visualmente muito mais próximo da clareza e sofisticação do Google Wallet, mas manter identidade própria, funções reais, código sustentável e um acabamento comprovado no telefone. Não encerre porque os testes passaram: encerre quando o produto estiver funcional, coerente e visualmente validado.
