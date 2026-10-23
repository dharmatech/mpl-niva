# 002 Go: a `^` inside a value doesn't return from the method: Gavr's design, as a draft pull request

**Status:** Opened 2026-10-23 as draft PR
[#437](https://github.com/gavr123456789/Niva/pull/437), after
[#436](https://github.com/gavr123456789/Niva/pull/436)
([draft](003-go-statement-match-fixes-pr.md)), which it builds on, was
merged. Its "Fixes #435" links it from the issue. Meant as something
concrete to discuss the design around, whether or not he takes it.
**Issue:** [#435](https://github.com/gavr123456789/Niva/issues/435) ([draft](002-go-return-inside-value.md))
**Branch:** [`dharmatech/Niva:fix-435-go-match-temp`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:fix-435-go-match-temp),
two commits on `self-host` at `cf25e6af` (#436, squashed): `5d30f8cc`
and `cb5f33db`. Locally it's `go-match-temp` in
`~/src/Niva-self-host-go-value`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

After #435 was filed, Gavr proposed his own fix on Discord: replace
each match expression with a temp variable assigned in each branch,
instead of "lambda that calls itself", and do it for every match,
"since lambdas is overhead". This sketches that, as a draft PR he
doesn't have to take.

It builds on option A's branch ([draft](002-go-return-inside-value-pr.md)).
A's first commit went to #436, with two more statement-form fixes the
sketch needed. A's third commit is kept as it is, and its
second is reworked. A already emitted a bound or returned match as statements;
the sketch does it for every match, lifts one out of any other
expression into a temp, and drops A's `validateGoReturns` check, which
reported the positions it couldn't handle. Option B
([draft](002-go-return-inside-value-pr-forbid.md)) stays a fallback.

How it was checked, beyond what the PR says:

- On top of #436's commits, each prefix built and passed: 358, 359,
  360, 368 and 370 tests, against 357 at `b00ec027`. #436 was merged
  squashed, with the same tree as our branch, so after the rebase the
  two commits have the same trees as before and the results stand:
  368 and 370. At the tip, `niva build` and
  `java -jar main.jar build --backend=go` were run again and work. Only
  the `[go]` commit's message changed, to say "#436's fixes" instead of
  "the three commits before this one".
- The first version had #436's fixes inside its `[go]` commit; splitting
  them out changed only a comment, a variable name, and #436's two new
  tests.
- `niva build` works, and `java -jar main.jar build --backend=go` on a
  copy of `Niva/NivaInNiva` builds `.niva_go/build/niva_go`. That Go has
  22 multi-line anonymous functions where option A's has 980 (the rest
  are blocks and lambdas), and 34,980 lines instead of 38,023.
- The Go-built compiler builds the compiler again, but as with option A
  its output isn't the JVM jar's: which receiver fields get aliased
  (and so read as `x`, `this.x` or `nivaField__x()`) depends on map
  order, and four `orElse` instances are missing. Leaving out the
  aliases, the only difference left is those four functions, for option
  A's Go-built compiler too.
- The handoff's 41 programs on Go give option A's output, except where A
  failed: the cases A reported (`asArg`, `reportReturnOperand`,
  `reportReturnOperand2`, `reportSubject`) now run, `^ (this > 0
  ifTrue: [^ 1] ifFalse: [2]) + 10` gives 1, `switch` builds, and
  `ifTrue:` alone as a value (README item 16) runs. `inl` still fails
  in the type checker (`^=` isn't done).
- mpl-niva's 31 checks pass with this compiler.
- Lifting blocks too was tried first: `goUserInlineMethodSplicesCallbackReturn`
  showed that a `^` in a `^=` method's block gives the block's value
  today (`1 run echo` prints 42), and lifting would make it return from
  the caller. So blocks stay functions; that's the `^=` question.
- `~/src/Niva-self-host` and its pinned jar were left alone.

Everything below the line is the pull request as opened.

---

**Title:** [go] a match used as a value becomes a variable assigned in each branch (#435)

A sketch of your design from Discord for #435, as a draft: you don't
have to take it. Fixes #435.

It uses the statement form of a match for every match, with the fixes
from #436.

**Where to start reading:** in `goBack.niva`, the comment "a match whose
value is used" (`goLifted:` and `goLiftedOperands:`), then where
`toGoStmt`, `emitAsStmt` and `emitResult` call them.

Every match whose value is used is now emitted as statements. Bound
(`=`, `<-`) or returned, each branch assigns or returns its value.
Anywhere else in an expression, the match is lifted out first into a
temp:

```Scala
x = (this > 0 ifTrue: [^ 2] ifFalse: [2]) + 2
```

```go
var __matchValue int
if (this > 0) {
    return 2
} else {
    __matchValue = 2
}
var x int = (__matchValue + 2)
```

No match becomes an anonymous function anymore, so `matchToGoExpr` and
`goMatch*Into` are gone. The compiler built for Go has 22 multi-line
anonymous functions instead of 980, and 8% less Go.

The two things to be careful about:

- **Evaluation order:** an operand before the match is kept in a temp
  first (`var __operand int = …g(this)`), unless it's a literal, a
  lambda or a local that isn't `mut`. An argument a call writes through,
  like the list of `add:`, isn't copied.
- **`&&` / `||`:** with a match on the right, `l && r` becomes
  `| l | true => r |=> false` (and `||` the other way around), so the
  right side still runs only when needed.

A match in a loop condition is lifted inside the `for`, so it runs on
every iteration.

A block used as a value, like the body of a `^=` method, stays a
function: a `^` in it gives the block's value today (`1 run echo`
prints 42 in `goUserInlineMethodSplicesCallbackReturn`), and lifting it
would make that `^` return from the caller instead.

Two commits, each building and passing the tests:

1. **The change above.** It also makes `ifTrue:` alone as a value build,
   since a match without a value now runs its branches as statements.
2. **A match branch that ends with `^` doesn't decide the match's
   type**, so `s = | this | 0 => ^ 1 |=> "other"` is a String.

`niva test`: 370 pass, 10 of them new here. `goNestedMatchCodegen` and
`goMatchVariableSubjectNarrowsAlias` checked the anonymous function, so
they now check the assignments. `niva build` and
`java -jar main.jar build --backend=go` work, and the programs from
#435 and about 40 others give the right output on Go.
