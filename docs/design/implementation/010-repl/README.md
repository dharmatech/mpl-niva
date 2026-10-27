# 010 repl

**Status:** Done at `d8b6a9a` (2026-10-26), as a first step: `tools/repl`, a REPL on
the Go backend by replay. Editor support, such as F8 to send a line, is
left until there's a better story for what to use.

MPL suits an interactive style: build an expression, simplify it, try
another. Niva has no REPL where you type Niva, so this series looked at
what there is and added the smallest thing that works on MPL today.

## What Niva has

At `self-host` `781c5b05`:

- **The Kotlin compiler's inline REPL**: `> y` in a file, and after a
  run `//> 9` appeared above it. JVM target only, and commented out on
  `main` and `self-host` alike. Issues #23, #34, #62, #65, #240 and #318
  are about it.
- **NIN's Clojure REPL mode**, from 2026-10-20 to 10-22:
  - `nin run --backend=clj --repl` compiles the program into one
    namespace, each entry-level `x = …` a `(def x …)`, and starts
    `bb repl` or `clojure -r` in it. What you type there is Clojure, with
    generated names such as `(main__mpl__tree_Expr__showSimplified x)`.
  - `nin build --backend=clj --repl --repl-line=N` type-checks the whole
    file but emits only the entry lines from line N, for a live session
    that already has the earlier ones: what an F8 would need.
  - `nin watch` recompiles on save and reloads into a live Clojure
    session.
- **The Go backend** has none. `--target=ixgo` runs the generated Go in
  an interpreter, but as a whole program, with no session.
- **The VS Code extension**, `niva-vscode-bundle`, has the language
  server and an install command, and nothing that evaluates.

## What was considered

- **Clojure's REPL mode, in a live Babashka session.** The true REPL:
  state lives in the running process, and each line costs the same. On
  MPL it doesn't load today, for four reasons:
  1. in the one-namespace module, a package's records come after the
     functions of the packages before it that use them;
  2. the records keep their package's dots, `mpl.tree_Num`, which
     Clojure reads as class names that don't resolve;
  3. the runtime calls `List__+` but defines `List__plus`, as the
     [Niva notes](../../../niva-notes.md) say;
  4. the runtime has no `List__any`, as the notes say too.

  A one-file program with a union loads, so 1 and 2 are how feedback
  item 3, a type used from another file, shows up in REPL mode. Patched
  by hand, all four, MPL's demo ran in bb in 0.06 s with the same output
  as Go. For F8, a line built with `--repl-line` and loaded into the same
  session worked, but only without the module's records: loading them
  again makes new classes, and the session's values then match no
  branch. So this route needs compiler work, and Gavr is on a break from
  new asks.
- **Go, by replay.** A whole cycle on MPL, from starting the JVM to
  running the binary with Go's build cache warm, takes 0.7 s. So a REPL
  can keep the lines entered so far and run them all again with each new
  one, showing only the new one's output. It needs no compiler changes.
  Each line re-runs the ones before it, so time grows with the session
  and side effects repeat; for MPL, which is pure, neither matters much.
- **Both, behind one `nin repl`.** The two would behave differently, one
  re-running everything and the other keeping live state, so not as a
  first step.

Go by replay was chosen, to have something to use now. The Clojure
route stays the candidate for a true REPL.

## What was done

- [`tools/repl`](../../../../tools/repl): the REPL. It writes the session
  to `repl.niva` at the project root, where the compiler finds `mpl/`
  (from a folder it only finds what's under it), and removes it at the
  end. It won't start if `repl.niva` is already there. A hidden
  `.repl.niva` was tried, but other builds compile it all the same, so
  the plain name is easier to notice if one is ever left behind.
- `.gitignore` ignores `repl.niva`.
- [The REPL](../../../repl.md): how to use it, and its limits.

## MPL on Clojure, as a program

Since a Clojure REPL wants MPL working on Clojure first, the program
was tried there too, after this landed. `nin run --backend=clj` fails on
`bb` and on JVM Clojure alike, at feedback item 3: records with dots in
their names, and another namespace naming a record class with `/`.
Patched by hand in the generated code, with item 8 (`any:`) and item 17
(list `+`) shimmed in the runtime, the demo's output on `bb` matches
Go's line for line, and all 151 checks pass on `bb` and on JVM Clojure.
So four fixes to NIN make MPL work on every backend, and three of them
are also three of the REPL mode's four blockers.

## Since

Later on 2026-10-26 the break from asks ended, and the Clojure sections
above went out of date. As of the pin at `a4429c50`
([series 012](../012-clj-records-pin/README.md)):

- **Blocker 2, and item 3,** fixed by our
  [#461](https://github.com/gavr123456789/Niva/pull/461), merged: records
  have no dots in their names, and another namespace names their class
  with `.`. let-go then can't resolve that class, so MPL no longer runs
  on `lg` ([let-go #1101](https://github.com/nooga/let-go/issues/1101)).
- **Blockers 3 and 4, items 17 and 8,** are in our
  [#463](https://github.com/gavr123456789/Niva/pull/463), open. With its
  two functions added by hand, MPL as a program passes all 151 checks
  on `bb` and JVM Clojure.
- **Blocker 1 is feedback item 37,** the only thing left stopping REPL
  mode from loading MPL. The F8 problem, a line's records defined again
  in a live session, is item 38. They're next in line after #463, one
  at a time.

With the item 37 fix, MPL's REPL module loads on `bb`, JVM Clojure
and let-go alike: [which runtime for a live REPL](runtimes.md) has the
timings, and why `bb` comes first.
