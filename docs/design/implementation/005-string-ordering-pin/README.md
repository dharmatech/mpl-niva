# 005 string-ordering-pin

**Status:** Done; landed at `PENDING` (2026-10-25).

Moves the pin from `4e809716` to `self-host` at `e823f7fe`, our
[#452](https://github.com/gavr123456789/Niva/pull/452), so strings have
`<`, `>`, `<=` and `>=`. MPL's code doesn't change yet. Done in one
conversation.

## Why

The simplifier's canonical order compares symbols by name, and Niva had
no ordering on strings (feedback item 26). We asked in
[#451](https://github.com/gavr123456789/Niva/issues/451)
([draft](../../../niva-feedback/006-string-ordering.md)); Gavr took
option 1 and merged #452 the same day. With the pin past it, MPL can
write `a name < b name` instead of comparing `Char code`s.

## Decisions

- **Pin the tip, `e823f7fe`:** #452 is the only commit since
  `4e809716`.
- **No new re-check:** #452 touches only the `String` protocol and
  Clojure's `IrOpBin` case, so the notes re-checked in
  [004](../004-notes-recheck/README.md) still hold. The header says
  which pin each was checked at.

## What changed

- `tools/build-self-hosted-niva` pins `e823f7fe`, and
  `~/src/Niva-self-host` was rebuilt there.
- [Niva notes](../../../niva-notes.md): the pin, a note that strings
  are ordered, and how each backend orders them. The library gap for
  `String` ordering is gone; `Char` still has none. The bind example,
  `strings.Compare`, now says it's from before #452.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md).

## What we checked

At `e823f7fe`:

- `tools/check`: all 31 pass on Go, and the demo prints as before. On
  Clojure the checks still stop at a type used from another file
  (item 3).
- The 13 comparisons from #452's checks, through `tools/nin`, give the
  same on Go and Clojure: the four operators, a prefix, case, the empty
  string, equal strings, two variables, a method comparing two types'
  `name` fields, and `"é" > "z"`.

## Next

The first simplification rules, as planned in
[001](../001-self-hosted/README.md#next): flattening sums and products,
numbers, like terms, the identities for 0 and 1, and canonical order,
now with `<` on strings.
