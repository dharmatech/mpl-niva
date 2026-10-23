# 000 `lg -m`: run a namespace's `-main`

**Status:** Filed 2026-10-23 as [#1096](https://github.com/nooga/let-go/issues/1096).
**Repository:** [nooga/let-go](https://github.com/nooga/let-go/issues)
**Checked against:** let-go `main` at `18df3ad5` (2026-10-22)

Found while running Niva's Clojure output on let-go in
[series 002](../design/implementation/002-let-go/README.md): Niva runs
that output on Babashka with `bb -cp generated:. -m main`, and `lg` has
nothing to match it, so we used a launcher with `-e`. nooga listed it
as out of scope in [#1085](https://github.com/nooga/let-go/pull/1085).
No existing issue covered it on 2026-10-23.

The comparison was run with `clojure` (CLI, JVM), `bb` 1.13.226 and
`lg` at `18df3ad5`. Which other dialects have `-m` comes from their
docs and source: ClojureCLR's `clojure/main.clj`, the
[ClojureScript reference](https://clojurescript.org/reference/repl-and-main),
the [nbb README](https://cdn.jsdelivr.net/npm/nbb@1.6.213/README.md)
and jank's `cli.cpp`.

Everything below the line is the issue as filed.

---

**Title:** CLI: add `-m ns` to call a namespace's `-main`, as `clojure -m` and `bb -m` do

## Summary

`lg` can't start a program at a namespace's `-main`. JVM Clojure,
ClojureCLR, ClojureScript (`cljs.main`), Babashka and nbb all have
`-m` for this, and jank has `run-main`. It's the usual entry point for
a Clojure program. #1085 left it out: "`lg` has no `-m`. That's a CLI
design question".

```clojure
;; src/app.clj
(ns app)
(defn -main [& args]
  (println "args:" args "cla:" *command-line-args*)
  :returned)
```

```sh
clojure -Sdeps '{:paths ["src"]}' -M -m app a b   # args: (a b) cla: (a b)
bb -cp src -m app a b                              # args: (a b) cla: (a b)
lg -source-paths src -m app a b                    # flag provided but not defined: -m
```

## Today

- `lg src/app.clj a b` loads the file but doesn't call `-main`.
- `-e` can call it but can't pass args. The first positional is run as
  a script (`open a: no such file or directory`), and `-e` prints
  `-main`'s return value (`:returned`).
- A launcher works: a `run.lg` holding
  `(require 'app) (apply app/-main *command-line-args*)`, run with
  `lg -source-paths src run.lg a b`. Every project needs its own.

## Proposal

`lg [-source-paths …] -m ns [args…]`:

1. Require `ns` from the source paths.
2. Bind `*command-line-args*` to every positional after `ns`. There's no
   script positional in this mode.
3. Call `ns/-main` with them.
4. Don't print what it returns. Exit nonzero if it throws, as a failed
   script does.

Most of this is in place: `-source-paths` resolves namespaces,
`*command-line-args*` exists (#204), and `-entry-frame-entry` already
knows `ns/-main`.

Design points for you to decide:

- `--main` as well as `-m`, as `-v` has `-version`.
- `-m ns/fn` to call any var, as `bb -m` allows.
- `-e` with `-m`. `clojure.main` runs init options such as `-e` first,
  then the main option.

## Context

Niva's Clojure backend runs its output with `bb -cp generated:. -m main`.
Since #1085 that output runs on `lg` too, and with `-m` it would be the
same one-line command: `lg -source-paths generated:. -m main`.
