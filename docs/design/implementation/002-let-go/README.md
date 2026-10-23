# 002 let-go

**Status:** Done (2026-10-23). An experiment: nothing in `mpl/`,
`tests/` or `tools/` changed.

Tries [let-go](https://github.com/nooga/let-go), a Clojure dialect
written in Go, as the runtime for Niva's Clojure output in place of
Babashka. Designed and run in one conversation.

## Why

The self-hosted compiler's Clojure output runs on Babashka or JVM
Clojure. nooga, let-go's author, wants Niva to target let-go too. He
ran Niva's Clojure output on it himself and filled the JVM interop gaps
it hit in [let-go #1085](https://github.com/nooga/let-go/pull/1085),
merged 2026-10-22, so Niva's runtime, `niva/core.clj`, now loads
unchanged. MPL is a real program to test that with.

## How we ran it

- `lg` built from let-go's `main` at `18df3ad5`, the merge of #1085,
  in a scratch clone.
- The self-hosted compiler at the pinned `6af8f9f6`. Its Clojure output
  ran on `lg` as in
  [The self-hosted compiler](../../../self-hosted-compiler.md#on-let-go),
  and on `bb` for comparison.
- MPL as laid out, and one-file copies made by concatenating the
  sources, since `bb` can't use a type from another file: `mpl/*.niva`
  with `main.niva` for the demo, and `mpl/*.niva`, `tests/*.niva` and
  `check.niva` for the checks.
- A probe of what simplification will need: `gcd` by recursion, `/`
  and `%`, `sortedBy:` on a union, `mut` with `forEach:` and
  `whileTrue:`, map and set literals, and string methods.

## What we found

| Program | `bb` | `lg` |
|---|---|---|
| The demo, as laid out | fails at a type from another file | runs, same output as `bb` gives in one file |
| The checks, as laid out | fails at a type from another file | 31 of 31 pass\* |
| The demo in one file | runs | same output |
| The checks in one file | 31 of 31 pass\* | 31 of 31 pass\* |
| The probe | runs | same output |

\*Once `niva.core/List__+` was renamed to `List__plus` in the
generated code; see below.

Where both ran, the output was the same: ratios, sorting, maps, sets
and errors. On a run-time error both exit 1 when run directly. `lg` ran
the checks in about 0.03s, against 0.04 to 0.07s for `bb`.

The one difference is a type from another file. Niva's Clojure backend
names a type in a folder with a dot, `(defrecord mpl.tree_Num [value])`,
and calls `->mpl.tree_Num`. JVM Clojure and Babashka read that as a
class that doesn't exist, which is Niva feedback item 3. let-go accepts
it, so on `lg` that bug doesn't show and MPL runs as laid out. That's
let-go being more lenient than Clojure, not Niva being right.

What's left is Niva's Clojure backend.
[Niva feedback](../../../niva-feedback/README.md) lists these:

- `+` on two lists calls a function the runtime doesn't have (17).
  This is all that stops MPL's checks on `lg`.
- A type declared in one file can't be used from another (3). This
  stops MPL on `bb` and JVM Clojure.
- `/` on two `Int`s gives a ratio: `17 / 5` is `17/5` (18).
- A set literal with a repeated item doesn't load (19).

Item 3 was already known; the other three are new.

## Decisions

- **Go stays first.** Clojure comes back once list `+` works, on `lg`
  first and on `bb` once a type can be used across files.
- **`bb` too, not `lg` alone.** `lg` accepts code that Clojure rejects,
  so a Clojure check on `lg` alone would hide bugs like item 3.
- **No let-go script in `tools/` yet.** The checks stop at `List__+`
  until Niva fixes it, and patching the generated code in a script
  would hide the bug.

## Next

Once list `+` is fixed, give `tools/check` a way to run the Clojure
output on `lg`, and on `bb` once item 3 is fixed. The bugs above go to
Gavr one at a time, after
[#435](https://github.com/gavr123456789/Niva/issues/435). nooga may
want to know that `lg` accepts a dotted `defrecord` name that JVM
Clojure rejects.
