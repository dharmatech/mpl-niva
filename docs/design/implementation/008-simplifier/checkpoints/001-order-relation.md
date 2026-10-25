# simplifier 001: order relation

**Status:** Done at `c277081` and reviewed (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

Cohen's order relation on `Expr`, as `compare:` and `before:`, with its
checks. Nothing uses it yet; increment 003 will. Stop when the checks
pass and the commit is in.

## Read first

- [The spec](../spec.md): the decisions, and §001, which this
  checkpoint carries out. The later increments are context, not work.
- [Niva notes](../../../../niva-notes.md), before writing any Niva.
- `mpl/tree.niva` and `mpl/notation.niva`, `check.niva`, and the
  files in `tests/`, to match how they're written.
- `~/src/mpl/order-relation.sls`, the Scheme version.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 31 checks. The compiler is built and pinned at `781c5b05`; if
`tools/nin` is missing, run `tools/build-self-hosted-niva`.

## Build

**`mpl/order.niva`**, new:

- `Expr compare: Expr -> Int`: negative when the receiver comes first
  in Cohen's ≺, 0 when they're equal, positive when the argument comes
  first. -1, 0 and 1 are fine.
- `Expr before: Expr -> Bool`: `compare:` below 0.
- The rules are the spec's O-1 to O-4, O-7 to O-10 and O-13. In O-9,
  `v^1` is a comparison of bases and then of the exponent with `1 num`;
  there's no need to build the power. In O-8 and O-10, `v` as a list of
  one is `{v}` handed to the O-3 comparison.
- O-3 compares two operand lists from the last. Where that function
  lives is up to you: a method on `List(Expr)` if Go takes it, or one
  on `Expr` or a small type taking both lists. Increment 002 needs the
  same answer for the merge, so say in your report which you used and
  what Go refused, if anything.
- A comment at the top in the style of `notation.niva`: what this is,
  and that it's Cohen's ≺, an order only on simplified expressions.

**`tests/orderChecks.niva`**, new, as `type OrderChecks` with
`constructor OrderChecks all -> List(Check)`, like the other two.
Added to `check.niva`. The checks are the spec's §001 table, one
`Check named: … holds: …` each, named by rule and pair, such as
`"O-3: a + b before a + c"`:

- Each `u ≺ v` is `u before: v`.
- `not 3 ≺ 2` is `(3 num before: 2 num) not`.
- The two "compares equal" are `compare:` `== 0`.
- Three-operand sums are `Sum terms: {a c d}`, since `+` nests.

Two more:

- **Sort**, a `got:`/`want:` check: the list `x * y`, `y`, `1 + x`,
  `x^2`, `x`, `2`, sorted with `sortedWith: [a, b -> a compare: b]`,
  each item's `text` joined with `", "`, is `"2, x, x^2, 1 + x, y, x * y"`.
  Declare the list `l::List(Expr) = …`.
- **Antisymmetry**, a `holds:` check: over the same six, for every
  pair `a`, `b`, `a compare: b` and `b compare: a` have opposite signs,
  or are both 0. There's no `all:`; `filter:` and `isEmpty` do it.

The spec worked out the expected answers by hand. If one fails, trace
Cohen's rules by hand to see which side is wrong. If it's the spec, use
the right answer, keep going, and say so in your report; don't edit
`spec.md`.

**The demo** doesn't change: nothing uses the order yet.

## Docs

- `README.md`: the layout table's `mpl/` row adds the order relation
  (`order`).
- The series [README](../README.md): mark 001 done in its increments
  table. Leave the commit cell for the manager, who records it after
  review.
- A Niva bug or gap you meet goes in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 31 plus
  yours.
- `tools/nin run --backend=go` still runs the demo as before.
- One commit on `main`, with the code, checks and docs, in the style of
  `git log`: a subject line, then a wrapped paragraph saying what and
  why. Don't push.

## Report

When done, say in a few lines: the commit, the number of checks, where
the O-3 comparison lives and why, any answer from the spec that was
wrong, and any Niva findings. Then stop; increment 002 is a separate
checkpoint.
