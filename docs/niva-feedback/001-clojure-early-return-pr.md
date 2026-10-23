# 001 Clojure: an early `^` is silently dropped: pull request

**Status:** Merged 2026-10-22 into `self-host` (`2216b570`), as
[#433](https://github.com/gavr123456789/Niva/pull/433). Opened with two
commits, cut down to the first as agreed with Gavr on Discord, plus one
from his review.
**Issue:** [#432](https://github.com/gavr123456789/Niva/issues/432) ([draft](001-clojure-early-return.md))
**Branch:** [`dharmatech/Niva:fix-432-clj-early-return`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:fix-432-clj-early-return),
opened with `0f918fdf` (report) and `a24a40f2` (move the rest of the
block), on `self-host` at `6af8f9f6`, then force-pushed back to
`0f918fdf` alone, and `06731524` added after review. Locally the first
two are on `clj-early-return` in
`~/src/Niva-self-host-clj-return`, a worktree of the self-host clone.
**Base:** `gavr123456789/Niva` `self-host`, not `main`
**Follow-up:** [#434](https://github.com/gavr123456789/Niva/pull/434) ([draft](001-clojure-early-return-followup-pr.md)), the `:key` shorthand from his review

Opened minutes after the issue, as an option rather than the answer:
the first commit alone fixes the silent part, and the second moves the
rest of a block into the branches. The description as opened, for both
commits, is in this file's history (`1c5e7c3`).

On Discord, Gavr first answered #432 with "for now thats intended,
there is no return in Clojure, and I dont know how to implement it". We
replied with three options: just forbid it (the first commit), forbid
and rewrite simple guards (both), or something else, adding that the
issue's `this > 0 ifTrue: [^ "positive"]` compiles on clj, the default
backend, and silently gives the wrong answer, "So I think it needs at
least an error." He answered "yapp, 100%". So the PR is cut down to the
error, which keeps it small to review, and the guard rewrite is offered
as a separate PR if he wants it.

Gavr reviewed it the same day and asked for two refactorings: the
`ifNotNull:` helper from `libs/maybe`, and the `:key` shorthand for an
argument named like its key. Neither built with the self-hosted
compiler yet: its parser doesn't read `:key` (he plans that next), and
on Go `ifNotNull:` on an `IrExpr?` fails with `cannot infer T`, since a
nullable union is an interface there, not a pointer ("maybe thats a new
case"). He said to push only what keeps `niva build` and
`java -jar main.jar build --backend=go` working, so `06731524` uses
`ifNotNull:` for the one `IrBlock?`. Both changes in full are on
`clj-433-review` in `~/src/Niva-self-host-go-value`, for when nin
handles them.

Gavr merged it that morning ("thanks, merged!"), adding that in nin the
last expression of a body is its value, as in code blocks, so on the
Clojure backend you don't need `^` anywhere. The merge into `self-host`
didn't close #432, since `self-host` isn't the default branch.

How it was checked, beyond what the PR says:

- With `06731524` too, merged into `ad439a32`: 355 tests pass,
  `niva build` and `java -jar main.jar build --backend=go` work, and
  the four methods from #432 are still reported.
- The first commit alone, merged into `self-host` at `ad439a32`
  (2026-10-22), builds and passes all 355 tests: the 354 there and its
  own. #433 still merges cleanly, so no rebase is needed.
- With that compiler, the four methods from #432 are each reported with
  their position, where `ad439a32` prints `not positive`, `positive`,
  `unreachable`, `unreachable`. A `^` in front of a switch,
  `^ | true | mergeFailed => "failed" | hasNullable => "nullable" |=>
  "merged"`, still runs on Clojure, as does a switch at the end of a
  method with a `^` in a branch.
- With both commits, when opened: Clojure gave the same output as Go
  for the four methods from the issue and for more programs, the two
  tests that run code failed with the emitter reverted, and the full
  suite passed (343), apart from
  `babashkaHostLoadsRecoversAndWatchesSources`, which failed once at
  baseline and is flaky.
- `~/src/Niva-self-host` and its pinned jar, which this project uses,
  were left alone.

Everything below the line is the pull request as edited on 2026-10-22.
`gh pr edit` failed on GitHub's retired Projects (classic) API, so the
title and description were set with `gh api -X PATCH
repos/gavr123456789/Niva/pulls/433`.

---

**Title:** [clj] report a `^` that isn't in tail position instead of dropping it (#432)

Fixes #432.

Clojure has no return, so a `^` only works at the end of a method, or
at the end of a branch that ends the method. Anywhere else it became
its branch's value and the method carried on, so

```Scala
Int viaIfTrue -> String = [
  this > 0 ifTrue: [^ "positive"]
  "not positive"
]
```

compiled and gave `not positive` for 5. Now every `^` that isn't in
tail position is a compile error with its position:

```
main.niva:2:20: the Clojure backend can't return from here yet. `^` only works at the end of a method, or at the end of a branch that ends the method
```

They're collected in `CljContext` while emitting, and reported together
with `TO DO:` once the package is done. A `^` followed by more
statements now ends its block instead of stopping the build. A `^` at
the end still works, including in front of a switch,
`^ | true | x => … |=> …`.

As agreed on Discord, this is only the error. I also have the rewrite
that moves the code after `x ifTrue: [^ y]` into the other branch, so
those guards work on Clojure too. I can open it as its own PR if you
ever want it.

Tests:

- `niva test cljBackTests`: the existing tests and a new one pass.
  `cljEarlyReturnInLoopOrValueIsReported` checks that a `^` in a loop,
  inside a value, and in a guard followed by more code is reported.
- `niva test`: all 355 pass, merged into `self-host` at `ad439a32`.
