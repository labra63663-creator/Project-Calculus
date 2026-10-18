# Changelog — Project Calculus

## 1.0.0

### Adicionado
- Módulo de transmutação EMC (`projcalc:emc_transmutation_module`).
- Bridge SCC ↔ ProjectE via `BulkItemStore`.
- GUI de 1 slot + painel de inventário separado.
- Formatação de EMC (K/M/B/T) + tooltip de valor absoluto.
- Multi-módulo com deduplicação de estoque.
- Team sharing via times do Minecraft (scoreboard).
- Integração com Jade (EMC + owner por nome).
- Logs dedicados em `logs/projcalc/`.

### Alterado
- Atualizado para SCC 1.4.6.
- Adicionado cache curto para reduzir polling do SCC.
- Declarado `indexed() = false` (storage virtual).

### Autor
- LabraHell
