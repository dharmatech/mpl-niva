# What a sound REPL needs

Written 2026-10-27, after filing item 38 as
[#465](https://github.com/gavr123456789/Niva/issues/465) with option 1
as draft PR [#466](https://github.com/gavr123456789/Niva/pull/466). The
question: does a hosted language like Niva, which compiles to Go or
Clojure, make a correct REPL awkward by nature? In short, no. A sound
one is possible, and the awkwardness has two other sources.

## Hosting isn't the problem

Clojure is hosted too: it compiles each form typed into JVM bytecode on
the fly, and has one of the best REPLs there is. So do Scala, F#,
Kotlin and ClojureScript, all compiled and hosted, and Chez compiles to
machine code and is still interactive. Interpreting or compiling is an
implementation choice. What a REPL needs is a way to add one more piece
of code, given everything already defined, to a running program.

## The host matters more than hosting

Clojure is close to an ideal host for a live REPL: it has `eval`, it
calls functions through vars, so a redefined function is the one every
caller gets, and namespaces can grow while the program runs.

Go is the opposite: whole-program, statically linked, no `eval`. So
[`tools/repl`](../../../repl.md), on Go, replays the file with each
new line. Replay is the cleanest meaning of all: the session is the
file, run from the top, like a notebook's "restart and run all". Its
costs are time, and side effects that happen again with every line.

## Where the awkwardness comes from

**NIN's REPL mode is a whole-program compiler with no memory of the
session.** `--repl-line=N` type-checks the whole file and emits the
whole module, every record and function, with the entry cut to line N.
It doesn't know what the session already has, so it sends everything
again, and item 38 follows. A principled design keeps the session in
the compiler, its typing environment and what it has emitted, and each
line then emits only what changed: new or changed definitions, and the
new expression. NIN's `--incremental` mode, "a persistent compiler
session", might be a place to start. Option 1 is the pragmatic version:
definitions that are safe to repeat, as `defonce` and hot reload's
`^:clj-reload/keep` are.

**Redefining things in a live, typed session is hard everywhere.** The
core of item 38 shows in plain Clojure, with no Niva:

```clojure
(defrecord Square [side])
(def shape (->Square 3))
(defrecord Square [side])
(instance? Square shape)    ; => false
```

Every language with a live image decides what redefining a type means.
Common Lisp migrates existing instances to the new class. OCaml's
toplevel and GHCi let the new type shadow the old one, and old values
keep the old type, so the errors name two types, `t/1` and `t/2` in
OCaml, `Ghci1.T` and `T` in GHCi: item 38 as a type error. Erlang keeps
two versions of the code loaded. Static typing adds one more case: a
function redefined with a new type leaves callers checked against the
old one.

## The three decisions

A correct REPL for Niva makes these explicit:

1. **A line sees everything defined before it.**
2. **Redefining a function replaces it** for every caller, which
   Clojure's vars give for free. A change of its type is checked
   against the code that uses it, or rejected.
3. **Redefining a type** is either rejected, "start a new session", or
   shadows the old one with a clear error.

None of this conflicts with Clojure as the host. It needs a compiler
that knows about sessions, not only programs.

## Where option 1 stands

It covers the common case, sending lines and loading the module again,
and leaves one gap: a record whose fields change keeps its old fields,
silently. Against decision 3, it should say "`Square`'s fields changed;
start a new session" instead. That's a follow-up for once Gavr has
settled #465.
