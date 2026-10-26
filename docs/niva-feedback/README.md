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
| [002 Go: a `^` inside a value doesn't return from the method](002-go-return-inside-value.md) | Filed as [#435](https://github.com/gavr123456789/Niva/issues/435) (2026-10-23). Gavr proposed his own design on Discord: every match whose value is used becomes a temp variable assigned in each branch. A [sketch of it](002-go-return-inside-value-pr-temp.md), built on 003 ([#436](https://github.com/gavr123456789/Niva/pull/436)), was opened as draft PR [#437](https://github.com/gavr123456789/Niva/pull/437) and merged into `self-host` the same day (2026-10-23). We closed it on 2026-10-25: "Fixed by #437, merged into `self-host`." The drafts for the issue's options, [make it work](002-go-return-inside-value-pr.md) and [forbid it](002-go-return-inside-value-pr-forbid.md), stay as fallbacks. |
| [003 Go: three bugs in a match used as a statement](003-go-statement-match-fixes-pr.md) | A PR without an issue: opened as [#436](https://github.com/gavr123456789/Niva/pull/436) (2026-10-23) and merged into `self-host` the same day. Split out of the 002 sketch, which builds on it. |
| [004 Go: `==` on a type that holds a list](004-go-equality.md) | Filed as [#439](https://github.com/gavr123456789/Niva/issues/439) (2026-10-23). Asks what `==` should mean in NIN, with three options: content with a type error where Go can't compare, content everywhere, or identity. On Discord, Gavr then listed five options of his own. His 3, structural by default with a generated compare function for types that hold collections, was sketched as [#440](https://github.com/gavr123456789/Niva/pull/440) (row below) and merged into `self-host` on 2026-10-25. We closed it on 2026-10-25: "Fixed by #440, merged into `self-host`. Comparing blocks on Clojure is in #446." |
| [004 Go: `==` by content, Gavr's option 3, as a pull request](004-go-equality-pr.md) | Opened 2026-10-24 as draft PR [#440](https://github.com/gavr123456789/Niva/pull/440), "For #439". A sketch of the third option Gavr listed on Discord: structural `==`, with a generated compare function on Go where Go's `==` can't compare by content, plus a type's own `==` as a commit he can drop. For discussion, not to push him toward it. Gavr agreed on `mut` fields and generic types, and asked for null lists, blocks by signature and `Box(List(Int))`; fixed and pushed the same day, after merging his `200aa83a`, with a question back on blocks on Clojure. He added style updates and merged it on 2026-10-25, squashed as `f7979fd4`. Right after, he filed our blocks question as [#446](https://github.com/gavr123456789/Niva/issues/446), and the generic union value the PR couldn't run (`r::Result(Int) = Ok t: 1`) as [#447](https://github.com/gavr123456789/Niva/issues/447). |
| [005 Go: a list literal of one union branch](005-go-list-literal-of-branch.md) | Filed as [#449](https://github.com/gavr123456789/Niva/issues/449) (2026-10-25). README item 5, re-checked at `a56f659c`: any literal whose items are all one branch, a narrowed `this` included, has that branch's type, which Go won't take as a list of the union. Asks how far a fix should go, suggesting 1: type a literal by what its context expects, and points to draft PR [#450](https://github.com/gavr123456789/Niva/pull/450) (row below) instead of offering one. Gavr took 1, merging #450 the same day. We closed it on 2026-10-25, noting the case it leaves (an untyped local list passed on as a `List(Expr)`) for a separate issue if it comes up. None of Gavr's #441 to #448 covers it; #447 (a generic union as a type) only means generic unions can't be tried yet. |
| [005 Go: a list literal of one union branch, option 1 as a draft pull request](005-go-list-literal-of-branch-pr.md) | Opened 2026-10-25 as draft PR [#450](https://github.com/gavr123456789/Niva/pull/450), "For #449", right after the issue, whose last paragraph was then edited to name it. Branch `dharmatech/Niva:literal-expected-type` at `e01dc22d` on `self-host` `a56f659c`, locally in `~/src/Niva-self-host-literal`. The issue's option 1 in Gavr's style, with four tests that fail without it. Approved without comments and merged into `self-host` the same day (13:32 UTC), squashed as `ab69e59b`, unchanged. |
| [006 `String` has no ordering](006-string-ordering.md) | Filed as [#451](https://github.com/gavr123456789/Niva/issues/451) (2026-10-25). README item 26: neither compiler has `<` on strings, so it's new, and the form is Gavr's call. Options: 1 `<`, `>`, `<=`, `>=` (Go's operators, Clojure's `compare`), 2 that and a `compareTo:`, 3 no change. Suggests 1, pointing to draft PR [#452](https://github.com/gavr123456789/Niva/pull/452) (row below). Gavr took 1, merging #452 the same day, and we closed the issue with a comment leaving `Char` and a `compareTo:` for when they're needed. |
| [006 `String` ordering, option 1 as a draft pull request](006-string-ordering-pr.md) | Opened 2026-10-25 as draft PR [#452](https://github.com/gavr123456789/Niva/pull/452), "For #451", right after the issue, whose last paragraph was then edited to name it. Branch `dharmatech/Niva:string-ordering` at `69e688aa` on `self-host` `4e809716`, locally in `~/src/Niva-self-host-string-ordering`. Four lines in the `String` protocol and a `compare` case on Clojure, with three tests that fail without it. Approved without comments and merged into `self-host` the same day (15:52 UTC), squashed as `e823f7fe`, with Gavr's restyle of the Clojure case. |
| [007 `inject:into:` has the wrong name in both runtimes, as a pull request](007-inject-into-pr.md) | Opened 2026-10-25 as [#454](https://github.com/gavr123456789/Niva/pull/454): a PR without an issue, as 003 was, since there's nothing to decide. README item 30. Branch `dharmatech/Niva:inject-into` at `a210b4df` on `self-host` `e823f7fe`, locally in `~/src/Niva-self-host-inject-into`: `List__injectInto` renamed to `List__inject__into` in both runtimes, with two tests that fail without it. Approved without comments and merged into `self-host` the same day (16:50 UTC), squashed as `2ad4f629`, unchanged. |
| [008 `sortedWith:` on lists, as on the JVM compiler, as a pull request](008-sorted-with-pr.md) | Opened 2026-10-25 as [#455](https://github.com/gavr123456789/Niva/pull/455): a PR without an issue, since matching the JVM compiler's signature (a block `[a, b -> Int]`) leaves nothing to decide. From the gaps below the list. Branch `dharmatech/Niva:sorted-with` at `b2ff55a2` on `self-host` `2ad4f629`, locally in `~/src/Niva-self-host-sorted-with`: the protocol entry next to `sortedBy:` and `List__sortedWith` in both runtimes, with three tests that fail without it. Approved without comments and merged into `self-host` the same day (17:39 UTC), squashed as `781c5b05`, unchanged. |
| [009 Clojure: a type from another file, as a pull request](009-clj-record-names-pr.md) | Opened 2026-10-26 as [#461](https://github.com/gavr123456789/Niva/pull/461): a PR without an issue, as 007 and 008 were. README item 3. Branch `dharmatech/Niva:clj-record-names` at `c6ade25a` on `self-host` `139a44e0`, locally in `~/src/Niva-self-host-clj-records`: type names without the package's dots, as on Go, a record's class from another namespace named with `.`, and a thrown branch without fields qualified, with four tests that fail without it and two updated. MPL then loads on `bb` and JVM Clojure up to items 17 and 8, and with those shimmed passes all 151 checks on both. It no longer runs on `lg`, which names another namespace's record only with `/`: chosen on 2026-10-26, with a let-go issue for it drafted as [let-go 001](../let-go-feedback/001-record-class-name.md). |

## Self-hosted compiler

Found while porting to the self-hosted compiler
([series 001](../design/implementation/001-self-hosted/README.md)), on
`self-host` at `6af8f9f6`. Most likely to bite first. Re-checked on
2026-10-23 at `3746ffb7`, after #437, and on 2026-10-25 at `4e809716`,
after #450 ([series 004](../design/implementation/004-notes-recheck/README.md)),
11 to 13 included: each still reproduces unless noted. Items 1, 2, 5
and 7 are filed by us, 23 by Gavr.

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
   At `781c5b05` there are two causes, both in the emitter. The record
   keeps its package's dots, since `cljSanitizeIdent` leaves them; JVM
   Clojure then can't build even its constructor, `->mpl.tree_Num`, in
   its own file. And another namespace names the class
   `niva.generated.mpl.tree/mpl.tree_Num`, with `/`, which is how
   `cljQualifiedTypeName` qualifies an enum var, where Clojure names a
   record class `niva.generated.mpl.tree.mpl_tree_Num`. With both
   patched by hand in the generated code, and 8 and 17 shimmed, MPL's
   demo matches Go and all its 151 checks pass, on `bb` and on JVM
   Clojure ([series 010](../design/implementation/010-repl/README.md)).
   A fix in the emitter, which also finds a third cause (a thrown
   branch without fields from another file), is open as
   [#461](https://github.com/gavr123456789/Niva/pull/461) ([draft](009-clj-record-names-pr.md)).
4. **Both:** compile errors exit 0. On Clojure, a run-time error exits 0
   too, though `bb` and `lg` exit 1 when run directly: `nin run` drops
   it.
5. **Go:** a list literal of branch values given as a `List(Expr)`, such
   as `Sum terms: {(Sym name: "x")}`, becomes a Go `[]main_Sym`, which
   doesn't compile. At `a56f659c` it's any literal whose items are all
   one branch, a value a match has narrowed included (`{this}` in
   `| Num =>`), and sets and maps too. On Clojure, a `mut` list
   declared from one loses its atom. Filed as
   [#449](https://github.com/gavr123456789/Niva/issues/449)
   ([draft](005-go-list-literal-of-branch.md)), with draft PR
   [#450](https://github.com/gavr123456789/Niva/pull/450) for its
   option 1, merged into `self-host` 2026-10-25 as `ab69e59b`. A list
   with nothing expected of it, `l = {(Sym name: "x")}` passed on as a
   `List(Expr)`, still doesn't build on Go.
6. **Go:** `^ (…) any: […], not` generates an empty `return` before the
   real one, so the Go build fails. Works at `3746ffb7`, likely since
   #437, and still at `4e809716`.
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
    Doesn't reproduce at `a56f659c` or `4e809716`: a parse error in a
    file other than the entry file names it, `other.niva:2:19`.
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
26. `String` has no `<` or other ordering, on either backend, and
    `Char` has only `code`: `"x" < "y"` gives "Searching for < method
    but its not found for type Type(String)". MPL's canonical order of
    symbols needs one. The Kotlin compiler has none either; it can bind
    Kotlin's `compareTo` ([series 000](../design/implementation/000-expression-tree/README.md)).
    Found re-checking at `a56f659c`, as are 27 to 29. Filed as
    [#451](https://github.com/gavr123456789/Niva/issues/451)
    ([draft](006-string-ordering.md)), with draft PR
    [#452](https://github.com/gavr123456789/Niva/pull/452) for its
    option 1, merged into `self-host` 2026-10-25 as `e823f7fe`: strings
    have `<`, `>`, `<=` and `>=`. `Char` still has none.
27. A type name of one capital letter is read as a type parameter, and
    the error doesn't say so: `type P n: Int`, then `p = P n: 1`, gives
    "Missing constructor table for Type(P)", and `union R = | Ok t: Int
    | Fail`, then `r::R = Ok t: 4`, gives "Type declared for r is not
    equal for it's value type Type(R) != Type(Ok t: Type(Int))". With
    `Pt` and `Res` both work.
28. `Bind` in a program's own file works only when `niva.bindings`
    lists it with a module id ending in `.bind`, as in
    `go|binds.str.bind|binds/str.niva`. With another id, or no entry,
    the Go build fails at `core.Bind__imports__emit(, …)`, "syntax
    error: unexpected comma", and Clojure at an undefined
    `niva.core/Bind__imports__emit`. No error says what's missing.
29. After a type error and after a Go build error, `a56f659c` also
    printed a Java exception from mapping its own stack trace, "Cant
    find niva line above 292 in file main/kotlin/Main.kt" (005's
    programs). Not seen at `4e809716`, in any of the re-check's runs
    that fail. **Not a Niva bug:** it's our setup, found drafting 006.
    NIN maps its stack traces from the Kotlin project stage 2 leaves in
    the clone's `local/home`, and running the JVM compiler on a test
    program with that home replaces it. Then every NIN error also prints
    this, and exits 1. The re-check ran right after a rebuild, so it was
    clean. See [The self-hosted compiler](../self-hosted-compiler.md).
30. `inject:into:` on a list passes the type checker but doesn't run on
    either backend: `{1 2 3} inject: 0 into: [acc, x -> acc + x]` stops
    at `undefined: core.List__inject__into` on Go and `Unable to resolve
    symbol: niva.core/List__inject__into` on Clojure. Both runtimes
    define it as `List__injectInto`, where every other two-part keyword
    is `Type__first__second`, as `List__joinWith__transform` is. The
    Kotlin compiler prints 6. Found at `e823f7fe`, probing the list
    operations a simplifier would use, as are 31 and 32. Opened as
    [#454](https://github.com/gavr123456789/Niva/pull/454), a PR without
    an issue ([draft](007-inject-into-pr.md)), and merged into
    `self-host` 2026-10-25 as `2ad4f629`.
31. `reversed` and `sum` on a list aren't in the type checker. The
    Kotlin compiler has both: `{1 2 3} reversed` and `{1 2 3} sum`.
32. **Clojure:** `toSet` on a list passes the type checker but isn't in
    the runtime: `Unable to resolve symbol: niva.core/List__toSet`. Go
    has it.
33. **Clojure:** `nin run` writes the runtime, `.niva_clj/niva/core.clj`,
    only when it's missing, where Go's `.niva_go/build/core/core.go` is
    rewritten on every run. So a project built before keeps the old
    Clojure runtime after a newer compiler: a line added to `core.clj`
    survives a run, one added to `core.go` doesn't. Found drafting 007,
    when a stock run in a folder the branch had built printed the
    branch's results.
34. `inject:into:`'s total has to be the items' type: `{"a" "bb"}
    inject: 0 into: [acc, s -> acc + s count]` gives "wrong arg type for
    inject expected Type(String) but got Type(Int)". The protocol types
    both the start and the block's accumulator as the item type, though
    the Go runtime's `List__inject__into[T any, R any]` takes any. The
    Kotlin compiler prints 3. Workaround: `map:` first. Found at
    `2ad4f629`, after #454. The Kotlin compiler types the start, the
    accumulator and the result with a second type parameter
    (`differentGenericType`) and compiles it to Kotlin's `fold`. Spiked
    at `781c5b05` by doing the same with NIN's `g`: the type checker
    then takes a fold into another type and still rejects a wrong one,
    but the call is named `core.List__inject__into__core_Int[string,
    int]`, which neither runtime has. A generic method's specialized
    name gets the concrete type of each type parameter that isn't the
    receiver's (`replaceGenericsFromTable: … dontMangle: receiverNames`
    in `typeDB.niva`); `sortedBy:` and `map:` escape it only because
    their `g` is inside a block. So a fix needs a rule for naming
    built-in generic methods too, which is Gavr's call.
35. A keyword message named `try:` can be declared but not sent:
    `Int try(x): Int -> Int = this + x`, then `(3 try: 4) echo`, gives
    "keyword try at main.niva:2:2 on Type(Int) expects 0 arguments but
    got 1". NIN looks up any message named `try` as the error-handling
    `try` (`findTryMethod:byName:` in `typeDB.niva`), whatever its kind,
    and the declaration alone passes without a word. The Kotlin compiler
    takes the send but generates `fun Int.try(…)`, which Kotlin rejects,
    `try` being a Kotlin keyword. Found at `781c5b05` naming a scratch
    helper while building simplifier 002; MPL doesn't use the name.
36. A chain of binary messages is typed on each one's argument, not on
    the message before it: in `a = 1 < 2 && true`, `&&` is looked up on
    `2`, so both backends stop at "Searching for && method but its not
    found for type Type(Int)". The Kotlin compiler prints `true`. Where
    the argument's type has the method, the chain passes with the wrong
    type: `x = 2 < 3 + 1` types `x` as an `Int`, and Go stops later, at
    "unknown Go IR func id: main.core_Bool.+", where the Kotlin compiler
    says "Can't find binary message + for type Bool". A chain of one
    type, `x + y + z` or `a && b && c`, doesn't show it. It's
    `replaceArgsWithTypedfromDb2:withContext:` in
    `front/resolver/messages/binary.niva`: after each message,
    `lastReceiverTypedExpr <- typedArg expr` makes the argument the next
    receiver. Workaround: parentheses, `(1 < 2) && true`. Found at
    `781c5b05` writing simplifier 006's `simplifiedPower:`, whose
    `this == 0 num && w isNum` looked for `&&` on an `Expr`.
37. **Clojure, REPL mode:** `--repl` puts every package in one
    namespace, in package order, with each package's records in its own
    part. So a package's records come after the functions of earlier
    packages that use them: MPL's `main.clj` names `mpl.tree_Num` 455
    lines before it's defined, and `bb` stops at "Unable to resolve
    symbol: mpl.tree_Num". A program in one file works. Found at
    `781c5b05` trying a Clojure REPL for MPL
    ([series 010](../design/implementation/010-repl/README.md)), as is
    38.
38. **Clojure, REPL mode:** a module built with `--repl-line=N` for a
    live session defines its records again, which makes new classes, so
    the session's values no longer match any branch: after the full
    module, a line using its `x` stops at "no matching type". Without
    the records the line runs. It's what an editor's evaluate-line
    would hit.

Gaps rather than bugs: `sortedWith:`, which the Kotlin compiler has,
was missing from NIN until
[#455](https://github.com/gavr123456789/Niva/pull/455)
([draft](008-sorted-with-pr.md)), merged into `self-host` 2026-10-25 as
`781c5b05`. Neither has `all:`. Neither
compiler has `flatMap:`, `flatten`, `groupBy:`, `max` or `zip:`
(probed at `e823f7fe`).

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
