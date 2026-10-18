# Leve como referência visual do CardControl

## Objetivo e fontes verificadas

O pedido mais recente é usar o projeto local Leve como referência real para elevar a qualidade visual do CardControl inteiro — tipografia, cores, botões, superfícies, espaçamento, formulários, navegação, ícones, estados e comportamento responsivo.

Repositório de referência somente leitura: `C:\Users\kelvi\Desktop\Projetos\Leve`.
Fontes efetivamente inspecionadas:
- `DESIGN.md`
- `design-tokens.json` (referência auxiliar, não fonte autoritativa isolada)
- `apps/web/src/styles/editorial.css`
- `apps/web/src/styles/glass.css`
- `apps/web/src/styles/app.css`
- `apps/web/src/platform/theme.ts`
- `packages/domain/src/themes.ts`

Não abrir nem copiar `.env.local`, credenciais, tokens, dados privados ou configuração de deploy do Leve. Não modificar o repositório Leve.

## Direção criativa recomendada

Construir uma identidade coesa “Leve editorial adaptado a fintech”: conteúdo claro e calmo, superfícies de papel, navegação em tom de tinta/verde profundo, hierarquia tipográfica expressiva, contornos finos e sombras discretas. Usar vidro com moderação apenas onde acrescenta hierarquia (por exemplo, navegação flutuante), com alternativa opaca. O resultado deve parecer nativo em Jetpack Compose + Material 3 Expressive, não uma página web transplantada.

Princípios observados no Leve:
- Nunito para títulos/marca; DM Sans para interface e texto, com hierarquia por peso, escala e espaçamento.
- Conteúdo em superfícies legíveis com bordas visíveis e sombras contidas; navegação visualmente distinta do conteúdo.
- Botão primário claro e tátil, com resposta de pressão; secundários planos e sem competição com a ação principal.
- Campos com fundo definido, foco inequívoco e rótulos compreensíveis; painéis, campos e notas usam raios diferentes.
- Escala de espaçamento consistente baseada em 4px/rem no web; adaptar para uma escala Compose coerente em dp.
- Glass só em estrutura/camadas flutuantes; modo sólido, fallback sem blur, transparência reduzida e movimento reduzido respeitados.
- Layout móvel reordena e simplifica sem ocultar ações importantes; estados vazios, erro e carregamento são deliberados.
- Cores semânticas e estados nunca comunicam significado apenas por cor.

## Cores e tipografia: validação antes de portar

Há uma divergência entre `design-tokens.json` (ex.: canvas `#EDF0ED`, ação `#4D685C`) e os tokens ativos vistos em `app.css` / `packages/domain/src/themes.ts` (tema verde claro com canvas `#E8F0EB`, texto `#182A20` e valores de ação diferentes). `editorial.css` também deriva superfícies de outros tokens. Portanto:
1. Trate o runtime real (`packages/domain/src/themes.ts`, CSS ativo e overrides de aparência) como fonte visual principal.
2. Confirme quais fontes Nunito/DM Sans estão instaladas e licenciadas antes de adicioná-las ao APK. Se o custo/tamanho ou a licença não forem adequados, escolha a melhor alternativa existente e registre a decisão.
3. Traduza as cores para papéis semânticos Compose; não copie valores CSS/transparências sem testar contraste em claro/escuro.
4. Preserve o override de cores dinâmicas do Android. O acento do banco pode destacar o cartão selecionado e o palco do carrossel, mas não deve tingir indiscriminadamente toda a aplicação nem confundir estados financeiros.

## Mapeamento para Compose

- Tema: uma fonte de verdade de cores semânticas, tipografia, formas, espaçamento, elevação e movimento. Mapear tokens para `MaterialTheme`; não espalhar hex/fontSize/dp aleatórios.
- TopBar/BottomBar: hierarquia de navegação inspirada no “ink navigation”, adaptada ao edge-to-edge e aos insets Android; rótulos e toque confortáveis.
- Botões: primário por contexto, secundários discretos, destrutivo explícito; alvos de toque de pelo menos 48dp. Texto não deve cortar em 360dp ou com fonte ampliada.
- Campos/dialogs: superfícies definidas, labels e erros humanos, foco visível, teclado correto e ações sem truncamento.
- Cards e métricas: bordas e sombras sóbrias, valor financeiro como principal hierarquia, dígitos tabulares e explicações secundárias legíveis.
- Análises: gráficos discretos e precisos; cores de categorias distintas da paleta de navegação; pagamento separado de gasto bruto.
- Vitrine de cartões: preservar carrossel HorizontalPager, peek do próximo cartão, proporção 1.586:1, flip 3D e paleta específica do cartão/banco. O cartão pode ser mais expressivo que o restante da interface.
- Glass/blur: não aplicar a todos os cartões/containers; limitar a elementos flutuantes e oferecer superfície opaca equivalente, evitando custo gráfico permanente.
- Claro/escuro: ambos coerentes com a identidade Leve; não confiar em cores do papel de parede Android.

## Contrato de orquestração e handoff

1. **Design System & Layout Review** começa imediatamente em modo somente leitura: audita fontes Leve acima, confere componentes do CardControl e entrega tabela `princípio Leve → componente Compose/arquivo → mudança proposta → risco → teste/evidência`. Priorize P0/P1/P2; não aguarde aprovação do usuário para recomendar.
2. **Gemini - Continuidade (escritor atual)** conclui a alteração já iniciada em `AnalyticsScreen.kt`, executa apenas as verificações adequadas ao escopo e registra handoff. Não misturar redesign global novo no meio dessa mudança.
3. **Maestro** escolhe os padrões, resolve conflitos, define uma ordem por etapas e libera escopos exclusivos após o handoff. Não declare “pronto para homologação” antes de incorporar o novo pedido e de verificar a interface renderizada.
4. **Data Integrity / Room** pode investigar em paralelo, em read-only. O achado sobre exclusão em cascata e o cold-start deve ser tratado como lote funcional separado do redesign; depois que houver handoff de código, o Maestro decide/autoriza correções seguras no código, com testes, sem tocar no banco real.
5. **APK & Release Validator** audita apenas o estado final consolidado; hashes e gates antigos não comprovam mudanças locais atuais.
6. Um só escritor por arquivo; Gradle sequencial; sem novos terminais desnecessários; preservar todos os diffs existentes. Não fazer commit/push/merge/publicação.

## Critérios de conclusão visual

- Auditoria real de todas as telas principais e mapeamento para componentes reutilizáveis.
- Fonte/tokens escolhidos com evidência; sem mistura de duas identidades ou temas concorrentes.
- Verificação em 360dp e aparelho real/emulador disponível, font scale ampliada, claro/escuro, estados vazio/erro/loading, insets, contraste e TalkBack.
- Capturas antes/depois pertencem ao APK efetivamente testado; inspecionar recortes, alinhamento, densidade, navegação e responsividade.
- Testes, lint e build rodam sequencialmente depois da última alteração; registrar comandos, resultados, APK/hash e limitações.
- Nenhum fluxo de negócio, lógica financeira ou Room é alterado “por estética”; mudanças funcionais vão em lote separado com testes de causa-raiz.

