# 005 Go: a list literal of one union branch

**Status:** Draft, not filed.
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `a56f659c` (2026-10-25), first found
at `6af8f9f6` (2026-10-19)

Found while porting to the self-hosted compiler in
[series 001](../design/implementation/001-self-hosted/README.md):
`Sum terms: {(Sym name: "x")}` didn't build on Go, so MPL builds leaves
with `sym` and `num`, which return `Expr`. It's item 5 in the
[README](README.md), picked as next after #440. Re-checking it at
`a56f659c` showed it's broader than the README said: any value a match
has narrowed, such as `{this}` inside `| Num =>`, gives a list of the
branch too. Cohen's algorithms do that in most rules, starting with
the first one MPL needs, flattening a sum. No existing issue covered it
on 2026-10-25 (`gh search issues` for "list literal", "collection
literal", "union branch list", "cannot use", "slice", "covariance",
"covariant", "variance" and "List(Expr)").

Checked with small programs, each run on NIN's Go and Clojure backends,
on the JVM compiler (stage 1, `Niva/Niva`, at `a56f659c` too), and on
NIN with the spike described below:

| Program | NIN on Go | NIN on Clojure | JVM compiler | Spike, Go and Clojure |
|---|---|---|---|---|
| `Sum terms: {(Sym name: "x")}`, a field | doesn't build | works | works | works |
| `Sum terms: {this}` in `\| Num =>` | doesn't build | works | works | works |
| `terms + {this}` in `\| Sum =>`, a binary argument | doesn't build | `List__+` missing (item 17) | works | works on Go; item 17 on Clojure |
| `{(Num value: 1)}` as a keyword argument typed `List(Expr)` | doesn't build | works | works | works |
| the same, returned from `-> List(Expr)` | doesn't build | works | works | works |
| `l::List(Expr) = {(Num value: 1)}` | doesn't build | works | works | works |
| `l::mut List(Expr) = {(Num value: 1)}`, then `l add:` | doesn't build | run-time error: `List__mut_add expects an atom` | works | works |
| `#((Num value: 1))` as a `Set(Expr)` | doesn't build | works | works | works |
| `#{"a" (Num value: 1)}` as a `Map(String, Expr)` | doesn't build | works | works | works |
| `{{(Num value: 1)}}` as a `List(List(Expr))` | doesn't build | works | doesn't parse (`{{`) | works |
| an item from a method `-> Num` | doesn't build | works | works | works |
| `\| Num => {this}` and `\| Sym => {this}` as a `-> List(Expr)` method's value | type error: "Match branches return incompatible types" | the same | works | works |
| `l = {(Num value: 1)}`, then `Sum terms: l` | doesn't build | works | works | doesn't build on Go |
| items of two branches, `{(Num value: 1) (Sym name: "x")}` | works | works | works | works |
| an item declared `x::Expr` | works | works | works | works |

"Works" means it printed the right count. Two rows were new while
checking: the `mut` row fails on Clojure too, at run time, and the
match row is rejected by the type checker on both backends.

**Why.** The resolver types a literal from its items
(`mergeTypesByUnion`): items of one branch give that branch, items of
different branches their common union. The element type the context
expects is worked out just above and given to each item, but not used
for the literal itself. The type checker then accepts a `List(Num)`
where a `List(Expr)` is expected, because `isAssignableToSameKind`
compares type arguments with `isAssignableTo`: lists are covariant.
The Go backend writes the literal's own type, `[]main_Num{…}`, and Go's
slices aren't covariant. Clojure has no types to clash, but
`toCljExprForExpected` wraps a value in an atom only when its type is
the same collection type as the declared one, and `List(Num)` isn't
`mut List(Expr)`. The match row is the same thing on the type checker's
side: merging branch types finds a common union for the branches
themselves, not for lists of them, so `List(Num)` and `List(Sym)` don't
merge to `List(Expr)`. With the spike, each `{this}` is typed by the
method's return type first, so there's nothing to merge.

