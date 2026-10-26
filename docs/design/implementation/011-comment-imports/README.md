# 011 comment-imports

**Status:** Done, merged into `main` (2026-10-26), after review as a
sketch on the branch `sketch/comment-imports` (`e83e7bc`). `tools/check`
now fails on a missing or unused import comment.

Each MPL file lists the modules it uses, as comments in the syntax
imports would have, `// Project import: "mpl.tree"`. A script,
`tools/check-imports`, checks them against what each file really uses,
as NIN's generated Go shows, and `tools/check` runs it after the
checks. [Comment imports](../../../comment-imports.md) has the
convention, the checker and its limits.

## Why

- **Dependencies visible in each file,** to people and agents, without
  running the compiler. Before this, nothing in `main.niva` said it
  used `mpl/simplify.niva`: `simplify` is a method that file adds to
  `Expr`.
- **Check and suggest, never write.** What a file imports is the
  programmer's decision. The script says what's missing or unused and
  the line to add or remove, and leaves the file alone.
- **A cheap trial of [series 009](../009-optional-imports/README.md)'s
  idea,** imports optional per file and checked when present, plus no
  cycles. 009 is parked until NIN matures. This needs no compiler
  change, and the comments become 009's syntax by deleting the `//`.

## What was considered

- **Where to learn the uses.** NIN's `info` lists what each package
  declares, not what it uses, and `nin ir` writes only a summary. The
  Go backend's output has it all: each function and type is named after
  its module, and `//line` comments map statements to files and lines.
- **Generating the comments.** No: that's the compiler's job in 009, and
  here they're meant to be chosen.
- **Where the comments go:** first in the file, before its header
  comment, as `Project use:` is in Niva's examples.
- **Quiet when they hold,** so `tools/check`'s output still ends with
  `all N checks passed`.
- **Python, not bash,** for parsing the Go and finding cycles
  (`graphlib`).

## What was done

- [`tools/check-imports`](../../../../tools/check-imports): builds
  `main.niva` and `check.niva` on Go, collects each file's uses, and
  reports missing and unused imports and one cycle, exiting 1 if there's
  any.
- [`tools/check`](../../../../tools/check) runs it after the checks and
  fails if either fails.
- Import comments in nine files. `mpl/tree.niva` and
  `tests/checking.niva` use only `core`, so they have none.

## What the tests showed

All in this branch's worktree, or in a throwaway copy for cycles:

- **A missing import:** `mpl.simplify`'s line taken out of `main.niva`.
  `tools/check` printed `all 151 checks passed`, then the use, `Expr
  simplify` at `main.niva:24`, and the line to add, and exited 1.
- **An unused import,** `mpl.order` in `main.niva`, and one of a module
  that doesn't exist: each reported with the line to remove.
- **Cycles,** two modules (`mpl/tree.niva` calling `before:`) and three
  (`notation` → `simplify` → `order` → `notation`): each reported in
  order, with a use for each step. NIN compiles both without complaint.
- **A file of only top-level code** that isn't an entry: reported as not
  covered, rather than passing.
- **A failed build:** reported with the compiler's output.

## What the Go doesn't show

- **A field read off a call.** `PrintingChecks first name` is
  `tests_printingChecks__PrintingChecks__first().name` in Go: the use of
  `tests.printingChecks` shows, but not that `Check`, from
  `tests.checking`, is read. Bound to a variable first, it would show,
  as Go spells the variable's type.
- **Inline methods** (`^=`) would hide their callers, but at `781c5b05`
  NIN expands them only within their own module
  (`inlineExpandPackage`), so a call from another module stays a call.
- **Type declarations** have no `//line`, so a use there is reported
  without a line.

No renamed calls turned up: every function the Go declares is called
under its own name, and each `//line` inside a function names that
function's own file.
