# 004 Go: `==` on a type that holds a list

**Status:** Filed 2026-10-23 as [#439](https://github.com/gavr123456789/Niva/issues/439).
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `3746ffb7` (2026-10-23), first found
at `6af8f9f6` (2026-10-19)

Found while porting to the self-hosted compiler in
[series 001](../design/implementation/001-self-hosted/README.md): `==`
on two sums failed on Go, so MPL compares expressions with its own
structural `same:` ([`mpl/equality.niva`](../../mpl/equality.niva),
checked in [`tests/equalityChecks.niva`](../../tests/equalityChecks.niva)).
It's item 7 in the [README](README.md), picked as next after #437: a
computer algebra library compares expressions all the time, and the
compilers disagree on what `==` means, which NIN needs settled before it
replaces the JVM compiler. No existing issue covered it on 2026-10-23
(`gh search issues` for "equality", "==", "equals", "data class",
"equal_niva", "comparing uncomparable" and "struct containing"), and
Niva's README doesn't say what `==` means.

Re-checked on `3746ffb7` with small programs, each run on NIN's Go and
Clojure backends and on the JVM compiler (stage 1, `Niva/Niva`):

| Program | NIN on Go | NIN on Clojure | JVM compiler |
|---|---|---|---|
| two equal `Point`s, unequal `Point`s, two equal `Circle`s | `true false true` | `true false true` | `false false false` |
| two equal `Line`s of `Point`s, then `!=` | `true false` | `true false` | `false true` |
| two equal `Bag items: {1 2}` | doesn't build | `true` | `false` |
| the same with a `Set`, a `Map` | doesn't build | `true` | `false` |
| the same with a block, `Op f: [it + 1]` | doesn't build | `false` | `false` |
| two equal `Sum`s, unequal `Sum`s | panics | `true false` | `false false` |
| `p == p` | `true` | `true` | `true` |
| `{1 2} == {1 2}`, `{1 2} != {1 3}` | type error | type error | `true true` |
| `Point` declaring `==` on `x` only: `(1, 2) == (1, 3)`, `!=` | `false true` | `false true` | `true false` |
| `Expr` declaring `==`, two equal `Sum`s | panics | `true` | `true` |

The rows for a set, a map, a block and a declared `==` were new while
verifying. The JVM compiler calls a type's own `==` (`equal_niva`, since Gavr's `dfd22100`, 2026-02-11); NIN
compiles it to a function but never calls it, since its IR makes every
`==` a native comparison. NIN's own `libs/binds/io.niva` declares one
for `Path`. So the issue says all three options call a type's own `==`,
and option 1 builds on that: MPL would declare `Expr ==` with `same:`'s
body.

The options are about what `==` means for a type without its own,
smallest first, as for #432 and #435. The issue suggests 1: it changes
the least, keeps what Go and Clojure do today, and turns the crash into
an error that says what to do. 3 is there because it's what the JVM
compiler does; the Go backend's `isGoPointerValue` keeps `mut` values
as pointers to mirror "the reference semantics of the jvm and clojure
backends", so Gavr may think of Niva values as references.

