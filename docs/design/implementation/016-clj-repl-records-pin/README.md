# 016 clj-repl-records-pin

**Status:** Done; landed at `105e213` (2026-10-28).

Moves the pin from `622df25f` to `self-host` at `dccd5143`, our
[#466](https://github.com/gavr123456789/Niva/pull/466): in Clojure's
REPL mode, each record is defined once per session,
`(when-not (resolve 'X) (eval '(defrecord X […])))`, so a
`--repl-line=N` module loaded after the full module no longer breaks
the session at "no matching type" (feedback item 38, option 1 of
[#465](https://github.com/gavr123456789/Niva/issues/465)). Gavr merged
it unchanged after one review round. MPL's code doesn't change.

## Decisions

- **Pin the tip, `dccd5143`:** #466 is the only commit since
  `622df25f`, and its tree is our branch's, `381a8765`.
- **Remove MPL's `.niva_go/` and `.niva_clj/`,** for item 33, as at
  every pin.
- **No re-check of the notes:** #466 changes only REPL mode, which no
  note is about.
- **`tools/check-repl` unchanged:** whether it should also load a line
  module after the full one is the user's call, proposed below.

## What changed

- `tools/build-self-hosted-niva` pins `dccd5143`, and
  `~/src/Niva-self-host` was rebuilt there.
- The pin in the [Niva notes](../../../niva-notes.md) and
  [The self-hosted compiler](../../../self-hosted-compiler.md).
- [Niva feedback](../../../niva-feedback/README.md): item 38 fixed.
- Series [010](../010-repl/README.md) says so, in its README,
  [which runtime](../010-repl/runtimes.md) and
  [from option 1 to a sound REPL](../010-repl/sound-repl-path.md),
  whose step 1 is done.

## What we checked

At `dccd5143`, from fresh `.niva_go/` and `.niva_clj/`:

- **Go:** `tools/check` passes all 151 checks.
- **Babashka and JVM Clojure:** `tools/check --backend=clj` passes all
  151 checks on each, and the demo matches Go's 34 lines on both.
- **REPL mode:** `tools/check-repl` passes on `bb`, JVM Clojure and
  `lg` (with `LG=~/src/let-go/build/lg`).
- **A line after the full module,** from stock: MPL's REPL module
  (564 lines) and `--repl-line=64` (532), with all 10 records guarded,
  loaded into one session print the demo's 34 lines, then line 64's
  five `showSimplified` lines (lines 64 to 68), as Go prints them. On
  `bb` with `load-file` on both, on JVM Clojure with
  `clojure -M -i full.clj -i line.clj`, and on `lg` with the two files
  run as one, since it has no `load-file`. At `622df25f` this stopped
  at "no matching type" on all three
  ([draft 013](../../../niva-feedback/013-clj-repl-line-records.md)).

Babashka 1.13.226, Clojure CLI 1.12.6 on OpenJDK 25, let-go
`18df3ad5`.

## Proposed: a line module in `tools/check-repl`

`tools/check-repl` loads the full module only, so nothing would catch
item 38 coming back. Options, for the user to choose:

1. **Leave it as is.** The session check above, and the PR's, record
   that it works at `dccd5143`; it's run again by hand when REPL mode
   changes.
2. **Also load a line module after the full one,** on each runtime,
   and compare with Go's demo followed by its last lines. That's one
   more build, about 0.7 s, on top of the 1.7 s `tools/check-repl bb`
   takes now. Rather than a fixed 64, the line would be counted from
   the end of `main.niva`, so the check follows the demo as it
   changes. `lg` gets the two files as one, as above.
3. **2, plus a value kept across a reload:** after the line, a
   `(def e …)` made in the session, the full module loaded again, and
   `e` shown, as the PR checked. It needs a Clojure form per runtime
   rather than only Niva, so it's more to keep in step.

2 covers what item 38 was, at little cost; 3 is the other half of
option 1, reloading, but ties the script to the generated names.
