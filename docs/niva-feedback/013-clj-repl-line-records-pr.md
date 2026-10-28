# 013 Clojure REPL mode: a line's records break the session, option 1 as a draft pull request

**Status:** Opened 2026-10-26 as draft PR
[#466](https://github.com/gavr123456789/Niva/pull/466), right after the
issue, linking it with "For #465", as 005 and 006 were. No CI checks.
Each paragraph went in on one line. `self-host` was still at
`622df25f`. On 2026-10-27 (16:44 UTC) Gavr left one inline comment,
labeled the PR `codegen` and took it out of draft. The fix was pushed
with a reply at 22:25 UTC. See [Review](#review). He merged it into
`self-host` on 2026-10-27 (2026-10-28 01:11 UTC), squashed as
`dccd5143`, unchanged from `381a8765` and retitled "[clj repl] define
each record once per session (#465) (#466)". His thread was left
unresolved. "For #465" didn't close #465, so we closed it on
2026-10-28.
**Issue:** [#465](https://github.com/gavr123456789/Niva/issues/465) ([draft](013-clj-repl-line-records.md))
**Branch:** [`dharmatech/Niva:clj-repl-keep-records`](https://github.com/gavr123456789/Niva/compare/self-host...dharmatech:Niva:clj-repl-keep-records),
one commit on `self-host` at `622df25f`: `f52ae1f5`. For his review:
`381a8765`, pushed as a fast-forward. Locally in
`~/src/Niva-self-host-clj-repl-records`, a worktree of
`~/src/Niva-self-host`. Its `Niva/NivaInNiva/main.jar` is built from it
(ignored by git), so
`NIVA_SELF_HOST=~/src/Niva-self-host-clj-repl-records` works with
`tools/nin`. It's now built from `381a8765`.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

The issue's option 1, in Gavr's style:

- **`back/clojureBackend/cljBack.niva`:** `CljContext` gets `repl`,
  next to `hotReload`, and a constructor that takes it. The one without
  it passes `false`, so its callers are unchanged.
- **`back/clojureBackend/cljEmit.niva`:** `emitTypeDef:` and
  `emitUnionDef:` each had the same `ifTrue:ifFalse:` for hot reload.
  Both now call `CljContext recordDef:fields:`, a `| true` match:
  `^:clj-reload/keep` for hot reload, the guarded `eval` for REPL mode,
  and the plain `defrecord` otherwise. `fullFromIrModule:…` sets `repl`
  from the mode, as it does `hotReload`.

Enums are keywords, `(def main_Color_Red :main_Color_Red)`, so defining
them again is harmless and they're left alone.

**Why the `eval`,** found while writing it, and what the code comment
says: on JVM Clojure and `bb`, compiling a `defrecord` makes a new class
even in a branch that isn't run. After `(defrecord Foo [a])` and a value
made with it, `(when-not (resolve 'Foo) (defrecord Foo [a]))` leaves
`Foo` naming a new class, and the value is no longer `instance?` of it.
With `eval` the class stays the same. `lg` keeps the class either way.
That's why the issue's plain `when-not` row still says "no matching
type" on JVM.

**Order of filing,** as for 005 and 006:

1. The issue was filed saying "I'm opening a draft PR for 1 right after
   this issue".
2. The branch was pushed to `dharmatech/Niva`, and the PR opened as a
   draft with the issue's number in its title and body.
3. The issue's sentence was changed to "Draft PR #466 shows what 1
   looks like" with `gh api -X PATCH`.

## Review

**Gavr's comment** (2026-10-27,
[comment](https://github.com/gavr123456789/Niva/pull/466#discussion_r4391503464)),
on `cljBack.niva` line 52, the new constructor: "same names for local
and key-name (and the rest of the file)", `repl(repl): Bool` ->
`repl: Bool`. In a declaration, `key: Type` names the local after the
key, so `key(key)` isn't needed.

**The fix,** `381a8765` [clj] key: Type instead of key(key): Type in
cljBack.niva: all five `CljContext newDebug:` constructors, and
`:imports :pkg :hotReload` in the one call that still spelled them out.
`newDebug(debug)` keeps its local, since the names differ. Only
`cljBack.niva`, as he asked: `cljEmit.niva` has twelve more in code the
PR doesn't touch, left to him, and the PR's own new code had none.

Checked as before: `niva test` 412 of 413 (the same one failing), the Go
self-build, and the same modules as `f52ae1f5` for the issue's program
(`3` twice on `bb`) and for MPL's REPL module, `--repl-line=64` and
program.

**The reply** (22:25 UTC,
[comment](https://github.com/gavr123456789/Niva/pull/466#discussion_r4394876385)),
in his thread:

> Done in 381a8765: `key: Type` wherever the local had the key's name,
> in all of `CljContext`'s constructors, and `:imports :pkg :hotReload`
> in the one call that still spelled them out. Tests and the Go build
> are as before.

## What was checked

On 2026-10-26, from exported copies, with the pin's stage 1 and a copy
of its home:

- `niva test`: 412 of 413 pass on the branch. The one that fails is
  `babashkaHostLoadsRecoversAndWatchesSources`, as for 005 to 012.
- The tests fail without the change: with only the test file added to
  stock, 410 pass and 3 fail, the 2 new ones and
  `babashkaHostLoadsRecoversAndWatchesSources`.
- **The issue's program,** the full module then `--repl-line=8`, in one
  session: `load-file` on `bb`, `clojure -M -i full.clj -i line.clj` on
  JVM Clojure, and the two files run as one on `lg`. Stock stops at "no
  matching type" on all three; the branch prints `3` twice on all
  three.
- **Program mode and hot reload:** MPL's `main.niva` and `check.niva`
  built with `--backend=clj` (13 files), and MPL's hot reload files
  (`nin watch main.niva --runtime=bb`, stopped after 40 s), are the
  same byte for byte as stock's.
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and stock sources compiled with the branch's jar give the same
  Go as with the stock jar, but for the `//line` paths. The Go-built
  compiler gives the same two modules as the jar for the issue's
  program.
- **MPL with the branch,** `NIVA_SELF_HOST=~/src/Niva-self-host-clj-repl-records`:
  - The REPL module (564 lines) and `--repl-line=64` (532) are stock's
    but for their 10 records, each now guarded.
  - The full module, then line 64, in one session: the demo's 34
    lines, then line 64's five `showSimplified` lines as Go prints
    them, on `bb`, JVM Clojure and `lg`. Stock stops at "no matching
    type" on all three.
  - A value made in the session, `(def e (mpl__tree_Expr__+ x x))`,
    then the full module loaded again, then `e` shown:
    `x + x   =>   2 * x` on all three. Stock stops at "no matching
    type" on all three, `lg` too, which the issue draft hadn't tried.
  - The module loads as fast as stock's, three runs each: 0.06 s on
    `bb`, 0.05 s on `lg`, about 1.4 s on JVM Clojure. On JVM, the
    module's two class hints, `^niva.repl.session.tests_checking_Check`,
    resolve to the kept class when the module is loaded again.
  - `tools/check` passes all 151 checks on Go, on `bb`
    (`--backend=clj --runtime=bb`) and on JVM Clojure
    (`--runtime=jvm`), and the import comments hold. `tools/check-repl`
    (with `LG=~/src/let-go/build/lg`) prints the demo as Go does on all
    three.

On JVM, `clojure -M -e '(load-file …)'` also prints each form's value,
so a session run that way shows `3` four times; `-i` doesn't.

Not covered: a record whose fields change between loads keeps its old
fields until a new session, as the issue says.

Everything below the line is the pull request as opened.

---

**Title:** [clj] repl mode: define each record once per session (#465)

Option 1 from #465, as a draft, to show what it looks like. You don't
have to take it, and if another option fits better, that's fine.
For #465.

In REPL mode, a record is now defined only if the session doesn't have
it yet:

```clojure
(when-not (resolve 'main_Square) (eval '(defrecord main_Square [side])))
```

It was `(defrecord main_Square [side])`, which makes a new class each
time a module is loaded, so after the issue's line module the session's
`shape` matched no branch of `size`. Now the full module and then line
8, in one session, print `3` twice on bb and JVM Clojure, and a value
made in the session still matches after the whole module is loaded
again.

The `eval` is needed: on JVM Clojure and bb, compiling a `defrecord`
makes its class even in a branch that isn't run, so with a plain
`when-not` the class was replaced all the same.

**Where to look:** `CljContext recordDef:fields:` in
`back/clojureBackend/cljEmit.niva`, which `emitTypeDef:` and
`emitUnionDef:` now share, with hot reload's `^:clj-reload/keep` in the
same match; and `repl` in `CljContext`, set from the mode in
`fullFromIrModule:…`, as `hotReload` is. Programs and hot reload come
out as before. Enums are keywords, so they're left alone. A record
whose fields change keeps its old ones until a new session.

Two tests, each failing without the change, after the `cljRepl…` tests:
in `cljBackTests.niva`, the guarded records of a REPL module, with none
in a program's or hot reload's; and the issue's program on bb, the full
module and then a `toIrModuleFromEntryLine: 8` module in one process,
printing `3` twice.

`niva test`: 412 of 413 pass, 2 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work, and the compiler's own Go is unchanged.
