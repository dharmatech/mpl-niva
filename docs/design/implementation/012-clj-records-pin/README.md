# 012 clj-records-pin

**Status:** Done (2026-10-26).

Moves the pin from `781c5b05` to `self-host` at `a4429c50`, to stay in
sync with upstream. Five commits come in: our
[#461](https://github.com/gavr123456789/Niva/pull/461) (item 3, a type
from another file on Clojure) and
[#462](https://github.com/gavr123456789/Niva/pull/462) (item 39, no
class hint for a union or an enum), and Gavr's `139a44e0` (a refactor
of the Clojure backend's tests), `b73180dd` (type hints in generated
Clojure) and `03b69df4` (their tests). MPL's code doesn't change.

## Decisions

- **Pin the tip, `a4429c50`:** nothing after #462.
- **Remove MPL's `.niva_go/` and `.niva_clj/`,** for item 33, so the
  Clojure runtime is the new one.
- **No new re-check of the notes:** the five commits are all Clojure,
  and #461 is the only one that changes a note.

## What changed

- `tools/build-self-hosted-niva` pins `a4429c50`, and
  `~/src/Niva-self-host` was rebuilt there.
- [Niva notes](../../../niva-notes.md): the pin, and the Clojure note.
  A type from another file now works on Babashka and JVM Clojure, but
  no longer on let-go ([let-go #1101](https://github.com/nooga/let-go/issues/1101)),
  and MPL stops at list `+`, which
  [#463](https://github.com/gavr123456789/Niva/pull/463) adds.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md).

## What we checked

At `a4429c50`:

- **Go:** `tools/check` passes all 151 checks, and the demo runs.
- **Clojure, as built:** `tools/check --backend=clj` stops on `bb` and
  JVM Clojure alike at `niva.core/List__+`, in `mpl/simplify.clj`.
  Item 3 is gone.
- **Clojure, with #463's two functions** added by hand to the generated
  runtime (`List__+` and `List__any`): all 151 checks pass on `bb` and
  on JVM Clojure, and the demo's output on `bb` matches Go's. So once
  #463 is in, MPL runs on every backend from stock `self-host`.
- **REPL mode** (`build --repl`, loaded into `bb` with the same two
  functions): record names have no dots now, thanks to #461. What stops
  it is item 37 alone: with the records moved to the top, the whole
  module loads and runs the demo. A line built with `--repl-line` and
  loaded into that session works without its records, and fails with
  them ("no matching type"), which is item 38.
