# 002 Go: a `^` inside a value doesn't return from the method: pull request, if it's forbidden

**Status:** Drafted, not opened. Only if Gavr would rather make a `^`
inside a value an error; the issue suggests [the other
option](002-go-return-inside-value-pr.md).
**Issue:** [#435](https://github.com/gavr123456789/Niva/issues/435) ([draft](002-go-return-inside-value.md))
**Branch:** local `go-value-return-forbid` in
`~/src/Niva-self-host-go-value`, to push as
`dharmatech/Niva:fix-435-forbid-return-inside-value`, one commit,
`cdec8f2a`, on `self-host` at `b00ec027`
**Base:** `gavr123456789/Niva` `self-host`, not `main`

It rejects 34 `^` in the compiler's own sources, so the compiler can't
compile itself until they're rewritten. If Gavr picks this, ask whether
he wants those rewrites in the PR or would rather do them himself. They
aren't done here.

How it was checked, beyond what the PR says:

- First written on `6af8f9f6`, then rebased on `ad439a32` and
  `b00ec027` without conflicts, using `:key` for its `LazyError :tok`.
  On `b00ec027` one test from #433, merged since, had to change, as the
  PR says.
- mpl-niva's demo and its 31 checks give the same output: mpl-niva has
  no `^` inside a value.
- The typing commit from the other option isn't included: without that
  option's statements, a tail match like `| Num => ^ this |=> Sym name:
  "x"` would get the type `Sym` and stop building on Go.

Everything below the line is the pull request as it would be opened.

---

**Title:** [types] report a `^` inside a value (#435)

Fixes #435 by making it an error.

A `^` can be a statement, end a branch of a match or an inline block
used as a statement, or be in the method's last expression or in what
another `^` returns. Anywhere else, in a value that's bound, passed or
computed with, it's now an error at the `^`, for every backend:

```
main.niva:2:24 ^ can't be used inside a value, only as a statement or in the method's last expression
```

So `^ | true | mergeFailed => null |=> merged` and `this > 0 ifTrue:
[^ 1]` are fine, and `s = this > 0 ifTrue: [^ "returned"] ifFalse:
["kept"]` isn't. It's a walker over the typed body, next to
`collectNonLocalReturnTypesInto`, run in `resolveBodyWithDb`. That's
what #433's first commit does for Clojure, in the type checker instead
of a backend.

The compiler's own sources have 34 of these, which this doesn't
rewrite, so it can't compile itself until they are: 10 in
`resolveExpr2.niva`, 8 in `lex.niva`, 7 in `messageDeclParse.niva`, 6
in `typeDB.niva` and 3 in `astToType.niva`.

Tests: `niva test`, the 357 existing tests and 2 new ones pass.
`returnInsideValueIsRejected` fails without the change, and
`returnAsStatementOrInLastExpressionIsAllowed` checks that a `^` as a
statement, in a branch, in a loop and at the end still type checks.
#433's `cljEarlyReturnInLoopOrValueIsReported` had a `^` inside a value,
which is now a type error before the Clojure backend sees it, so that
case is dropped and the test is `cljEarlyReturnInLoopOrBranchIsReported`.
