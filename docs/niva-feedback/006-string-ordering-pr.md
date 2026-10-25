# 006 `String` ordering, option 1 as a draft pull request

**Status:** Opened 2026-10-25 as draft PR
[#452](https://github.com/gavr123456789/Niva/pull/452), right after the
issue, linking it with "For #451", as 005 was. No CI checks. Each
paragraph went in on one line.
**Issue:** [#451](https://github.com/gavr123456789/Niva/issues/451) ([draft](006-string-ordering.md))
**Branch:** [`dharmatech/Niva:string-ordering`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:string-ordering),
one commit on `self-host` at `4e809716`: `69e688aa`. Locally in
`~/src/Niva-self-host-string-ordering`, a worktree of
`~/src/Niva-self-host`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

The issue's option 1, in Gavr's style:

- **`front/resolver/protocols/string.niva`:** `<`, `>`, `<=` and `>=`,
  each `String` to `Bool`, after `==` and `!=`. Nothing else is needed
  for Go: the IR already makes any comparison operator an `IrOpBin`, and
  Go's operators order strings.
- **`back/clojureBackend/cljEmit.niva`:** the `IrOpBin` case, which
  already turned `+` on strings into `str`, turns the four into
  `(op (compare l r) 0)` when the left side is a `String`. It's a
  `| true` match now in place of the `ifTrue:ifFalse:`, with `$`
  interpolation, and one `isStr` for both cases.

**Order of filing,** as for 005: file the issue saying a draft PR for 1
follows, open the PR with the issue's number, then put the PR's number
in the issue with `gh api -X PATCH`.

## What was checked

On 2026-10-25, each from an exported copy, with stage 1 at `4e809716`
and a copy of its home (see below):

- `niva test`: 389 pass on the branch, against 386 on stock
  `4e809716`, so the 3 new tests pass and nothing else changed. The one
  failing on both is `babashkaHostLoadsRecoversAndWatchesSources`, as
  for 005. The bind tests pass now, since Gavr's `5e347c29`.
- The 3 new tests fail without the change: with only the test files
  added to stock, 386 pass and the 3 fail.
- `java -jar main.jar build --backend=go` builds the branch's own
  sources, and stock sources compiled with the branch's jar give the
  same Go as with the stock jar, but for the `//line` paths. The
  Go-built compiler gets the 13 comparisons right on both backends.
- 13 comparisons, on Go and Clojure, all as expected: the four
  operators, a prefix, case, the empty string, equal strings, two
  variables, a method comparing two types' `name` fields, and
  `"é" > "z"`.
- mpl-niva's 31 checks pass on Go with the branch.

**The stage 1 home.** Running the JVM compiler on a test program with
`~/src/Niva-self-host/local/home` rewrites the Kotlin project stage 2
left there, and NIN reads that project to map its own stack traces: a
type error then also printed "Cant find niva line above 292 in file
main/kotlin/Main.kt" and exited 1. That's README item 29, which is our
setup, not Niva. `tools/build-self-hosted-niva` was re-run to restore
it, and the JVM compiler and the branch's build here used a copy of
the home.

Not covered: `Char`, which has no ordering either, and a three-way
`compareTo:`, the issue's option 2.

Everything below the line is the pull request as opened.

---

**Title:** [resolver] strings get < > <= >=, which clojure orders with compare (#451)

Option 1 from #451, as a draft, to show what it looks like. You don't
have to take it, and if another option fits better, that's fine.
For #451.

`String` gets `<`, `>`, `<=` and `>=`, like `Int`:

```Scala
a = "apple"
b = "banana"
(a < b) echo
```

On Go nothing else changes: the IR already makes every comparison
operator native, so it's `(a < b)`, Go's own, which orders strings. On
Clojure, whose `<` takes only numbers, a string comparison becomes
`(< (compare a b) 0)`, next to `+` on strings becoming `str`.

**Where to look:** the four lines in
`front/resolver/protocols/string.niva`, and the `IrOpBin` case in
`back/clojureBackend/cljEmit.niva`.

The two orders agree but for a character above U+FFFF compared with one
from U+E000 to U+FFFF: Go compares UTF-8 bytes, and Clojure's `compare`
UTF-16 units. `Char` is left out, and so is a `compareTo:`.

Three tests, each failing without the change: in `resolverTests.niva`,
the four operators type-check as `Bool`; in `goBackTests.niva`, Go's
operators; in `cljBackTests.niva`, `compare`, with `<` on numbers and
`+` on strings unchanged.

`niva test`: 389 pass, 3 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work, and the compiler's own Go is unchanged.
