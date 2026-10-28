# 017 check-repl-line

**Status:** Done; landed at `68cc60a` (2026-10-28).

`tools/check-repl` also sends a line into a live session now: after
loading MPL's REPL module, it loads `main.niva`'s last line, built
alone with `--repl-line`, as an editor's evaluate-line would, and
checks that the session prints Go's last line again. That's what
feedback item 38 broke, so the check catches it coming back. It's
option 2 of those proposed in
[series 016](../016-clj-repl-records-pin/README.md).

## Decisions

- **The last line, counted from the end:** the last line of
  `main.niva` that isn't blank, 68 today, so it follows the demo as it
  changes. `--repl-line=N` builds the entry lines from N on, so the
  last line is a module of one line, as an editor sends it. Series 016
  checked 64, five lines; one is enough for item 38, which any line
  using the session's `x` hits.
- **Go's demo, then its last line again,** is what the session should
  print. Go can't print the line alone: `--repl-line` only changes
  Clojure's REPL mode. So this holds while `main.niva`'s last line
  prints one line, as a `showSimplified` does, and the script says so.
- **Each runtime's session as an editor would load it:** `load-file`
  on `bb`; `clojure -M -i main.clj -i line.clj` on JVM Clojure, since
  `-e` would also print each value; and the two modules as one file on
  `lg`, which has no `load-file`.
- **Only after the demo matches:** a module that doesn't load fails
  the line too, so a runtime that fails the demo stops there.
- **A failure shows the lines Go doesn't print:** the session prints
  the demo before the line fails, so the four lines shown skip those,
  and the error is what's left.
- **Not covered: a value kept across a reload,** option 3 in series
  016. It needs a Clojure form naming generated functions, which would
  tie the script to how NIN names them.

## What changed

- `tools/check-repl`, as above.
- The README, [The self-hosted compiler](../../../self-hosted-compiler.md),
  and series [014](../014-check-repl/README.md), its "not covered",
  [016](../016-clj-repl-records-pin/README.md), its proposal, and
  [010](../010-repl/README.md) say so.

## What we checked

- **At the pin, `dccd5143`:** each of `bb`, JVM Clojure and `lg` (with
  `LG=~/src/let-go/build/lg`) prints the demo's 34 lines as Go does,
  then 35 with line 68, and the script exits 0. All three take 6.5 s;
  `bb` alone 2.4 s, from 1.7 s.
- **At `622df25f`,** with #464's branch, whose tree it is
  (`NIVA_SELF_HOST=~/src/Niva-self-host-clj-repl-order`): the demo
  matches on all three, the line stops at "no matching type" on all
  three, and the script exits 1.
- **At `7ac94536`,** with #463's branch, whose tree it is
  (`~/src/Niva-self-host-clj-ops`): `bb` fails the demo at "Unable to
  resolve symbol: mpl_tree_Num", item 37, and the line isn't tried.
- `tools/check-repl bb` checks `bb` alone; `lg` without a binary is
  skipped; an unknown runtime is refused before anything is built.
