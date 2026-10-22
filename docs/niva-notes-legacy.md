# Niva notes: the legacy compiler

Found while building [series 000](design/implementation/000-expression-tree/README.md)
with the Kotlin compiler at Niva `402e02a6` (2026-10-20). The project
moved to the self-hosted compiler in
[series 001](design/implementation/001-self-hosted/README.md); its notes
are in [Niva notes](niva-notes.md). This page is history.

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
- `*` binds tighter than `+`, as in arithmetic.
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
- `niva run` and `niva test` exit 0 even when the build or a test fails.
  If nothing changed since the last `niva test`, Gradle skips the tests
  (`:test UP-TO-DATE`) and lists no results.

[boids-niva's README](https://github.com/dharmatech/boids-niva#niva-gotchas-hit-along-the-way)
has more.
