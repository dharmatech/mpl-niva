# 003 Go: three bugs in a match used as a statement: pull request

**Status:** Opened 2026-10-23 as [#436](https://github.com/gavr123456789/Niva/pull/436),
with no issue of its own. The [#435 sketch](002-go-return-inside-value-pr-temp.md)
builds on it and waits until it's merged.
**Branch:** [`dharmatech/Niva:go-stmt-match-fixes`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:go-stmt-match-fixes),
three commits on `self-host` at `b00ec027`: `d450c31f`, `1b94b4f3`,
`cbc049ba`. Locally it's `go-stmt-match-fixes` in
`~/src/Niva-self-host-go-value`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

The #435 sketch emits every match whose value is used through the
statement form of a match from 6af8f9f6, and building the compiler with
it turned up bugs in that form. Three of them happen on `self-host`
today, in a match used as a statement, so they go first, on their own:

- A match on `true` with conditions is left out. Found by the sketch;
  the user confirmed it on stock `b00ec027`: `f`'s body compiles to
  just `return "end"`.
- A match on errors runs its else branch after an error branch too.
  This is option A's first commit, `9f7368f2`, unchanged (item 14 in
  the [README](README.md)).
- A `mut` cell subject is matched as the pointer. Found by the sketch,
  where the JSON parser's `| c | '{' => …` didn't build; checked on a
  stock `b00ec027` jar with a match statement in a `mut` method, where
  the Go build fails the same way.

The sketch's other fix there, for `ifTrue:` alone as a value, is about
a match used as a value, so it stays in the sketch.

How it was checked, beyond what the PR says:

- Each prefix builds and passes: 358, 359 and 360 tests, against 357 at
  `b00ec027`. The first and third new tests fail with their commit's
  code change reverted; `9f7368f2`'s was checked when it was written.
- `niva build` works, and `java -jar main.jar build --backend=go` on a
  copy of `Niva/NivaInNiva` builds `.niva_go/build/niva_go`.
- The two reproductions print `is a`, `end`, `not a`, `end` and
  `object`, `other`.
- Of the #435 handoff's 41 programs, compared with stock `b00ec027`,
  only the ones with a match on errors as a statement change:
  `errStmtFallthrough` no longer prints `parsed 0`, `errAliasPinned`,
  `throwAtEnd` and `twoErrStmts` build and run, and `unitErrTail` loses
  one of its two Go errors.
- mpl-niva's 31 checks pass with this compiler.

Everything below the line is the pull request as opened.

---

**Title:** [go] fix three bugs in a match used as a statement

Three bugs in the statement form of a match from 6af8f9f6, found while
working on #435 but independent of it. Each commit fixes one, with a
test that fails without it.

1. **A match on `true` with conditions was left out.**

   ```Scala
   String f -> String = [
     | true
     | this == "a" => "is a" echo
     |=> "not a" echo
     "end"
   ]
   ```

   compiled to just `return "end"`: `emitBoolMatchStatement` only knows
   the cases `true` and `false` of `ifTrue:` and `ifFalse:`. Now such a
   match is an if/else chain like a match on a value, as its anonymous
   function form already was.
2. **A match on errors ran its else branch after an error branch too**,
   with the zero value as `it`. For -5,
   `| this parse | Bad => it text echo |=> ("parsed " + it toString) echo`
   printed `negative`, then `parsed 0`. Two such matches in one block
   declared `it` twice, and an `it` a branch doesn't use stopped the
   build, in the anonymous function form too. Now the else branch is
   the `else` of the `if err != nil`, in its own block, and an unused
   alias is assigned to `_`.
3. **A `mut` cell subject was matched as the pointer.** In a `mut`
   method, `c = this peek` then `| c | '{' => …` became
   `__matchSubject := c` with `c` a `*rune`, and the Go build failed.
   Now the subject is read with `toGoValueExpr`, as the anonymous
   function form does.

`niva test`: 360 pass, 3 of them new. `niva build` and
`java -jar main.jar build --backend=go` work.
