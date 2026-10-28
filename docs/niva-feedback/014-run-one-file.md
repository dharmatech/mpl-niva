# 014 Running one file builds its whole folder

**Status:** Drafted 2026-10-28, not filed: for the user's review first.
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `1109e9ee` (2026-10-28), first found
at `dccd5143` (2026-10-28)

It's item 45 in the [README](README.md). It was found setting up
worksheets of MPL expressions to send to the REPL a line at a time with
F8 from VS Code ([series 021](../design/implementation/021-repl-values/README.md)
has the REPL). A worksheet in a folder of MPL's project is compiled into
every build, so a half-written line in it stops the demo. The general
case is a folder of small, independent example programs, each run on its
own, from the command line or in a REPL session.

No existing issue covers it (`gh search issues` on 2026-10-28 for
"import", "module", "load everything", "repl", "Project use", "folder",
"single file", "run file", "one file", "examples", "scripts", "entry
file", "all files", "whole folder", "hidden", "privatePkg" and "same
name"). Related, all Gavr's:

- [#128](https://github.com/gavr123456789/Niva/issues/128), closed
  2023-12-16: the Kotlin compiler's commands, where `niva file.niva` is
  "single file compilation + run" and `niva run file` builds from the
  file's folder. NIN has only the second: `nin a.niva` says "a.niva arg
  is unknown".
- [#372](https://github.com/gavr123456789/Niva/issues/372), open: since
  Niva has no imports, a package's dependencies are hard to track; he
  proposes `niva showDeps`.
- [#222](https://github.com/gavr123456789/Niva/issues/222), open:
  "export only if import", a `Project privatePkg: true` package seen
  only where it's `Project use:`d.
- [#68](https://github.com/gavr123456789/Niva/issues/68), open: types
  with the same name in different packages.

**Framing.** One decision, with options, smallest first. Option 1 is
parity with the Kotlin compiler, as 000 to 005 were, so it's the
easiest to take. Option 2 is what the user would like, and is said so,
but isn't argued for: since 2026-10-26 the user builds alternatives
rather than lobbying for imports
([series 009](../design/implementation/009-optional-imports/README.md)).
NIN's other failures in a shared folder, where the Kotlin compiler copes
(item 46), are mentioned in a sentence and left for an issue of their
own.

## What was checked

Each case is a folder of its own, made by a script in the scratchpad;
`a.niva` is the issue's. NIN ran with `run FILE --backend=go`, at
`dccd5143` and at `1109e9ee` (the only commit since, a style
refactor), with the same results. The Kotlin compiler is the branch's
own, stage 1 at `1109e9ee`, run as `niva FILE` and `niva run FILE`.

| Folder | Run | NIN `run` | Kotlin `niva FILE` | Kotlin `niva run FILE` |
|---|---|---|---|---|
| `a.niva` alone | `a.niva` | `3` | `3` | `3` |
| `b.niva` beside, its own `type Point x: Int` | `a.niva` | "CloseParen expected but got IdentifierColon(x) at: 2:9" | `3` | `3` |
| `c.niva` and `d.niva` beside, each with its own `Int double` | `a.niva` | "Type Type(Int) already has method (NivaMethod double() -> Type(Int))" | `3` | `3` |
| `h.niva` beside, unfinished: `x = (1 +` | `a.niva` | "step: EOF", no file named | `3` | "Primary was expected but received '+ EOF'.(h.niva:1)" |
| the same `h.niva` in `.examples/` | `a.niva` | `3` | `3` | `3` |
| `lib.niva` with `Int double`, `main.niva`, `examples/e.niva` | `main.niva` | `6` | can't find `double` | `6` |
| the same | `examples/e.niva` | can't find `double` | can't find `double` | can't find `double` |

And `nin a.niva`, with no command, gives "a.niva arg is unknown".

Go, which NIN compiles to, has the same split: with `a.go`, a `b.go`
with its own `Point` and `main`, and an unfinished `broken.go` in one
module, `go run a.go` prints `3` and `go run .` stops at `broken.go`
(Go 1.26.6).

Everything below the line is the issue as it would be filed.

---

**Title:** Running one file builds its whole folder (NIN has no `niva file.niva` from #128)

I'd like a folder of small example programs that I can run one at a
time, from the command line or in a REPL session. In NIN, running one
compiles every `.niva` file under its folder, so the examples break
each other. In an empty folder, with `a.niva`:

```Scala
type Point x: Int y: Int
p = Point x: 1 y: 2
(p x + p y) echo
```

`nin run a.niva --backend=go` prints `3`. Then add any of these beside
it, none of which `a.niva` uses:

| Beside `a.niva` | `nin run a.niva --backend=go` |
|---|---|
| nothing | `3` |
| `b.niva`: `type Point x: Int` and `(Point x: 5) x echo` | `CloseParen expected but got IdentifierColon(x) at: 2:9` |
| `c.niva` and `d.niva`, each with its own `Int double -> Int = …` | `Type  Type(Int) already has method (NivaMethod  double() ->  Type(Int))` |
| `h.niva`, unfinished: `x = (1 +` | `step: EOF` |
| the same `h.niva`, in a folder `.examples/` | `3` |

Only a folder whose name starts with `.` is left out, which also hides
it from `ls`.

The other way round, an example can't use the project it's in. With
`lib.niva` declaring `Int double -> Int = this * 2`, `main.niva` saying
`3 double echo` and `examples/e.niva` saying `4 double echo`,
`nin run main.niva` prints `6`, compiling `examples/e.niva` along with
it, but `nin run examples/e.niva` stops at `Searching for double method
but its not found`, since a build sees only what's under its file's
folder.

REPL mode is built the same way: `nin build --repl` makes one module of
the whole folder, so a session can't start from one example, or have a
library and then load an example into it.

The Kotlin compiler has the single-file case (#128): `niva a.niva`
compiles `a.niva` alone, and prints `3` beside each of the files above.
NIN has only `niva run file`; `nin a.niva` says `a.niva arg is unknown`.
Go has the same split: `go run .` builds the folder, but `go run a.go`
builds only the files named, so a folder can hold several programs that
each declare `main`.

How would you like running one file to work?

1. **`nin FILE` builds that file alone,** as `niva file.niva` does in
   the Kotlin compiler (#128), and `go run a.go` in Go. A one-file
   example then runs whatever is beside it. It doesn't cover an example
   that uses another file, such as the library it demonstrates.
2. **A file names what it uses, and running it builds the file and what
   it names,** with `Project use:` say, which the Kotlin compiler
   already parses, and close to #222. That also covers an example using a library, in the
   library's project or beside it, and REPL mode built the same way
   could start from one file and load another.
3. **No change:** examples go in a folder starting with `.`, or outside
   the project, where they can't use it.

I'd suggest 1 first: it's small, and the Kotlin compiler and Go both
have it. 2 is where I'd like this to go, since it's what lets an
example use the library it sits beside, but it's the bigger decision,
and yours.

In NIN's folder mode, the `Point` and `double` cases above also fail
where the Kotlin compiler's `niva run a.niva` prints `3`, and the
unfinished file isn't named. That's for another issue, if you'd like
one.

Niva `self-host` at `1109e9ee`, Go 1.26, Linux. Found while writing
worksheets for a small computer algebra library in Niva. Thanks for
Niva!
