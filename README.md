# BIKESHARE

Sistema de compartilhamento de bicicletas desenvolvido como projeto da disciplina de
**Programação 02** (Instituto de Computação – UFAL).

O projeto implementa **apenas a lógica de negócio**: não há interface gráfica nem banco
de dados. Todo o sistema é acessado por uma única classe `Facade` e validado por testes
de aceitação automatizados executados com o **EasyAccept**.

## Como executar

```bash
./run-tests.sh            # compila e roda todos os testes (us1 a us16)
./run-tests.sh us9 us9_1  # compila e roda apenas os scripts informados
```

Ou, manualmente:

```bash
javac -encoding UTF-8 -cp lib/easyaccept.jar -d out $(find src -name '*.java')
java -cp out:lib/easyaccept.jar Main
```

Requisitos: JDK 17 ou superior.

## Estrutura

```
src/Main.java                      executor dos scripts de teste
src/br/ufal/ic/p2/bikeshare/
    Facade.java                    ponto único de entrada (BS01)
    config/                        parâmetros de negócio configuráveis (BS04)
    exceptions/                    hierarquia de erros e catálogo de mensagens (BS05)
    model/                         entidades de domínio
        usuario/                   Usuario, PessoaFisica, PessoaJuridica
        pagamento/                 MetodoPagamento, Cartao, Pix, SaldoPreCarregado
        plano/                     Plano (avulso, assinante e customizados)
        penalidade/                Penalidade e seus tipos
    service/                       regras de negócio por User Story
    persistence/                   gravação e leitura do estado (BS03)
    util/                          texto, números e datas
config/bikeshare.properties        tarifas, franquias, carência e limites
tests/us1.txt ... us16_1.txt       testes de aceitação (EasyAccept)
lib/easyaccept.jar                 biblioteca de testes
```

## Decisões de projeto

- **Facade fina.** A `Facade` não contém regra de negócio; ela apenas delega aos
  serviços e cuida do ciclo de vida do estado.
- **Herança no lugar de `if` por tipo.** Usuário, bicicleta, método de pagamento e
  penalidade são hierarquias: cada subtipo responde pelos seus próprios atributos e
  pelas suas próprias regras, em vez de um `switch` central sobre um campo `tipo`.
- **Parâmetros fora do código.** Tarifa por minuto, franquia, carência, tempo máximo de
  aluguel, nível mínimo de bateria e valor da penalidade de atraso ficam em
  `config/bikeshare.properties` (requisito BS04).
- **Mensagens centralizadas.** Todas as mensagens de erro vivem em
  `exceptions/Mensagens.java`, garantindo que comandos distintos respondam da mesma
  forma à mesma situação (requisito BS05).
- **Valores monetários em `BigDecimal`.** Tarifas e penalidades nunca passam por
  `double`, evitando erro de arredondamento acumulado.
- **Undo/redo por Memento.** O histórico guarda retratos do estado inteiro, e não uma
  operação inversa por comando. É isso que permite desfazer até mesmo `zerarSistema` e
  manter coerentes os contadores de identificadores depois de um desfazer. Comandos
  recusados não entram no histórico, porque nada chegou a mudar.

## Estado dos testes

Todos os 30 scripts de aceitação passam — 1385 asserções:

```
us1 71 | us1_1 14 | us2 46 | us3 129 | us3_1 17 | us4 37 | us4_1 15
us5 42 | us5_1 11 | us6 74 | us6_1 14 | us7 39
us8 106 | us8_1 17 | us9 180 | us9_1 22 | us10 60 | us10_1 7
us11 46 | us11_1 7 | us12 71 | us12_1 13 | us13 71 | us13_1 11
us14 71 | us14_1 9 | us15 56 | us15_1 7 | us16 112 | us16_1 10
```

## User Stories

| US | Descrição |
|----|-----------|
| 01 | Cadastro de usuário pessoa física / pessoa jurídica |
| 02 | Consulta de usuário |
| 03 | Alteração de usuário, incluindo método de pagamento |
| 04 | Remoção de usuário |
| 05 | Cadastro de estação |
| 06 | Gestão de bicicletas convencionais e elétricas |
| 07 | Consulta de disponibilidade (bicicletas e vagas) |
| 08 | Retirada de bicicleta |
| 09 | Devolução de bicicleta e cálculo da tarifa |
| 10 | Consulta do histórico de aluguéis |
| 11 | Planos de tarifação avulso e assinante |
| 12 | Criação de novos planos de tarifação |
| 13 | Regras de bloqueio e lançamento de penalidades |
| 14 | Lançamento e conclusão de manutenção |
| 15 | Relatórios de disponibilidade e faturamento |
| 16 | Desfazer e refazer operações |
