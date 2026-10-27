# 013 clj-collection-ops-pin

**Status:** Done; landed at `8433333` (2026-10-27).

Moves the pin from `a4429c50` to `self-host` at `7ac94536`, our
[#463](https://github.com/gavr123456789/Niva/pull/463): on Clojure,
`+` and `-` on lists, sets and maps call the runtime's `plus` and
`minus`, and lists get `any:` (feedback items 17 and 8). Gavr approved
it without comments and merged it unchanged. With it, MPL runs on every
backend from stock `self-host`. MPL's code doesn't change.

## Decisions

- **Pin the tip, `7ac94536`:** #463 is the only commit since
  `a4429c50`.
- **Remove MPL's `.niva_go/` and `.niva_clj/`,** for item 33, so the
  Clojure runtime is the new one, with `List__any`.
- **No new re-check of the notes:** #463 only changes the two notes it
  fixes.

## What changed

- `tools/build-self-hosted-niva` pins `7ac94536`, and
  `~/src/Niva-self-host` was rebuilt there.
- [Niva notes](../../../niva-notes.md): the pin; the Clojure note is
  now let-go's alone; list `+` and `any:` are gone from the gaps.
- The pin in [The self-hosted compiler](../../../self-hosted-compiler.md).
- The README says the demo and the checks run on Clojure too.
- [Niva feedback](../../../niva-feedback/README.md): #463 merged, and
  items 17 and 8 fixed.

## What we checked

At `7ac94536`, with nothing shimmed:

- **Go:** `tools/check` passes all 151 checks.
- **Babashka and JVM Clojure:** `tools/check --backend=clj` passes all
  151 checks on each (`--runtime=bb`, `--runtime=jvm`), and the demo's
  output matches Go's line for line on both.
- **REPL mode** (`build --repl`, loaded into `bb`): it stops at
  `mpl_tree_Num`, feedback item 37. With the records moved to the top,
  and nothing else changed, the whole module loads and runs the demo.
  Item 37 is next in line, then item 38
  ([series 010](../010-repl/README.md)).
