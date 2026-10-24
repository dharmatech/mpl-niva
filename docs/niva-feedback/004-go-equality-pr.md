# 004 Go: `==` by content, Gavr's option 3, as a draft pull request

**Status:** Opened 2026-10-24 as draft PR
[#440](https://github.com/gavr123456789/Niva/pull/440). Meant as
something concrete to discuss, not to push Gavr toward this option: he
hasn't picked one. The body links the issue with "For #439" rather than
"Fixes #439", since it's for discussion. #439 had no comments on GitHub
when it was opened, and `self-host` was still at `390d06b6`. The PR has
no CI checks. Gavr commented the same day; the fixes for it were pushed,
with a reply and an updated body, at 15:45 UTC. See [Review](#review).
**Issue:** [#439](https://github.com/gavr123456789/Niva/issues/439) ([draft](004-go-equality.md))
**Branch:** [`dharmatech/Niva:go-structural-eq`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:go-structural-eq),
two commits on `self-host` at `390d06b6`: `9f4a4506` and `4ae4dc44`.
For his review: `9f22d4a6`, `daa2cc53`, `8169174b`, then `246b1f71`
merging `self-host` at `200aa83a`, and `e06c689d` and `ebe1c0f2` for
that, pushed as a fast-forward to `ebe1c0f2`.
Locally it's `go-structural-eq` in `~/src/Niva-self-host-go-value`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

## Review

**Gavr's comment** (2026-10-24,
[comment](https://github.com/gavr123456789/Niva/pull/440#issuecomment-6385428940)),
on the open points: 2 (`mut` fields compare values) "yes". 3 (generic
types): "I dont see another way", since `reflect.DeepEqual` won't use a
custom `==` and `T comparable` works for `Box` but not for a list. On 1
(blocks): compare functions by their number of args, then each arg type
and the return type, "funny since such comparison for types already
implemented in type checker". Then three examples, "for tests
probably":

- `x::List(Int)? = {}`, `y::List(Int)? = null`, `x == y`: Clojure
  `false`, Go `true`.
- `o == o` for `Op f: [it + 1]`: Clojure `true`, Go `false`.
- `a == b` for two `Box(List(Int))`: Clojure `true`, Go doesn't build.

**The fixes,** three commits on top, then a merge:

- `9f22d4a6` [go] a null list, set or map isn't equal to an empty one: a
  nullable collection checks that both or neither are nil first. For
  that, lists the Go runtime builds are never nil: `filter`, `sortedBy`,
  `chunked` and the range `map:` and `filter:` started from a nil slice,
  so an empty result looked like null. A `filter:` with no match against
  `null` is `false` and against `{}` `true`, as on Clojure.
- `daa2cc53` [go] two blocks of the same type are equal: on Go both
  sides of `==` have the same type, which fixes the signature already,
  so it's a nil check. `o == o` is `true`, but so is
  `(Op f: [it + 1]) == (Op f: [it * 2])`, which Clojure says is `false`,
  since its `=` compares functions by identity. That's the question in
  the reply.
- `8169174b` [go] generic types get a compare function too, with one for
  each type param: `func main_Box__eq[T any](a, b main_Box[T], eqT
  func(T, T) bool) bool`, called with the type args' compare functions.
  `Box(Int)` passes `core.Eq[int]`. A generic union gets one too, but
  NIN's type checker doesn't take a branch value as a generic union
  value (`r::Result(Int) = Ok t: 1` is rejected, as is returning one
  from a method, on stock `390d06b6` too), so only generic branches were
  run; the union function's Go was compiled and run by hand. In a
  generic method, where T's compare isn't known, values of T compare
  with Go's `==` through `any` (`core.EqAny`): `T isSame: other::T ->
  Bool = this == other` didn't build before.

Checked, each commit from an exported copy as before:

- 381, 381 and 383 tests pass, against 380 before; four new tests (one
  for null collections, three for generic types), and the block test
  changed.
- `niva build` and `java -jar main.jar build --backend=go` work at each.
  The compiler still gets no compare function. The first version of the
  generic commit broke the compiler's own Go build: it used `contains:`
  on a list, which isn't in the Go runtime (README item 22). It was
  amended to use `any:` before pushing.
- mpl-niva's 31 checks pass on Go at each, and so does the copy with
  `==` in place of `same:`.
- His three examples, as programs (`nullList` with an empty `filter:`
  and a map added, `blockSelf` with a second, different block,
  `genericBox`, and `genericMore` with nested boxes, a generic branch,
  a two-param type and a list of boxes): Go prints what Clojure prints,
  except two different blocks (`blockSelf`'s second line and the older
  `withBlock`), and `contains:`.

**Gavr's `200aa83a`** landed on `self-host` meanwhile, "fix generic
params inference, fix `==` on collection protocol": the same
`collection.niva` change as ours, and `IrCall` gets `genericArgs`. Git
merges the branch cleanly, but the call `irOp` builds for a declared `==`
then misses `genericArgs` and stage 2 doesn't build ("not all fields are
listed, you missed: genericArgs"). So the branch merges `self-host`
(`246b1f71`, no force-push) and passes it (`e06c689d`). Echo calls now
get type args (`core.Any__echo[bool](...)`), so eight of our tests no
longer matched; they check only the comparison now (`ebe1c0f2`).
`200aa83a` itself fails 6 Go tests (365 pass): `goImplicitLambdaArgCodegen`,
`goMatchInsideValueIsLiftedCodegen`, `goStatementMatchOnTrueWithConditions`,
`goUnitMatchBranchResultIsEmitted`, `goCoreAnyMethodOwnerCodegen` and
`goUnitMatchValueRunsBranchesAsStatements`. At the tip, 377 pass and
the same 6 fail. The compiler builds for Go, mpl-niva's 31 checks pass
(with `==` for `same:` too), and the cases are as at `8169174b`. The
generic union limit is still there.

**Reply,** posted with the push
([comment](https://github.com/gavr123456789/Niva/pull/440#issuecomment-6386198559)),
with each paragraph on one line, since GitHub shows a newline in a
comment as a line break:

> Thanks! Three commits for your examples, each with a test:
>
> - 9f22d4a6: a null list, set or map isn't equal to an empty one, so
>   your first example is `false` now. For that, lists the Go runtime
>   builds (`filter:`, `sortedBy:`, `chunked:`, and a range's `map:` and
>   `filter:`) are never nil, so an empty one doesn't look like null.
> - daa2cc53: two blocks of the same type are equal. On Go both sides
>   of `==` have the same type, which already fixes the signature, so
>   it's a nil check, and `o == o` is `true`.
> - 8169174b: a generic type's compare function takes one for each type
>   param, `func main_Box__eq[T any](a, b main_Box[T], eqT func(T, T)
>   bool) bool`, and `==` passes the type args' ones, so your `Box`
>   example prints `true`. In a generic method, where T's compare isn't
>   known, values of T compare with Go's `==` through `any`; that didn't
>   build before.
>
> One question on blocks: Clojure's `=` compares functions by identity,
> so `(Op f: [it + 1]) == (Op f: [it * 2])` is now `true` on Go and
> `false` on Clojure. Should Clojure compare them by signature too?
>
> Generic unions get the same function, but I couldn't run it: the type
> checker doesn't take a branch value as a generic union value yet
> (`r::Result(Int) = Ok t: 1` is rejected), so only generic branches
> were tested.
>
> I also merged self-host: since 200aa83a `IrCall` has `genericArgs`, so
> the call to a type's own `==` needed it (e06c689d), and echo gets type
> args, so my tests check only the comparison now (ebe1c0f2). Heads-up:
> 6 Go tests fail on self-host at 200aa83a itself:
> `goImplicitLambdaArgCodegen`, `goMatchInsideValueIsLiftedCodegen`,
> `goStatementMatchOnTrueWithConditions`,
> `goUnitMatchBranchResultIsEmitted`, `goCoreAnyMethodOwnerCodegen` and
> `goUnitMatchValueRunsBranchesAsStatements`. I left them alone.

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

Everything below the line is the pull request as opened, with the
changes for his review: open points 1 and 3, the null collections, the
commit list and the test count. The body was updated to this with
`gh api -X PATCH` when the fixes were pushed, with each paragraph on one
line.

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
parameter, a type that gets a function itself, or a nullable or `mut` struct field. Those
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

1. **Blocks** compare by their signature, as you suggested: on Go both
   sides of `==` have the same type, which fixes it already, so two
   blocks are equal unless one is null. On Clojure, `=` still compares
   functions by identity.
2. **`mut` fields** compare the values they hold, as on Clojure, not
   the cells.
3. **Generic types** get a compare function that takes one for each
   type parameter, passed where `==` is called, so `Box(List(Int))`
   compares by content and `Box(Int)` passes `core.Eq[int]`. In a
   generic method, where the compare for `T` isn't known, values of `T`
   compare with Go's `==` through `any`.
4. **Out of scope:** a match on a value, map keys and set items still
   use Go's `==`, and so does `==` on two nullable locals, which on Go
   are pointers.

Five commits, each building and passing the tests, then a merge of
`self-host` with two follow-ups for 200aa83a:

1. **`==` compares by content on Go**, as above. `{1 2} == {1 2}` was a
   type error on both backends, since `==` and `!=` on a list took a
   `String` (`collection.niva`); now they take the list, the same change
   as in your 200aa83a.
2. **A type's own `==` and `!=` are called**, as on the JVM compiler
   since dfd22100, on both backends, including one declared on a union
   for its branches. `!=` negates the type's `==` when it has no `!=`.
   If you don't want it, I'll take it out.
3. **A null list, set or map isn't equal to an empty one**, from your
   review. Lists the Go runtime builds are never nil now, so an empty
   one doesn't look like null.
4. **Two blocks of the same type are equal**, from your review.
5. **Generic types get a compare function too**, from your review.

`niva test`: 377 pass, 12 of them new. The other 6 fail on `self-host`
at 200aa83a too. `niva build` and
`java -jar main.jar build --backend=go` work. The programs from #439
print on Go what they print on Clojure, and my computer algebra
library's checks pass with `==` in place of its own `same:`.
