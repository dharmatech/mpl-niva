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
| [`004-notes-recheck`](004-notes-recheck/README.md) | The pin moves past #450, and every Niva note and open feedback item is re-checked there. | Done at `7cae0ee` (2026-10-25). |
| [`005-string-ordering-pin`](005-string-ordering-pin/README.md) | The pin moves to #452, so strings have `<`, `>`, `<=` and `>=`, and the notes say so. | Done at `dae19a8` (2026-10-25). |
| [`006-inject-into-pin`](006-inject-into-pin/README.md) | The pin moves to #454, so `inject:into:` runs, and the notes say so. | Done at `ff82209` (2026-10-25). |
| [`007-sorted-with-pin`](007-sorted-with-pin/README.md) | The pin moves to #455, so lists have `sortedWith:`, and the notes say so. | Done at `e5874de` (2026-10-25). |
| [`008-simplifier`](008-simplifier/README.md) | Cohen's automatic simplification, in small increments, as a charter, a spec and checkpoints. | The spec's seven increments done (2026-10-25), the last at `435dea7`. What comes next is open. |
| [`009-optional-imports`](009-optional-imports/README.md) | An idea for Niva: imports optional per file and checked when present, and a no-cycles flag, as an alternative to show Gavr. | Parked (2026-10-26) until NIN is the main implementation. |
| [`010-repl`](010-repl/README.md) | What Niva has for REPLs, and `tools/repl`, a REPL on the Go backend by replay. The Clojure REPL mode stays the candidate for a true REPL. | Done at `d8b6a9a` (2026-10-26). |
| [`011-comment-imports`](011-comment-imports/README.md) | Each file's imports as comments in the future syntax, and `tools/check-imports`, which checks them against NIN's Go: series 009's idea, tried without a compiler change. | Done, merged at `a9184c0` (2026-10-26). |
| [`012-clj-records-pin`](012-clj-records-pin/README.md) | The pin moves to `a4429c50`, with #461, #462 and Gavr's Clojure type hints; MPL on Clojure now stops only at list `+`, which #463 adds. | Done at `b282791` (2026-10-26). |
| [`013-clj-collection-ops-pin`](013-clj-collection-ops-pin/README.md) | The pin moves to #463, so MPL runs on Go, Babashka and JVM Clojure from stock `self-host`. | Done at `8433333` (2026-10-27). |
| [`014-check-repl`](014-check-repl/README.md) | `tools/check-repl`: MPL's Clojure REPL module, loaded on `bb`, JVM Clojure and let-go, prints the demo as Go does. It passes once the pin moves past #464. | Done at `1c60ce0` (2026-10-26). |
| [`015-clj-repl-types-pin`](015-clj-repl-types-pin/README.md) | The pin moves to #464, so MPL's REPL module loads on Babashka, JVM Clojure and let-go from stock `self-host`, and `tools/check-repl` is in the README. | Done at `c830f8c` (2026-10-26). |
| [`016-clj-repl-records-pin`](016-clj-repl-records-pin/README.md) | The pin moves to #466, so a `--repl-line` module loaded after MPL's full REPL module runs in the same session on Babashka, JVM Clojure and let-go. | Done at `105e213` (2026-10-28). |
| [`017-check-repl-line`](017-check-repl-line/README.md) | `tools/check-repl` also sends `main.niva`'s last line, built alone with `--repl-line`, into the session that loaded the module, and checks it prints as Go does: what item 38 broke. | Done at `68cc60a` (2026-10-28). |
| [`018-clj-repl-loop`](018-clj-repl-loop/README.md) | `tools/repl --backend=clj`: a REPL with a live session on `bb`, which loads MPL's REPL module, then each line built alone with `--repl-line`. It prints the demo's lines as Go does. | Done at `15eb107` (2026-10-28). |
| [`019-repl-one-jvm`](019-repl-one-jvm/README.md) | `tools/repl --backend=clj --runtime=jvm` runs NIN in the session's own JVM, so the compiler stays warm and a line takes about 0.14 s instead of 0.6 s. Series 020 moves it to `tools/repl-jvm`. | Done at `84ff3c2` (2026-10-28). |
| [`020-repl-jvm`](020-repl-jvm/README.md) | `tools/repl-jvm`: a Niva REPL that's one JVM, running NIN, the session and its own loop, in Clojure; the bash only finds the jar and becomes the JVM. `tools/repl` runs on Go, `bb` and `lg` again. | Done at `b684836` (2026-10-28). |
| [`021-repl-values`](021-repl-values/README.md) | `tools/repl-jvm` prints a line's value by itself: it builds the line as `resN = line` first, and prints `resN` as `echo` does, unless it's Unit or echo printed it. | Done at `HASH` (2026-10-28). |
