# 008 simplifier: spec

**Status:** Agreed with the user (2026-10-25). All seven increments
are done and reviewed (2026-10-25).

What `simplify` will do, in seven increments, one checkpoint each. The
[charter](charter.md) has the background and the references; this page
is what was decided.

## Decisions

- **A separate `simplify`.** `Expr simplify -> Expr` gives the canonical
  form. `+`, `*` and `pow:` keep building exactly what's written, so
  printing, `==` and their checks stay as they are, and the demo can
  show before and after.
- **Cohen's algorithm, as in mpl.** `simplify` works bottom-up, operands
  first and then the node, as Cohen's `automatic_simplify` does. A sum's
  operands go through `simplify_sum_rec` and `merge_sums`, a product's
  through `simplify_product_rec` and `merge_products`, as in mpl's
  `sum-product-power.sls`. The algebra is all in their two-operand case,
  the pair rule, so each increment adds a case there. The merge sorts as
  it goes, so `sortedWith:` isn't used to simplify.
- **A three-way order.** `Expr compare: Expr -> Int` is negative when the
  receiver comes first in Cohen's ≺, 0 when they're equal, and positive
  when the argument comes first. `before:` is `compare:` below 0: ≺
  itself.
- **Integers only.** No `Frac`, no Undefined, no `-` or `/`. Where Cohen
  gives Undefined, as for `0^0` and `0^(-1)`, the power is left as
  written. What comes after increment 007 is decided then: more
  increments here, or a series of its own.
- **Printed in canonical order.** `(x + 2) + 3` simplifies to `5 + x`.
  A display order, such as `x + 5` or `-x` for `-1 * x`, is for later,
  in `text`.

## Increments

| # | Increment | Adds |
|---|---|---|
| 001 | order relation | `compare:` and `before:`, checked alone |
| 002 | flattening | `simplify`, the merge, sums in sums and products in products spliced |
| 003 | canonical order | the pair rule swaps operands out of order |
| 004 | numbers and identities | numbers combine, `0` and `1` drop out, `0` in a product |
| 005 | like terms | `x + x` is `2 * x` |
| 006 | powers | `x^0`, `x^1`, `1^n`, `0^n`, numbers, `(x^a)^n`, `(x * y)^n` |
| 007 | like factors | `x * x` is `x^2` |

Canonical order comes before numbers because the merge only combines
numbers that are next to each other, and the order is what brings them
to the front. Powers come before like factors because `x^2 * x^-2`
makes `x^0`, which only a power rule turns into `1`.

In the checks below, the left column is math, read with the usual
precedence. In Niva it needs parentheses: `2 * x + 3 * x` is
`(2 num * x) + (3 num * x)`. A sum or product of three operands or more
is built with `Sum terms:` or `Product factors:` where `+` and `*`
would nest it.

### 001 order relation

`mpl/order.niva`: `compare:` and `before:` for every pair of kinds.
Nothing uses them yet. Cohen's rules, by his numbers; O-5, O-6, O-11
and O-12 are for factorials and functions, which `Expr` doesn't have:

- **O-1** Two numbers: by value.
- **O-2** Two symbols: by name, with string `<`.
- **O-3** Two sums, or two products: operands compared from the last.
  The first pair that differs decides. If one runs out of operands
  first, it comes first; if both do, they're equal.
- **O-4** Two powers: by base, and by exponent when the bases are equal.
- **O-7** A number comes before anything else.
- **O-8** A product and a power, sum or symbol `v`: as O-3, with `v` as
  a list of one.
- **O-9** A power and a sum or symbol `v`: as O-4, against `v^1`.
- **O-10** A sum and a symbol `v`: as O-3, with `v` as a list of one.
- **O-13** Anything else: the argument compared with the receiver,
  negated.

`compare:` is defined on every pair, but it's an order only on
simplified expressions: `Product factors: {x}` and `x` compare equal.

Checks, in `tests/orderChecks.niva`, most of them Cohen's own examples:

```
O-1    -1 ≺ 2        not 3 ≺ 2        3 compares equal to 3
O-2    a ≺ b         v1 ≺ v2          x1 ≺ xa
O-3    a + b ≺ a + c       a + c + d ≺ b + c + d
       c + d ≺ b + c + d   a * b ≺ a * c
O-4    (1 + x)^3 ≺ (1 + y)^2      (1 + x)^2 ≺ (1 + x)^3
O-7    2 ≺ x
O-8    a * x^2 ≺ x^3       x ≺ x * y
O-9    (1 + x)^3 ≺ 1 + y   x ≺ x^2
O-10   1 + x ≺ y           y ≺ x + y
equal  x + y compares equal to x + y
```

And one with `sortedWith: [a, b -> a compare: b]`: the list
`x * y, y, 1 + x, x^2, x, 2` sorts to `2, x, x^2, 1 + x, y, x * y`.

### 002 flattening

`mpl/simplify.niva`: `Expr simplify`, bottom-up, and the frame of
Cohen's `simplify_sum` and `simplify_product`:

- A sum or product of one operand is that operand.
- Otherwise its operands are merged. A merge that leaves none gives `0`
  for a sum and `1` for a product, and one that leaves one gives that
  operand.
- The pair rule keeps both operands as they are, so the merge splices a
  sum's sums into it in the order written, and a product's products.
- A power simplifies its base and exponent, and nothing more until 006.

