# Comment imports

Niva has no imports: the compiler finds every type and method by
itself. So each MPL file says what it uses in comments, written as
imports would be, and `tools/check-imports` checks them against what the
file really uses.

## The convention

At the top of each `.niva` file, one line per module the file uses:

```
// Project import: "mpl.order"
// Project import: "mpl.tree"

// Simplifying: Cohen's automatic simplification, as `simplify`. …
```

- **Module names are NIN's:** the path from the project root without
  `.niva`, with `/` as `.`. So `mpl/tree.niva` is `mpl.tree`.
- **Uses, not names.** A file imports a module if it uses anything
  declared there: a type, or a method on any type. `main.niva` imports
  `mpl.simplify` for `simplify`, a method that file adds to
  `mpl.tree`'s `Expr`.
- **`core` needs no import.** A file that uses nothing has no lines.
- **Alphabetical,** though nothing checks the order.
- **The future syntax:** the Kotlin compiler's `Project import:`, with
  NIN's module names. If NIN gets imports in that form, deleting the
  `//` promotes them.

## The checker

`tools/check` runs it after the checks. To run it alone:

```bash
tools/check-imports
```

It prints nothing when every import is used, every use is imported,
and no modules form a cycle. Otherwise it says what's wrong and the line
to add or remove, and exits 1. It never edits a file. For example, with
`mpl.simplify`'s line left out of `main.niva`:

```
main.niva: uses mpl.simplify without importing it: `Expr simplify` at main.niva:24.
  Add at the top: // Project import: "mpl.simplify"
```

And a cycle, made by having `mpl/tree.niva` call `before:`, with what
makes each step of it:

```
cycle: mpl.tree -> mpl.order -> mpl.tree
  mpl.tree uses mpl.order: `Expr before` at mpl/tree.niva:31
  mpl.order uses mpl.tree: `Expr` at mpl/order.niva:25 and 6 more
```

**How it knows.** It builds `main.niva` and `check.niva` with NIN on Go
and reads the generated `.niva_go/build/main.go`. There, every function
is named after the module that declares it
(`mpl_simplify__mpl_tree_Expr__simplify`), types are too
(`mpl_tree_Expr`), `//line` comments give each statement's file and
line, and top-level code is in `func main()`. Every build has every
file's declarations, but only its entry's top-level code, so it builds
both programs.

## Limits

- **It relies on NIN's internals:** how its Go backend names things, at
  `781c5b05`. Nothing promises that naming, and the Clojure backend
  isn't read at all.
- **It sees what the Go spells out.** The Go names the types Niva
  infers, so a type used only through inference counts: `check.niva`
  imports `tests.checking` for reading a `Check`'s fields, though it
  never writes `Check`. But a field read off a call, as in
  `PrintingChecks first name`, is just `.name` in Go, so that use of
  `Check` isn't seen.
- **Inline methods.** A method declared with `^=` is spliced into its
  callers, but at `781c5b05` only in its own module, so a call from
  another module stays a call and is seen. If NIN starts inlining across
  modules, those uses will vanish from the Go.
- **Programs are listed in the script,** in `ENTRIES`. A file whose
  top-level code isn't an entry's has that code dropped from every
  build. If that's all the file has, the checker says so; if the file
  declares things too, its top-level code goes unchecked.
- **Some uses have no line.** Go's type declarations carry no `//line`,
  so a field of another module's type is reported by file only.
- **One cycle at a time.**
- **It costs two builds,** about 1.5 s on MPL.

Why it's built this way is in
[series 011](design/implementation/011-comment-imports/README.md).
