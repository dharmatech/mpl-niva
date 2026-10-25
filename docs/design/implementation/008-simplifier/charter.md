# 008 simplifier: charter

The handoff for starting the simplifier in a new conversation. It says
where MPL stands, what Niva now gives it, and how to proceed: design
first, then small increments, each landing on its own with checks.

## The goal

Cohen's automatic simplification: every expression in one canonical
form, so `x + x` is `2 * x` and `(x + y) + z` is `x + y + z`. Built up a
few rules at a time, not in one go. This conversation's job is to agree
on those increments with the user, write them down in `spec.md`, and
then do them one at a time as checkpoints.

## Where MPL is

- `mpl/tree.niva`: `union Expr = Num value: Int | Sym name: String |
  Sum terms: List(Expr) | Product factors: List(Expr) | Power base: Expr
  exp: Expr`; `3 num`, `"x" sym`; `+`, `*` and `pow:` build exactly
  what's written, nothing simplified.
- `mpl/notation.niva`: `text`, math notation with only the parentheses
  the structure needs. A nested sum shows: `(x + y) + z`.
- `==` compares structure, operands in order (series
  [003](../003-equals/README.md)).
- Checks: `tools/check`, 31 of them in `tests/`, each a `Check named:
  got: want:` or `Check named: holds:`, collected in `check.niva`. The
  demo: `tools/nin run --backend=go`.
- Integers only. No `Frac`, and no `/` operator yet.
- The Go backend. Clojure stops at a type used from another file
  (Niva feedback item 3); let-go runs it
  ([series 002](../002-let-go/README.md)).

## What Niva gives now

The compiler is pinned to `self-host` `781c5b05`
([series 007](../007-sorted-with-pin/README.md)). Since this project
started, Niva gained, from our PRs:

