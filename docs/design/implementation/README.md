# Design and implementation

One folder per change, called a series, numbered in the order they were
started: `000-expression-tree`, `001-…`. Numbers are never reused or
changed. A series that is dropped keeps its number and says so.

Every series has a `README.md`: what the change is, why, what was
considered, where it stands, and the commit it landed at. Most need
nothing more. When a change is too big to design and build in one
conversation, its series adds what the handoff needs: a `charter.md`
for the design work, a `spec.md` it produces, and `checkpoints/`
handed one at a time to an implementer. Use only as much of that as the
change needs.

A series is named by its name and a checkpoint by its number within the
series: **expression-tree**, **collect-terms 001**.

| Series | What | Status |
|---|---|---|
| [`000-expression-tree`](000-expression-tree/README.md) | `Expr`, building it with operators, printing, structural equality, and the project setup. | Done at `7cc033f` (2026-10-22). |
| [`001-self-hosted`](001-self-hosted/README.md) | The move to Niva's self-hosted compiler: the port, checks in place of `niva test`, and scripts to build and run the compiler. | Done at `5cbdef7` (2026-10-22). |
| [`002-let-go`](002-let-go/README.md) | An experiment: Niva's Clojure output on let-go instead of Babashka. MPL runs there as laid out, but for one function missing from Niva's runtime. | Done at `4087f92` (2026-10-23). |
| [`003-equals`](003-equals/README.md) | MPL compares expressions with `==` in place of its own `same:`, on a self-hosted compiler pinned past #440. | Done at `1fbabc9` (2026-10-24). |
| [`004-notes-recheck`](004-notes-recheck/README.md) | The pin moves past #450, and every Niva note and open feedback item is re-checked there. | Done at `PENDING` (2026-10-25). |
