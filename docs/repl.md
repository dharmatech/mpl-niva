# The REPL

`tools/repl` reads Niva a line at a time and runs it, on the Go backend
unless you ask for Clojure:

```
niva> x = "x" sym
niva> y = "y" sym
niva> (x + y + x) showSimplified
(x + y) + x   =>   2 * x + y
niva> Expr twice -> Expr = this + this
niva> (x twice) simplify text echo
2 * x
```

Ctrl-D ends the session. The arrow keys edit the line and go back
through what you've entered.

## In a live Clojure session

```bash
tools/repl --backend=clj
```

runs each line in one JVM, which keeps what the lines before it made,
so no line runs again. The compiler runs in the same JVM, so after 2 s
to start, a line takes about 0.14 s. `--runtime=bb` runs it on
Babashka, which starts in under a second but compiles each line in a
new process, at about 0.65 s a line, and `--runtime=lg` on let-go, the
same way.

It's newer than the Go mode, and has a gap Go's doesn't: a record whose
fields you edit in a file during a session keeps its old fields, so
start a new session after such an edit.

## How it works

On Go, by replay. The lines kept so far, then the new one, are written
to `repl.niva` at the project root and run as a program, and only the
new line's output is shown. On Clojure, the session has loaded MPL, and
each line is written to `repl.niva` the same way, then built alone
with `--repl-line` and loaded into the session.

Either way, a line that doesn't build or fails when run is reported and
not kept, so the session carries on without it. Each line takes under a
second on MPL.

## What to know

- **One line at a time.** A declaration has to fit on one line, as
  `twice` does above.
- **Nothing prints by itself.** A line shows only what it prints: use
  `showSimplified`, or `text echo`.
- **`main.niva`'s declarations are there, its top-level code isn't.**
  `showSimplified` comes from `main.niva`, but its `x` doesn't: only the
  entry file's top-level code runs.
- **On Go, every line runs the ones before it again,** so their output
  is computed again too, though not shown, and a long session gets
  slower.
- **On Clojure, a value keeps what it was made with.** A function you
  edit in a file applies from the next line, but a value made before
  the edit isn't made again.
- **Other builds see the session.** While it runs, `tools/check` and the
  demo compile `repl.niva`'s declarations along with everything else.

Why it's built this way, and the other routes, are in
[series 010](design/implementation/010-repl/README.md), the Clojure
mode in [series 018](design/implementation/018-clj-repl-loop/README.md),
and the compiler in the JVM's session in
[series 019](design/implementation/019-repl-one-jvm/README.md).
