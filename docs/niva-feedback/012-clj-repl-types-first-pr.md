# 012 Clojure REPL mode: every package's types before the functions that use them, as a pull request

**Status:** Opened 2026-10-26 as [#464](https://github.com/gavr123456789/Niva/pull/464), not as a draft. A PR
without an issue, as [009](009-clj-record-names-pr.md) (#461) to
[011](011-clj-collection-ops-pr.md) (#463) were: a function can't name
a record before it's defined, so there's nothing for Gavr to decide.
No CI checks. Each paragraph went in on one line. `self-host` was
still at `7ac94536`.
**Branch:** [`dharmatech/Niva:clj-repl-types-first`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:clj-repl-types-first),
one commit on `self-host` at `7ac94536`: `2cda679a`. Locally in
`~/src/Niva-self-host-clj-repl-order`, a worktree of
`~/src/Niva-self-host`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

It's item 37 in the [README](README.md), found at `781c5b05` trying a
Clojure REPL for MPL
([series 010](../design/implementation/010-repl/README.md)). With #461
to #463 merged, it's the only thing stopping NIN's REPL mode from
loading MPL: at `7ac94536`, moving every `defrecord` to just after the
`ns` form by hand was all MPL needed. Item 38, a `--repl-line` module
defining its records again, comes after, as an issue with options.

**The fix:** in `fullFromIrModule:…classlessTypes:mainClass:`, the loop
over the packages is split in two. The first emits every package's
state cells, records, union branches and enum values, and the second
every package's functions. What else it touches:

- A program's files, and hot reload's, are each emitted from a module
  of one package, so their order doesn't change: MPL's 13 Clojure files
  as a program are the same byte for byte.
- With several packages, REPL mode's class hints already name the
  session's namespace, `^niva.repl.session.model_shapes_Circle`, which
  JVM Clojure resolves as it compiles. The class is now there first.
- An incremental build's whole-module `content` is reordered the same
  way. Only tests read it, comparing it with a clean build's.
- A `--repl-line` module still defines every type, now first: item 38
  is unchanged.

**Tests,** after the `cljRepl…` tests: a union, an enum and a record
from `model/shapes.niva`, each named by a function in `main`, all
defined before the first `defn`; and the REPL module of a two-file
program run on `bb`, with `CompilerConstants nivaCoreSource` written as
`niva/core.clj`, as for #463. `model.shapes` comes after `main`, as
MPL's `mpl.*` packages do.

## What was checked

On 2026-10-26, from exported copies, with the pin's stage 1 and a copy
of its home:

- `niva test`: 410 of 411 pass on the branch. The one that fails is
  `babashkaHostLoadsRecoversAndWatchesSources`, as for 005 to 011.
- The tests fail without the change: with only the test file added to
  stock, 408 pass and 3 fail, the 2 new ones and
  `babashkaHostLoadsRecoversAndWatchesSources`.
- Two programs of two files in REPL mode, built with
  `nin build main.niva --backend=clj --repl` and loaded in `.niva_clj`
  with `bb -cp . -e '(load-file "main.clj")'` and
  `clojure -M -i main.clj`: the PR's, and one with a record, a union
  and an enum in `model/shapes.niva`, each with a method in `main`, two
  of them class-hinted. Stock stops at `Unable to resolve symbol:
  model_shapes_Circle` on both runtimes. The branch prints what Go
  prints on both (and `bb -e` the last form's value after it).
- Program mode: MPL and the second program, built with `--backend=clj`
  by stock and by the branch, give the same files byte for byte.
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and the Go-built compiler gives the same module as the jar for
  the PR's program, which prints `3` on `bb` and JVM Clojure.
- **MPL with the branch,** `NIVA_SELF_HOST=~/src/Niva-self-host-clj-repl-order`,
  `tools/nin build main.niva --backend=clj --repl`:
  - The module is 564 lines, the same lines as stock's, reordered: its
    10 records, 5 from `mpl.tree` and 5 from `tests/`, come first.
  - Loaded in `bb`, the demo's 34 lines match
    `tools/nin run --backend=go`, followed by `:unit`, the module's last
    value, which `bb -e` prints. On JVM Clojure, `clojure -M -i
    main.clj` prints the 34 lines exactly. It loads and runs in 0.08 s
    in `bb`. Stock stops at `Unable to resolve symbol: mpl_tree_Num` on
    both.
  - `--repl-line=64`: stock and the branch give the same 532 lines,
    reordered the same way, with the entry from line 64 only (its five
    `showSimplified` lines) and the 10 records again. Item 38 is as it
    was: loaded into a `bb` session after the full module, it stops at
    "no matching type", and without its records it prints the five
    lines.
  - `tools/check` passes all 151 checks on Go, on `bb`
    (`--backend=clj --runtime=bb`) and on JVM Clojure
    (`--runtime=jvm`), and the import comments hold.

Found while writing it, on stock too, and not in the PR:

- **A one-file program's REPL module doesn't load on JVM Clojure**
  ([README](README.md) item 41). A record method's hint names the
  program's namespace, `^niva.generated.main.main_Point`, but in REPL
  mode the class is `niva.repl.session.main_Point`:
  `ClassNotFoundException`. `bb` ignores hints.
  [010](010-clj-hint-records-pr.md) left this untried. With several
  packages `cljClassHint` names the session's namespace, so MPL doesn't
  hit it.
- **A method on another package's record doesn't bind its fields**
  (item 42). `Circle grow -> Int = radius + 1` in `main.niva`, with
  `Circle` in `model/shapes.niva`, emits `(+ radius 1)`, where the
  type's own package gets `(let [radius (:radius this)] …)`. Both
  runtimes stop at `Unable to resolve symbol: radius`, as a program
  too; Go prints `3`. `this radius` works. MPL doesn't hit it.
- **The Go-built compiler is slow on MPL's REPL module,** and varies a
  lot: 39 s to 3 min a run on stock (three runs), 35 s to 2.5 min on the
  branch (three), at full CPU. The jar takes 0.6 s, and the Go-built
  compiler builds MPL as a Clojure program in 5.9 s. Not looked into.
  Its module runs the demo in `bb` as the jar's does. It differs from
  the jar's only in its `declare` line, whose order is random on Go,
  and in 8 copies of `Expr` methods specialized for `Sum` and
  `Product`, which the jar emits and the Go-built compiler doesn't, as
  [010](010-clj-hint-records-pr.md) found for a smaller program.

Left out of the PR on purpose: MPL, and item 38.

Everything below the line is the pull request as opened.

---

**Title:** [clj] repl mode: every package's types before the functions that use them

A program in REPL mode doesn't load when a function names a type from a
file that sorts after its own. With `model/shapes.niva`:

```Scala
union Shape =
| Circle radius: Int
| Square side: Int
```

and `main.niva`:

```Scala
Shape size -> Int = | this
  | Circle => radius
  | Square => side
shape::Shape = Square side: 3
shape size echo
```

`nin build --backend=clj --repl`, then `(load-file "main.clj")` in
`.niva_clj`, stops at

```
Unable to resolve symbol: model_shapes_Circle
```

on bb and on JVM Clojure. As a program, `nin run --backend=clj` prints
`3`.

REPL mode emits every package into one namespace, `niva.repl.session`,
each package's records, union branches and enum values just before its
own functions. Packages go in the order of their files' paths, so
`main`'s `size` came before `(defrecord model_shapes_Circle [radius])`.
A method on a record names its class in a hint too,
`^niva.repl.session.model_shapes_Circle`, which JVM Clojure resolves as
it compiles.

Now every package's types are emitted first, then every package's
functions. A program's files are each emitted from one package, so
they're unchanged.

Two tests, each failing without the change: a union, an enum and a
record from `model/shapes.niva`, all defined before any function, and
a two-file REPL module run on bb.

`niva test`: 410 pass, 2 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work.
