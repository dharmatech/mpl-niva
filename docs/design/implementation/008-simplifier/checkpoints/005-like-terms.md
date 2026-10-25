# simplifier 005: like terms

**Status:** Issued (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

Terms that differ only in their number combine: `x + x` is `2 * x`,
and `2 * x + 3 * x` is `5 * x`. Before that, the pair rules become flat
lists of cases, since this increment and 007 each add one. Stop when
the checks pass and both commits are in.

## Read first

- [The spec](../spec.md): the decisions, and §005, which this
  checkpoint carries out.
- [Niva notes](../../../../niva-notes.md), before writing any Niva.
- `mpl/simplify.niva`: the frame and the pair rules, from 002 to 004.
- `~/src/mpl/order-relation.sls` for `term` and `const`, and
  `~/src/mpl/sum-product-power.sls` for the like-terms case of
  `simplify-sum-rec`.
- `tests/simplifyChecks.niva`, `main.niva`, and `docs/simplifying.md`.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 95 checks. The compiler is built and pinned at `781c5b05`.

## Build

### First commit: the pair rules as flat matches

`sumPair:` and `productPair:` are nested `ifTrue:ifFalse:` chains, one
level deeper per case. Rewrite each as a match on `true`, one arm a
case, the way Gavr writes such chains in the compiler. The manager
tried this form on Go at `781c5b05`; it takes the first arm whose
condition holds:

```Scala
on pair(other): Ex -> List(Ex) = | true
| this isNm && other isNm => {(Nm value: 9)}
| this == (Nm value: 0) => {other}
| other == (Nm value: 0) => {this}
|=> {this other}
```

The last case, the order, may need parentheses as an arm, or can be
the `|=>` arm's `ifTrue:ifFalse:`. Nothing else changes, and all 95
checks pass. Commit it on its own, so the review sees the rewrite apart
from the rule.

### Second commit: like terms

**`mpl/simplify.niva`:**

- **Cohen's term and constant,** for an operand that isn't a number:
  - `Expr termFactors -> List(Expr)`: a product whose first factor is
    a number gives its other factors; any other product, its factors;
    anything else, a list of itself.
  - `Expr constant -> Int`: a product whose first factor is a number
    gives that number's value; anything else, 1.

  Numbers never reach these: the pair rule deals with them first.
- **A case in `sumPair:`,** after the numbers and the `0`s and before
  the order: two operands, neither a number, whose `termFactors` are
  `==`, give the simplified product of their constants' sum and the
  term, `{c num} + termFactors` through `simplifiedProduct`, as a list
  of one, or none if it's `0`. 004's `listUnless:` does that last part.
  004's rules then make a constant of 1 vanish and a constant of 0 make
  `0`.
- `productPair:` doesn't change.
- The comments, the pair rule's and the file's, say what holds now.

**`tests/simplifyChecks.niva`:** the spec's §005 table, as `got: (e
simplify) text want:` checks named like `"like terms: x + x"`, in a
group after the number checks. In Niva, `2 * x + 3 * x` is
`(2 num * x) + (3 num * x)`, and `x * y + y * x` is
`(x * y) + (y * x)`. Also:

- `x + 2 * x` is `3 * x`: a constant of 1 on one side.
- `(x + 1) + (x + -1)` is `2 * x`: the numbers cancel, then the terms
  combine.
- `Sum terms: {x y x y x}` is `3 * x + 2 * y`.
- `2 * (x * y) + x * y` is `3 * x * y`: a term of two factors.
- Unlike terms stay apart: `x + x * y` and `2 * x + 2 * y` are as
  written.

Write each input so that its result is final: no two factors with the
same base inside a term, so that 006 and 007 leave it alone. The checks
from 002 to 004 must still pass unchanged.

**The demo:** an example of like terms, such as `x + y + x`, which is
`2 * x + y`.

## Docs

- **`docs/simplifying.md`:** like terms join the rules, with a sentence
  on term and constant; "`x + x` stays as it is" goes.
- **`README.md`:** the status line says what simplifying does now.
- **The series [README](../README.md):** mark 005 done in its
  increments table. Leave the commit cell for the manager.
- **Niva findings** go in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 95 after the
  first commit, and 95 plus yours after the second.
- `tools/nin run --backend=go` shows the new example.
- Two commits on `main`, in the style of `git log`: a subject line,
  then a wrapped paragraph saying what and why. Don't push.

## Report

When done, say in a few lines: the two commits, the number of checks,
anything in the spec that was wrong, and any Niva findings. Then stop;
increment 006 is a separate checkpoint.
