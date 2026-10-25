# Simplifying

`e simplify` is `e` in canonical form: Cohen's automatic
simplification, in `mpl/simplify.niva`. It's separate from the
operators. `+`, `*` and `pow:` build exactly what's written, so
`x + y + z` is a sum inside a sum and prints as `(x + y) + z`;
`(x + y + z) simplify` is one sum of three terms, `x + y + z`.

It works bottom-up: an expression's operands are simplified first, then
the expression. A sum's terms are merged two lists at a time, as in
Cohen's algorithm and mpl, and what two terms become is up to a pair
rule; a product's factors likewise. Each new rule for sums and
products is a case in a pair rule, and a power has rules of its own.

## Rules so far

Simplifying flattens, sorts, works out numbers, combines like terms and
simplifies powers to integers for now. Numbers combine, as do terms that
differ only in their number, and `0` and `1` drop out. Factors don't
combine yet: `x * x` stays as it is.

| Rule | Example | Simplified |
|---|---|---|
| A sum's sums are spliced into it. | `(a + b) + c` | `a + b + c` |
| A product's products are spliced into it. | `a * (b * c)` | `a * b * c` |
| A sum's terms, and a product's factors, are in canonical order. | `(y + x) + 2` | `2 + x + y` |
| A sum or product of one operand is that operand. | `Sum terms: {x}` | `x` |
| A sum of none is 0, and a product of none is 1. | `Product factors: {}` | `1` |
| Numbers in a sum add. | `(x + 2) + 3` | `5 + x` |
| Numbers in a sum that make 0 drop out. | `(x + 2) + -2` | `x` |
| A 0 in a sum drops out. | `x + 0` | `x` |
| Numbers in a product multiply. | `(x * 2) * 3` | `6 * x` |
| Numbers in a product that make 1 drop out. | `(-1 * x) * -1` | `x` |
| A 1 in a product drops out. | `x * 1` | `x` |
| A product with a 0 among its factors is 0. | `0 * (x + y)` | `0` |
| Like terms in a sum combine, their numbers added. | `2 * x + 3 * x` | `5 * x` |
| Like terms that make 0 drop out. | `(-3 * x + y) + 3 * x` | `y` |
| Anything to 0 is 1, except 0 itself. | `x^0` | `1` |
| Anything to 1 is itself. | `x^1` | `x` |
| 1 to anything is 1. | `1^x` | `1` |
| 0 to a positive number is 0. | `0^3` | `0` |
| A number to one that isn't negative is worked out. | `(-2)^3` | `-8` |
| A power of a power, to a number, multiplies the exponents. | `(x^2)^3` | `x^6` |
| A power of a product, to a number, is the product of its factors' powers. | `(2 * x)^3` | `8 * x^3` |

The canonical order is Cohen's, `before:` in `mpl/order.niva`: numbers
first, by value; symbols alphabetically; and sums, products and powers
mostly by their last operands, so `x * y + y` is `y + x * y`. It's what
brings a sum's numbers together, at the front, and its like terms side
by side, so they combine.

Like terms are Cohen's: an operand's constant is its number, if it's a
product that starts with one, and 1 otherwise, and its term is the rest,
so `2 * x * y` has constant 2 and term `x * y`, and `x` has constant 1
and term `x`. Two operands with the same term combine, their constants
added: `x + x` is `2 * x`, `2 * x * y + x * y` is `3 * x * y`, and
`2 * x + -1 * x` is `x`, since a 1 in a product drops out.

Powers are Cohen's simplify_power and simplify_integer_power, for
integers. The base and exponent are simplified first, so `(1 + 1)^3` is
`8` and `x^(2 + -2)` is `1`. A power of a power to a number is the base
to the exponents' product, simplified, and again an integer power if
that's a number: `(x^y)^2` is `x^(2 * y)`, and `(x^-1)^-1` is `x`. To
anything else, as in `(x^2)^y`, it stays as written. A power of a
product to a number is its factors' powers, multiplied and simplified,
so `(-1 * x)^2` is `x^2`. A sum's power stays, since nothing expands:
`(x + y)^2` is `(x + y)^2`.

`0^0`, `0^(-1)` and `0^x` stay as written, where Cohen's answer is
Undefined, and so does a number to a negative power, such as `2^(-1)`,
which is a fraction. Neither Undefined nor fractions are here yet.

## What's coming

Like factors, so `x * x` is `x^2`: see the
[spec](design/implementation/008-simplifier/spec.md).