```
(a + b) + c          a + b + c
a + (b + c)          a + b + c
((a + b) + c) + d    a + b + c + d
(a * b) * c          a * b * c
a * (b * c)          a * b * c
(a + b) * (c + d)    (a + b) * (c + d)
(a + (b + c))^2      (a + b + c)^2
Sum terms: {x}       x, checked with ==
Sum terms: {}        0
Product factors: {}  1
```

The demo gains a second part: each example's text, and its simplified
text.

### 003 canonical order

The pair rule gives `q p` when `q before: p`. The merge then sorts.

```
y + x            x + y
x + 2            2 + x
(c + a) + b      a + b + c
y * x            x * y
x * 3            3 * x
x^2 + x          x + x^2
x * y + y        y + x * y
b * c + a * c    a * c + b * c
```

### 004 numbers and identities

New cases in the pair rule, ahead of the order, as in mpl:

- In a sum, two numbers add, and drop out if they make `0`. A `0` drops
  out.
- In a product, two numbers multiply, and drop out if they make `1`. A
  `1` drops out.

And in the frame: a product with a `0` among its operands is `0`.

```
2 + 3                     5
(x + 2) + 3               5 + x
x + 0                     x
(x + 2) + -2              x
Sum terms: {1 x 2 y 3}    6 + x + y
(x * 2) * 3               6 * x
x * 1                     x
x * 0                     0
0 * (x + y)               0
(2 + 3) * x               5 * x
x^(1 + 1)                 x^2
```

### 005 like terms

Cohen's term and constant. A product whose first factor is a number has
that number as its constant and the other factors as its term. Any
other operand that isn't a number has constant 1 and is its own term: a
product's factors, or a list of one.

In a sum, two operands with the same term combine: the constants add,
and the product of that number and the term is simplified, which 004
turns into the term itself for 1 and into `0`, dropped, for 0. The case
comes after the numbers and before the order.

```
x + x                  2 * x
2 * x + 3 * x          5 * x
-3 * x + 3 * x         0
x + y + x              2 * x + y
x * y + y * x          2 * x * y
2 * x + -1 * x         x
x^2 + x^2              2 * x^2
(a + b) + (a + b)      2 * a + 2 * b
```

### 006 powers

Cohen's `simplify_power` and `simplify_integer_power`, for integers.
For `v^w`, the first that applies:

- `0^w` is `0` for a positive integer `w`. Any other `0^w` is left as
  written.
- `1^w` is `1`.
- For an integer `n`:
  - a number to an `n` that isn't negative is worked out: `2^3` is
    `8`;
  - `v^0` is `1`, and `v^1` is `v`;
  - `(r^s)^n` is `r` to the simplified product `s * n`, simplified
    again as an integer power if that product is an integer;
  - `(u * v)^n` is the simplified product `u^n * v^n`;
  - anything else is `v^n`.
- Anything else is left as written.

```
x^0           1
x^1           x
1^x           1
0^3           0
2^3           8
(-2)^3        -8
(x^2)^3       x^6
(x^y)^2       x^(2 * y)
(x^-1)^-1     x
(x * y)^2     x^2 * y^2
(2 * x)^3     8 * x^3
(x^2)^y       (x^2)^y
(x + y)^2     (x + y)^2
```

And `0^0` isn't `1`, checked with `==`: Undefined will change what it
is, so the checks don't pin it, nor `2^(-1)`.

### 007 like factors

Cohen's base and exponent: a power's own, and for any other operand
that isn't a number, the operand itself to the exponent 1. Numbers have
neither, so `2 * 2^x` stays as it is.

In a product, two operands with the same base combine: the exponents
are added, and that power is simplified by 006. A result of `1` drops
out. The case comes after the numbers and before the order.

```
x * x                  x^2
x^2 * x^3              x^5
x * x^2                x^3
x^2 * x^-2             1
x^2 * x^-1             x
x * y * x              x^2 * y
x^y * x^z              x^(y + z)
2 * x * 3 * x          6 * x^2
(x + y) * (x + y)      (x + y)^2
2^x * 2^y              2^(x + y)
2 * 2^x                2 * 2^x
x * x + x^2            2 * x^2
```

## How each increment lands

- **A checkpoint first.** `checkpoints/NNN-name.md`, written when the
  one before has landed, says what to build and check, from this page.
- **One commit,** with its code, checks and docs, and `tools/check`
  passing. The demo, `tools/nin run --backend=go`, shows the new rule.
  The series README records the commit.
- **Checks pin final answers.** An input whose result a later increment
  would change doesn't go in an earlier increment's checks: 002 checks
  `(a + b) + c`, not `(c + b) + a`.
- **Docs.** From 002, `docs/simplifying.md` lists the rules that hold,
  one example each, linked from the README. The README's status line
  and layout table follow the code.
- **Niva findings** go in the
  [feedback list](../../../niva-feedback/README.md) with a small
  program, and the workaround in MPL names the item. No new issues or
  PRs with Niva for now, and nothing on Discord.

## Niva to watch

From the [Niva notes](../../../niva-notes.md), what the simplifier will
likely meet:

- Binary messages go left to right: `x + (2 num * y)`.
- A list literal with nothing expected of it keeps its items' type, so
  `{q p}` may need `l::List(Expr) = …`.
- No `all:` or `contains:`; `any:` works on Go.
- `inject:into:`'s total has to be the items' type: `map:` first.
- Where the list-level parts live, such as the merge, is for 002 to
  find out: methods on `List(Expr)` if Go takes them, or keyword
  messages on a small type. Record which.

## Not in this spec

Undefined; fractions; `-` and `/`; expanding, so `(x + y)^2` stays; a
display order; the Clojure backend; factorials and functions.
