# Niva notes

What to know when writing Niva for this project, with the self-hosted
compiler at `self-host` `4e809716` (2026-10-25). Each note was
re-checked at that pin
([series 004](design/implementation/004-notes-recheck/README.md)). The
Go backend is the one we use; Clojure differences are marked, and hold
on Babashka and let-go alike unless noted
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
- A list, set or map literal takes the union its context expects:
  `{this}` in `| Num =>`, given as a `List(Expr)`, is a `List(Expr)`.
  That's since [#450](https://github.com/gavr123456789/Niva/pull/450),
  merged as `ab69e59b`; for the case it leaves, see Workarounds.
- A type name of one capital letter is read as a type parameter:
  `type P n: Int`, then `P n: 1`, gives "Missing constructor table for
  Type(P)". Use two letters or more.
- `ifNotNull:` isn't there: it's in the compiler's own
  `libs/maybe.niva`, not in what programs get. Match instead:
  `| x | null => … |=> …`. On Go, two nullable locals compare as
  pointers, so `x::Int? = 5` and `y::Int? = 5` aren't `==`. MPL has no
  nullables.
- Only the entry file's top-level code runs. Top-level code in other
  files isn't type-checked either, so keep it out of them.
- A file named `test.niva` at the root is left out of the sources, which
  is why the checks' entry file is `check.niva`.

## Workarounds

- **A list with nothing expected of it** keeps the type of its items:
  `l = {(Sym name: "x")}` is a `List(Sym)`, and passing it on as a
  `List(Expr)` doesn't build on Go. Declare it, `l::List(Expr) = …`, or
  build it where it's used. The rest of this bug was fixed by #450, and
  [#449](https://github.com/gavr123456789/Niva/issues/449) was closed
  with this case noted, to be filed on its own if a program needs it.
- **`^` inside `ifTrue:` is a compile error on Clojure**: "the Clojure
  backend can't return from here yet". Until
  [#433](https://github.com/gavr123456789/Niva/pull/433) it was silently
  dropped. Write the whole method as an `ifTrue:ifFalse:` expression
  instead.
- **A type used from another file can't be found on Babashka or JVM
  Clojure.** let-go accepts the generated code. No workaround on the
  others short of one file, so we don't use Clojure for now.
- **`/` on two `Int`s gives a ratio on Clojure**: `17 / 5` is `17/5`,
  where Go gives 3. Nothing in MPL divides yet; when it does, check it
  on Go.
- **A set literal with a repeated item** doesn't build on Go
  ("duplicate key") or load on Clojure: `#(1 2 2 3)`. Write each item
  once.
- **`toString` is special.** On Clojure, `e toString` ignores a
  `toString` declared on the union's branches. We print with `text`.
- **A `throw` that ends a block doesn't build on Go**: `this < 0
  ifTrue: [(Bad text: "negative") throw]` stops at an unused
  `__callResult`. MPL doesn't throw.

## Library gaps

- `String` has no `<` or other ordering, and `Char` has none either,
  only `code`. Comparing `code`s character by character works on both
  backends. On Go, a bind works too (below).
- No `sortedWith:`, only `sortedBy:`. Sorting by a comparator means
  writing our own sort.
- No `all:`. `any:` passes the type checker but is missing from the
  Clojure runtime, so we use `filter:` and `isEmpty`.
- `contains:` on a list passes the type checker but is in neither
  runtime.
- `+` on two lists is missing from the Clojure runtime: the call is to
  `List__+`, and the runtime has `List__plus`. `check.niva` uses it, so
  the checks can't run on Clojure even in one file.
- `indexOfFirst:` is in both runtimes, but the type checker doesn't know
  it.

## Binding Go

A method can be Go code:

```Scala
String before: String -> Bool = Bind imports: #("strings") emit: "strings.Compare($0, $1) < 0"
```

in a file listed in `niva.bindings` at the project root, as
`go|binds.str.bind|binds/str.niva`. The middle part, the module id,
has to end in `.bind`, as the compiler's own entries do. With another
id, or with no entry, the Go build fails at
`core.Bind__imports__emit(, …)`, "syntax error: unexpected comma", and
nothing says why. Only Go was tried.

## Exit codes

Compile errors exit 0 on both backends. A run-time error exits 1 on Go
and 0 on Clojure, though `bb` and `lg` themselves exit 1; it's
`nin run` that drops it. So `tools/check` reads the output instead: it
passes only when the last line is `all N checks passed`.