Not verified: neither 1 nor 2 is spiked, and that fixing the `String`
argument alone would still fail on Go is read from the code (`IrOpBin`
becomes Go's `==`), not built.

After it was filed, on Discord: "an interesting question", and with
NIN's second backend "such little differences appears more and more".
Comparing two collections item by item is costly, so he'd say the user
should define it, and he's checking what other languages do. No
comments on GitHub yet. That leans to option 1, but may go further
than the issue does: all three options compare lists item by item,
and "user should define it" could mean `{1 2} == {1 2}` too, which Go
itself rejects for slices.

Everything below the line is the issue as filed.

---

**Title:** [go] `==` on a type that holds a list doesn't build, or panics

On `--backend=go`, `==` on a type that holds a list passes the type
checker, then doesn't build for a plain type, and panics at run time
for union values:

```Scala
type Bag items: List(Int)
b1 = Bag items: {1 2}
b2 = Bag items: {1 2}
(b1 == b2) echo
```

```
main.niva:4: invalid operation: b1 == b2 (struct containing []int cannot be compared)
```

```Scala
union Expr = | Num v: Int | Sum terms: List(Expr)
Int num -> Expr = Num v: this
a::Expr = Sum terms: {(1 num) (2 num)}
b::Expr = Sum terms: {(1 num) (2 num)}
(a == b) echo
```

```
runtime error: comparing uncomparable type main.main_Sum
```

The IR makes every `==` a native comparison:

https://github.com/gavr123456789/Niva/blob/3746ffb7d15db884a49bcf75dee95e646edb1e3e/Niva/NivaInNiva/ir/fromTypedAst.niva#L863-L869

and Go gets its own `==`:

https://github.com/gavr123456789/Niva/blob/3746ffb7d15db884a49bcf75dee95e646edb1e3e/Niva/NivaInNiva/back/goBackend/goBack.niva#L1237-L1241

Go compares structs field by field, but not one that holds a slice, and
two interface values (a union) that hold such structs panic. The same
goes for a set, a map or a block in a field. On Clojure, `==` becomes
`=`, which compares records by content, so both print `true`.

Underneath, the compilers don't agree on what `==` means:

| | Go | Clojure | JVM compiler |
|---|---|---|---|
| two equal `Point`s | true | true | false |
| two equal `Bag`s | doesn't build | true | false |
| two equal `Sum`s | panics | true | false |
| `p == p` | true | true | true |
| `{1 2} == {1 2}` | type error | type error | true |

The JVM compiler emits each type as a plain Kotlin `class`, so `==` is
identity:

https://github.com/gavr123456789/Niva/blob/3746ffb7d15db884a49bcf75dee95e646edb1e3e/Niva/Niva/src/main/kotlin/codogen/declarations/TypeDeclaration.kt#L25-L28

and a type can declare its own `==`, which it calls instead (dfd22100):

https://github.com/gavr123456789/Niva/blob/3746ffb7d15db884a49bcf75dee95e646edb1e3e/Niva/Niva/src/main/kotlin/codogen/expressions/MessageSend.kt#L687-L696

NIN compiles a declared `==` but doesn't call it:

```Scala
type Point x: Int y: Int
Point == other::Point -> Bool = x == other x
p1 = Point x: 1 y: 2
p2 = Point x: 1 y: 3
(p1 == p2) echo // JVM compiler: true, Go and Clojure: false
```

The same goes for `Path ==` in `libs/binds/io.niva`. And
`{1 2} == {1 2}` is a type error, "wrong arg type for == expected
String", because `==` and `!=` on collections take a `String`:

https://github.com/gavr123456789/Niva/blob/3746ffb7d15db884a49bcf75dee95e646edb1e3e/Niva/NivaInNiva/front/resolver/protocols/collection.niva#L22-L23

Fixing that alone would run into Go's `==` again, which can't compare
slices.

I'm writing a computer algebra library, where comparing expressions
like the `Sum`s above is basic; for now it has its own `same:`. I'd
like to send a PR, and the question is what `==` should mean in NIN
for a type that doesn't declare its own. In all three options, NIN
would call a type's own `==` when it has one, as the JVM compiler does,
and lists would compare item by item.

1. **Content, and a type error where Go can't compare.** Types keep
   comparing by content, as on Go and Clojure today. `==` on a type
   that holds a collection or a block, in a field or a union branch,
   and has no `==` of its own, becomes a type error saying to declare
   one. My library would declare `Expr ==` with `same:`'s body.
2. **Content everywhere,** as on Clojure. Go compares types that hold
   collections too, with an equality function generated per type.
   Nothing to declare.
3. **Identity,** as on the JVM compiler. Two equal `Point`s are
   different. On Go, types are values, so this would need a pointer or
   an id per value; Clojure would use `identical?`.

I'd suggest 1: it's the smallest change, keeps what works today, and
turns the crash into an error that says what to do. Unless it's 3,
NIN and the JVM compiler would still differ on two equal `Point`s.
Which would you prefer? I'm happy to open a PR, or to leave it to you.

Niva `self-host` at `3746ffb7`, Go 1.26, OpenJDK 25, Babashka 1.13,
Linux. Found while writing a small computer algebra library in Niva.
Thanks for Niva!
