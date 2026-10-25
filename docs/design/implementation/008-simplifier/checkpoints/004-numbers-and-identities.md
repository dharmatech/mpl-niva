# simplifier 004: numbers and identities

**Status:** Issued (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

Numbers in a sum add up and numbers in a product multiply; `0` drops
out of a sum and `1` out of a product; a product with a `0` in it is
`0`. These are the first pair-rule cases that turn two operands into
one or none, so the merge's other two paths start to run. Stop when the
checks pass and the commit is in.

## Read first

- [The spec](../spec.md): the decisions, and §004, which this
  checkpoint carries out.
- [Niva notes](../../../../niva-notes.md), before writing any Niva.
- `mpl/simplify.niva` (the frame and the pair rules, from 002 and
  003) and `mpl/order.niva`, whose nested matches show how to narrow
  both operands.
- `~/src/mpl/sum-product-power.sls`: the number and identity cases of
  `simplify-sum-rec` and `simplify-product-rec`, and `simplify-product`.
- `tests/simplifyChecks.niva`, `main.niva`, and `docs/simplifying.md`.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 78 checks. The compiler is built and pinned at `781c5b05`.

## Build

**`mpl/simplify.niva`:**

- **`sumPair:`**, the first that applies, ahead of the order:
  - two numbers: their sum, as a list of one, or an empty list if it's
    0;
  - this is `0`: `{other}`;
  - other is `0`: `{this}`;
  - then the order, as now.
- **`productPair:`** likewise: two numbers give their product, or
  nothing if it's 1; this is `1`: `{other}`; other is `1`: `{this}`;
  then the order.
- **`simplifiedProduct`**, Cohen's `simplify_product`: if any operand
  is `0`, the product is `0 num`, before anything else. `any:` works on
  Go.
- The pair rules' comments, and the file's top comment, say what holds
  now. Later increments still add cases between these and the order.

Whether to match on `Num` or compare with `== 0 num` is up to you;
keep the pair rules easy to read as a list of cases, since 005 and 007
add to them.

**`tests/simplifyChecks.niva`:** the spec's §004 table, as `got: (e
simplify) text want:` checks named like `"numbers: 2 + 3"`, in a
group after the order checks. Also:

- `2 + -2` is `0`, and `2 * 3` is `6`.
- `0 + x` and `1 * x` are `x`: the identity on the left.
- `(-1 * x) * -1` is `x`: two numbers that multiply to 1 and drop out,
  leaving one factor.
- `Product factors: {2 x 3 y}` is `6 * x * y`.

Write each input so that its result is final: no two operands with the
same term or base, so that 005 and 007 leave it alone. The checks from
002 and 003 must still pass unchanged.

**The demo:** one or two examples that combine numbers, such as
`(x + 2) + 3`, which is `5 + x`, and `(x * 2) * 3`, which is `6 * x`.

## Docs

- **`docs/simplifying.md`:** the new rules, an example each, in the
  table; "nothing combines yet" goes.
- **`README.md`:** the status line says what simplifying does now.
- **The series [README](../README.md):** mark 004 done in its
  increments table. Leave the commit cell for the manager.
- **Niva findings** go in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 78 plus
  yours.
- `tools/nin run --backend=go` shows the new examples.
- One commit on `main`, with the code, checks and docs, in the style of
  `git log`: a subject line, then a wrapped paragraph saying what and
  why. Don't push.

## Report

When done, say in a few lines: the commit, the number of checks,
anything in the spec that was wrong, and any Niva findings. Then stop;
increment 005 is a separate checkpoint.
