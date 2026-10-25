# 008 `sortedWith:` on lists, as on the JVM compiler: pull request

**Status:** Draft, not opened. A PR without an issue, as
[007](007-inject-into-pr.md) (#454) was: the JVM compiler already has
`sortedWith:`, so matching its signature leaves nothing for Gavr to
decide.
**Branch:** `sorted-with`, one commit on `self-host` at `2ad4f629`:
`b2ff55a2`. Locally in `~/src/Niva-self-host-sorted-with`, a worktree
of `~/src/Niva-self-host`. Not pushed; it goes to `dharmatech/Niva`
like #454's branch.
**Base:** `gavr123456789/Niva` `self-host`, not `main`

From the gaps at the end of the [README](README.md) list, picked as
next after #454: the simplifier keeps operands in a canonical order,
and sorting them with the order relation as a comparator is the
natural way to write that. NIN has only `sortedBy:`, which sorts by a
key.

The JVM compiler declares it in `CreateProtocols.kt` as a keyword
`sortedWith:` taking a block of two items, `a` and `b`, returning an
`Int`, and giving back the same collection type. This does the same,
next to `sortedBy:` in the list part of
`front/resolver/protocols/collection.niva`, with `List__sortedWith` in
each runtime:

- **Go:** a copy, sorted with `sort.SliceStable` on `cmp(a, b) < 0`, as
  `List__sortedBy` sorts on its keys.
- **Clojure:** `(vec (sort cmp s))`. `sort` takes a function returning
  a number as a comparator, and is stable.

**Tests,** as for #454: the type of `{3 1 2} sortedWith: […]` in the
resolver, and on each backend the generated call and that the runtime's
source defines the function.

## What was checked

On 2026-10-25, each from an exported copy, with stage 1 at `2ad4f629`
and a copy of its home:

- `niva test`: 394 pass on the branch, against 391 on stock
  `2ad4f629`, so the 3 new tests pass and nothing else changed. The one
  failing on both is `babashkaHostLoadsRecoversAndWatchesSources`, as
  for 005 to 007.
- The 3 new tests fail without the change: with only the test files
  added to stock, 391 pass and the 3 fail.
- The same program on Go, Clojure and the JVM compiler, sorting
  `{3 1 2}` up and down, words by length, an empty `List(Int)`, and
  three values of a type by one field with a tie: `1 2 3`, `3 2 1`,
  `fig pear apple`, `0`, and `y x x`, so all three are stable. Stock NIN
  rejects it: "Searching for sortedWith method but its not found".
- Closer to MPL: a `List(Expr)` of a union, sorted by a method that
  compares names with the new string `<` and returns -1, 0 or 1. The
  number comes first, then `a`, `m`, `z`, on both backends.
- `java -jar main.jar build --backend=go`: the branch's own sources
  build, and stock sources compiled with the branch's jar give the same
  Go as with the stock jar but for the `//line` paths and the new
  function in `core.go`. The Go-built compiler runs the program above
  the same way on both backends.
- mpl-niva's 31 checks pass on Go with the branch.

Not covered: `sortedWith:` on a set or a map. As on the JVM compiler,
it's declared for lists only, next to `sortedBy:`.

Everything below the line is the pull request as it would be opened.

---

**Title:** [go, clj] lists get sortedWith:, a comparator sort, as on the jvm compiler

The JVM compiler has `sortedWith:`, sorting with a block of two items
that returns a negative number if `a` comes first, 0 if they're equal,
and a positive one if `b` does. NIN had only `sortedBy:`:

```Scala
({3 1 2} sortedWith: [a, b -> a - b]) echo
```

```
Searching for sortedWith method but its not found for type  Type(List < Type(Int)>) at main.niva:1:1
```

This adds it with the same signature, next to `sortedBy:` in
`collection.niva`, and `List__sortedWith` in both runtimes. Like
`sortedBy:`, it's stable and returns a new list: on Go it's
`sort.SliceStable`, on Clojure `sort`, which takes a comparator
function. On both backends the program above prints 1 2 3, as on the
JVM compiler.

Three tests, each failing without the change: the type of a
`sortedWith:` in `resolverTests.niva`, and on each backend the call and
that the runtime's source defines it.

`niva test`: 394 pass, 3 of them new. The one that fails,
`babashkaHostLoadsRecoversAndWatchesSources`, fails on this machine on
`self-host` too. `niva build` and `java -jar main.jar build --backend=go`
work.
