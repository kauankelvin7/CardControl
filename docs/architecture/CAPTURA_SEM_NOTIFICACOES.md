# CardControl: controle de faturas independente de notificações

## Problema observado (22/10/2026)
A captura atual depende de o Android entregar um `StatusBarNotification` autorizado, de o pacote bancário pertencer à lista fixa, de o parser aceitar o texto e de a associação com cartão/titular ser inequívoca. O app não expõe ao usuário por que cada evento foi descartado. Nenhum teste unitário reproduz as condições reais de entrega de notificações pelos bancos.

**Nova premissa:** a fonte de verdade são as despesas/faturas conferidas pelo usuário, não os alertas dos bancos. Notificações tornam-se um canal experimental, nunca uma dependência do produto.

## Fontes, por ordem de prioridade
1. **Texto compartilhado ou colado (MVP nesta branch):** receber `ACTION_SEND` (`text/plain`) ou colar texto no formulário. Extração totalmente offline de valor, estabelecimento, parcelas e final do cartão; preenchimento apenas sugerido. O usuário confere e confirma antes de persistir em Room.
2. **Extratos/faturas CSV (próxima etapa):** seleção explícita de arquivo via seletor Android, formatos mapeados por banco, prévia de linhas, revisão de débito/crédito, mapeamento de cartões, competência, conciliação de duplicidade e importação transacional.
3. **PDFs de fatura/comprovantes (etapa posterior):** extração local de texto, OCR local apenas quando necessário; preservar documento e evidência quando autorizado; jamais fabricar valores com baixa confiança.
4. **Notificações (opcional):** manter compatibilidade, adicionar diagnóstico local e fila de revisão para casos ambíguos; não considerar a permissão como prova de captura funcional.

## Fluxo alvo
```text
Compartilhar / Colar / Selecionar arquivo
          |
    Normalizar localmente
          |
    Gerar rascunhos com origem e confiança
          |
    Conferência + cartão + titular + data
          |
    Conciliação e aviso de possíveis duplicatas
          |
    Confirmar explicitamente
          |
  CardRepository -> Room -> Faturas / Rateios / Análises
```

## O que está implementado
- Compartilhamento externo de **texto puro** recebido pela Activity; sem acesso às notificações.
- Parser local limitado a 4.000 caracteres, com bloqueio de estorno, Pix recebido e códigos de autenticação.
- Pré-preenchimento de compra, seleção de cartão pelo final quando unívoco, edição e confirmação humana.
- Colagem do texto dentro de **Lançar compra**, mantendo o cadastro manual se não houver correspondência.
- Ajuste da interface: permissão de notificações aparece como **experimental/opcional**.
- Sem mudança no schema Room, no modelo de centavos, nas permissões financeiras ou no envio de dados.

## Limites explícitos do MVP
- Compartilhamento suportado: `text/plain`; **imagem e PDF ainda não são importados**.
- A data usada pelo registro compartilhado ainda é a data corrente, não a data histórica extraída de um comprovante.
- O usuário deve revisar cartão, titular e valor. O parser não prova que o texto vem do banco.
- As regras de deduplicação de importação em lote e o histórico de erros de captura pertencem às próximas etapas.
- O funcionamento em dispositivos físicos, a entrega real do Android Sharesheet e o CI ainda exigem verificação após as mudanças.

## Critérios para avançar às demais etapas
- Parse seguro sem enviar dados a servidores.
- Valor monetário em `Long` (centavos), com integridade das parcelas e do mês de fechamento.
- Nenhum registro automático oriundo de texto externo sem aceite explícito.
- Importação sem sobrescrita/destruição do histórico preexistente.
- Reimportação detectada; conflitos ficam em revisão, nunca descartados silenciosamente.
- Tests: unidade, integração Room, lint, build Android, ensaio com aparelho real e exemplos de ao menos três formatos de CSV distintos.
