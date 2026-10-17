# Project Calculus (projcalc)

Mod para **Minecraft 1.21.1 (NeoForge)** que integra o **Super Cool Calculator (SCC)** com o **ProjectE**, permitindo que o SCC solicite e fabrique itens transmutáveis usando o EMC do jogador.

Inspirado no AppliedE, mas **sem adicionar o AppliedE como dependência**.

## Autor

- **LabraHell**

## Requisitos

- Minecraft **1.21.1**
- NeoForge **21.1.238+**
- **SCC (Super Cool Calculator)** **1.4.4+** (testado com **1.4.6**)
- **ProjectE** **1.21.1-PE1.1.0+**

### Opcionais

- **JEI** 19.21+ (recomendado: melhora a descoberta de receitas)
- **Jade** 15.0.0+ (tooltip customizado no módulo)

## Instalação

1. Instale o NeoForge na instância.
2. Coloque na pasta `mods/`:
   - `projcalc-*.jar`
   - `scc-*.jar`
   - `ProjectE-*.jar`
3. (Opcional) Coloque `jei-*.jar` e `Jade-*.jar`.
4. Inicie o jogo.

## Como usar

1. Aprenda itens no ProjectE (transmutação).
2. Coloque o bloco **EMC Transmutation Module** no mundo.
3. O módulo captura o **owner** (jogador que colocou).
4. Conecte o módulo à rede do SCC como um storage normal.
5. O SCC verá os itens transmutáveis com quantidade baseada no EMC disponível.

## Funcionalidades

- Bridge SCC ↔ ProjectE via `BulkItemStore`.
- Extração/inserção de itens transmutáveis.
- GUI de 1 slot (144x100) + painel de inventário separado (176x84).
- Formatação de EMC (K/M/B/T) com tooltip de valor absoluto.
- Multi-módulo: apenas o módulo primário expõe estoque (evita duplicação).
- Team sharing via times do Minecraft (scoreboard).
- Integração com Jade (EMC + owner por nome).
- Logs dedicados em `logs/projcalc/`.

## Logs

- `logs/projcalc/` — logs da sessão do mod.
- `logs/scc.log` — logs do SCC (útil para diagnóstico de rede).

## Dependências

### Obrigatórias
- NeoForge
- Minecraft 1.21.1
- SCC
- ProjectE

### Opcionais
- JEI
- Jade


## Desenvolvimento (build)

### Requisitos de build
- Java **21**
- Gradle (wrapper incluído)
- Jars de runtime para compilação (SCC, ProjectE, JEI, Jade) na instância ou em caminhos equivalentes.

### Compilar
```bash
./gradlew build
```

O jar gerado fica em:
```
build/libs/projcalc-0.0.1-dev.jar
```

### Observações
- O `build.gradle.kts` usa jars locais da instância SCE para `compileOnly`.
- Ajuste os caminhos se sua instância estiver em outro diretório.


## Licença

LGPL-3.0-or-later
