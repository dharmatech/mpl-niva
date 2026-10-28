# 021 repl-values

**Status:** Done; landed at `c99c9f9` (2026-10-28).

`tools/repl-jvm` prints a line's value by itself, as a REPL should:
`10` shows `10`, where before it showed nothing. Each line is built
first as `resN = line`; if that builds, `resN` is printed as `echo`
prints it, unless it's Unit. A line that doesn't build that way is built
as it is, as before. `tools/repl`, on Go, `bb` and `lg`, is unchanged:
a line there shows only what it prints.

```
niva> 10
10
niva> x = "x" sym
niva> x + x
mpl_tree_Sum terms: {mpl_tree_Sym name: "x" mpl_tree_Sym name: "x"}
niva> res1 + 1
11
niva> x showSimplified
x   =>   x
```

## Why

A REPL that shows nothing for `10` makes you write `echo` after every
expression. This version is in the REPL, a few lines of Clojure, and
shows how values should print. The principled version is NIN's REPL mode
printing a value with its type, which goes with a session-aware REPL
mode, step 4 of
[the path to a sound REPL](../010-repl/sound-repl-path.md#the-steps),
as one proposal to Gavr.

## How a line runs

1. A variable's declaration or an assignment, `x = …`, `x::T = …`,
   `mut x = …` or `x <- …`, is built as it is, as before.
2. Any other line is written to `repl.niva` as `resN = line` and built
   with NIN in this JVM. If it builds, it's loaded, and `resN` is looked
   up in the session's namespace, `niva.repl.session`, and printed with
   `niva.core/Any__echo`, unless it's Unit or `echo` printed it already.
3. If it doesn't build, the line is built as it is: a type, a method, a
   parse error. A wrong line fails both ways, and the second build's
   message is shown, as before.

A line is kept in the form that ran, so `res3` can be used later.

## Decisions

- **N counts the lines kept as `resN = …`, from 1,** not the line
  number. The names follow each other, so the third expression you
  entered is `res3`, whatever declarations came between. The REPL
  doesn't show the name, so it should be one you can count; line
  numbers would skip each declaration. From 1 rather than Scala's
  `res0`, as Niva counts lines and `--repl-line` from 1. A line whose
  value isn't printed, Unit or echo's, takes a number too, since it's
  kept as `resN = …`: every expression you enter has one.
- **Unit is `:unit`.** NIN's Clojure ends a Unit method with `:unit`,
  and `Any__toString` shows it as `()`. A value equal to `:unit` isn't
  printed, so `x showSimplified` prints only its own line.
- **A line ending in `echo` isn't printed twice.** `echo` returns what
  it prints, `T echo -> T` in NIN and `Any__echo` in its runtime, so
  `"" echo` builds after `resN =`, and the demo's nine echo lines would
  each print twice. The line's module is checked for
  `(def resN (niva.core/Any__echo`, which covers `echonl` too, a line in
  parentheses and one with a comment after it. The value is still kept:
  after `"a" echo`, its `resN` is `"a"`. Considered instead: printing no
  value when the line printed anything, which would hide the value of
  `(x echo) + y`, and checking the source for `echo` at the end, which
  misses a comment after it.
- **Variable declarations and assignments are recognized by their
  text,** before `resN =`, since NIN builds some of them there.
  `res1 = x = 5` builds as `(def res1 (let [x 5] :unit))`: `x` is local
  to `res1`'s value, though later lines type-check against it, and
  `mut n = 1` is the same. A first check by hand found
  `res4 = x = "y" sym` refused, but only because `x` was declared
  already.
  `res1 = n <- 5` gets as far as the Clojure backend, which refuses it,
  at about 40 ms more a line. Every other declaration, `type`, `union`,
  `enum`, `errordomain`, `extend`, `constructor` or a method, fails to
  parse after `resN =`, so it's left to the second build.
- **Printing is part of running the line,** in the same error handling:
  a value that fails as it's printed, such as a lazy `map:` that
  divides by zero, is reported and not kept.
- **Printed as `echo` prints it,** with `Any__echo`, so how values look
  is left to NIN's runtime for now. See the open questions.

## Open questions

- **How MPL's expressions should print.** `x + x` shows as
  `mpl_tree_Sum terms: {mpl_tree_Sym name: "x" mpl_tree_Sym name: "x"}`,
  the record as the runtime shows it, with its package in its name.
  MPL's `text`, `x + x`, reads better. Whether the REPL should use a
  method like `text` when a type has one, or Niva should have a way for
  a type to say how it prints, is open. A record declared in the session
  shows as `repl_Point x: 1 y: 2`.
- **Whether strings get quotes.** `"hi"` shows as `hi`, as `echo` shows
  it, so a string looks like a name. Inside a list or a record, strings
  are quoted: `{"a" "b"}`. Scala's and Clojure's REPLs quote them.
- **Whether the name shows.** Scala prints `val res0: Int = 10`; this
  prints `10`, so a value's name is found by counting. NIN printing a
  value with its type could show the name too.

## What's known not to work yet

- **A variable you name `resN` yourself** stops values from printing
  from that N on: after `res2 = 5`, `10` fails to build as `res2 = 10`,
  is built as it is, and prints nothing, and so does each value after
  it.
- **Other declarations are built twice,** first as `resN = …`, which
  fails at parsing: about 6 ms more for a method and 8 ms for a type,
  against about 160 ms a line.
- **The message for an unknown name lists the session's variables,**
  which now include `res1` and the rest.
- **A statement that NIN builds after `resN =`** would be taken for an
  expression. Variable declarations and assignments are the ones known,
  and recognized by their text; another would need adding.
- Series 018's to 020's gaps hold: a record whose fields change keeps
  its old fields, and loading is most of a line's cost.

## Found in NIN on the way

Not filed; for [the Niva notes](../../../niva-notes.md) if they matter:

- **A variable's declaration parses as an expression.** `a = x = 5`
  type-checks, with `x` in scope after it, but the Clojure backend makes
  `x` local to `a`'s value, and Go's build fails with "undefined: x".
- **An assignment to an undeclared variable builds on Clojure.**
  `m <- 3`, with no `m`, is emitted as `(def m 3)`; the Go backend
  refuses it, "assignment to non mut var m". `tools/repl-jvm` keeps it,
  as it did before this series.

## What changed

- [`tools/repl-jvm.clj`](../../../../tools/repl-jvm.clj): a line is
  tried as `resN = line` and its value printed. `tools/repl-jvm`, the
  launcher, is unchanged.
- [The REPL](../../../repl.md) says so, and series
  [020](../020-repl-jvm/README.md) points here.

## What we checked

At the pin, `dccd5143`, with Clojure CLI 1.12.6 on OpenJDK 25, in a copy
of the project, against `tools/repl-jvm` as it was at `92a430e`. The
transcripts are in [the checks](checks.md).

- **The demo, line by line:** `main.niva`'s 40 lines from `x = "x" sym`,
  piped in, print what `tools/nin run --backend=go` prints, 34 lines and
  the blank line the REPL ends with, as before, in 7.4 s against 7.5 s.
  None prints a value: `x`, `y` and `z` are declarations, `show:`,
  `showBool:` and `showSimplified` return Unit, and the echo lines'
  values are echo's.
- **Values:** `10`, `x + x`, `{1 2}`, `res1 + 1`, `"hi"`, `x == x`, a
  set, a list of strings, and `n` after `mut n = 1` and `n <- 5`, print;
  `x showSimplified` and the echo lines print only their own output.
- **Lines that fail:** 24 lines, among them a division by zero, an
  unknown method, two parse errors, a type, a method and a variable
  declared twice, an index out of range, `mut`, `<-` and `"" echo`,
  print what they printed before, but for the four values now shown and
  the variables listed for an unknown name.
- **The second build:** 20 lines of each kind, against the script
  before.
- **On a terminal,** under `script`: the prompt shows, values print
  after it, and `repl.niva` is removed at the end.
- **A project that doesn't build:** the message, and exit 1, as before.
