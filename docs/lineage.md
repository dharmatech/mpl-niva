# Lineage

MPL in Niva is the latest in a line of small computer algebra
simplifiers. All of them come from the same two books, and each one is
rewritten to suit its language.

| Year | Project | Language | An expression is |
|---|---|---|---|
| 2002–03 | Cohen's books | pseudocode | — |
| 2009 | [mpl](https://github.com/dharmatech/mpl) | R6RS Scheme | a list: `(* -1/3 x y)` |
| 2012 | [Symbolism](https://github.com/dharmatech/Symbolism) | C# | an object, one class per kind |
| 2026 | [symbolism.lean](https://github.com/dharmatech/symbolism.lean) | Lean | an inductive type with binary nodes |
| 2026 | Aloe's `examples/mpl` | Aloe | an object of a class implementing `Math` |
| 2026 | this project | Niva | a union whose sums and products hold lists |

## Cohen's books

Joel S. Cohen, of the University of Denver, wrote two volumes:

- *Computer Algebra and Symbolic Computation: Elementary Algorithms*
- *Computer Algebra and Symbolic Computation: Mathematical Methods*

They present their algorithms in a "Mathematical Pseudo-Language", MPL,
which is where the name comes from. The algorithm at the center of this
line is automatic simplification: putting every expression in one
canonical form, so `x + x` becomes `2 * x`.

## mpl, for Scheme (2009)

R6RS libraries implementing some of the books' algorithms. Expressions
are plain lists, and `+`, `-`, `*` and `/` are redefined to simplify as
they build: `(- (/ (* x y) 3))` is `(* -1/3 x y)`. Besides
simplification it has expansion, polynomial division and GCD,
trigonometric rules, and derivatives.

## Symbolism, for C# (2012)

Began as a port of mpl. Each kind of expression is a class (`Sum`,
`Product`, `Power`, `Symbol`, `Integer`, `Fraction`), and the overloaded
operators simplify as they build, as in mpl. It grew equation solving
and is exercised with physics problems from Serway and Jewett's *Physics
for Scientists and Engineers*.

## symbolism.lean (2026)

A very basic simplifier, based on Symbolism. `Expr` is an inductive type
with binary `add`, `mul` and `pow`, and the simplifier pattern-matches
on it. To work on a sum, it flattens the binary nodes into a list of
terms, then folds the result back.

## Aloe (2026)

A small demo in `examples/mpl` of
[Aloe](https://github.com/dharmatech/2026-09-02-aloe-racket), an
experimental language. Each kind of expression (`Sym`, `Num`, `Sum`,
`Prod`, `Pow`) is a class implementing a `Math` protocol. A closed `Expr`
type was considered and set aside for that experiment.

## MPL in Niva (2026)

This project. Niva has no classes, only tagged unions, so an expression
is a `union` and the algorithms `match` on it, as in Lean. Sums and
products hold lists of operands, as in mpl and Symbolism, because that's
what Cohen's algorithms work on. So far the operators build exactly
what's written, and nothing is simplified.
[000-expression-tree](design/implementation/000-expression-tree/README.md)
explains the choices.
