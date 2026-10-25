# 005 Go: a list literal of one union branch, option 1 as a draft pull request

**Status:** Opened 2026-10-25 as draft PR
[#450](https://github.com/gavr123456789/Niva/pull/450), right after the
issue, linking it with "For #449" rather than "Fixes #449", since the
issue asks which option Gavr prefers. No CI checks. Each paragraph went
in on one line, since GitHub shows a newline as a line break.
**Issue:** [#449](https://github.com/gavr123456789/Niva/issues/449) ([draft](005-go-list-literal-of-branch.md))
**Branch:** [`dharmatech/Niva:literal-expected-type`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:literal-expected-type),
one commit on `self-host` at `a56f659c`: `e01dc22d`. Locally in
`~/src/Niva-self-host-literal`, a worktree of `~/src/Niva-self-host`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

The issue's option 1, built from the spike in the issue draft and
rewritten in Gavr's style: `.isAssignableTo:` with a comma cascade, the
helper as a match without brackets, and `itemType` for the list's item
type. The map path now names the expected key and value types once and
uses them for the items too, in place of the two inline matches it had.
Same behavior as the spike.

**Order of filing.** The issue says a draft PR shows option 1, and the
PR says "For #449", so one of them had to be edited after the other
existed:

1. The issue was filed saying "I'm opening a draft PR for 1 right after
   this issue", rather than a `#PR` placeholder, since GitHub emails the
   body as filed.
2. The branch was pushed, and the PR opened as a draft with the issue's
   number in its title and body.
3. The issue's sentence was changed to "Draft PR #450 shows what 1 looks
   like" with `gh api -X PATCH`.

GitHub also links the two on its own, since the PR mentions the issue.

## What was checked

On 2026-10-25, each from an exported copy as before, with stage 1 at
`a56f659c`:

- `niva test`: 381 pass on the branch, against 377 on stock
  `a56f659c`, so the 4 new tests pass and nothing else changed. The same
  6 fail on both: the 5 bind tests from the day before, and
  `babashkaHostLoadsRecoversAndWatchesSources`, which passed on both
  stock and the spike on 2026-10-24 and failed on both stock and the
  branch here, twice. It sleeps and watches files, and the failing
  assertion is an exit code; no stray `bb` was running.
- The 4 new tests fail without the change: with only the test files
  added to stock, 377 pass and 10 fail, the 4 among them.
- `java -jar main.jar build --backend=go` on the branch's sources
  builds, and the Go-built compiler gets the issue's three programs
  right. Stock sources compiled with the branch's jar give the same Go
  as with the stock jar, but for the `//line` paths.
- The issue draft's programs give what the spike gave: every row works
  but the untyped local.
- mpl-niva's 31 checks pass on Go with the branch
  (`NIVA_SELF_HOST=~/src/Niva-self-host-literal tools/check`), and the
  demo prints as before.

Not covered: a generic union, since writing `Result(Int)` as a type
fails first (#447), and a nullable or `Any` item type, which the change
leaves as it was.

Everything below the line is the pull request as opened.

---

**Title:** [resolver] a collection literal of union branches takes the union it's expected to hold (#449)

Option 1 from #449, as a draft, to show what it looks like. You don't
have to take it, and if another option fits better, that's fine.
For #449.

A collection literal was typed from its items alone, so
`{(Sym name: "x")}` is a `List(Sym)` even where a `List(Expr)` is
expected, and Go won't take a `[]main_Sym` there. Now, when the context
expects a union and the items are branches of it, the literal gets the
union:

```Scala
union Expr = | Num v: Int | Sym name: String | Sum terms: List(Expr)
s = Sum terms: {(Sym name: "x")}
```

```go
var s main_Sum = main_Sum{[]main_Expr{main_Sym{"x"}}}
```

It was `[]main_Sym{main_Sym{"x"}}`, which didn't build. The same goes
for `{this}` in `| Num =>`, for sets and maps, and on Clojure for
`l::mut List(Expr) = {(Num v: 1)}`, which lost its atom. A match whose
branches are `{this}` in a method `-> List(Expr)` no longer fails with
"Match branches return incompatible types".

**Where to look:** `widenedTo:` in `front/resolver/resolveExpr2.niva`,
used for a list's or set's item type and for a map's key and value
types. The expected type is the one `resolveCollectionElements` already
gets: from a field, an argument, a declared variable, a return type or
an outer literal. A literal with nothing expected of it keeps its
items' type, so `l = {(Sym name: "x")}` is still a `List(Sym)`, and
passing it on as a `List(Expr)` still doesn't build on Go; that's
options 2 and 3 in the issue. Only a union is widened to, so a nullable
or `Any` item type is left as it was. A generic union can't be tried
until #447.

Four tests, each failing without the change: in `resolverTests.niva`,
the types of a list, set and map given as the union, and of one with
nothing expected, and the match; in `goBackTests.niva`, the generated
literals; in `cljBackTests.niva`, the atom.

`niva test`: 381 pass, 4 of them new. The 6 that fail fail on
`self-host` too: the 5 bind tests, and
`babashkaHostLoadsRecoversAndWatchesSources`, which fails on this
machine without the change as well. `niva build` and
`java -jar main.jar build --backend=go` work, and the compiler's own Go
is unchanged.
