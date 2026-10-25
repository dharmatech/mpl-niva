# 004 notes-recheck

**Status:** Done; landed at `7cae0ee` (2026-10-25).

Moves the pin to `self-host` at `4e809716`, which has our
[#450](https://github.com/gavr123456789/Niva/pull/450), and re-checks
every [Niva note](../../../niva-notes.md) and every open item in
[Niva feedback](../../../niva-feedback/README.md) there, as
[003](../003-equals/README.md#next) planned. MPL's code doesn't change.
Done in one conversation.

## Why

The notes dated from `6af8f9f6` and weren't re-checked when 003 moved
the pin to `a56f659c`. Since then, #450 (filed as
[#449](https://github.com/gavr123456789/Niva/issues/449), merged as
`ab69e59b`) makes a list literal take the union its context expects,
which the simplifier's first rules need: `{this}` inside `| Num =>`
didn't build on Go before. Re-checking at `a56f659c` while drafting
005 had also turned up items that were fixed, and new ones.

## Decisions

- **Pin the tip, `4e809716`** (2026-10-25): #450, then Gavr's
  `5e347c29` (the bind tests fixed) and `4e809716` (Go tests written
  with `Assert nivaToGo: … containsAll:`, #445). MPL passes on it, and
  it's what he's working on.
- **MPL's code stays.** It builds leaves with `sym` and `num`, which
  return `Expr`. That was the workaround for item 5, but it's how
  expressions are meant to be built anyway, so nothing comes out.
- **One note per finding, where it's useful when writing MPL;** the
  details stay in the feedback list. Nullables, `throw` and binds get
  a line each, since the simplifier may reach for them.

## What changed

- `tools/build-self-hosted-niva` pins `4e809716`. As asked,
  `~/src/Niva-self-host` was moved there and rebuilt.
- [Niva notes](../../../niva-notes.md): the pin, and every note
  re-checked. Item 5's workaround is now the one case #450 leaves, a
  list with nothing expected of it. Item 6's (`^ (…) any: […], not`) is
  gone. `^` inside `ifTrue:` on Clojure is a compile error now, not
  dropped. New: one-letter type names, `ifNotNull:` and nullable `==`,
  a `throw` ending a block, `String` ordering, `contains:`, and how to
  bind Go.
- [Niva feedback](../../../niva-feedback/README.md): the re-check, 6 and
  13 no longer reproducing, and new items 26 to 29.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md).

## What we checked

At `4e809716`:

- `tools/check`: all 31 pass on Go, and the demo prints as before. On
  Clojure the checks still stop at a type used from another file
  (item 3).
- The reproducers from re-checking at `a56f659c` and from 005, 53
  programs, each run on Go and Clojure, then four more for binds and
  one-letter names. Item 5: every case from 005
  works on both but the untyped local, which still doesn't build on Go.
  Items 6 and 13 don't reproduce. 3, 4, 8 to 12, 15, 17 to 19, 21, 22,
  24 and 25 do. 23's `ifNotNull:` isn't there for a program to call.
- New since `a56f659c`, all reproducing: `String` has no `<` (26), a
  one-letter type name is a type parameter (27), and a bind needs a
  `.bind` module id (28). A bind listed as `go|binds.str.bind|…` works,
  so `strings.Compare` can order strings on Go. The Java exception from
  `a56f659c` (29) didn't show in any failing run. It later turned out
  to be our setup, not Niva: see item 29.
- Exit codes: a compile error exits 0 on both, a run-time error 1 on Go
  and 0 on Clojure, as before.

## Next

The first simplification rules, as planned in
[001](../001-self-hosted/README.md#next): flattening sums and products,
numbers, like terms, the identities for 0 and 1, and canonical order,
which needs a `String` ordering (item 26).
