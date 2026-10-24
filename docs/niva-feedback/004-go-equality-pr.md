# 004 Go: `==` by content, Gavr's option 3, as a draft pull request

**Status:** Opened 2026-10-24 as draft PR
[#440](https://github.com/gavr123456789/Niva/pull/440). Meant as
something concrete to discuss, not to push Gavr toward this option: he
hasn't picked one. The body links the issue with "For #439" rather than
"Fixes #439", since it's for discussion. #439 had no comments on GitHub
when it was opened, and `self-host` was still at `390d06b6`. The PR has
no CI checks.
**Issue:** [#439](https://github.com/gavr123456789/Niva/issues/439) ([draft](004-go-equality.md))
**Branch:** [`dharmatech/Niva:go-structural-eq`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:go-structural-eq),
two commits on `self-host` at `390d06b6`: `9f4a4506` and `4ae4dc44`.
Locally it's `go-structural-eq` in `~/src/Niva-self-host-go-value`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

After #439 was filed, Gavr listed his own options on Discord. His 3:
always compare by structure, and generate a compare function for each
type with a collection in a field, since Go can't compare collections,
with sizes compared first to keep the cost down. This sketches that,
plus the two things the issue said any option needs: `==` on lists, and
calling a type's own `==`.

How it was checked, beyond what the PR says:

- Stage 2 was rebuilt at `390d06b6` first: 371 tests pass, and the
  issue's programs and the ones from verifying it give the same output
  as at `3746ffb7`.
- Each commit was checked from an exported copy of it, so later edits
  couldn't leak in: it builds (`niva build`), passes 377 and then 380
  tests, and `java -jar main.jar build --backend=go` on a copy of
  `Niva/NivaInNiva` builds `.niva_go/build/niva_go`. mpl-niva's 31
  checks pass on Go with both. The Clojure check fails at README item
  3, as on stock `self-host`.
- The programs: the 15 from `eq-handoff` and `eq-verify`, plus
  `collections` (lists of lists and of union values, sets, maps),
  `shapes` (nullable, `mut` and nullable `Int` fields, a set, a map of
  types, nested unions, a branch against its union), `customEqBranch`
  (a `==` declared on a union, called on two branch values, and `!=`)
  and `customNe` (only `!=` declared). On Go they now print what Clojure
  prints, except `contains:`, which neither runtime has (README item 22).
  The three with a declared `==` or `!=` print what the JVM compiler
  prints too.
- The compiler built for Go gets no compare function: it never compares
  such types. Its Go is the same as a stock jar's on the same sources,
  apart from the 43 new lines in `core.go` and the sketch's own code.
- The first version generated a function for every type that needs
  one: 181 for the compiler, none of them called, and they all built.
  That's in the PR as a check of the generator; the sketch only
  generates one for types some `==` reaches.
- A copy of `tests/equalityChecks.niva` with `==` in place of `same:`
  passes all 31 checks on Go, through the generated
  `mpl_tree_Expr__eq`. MPL itself wasn't changed.
- `o == o` for `Op f: [it + 1]` is `false` on Go and `true` on
  Clojure; that's open point 1 below.
- `~/src/Niva-self-host` and `~/src/Niva-self-host-clj-return` were left
  alone.

Side findings, added to the [README](README.md) list as items 23 to 25
rather than to this PR:

- **Go:** `ifNotNull:` on a nullable union, map or list value doesn't
  build: "type map[string]… of fieldsMaybe does not match *T (cannot
  infer T)". It expects a pointer, which only nullable structs and
  numbers are. Found writing the sketch, which uses `| x | null =>`
  instead, as the rest of NIN does.
- **Go:** two nullable locals compare as pointers: `x::Int? = 5`,
  `y::Int? = 5`, `x == y` is `false` (Clojure `true`), and `x == 5`
  doesn't build ("mismatched types *int and untyped int"). The same
  family as #439, at the top level instead of in a field; the sketch
  leaves it, since a narrowed nullable is already read through.
- A nullable block type, `f: [Int -> Int]?`, doesn't parse: "Got invalid
  token after CloseBracket".

Everything below the line is the pull request as opened.

---

**Title:** [go] == compares by content, with a generated compare function where go's == can't (#439)

A sketch of the third option you listed on Discord for #439, as a
draft, to have something concrete to discuss. You don't have to take
it, and if another option fits better, that's fine. For #439.

`==` compares by structure by default. On Go, a type whose values Go's
`==` can't compare by content gets a generated compare function, which
`==` and `!=` call. Every other type keeps Go's `==`. On Clojure, `=`
already compares by content, so only the list fix and the second commit
touch it.

With the union from the issue,

```Scala
union Expr = | Num v: Int | Sum terms: List(Expr)
Int num -> Expr = Num v: this
a::Expr = Sum terms: {(1 num) (2 num)}
b::Expr = Sum terms: {(1 num) (2 num)}
(a == b) echo
```

`a == b` becomes `main_Expr__eq(a, b)`, and prints `true`:

```go
func main_Sum__eq(a, b main_Sum) bool {
    return core.List__equals(a.terms, b.terms, main_Expr__eq)
}

func main_Expr__eq(a, b main_Expr) bool {
    switch x := a.(type) {
    case main_Sum:
        y, ok := b.(main_Sum)
        return ok && main_Sum__eq(x, y)
    }
    return a == b
}
```

**Where to start reading:** `back/goBackend/goEquality.niva`, from
`goEqTypeIdsFor`.

A type or union branch gets a function when a field holds something
Go's `==` can't compare by content: a list, set or map, a block, a type
that gets a function itself, or a nullable or `mut` struct field. Those
two are Go pointers, so `==` compared addresses, and two equal
`Holder p: Point?` were `false`. A union gets one when a branch does.
Only types some `==` reaches get a function, so the compiler built for
Go gets none. As a check, generating one for every type that needs one
gave 181 for the compiler's own types, and they all build.

On the cost:

- a function compares the fields Go's `==` can compare first, then the
  rest,
- `core.List__equals`, `Set__equals` and `Map__equals` compare sizes
  first,
- a union compares the branch first, and leaves branches Go's `==` can
  compare to it,
- and a type can declare its own `==` (the second commit), for when
  comparing by content costs too much or isn't what the type means.

Open points, with what the sketch does:

1. **Blocks** have nothing to compare, so two are equal only when both
   are null. That makes `o == o` `false` for `o = Op f: [it + 1]`,
   where Clojure says `true` (the same function). A compile error would
   be the alternative.
2. **`mut` fields** compare the values they hold, as on Clojure, not
   the cells.
3. **Generic types** keep Go's `==`: the compare for `T` isn't known
   where the type is declared, so `Box(List(Int))` with `==` still
   doesn't build. A compare function per type parameter, passed where
   `==` is called, would cover them.
4. **Out of scope:** a match on a value, map keys and set items still
   use Go's `==`, and so does `==` on two nullable locals, which on Go
   are pointers.

Two commits, each building and passing the tests:

1. **`==` compares by content on Go**, as above. `{1 2} == {1 2}` was a
   type error on both backends, since `==` and `!=` on a list took a
   `String` (`collection.niva`); now they take the list.
2. **A type's own `==` and `!=` are called**, as on the JVM compiler
   since dfd22100, on both backends, including one declared on a union
   for its branches. `!=` negates the type's `==` when it has no `!=`.
   It's the last commit, so you can drop it.

`niva test`: 380 pass, 9 of them new. `niva build` and
`java -jar main.jar build --backend=go` work. The programs from #439
print on Go what they print on Clojure, and my computer algebra
library's checks pass with `==` in place of its own `same:`.
