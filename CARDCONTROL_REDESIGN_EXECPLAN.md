# CardControl — Plano de Execução do Redesign Visual & UX (Vitrine Google Wallet Style)

## 1. Diretrizes Fundamentais
- **Inspiração:** Simplicidade, clareza, superfícies contínuas e elegância tátil do Google Wallet, preservando a identidade própria do CardControl.
- **Princípio Central:** O cartão é o protagonista. Menos contêineres aninhados, menos texto explicativo de engenharia, zero dados falsos e ações reais.
- **Fonte Canônica de Cores:** Design System Vitrine (Claro: Petróleo & Cobre, Escuro: Grafite) como fonte única da verdade, 100% imune a Dynamic Color.

---

## 2. Mapa de Fases e Entregáveis

| Fase | Foco | Entregáveis |
|---|---|---|
| **F0** | Descoberta e Baseline | Registro de estado Git, verificação de build e testes (126 PASS) |
| **F1** | Fundação Visual | Substituir `LiquidAmbientBackground` por superfície contínua Vitrine (`Surface`), remover resquícios de Liquid Glass |
| **F2** | TopBar & Navegação | Top App Bar compacta com títulos contextuais ("Faturas", "Cartões", "Automação"), seletor de mês compacto em Faturas, ação de adicionar na TopBar de Cartões |
| **F3** | Redesign de Cartões | Eliminar repetição de banco/final abaixo do cartão, suavizar tingimento do palco sem cortes, duas ações primárias equilibradas ("Ver fatura" e "Lançar compra"), menu ⋮ para editar/excluir, eliminar duplicidade de botões de adicionar |
| **F4** | Redesign de Faturas | Resumo mensal compacto e moderno, botão "Lançar compra" com alto contraste, lista de parcelas limpa e contextual |
| **F5** | Redesign de Automação | Grupos compactos ("Captura de notificações", "Aparência", "E-mail"), remoção de jargões técnicos ("paleta sob medida", "imune a papel de parede"), formulário SMTP recolhível |
| **F6** | Ícones e Acabamento | Ícones consistentes, sombras sutis, animações de mola e acessibilidade |
| **F7** | Validação no Dispositivo | Instalação do APK, inspeção das telas, capturas reais nos temas claro e escuro |
| **F8** | Revisão Independente | Auditoria de acessibilidade, contraste, integridade contábil e diff |
| **F9** | Entrega & Governança | Gates executados, sincronização de `.agents/`, relatório final em `walkthrough.md` |
