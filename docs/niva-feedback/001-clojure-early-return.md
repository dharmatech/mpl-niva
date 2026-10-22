# 001 Clojure: an early `^` is silently dropped

**Status:** Filed 2026-10-22 as [#432](https://github.com/gavr123456789/Niva/issues/432).
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `6af8f9f6` (2026-10-19)

Found while porting to the self-hosted compiler in
[series 001](../design/implementation/001-self-hosted/README.md): an
early `^` inside `ifTrue:` gave wrong answers on Clojure, so the list
`same:` was written without one. No existing issue covered it on
2026-10-22.
[#345](https://github.com/gavr123456789/Niva/issues/345) is related: a
non-local return from a block on Go.

The fix was opened right after as an optional PR,
[#433](https://github.com/gavr123456789/Niva/pull/433)
([draft](001-clojure-early-return-pr.md)): C, then A on top, as two
commits Gavr can take together, take C alone, or close.

Everything below the line is the issue as filed.

---

**Title:** [clj] a `^` before the end of a method is silently dropped

On `--backend=clj`, a `^` inside `ifTrue:`, `ifFalse:`,
`ifTrue:ifFalse:` or a `match` branch doesn't return from the method.
The method carries on and returns whatever comes next. On Go it
returns.

```Scala
Int viaIfTrue -> String = [
  this > 0 ifTrue: [^ "positive"]
  "not positive"
]
Int viaIfFalse -> String = [
  this > 0 ifFalse: [^ "not positive"]
  "positive"
]
Int viaMatch -> String = [
  | this
  | 0 => ^ "zero"
  |=> ^ "not zero"
  "unreachable"
]
Int viaIfTrueIfFalse -> String = [
  this > 0 ifTrue: [^ "positive"] ifFalse: [^ "not positive"]
  "unreachable"
]
5 viaIfTrue echo
-5 viaIfFalse echo
0 viaMatch echo
5 viaIfTrueIfFalse echo
```

| | Go | Clojure |
|---|---|---|
| `5 viaIfTrue` | positive | not positive |
| `-5 viaIfFalse` | not positive | positive |
| `0 viaMatch` | zero | unreachable |
| `5 viaIfTrueIfFalse` | positive | unreachable |

The backend documents this:

https://github.com/gavr123456789/Niva/blob/6af8f9f6726544063315cfa4e43c20d96ffd7431/Niva/NivaInNiva/back/clojureBackend/cljEmit.niva#L7

and `toCljBlockBodyWithBind` stops with `TO DO: "Clojure does not
support ^"` when a `^` is followed by more statements in the same
block:

https://github.com/gavr123456789/Niva/blob/6af8f9f6726544063315cfa4e43c20d96ffd7431/Niva/NivaInNiva/back/clojureBackend/cljEmit.niva#L544-L565

But a branch is a block of its own, and there the `^` is the last
statement, so it becomes the branch's value. The `if` is a statement,
so its value is thrown away:

```clojure
(defn main__core_Int__viaIfTrue [this]
  (let [_ (let [__cljSubject (> this 0)]
    (cond
      (= __cljSubject true) "positive"
      :else :unit))] "not positive"))
```

So the limitation is known, but it isn't enforced for a `^` in a
branch, and the program compiles and gives the wrong answer. The same
happens in a loop:

```Scala
List(Int) hasThree -> Bool = [
  this forEach: [it == 3 ifTrue: [^ true]]
  false
]
{1 2 3} hasThree echo // Go: true, Clojure: false
```

Some options:

- **A.** Move the rest of the block into the branches that don't
  return, so every `^` is in tail position. `viaIfTrue` becomes
  `(cond (= s true) "positive" :else "not positive")`. Plain Clojure,
  no run-time cost, covers the four methods above. It doesn't cover
  loops: a `^` inside a `while` can't be moved out this way.
- **B.** Return by exception: `^ v` throws a signal carrying `v`, and a
  method with an early `^` catches its own. That covers loops too, but
  Niva errors are already exceptions here, and `niva.core/handling`
  catches `Throwable` and treats anything without `:value` as the
  error itself, so a catch-all `Error` branch would swallow a return
  from its subject. Every catch would have to re-throw the signal
  first, and `ex-info` fills in a stack trace on every return.
- **C.** Any `^` the backend can't place becomes a compile error with
  its position, instead of being dropped. On its own as a first step,
  or with A for what A doesn't cover (loops).

I'm opening a PR right after this with C, then A on top, as two
commits. It's optional: take both, take only C, or close it if you'd
rather do this another way.

Related: #345 (non-local return from a block on Go).

Niva `self-host` at `6af8f9f6`, OpenJDK 25, Clojure CLI 1.12, Linux.
Found while writing a small computer algebra library in Niva. Thanks
for Niva!
