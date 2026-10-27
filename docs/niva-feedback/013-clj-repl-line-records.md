# 013 Clojure REPL mode: a line's records break the session

**Status:** Filed 2026-10-26 as [#465](https://github.com/gavr123456789/Niva/issues/465).
Option 1 was opened right after as draft PR
[#466](https://github.com/gavr123456789/Niva/pull/466)
([draft](013-clj-repl-line-records-pr.md)). As for 005 and 006, the
issue was filed saying a draft PR for 1 would follow, then edited with
`gh api -X PATCH` to name #466, and each paragraph went in on one line.
`self-host` was still at `622df25f`.
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `622df25f` (2026-10-27), first found
at `781c5b05` (2026-10-26)

It's item 38 in the [README](README.md), found with item 37 trying a
Clojure REPL for MPL
([series 010](../design/implementation/010-repl/README.md)). With 37
fixed by #464, MPL's REPL module loads in a live session, so this is
what stands between that session and sending it Niva a line at a time.
No existing issue covered it on 2026-10-26 (`gh search issues` for
"repl", "repl-line", "defrecord", "no matching type", "hot reload",
"redefine" and "live session"; the REPL issues there are the old inline
REPL's).

Unlike 37, it needs Gavr's choice, so it's an issue with options, as
[005](005-go-list-literal-of-branch.md) and
[006](006-string-ordering.md) were. As for those, a draft PR for 1
followed it.

## What was checked

Each session loads the full REPL module, then a second module, in one
process: `load-file` on `bb` and JVM Clojure, and the two files run as
one on `lg`, which has no `load-file`. The program is the issue's,
whose line 8 is `shape size echo`; its records are `main_Circle` and
`main_Square`. The options were tried by editing the generated code.

| Second module | `bb` 1.13.226 | JVM Clojure (CLI 1.12.6, OpenJDK 25) | `lg`, let-go `18df3ad5` |
|---|---|---|---|
| line 8 (`--repl-line=8`), stock | "no matching type" | "no matching type" | "no matching type" |
| line 8 without its `defrecord`s (option 2) | `3` | `3` | `3` |
| line 8, both modules' records `(when-not (resolve 'X) (eval '(defrecord …)))` (option 1) | `3` | `3` | `3` |
| the same with a plain `when-not`, no `eval` | `->main_Square` unbound, on the first load already | "no matching type" | "nil is not a function", on the first load already |

On MPL, line 64 (`--repl-line=64`, five `showSimplified` lines) after
the full module: stock stops at "no matching type" on all three, and
both options print the five lines as Go does, on all three. A value
made in the session, `(def e (mpl__tree_Expr__+ x x))`, then the full
module loaded again, then `e` shown: stock stops at "no matching type"
on `bb` and JVM; option 1 prints `x + x   =>   2 * x` on both. Loading
the full module twice on its own works on stock, since it makes `x`,
`y` and `z` again too.

**Why the options are ordered so.** 1 is a few lines in `emitTypeDef:`
and `emitUnionDef:`, which already have hot reload's
`^:clj-reload/keep`, and covers both sending a line and loading the
module again. 2 matches how `--repl-line` already cuts the entry at
line N, but needs the emitter to skip types the IR keeps (a union's
methods need its set of classless types, #462), and doesn't cover
loading the module again. 3 is no change, for completeness: it's what a
REPL loop of ours would do meanwhile.

Everything below the line is the issue as filed, with the PR's number
put in.

---

**Title:** Clojure REPL mode: a `--repl-line` module defines its records again, so the session's values match nothing

Sending a line built with `--repl-line` into a live session breaks the
values the session already has. With `main.niva`:

```Scala
union Shape =
| Circle radius: Int
| Square side: Int
Shape size -> Int = | this
  | Circle => radius
  | Square => side
shape::Shape = Square side: 3
shape size echo
```

build the module and then line 8, and load both into one session, as an
editor's "evaluate line" would:

```
nin build main.niva --backend=clj --repl && cp .niva_clj/main.clj .niva_clj/full.clj
nin build main.niva --backend=clj --repl --repl-line=8 && cp .niva_clj/main.clj .niva_clj/line.clj
cd .niva_clj && bb -cp . -e '(load-file "full.clj") (load-file "line.clj")'
```

The module prints `3`, then the line stops at

```
no matching type
```

The same on JVM Clojure. The line module defines every record again,
`(defrecord main_Square [side])`, which makes a new class. `size` is
defined again against the new classes, while `shape` is still an
instance of the old one, so `condp instance?` matches nothing. Without
the `defrecord`s, the line prints `3`. Loading the whole module again
does the same to any value made in the session.

Hot reload keeps its records across reloads with `^:clj-reload/keep`:

https://github.com/gavr123456789/Niva/blob/622df25fba0701d9bba6456a0df68ca40befd0df/Niva/NivaInNiva/back/clojureBackend/cljEmit.niva#L820-L829

but a REPL module is loaded with `load-file`, where that doesn't apply.

What should a REPL module do with records the session already has?

1. **Define each record once per session.** In REPL mode, a record
   becomes `(when-not (resolve 'main_Square) (eval '(defrecord main_Square [side])))`,
   the REPL's counterpart of `^:clj-reload/keep`. The `eval` matters:
   with a plain `when-not`, bb finds `->main_Square` unbound even on the
   first load, and JVM Clojure still stops at `no matching type`. Tried
   by hand on bb and JVM Clojure: the line prints `3`, and a value made
   in the session still matches after loading the whole module again. A
   record whose fields change then needs a new session.
2. **A `--repl-line=N` module leaves out the records declared before
   line N,** as it already leaves out the entry lines before N; records
   its own lines declare stay. This fixes sending lines, but not loading
   the module again.

   https://github.com/gavr123456789/Niva/blob/622df25fba0701d9bba6456a0df68ca40befd0df/Niva/NivaInNiva/ir/fromTypedAst.niva#L1434-L1451

3. **No change:** whatever sends the lines drops the records the
   session already has.

I'd suggest 1: it's a few lines next to `^:clj-reload/keep`, and covers
both cases. Draft PR #466 shows what 1 looks like, with tests, so
there's something concrete to look at. You don't have to take it, and
if another option fits better, that's fine.

Niva `self-host` at `622df25f`, Babashka 1.13, Clojure CLI 1.12 on
OpenJDK 25, Linux. Found while building a REPL for a small computer
algebra library in Niva. Thanks for Niva!
