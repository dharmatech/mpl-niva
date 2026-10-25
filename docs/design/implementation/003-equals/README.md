# 003 equals

**Status:** Done; landed at `1fbabc9` (2026-10-24).

MPL compares expressions with Niva's `==` instead of its own `same:`,
now that the self-hosted compiler compares by content on Go too. That
needs a newer compiler, so the pin moves from `6af8f9f6` to `self-host`
at `a56f659c`. Done in one conversation.

## Why

On Go, `==` on two sums or products failed at run time, because Go
can't compare the lists inside them. So
[000](../000-expression-tree/README.md) and
[001](../001-self-hosted/README.md) compared expressions with `same:`,
a structural equality written by hand. We asked Gavr what `==` should
mean in [#439](https://github.com/gavr123456789/Niva/issues/439)
([draft](../../../niva-feedback/004-go-equality.md)). He chose
structure, with a generated compare function where Go's `==` can't
compare, and our sketch of that,
[#440](https://github.com/gavr123456789/Niva/pull/440)
([draft](../../../niva-feedback/004-go-equality-pr.md)), was merged into
`self-host` as `f7979fd4` on 2026-10-25. From then on `==` does what
`same:` did.

## Decisions

- **Pin the tip, `a56f659c`** (2026-10-25): Gavr's binds refactor, on
  top of `f7979fd4`. MPL passed on both, and the tip is what he's working
  on. Had it failed, we'd have pinned `f7979fd4` and recorded what broke
  in [Niva feedback](../../../niva-feedback/README.md).
- **Drop `same:`.** `==` gives the same answers on Go and Clojure, and
  two names for one equality would have to be kept in step.
  `mpl/equality.niva` is gone. Its one rule, operands in order, is now a
  line on `Expr` in `mpl/tree.niva`.
- **The checks stay as they were,** with `==` for `same:`. `same sum` and
  `operand count` compare two `Sum`s, the case that failed on Go.

## What changed

- `tools/build-self-hosted-niva` pins `a56f659c`. With the user's OK,
  `~/src/Niva-self-host` was moved there and rebuilt.
- `mpl/equality.niva` is removed, and `tests/equalityChecks.niva` and
  `main.niva` use `==`.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md),
  `==` in [Niva notes](../../../niva-notes.md), and the layout in the
  [README](../../../../README.md).

## What we checked

Each compiler was built from scratch, stage 1 and stage 2, outside the
clone, then MPL was run on Go:

| Compiler | MPL with `same:` | MPL with `==` |
|---|---|---|
| `6af8f9f6`, the old pin | 31 pass | doesn't build: `struct containing []mpl_tree_Expr cannot be compared` |
| `f7979fd4`, #440 | 31 pass | 31 pass |
| `a56f659c`, the tip | 31 pass | 31 pass |

- The demo printed the same on all three.
- In the repo, on the rebuilt clone: `tools/check` passes all 31, and the
  demo prints as before, its last lines now about `==`.
- The 19 small programs written for #439 and #440 give at `a56f659c`
  what they gave on #440's branch, but for two blocks of the same type:
  now `true` on Go, since Gavr's review asked for blocks to be compared
  by signature. Clojure still says `false`
  ([Review](../../../niva-feedback/004-go-equality-pr.md#review)).
- On Clojure, `tools/check --backend=clj` still stops at a type used
  from another file (Niva feedback item 3), as on the old pin.

Nothing MPL uses broke between `6af8f9f6` and `a56f659c`.

## Next

Re-check [Niva notes](../../../niva-notes.md) at the new pin. Most notes
date from `6af8f9f6`, and some workarounds may no longer be needed:
Niva feedback item 6 works since `3746ffb7`, for one. Then the first
simplification rules, as planned in
[001](../001-self-hosted/README.md#next).
