# 018 clj-repl-loop

**Status:** Done; landed at `15eb107` (2026-10-28).

`tools/repl --backend=clj` is a REPL with a live session: one `bb`
process loads MPL's REPL module, then each line you type, built alone
with `--repl-line`, so the session's state stays in the process and no
line runs again. It's step 2 of
[from option 1 to a sound REPL](../010-repl/sound-repl-path.md#the-steps).
On the demo's lines it prints what Go prints.

```
tools/repl --backend=clj --runtime=bb   # on bb
tools/repl --backend=clj --runtime=jvm  # or JVM Clojure, or lg
```

## The loop

1. At the start, `repl.niva` at the project root is empty, and its REPL
   module, MPL and `main.niva`'s declarations with no line, is built and
   loaded into a new `bb`.
2. Each line typed is written to `repl.niva` after the lines kept so
   far and built with `--repl-line=N`, N being its line in the file.
3. `bb` loads that module and the line's output is shown. A line that
   fails to build or run is dropped from the file, as on Go.

## Decisions

- **A pipe to `bb`'s standard input,** not a socket REPL or nREPL. The
  session runs ten lines of Clojure, written by the tool: read a file's
  path, `(load-string (slurp path))`, print a marker. It needs no port,
  ends with the tool, and the same code runs on `bb`, JVM Clojure and
  `lg`, which has no `load-file`. nREPL is what an editor would
  connect to, so it's for when the REPL moves into one; the change
  would be confined to `load`, which sends a file and reads its output.
- **A mode of `tools/repl`,** `--backend=clj` with `--runtime=bb`,
  `jvm` or `lg`, the flags `tools/check` passes to NIN. The two share
  everything but running a line: reading it, with history, the session
  file, keeping a line or dropping it, and trimming the compiler's
  message. **Go stays the default** for now, since this mode is new
  and has the gap below that Go doesn't; switching is one word.
- **Output and errors:**
  - A line that doesn't build writes no module, since the compiler
    exits 0 either way, as `tools/check-repl` checks. Its message is
    shown as on Go.
  - At run time, the session catches anything the load throws and
    prints a failed marker, then the message of the error's root cause:
    JVM Clojure wraps it as `Syntax error macroexpanding`.
  - A done marker ends each load, and the runtime's stderr goes into
    the same pipe, so what the line printed before failing is shown, in
    order.
- **Built in a temporary folder:** NIN writes `.niva_clj/` where it
  runs and finds the project from the file it's given, so the
  project's `.niva_clj/` isn't touched, and each session gets the
  compiler's own runtime (Niva feedback item 33).
- **The module before any line, loaded first:** if MPL doesn't build
  or load, that shows before the prompt, and the first line costs what
  the others do.
- **The runtime a choice, built and tested on `bb`:** `jvm` and `lg`
  run as `tools/check-repl` runs them, `LG` naming the binary. Both
  print the demo as `bb` does, but only `bb` is meant for use yet.
  Since [series 019](../019-repl-one-jvm/README.md), `jvm` runs NIN in
  the session's JVM instead, and is the default.

## What's known not to work yet

- **A record whose fields change keeps its old fields.** A line's
  module defines a record only if the session lacks it (#466), so a
  record edited in a file during a session isn't updated. A field
  added fails when the record is built, at "Wrong number of args (2)";
  a field renamed reads as `null`, silently. It needs step 3, the
  manifest check in REPL mode, a Niva PR. A record declared again in
  the session is refused by the compiler, on Go too: "such type was
  already added to methodDB".
- **Each line costs about 0.6 s of compiling:** NIN checks the whole
  project again for each line, and loading the module takes the rest,
  well under a tenth of a second. A NIN that stays running would cut
  it; not done here. Series 019 does it on JVM Clojure, at about 0.14 s
  a line.
- **A value made before an edit keeps what it was:** `p = 1 point`,
  then a new `point`, leaves `p` alone. Functions edited in a file
  apply from the next line, since every line's module sends them all
  again. On Go, every line runs everything again, so nothing is old.
- **Errors read differently:** `(1 / 0) echo` fails Go's build, and
  only at run time on Clojure, with "Divide by zero". Both drop it.
- **Ctrl-C ends the session,** runtime and all, as on Go. It's the way
  out of a line that doesn't end.

## What changed

- [`tools/repl`](../../../../tools/repl): the Clojure mode, with Go's
  replay as one function beside it.
- [The REPL](../../../repl.md) and the README say how to use it.
- Series 010's [README](../010-repl/README.md) and
  [path to a sound REPL](../010-repl/sound-repl-path.md), whose step 2
  this is.

## What we checked

At the pin, `dccd5143`, with Babashka 1.13.226, Clojure CLI 1.12.6 on
OpenJDK 25, and let-go `18df3ad5`:

- **The demo, line by line:** `main.niva`'s 40 lines from `x = "x" sym`
  on, piped into `tools/repl --backend=clj`, print what
  `tools/nin run --backend=go` prints, all 34 lines, then the blank
  line the REPL ends with. The same on `--runtime=jvm` and `lg`.
- **Time,** for those 40 lines: 24 s on `bb` and on `lg`, 29 s on JVM
  Clojure, and 26 to 33 s on Go over two runs, where each line takes
  longer than the one before. Starting takes 0.7 s on `bb` and 2.9 s on JVM Clojure, then
  0.65 s a line on `bb`.
- **Go's own mode dropped a line that printed only a blank line:** the
  five `"" echo` lines showed nothing, since `$(…)` strips trailing
  newlines. It was older than this series; the Clojure mode showed
  them. Fixed afterwards, at `4838a0e`: the Go mode prints the status
  after the output, so the newlines stay, and the demo's 40 lines then
  print what `tools/nin run --backend=go` prints there too. The lines
  that fail, below, are reported as before.
- **Lines that fail,** on `bb`: a message the type has none of, a
  method or a variable defined again, `1 / 0`, and an index out of
  range are each reported and not kept, and the session goes on. `mut`
  and `<-` work across lines, and so does a record declared in the
  session or in a file.
- **Edits during a session:** a function changed in `main.niva` shows
  on the next line; a record's field added or renamed, as above.
- `--runtime=lg` with no binary says so; an unknown option is refused.
