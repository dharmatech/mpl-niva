# 009 optional imports

**Status:** Parked (2026-10-26). Nothing is built. It waits until NIN,
Niva's self-hosted compiler, is the main implementation, with much more
Niva code to try it on.

An idea for Niva: imports that are optional per file and checked when
they're present, and a flag that rejects cycles between modules. It's
meant as a working alternative to show Gavr, not to argue him out of
his own design: "no imports until the full type name and fields clash",
and in the long run a Smalltalk-like image.

## The idea

- **Optional, per file.** A file without imports compiles as now, with
  the compiler finding everything. A file with imports says what it
  expects to use, and the compiler reports anything it uses from a
  module it didn't import.
- **Niva's own spelling.** The Kotlin compiler already takes
  `Project import: "a"` as an alias of `Project use: "a"`, which makes a
  package's names win. Optional, and it reads like an import.
- **A check, not a scope.** Lookup doesn't change. After resolving a
  file, the compiler knows whose types and methods it used, and compares
  that with its imports. Methods are the point: `main.niva` uses
  `simplify`, which `mpl/simplify.niva` adds to `mpl/tree.niva`'s
  `Expr`, and today nothing in the file says so. Programs without
  imports, or whose imports hold, compile exactly as before, so it's
  not a fork of the language.
- **A no-cycles flag** over the same graph of uses.

## What the compilers have

At `self-host` `781c5b05`:

- **The Kotlin compiler:** `Project use:` and `Project import:`, and
  qualified names such as `a.Person`. Its packages are flat: a file's
  name, whatever its folder.
- **NIN:** modules named by path, so `mpl/tree.niva` is `mpl.tree`, and
  two files that would get the same name are rejected. But no `Project`
  forms (`Project use: "tree"` fails with "Cant find type Project"), no
  qualified names, and an LSP backend that's a stub.

## Candidate increments

1. NIN accepts `Project import:` and `use:` as the Kotlin compiler
   does, with no checking: parity, and the piece that could go to Gavr
   alone.
2. A dependency report the resolver derives, behind a flag.
3. The no-cycles flag.
4. Declared imports, checked against the report.
5. A demo: a body of code with `Project import:` lines throughout.

## How it would be laid out

- The charter, spec and checkpoints in this folder, as in
  [008](../008-simplifier/README.md).
- A branch of Niva in its own clone, with only what Gavr would read:
  the code, NIN tests, an example in the idiom of his
  `examples/2_Module_system/`, and a short design note linking here.
  Based on one `self-host` commit and rebased once, at the end.
- The demo on a branch of each program it's shown on.

## When to pick it up

When NIN is the main implementation and there's a lot of code compiled
by it: Gavr's own, NIN's source once NIN compiles itself, MPL, and the
boards program once it's ported from the legacy compiler. The more code
the check runs on, the more it shows. Then: a charter, in a fresh
conversation, starting from this page.
