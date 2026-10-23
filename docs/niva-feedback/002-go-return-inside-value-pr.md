# 002 Go: a `^` inside a value doesn't return from the method: pull request

**Status:** Drafted, not opened. To be opened once Gavr picks this
option on the issue.
**Issue:** not filed yet ([draft](002-go-return-inside-value.md))
**Branch:** local `go-value-return` in `~/src/Niva-self-host-go-value`,
to push as `dharmatech/Niva:fix-NNN-go-value-return`, three commits on
`self-host` at `ad439a32`: `6554dc9b`, `86dc10f2`, `7da54ee7`
**Base:** `gavr123456789/Niva` `self-host`, not `main`

This is option A from the issue, making a `^` inside a value work on Go.
Option B, making it an error, has its own
[draft](002-go-return-inside-value-pr-forbid.md). Before opening,
replace NNN with the issue's number and rebase if `self-host` has moved.

How it was checked, beyond what the PR says:

- First written on `6af8f9f6`, then rebased on `ad439a32`. The rebase
  conflicted with 2cea9063 ("fix go mut value semantics") in `IrLet` and
  `IrSetLocal`; its code is kept as is, under the new check.
- Each prefix builds and passes: 355, 358 and 360 tests, against 354 at
  `ad439a32`.
- mpl-niva's demo and its 31 checks give the same output.
- Compiling the compiler's own sources changes the Go of 17 of its 879
  functions, the ones with a `^` in a bound or returned value, and that
  Go still builds. The Go-built compiler isn't a check of its own: even
  at `ad439a32` it doesn't reproduce the JVM jar's output for those
  sources.
- `~/src/Niva-self-host` and its pinned jar, which this project uses,
  were left alone.

Everything below the line is the pull request as it would be opened.

---

**Title:** [go] return from the method for a `^` inside a value (#NNN)

Fixes #NNN.

Three commits, each of which builds and passes the tests on its own.
The first fixes a separate bug the second relies on, so it can also go
alone.

1. **Run a match on errors' else branch only when there's no error.**
   The statement form of a match on errors, from 6af8f9f6, put the else
   branch after the `if err != nil { switch … }`, so it also ran after
   an error branch, with the zero value as `it`. For -5,

   ```Scala
   | this parse
   | Bad => it text echo
   |=> ("parsed " + it toString) echo
   ```

   printed `negative` and then `parsed 0`. Two such matches in one block
   declared `it` twice, and an `it` a branch doesn't use stopped the Go
   build, in the anonymous function form too (`| Bad => 0 |=> it`). Now
   the else branch is the `else` of that `if`, in its own block, and an
   alias a branch doesn't use is assigned to `_`, as the type switch
   already does.
2. **Emit a value that can `^` as statements where it's bound or
   returned.** A match or block whose branches can `^`, bound with `=` or
   `<-`, or returned with `^` or as the method's last value, goes through
   the statement emitters from 6af8f9f6, each branch ending by assigning
   or returning its value:

   ```go
   var s string
   if (this > 0) {
       return "returned"
   } else {
       s = "kept"
   }
   ```

   It's a rewrite of the IR before emitting (`goEndingBranches:`), so the
   emitters themselves don't change. The local counts as `mut` while its
   branches are emitted, for the check from 2cea9063. A `^` in a value
   anywhere else is reported by `validateIr`, with its position:

   ```
   main.niva:2:22: the Go backend can't return from inside this value. Bind the value with = first, or use ^ in a statement
   ```
3. **A match branch that ends with `^` doesn't decide the match's
   type.** It counts as Nothing, which `mergeTypesByUnion` already leaves
   out, so `v = | this parse | Bad => ^ "bad input" |=> it` is an Int,
   and the statement `| this parse | Bad => ^ "bad input" |=> []` type
   checks. On Go this needs the previous commit: the type can now be
   narrower than what the `^` returns (`| Num => ^ this |=> Sym name:
   "x"` is a Sym), and an anonymous function of that type wouldn't
   build.

Tests:

- `niva test`: the 354 existing tests and 6 new ones pass:
  `goErrorMatchStatementAliasAndElse`,
  `goValueMatchNonLocalReturnCodegen`,
  `goValueMatchReturnOnErrorCodegen`,
  `goTailMatchNonLocalReturnCodegen`,
  `goReturnInsideOtherValueIsReported` and
  `matchBranchEndingWithReturnDoesNotDecideItsType`. They fail without
  the change.
- Like the other Go backend tests they check the generated code, so I
  also ran programs with `--backend=go`: the ones from #NNN, a nested
  value, `x <- …`, a match by type that uses a field, a nullable local,
  a `mut` local and a pointer cell, an errorable method, a loop, two
  such matches in one method, and a `Unit` method that returns early.
- The 34 `^` inside a value in the compiler's own sources still compile,
  and on Go they now return from the method, as on the JVM.
