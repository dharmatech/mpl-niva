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
like factors, and simplifies powers to integers. Numbers combine, as do
terms that differ only in their number and factors with the same base,
and `0` and `1` drop out.

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
| Like factors in a product combine, their exponents added. | `x^2 * x^3` | `x^5` |
| Like factors that make 1 drop out. | `x^2 * y * x^-2` | `y` |
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

Like factors are Cohen's too: an operand's base and exponent are a
power's own, and for anything else that isn't a number, the operand
itself and 1, so `x^2` has base `x` and exponent 2, and `x` has base
`x` and exponent 1. Two factors with the same base combine, the base to
the simplified sum of their exponents, simplified as a power: `x * x^2`
is `x^3`, `x^y * x^z` is `x^(y + z)`, and `x^2 * x^-1` is `x`, since
anything to 1 is itself. A number has neither, so `2 * 2^x` stays as it
is, while `2^x * 2^y` is `2^(x + y)`.

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

## A known gap

Cohen's algorithm, and mpl's, takes what two like factors make as a
single factor. When that's a product, it isn't spliced into the product
around it. `(a * b)^y * (a * b)^(1 + -1 * y)` is `a * b`, as it should
be, and `((a * b)^y * (a * b)^(1 + -1 * y)) * c` is `a * b * c`, since
the inner product is simplified first and its `a * b` then spliced. But
`c * (a * b)^y * (a * b)^(1 + -1 * y)`, which `*` builds as
`(c * (a * b)^y) * (a * b)^(1 + -1 * y)`, merges the like factors beside
`c`, and gives `(a * b) * c`: a product inside a product.
`Product factors:` of the three gives the same.

A number that like factors make stays apart from the product's number
in the same way: `(3 * 2^x) * 2^(3 + -1 * x)` is `3 * 8`. Like terms
have the gap too: `(c + 2 * (a + b)) + -1 * (a + b)` is `(a + b) + c`.
The checks don't pin any of these.

## What's coming

The spec's seven increments are done. Undefined, fractions, `-` and `/`
are to be decided: more increments in the
[spec](design/implementation/008-simplifier/spec.md), or a series of
their own.
