# Simplifying

`e simplify` is `e` in canonical form: Cohen's automatic
simplification, in `mpl/simplify.niva`. It's separate from the
operators. `+`, `*` and `pow:` build exactly what's written, so
`x + y + z` is a sum inside a sum and prints as `(x + y) + z`;
`(x + y + z) simplify` is one sum of three terms, `x + y + z`.

It works bottom-up: an expression's operands are simplified first, then
the expression. A sum's terms are merged two lists at a time, as in
Cohen's algorithm and mpl, and what two terms become is up to a pair
rule; a product's factors likewise. Each new rule is a case in a pair
rule.

## Rules so far

Simplifying flattens and sorts for now: nothing combines yet.

| Rule | Example | Simplified |
|---|---|---|
| A sum's sums are spliced into it. | `(a + b) + c` | `a + b + c` |
| A product's products are spliced into it. | `a * (b * c)` | `a * b * c` |
| A sum's terms, and a product's factors, are in canonical order. | `(y + x) + 2` | `2 + x + y` |
| A sum or product of one operand is that operand. | `Sum terms: {x}` | `x` |
| A sum of none is 0, and a product of none is 1. | `Product factors: {}` | `1` |

The canonical order is Cohen's, `before:` in `mpl/order.niva`: numbers
first, by value; symbols alphabetically; and sums, products and powers
mostly by their last operands, so `x * y + y` is `y + x * y`.

A power simplifies its base and exponent, so `(a + (b + c))^2` is
`(a + b + c)^2`.

## What's coming

Numbers and identities, like terms, powers and like factors, one
increment each: see the
[spec](design/implementation/008-simplifier/spec.md).
