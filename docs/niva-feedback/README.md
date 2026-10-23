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
| [002 Go: a `^` inside a value doesn't return from the method](002-go-return-inside-value.md) | Drafted, to be filed now that #433 is merged, after re-checking it on the new `self-host`. A PR is drafted for each option: [make it work](002-go-return-inside-value-pr.md), the one suggested, or [forbid it](002-go-return-inside-value-pr-forbid.md). |

## Self-hosted compiler

Found while porting to the self-hosted compiler
([series 001](../design/implementation/001-self-hosted/README.md)), on
`self-host` at `6af8f9f6`. Most likely to bite first; only the first is
filed so far:

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
   [002](002-go-return-inside-value.md), to be filed after
   re-checking it on the new `self-host`, with a fix and a check
   spiked.
3. **Clojure:** a type declared in one file can't be used from another.
   The generated code names a class that doesn't exist:
   `niva.generated.mpl.tree.mpl.tree_Num` for a file in a folder (the
   path is doubled), an unresolved `niva.generated.tree/tree_Num` for one
   at the root.
4. **Both:** compile errors exit 0. On Clojure, a run-time error exits 0
   too.
5. **Go:** a list literal of branch values given as a `List(Expr)`, such
   as `Sum terms: {(Sym name: "x")}`, becomes a Go `[]main_Sym`, which
   doesn't compile.
6. **Go:** `^ (…) any: […], not` generates an empty `return` before the
   real one, so the Go build fails.
7. **Go:** `==` on two sums doesn't compile when both are literals, and
   fails at run time when they're `Expr` values (Go can't compare the
   slices inside). The type checker accepts both.
8. **Clojure:** `any:` passes the type checker but isn't in the runtime.
9. **Clojure:** `e toString` ignores a `toString` declared on the union's
   branches.
10. `indexOfFirst:` is in both runtimes, but the type checker doesn't
    know it.
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
    spiking 002, whose option A fixes it first.
15. **Go:** a `throw` that ends a block, as in
    `this < 0 ifTrue: [(Bad text: "negative") throw]`, becomes
    `core.Error__throw()` without the error: the build fails, or the
    error is lost. Found while spiking 002.
16. **Go:** `ifTrue:` alone as a value, `s = this > 0 ifTrue: ["x" echo]`,
    doesn't build: `func() {…}() (no value) used as value`. Found while
    spiking 002.

Gaps rather than bugs: neither runtime has `sortedWith:` or `all:`.

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
