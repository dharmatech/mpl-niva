# simplifier 002: flattening

**Status:** Done at `68906c4` and reviewed (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

`Expr simplify`, bottom-up, with the frame of Cohen's algorithm for
sums and products: the merge, and a pair rule that for now keeps both
operands. That's enough to splice sums into sums and products into
products. Every later increment adds a case to the pair rules this one
writes, so their shape matters more than what they do yet. Stop when
the checks pass and the commit is in.

## Read first

- [The spec](../spec.md): the decisions, and §002, which this
  checkpoint carries out. §003 to §007 show where the pair rules are
  going.
- [Niva notes](../../../../niva-notes.md), before writing any Niva.
- `mpl/tree.niva`, `mpl/notation.niva`, `mpl/order.niva`, `check.niva`,
  `tests/`, and `main.niva`, to match how they're written.
  `mpl/order.niva` shows a method on `List(Expr)`, which Go takes.
- `~/src/mpl/sum-product-power.sls`: `simplify-sum`, `simplify-sum-rec`
  and `merge-sums`, and their product twins. That's the algorithm to
  follow, less the rules that come later.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 53 checks. The compiler is built and pinned at `781c5b05`.

## Build

**`mpl/simplify.niva`**, new. The names below are suggestions; keep
Cohen's name for each in its comment.

- **`Expr simplify -> Expr`**, Cohen's `automatic_simplify`. A number
  or symbol is itself. A sum simplifies its terms, then hands them to
  the sum frame; a product likewise its factors. A power is
  `Power base: base simplify exp: exp simplify` until 006.
- **The sum frame,** Cohen's `simplify_sum`, as
  `List(Expr) simplifiedSum -> Expr` on simplified operands. No
  operands give `0 num`. One gives that operand. Otherwise the operands
  go through `sumRec`, and the result is `0 num` if it's empty, its
  operand if it has one, and `Sum terms:` of it if more.
- **`List(Expr) sumRec -> List(Expr)`**, Cohen's `simplify_sum_rec`,
  for two operands or more:
  - Two, neither a sum: the pair rule.
  - Two, one or both a sum: their terms merged. A helper such as
    `Expr asTerms -> List(Expr)`, a sum's terms or else a list of the
    one operand, keeps this short. The "neither a sum" test must come
    first, or the merge calls back into it forever.
  - More than two: the first operand's terms merged with `sumRec` of
    the rest.
- **`List(Expr) mergeSum: List(Expr) -> List(Expr)`**, Cohen's
  `merge_sums`, two lists in order:
  - If one is empty, the other.
  - Otherwise `h` is `sumRec` of their two first operands, and the
    first that applies:
    - `h` is empty: merge the two rests;
    - `h` has one operand: that, then the two rests merged;
    - `h` is the two firsts in order: `p`'s first, then `p`'s rest
      merged with all of `q`;
    - `h` is them swapped: `q`'s first, then all of `p` merged with
      `q`'s rest.

    Compare `h` with the firsts using `==` on lists.
- **The pair rule,** `Expr sumPair: Expr -> List(Expr)`, the base case
  of `simplify_sum_rec` for two operands that aren't sums. For now it
  returns both in the order given. Later increments add cases above
  that last line, so write it as a method of its own with that one
  case and a comment saying so.
- **The product side,** the same four pieces for products:
  `simplifiedProduct` (no operands give `1 num`, and so does an empty
  result), `productRec`, `mergeProduct:` and `productPair:`, with
  `asFactors`. Keep the two sides as separate parallel code, as mpl
  does, rather than one parameterized version: their pair rules diverge
  from 004 on.
- A comment at the top in the style of `notation.niva`: what
  `simplify` is (Cohen's automatic simplification, separate from the
  operators, which build what's written), and that it works bottom-up.

A list literal that Go types too narrowly needs `l::List(Expr) = …`
(see the notes); a return type may or may not count as what's expected.
If you find out which, say so in the report.

**`tests/simplifyChecks.niva`**, new, as `type SimplifyChecks` with
`constructor SimplifyChecks all -> List(Check)`, added to
`check.niva`. The spec's §002 table, as `got: (e simplify) text want:`
checks named after the input, such as `"flatten: (a + b) + c"`. Write
`((a + b) + c) + d` in Niva as `a + b + c + d`, which builds the same
thing. Also:

- `Sum terms: {x}` and `Product factors: {x}` simplify `==` to `x`,
  as `holds:` checks, since their text can't tell.
- `Sum terms: {(a + b)}` simplifies to `a + b`, its one operand.

Write each input so that its result is final: no later increment may
change it. Operands already in alphabetical order, with no numbers,
do that.

**The demo,** `main.niva`, gains a second part after the first: a
blank line, then for each example its text and its simplified text,
as `(x + y) + z   =>   x + y + z`. Use three or four examples from
the checks, with sums, products and a power.

## Docs

- **`docs/simplifying.md`**, new, a short page: what `simplify` does
  and how it differs from the operators, then the rules that hold so
  far, one example each (flattening sums, flattening products, a sum or
  product of one operand, of none), and a link to the spec for what's
  coming.
- **`README.md`:** the status line says simplifying has begun, with a
  link to `docs/simplifying.md`; the Docs list links it too; the layout
  table's `mpl/` row adds `simplify`; the demo output under Running
  shows a line of the new part.
- **The series [README](../README.md):** mark 002 done in its
  increments table. Leave the commit cell for the manager.
- **Niva findings** go in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 53 plus
  yours.
- `tools/nin run --backend=go` shows the new part of the demo.
- One commit on `main`, with the code, checks and docs, in the style of
  `git log`: a subject line, then a wrapped paragraph saying what and
  why. Don't push.

## Report

When done, say in a few lines: the commit, the number of checks, the
names you used for the pieces, whether a return type counts as what a
list literal expects, anything in the spec that was wrong, and any Niva
findings. Then stop; increment 003 is a separate checkpoint.