The JVM compiler gets every row right because Kotlin does both things
NIN doesn't: it infers `listOf`'s type argument from the expected type,
and its `List` is covariant. NIN on Go has neither.

**Spike.** In a scratch copy of `Niva/NivaInNiva` at `a56f659c`, built
with stage 1: a helper, `NivaType widenedTo: expected db:`, gives the
expected type when it's a union (`TypeT`) and the literal's element
type is assignable to it, and the literal otherwise keeps its own. Used
for lists and sets (`ListT t:`, `SetT t:`) and for a map's keys and
values, about 20 lines in `front/resolver/resolveExpr2.niva`. That's
the issue's option 1. It fixes every row but the untyped local, as
expected from the code. It only widens to a union, so a nullable or
`Any` element type is left as it is; those weren't tried. The helper:

```Scala
// a collection literal whose elements are branches of the union it is expected to hold gets that union as its element type, so {(Sym name: "x")} given as a List(Expr) is a List(Expr), not a List(Sym)
NivaType widenedTo(expected): NivaType? db: TyperDB -> NivaType = [
  ^ | expected
    | null => this
    |=> [
      | expected
      | TypeT => (this isAssignableTo: expected :db) ifTrue: [expected] ifFalse: [this]
      |=> this
    ]
]
```

and its uses: `elemType = f widenedTo: expectedElem :db` before the
list or set type is made from `elemType` instead of `f`, and
`MapT k: (first widenedTo: expectedKey :db) v: (second widenedTo:
expectedValue :db)`, with `expectedKey` and `expectedValue` taken from
`expectedMap`. A PR would rewrite it in Gavr's style (a comma cascade
for the `ifTrue:`).

Checked against stock `a56f659c`, each from an exported copy:

- `niva test`: 378 pass with and without the spike. The same 5 fail on
  both, all about binds (`cljBindByNameInstance`,
  `cljBindByNameStatic`, `cljPackageOutputKeepsBindRequirementWithQualifiedCall`,
  `goBindByNameStatic`, `irDependenciesExcludeExternalBindPackages`),
  likely from Gavr's binds refactor in `a56f659c` itself.
- `java -jar main.jar build --backend=go` on NIN's own sources builds
  with either jar, and the Go they generate is the same but for the
  `//line` paths: NIN's own code has no literal the spike retypes.
- The issue's three programs, run as they're shown (with the union
  above each, and a call to `operands`): the output below is from
  stock NIN. With the spike, and on the JVM compiler, all three print
  the right count on both backends. The third one doesn't build on Go
  either, at `*[]main_Num`. After the type error, and after the
  third's Go error, stock NIN also prints a Java exception, "Cant find
  niva line above 292 in file main/kotlin/Main.kt", from mapping its
  own stack trace; that's left out of the issue.

A PR would also need tests in `back/tests/goBackTests.niva`, as #436,
#437 and #440 had.

The options are about how far it should go. 1 is the spike. 2 and 3 are
about the untyped local, the one row 1 leaves. 3 is there because Go
already works that way. 2 has a catch for `mut` lists that 3 avoids.
The issue suggests 1 and leaves 2 and 3 for later, so there's one
decision: is 1 the right direction.

Everything below the line is the issue as it would be filed.

---

**Title:** [go] a list literal of one union branch doesn't build as a list of the union

On `--backend=go`, a list literal whose items are all one branch of a
union doesn't build where a list of the union is expected, though the
type checker accepts it:

```Scala
union Expr =
| Num value: Int
| Sym name: String
| Sum terms: List(Expr)

s = Sum terms: {(Sym name: "x")}
s terms count echo
```

```
main.niva:6: cannot use []main_Sym{…} (value of type []main_Sym) as []main_Expr value in struct literal
```

The same goes for a value a match has narrowed, such as `{this}`
inside `| Num =>`, for a set, a map and a nested list, and for a literal
given as an argument, returned, or assigned to a declared variable.
Items of different branches work, since they're merged to the union.

