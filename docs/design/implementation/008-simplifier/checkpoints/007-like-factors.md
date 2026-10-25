# simplifier 007: like factors

**Status:** Issued (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

Factors with the same base combine: `x * x` is `x^2`, `x^2 * x^3` is
`x^5`, and `x^2 * x^-2` is `1`. It's the spec's last increment: after
it, `simplify` gives Cohen's canonical form for MPL's integer
expressions. Stop when the checks pass and the commit is in.

## Read first

- [The spec](../spec.md): the decisions, and §007, which this
  checkpoint carries out.
- [Niva notes](../../../../niva-notes.md), before writing any Niva,
  including the new note on chains of binary messages (feedback item
  36).
- `mpl/simplify.niva`: the pair rules, `simplifiedPower:`, and
  `powerBase` and `powerExp`.
- `~/src/mpl/sum-product-power.sls`: the like-factors case of
  `simplify-product-rec`, and `base` and `exponent` in
  `~/src/mpl/order-relation.sls`.
- `tests/simplifyChecks.niva`, `main.niva`, and `docs/simplifying.md`.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 134 checks. The compiler is built and pinned at `781c5b05`.

## Build

**`mpl/simplify.niva`:**

- **Cohen's base and exponent** are 006's `powerBase` and `powerExp`:
  a power's own, and for anything else the operand itself and `1`. Use
  them, and say in their comments that they're Cohen's base and
  exponent, used by `integerPower:` and the product pair rule.
- **A case in `productPair:`,** after the numbers and the `1`s and
  before the order: two operands, neither a number, whose `powerBase`
  is `==`, give the base's `simplifiedPower:` the simplified sum of
  their exponents, as a list of one, or none if it's `1`, through
  `listUnless: 1`. Numbers have neither base nor exponent in Cohen's
  rules, so `2 * 2^x` stays as it is; `2^x * 2^y` combines, since both
  are powers.
- `sumPair:` doesn't change.
- The comments, the pair rule's and the file's, say what holds now.

**`tests/simplifyChecks.niva`:** the spec's §007 table, as `got: (e
simplify) text want:` checks named like `"like factors: x * x"`, in a
group after the power checks. In Niva, `x^2 * x^3` is
`(x pow: 2 num) * (x pow: 3 num)`, and `x * x + x^2` is
`(x * x) + (x pow: 2 num)`. Also:

- `x * x * x` is `x^3`.
- `x^2 * y * x^-2` is `y`: factors that make 1 drop out, leaving the
  rest. A check like `x^2 * x^-2`, whose answer is `1` either way,
  doesn't show that, as 005 found for like terms.
- `(x * y)^2 * x` is `x^3 * y^2`: a power of a product, then like
  factors.
- `(x * y) * (x * y)^-1` is `1`.

The checks from 002 to 006 must still pass unchanged. If one doesn't,
it's a check that wasn't final: say which in the report.

**The demo:** an example or two of like factors, such as `x * y * x`,
which is `x^2 * y`.

## Docs

- **`docs/simplifying.md`:** like factors join the rules, with a
  sentence on base and exponent. "Factors don't combine yet" goes. The
  "What's coming" part says the spec's increments are done, and that
  Undefined, fractions, `-` and `/` are to be decided.
- **A known gap,** on the same page: Cohen's algorithm, and mpl's,
  takes what two like factors make as a single factor. When that's a
  product, as `(a * b)^y * (a * b)^(1 + -1 * y)` makes `a * b`, it isn't
  spliced into the product around it. Try it with another factor
  alongside, such as `c`, and say what it gives. Don't pin it with a
  check.
- **`README.md`:** the status line says what `simplify` does now.
- **The series [README](../README.md):** mark 007 done in its
  increments table. Leave the commit cell, and the series status, for
  the manager.
- **Niva findings** go in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 134 plus
  yours.
- `tools/nin run --backend=go` shows the new examples.
- One commit on `main`, with the code, checks and docs, in the style of
  `git log`: a subject line, then a wrapped paragraph saying what and
  why. Don't push.

## Report

When done, say in a few lines: the commit, the number of checks, what
the known-gap example gave, anything in the spec that was wrong, and
any Niva findings. Then stop.
