# 010 Clojure: no class hint for a union or an enum, as a pull request

**Status:** Opened 2026-10-26 as [#462](https://github.com/gavr123456789/Niva/pull/462), not as a draft. A PR
without an issue, as [009](009-clj-record-names-pr.md) (#461) was: a
hint has to name a class, and a union or an enum has none, so there's
nothing for Gavr to decide. No CI checks. Each paragraph went in on one
line. `self-host` was still at `03b69df4`.
**Branch:** [`dharmatech/Niva:clj-hint-records`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:clj-hint-records),
one commit on `self-host` at `03b69df4`: `11f91733`. Locally in
`~/src/Niva-self-host-clj-hints`, a worktree of `~/src/Niva-self-host`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

It's item 39 in the [README](README.md), found right after #461 was
merged, checking Gavr's next two commits, `b73180dd` and `03b69df4`.
They add type hints to the generated functions, and a record's hint
names its class with `.`, as #461 does. But `cljClassHint` hints every
custom type, and a union is one record per branch and an enum is
keywords: on JVM Clojure, a method on either stops at
`ClassNotFoundException`, in one file too. `bb` ignores hints. MPL is
all methods on its `Expr` union, so on JVM it stopped at the first.

**The fix** follows the Go backend, which has the union types of every
package in its symbol table (`unionTypeIds`):

- `IrModule cljClasslessTypes` collects the unions and enums of every
  package. The context holds them as `classlessTypes`, and
  `cljClassHint` gives them no hint. A union's branch is a record and
  keeps its hint. Types not in the set are hinted as before, so Gavr's
  `cljTypeHintsMapNullableMutableAndCustomTypes`, which builds a
  context by hand, holds unchanged.
- Each file of a program in several is emitted from a module of its
  own package, but another package's union can be an argument there.
  So a new `fullFromIrModule:debug:mode:namespace:initialImports:classlessTypes:mainClass:`
  takes the whole module's set, and the existing six-keyword one, the
  "Public entry point", computes it from its own module.
- Found while writing it, and in the same expression: the hint's
  package used `cljGeneratedNamespace`, so a record method in
  `my-geo.niva` was hinted `^niva.generated.my-geo.my-geo_Circle`, a
  package that doesn't exist (the class is in `niva.generated.my_geo`):
  `ClassNotFoundException` on JVM. It now uses #461's
  `cljGeneratedClassPackage`. It's a separate line of the diff, so it
  can be dropped if the PR should be only item 39.

**Tests,** next to Gavr's hint tests: no hint for a union or an enum
but a branch's, in one file; another package's union and enum in a
file of a program in several; the package of a namespace with a dash;
and a program with methods on a union and an enum run on JVM Clojure,
since `bb` would pass without the fix.

## What was checked

On 2026-10-26, each from an exported copy, with stage 1 at `781c5b05`
(`Niva/Niva` is the same at `03b69df4`) and a copy of its home:

- `niva test`: 405 of 406 pass on the branch, against 401 of 402 on
  stock `03b69df4`, so the 4 new tests pass. The one failing on both is
  `babashkaHostLoadsRecoversAndWatchesSources`, as for 005 to 009.
- The tests fail without the change: with only the test file added to
  stock, 401 pass and 5 fail, the 4 new ones and
  `babashkaHostLoadsRecoversAndWatchesSources`.
- Nine small programs, at stock `03b69df4` and on the branch, on `bb`,
  JVM Clojure and `lg`: a union method in one file, an enum method in
  one file, a record method in `my-geo.niva`, and 009's six (types,
  unions and enums across files, a thrown branch, a dashed package, a
  constructor in its own file). Stock fails on JVM for 7 of the 9, at
  `ClassNotFoundException` on a hint. The branch runs all 9 on `bb` and
  JVM. On `lg` nothing changes: the hints don't bother it, the 3 that
  match only within a file run, and the others stop at the class name
  of another namespace's record, as with #461 (let-go #1101).
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and the Go-built compiler runs the one-file union program on
  JVM Clojure. Its Clojure differs from the jar's by an unused copy of
  `area` specialized for `Square`, which the jar emits and the Go-built
  compiler doesn't. Stock `03b69df4` differs the same way, so it's not
  from this change; both call `main_Shape__area`.
- MPL with the branch,
  `NIVA_SELF_HOST=~/src/Niva-self-host-clj-hints`:
  - Without shims, JVM Clojure stops at item 17 in
    `mpl/simplify.clj` again, as at #461, where at `03b69df4` it
    stopped at the hint on `Expr` in `mpl/tree.clj`.
  - With items 17 and 8 shimmed in the generated `niva/core.clj`, as
    for 009, the demo's 34 lines match Go's on `bb` and JVM Clojure,
    and all 151 checks pass on `bb`, on JVM Clojure and on Go.

Not covered: REPL mode with one package. There `currentPackage` is the
package, so a record's hint is `niva.generated.main.main_Person`, while
the REPL's namespace is `niva.repl.session`. `bb` ignores hints, and it
wasn't tried on JVM. Gavr's `cljFunctionHintsUseClojureTypeTags`
asserts the same hint for a one-file program emitted as `(ns main …)`,
a path the compiler uses only for REPL mode. Left as it is.

Everything below the line is the pull request as opened.

---

**Title:** [clj] no class hint for a union or an enum, which have no class

Since `b73180dd`, a method on a union doesn't compile on JVM Clojure:

```Scala
union Shape =
| Circle r: Int
| Square side: Int
Shape area -> Int = | this
  | Circle => r * r * 3
  | Square => side * side
(Square side: 3) area echo
```

`nin run --backend=clj --runtime=jvm` stops at

```
Syntax error (ClassNotFoundException) compiling fn* at (niva/generated/main.clj:8:1).
niva.generated.main.main_Shape
```

bb ignores hints and prints `9`, as JVM Clojure did before. An enum is
the same: `Color isRed -> Bool = this == Color.Red` stops at
`niva.generated.main.main_Color`.

`cljClassHint` gives every custom type its class,
`(defn main_Shape__area ^long [^niva.generated.main.main_Shape this]`,
but a union is one record per branch and an enum is keywords, so
neither has a class. Arguments and return values of those types got
the same hint.

The context now has the unions and enums of every package, as the Go
backend has its `unionTypeIds`, and `cljClassHint` skips them. A branch
is a record and keeps its hint. A file of a program in several is
emitted from its own package, but another package's union can be an
argument there, so it gets the whole module's set through a new
`fullFromIrModule:…classlessTypes:mainClass:`. The existing one
computes the set from its own module.

Also in the same expression: a hint's package now has a namespace's
`-` as `_`, with `cljGeneratedClassPackage`. A record method in
`my-geo.niva` was hinted `^niva.generated.my-geo.my-geo_Circle`, which
JVM Clojure can't find either.

Four tests, each failing without the change: no hint for a union or an
enum but a branch's, another package's union and enum in a program of
several files, the package of a namespace with a dash, and a program
with methods on a union and an enum run on JVM Clojure.

`niva test`: 405 pass, 4 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work.
