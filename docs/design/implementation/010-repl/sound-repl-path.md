# From option 1 to a sound REPL

Written 2026-10-27, after [what a sound REPL needs](sound-repl.md).
That page says what a correct REPL decides. This one is how we'd get
there from option 1 of item 38, our draft PR
[#466](https://github.com/gavr123456789/Niva/pull/466), and what Niva
already has for it. Links into Niva are at `self-host` `622df25f`.

## Option 1 doesn't lock anything in

It changes only the Clojure that REPL mode emits: a guard around each
`defrecord`. Nothing else depends on it, and it can be taken out or
extended. With a compiler that tracks the session, the guard becomes
redundant, since records the session has are never sent again, so
there's nothing to undo. Option 3, our own tool dropping the records,
would have been the compromise: the decision outside the compiler,
where Niva's types don't reach.

## Hot reload already has the other half

Option 1 is the REPL's `^:clj-reload/keep`. Hot reload pairs that with
a check. `nin watch` keeps a compiler session with a manifest of every
record's, union's and enum's shape, and when one changes, it refuses
the reload:

```
hard restart required: type shape changed: main/main_Person; automatic state migration is not supported
```

https://github.com/gavr123456789/Niva/blob/622df25fba0701d9bba6456a0df68ca40befd0df/Niva/NivaInNiva/compiler/incremental.niva#L583-L587

tested by `incompatibleReloadShapeRequiresHardRestart` in
`compiler/compilerTests.niva`. That's decision 3's first answer,
"start a new session". Option 1's gap, a record whose fields change,
closes the same way: compare the line module's manifest with the
session's, and stop with that message. A small PR, reusing hot
reload's.

## What the compiler session has, and what it lacks

`CompilerSession`, in `compiler/incremental.niva`, came with #423 and
is what `nin watch` runs on. It has:

- a fingerprint of each package's interface, a hash of its types'
  fields and its functions' signatures (`IrPackage apiHash`);
- on a change, a new type-check of only the packages whose interface
  changed and those that depend on them;
- the manifest above;
- the same interface strings for each declaration,
  `IrTypeDef canonicalInterfaceString` and
  `IrFunction canonicalInterfaceString`, which say whether a type's
  fields or a function's signature changed.

What it lacks for a REPL is granularity: it tracks packages, which are
files, and a REPL session is one namespace that grows by lines. Nor
does it send less: hot reload writes every file again, and clj-reload
reloads the namespaces whose files changed. A session-aware REPL mode
would make each line a new revision of the snapshot, compare each
declaration with what the session has, and emit only the new or changed
ones, and the line. A function's fingerprint covers its signature, not
its body, so a changed body needs a comparison of its own, of the
emitted code say.

## What it would buy MPL

Less than it sounds:

- **Soundness:** most of it comes from the manifest check alone.
- **Speed:** MPL's whole module loads and runs the demo in under a
  tenth of a second on `bb` ([runtimes](runtimes.md)), so sending its
  functions again costs little.
- **Earlier lines:** a line module never runs them again anyway.

A value made before a redefinition keeps what it was: `x = f 1`, then a
new `f`, leaves `x` alone. While `f`'s type is the same, that's how
Clojure, OCaml and GHCi behave too. A new type is decision 2's case.

So sending only what changed is for when real use shows a need: a big
session, slow loads, or redefinitions that confuse. It also changes
Gavr's compiler at the design level, so it's his call, as an issue
first.

## The steps

1. **#466**, once Gavr has settled
   [#465](https://github.com/gavr123456789/Niva/issues/465).
2. **Our REPL loop on `bb`,** the tool around `--repl-line`, and using
   it on MPL.
3. **The manifest check in REPL mode,** a small PR reusing hot
   reload's.
4. **A session-aware REPL mode,** if step 2 shows it's needed.

Each stands on its own, and none is undone by the next.
