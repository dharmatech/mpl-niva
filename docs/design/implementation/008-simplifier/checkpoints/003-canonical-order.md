# simplifier 003: canonical order

**Status:** Issued (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

A simplified sum keeps its terms in Cohen's order, and a product its
factors: each pair rule swaps two operands that are out of order, and
the merge does the rest. Stop when the checks pass and the commit is
in.

## Read first

- [The spec](../spec.md): the decisions, and §003, which this
  checkpoint carries out.
- [Niva notes](../../../../niva-notes.md), before writing any Niva.
- `mpl/order.niva` (`compare:` and `before:`, from 001) and
  `mpl/simplify.niva` (the frame and the pair rules, from 002).
- `tests/simplifyChecks.niva`, `main.niva`, and `docs/simplifying.md`.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 67 checks. The compiler is built and pinned at `781c5b05`.

## Build

**`mpl/simplify.niva`:**

- `sumPair:` gives the two operands swapped when the second comes
  first, `other before: this`, and as given otherwise. `productPair:`
  likewise. That's the whole change to the algorithm: with it, the
  merge sorts, since `sumRec` inserts each operand into the merged
  rest. 002 found that a list literal returned from a method declared
  `-> List(Expr)` takes that type, so `{other this}` needs no local.
- The comments that say the pair rules only keep both operands, and
  that simplifying only flattens, say what holds now.

**`tests/simplifyChecks.niva`:** the spec's §003 table, as `got: (e
simplify) text want:` checks named like `"order: y + x"`, in a group
of their own after the flattening checks. Also:

- `Sum terms: {d b c a}` is `a + b + c + d`, and
  `Product factors: {c a b}` is `a * b * c`: longer lists out of order.
- `Sum terms: {(x * y) y (x pow: 2 num) x (2 num)}` is
  `2 + x + x^2 + y + x * y`, the same order 001's sort check gives.

Write each input so that its result is final: at most one number in a
sum or product, and no two operands with the same term or base, so
that 004, 005 and 007 leave it alone. The flattening checks from 002
must still pass unchanged.

**The demo:** one or two examples in the simplifying part that show
the order, such as `(y + x) + 2`, which is `2 + x + y`.

## Docs

- **`docs/simplifying.md`:** canonical order joins the rules, with an
  example, and a sentence on what the order is: numbers first, symbols
  alphabetically, and sums, products and powers mostly by their last
  operands, as in `mpl/order.niva`. The line saying operands keep the
  order they're written in goes.
- **`README.md`:** the status line says what simplifying does now.
- **The series [README](../README.md):** mark 003 done in its
  increments table. Leave the commit cell for the manager.
- **Niva findings** go in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 67 plus
  yours.
- `tools/nin run --backend=go` shows the new examples.
- One commit on `main`, with the code, checks and docs, in the style of
  `git log`: a subject line, then a wrapped paragraph saying what and
  why. Don't push.

## Report

When done, say in a few lines: the commit, the number of checks,
anything in the spec that was wrong, and any Niva findings. Then stop;
increment 004 is a separate checkpoint.
