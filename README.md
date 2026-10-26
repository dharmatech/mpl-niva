# MPL in Niva

A small computer algebra simplifier written in
[Niva](https://github.com/gavr123456789/Niva), to exercise the language.

It follows a line of simplifiers based on Joel S. Cohen's *Computer
Algebra and Symbolic Computation* books: mpl for Scheme, Symbolism for
C#, and small versions in Lean and Aloe. [Lineage](docs/lineage.md) has
the story.

Niva's only polymorphism is tagged unions, so an expression is a `union`
and the algorithms `match` on it, as in the Lean version. Sums and
products hold a list of operands, as in mpl and Symbolism, because
Cohen's algorithms work on operand lists.

**Status:** expressions can be built, printed and compared, and
[simplified](docs/simplifying.md) to Cohen's canonical form for
integers: `simplify` flattens sums in sums and products in products,
puts their operands in Cohen's order, works out numbers, so `0` and `1`
drop out and a product with a `0` is `0`, combines like terms, so
`x + x` is `2 * x`, combines like factors, so `x * x` is `x^2`, and
simplifies powers to integers, so `(x^2)^3` is `x^6` and `(x * y)^2` is
`x^2 * y^2`. Undefined, fractions, `-` and `/` aren't here yet.

## Running

This project uses Niva's self-hosted compiler, which compiles to Go.
Build it once (it needs a JDK and Go; see
[The self-hosted compiler](docs/self-hosted-compiler.md)):

```bash
tools/build-self-hosted-niva
```

Then run the demo, which builds a few expressions and prints each
beside the Niva that built it, compares two, then simplifies a few,
each part under a heading. Some of its output:

```bash
tools/nin run --backend=go
```

```
Building: the operators build what's written, and nothing is simplified.
Binary messages go left to right, so x + 2 num * y is (x + 2) * y.
Each line is the Niva, then the expression it builds.

x + (2 num * y)   =>   x + 2 * y
x + y + z   =>   (x + y) + z
(x + y) pow: 2 num   =>   (x + y)^2

Comparing: == compares structure, with operands in order.

(x + y) == (x + y)   =>   true
(x + y) == (y + x)   =>   false

Simplifying: simplify puts an expression in Cohen's canonical form.
Each line is an expression as built, then simplified.

(x + y) + z   =>   x + y + z
(y + x) + 2   =>   2 + x + y
(x + 2) + 3   =>   5 + x
(x + y) + x   =>   2 * x + y
(x * y)^2   =>   x^2 * y^2
(x * y) * x   =>   x^2 * y
```

And the checks, which end with `all N checks passed` when they do:

```bash
tools/check
```

## Building expressions

```
x = "x" sym
y = "y" sym
e = x + (2 num * (y pow: 3 num))
```

Binary messages have no precedence and go left to right, so
`x + 2 num * y` is `(x + 2) * y`. Keyword messages bind loosest, so
`pow:` needs parentheses. The operators build exactly what's written:
`x + y + z` is a sum inside a sum and prints as `(x + y) + z`.

## Layout

| Path | What |
|---|---|
| `mpl/` | The library: `Expr` and its operators (`tree`), printing (`notation`), the order relation (`order`), and simplifying (`simplify`). |
| `main.niva` | The demo. |
| `check.niva`, `tests/` | The checks. |
| `tools/` | Building and running the self-hosted compiler, and running the checks. |

## Docs

- [Simplifying](docs/simplifying.md): what `simplify` does, the rules
  that hold, and a known gap.
- [Niva notes](docs/niva-notes.md): the language as this project meets
  it, and workarounds for its bugs.
- [The self-hosted compiler](docs/self-hosted-compiler.md): building and
  running it.
- [Design and implementation](docs/design/implementation/README.md): one
  folder per change, in order, with what was done and why.
- [Niva feedback](docs/niva-feedback/README.md): bugs found, and the ones
  filed.
- [let-go feedback](docs/let-go-feedback/README.md): suggestions for
  let-go, found running Niva's Clojure output on it.
- [Lineage](docs/lineage.md): the simplifiers this one descends from.
