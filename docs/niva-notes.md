# Niva notes

What to know when writing Niva for this project, with the self-hosted
compiler at `self-host` `a56f659c` (2026-10-25). Most notes date from
`6af8f9f6` (2026-10-19) and weren't re-checked when the pin moved
([series 003](design/implementation/003-equals/README.md)). The Go
backend is the one we use; Clojure differences are marked, and hold on
Babashka and let-go alike unless noted
([series 002](design/implementation/002-let-go/README.md)). The bugs
behind the workarounds are listed for filing in
[Niva feedback](niva-feedback/README.md).
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
- `==` compares structure, operands in order: `x + y` isn't `y + x`. It
  works on lists, and a type's own `==` is called. On Go that's since
  [#440](https://github.com/gavr123456789/Niva/pull/440), merged as
  `f7979fd4`; before it, comparing two sums failed at run time.
- Only the entry file's top-level code runs. Top-level code in other
  files isn't type-checked either, so keep it out of them.
- A file named `test.niva` at the root is left out of the sources, which
  is why the checks' entry file is `check.niva`.

## Workarounds

- **`^` inside `ifTrue:` is dropped on Clojure**, so the method carries
  on. Write the whole method as an `ifTrue:ifFalse:` expression instead.
- **A type used from another file can't be found on Babashka or JVM
  Clojure.** let-go accepts the generated code. No workaround on the
  others short of one file, so we don't use Clojure for now.
- **`/` on two `Int`s gives a ratio on Clojure**: `17 / 5` is `17/5`,
  where Go gives 3. Nothing in MPL divides yet; when it does, check it
  on Go.
- **A set literal with a repeated item doesn't load on Clojure**:
  `#(1 2 2 3)` keeps the whole program from loading. Write each item
  once.
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
- `+` on two lists is missing from the Clojure runtime: the call is to
  `List__+`, and the runtime has `List__plus`. `check.niva` uses it, so
  the checks can't run on Clojure even in one file.
- `indexOfFirst:` is in both runtimes, but the type checker doesn't know
  it.

## Exit codes

Compile errors exit 0 on both backends. A run-time error exits 1 on Go
and 0 on Clojure, though `bb` and `lg` themselves exit 1; it's
`nin run` that drops it. So `tools/check` reads the output instead: it
passes only when the last line is `all N checks passed`.
