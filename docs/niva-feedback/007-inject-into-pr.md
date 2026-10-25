# 007 `inject:into:` has the wrong name in both runtimes: pull request

**Status:** Opened 2026-10-25 as
[#454](https://github.com/gavr123456789/Niva/pull/454), not as a draft.
A PR without an issue, as [003](003-go-statement-match-fixes-pr.md)
(#436) was: it's a plain bug with one obvious fix, so there's no
decision to ask Gavr for. No CI checks. Each paragraph went in on one
line.
**Branch:** [`dharmatech/Niva:inject-into`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:inject-into),
one commit on `self-host` at `e823f7fe`: `a210b4df`. Locally in
`~/src/Niva-self-host-inject-into`, a worktree of `~/src/Niva-self-host`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

It's item 30 in the [README](README.md), found at `e823f7fe` while
probing 23 list operations a simplifier would use, on NIN's two
backends and on the JVM compiler. Picked as next after #452: the
compiler accepts code its runtimes can't run, the fix is a rename, and
MPL will want a fold for combining numbers.

The keyword message `inject:into:` compiles to a call to
`List__inject__into`, as every two-part keyword does
(`List__joinWith__transform`, `String__replace__with`,
`Char__toInt__base`). Both runtimes define it as `List__injectInto`,
so the fix is in the runtimes, not the call: `compiler/goRuntime.niva`
and `compiler/compilerClojureConstants.niva`, one line each. The other
`"injectInto"`, in `front/resolver/protocols/collection.niva`, names the
block's type, as `"sortedBy"` does next to it, and stays.

**Tests.** A codegen test alone can't catch this, since the call was
always right; the runtime was wrong. So each test checks both sides:
the generated call, with `Assert nivaToGo: … containsAll:` on Go, and
that the runtime's source defines that function, as
`goRuntimeRepresentationIsStable` checks `GoRuntimeSources coreSource`.
On Clojure, `CompilerConstants nivaCoreSource`.

## What was checked

On 2026-10-25, each from an exported copy, with stage 1 at `e823f7fe`
and a copy of its home:

- `niva test`: 391 pass on the branch, against 389 on stock
  `e823f7fe`, so the 2 new tests pass and nothing else changed. The one
  failing on both is `babashkaHostLoadsRecoversAndWatchesSources`, as
  for 005 and 006.
- The 2 new tests fail without the change: with only the test files
  added to stock, 389 pass and the 2 fail.
- `{1 2 3} inject: 0 into: [acc, x -> acc + x]`, the same on an empty
  `List(Int)` from 10, and on strings from `""`: `6`, `10` and `abc` on
  Go and Clojure with the branch. Stock stops at the undefined call on
  both, in a fresh folder (see README item 33 for why a reused one can
  hide it on Clojure).
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and stock sources compiled with the branch's jar give the same
  Go as with the stock jar but for the `//line` paths and the renamed
  function in `core.go`.
- mpl-niva's 31 checks pass on Go with the branch.

Everything below the line is the pull request as opened.

---

**Title:** [go, clj] name inject:into:'s runtime function List__inject__into, as it's called

`inject:into:` passes the type checker but doesn't run on either
backend:

```Scala
({1 2 3} inject: 0 into: [acc, x -> acc + x]) echo
```

```
main.niva:1: undefined: core.List__inject__into
```

on Go, and `Unable to resolve symbol: niva.core/List__inject__into` on
Clojure. The JVM compiler prints 6.

The call is named like every other two-part keyword,
`List__joinWith__transform` or `String__replace__with`, but both
runtimes define it as `List__injectInto`. This renames the two runtime
functions. Nothing in the compiler calls them, so nothing else changes.

Two tests, one per backend, each failing without the change: the call
`inject:into:` compiles to, and that the runtime's source defines it.

`niva test`: 391 pass, 2 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work.
