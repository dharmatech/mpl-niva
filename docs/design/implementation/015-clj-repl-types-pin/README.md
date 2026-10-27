# 015 clj-repl-types-pin

**Status:** Done; landed at `c830f8c` (2026-10-26).

Moves the pin from `7ac94536` to `self-host` at `622df25f`, our
[#464](https://github.com/gavr123456789/Niva/pull/464): in Clojure's
REPL mode, every package's types are emitted before any package's
functions (feedback item 37). Gavr approved it without comments and
merged it unchanged. With it, MPL's REPL module loads on Babashka, JVM
Clojure and let-go from stock `self-host`, so
[`tools/check-repl`](../014-check-repl/README.md) passes and goes in
the README. MPL's code doesn't change.

## Decisions

- **Pin the tip, `622df25f`:** #464 is the only commit since
  `7ac94536`, and its tree is our branch's, `2cda679a`.
- **Remove MPL's `.niva_go/` and `.niva_clj/`,** for item 33, as at
  every pin.
- **No re-check of the notes:** #464 changes only REPL mode, which no
  note is about.
- **`tools/check-repl` in the README, not in `tools/check`:** `lg`
  isn't one of NIN's runtimes, and the REPL module isn't what MPL's
  checks build.

## What changed

- `tools/build-self-hosted-niva` pins `622df25f`, and
  `~/src/Niva-self-host` was rebuilt there.
- The pin in the [Niva notes](../../../niva-notes.md) and
  [The self-hosted compiler](../../../self-hosted-compiler.md), whose
  paragraph on `tools/check-repl` no longer says it fails.
- The README runs `tools/check-repl` after the Clojure checks.
- [Niva feedback](../../../niva-feedback/README.md): #464 merged, and
  item 37 fixed.
- Series [010](../010-repl/README.md) and
  [014](../014-check-repl/README.md) say so.

## What we checked

At `622df25f`, from fresh `.niva_go/` and `.niva_clj/`:

- **Go:** `tools/check` passes all 151 checks.
- **Babashka and JVM Clojure:** `tools/check --backend=clj` passes all
  151 checks on each, and the demo matches Go's 34 lines on both.
- **REPL mode:** `tools/check-repl` passes on `bb`, JVM Clojure and
  `lg` (with `LG=~/src/let-go/build/lg`). Lines sent one at a time
  into a live session still stop at feedback item 38, next in line.
