---
name: regression-review
description: Revisao adversarial obrigatoria para detectar regressao funcional, dados duplicados, divergencia entre UI/domínio/persistencia, testes artificiais e gates de CI/CD incompletos antes de concluir ou recomendar merge.
---

# Regression Review

Use esta skill em toda feature com impacto em regras de negocio, persistencia, dados financeiros, migracoes Room, fluxos entre UI e dominio, automacao, release ou CI/CD. O implementador nao pode ser o unico revisor.

## Procedimento obrigatorio

1. **Estabeleca a verdade atual.** Confira branch, HEAD, working tree, diff completo, base do PR e workflows. Diferencie evidencias atuais de notas antigas em `.agents/CARDCONTROL_STATE.md`.
2. **Siga o dado de ponta a ponta.** Trate o fluxo UI -> ViewModel -> repository -> DAO/Room -> leitura/exibicao. Confirme que filtros, validacao e persistencia aceitam o mesmo conjunto de estados que a interface oferece.
3. **Teste casos negativos e de borda.** Para cada regra, inclua o comportamento esperado e pelo menos um caso que deve ser rejeitado. Nao teste somente o caminho feliz.
4. **Verifique identidade e normalizacao.** Para chaves logicas como titular, cartao, comercio e transacao, confirme que normalizacao, consulta, atualizacao e deduplicacao usam o mesmo criterio. Teste variantes de caixa, espacos, acentos e formatos quando aplicavel.
5. **Avalie sequencias completas.** Em deteccao por historico, teste a ordem real dos eventos e a cobranca mais recente; um par antigo que combina nao valida automaticamente uma sequencia posterior irregular. Nao pare a analise no primeiro exemplo que satisfaz uma condicao.
6. **Confronte a UI com o dominio.** Todo estado que a interface permite gravar deve ter semantica explicita no dominio. Sinalizadores manuais nao devem ser silenciosamente descartados por filtros secundarios; casos incompatíveis devem ser bloqueados ou explicados pela UI.
7. **Preserve a verdade financeira.** Valores em centavos devem continuar como `Long`. Percentuais, totais e alertas nao podem ser silenciosamente truncados ou limitados se isso esconder um estouro real. Adicione testes acima de limites convencionais, limites zerados e entradas invalidas.
8. **Valide migracoes com SQLite real.** Nao aceite apenas mocks, proxies que gravam strings SQL, asserts de chamadas ou inspecao de texto como evidencia de migracao. Execute o SQL sobre uma base SQLite de teste, confira colunas, tabelas, indices, foreign keys e preservacao dos dados relevantes. Sempre que a infraestrutura permitir, valide tambem a abertura do banco pela versao Room de destino.
9. **Exija testes de regressao ligados ao defeito.** Um teste que apenas confirma que um metodo foi chamado nao prova integridade persistida. Para bugs de repository/DAO, cubra a leitura e gravacao e verifique que reexecucoes nao geram duplicatas nem perdem estado.
10. **Revise o mecanismo, nao so o resultado.** Procure concorrencia, duplicidade em reexecucoes, falhas parciais, cache stale, comportamento offline, dados existentes e compatibilidade retroativa. Justifique cada risco nao coberto.
11. **Audite CI/CD.** Confirme que os checks essenciais sao executados em pull requests, falhas bloqueiam o job e os artefatos de release sao gerados apenas do SHA pretendido. Testes skipped, ausentes ou com escopo diferente nao contam como aprovados.
12. **Revalide apos corrigir.** O auditor independente deve revisar o novo diff e executar/inspecionar novamente os testes afetados. Relacione o SHA efetivamente testado ao parecer.

## Criterio de bloqueio

Nao recomende merge se houver falha reproducivel, migracao testada apenas por mock, regressao importante sem teste automatizado, divergencia entre UI e dominio, metricas financeiras enganosas ou CI que nao cubra o risco identificado. Registre cada achado com severidade, arquivo/trecho, consequencia, reproducao e correcao na causa raiz.

A conclusao deve separar: **validado**, **nao validado**, **bloqueado** e **fora do escopo**. Nao declare teste, artifact, release ou deploy como concluido sem evidencia observavel.
