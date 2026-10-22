# Niva notes

What to know when writing Niva for this project, with the self-hosted
compiler at `self-host` `6af8f9f6` (2026-10-19). The Go backend is the
one we use; Clojure differences are marked. The bugs behind the
workarounds are listed for filing in [Niva feedback](niva-feedback/README.md).
The legacy compiler's notes are in [Niva notes: the legacy compiler](niva-notes-legacy.md).

## The language

- Generic types are written `List(Expr)`.
- Binary messages have no precedence and go left to right, as in
  Smalltalk: `2 + 3 * 4` is 20, and `x + 2 num * y` is `(x + 2) * y`.
  Write `x + (2 num * y)`. Keyword messages bind loosest, so
  `x + y pow: 2 num` is `(x + y)^2`.
- `^` is return, so power is the keyword message `pow:`.
- There's no `cond => expr` guard. Write `cond ifTrue: [...]`.
- `echo` returns its `String`, so a method that ends with `echo` can't
  declare `-> Unit`. End it with `()`.
- Interpolation takes a name only: `"$name"` works, `"${a b}"` is printed
  as written.
- `==` compares structure. On Go, comparing two sums or products fails
  at run time, because Go can't compare the lists inside them. Use
  `same:`.
- Only the entry file's top-level code runs. Top-level code in other
  files isn't type-checked either, so keep it out of them.
- A file named `test.niva` at the root is left out of the sources, which
  is why the checks' entry file is `check.niva`.

## Workarounds

- **`^` inside `ifTrue:` is dropped on Clojure**, so the method carries
  on. Write the whole method as an `ifTrue:ifFalse:` expression instead.
- **A type used from another file can't be found on Clojure.** No
  workaround short of one file, so we don't use Clojure for now.
- **`^ (…) any: […], not` doesn't compile on Go** (a stray empty
  `return`). Name the value, then return it.
- **A list literal of branch values**, such as `{(Sym name: "x")}` given
  as a `List(Expr)`, doesn't compile on Go. Build leaves with `sym` and
  `num`, which return `Expr`.
- **`toString` is special.** On Clojure, `e toString` ignores a
  `toString` declared on the union's branches. We print with `text`.

## Library gaps

- No `sortedWith:`, only `sortedBy:`. Sorting by a comparator means
  writing our own sort.
- No `all:`. `any:` passes the type checker but is missing from the
  Clojure runtime, so we use `filter:` and `isEmpty`.
- `indexOfFirst:` is in both runtimes, but the type checker doesn't know
  it.

## Exit codes

Compile errors exit 0 on both backends. A run-time error exits 1 on Go
and 0 on Clojure. So `tools/check` reads the output instead: it passes
only when the last line is `all N checks passed`.
