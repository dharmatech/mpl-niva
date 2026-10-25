# 006 inject-into-pin

**Status:** Done; landed at `PENDING` (2026-10-25).

Moves the pin from `e823f7fe` to `self-host` at `2ad4f629`, our
[#454](https://github.com/gavr123456789/Niva/pull/454), so
`inject:into:` runs on both backends. MPL's code doesn't change yet.
Done in one conversation.

## Why

`inject:into:` passed the type checker but didn't run: both runtimes
named it `List__injectInto`, where the call is `List__inject__into`
(feedback item 30). #454 renamed the runtime functions, without an
issue ([draft](../../../niva-feedback/007-inject-into-pr.md)), and Gavr
merged it the same day. The simplifier will want a fold for combining
numbers.

## Decisions

- **Pin the tip, `2ad4f629`:** #454 is the only commit since
  `e823f7fe`.
- **Remove MPL's `.niva_clj/`.** NIN writes the Clojure runtime only
  when it's missing (item 33), and the one there still defined
  `List__injectInto`. It's generated and gitignored; the next Clojure
  run wrote a fresh one, with `List__inject__into`.
- **No new re-check:** #454 changes one line in each runtime, so the
  notes re-checked in [004](../004-notes-recheck/README.md) still hold.

## What changed

- `tools/build-self-hosted-niva` pins `2ad4f629`, and
  `~/src/Niva-self-host` was rebuilt there.
- [Niva notes](../../../niva-notes.md): the pin, and `inject:into:`
  moves from the library gaps to the language, with the one limit it
  has: the total has to be the items' type, so `map:` first.
- [Niva feedback](../../../niva-feedback/README.md): that limit is new
  item 34. The Kotlin compiler doesn't have it.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md).

## What we checked

At `2ad4f629`:

- `tools/check`: all 31 pass on Go, and the demo prints the same as at
  `e823f7fe`. On Clojure the checks still stop at a type used from
  another file (item 3).
- Through `tools/nin`, in a fresh folder: `inject:into:` on `{1 2 3}`
  from 0, on an empty `List(Int)` from 10, and on strings from `""`
  gives `6`, `10` and `abc` on Go and Clojure. The notes' example,
  `map:` then `inject:into:`, gives 6 on both.
- Folding strings into an `Int` is a type error on NIN and prints 3 on
  the Kotlin compiler (item 34).

## Next

The first simplification rules, as planned in
[001](../001-self-hosted/README.md#next).
