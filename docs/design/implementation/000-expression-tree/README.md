# 000 expression-tree

**Status:** Done; landed at `7cc033f` (2026-10-22).

The expression type, how to build and print expressions, and structural
equality: what the simplifier will work on. Also the project setup:
layout, `.gitignore`, the demo, tests, and this docs folder. Designed and
built in one conversation, so this README is the whole record.

## Shape

```
union Expr =
| Num     value: Int
| Sym     name: String
| Sum     terms: List::Expr
| Product factors: List::Expr
| Power   base: Expr exp: Expr
```

**One version, not two.** The Aloe MPL was planned as two variants:
Cohen-style like mpl and Symbolism, and algebraic data types like
symbolism.lean. Those differ in two separate ways:

1. **How behavior is organized.** Lean uses a closed type and pattern
   matching; Symbolism uses a class per kind of expression with its own
   methods and operators. Niva's only polymorphism is tagged unions, so a
   Symbolism-style version would still be a union and a `match`, with the
   methods spread over more files. That isn't a different design.
2. **How sums and products hold operands.** Lean's `Expr` is binary
   (`add a b`). mpl and Symbolism keep a list (`(+ a b c)`,
   `Product.elts`). Both work in Niva.

**Operand lists.** Cohen's algorithms work on operand lists: merging sums
and products, collecting like terms, sorting into canonical order. The
Lean version flattens binary nodes into lists and folds them back around
each step. Binary nodes versus lists is the comparison worth making in
Niva, if any, and it can wait until the simplifier works.

**Integers only.** Cohen's simplifier wants rational numbers and Niva has
none built in. A `Frac` branch, or a binding to `java.math.BigInteger`,
comes when a series needs it.

## Building and printing

- `3 num` and `"x" sym` make leaves. `Num value: 3` works too and is
  needed for negative numbers (see below).
- `+` and `*` build a two-operand `Sum` or `Product`, and `pow:` builds a
  `Power`. Nothing is simplified or flattened, so `x + y + z` is a sum
  inside a sum. The simplifier will turn that into one sum, and the demo
  can then print before and after.
- Printing puts an operand in parentheses unless it binds tighter than
  its parent: sums loosest, then products, then powers, then numbers and
  symbols. Because a nested sum keeps its parentheses, unsimplified input
  shows its shape: `(x + y) + z`. A negative number binds like a power,
  so `x^(-1)` and `(-2)^x` get parentheses and `x + -1` doesn't. An empty
  sum prints `0` and an empty product `1`.
- `same:` is structural equality, operands in order: `x + y` isn't the
  same as `y + x`. Niva's `==` compares identity.

## What we checked before building

The project began with a throwaway spike to answer these questions:

| Question | Answer |
|---|---|
| Can a union branch hold a list of the union? | Yes: `Sum terms: List::Expr` works. |
| What does `==` do on union values? | Compares identity. Hence `same:`. |
| How far does `match` go? | Branch type only, but matches nest. `same:` matches `this`, then `other`. |
| Does a custom `toString` work? | Only per branch, and only in the union's own file (see the README's Niva notes). |
| Operators? | `+` and `*` work on `Expr`, and `*` binds tighter. `^` is return and `**` isn't a selector, so power is `pow:`. No overloading by argument type, so no `2 + x`. |
| Can expressions be sorted? | `sortedWith:` with typed lambda parameters. `String` has no ordering, but Kotlin's `compareTo` can be bound with `@emit`. Canonical ordering of symbols will need that. |
| `niva run`, `niva test`? | Both work, and both exit 0 on failure. |

Two compiler bugs turned up while writing the real code: `-1 num`
miscompiles, and so does `List::Expr new`. They and the workarounds are
in the [README's Niva notes](../../../../README.md#niva-notes).

## Next

The first simplification rules: flattening nested sums and products,
adding and multiplying numbers, collecting like terms and factors, the
identities for 0 and 1, and canonical operand order. That needs an
ordering on expressions, Cohen's order relation, and the `String`
comparison binding.