On both backends, the type checker rejects a match whose branches wrap
the narrowed value. With the same union above it:

```Scala
Expr operands -> List(Expr) = | this
| Sum => terms
| Num => {this}
| Sym => {this}
```

```
Resolver errors: main.niva:6:30 Match branches return incompatible types:  Type(List < Type(Expr )>),  Type(List < Type(Num value:  Type(Int))>),  Type(List < Type(Sym name:  Type(String))>)
```

The resolver types a literal from its items, so `{(Sym name: "x")}` is
a `List(Sym)`:

https://github.com/gavr123456789/Niva/blob/a56f659c071416f4836c8adfb1e89357d0565086/Niva/NivaInNiva/front/resolver/resolveExpr2.niva#L783-L811

though the element type the context expects is already worked out just
above, and given to each item:

https://github.com/gavr123456789/Niva/blob/a56f659c071416f4836c8adfb1e89357d0565086/Niva/NivaInNiva/front/resolver/resolveExpr2.niva#L762-L777

The type checker then accepts a `List(Sym)` as a `List(Expr)`, since it
compares type arguments with `isAssignableTo`:

https://github.com/gavr123456789/Niva/blob/a56f659c071416f4836c8adfb1e89357d0565086/Niva/NivaInNiva/front/resolver/compare2Types.niva#L60-L74

and the Go backend writes the literal's own type, `[]main_Sym{…}`,
which Go won't use as a `[]main_Expr`:

https://github.com/gavr123456789/Niva/blob/a56f659c071416f4836c8adfb1e89357d0565086/Niva/NivaInNiva/back/goBackend/goBack.niva#L1553-L1559

Clojure builds it, but loses a `mut` the same way:

```Scala
l::mut List(Expr) = {(Num value: 1)}
l add: (Sym name: "x")
```

fails at run time with `List__mut_add expects an atom`, because the
atom is only added when the value's type is the same collection type as
the declared one:

https://github.com/gavr123456789/Niva/blob/a56f659c071416f4836c8adfb1e89357d0565086/Niva/NivaInNiva/back/clojureBackend/cljEmit.niva#L112-L130

The JVM compiler runs all of these, since Kotlin infers `listOf`'s type
from the expected type, and its `List` is covariant.

I'm writing a computer algebra library, where wrapping a narrowed value
in a list, as `operands` does above, comes up in most rules. For now it
builds leaves with methods typed `-> Expr`. I'd like to send a PR, and
the question is how far it should go:

1. **Type a literal by what's expected of it.** Where the context gives
   a type (a field, an argument, a declared variable, a return type, an
   outer literal) and the items are branches of its union, the literal
   gets the union: `{(Sym name: "x")}` given as a `List(Expr)` is a
   `List(Expr)`. It's about 20 lines in the resolver, and fixes all of
   the above on both backends. A list with nothing expected of it,
   `l = {(Sym name: "x")}`, is still a `List(Sym)`, and passing it on as
   a `List(Expr)` still doesn't build on Go.
2. **1, and Go converts the rest.** Where a `List(Sym)` is passed as a
   `List(Expr)`, Go copies it into a new slice. That covers the last
   case, but a copy of a `mut` list doesn't see changes to the
   original, so a `mut List(Sym)` couldn't be passed as a
   `mut List(Expr)`.
3. **1, and the rest is a type error.** Passing a `List(Sym)` as a
   `List(Expr)` becomes an error that says to declare
   `l::List(Expr)`. That's how Go works, but the JVM compiler accepts
   it.

I'd suggest 1: it's small, changes no rule, and covers what comes up in
practice; 2 or 3 could follow for the last case. Would 1 be OK? I have
it working and can open it as a PR, or leave it to you.

Niva `self-host` at `a56f659c`, Go 1.26, OpenJDK 25, Babashka 1.13,
Linux. Found while writing a small computer algebra library in Niva.
Thanks for Niva!
