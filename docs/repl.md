# The REPL

`tools/repl` reads Niva a line at a time and runs it on the Go backend:

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

## How it works

By replay. The lines kept so far, then the new one, are written to
`repl.niva` at the project root and run as a program, and only the new
line's output is shown. A line that doesn't build or fails when run is
reported and not kept, so the session carries on without it. Each line
takes under a second on MPL.

## What to know

- **One line at a time.** A declaration has to fit on one line, as
  `twice` does above.
- **Nothing prints by itself.** A line shows only what it prints: use
  `showSimplified`, or `text echo`.
- **`main.niva`'s declarations are there, its top-level code isn't.**
  `showSimplified` comes from `main.niva`, but its `x` doesn't: only the
  entry file's top-level code runs.
- **Every line runs the ones before it again,** so their output is
  computed again too, though not shown, and a long session gets slower.
- **Other builds see the session.** While it runs, `tools/check` and the
  demo compile `repl.niva`'s declarations along with everything else.

Why it's built this way, and the other routes, are in
[series 010](design/implementation/010-repl/README.md).
