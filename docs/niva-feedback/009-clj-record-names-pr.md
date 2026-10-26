# 009 Clojure: a type from another file, as a pull request

**Status:** Opened 2026-10-26 as [#461](https://github.com/gavr123456789/Niva/pull/461), not as a draft. A PR
without an issue, as
[007](007-inject-into-pr.md) (#454) and [008](008-sorted-with-pr.md)
(#455) were: the generated code names things Clojure can't resolve, and
naming them as Clojure does leaves nothing for Gavr to decide. No CI
checks. Each paragraph went in on one line.
**Branch:** [`dharmatech/Niva:clj-record-names`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:clj-record-names),
one commit on `self-host` at `139a44e0`: `c6ade25a`. Locally in
`~/src/Niva-self-host-clj-records`, a worktree of `~/src/Niva-self-host`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

It's item 3 in the [README](README.md), found at `6af8f9f6` while
porting MPL to the self-hosted compiler, with its causes pinned down at
`781c5b05` by patching the generated code by hand
([series 010](../design/implementation/010-repl/README.md)). First of
the items re-ranked on 2026-10-26, since it alone stops MPL from
building on Clojure, on `bb` and JVM Clojure alike. The base is
`139a44e0`, one commit past our pin: Gavr's refactor of
`back/tests/cljBackTests.niva`, which the tests build on.

The hand patch named two causes. Writing the fix found a third, and
that enum values have the first one too:

1. **Dots.** A type keeps its package's dots, since `cljSanitizeIdent`
   leaves them: `(defrecord mpl.tree_Num [value])`. JVM Clojure reads a
   dotted symbol as a class, so it can't build `->geo.shapes_Circle`
   even in that type's own file (`ClassNotFoundException`), nor resolve
   an enum value such as `geo.kinds_Color_Red` there. `bb` fails to load
   the namespace. A new `cljTypeIdent` turns the dots into `_`, as the
   Go backend's `goSanitizeIdent` does, and names records, union
   branches, enum values and constructors.
2. **`/` for a class.** Another namespace names a record's class as a
   var, `niva.generated.mpl.tree/mpl.tree_Num`. Neither runtime
   resolves that, and it breaks a file at the root too
   (`niva.generated.model/model_Circle`). `cljQualifiedTypeName:` now
   gives the Java class, `niva.generated.mpl.tree.mpl_tree_Num`, with
   the namespace's `-` as `_` (`cljGeneratedClassPackage`). A new
   `cljQualifiedVarName:ctx:` keeps `/` for the vars: constructors and
   enum values. The check for another package, which was written out
   in two places, is `cljCanQualify:`, after the `canQualify` it
   replaces.
3. **A thrown branch without fields.** `NotFound throw` named its
   constructor without its namespace, `(->kinds_NotFound)`, when the
   branch is declared in another file. It now goes through
   `cljQualifiedVarName:ctx:` too.

**Tests,** in `cljBackTests.niva`, next to Gavr's package tests: the
names in a folder package's file and in the `main` that uses it (no
dot in any name, the match case's class with `.`, the constructors and
the enum value with `/`), the same kind of program run on `bb`, a
namespace with a dash, and the thrown branch. Two existing tests,
`cljPackageOutputQualifiesUnionBranches` and
`cljPackageOutputQualifiesErrorBranches`, asserted the `/` form of a
class for a package at the root, the bug's second cause, so they now
assert `.`.

## What was checked

On 2026-10-26, each from an exported copy, with stage 1 at `781c5b05`
(`Niva/Niva` is the same at `139a44e0`) and a copy of its home:

- `niva test`: 398 of 399 pass on the branch, against 394 of 395 on
  stock `139a44e0`, so the 4 new tests pass. The one failing on both is
  `babashkaHostLoadsRecoversAndWatchesSources`, as for 005 to 008.
- The tests fail without the change: with only the test file added to
  stock, 392 pass and 7 fail, the 4 new ones, the 2 updated ones and
  `babashkaHostLoadsRecoversAndWatchesSources`.
- Five programs of two files, on `bb` and JVM Clojure: a folder package
  with a type, a union and an enum, used and matched from `main`; a
  union at the root, matched from `main`; an enum value used in its own
  folder file, with a branch without fields thrown and matched from
  `main`; the same at the root; and a package named `my-geo`. Stock
  stops on each, on both runtimes. With the branch, each prints what it
  should on both.
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and the Go-built compiler gives the same Clojure as the jar
  for the first program.
- MPL with the branch, `NIVA_SELF_HOST=~/src/Niva-self-host-clj-records`:
  - On Clojure, the demo and the checks now stop at item 17 on both
    runtimes: `mpl.tree`, `mpl.notation` and `mpl.order` load, and
    `mpl/simplify.clj` names `niva.core/List__+` as it loads. With 17
    shimmed in the generated `niva/core.clj`
    (`(def List__+ List__plus)`), they stop at item 8, `List__any`, in
    the same file. Stock stops at item 3, at
    `niva.generated.mpl.tree.mpl.tree_Num`.
  - With both shimmed (and `(defn List__any [l f] (boolean (some f l)))`),
    the demo's 34 lines match Go's on `bb` and on JVM Clojure, and
    `tools/check --backend=clj` passes all 151 checks on both
    (`--runtime=jvm` for JVM). That's what the hand patch gave, with
    the generated code as the compiler wrote it.
  - On Go, `tools/check` passes all 151, and MPL's generated Go is the
    same byte for byte with the stock jar.
- **let-go no longer runs it.** `lg` at let-go `18df3ad5`, still its
  `main`, ran stock's output: it accepts dotted record names, and its
  records are vars, so `ns/Name` resolves. It doesn't resolve a record
  by its class from another namespace, `ns.Name`, and has no `import`,
  so with the branch every program above that matches on another
  file's record stops on `lg` at "Can't resolve
  niva.generated.model.model_Circle in this context". So does MPL. No
  spelling works on all three, and `lg` isn't one of NIN's runtimes, so
  the PR targets `bb` and JVM Clojure. On `lg` it would want let-go to
  resolve `ns.Name` as Clojure does
  ([series 002](../design/implementation/002-let-go/README.md)). The
  user chose this on 2026-10-26: fix Niva for `bb` and JVM Clojure
  first, as drafted, and ask let-go for the class name, drafted as
  [let-go 001](../let-go-feedback/001-record-class-name.md), to be
  filed now that this PR is open.

Not covered: REPL and hot-reload modes beyond their existing tests,
which pass. In REPL mode every package is one namespace, so nothing is
qualified (items 37 and 38 are its own bugs).

Left out of the PR on purpose: MPL, and that it needs items 17 and 8
next, which is the following PR.

Everything below the line is the pull request as opened.

---

**Title:** [clj] types from another file: no dots in their names, and record classes qualified with `.`

A type declared in one file can't be used from another on Clojure. With
`geo/shapes.niva`:

```Scala
union Shape =
| Circle radius: Int
| Square side: Int
```

and `main.niva`:

```Scala
shape::Shape = Square side: 3
name = | shape
  | Circle => "circle"
  | Square => "square"
name echo
```

`nin run --backend=clj` stops at

```
Unable to resolve classname: niva.generated.geo.shapes.geo.shapes_Circle
```

on bb, and at `No such var: niva.generated.geo.shapes/geo.shapes_Circle`
on JVM Clojure. On Go it prints `square`.

Three things in the names the backend emits:

- A type keeps its package's dots: `(defrecord geo.shapes_Circle [radius])`.
  Clojure reads a dotted symbol as a class, so JVM Clojure can't build
  `->geo.shapes_Circle` even in that file, nor resolve an enum value
  like `geo.shapes_Color_Red` there. Type names now turn the dots into
  `_`, as the Go backend does: `geo_shapes_Circle`.
- Another namespace names a record's class as a var,
  `niva.generated.geo.shapes/geo_shapes_Circle`, which neither runtime
  resolves. This one breaks a file at the root too
  (`niva.generated.model/model_Circle`). A class is now named by its
  Java package, `niva.generated.geo.shapes.geo_shapes_Circle`, with a
  namespace's `-` as `_` as Clojure munges it. Constructors and enum
  values are vars and keep `/`.
- A thrown branch without fields, `NotFound throw`, named its
  constructor without its namespace: `(->model_NotFound)`.

The check for another package, written out twice before, is now
`cljCanQualify:`, used by `cljQualifiedTypeName:` for classes and a new
`cljQualifiedVarName:ctx:` for constructors and enum values.

Four tests, each failing without the change: the names in a folder
package and in the `main` that uses it, the same kind of program run on
bb, a namespace with a dash, and the thrown branch.
`cljPackageOutputQualifiesUnionBranches` and
`cljPackageOutputQualifiesErrorBranches` asserted the `/` form of a
class, so they now assert `.`.

`niva test`: 398 pass, 4 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work.
