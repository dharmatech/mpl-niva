# 002 Go: a `^` inside a value doesn't return from the method

**Status:** Drafted, not filed. #433 was merged on 2026-10-22, so this
is next, after re-checking it on the new `self-host`.
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `ad439a32` (2026-10-22), first found
at `6af8f9f6` (2026-10-19)

Found while writing [#433](https://github.com/gavr123456789/Niva/pull/433)
([draft](001-clojure-early-return-pr.md)): its first commit makes a `^`
inside a value a compile error on Clojure, and on Go the same code
compiles but returns from the wrong place. #433 says so in passing ("One
difference from Go"); [#432](https://github.com/gavr123456789/Niva/issues/432)
doesn't. No existing issue covered it on 2026-10-22.
[#345](https://github.com/gavr123456789/Niva/issues/345) is the lambda
case, which the type checker already reports.

What Gavr said on Discord, which shaped how this is pitched:

- On #432: "for now thats intended, there is no return in Clojure, and I
  dont know how to implement it". Earlier (2026-10-19) he planned to
  "forbid returns in clj back", and said "self hosting will always use
  go".
- He prefers switches to early returns: his 6af8f9f6 rewrote guards like
  `mergeFailed ifTrue: [^ null]` into
  `^ | true | mergeFailed => null |=> merged`. "its much cleaner anyway".
- Turning the IR into a state machine, as someone suggested, is "too
  hardcore". He answered #433's comment on moving the rest of a block
  into the branches with "hmm".

So the issue leads with A, which keeps `^` working on Go, where his
compiler runs and uses it 34 times. B is offered as the alternative,
and the issue says his switch style isn't affected by either.

Both are spiked, unpushed, in `~/src/Niva-self-host-go-value`, a
worktree of the self-host clone, rebased on `ad439a32`. The versions on
`6af8f9f6` are kept as `go-value-return-on-6af8f9f6` and
`go-value-return-forbid-on-6af8f9f6`.

- **A**, `go-value-return` ([PR draft](002-go-return-inside-value-pr.md)),
  three commits that can be taken as a prefix: `6554dc9b` fixes a
  separate bug in the statement form of a match on errors (item 14 in
  the [README](README.md)), `86dc10f2` emits the value as statements,
  `7da54ee7` fixes the match typing. 304 lines of code and 168 of
  tests. The 354 existing tests and 6 new ones pass; the new ones fail
  without it, and each prefix passes on its own. It edits 6 lines of
  Gavr's 2026-10-19 `emitMatchErrorStatement`, and wraps the assignment
  code of his 2026-10-21 "fix go mut value semantics" in a check.
- **B**, `go-value-return-forbid`
  ([PR draft](002-go-return-inside-value-pr-forbid.md)), one commit,
  `6e98c9ee`: 99 lines of code and 55 of tests, none of it in code from
  2026-10-19. The 354 existing tests and 2 new ones pass. It rejects 34
  `^` in the compiler's own sources, 19 of them from Gavr's "adapt
  compiler for go bootstrap" (2026-10-11).

Both pass mpl-niva's checks unchanged. Compiling the compiler's own
sources, A changes the Go of 17 of its 879 functions, and that Go still
builds. A's typing commit can't go with B alone: without A's statements,
a tail match like `| Num => ^ this |=> Sym name: "x"` would get the type
`Sym` and stop building on Go.

Everything below the line is the issue as it would be filed.

---

**Title:** [go] a `^` inside a value doesn't return from the method

On `--backend=go`, a `^` in a branch of `ifTrue:ifFalse:` or a `match`
whose value is used doesn't return from the method. The value returned
becomes the branch's value, and the method carries on.

```Scala
Int inValue -> String = [
  s = this > 0 ifTrue: [^ "returned"] ifFalse: ["kept"]
  s + "!"
]
Int inMatchValue -> String = [
  s = | this
      | 0 => ^ "zero"
      |=> "other"
  s + "!"
]
5 inValue echo
-5 inValue echo
0 inMatchValue echo
```

| | Go | expected |
|---|---|---|
| `5 inValue` | returned! | returned |
| `-5 inValue` | kept! | kept! |
| `0 inMatchValue` | zero! | zero |

The JVM compiler on `self-host` (`Niva/Niva`) gives `zero` for
`0 inMatchValue`, and rejects `inValue` with `Generic unification
failed, generic type T was already resolved to Unit but now its String
in return type`.

A match used as a value becomes an anonymous function, and the `^`
returns from that:

https://github.com/gavr123456789/Niva/blob/ad439a32f1ada63aaaea17931831a01b461965d8/Niva/NivaInNiva/back/goBackend/goBack.niva#L1568-L1594

```go
var s string = func() string {
    __matchSubject := (this > 0)
    if (__matchSubject == true) {
        return "returned"
    } else if (__matchSubject == false) {
        return "kept"
    } else {
        panic("unmatched value")
    }
}()
```

6af8f9f6 inlined a match used as a statement for this reason:

https://github.com/gavr123456789/Niva/blob/ad439a32f1ada63aaaea17931831a01b461965d8/Niva/NivaInNiva/back/goBackend/goBack.niva#L549-L550

so what's left is a match whose value is used. The same happens with
`x <- …`, in an argument (`((this > 0 ifTrue: [^ "returned"] ifFalse:
["kept"]) + "!") echo` prints `returned!`), in an operand
(`^ (this > 0 ifTrue: [^ 1] ifFalse: [2]) + 10` gives 11), in a nested
value, and in a value inside an inlined `forEach:`. When the value
returned has another type than the branch's, the Go build fails
instead: `s = this > 0 ifTrue: [^ 1] ifFalse: ["kept"]` in an `Int`
method gives `cannot use 1 (untyped int constant) as string value in
return statement`.

A `^` in front of a switch, like `^ | true | mergeFailed => null |=>
merged`, isn't affected: there the branches are plain values and the
whole match is returned. That works today and stays as it is.

Related, in the type checker: a match branch that ends with `^` gives
the match the type of the value returned. So an early exit on an error,

```Scala
v = | this parse
    | Bad => ^ "bad input"
    |=> it
```

is rejected with `Match branches return incompatible types:
Type(String), Type(Int)`, and so is the statement
`| this parse | Bad => ^ "bad input" |=> []`, with String and Unit.

The compiler uses `^` inside a value itself, 34 times, in `lex.niva`
(the `tok = | this ch` of `nextToken`), `messageDeclParse.niva`,
`resolveExpr2.niva`, `typeDB.niva` and `astToType.niva`. In the two I
read, `nextToken` and `messageDeclarationOrExpression`, both meanings
give the same result.

I'd suggest making it work on Go, and I have a branch for it:

- Where the value is bound (`x = …`, `x <- …`) or returned, emit the
  match as statements, as 6af8f9f6 does for a match used as a
  statement, each branch ending by assigning or returning its value:

  ```go
  var s string
  if (this > 0) {
      return "returned"
  } else {
      s = "kept"
  }
  ```

  Nothing else about control flow changes.
- A branch that ends with `^` stops deciding the match's type, so the
  early exit on an error above works.
- A `^` in a value anywhere else, like an argument or an operand, can't
  be emitted this way without changing the order things are evaluated
  in, so it becomes a compile error with its position.

All 34 in the compiler are bound or returned, so they compile as
before, and on Go they then do what they do on the JVM.

If you'd rather a `^` inside a value be an error on Go too, as you plan
for Clojure, I have that as a branch as well: the type checker reports
it for every backend. But then the 34 in the compiler would have to be
rewritten first.

The first branch also starts with a fix for a separate bug in the
statement form of a match on errors from 6af8f9f6, which it relies on:
the else branch also runs after an error branch, with the zero value as
`it`, and an `it` that a branch doesn't use stops the Go build. I'll
report that one separately.

Which would you prefer? I'm happy to open a PR with either, or to leave
it to you.

Related: #432 and #433 (Clojure; #433's first commit reports a `^`
inside a value), #345 (a `^` in a lambda on Go).

Niva `self-host` at `ad439a32`, Go 1.26, OpenJDK 25, Linux. Found while
writing a small computer algebra library in Niva. Thanks for Niva!
