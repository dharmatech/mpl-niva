# 011 Clojure: collection `+` and `-`, and `any:`, as a pull request

**Status:** Drafted 2026-10-26, not opened. A PR without an issue, as
[009](009-clj-record-names-pr.md) (#461) and
[010](010-clj-hint-records-pr.md) (#462) were: the runtime already has
the functions under Go's names, and `any:` is in the type checker, so
there's nothing for Gavr to decide.
**Branch:** `clj-collection-ops`, one commit on `self-host` at
`a4429c50`: `8fe22164`. Locally in `~/src/Niva-self-host-clj-ops`, a
worktree of `~/src/Niva-self-host`. Not pushed.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

It's items 17 and 8 in the [README](README.md), the last two that
stopped MPL on Clojure after #461 and #462. Both were shimmed by hand
in the generated runtime until now.

**Item 17 is wider than it said.** It named list `+` alone, but `+` and
`-` on sets and maps fail the same way: the calls are `List__+`,
`Set__+`, `Set__-`, `Map__+` and `Map__-`, and the runtime defines all
five, as `List__plus`, `Set__plus`, `Set__minus`, `Map__plus` and
`Map__minus`. Every operator method in the Clojure runtime has Go's
word for it (`Char__equals` too), and none has an operator in its
name, so it's the call that's off: the Go backend turns an operator
into a word (`goSanitizeIdent`), and the Clojure backend didn't. The
first plan was runtime aliases (`(def List__+ List__plus)`, as the
shim did); after seeing the five, the user chose on 2026-10-26 to fix
the call instead.

**The fix:**

- `String cljCoreMethodName`, next to `cljOperator`, gives an operator
  Go's word for it, all thirteen that `goSanitizeIdent` maps, and
  `coreFuncNameFromCljCall` uses it in place of `cljSanitizeIdent`.
  It's only for calls into `niva.core`. A method a program declares on
  an operator keeps its name, as `cljCustomBinaryOnCoreReceiverUsesDeclaringPackage`
  checks (`main__core_String__-`).
- `(defn List__any [s predicate] (boolean (some predicate s)))`, next
  to `List__filter`. `some` returns the predicate's value or `nil`, so
  `boolean` makes it `true` or `false`, as Go's `List__any` returns a
  `bool`.

**Tests,** at the end of `cljBackTests.niva`, after #454's and #455's,
in their style: the five calls and that the runtime defines each, the
`any:` call and its definition, and a program with both run on `bb`
against the runtime, `CompilerConstants nivaCoreSource` written as
`niva/core.clj`.

## What was checked

On 2026-10-26, each from an exported copy, with stage 1 at `781c5b05`
(`Niva/Niva` is the same at `a4429c50`) and a copy of its home:

- `niva test`: 408 of 409 pass on the branch, against 405 of 406 on
  stock `a4429c50`, so the 3 new tests pass. The one failing on both is
  `babashkaHostLoadsRecoversAndWatchesSources`, as for 005 to 010.
- The tests fail without the change: with only the test file added to
  stock, 405 pass and 4 fail, the 3 new ones and
  `babashkaHostLoadsRecoversAndWatchesSources`.
- A program using every operator on every core type: `Int`, `Float`,
  `String`, `Char`, `Bool`, `==` and `!=` on lists, the five collection
  operators, and `any:`. Compiled with stock and with the branch, the
  Clojure differs only in the five calls. It prints the same on `bb`,
  JVM Clojure and `lg` (but for the order of one set on `lg`), and the
  same as Go but for `7 / 2` (`7/2`, item 18), that set's order, and
  how Go prints a set (item 40).
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and the Go-built compiler gives the same Clojure and the same
  output for that program as the jar.
- **MPL with the branch, nothing shimmed,**
  `NIVA_SELF_HOST=~/src/Niva-self-host-clj-ops`, each from a fresh
  `.niva_clj/`: the demo's 34 lines match Go's on `bb` and on JVM
  Clojure, and `tools/check --backend=clj` passes all 151 checks on
  both (`--runtime=jvm` for JVM). `mpl/simplify.clj` calls
  `List__plus` and `List__any` 10 times. On Go, `tools/check` passes
  all 151. So with #461, #462 and this, MPL runs on every NIN backend
  and runtime as written, `lg` aside.
- On `lg`, MPL still stops at the class name of another namespace's
  record, `niva.generated.mpl.tree.mpl_tree_Num`, as since #461
  (let-go #1101).

Everything below the line is the pull request as it would be opened.

---

**Title:** [clj] collection + and - call the runtime's plus and minus, and lists get any:

`+` on two lists passes the type checker but doesn't run on Clojure:

```Scala
({1 2} + {3}) echo
```

```
Unable to resolve symbol: niva.core/List__+
```

on bb, and `No such var: niva.core/List__+` on JVM Clojure. On Go it
prints `{ 1 2 3 }`.

`+` and `-` on sets and maps are the same: they call `Set__+`,
`Set__-`, `Map__+` and `Map__-`. The runtime has all five, as
`List__plus`, `Set__plus`, `Set__minus`, `Map__plus` and `Map__minus`,
the names Go uses: the Go backend turns an operator into a word
(`goSanitizeIdent`), and the Clojure backend didn't. So a core call now
names an operator method with Go's word for it, through
`cljCoreMethodName` next to `cljOperator`, and each call reaches the
function the runtime has. It's only for calls into `niva.core`: a
method a program declares on an operator keeps its name.

`any:` passes the type checker too, but `({1 2} any: [it > 1]) echo`
stops at `Unable to resolve symbol: niva.core/List__any`. It's now
`(defn List__any [s predicate] (boolean (some predicate s)))`, `true`
or `false` as on Go.

Three tests, each failing without the change: the five calls and that
the runtime defines each, the `any:` call and its definition, and a
program with both run on bb against the runtime.

`niva test`: 408 pass, 3 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work.
