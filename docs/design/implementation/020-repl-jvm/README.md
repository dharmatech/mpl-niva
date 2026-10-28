# 020 repl-jvm

**Status:** Done; landed at `b684836` (2026-10-28).

`tools/repl-jvm` is a Niva REPL that's one process: a JVM that runs
NIN, the session, and the REPL's own loop, in
[`tools/repl-jvm.clj`](../../../../tools/repl-jvm.clj). The bash script
only finds NIN's jar and Clojure, then becomes the JVM. It takes
[series 019](../019-repl-one-jvm/README.md)'s JVM mode out of
`tools/repl`, which runs on Go, `bb` and `lg` again, with
`--backend=clj` meaning `bb`. Since
[series 021](../021-repl-values/README.md), `tools/repl-jvm` prints a
line's value.

```
tools/repl-jvm
rlwrap tools/repl-jvm    # with line editing
```

## Why

In series 019 a JVM session was two processes: `tools/repl`, in bash,
read the lines and kept the session file, and the JVM built and ran
them, the two talking through a pipe with markers. The bash did only
what a REPL's own loop does, so that can be Clojure in the JVM, as
Clojure's own REPL is. A script of its own also shows how little the
launcher does.

## Decisions

- **A launcher that becomes the JVM:** `exec java …` replaces the bash
  process, as the `clojure` command does. Before that, it finds NIN's
  jar as `tools/nin` does and Clojure's classpath with
  `clojure -Spath`, and makes the temporary folder the JVM runs in,
  since NIN writes `.niva_clj/` where its JVM runs.
- **The REPL in Clojure,** beside it: the prompt, the session file,
  building each line with NIN in this JVM, loading it, keeping it or
  dropping it, and trimming messages, as `tools/repl` does. No pipe and
  no markers: a failed build or line is an exception, caught where it's
  thrown.
- **No line editing:** `read-line` reads plain lines.
  `rlwrap tools/repl-jvm` adds editing and history; `rlwrap` is then a
  second process, as it is in Clojure's `clj`. JLine, in the JVM, would
  keep it one; it's a dependency to fetch, left for later.
- **The prompt only on a terminal,** as bash's `read -p` shows it, so
  lines piped in print only their output and can be compared with Go's.
  It asks `System.console().isTerminal()`, which is from JDK 22.
- **Cleanup in a shutdown hook:** `repl.niva` and the temporary folder
  go at the end, after Ctrl-D, and after Ctrl-C too, which runs the
  JVM's shutdown hooks. A `repl.niva` already there is refused, as
  `tools/repl` refuses it, and left in place.
- **`tools/repl --runtime=jvm` is refused,** naming `tools/repl-jvm`,
  rather than handing over to it, so each script has one job.
  `tools/repl --backend=clj` means `bb` again.
- **Some logic twice:** refusing a leftover `repl.niva`, keeping or
  dropping a line, and trimming messages are in both scripts, in bash
  and in Clojure, a few lines each.

## What's known not to work yet

- **A crash ends the REPL with it,** with nothing left to report it,
  as in any one-process REPL.
- **Ctrl-C ends the session,** as before, and is the way out of a line
  that doesn't end.
- Series 018's and 019's gaps hold: a record whose fields change keeps
  its old fields, loading is most of a line's cost, and NIN's main
  class is named.

## What changed

- [`tools/repl-jvm`](../../../../tools/repl-jvm) and
  [`tools/repl-jvm.clj`](../../../../tools/repl-jvm.clj), new.
- [`tools/repl`](../../../../tools/repl): the JVM mode out, `bb` the
  Clojure default again, and `--runtime=jvm` refused.
- The README, [The REPL](../../../repl.md), series
  [018](../018-clj-repl-loop/README.md),
  [019](../019-repl-one-jvm/README.md) and 010's
  [runtimes](../010-repl/runtimes.md) say so.

## What we checked

At the pin, `dccd5143`, with Clojure CLI 1.12.6 on OpenJDK 25,
Babashka 1.13.226, let-go `18df3ad5` and rlwrap 0.47:

- **One process:** during a session, `ps` shows one `java`, with no
  bash left as its parent.
- **The demo, line by line:** `main.niva`'s 40 lines from `x = "x" sym`
  on, piped into `tools/repl-jvm`, print what
  `tools/nin run --backend=go` prints, in 7.5 s, of which starting is
  2.0 s.
- **Lines that fail:** series 019's twenty lines print the same as on
  `tools/repl --backend=clj`.
- **On a terminal,** under `script`: the prompt shows, and
  `rlwrap tools/repl-jvm` takes lines, prints their output and ends at
  Ctrl-D.
- **Cleanup:** after Ctrl-D and after a SIGINT, `repl.niva` and the
  temporary folder are gone. With a `repl.niva` already there, it
  refuses, exits 1, and leaves it.
- **An edit during a session:** `showSimplified` changed in
  `main.niva` shows on the next line.
- **`tools/repl`:** the demo matches Go's on `bb` (24 s), `lg` (25 s)
  and Go (33 s), and `--runtime=jvm` is refused with exit 2.
