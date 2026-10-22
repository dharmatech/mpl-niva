# 001 Clojure: an early `^` is silently dropped: pull request

**Status:** Opened 2026-10-22 as [#433](https://github.com/gavr123456789/Niva/pull/433).
**Issue:** [#432](https://github.com/gavr123456789/Niva/issues/432) ([draft](001-clojure-early-return.md))
**Branch:** [`dharmatech/Niva:fix-432-clj-early-return`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:fix-432-clj-early-return),
two commits, `0f918fdf` (C) and `a24a40f2` (A), on `self-host` at
`6af8f9f6`. Locally it's `clj-early-return` in
`~/src/Niva-self-host-clj-return`, a worktree of the self-host clone.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

Opened right after the issue, as an option rather than the answer: the
first commit alone fixes the silent part, and the second is the fix
proper. Gavr can take both, take the first, or close it.

Opened minutes after the issue. Upstream `self-host` was still at
`6af8f9f6`, so no rebase was needed.

How it was checked, beyond what the PR says:

- With the patched compiler, Clojure gives the same output as Go for
  the four methods from the issue, and for more programs: a nested
  `ifTrue:`, a match without `|=>`, a match on type, a `mut` after the
  return, several guards in a row, a branch with side effects, a branch
  that rebinds a name, and a `Unit` method that returns early.
- The two new tests that run code fail with the emitter reverted.
- The first commit alone builds, its tests pass (62), and it reports
  all four methods from the issue.
- The full suite failed once at baseline, on
  `babashkaHostLoadsRecoversAndWatchesSources`. It passed on a second
  baseline run and on every run with the change, so it's flaky.
- `~/src/Niva-self-host` and its pinned jar, which this project uses,
  were left alone.

Everything below the line is the pull request as opened.

---

**Title:** [clj] report or move early returns instead of dropping them (#432)

Fixes #432.

Two commits. Take both, or only the first if you'd rather fix the rest
another way.

1. **Report a `^` that isn't in tail position.** Each one is a compile
   error with its position, instead of becoming its branch's value.
   They're collected in `CljContext` while emitting, and reported
   together with `TO DO:` once the package is done. A `^` followed by
   more statements now ends its block instead of stopping the build.
2. **Move the rest of a block into the branches that don't return.**
   When an `ifTrue:`, `ifFalse:`, `ifTrue:ifFalse:` or `match`
   statement can return, the statements after it go into its branches
   that carry on, so each `^` is in tail position:

   ```clojure
   (defn main__core_Int__viaIfTrue [this]
     (let [__cljSubject (> this 0)]
       (cond
         (= __cljSubject true) "positive"
         :else "not positive")))
   ```

   When more than one branch carries on, or the one that does binds
   names, the rest becomes a local fn, named after the statement's
   position, which those branches call. It isn't copied, and it doesn't
   see names a branch binds:

   ```clojure
   (let [x            1
         __cljRest3_3 (fn [] x)]
     (let [__cljSubject (> this 0)]
       (cond
         (= __cljSubject true) (let [x 2]
           (let [__cljSubject (> this 10)]
             (cond
               (= __cljSubject true) x
               :else (__cljRest3_3))))
         :else (__cljRest3_3))))
   ```

After both, these are still reported, because a `^` there can't be
moved this way:

- in a loop (`forEach:`, `forEachIndexed:`, `whileTrue:`);
- inside a value, such as `x = c ifTrue: [^ 1] ifFalse: [2]`;
- in a match on errors used as a statement: the rest would end up
  inside its `try`.

One difference from Go: on Go, a `^` inside a value compiles but only
leaves the branch. With

```Scala
Int inValue -> String = [
  s = this > 0 ifTrue: [^ "returned"] ifFalse: ["kept"]
  s + "!"
]
```

`5 inValue` gives `returned!`. Clojure did the same before this PR, and
now reports it. If leaving the branch is what `^` should mean there, the
check in the first commit can skip values.

Tests:

- `niva test cljBackTests`: the 61 existing tests and 3 new ones pass.
  `cljEarlyReturnFromBranches` runs the four methods from #432, and
  `cljEarlyReturnRestFn` runs the local fn cases: two branches carrying
  on, a match on type, and a branch that rebinds a name. Both run the
  generated code with `clojure`, as
  `cljCompilerCliArgsFollowMainArguments` does, and fail without the
  change. `cljEarlyReturnInLoopOrValueIsReported` checks what's still
  reported.
- `niva test`: all 343 pass.
