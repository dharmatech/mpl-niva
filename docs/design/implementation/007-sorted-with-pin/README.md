# 007 sorted-with-pin

**Status:** Done; landed at `e5874de` (2026-10-25).

Moves the pin from `2ad4f629` to `self-host` at `781c5b05`, our
[#455](https://github.com/gavr123456789/Niva/pull/455), so lists have
`sortedWith:`. MPL's code doesn't change yet. Done in one conversation.

## Why

The simplifier keeps operands in canonical order, and Cohen's order
relation compares two expressions; it isn't a key one expression gives,
so `sortedBy:` doesn't fit. The JVM compiler has `sortedWith:`, a
comparator sort; NIN didn't. #455 added it with the same signature,
without an issue ([draft](../../../niva-feedback/008-sorted-with-pr.md)),
and Gavr merged it the same day. With this, string `<` (#452),
`inject:into:` (#454) and `sortedWith:` are all in: what canonical
order and combining numbers need.

## Decisions

- **Pin the tip, `781c5b05`:** #455 is the only commit since
  `2ad4f629`.
- **Remove MPL's `.niva_clj/` again,** for item 33: its runtime had no
  `List__sortedWith`. The next Clojure run wrote one that has it.
- **No new re-check:** #455 adds one method, so the notes re-checked in
  [004](../004-notes-recheck/README.md) still hold.

## What changed

- `tools/build-self-hosted-niva` pins `781c5b05`, and
  `~/src/Niva-self-host` was rebuilt there.
- [Niva notes](../../../niva-notes.md): the pin, and a note on sorting:
  `sortedBy:` for a key, `sortedWith:` for a comparator. The
  `sortedWith:` gap is gone.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md).

## What we checked

At `781c5b05`:

- `tools/check`: all 31 pass on Go, and the demo prints the same as at
  `2ad4f629`. On Clojure the checks still stop at a type used from
  another file (item 3).
- #455's two programs, through `tools/nin` in fresh folders, on Go and
  Clojure: numbers up and down, words by length, an empty list, a tie
  kept in order, and a `List(Expr)` sorted by a comparator that uses
  string `<`. All as on the JVM compiler.

## Next

The first simplification rules, as planned in
[001](../001-self-hosted/README.md#next): flattening sums and products,
numbers, like terms, the identities for 0 and 1, and canonical order,
with `sortedWith:` and string `<`.
