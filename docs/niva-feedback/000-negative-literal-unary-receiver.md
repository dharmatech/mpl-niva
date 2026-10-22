# 000 negative literal as a unary receiver

**Status:** Filed 2026-10-22 as [#430](https://github.com/gavr123456789/Niva/issues/430).
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `main` at `ece36eb7` (2026-10-20)

Found while printing negative numbers in
[000-expression-tree](../design/implementation/000-expression-tree/README.md):
`-1 num` failed to compile. A smaller repro with `Int inc` compiles and
gives the wrong answer, so that's the one the issue leads with.

Everything below the line is the issue as filed.

---

**Title:** `-1 inc` compiles to `-1.inc()`, which Kotlin reads as `-(1.inc())`

A unary message sent to a negative number literal gives the wrong result:

```Scala
Double plusOne -> Double = this + 1.0

a = -1 inc
a echo // -2, expected 0

b = -1.5 plusOne
b echo // -2.5, expected -0.5
```

The generated Kotlin is

```kotlin
val a = -1.inc()
```

In Kotlin, `-1` there isn't a literal. A member call binds tighter than
prefix minus, so this is `-(1.inc())`, and the minus is applied last.

When the result isn't a number, it fails to compile instead:

```Scala
-1 inc echo
```

```
e: Main.kt:277:1 Unresolved reference 'unaryMinus' for operator '-'.
```

Keyword messages are fine, because their receiver goes in parentheses:
`-2 add: 3` becomes `(-2).add(3)`. In Niva, `(-1) inc` also works, and
so does a variable (`x = -1`, then `x inc`).

`generateSingleUnary` appends the receiver as is:

https://github.com/gavr123456789/Niva/blob/ece36eb71661255f747267febda5ad2dcb5940a6/Niva/Niva/src/main/kotlin/codogen/expressions/MessageSend.kt#L611

while `generateSingleKeyword` wraps it in brackets:

https://github.com/gavr123456789/Niva/blob/ece36eb71661255f747267febda5ad2dcb5940a6/Niva/Niva/src/main/kotlin/codogen/expressions/MessageSend.kt#L337-L340

Related: #45 (Lexer negative numbers).

Niva `main` at `ece36eb7`, OpenJDK 25, Linux. Found while writing a
small computer algebra library in Niva. Thanks for Niva!