- list, set and map literals that take the union their context expects,
  so `| Sum => terms |=> {this}` builds on Go (#450);
- `<`, `>`, `<=` and `>=` on strings (#452);
- `inject:into:` that runs (#454);
- `sortedWith: [a, b -> Int]`, a stable comparator sort (#455);
- `==` by content (#440), and early `^` fixed on Go (#437).

What still bites, with workarounds, is in
[Niva notes](../../../niva-notes.md): read it before writing Niva. The
ones the simplifier is most likely to meet:

- binary messages go left to right with no precedence, so write
  `x + (2 num * y)`;
- `inject:into:`'s total has to be the items' type: `map:` first (item
  34);
- a list with nothing expected of it keeps its items' type: declare
  `l::List(Expr) = …` (the case #450 leaves);
- no `contains:` (use `any:`), `all:`, `reversed`, `sum` or
  `indexOfFirst:`;
- a type name of one letter is read as a type parameter.

## Design questions for the spec

Settle these with the user before code, or decide they can wait:

- **The order relation.** Cohen's ≺ is what canonical order sorts by:
  13 rules in mpl's `order-relation.sls`, O-1 to O-13. MPL needs O-1
  to O-4 (numbers, symbols, sums and products, powers) and the mixed
  ones from O-7 on; O-5 (factorials) and O-6 (functions) don't apply
  to its `Expr`. As `Expr before: Expr -> Int` it plugs into
  `sortedWith:`, and symbols compare with string `<`.
- **Simplify as you build, or a separate `simplify`?** mpl and
  Symbolism simplify inside the operators. MPL's operators build
  exactly what's written, and its printing and checks rely on that,
  which suggests an `Expr simplify -> Expr` beside them.
- **Undefined.** Cohen returns *Undefined* for `0^0` and division by
  zero. A union branch, when a rule first needs it?
- **Rational numbers.** Cohen's numbers are fractions. Integer-only
  rules can come first; a `Frac` branch, with `gcd`, is its own
  increment.
- **Where the code goes:** `mpl/simplify.niva`, or a file per rule
  group (`sums`, `products`, `powers`, `order`).

## Candidate increments

A starting point to reorder, split or cut with the user, not a plan.
Each is small, lands on its own with new checks, and leaves the demo
showing before and after:

1. **The order relation** for numbers and symbols (O-1, O-2), then
   sums, products and powers (O-3, O-4), then mixed kinds (O-7 on).
   Checks only; nothing uses it yet.
2. **Flattening:** `(x + y) + z` is `x + y + z`, and the same for
   products. Nothing else.
3. **Numbers and identities:** numbers in a sum or product combine; `0`
   leaves a sum, `1` a product; `0` in a product is `0`; a sum or
   product of one operand is that operand, of none `0` or `1`.
4. **Canonical order:** operands sorted with the order relation.
5. **Like terms:** `x + x` is `2 * x`, `2 * x + 3 * x` is `5 * x`
   (Cohen's term and constant parts).
6. **Like factors:** `x * x` is `x^2`, `x^2 * x^3` is `x^5` (base and
   exponent).
7. **Powers:** `x^0`, `x^1`, `0^n`, `1^n`, `(x^a)^b` for an integer
   `b`, integer powers of numbers.
8. **Fractions,** if wanted: `Frac`, and division.

Increments 3 to 6 are where Cohen's `simplify_sum_rec` and
`simplify_product_rec` come in, merging operand lists in order.

## How to work

- **Increments, as checkpoints.** Write `spec.md` with the agreed
  increments, then hand them one at a time from `checkpoints/`. Each
  lands as its own commit with its checks passing (`tools/check`), and
  the series README records it.
- **Docs as before:** this series' README for status and commits; small
  linked pages; the README's layout table when files are added.
- **Niva findings:** record each new one in the
  [feedback list](../../../niva-feedback/README.md), with a small
  program, and work around it in MPL with a comment naming the item.
  **Don't file new issues or PRs with Niva for now**: the user wants to
  give Gavr a break after four merges in a day. Bugs waiting their turn
  stay off Discord too.
- **Moving the pin** (only if needed): change `commit` in
  `tools/build-self-hosted-niva`, run it, remove `.niva_clj/` (item 33),
  run `tools/check` and the demo, and record it as its own series, as
  in [005](../005-string-ordering-pin/README.md) to
  [007](../007-sorted-with-pin/README.md).
- **Comparing with the Kotlin compiler:** run it with a copy of the
  clone's `local/home`, never `local/home` itself
  ([The self-hosted compiler](../../../self-hosted-compiler.md)).

## References

The earlier versions are checked out locally, three in `~/src` and
Aloe's in `~/journal` ([lineage](../../../lineage.md)):

- `~/src/mpl`, Scheme: `automatic-simplify.sls`, `order-relation.sls`,
  and `sum-product-power.sls`, which has `simplify-sum-rec`,
  `merge-sums`, `simplify-product-rec`, `merge-products` and
  `simplify-power`. The closest to Cohen's pseudocode.
- `~/src/Symbolism`, C#: `Symbolism/Symbolism.cs`, with tests in
  `Tests/Tests.cs`.
- `~/src/symbolism.lean`: `Symbolism/Basic.lean`, a very basic
  simplifier over binary nodes, flattened to lists to work on sums.
- `~/journal/2026-09-02-aloe-racket/examples/mpl`, Aloe, the language
  closest to Niva of these: message sends to objects. Most of it is in
  `core.aloe`, with a class per kind in `sym.aloe`, `num.aloe`,
  `sum.aloe`, `prod.aloe` and `pow.aloe`. It simplifies as it builds:
  a `Sum` keeps its terms and a separate integer `const`, a `Prod`
  merges factors by base and exponent (`factor-base`, `factor-exp`,
  `merge-factor`), and `unwrap` drops the identities. Its checks, in
  `identities.aloe`, are a ready list of first rules: `x * x` is
  `x^2`, `(x * 2) * 3` is `x * 6`, `(x + 2) + 3` is `x + 5`,
  `x^2 * x^3` is `x^5`, `x * 1`, `x + 0` and `x^1` are `x`, `x * 0` is
  `0`, and `-3x + 3x` is `0`.

## First steps in the new conversation

1. Read this charter, the [Niva notes](../../../niva-notes.md), the
   [README](../../../../README.md), and `mpl/`.
2. Read `~/src/mpl`'s `order-relation.sls` and `sum-product-power.sls`,
   and Aloe's `examples/mpl` (`core.aloe` and `identities.aloe`): the
   closest in Niva's spirit, and the smallest.
3. With the user: settle the design questions above, or which can
   wait, and pick the increments and their order.
4. Write `spec.md`, then the first checkpoint, and start it.
