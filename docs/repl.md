# The REPL

Two scripts read Niva a line at a time and run it, after the lines
you've entered before:

- `tools/repl-jvm`, a live session in one JVM, which runs the compiler
  too, so a line takes about 0.14 s, and prints an expression's value.
- `tools/repl`, on the Go backend by replay, or with `--backend=clj` in
  a live session on Babashka, at about 0.65 s a line.

```
niva> x = "x" sym
niva> y = "y" sym
niva> (x + y + x) showSimplified
(x + y) + x   =>   2 * x + y
niva> Expr twice -> Expr = this + this
niva> (x twice) simplify text echo
2 * x
```

Ctrl-D ends the session.

## In one JVM

```bash
tools/repl-jvm
```

starts in about 2 s, and then each line takes about 0.14 s: the
compiler, NIN, runs in the session's JVM, so it stays warm. It has no
line editing; for the arrow keys and history, run it in rlwrap:

```bash
rlwrap tools/repl-jvm
```

An expression's value prints by itself, as `echo` prints it, and is
kept as `res1`, `res2` and so on, counting the expressions you've
entered:

```
niva> 10
10
niva> x = "x" sym
niva> x + x
mpl_tree_Sum terms: {mpl_tree_Sym name: "x" mpl_tree_Sym name: "x"}
niva> res1 + 1
11
```

Unit isn't printed, so `showSimplified` shows only its own line, and a
line ending in `echo` isn't printed twice.

## With tools/repl

The arrow keys edit the line and go back through what you've entered.
`tools/repl` runs each line on Go. `tools/repl --backend=clj` runs it
in a live session on Babashka, which starts in under a second but
compiles each line in a new process; `--runtime=lg` runs it on let-go
the same way.

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
- **On `tools/repl`, nothing prints by itself.** A line shows only what
  it prints: use `showSimplified`, or `text echo`. `tools/repl-jvm`
  prints a value as the runtime shows a record, so `text echo` reads
  better there too.
- **Don't name a variable `res2` or the like yourself** in
  `tools/repl-jvm`: values stop printing from that number on.
- **`main.niva`'s declarations are there, its top-level code isn't.**
  `showSimplified` comes from `main.niva`, but its `x` doesn't: only the
  entry file's top-level code runs.
- **On Go, every line runs the ones before it again,** so their output
  is computed again too, though not shown, and a long session gets
  slower.
- **On Clojure, a value keeps what it was made with.** A function you
  edit in a file applies from the next line, but a value made before
  the edit isn't made again.
- **On Clojure, a record whose fields you edit keeps its old fields,**
  so start a new session after such an edit.
- **Other builds see the session.** While it runs, `tools/check` and the
  demo compile `repl.niva`'s declarations along with everything else.

Why it's built this way, and the other routes, are in
[series 010](design/implementation/010-repl/README.md), the Clojure
mode in [series 018](design/implementation/018-clj-repl-loop/README.md),
the compiler in the JVM's session in
[series 019](design/implementation/019-repl-one-jvm/README.md),
`tools/repl-jvm` in [series 020](design/implementation/020-repl-jvm/README.md),
and its values in [series 021](design/implementation/021-repl-values/README.md).
