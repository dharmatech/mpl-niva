# 001 self-hosted

**Status:** Done; landed at `5cbdef7` (2026-10-22).

Moves the project from the legacy Kotlin compiler to Niva's self-hosted
compiler, NivaInNiva, on the `self-host` branch. Ports
[000](../000-expression-tree/README.md) to it, replaces `niva test` with
checks that run on the new compiler, and adds scripts to build and run
it. Designed and built in one conversation.

## Why

The Kotlin compiler is what Niva's install guide sets up today, but
Gavr, Niva's author, is building the self-hosted compiler, and that's
where the language is going. Since this project is for exploring Niva
and giving him feedback, it targets the version he's working on. His
reply on [#430](https://github.com/gavr123456789/Niva/issues/430), the
first bug we filed against the Kotlin compiler, already pointed to the
self-hosted version.

We run into bugs either way. Here we work around them and report them
one at a time, with fixes where we can.

## Decisions

- **Go first.** The self-hosted compiler compiles to Go or to Clojure.
  Go ran everything we had. Clojure can't use a type from another file,
  so a program spread over files fails; we'd need one file. Clojure
  comes back when that's fixed.
- **Its own clone, pinned.** `tools/build-self-hosted-niva` builds the
  compiler in `~/src/Niva-self-host` at `6af8f9f6`, without touching
  `~/.niva` or an installed `niva`. `tools/nin` runs it.
  [The self-hosted compiler](../../../self-hosted-compiler.md) has the
  details.
- **Checks instead of `niva test`.** The self-hosted compiler has no test
  command. `check.niva` runs the checks in `tests/` and ends with
  `all N checks passed` only when they all pass. `tools/check` passes
  only on that line, since the exit code is 0 even when the build fails.
  The 10 tests from 000 became 31 checks.
- **`text`, not `toString`.** Printing is an ordinary method with one
  `match`, so it no longer has to be split across branches or live in
  the union's file. It's back in its own file, `mpl/notation.niva`.

## What the port changed

- `List::Expr` is now `List(Expr)`.
- Binary messages have no precedence, so `x + 2 num * y` is now
  `(x + 2) * y`. The demo shows both, and the checks pin it.
- `-1 num` works, so the `Num value: -1` workaround is gone.
- The list `same:` avoids `=>`, `any:` and an early `^`, all of which
  fail somewhere here.

[Niva notes](../../../niva-notes.md) has the full list. The legacy notes
moved to [their own page](../../../niva-notes-legacy.md).

## What we checked

- The demo and all 31 checks run on Go.
- `tools/check` fails on a failing check and on a type error.
- On Clojure, the checks fail at the first type used across files, as
  expected.
- Building the compiler twice gives the same jar, and `~/.niva` is
  unchanged.

Along the way we found about a dozen bugs and gaps in the self-hosted
compiler. They're in [Niva feedback](../../../niva-feedback/README.md),
in the order we'd file them.

## Next

The first simplification rules, as planned at the end of 000. Sorting
operands into canonical order needs a sort with a comparator, which the
runtimes don't have yet, so that series writes one.
