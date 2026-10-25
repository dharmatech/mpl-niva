# Niva feedback

Bugs and suggestions for Niva found while writing this library, drafted
here before they're filed on
[gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues).
One report at a time: the next is filed once the previous one has been
dealt with, so Niva's author isn't flooded.

Each draft is numbered in the order it was written. It starts with a
short header (status, the Niva commit it was checked against, how we
found it), and below a line, the issue's title and body exactly as
filed. Once filed, the header links the issue.

| Draft | Status |
|---|---|
| [000 negative literal as a unary receiver](000-negative-literal-unary-receiver.md) | Filed as [#430](https://github.com/gavr123456789/Niva/issues/430) (2026-10-22). Fixed by [#431](https://github.com/gavr123456789/Niva/pull/431) ([draft](000-negative-literal-unary-receiver-pr.md)), merged the same day into `main` and `self-host`. |
| [001 Clojure: an early `^` is silently dropped](001-clojure-early-return.md) | Filed as [#432](https://github.com/gavr123456789/Niva/issues/432) (2026-10-22), closed the same day. Fixed by [#433](https://github.com/gavr123456789/Niva/pull/433) ([draft](001-clojure-early-return-pr.md)), cut down to the compile error after Gavr agreed it needs at least that, and merged into `self-host` the same day. Follow-up for his review: [#434](https://github.com/gavr123456789/Niva/pull/434) ([draft](001-clojure-early-return-followup-pr.md)), merged 2026-10-23. |
| [002 Go: a `^` inside a value doesn't return from the method](002-go-return-inside-value.md) | Filed as [#435](https://github.com/gavr123456789/Niva/issues/435) (2026-10-23). Gavr proposed his own design on Discord: every match whose value is used becomes a temp variable assigned in each branch. A [sketch of it](002-go-return-inside-value-pr-temp.md), built on 003 ([#436](https://github.com/gavr123456789/Niva/pull/436)), was opened as draft PR [#437](https://github.com/gavr123456789/Niva/pull/437) and merged into `self-host` the same day (2026-10-23). The issue is still open. The drafts for the issue's options, [make it work](002-go-return-inside-value-pr.md) and [forbid it](002-go-return-inside-value-pr-forbid.md), stay as fallbacks. |
| [003 Go: three bugs in a match used as a statement](003-go-statement-match-fixes-pr.md) | A PR without an issue: opened as [#436](https://github.com/gavr123456789/Niva/pull/436) (2026-10-23) and merged into `self-host` the same day. Split out of the 002 sketch, which builds on it. |
| [004 Go: `==` on a type that holds a list](004-go-equality.md) | Filed as [#439](https://github.com/gavr123456789/Niva/issues/439) (2026-10-23). Asks what `==` should mean in NIN, with three options: content with a type error where Go can't compare, content everywhere, or identity. On Discord, Gavr then listed five options of his own. His 3, structural by default with a generated compare function for types that hold collections, was sketched as [#440](https://github.com/gavr123456789/Niva/pull/440) (row below) and merged into `self-host` on 2026-10-25. The issue is still open. |
| [004 Go: `==` by content, Gavr's option 3, as a pull request](004-go-equality-pr.md) | Opened 2026-10-24 as draft PR [#440](https://github.com/gavr123456789/Niva/pull/440), "For #439". A sketch of the third option Gavr listed on Discord: structural `==`, with a generated compare function on Go where Go's `==` can't compare by content, plus a type's own `==` as a commit he can drop. For discussion, not to push him toward it. Gavr agreed on `mut` fields and generic types, and asked for null lists, blocks by signature and `Box(List(Int))`; fixed and pushed the same day, after merging his `200aa83a`, with a question back on blocks on Clojure. He added style updates and merged it on 2026-10-25, squashed as `f7979fd4`. Right after, he filed our blocks question as [#446](https://github.com/gavr123456789/Niva/issues/446), and the generic union value the PR couldn't run (`r::Result(Int) = Ok t: 1`) as [#447](https://github.com/gavr123456789/Niva/issues/447). |
| [005 Go: a list literal of one union branch](005-go-list-literal-of-branch.md) | Draft, not filed. README item 5, re-checked at `a56f659c`: any literal whose items are all one branch, a narrowed `this` included, has that branch's type, which Go won't take as a list of the union. Asks how far a fix should go, suggesting 1: type a literal by what its context expects, and points to the draft PR in the row below instead of offering one. None of Gavr's #441 to #448 covers it; #447 (a generic union as a type) only means generic unions can't be tried yet. |
| [005 Go: a list literal of one union branch, option 1 as a draft pull request](005-go-list-literal-of-branch-pr.md) | Drafted, not opened or pushed: branch `literal-expected-type` at `e01dc22d` on `self-host` `a56f659c`, in `~/src/Niva-self-host-literal`. The issue's option 1 in Gavr's style, with four tests that fail without it. To be opened as a draft right after the issue, "For #N", then the PR's number put in the issue. |

## Self-hosted compiler

Found while porting to the self-hosted compiler
([series 001](../design/implementation/001-self-hosted/README.md)), on
`self-host` at `6af8f9f6`. Most likely to bite first. Re-checked on
2026-10-23 at `3746ffb7`, after #437: each still reproduces unless
noted, except 11 to 13, which weren't re-run. Items 1, 2 and 7 are
filed.

1. **Clojure:** a `^` before the end of a method is dropped, with no
   error, and the method carries on to return the wrong value. It
   happens in a branch of `ifTrue:`, `ifFalse:`, `ifTrue:ifFalse:` and
   `match`, and in loops: `this > 0 ifTrue: [^ "positive"]` followed by
   `"not positive"` always gives `"not positive"`. Filed as
   [#432](https://github.com/gavr123456789/Niva/issues/432)
   ([draft](001-clojure-early-return.md)). Since
   [#433](https://github.com/gavr123456789/Niva/pull/433), merged the
   same day, such a `^` is a compile error instead.
2. **Go:** a `^` inside a value doesn't return from the method. In
   `s = this > 0 ifTrue: [^ "returned"] ifFalse: ["kept"]` the value
   is an anonymous function, which the `^` only leaves, so `s` is
   `"returned"` and the method carries on. Drafted as
   [002](002-go-return-inside-value.md) and filed as
   [#435](https://github.com/gavr123456789/Niva/issues/435). Fixed by
   [#437](https://github.com/gavr123456789/Niva/pull/437), Gavr's design
   of every match whose value is used as a variable assigned in each
   branch, merged into `self-host` 2026-10-23.
3. **Clojure:** a type declared in one file can't be used from another.
   The generated code names a class that doesn't exist:
   `niva.generated.mpl.tree.mpl.tree_Num` for a file in a folder (the
   path is doubled), an unresolved `niva.generated.tree/tree_Num` for one
   at the root. The record is declared with a dot in its name,
   `(defrecord mpl.tree_Num [value])`, which Babashka and JVM Clojure
   read as a class. let-go accepts it, so MPL runs as laid out on `lg`
   ([series 002](../design/implementation/002-let-go/README.md)).
4. **Both:** compile errors exit 0. On Clojure, a run-time error exits 0
   too, though `bb` and `lg` exit 1 when run directly: `nin run` drops
   it.
5. **Go:** a list literal of branch values given as a `List(Expr)`, such
   as `Sum terms: {(Sym name: "x")}`, becomes a Go `[]main_Sym`, which
   doesn't compile. At `a56f659c` it's any literal whose items are all
   one branch, a value a match has narrowed included (`{this}` in
   `| Num =>`), and sets and maps too. On Clojure, a `mut` list
   declared from one loses its atom. Drafted as
   [005](005-go-list-literal-of-branch.md).
6. **Go:** `^ (…) any: […], not` generates an empty `return` before the
   real one, so the Go build fails. Works at `3746ffb7`, likely since
   #437.
7. **Go:** `==` on a type that holds a list doesn't build (`struct
   containing []int cannot be compared`), and on two union values that
   hold one, such as two `Sum`s, fails at run time (`comparing
   uncomparable type main.main_Sum`). The type checker accepts both. The
   compilers also disagree on what `==` means: the Kotlin compiler
   compares Niva types by identity (two equal `Point`s are `false`),
   NIN by content on Go and Clojure. A type's own `==` is called by the
   Kotlin compiler and ignored by NIN. Filed as
   [#439](https://github.com/gavr123456789/Niva/issues/439)
   ([draft](004-go-equality.md)), a question of which it should be.
   Fixed by [#440](https://github.com/gavr123456789/Niva/pull/440), Gavr's
   option 3, merged into `self-host` 2026-10-25: `==` compares by content
   on Go too, and a type's own `==` is called.
8. **Clojure:** `any:` passes the type checker but isn't in the runtime.
9. **Clojure:** `e toString` ignores a `toString` declared on the union's
   branches.
10. `indexOfFirst:` is in both runtimes, but the type checker doesn't
    know it. The Kotlin compiler does.
11. A file named `test.niva` at the root is silently left out of the
    sources, so it can't be the entry file.
12. Top-level code in a file other than the entry file isn't
    type-checked.
13. The first parse error we hit gave a line and column but no file.
14. **Go:** a match on errors used as a statement runs its else branch
    after an error branch too, with the zero value as `it`: for -5,
    `| this parse | Bad => it text echo |=> ("parsed " + it toString) echo`
    prints `negative`, then `parsed 0`. Two in one block declare `it`
    twice, and an `it` a branch doesn't use stops the build. Found while
    spiking 002, and fixed in [#436](https://github.com/gavr123456789/Niva/pull/436) ([draft](003-go-statement-match-fixes-pr.md)).
15. **Go:** a `throw` that ends a block, as in
    `this < 0 ifTrue: [(Bad text: "negative") throw]`, becomes
    `core.Error__throw()` without the error: the build fails, or the
    error is lost. Found while spiking 002. At `3746ffb7` the build
    fails at an unused `__callResult`; Clojure gets it right.
16. **Go:** `ifTrue:` alone as a value, `s = this > 0 ifTrue: ["x" echo]`,
    doesn't build: `func() {…}() (no value) used as value`. Found while
    spiking 002, and fixed in its [sketch](002-go-return-inside-value-pr-temp.md).
17. **Clojure:** `+` on two lists calls `niva.core/List__+`, which the
    runtime doesn't define. It has `List__plus`, the name Go uses: the
    Go backend turns `+` into `plus` (`goNames.niva`), and the Clojure
    backend doesn't. `check.niva`'s `PrintingChecks all + EqualityChecks all`
    stops there. Found trying let-go
    ([series 002](../design/implementation/002-let-go/README.md)).
18. **Clojure:** `/` on two `Int`s becomes Clojure's `/`, which makes a
    ratio: `17 / 5` is `17/5` and `-7 / 2` is `-7/2`, where Go gives 3
    and -3. A loop halving `n` while it's over 1 ends at `5/8`. Found
    trying let-go.
19. **Both:** a set literal with a repeated item, `#(1 2 2 3)`, doesn't
    work. On Clojure it becomes `#{1 2 2 3}`, which Clojure won't read,
    so the program doesn't load; found trying let-go. On Go the build
    stops at `duplicate key 2 in map literal`.
20. `==` and `!=` on two lists are type errors: `{1 2} == {1 2}` gives
    "wrong arg type for == expected String". `protocols/collection.niva`
    declares both with a `String` argument. The Kotlin compiler compares
    lists by content. Fixed in `200aa83a` and #440 (2026-10-24 and 25).
21. A top-level line starting with `(` right after a one-line `type` or
    `union` is parsed as part of the declaration: `type Point x: Int
    y: Int` then `(1 + 2) echo` gives "in type declaration identifier
    expected, but got Integer(1)". The Kotlin compiler prints 3.
22. `contains:` on a list passes the type checker but is in neither
    runtime: Go stops at `undefined: core.List__contains`, Clojure at
    `Unable to resolve symbol: niva.core/List__contains`. The Kotlin
    compiler has it. Found while drafting 004.
23. **Go:** `ifNotNull:` on a nullable union, map or list value doesn't
    build ("cannot infer T"): it expects a pointer, which only nullable
    structs and numbers are. Found while sketching
    [004's PR](004-go-equality-pr.md), as are 24 and 25. Filed by Gavr
    as [#444](https://github.com/gavr123456789/Niva/issues/444)
    (2026-10-24), from rewriting our `goEquality.niva` with
    `ifNotNull:`, with [#443](https://github.com/gavr123456789/Niva/issues/443)
    for `it` not being narrowed there. `ifNotNull:` lives in NIN's own
    `libs/maybe.niva`, so programs built with NIN don't have it.
24. **Go:** two nullable locals compare as pointers: `x::Int? = 5`,
    `y::Int? = 5`, then `x == y` is `false` (Clojure `true`), and
    `x == 5` doesn't build ("mismatched types *int and untyped int").
25. A nullable block type, `f: [Int -> Int]?`, doesn't parse: "Got
    invalid token after CloseBracket".

Gaps rather than bugs: neither runtime has `sortedWith:`, which the
Kotlin compiler has, or `all:`, which it doesn't either.

## Legacy compiler

Since series 001 the project uses the self-hosted compiler, so these
matter less. Before filing one, check whether it affects the self-hosted
compiler too, as we did for #430.

Found but not drafted yet, in the order we plan to file them:

1. A binary method on `Int` with an existing selector, such as
   `Int + other::Box -> Box`, isn't rejected the way a duplicate on a
   user type is. It changes the built-in's return type, so `1 + 2`
   becomes a `Box`.
2. A `toString` declared on a union is dead code: Niva also generates a
   default `toString` on each branch, and that one wins.
3. A `toString` declared in another file than its type is moved into
   the type's file, without the imports its body needs.
4. `List::Int new` and `Set::Int new` pass the checker and generate
   Kotlin `List()` and `Set()`, which don't compile.
5. `niva run` and `niva test` exit 0 when the build or a test fails.

Each was reproduced on its own against `main` at `ece36eb7`, and none
had an existing issue on 2026-10-22.
