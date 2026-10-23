# 000 negative literal as a unary receiver: pull request

**Status:** Opened 2026-10-22 as [#431](https://github.com/gavr123456789/Niva/pull/431), merged the same day into `main` (`d32d442e`). It's on `self-host` too, as `ad439a32`.
**Issue:** [#430](https://github.com/gavr123456789/Niva/issues/430) ([draft](000-negative-literal-unary-receiver.md))
**Branch:** [`dharmatech/Niva:fix-430-negative-literal-receiver`](https://github.com/gavr123456789/Niva/compare/main...dharmatech:Niva:fix-430-negative-literal-receiver),
one commit (`7c74a5d8`) on `main` at `ece36eb7`
**Base:** `gavr123456789/Niva` `main`

Gavr answered #430 the same day: the self-hosted version doesn't have
the bug. The Kotlin compiler is still what `niva` installs, so the fix
is offered as a small PR he can merge or close.

How it was checked, beyond what the PR says:

- The new test fails at its first assertion with the fix reverted.
- Our installed `niva` (`~/.niva`) was left alone: the patched compiler
  ran from the clone's `build/install`, and the installed files'
  checksums were the same afterwards.
- With the patched compiler, `-1 num` works in this project, and
  boids' tests pass, plus a throwaway test that `-1 inc` is `0`.

Everything below the line is the pull request as opened.

---

**Title:** [codegen] wrap a negative number receiver of a unary message in brackets (#430)

Fixes #430.

`generateSingleUnary` now puts a negative number literal receiver in
brackets, as `generateSingleKeyword` already does, so `-1 inc` becomes
`(-1).inc()` instead of `-1.inc()`, which Kotlin reads as `-(1.inc())`.
Only `Int`, `Float` and `Double` literals that start with `-` are
wrapped; everything else is generated as before.

The new test, `CodogenTest.negativeNumberReceiverOfUnaryIsWrappedInBrackets`,
covers a chain (`-1 inc inc`), a `Double`, and a unary inside a binary
(`-1 inc + 1`), and checks that `2 inc` and `x inc` are unchanged. It
fails without the fix.

Tested:

- `./gradlew test`: the new test passes. The 11 tests that already fail
  on `main` (5 in `FailingResolverTests`, 5 in
  `LanguageServerIncrementalTest`, and `ResolverTest.dontAddThisToTheCostructor`)
  fail the same way with and without this change.
- With the patched compiler (`./gradlew installDist`), the program from
  #430 prints the expected values, and two small Niva projects still pass
  their `niva test`.
