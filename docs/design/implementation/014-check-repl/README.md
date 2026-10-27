# 014 check-repl

**Status:** Done; landed at `1c60ce0` (2026-10-26).

`tools/check-repl` builds MPL's Clojure REPL module, the one namespace
that `nin build --backend=clj --repl` writes, loads it on `bb`, JVM
Clojure and let-go, and checks that each prints the demo as
`tools/nin run --backend=go` does. `tools/check` covers MPL as a
program on Go, `bb` and JVM; nothing covered it in a REPL, which is
where [series 010](../010-repl/README.md) is headed.

## Decisions

- **The demo, compared with Go's.** It's what loading `main.niva`'s
  module prints, so one diff per runtime covers it. Building
  `check.niva` in REPL mode too would cover the 151 checks; it's left
  until the REPL needs more than the demo.
- **Three runtimes, and a missing one is skipped,** saying so, with
  exit 0. let-go isn't one of NIN's runtimes, and `lg` isn't on `PATH`
  here, so `LG` names its binary: it has to be built from let-go's
  `main` (see [The self-hosted compiler](../../../self-hosted-compiler.md#on-let-go)).
- **In a copy of the project,** in a temporary folder: `.niva_clj/` is
  left as it was, and the module always comes with the compiler's own
  runtime, not a stale one (Niva feedback item 33).
- **Not part of `tools/check`, nor in the README yet:** at the pin,
  `7ac94536`, all three runtimes stop at feedback item 37. Its fix,
  our [#464](https://github.com/gavr123456789/Niva/pull/464), was
  merged into `self-host` on 2026-10-27 (01:38 UTC) as `622df25f`,
  after the pin. Once the pin moves past it, this can join them.
- **Not covered:** lines sent one at a time into a live session, which
  item 38 breaks on all three runtimes; NIN's interactive
  `run --repl`; and `ixgo`, which isn't installed here.

## What was checked

- At the pin: `bb`, JVM and `lg` each fail to load the module, at
  `Unable to resolve symbol: mpl_tree_Num` on `bb` and JVM, and the
  script exits 1.
- With `NIVA_SELF_HOST=~/src/Niva-self-host-clj-repl-order`, #464's
  branch: all three print the demo's 34 lines as Go does, exit 0, in
  about 4 s.
- `tools/check-repl bb` checks `bb` alone; `lg` without `LG` is
  skipped; an unknown runtime is refused before anything is built.
