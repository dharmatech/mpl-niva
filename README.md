# MPL in Niva

A small computer algebra simplifier written in
[Niva](https://github.com/gavr123456789/Niva), to exercise the language.

It follows a line of simplifiers based on Joel S. Cohen's *Computer
Algebra and Symbolic Computation* books:

- [mpl](https://github.com/dharmatech/mpl), for R6RS Scheme
- [Symbolism](https://github.com/dharmatech/Symbolism), for C#, based on mpl
- [symbolism.lean](https://github.com/dharmatech/symbolism.lean), a small
  simplifier in Lean
- `examples/mpl` in [Aloe](https://github.com/dharmatech/2026-09-02-aloe-racket),
  a small simplifier in Aloe

Niva's only polymorphism is tagged unions, so an expression is a `union`
and the algorithms `match` on it, as in the Lean version. Sums and
products hold a list of operands, as in mpl and Symbolism, because
Cohen's algorithms work on operand lists.

**Status:** expressions can be built, printed and compared. Nothing is
simplified yet.

## Running

You need a JDK (this was built with OpenJDK 25) and the Niva compiler,
built as in [Niva's INSTALL.md](https://github.com/gavr123456789/Niva/blob/main/INSTALL.md).
Niva changes quickly; this was tested with Niva commit `402e02a6`
(2026-10-20).

| Run | What |
|---|---|
| `niva run` | Builds a few expressions and prints each beside the Niva that built it. |
| `niva test` | The tests in `tests/`. |

`niva run` and `niva test` exit 0 even when the build or a test fails.
Read the output: a failure says `FAILED`. If nothing changed since the
last `niva test`, Gradle skips the tests (`:test UP-TO-DATE`) and lists
no results.

```
x + 2 num * y   =>   x + 2 * y
x + y + z   =>   (x + y) + z
(x + y) pow: 2 num   =>   (x + y)^2
```

## Layout

| Path | What |
|---|---|
| `mpl/tree.niva` | `Expr`, the operators that build it, and printing. |
| `mpl/equality.niva` | `same:`, structural equality. |
| `main.niva` | The demo that `niva run` runs. |
| `tests/` | `niva test`. `assert.niva` comes from [boids-niva](https://github.com/dharmatech/boids-niva). |
| `docs/design/implementation/` | One folder per change, numbered in order, recording what was done and why. |

## Building expressions

```
x = "x" sym
y = "y" sym
e = x + 2 num * (y pow: 3 num)
```

`*` binds tighter than `+`, as in Niva's arithmetic. Keyword messages
bind loosest, so `x + y pow: 2 num` is `(x + y)^2`; write
`x + (y pow: 2 num)`.

The operators build exactly what's written. `x + y + z` is a sum inside
a sum and prints as `(x + y) + z`.

## Niva notes

Found while building this, with Niva `402e02a6`:

- `==` on union values compares identity: `"x" sym == "x" sym` is false.
  Use `same:`.
- A `toString` on a union isn't used when printing; define it on each
  branch. Niva copies each branch's `toString` into that branch's Kotlin
  class, in the union's file, so whatever it calls must be defined in
  that file too.
- `^` is return and `**` isn't a valid selector, so power is the
  keyword message `pow:`.
- There's no overloading on the argument type: `Expr + Expr` and
  `Expr + Int` can't both exist, and `Int + Expr` clashes with
  `Int + Int`. Hence `2 num`.
- **Compiler bug** ([#430](https://github.com/gavr123456789/Niva/issues/430)):
  a negative literal as the receiver of a unary message, as in `-1 num`,
  becomes Kotlin `-1.num()`, which is `-(1.num())`. Write `(-1) num` or
  `Num value: -1`.
- **Compiler bug:** `List::Expr new` generates broken Kotlin. Use a typed
  empty literal: `none::List::Expr = {}`.
- Lists have no `all:`; use `any:` and `not`.
- `String` has no `<` or `compare`. It can be bound to Kotlin's
  `compareTo` with `@emit`.
- A comparator lambda needs typed parameters: `sortedWith: [a: Expr,
  b: Expr -> ...]`.
- Inside a method on `List`, write `this count`, not `count`.

[boids-niva's README](https://github.com/dharmatech/boids-niva#niva-gotchas-hit-along-the-way)
has more.
