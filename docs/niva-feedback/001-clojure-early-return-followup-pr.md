# 001 Clojure: an early `^` is silently dropped: follow-up pull request

**Status:** Opened 2026-10-23 as [#434](https://github.com/gavr123456789/Niva/pull/434), with a reply linking it in his line 557 thread on #433, and merged into `self-host` the same morning, squashed as `b00ec027`.
**Follows:** [#433](https://github.com/gavr123456789/Niva/pull/433) ([draft](001-clojure-early-return-pr.md)), merged 2026-10-22
**Branch:** [`dharmatech/Niva:clj-key-sugar`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:clj-key-sugar),
one commit, `559e31d9`, on `self-host` at `6733f5c5`. Locally it's
`clj-key-sugar` in `~/src/Niva-self-host-go-value`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

In his review of #433, Gavr asked for the `:key` shorthand at line 557
(`toCljBlockBodyWithBind: ctx scope: scope bindName: bindName bindValue:
bindValue`). nin couldn't parse it yet, so that was left out, and we
said so in the thread. Overnight he added it ("add colon key sugar") and
moved the compiler to it ("use colon sugar everywhere"), but that pass
left this call, split over two lines, and the `resultType: resultType`
in `cljSplitAt:`. This finishes his comment.

How it was checked:

- `niva test`: 357 pass. Gradle didn't rerun the tests at `6733f5c5`
  for a baseline, because the shorthand compiles to the same Kotlin.
- `niva build` works, and `java -jar main.jar build --backend=go` builds
  the compiler for Go, `:resultType` on the block's own field included.
- With that compiler, the four methods from #432 are still reported on
  Clojure, and a `^` in front of a switch still runs.
- Upstream was checked first: every branch on GitHub matched the local
  refs, and neither `self-host`, another branch, nor an open PR had the
  change.

Everything below the line is the pull request as opened.

---

**Title:** [clj] use the `:key` shorthand in the early return checks (follow-up to #433)

Follow-up to your review of #433, now that nin reads `:key`: the two
calls in the early return checks that send arguments named like their
keys use it too.

- `toCljBlockBodyWithBind: ctx :scope :bindName :bindValue`, the one
  you commented on. It was split over two lines, so the move to `:key`
  left it.
- `result: split expr :resultType` in `cljSplitAt:`.

`niva build` and `java -jar main.jar build --backend=go` both work, and
`niva test` passes (357).
