# 006 `String` has no ordering

**Status:** Draft, not filed. Option 1 is drafted as a pull request in
[006's PR](006-string-ordering-pr.md), to be opened right after the
issue, as for 005.
**Repository:** [gavr123456789/Niva](https://github.com/gavr123456789/Niva/issues)
**Checked against:** `self-host` at `4e809716` (2026-10-25), first found
at `a56f659c` (2026-10-25)

It's item 26 in the [README](README.md), found re-checking at
`a56f659c` and picked as next after #450: the simplifier keeps the
operands of a sum or product in a canonical order, and Cohen's order
relation compares symbols by name. No existing issue covered it on
2026-10-25 (`gh search issues` for "string comparison", "compare
strings", "compareTo", "String <", "lexicographic", "string ordering",
"sort strings", "sortedWith" and "Comparable").

The JVM compiler has no ordering on strings either, so unlike 000 to
005 this is something new, not NIN behind the JVM compiler: what it
should look like is Gavr's call. Series 000 found the same on the
legacy compiler, where Kotlin's `compareTo` can be bound with `@emit`.

Checked with small programs on NIN's Go and Clojure backends, on the
JVM compiler (stage 1, at `4e809716`), and on NIN with option 1 (the
branch in [006's PR](006-string-ordering-pr.md)):

| Program | NIN on Go | NIN on Clojure | JVM compiler | Option 1, Go and Clojure |
|---|---|---|---|---|
| `("x" < "y") echo`, and the same with `>`, `<=`, `>=` | type error: "Searching for < method but its not found for type Type(String)" | the same | "Can't find binary message < for type String" | `true`, `false`, `true`, `true` |
| `('a' < 'b') echo` | type error, for `Char` | the same | "Can't find binary message < for type Char" | not in option 1 |
| `("a" compareTo: "b") echo` | not found | not found | not found | not in option 1 |

With option 1, 13 comparisons give the same on Go and Clojure: the four
operators, a prefix (`"a" < "ab"`), case (`"B" < "a"`), the empty
string, equal strings, two variables, a method on a type comparing its
`name` fields, and `"é" > "z"`.

**Why.** `String`'s protocol has `+`, `==` and `!=` as binary messages,
and no ordering. The IR already makes every comparison operator native,
whatever the types, so once the type checker accepts `"x" < "y"`, Go
gets its own `<`, which orders strings. Clojure's `<` takes only
numbers, so there it needs `compare`, the way `+` on strings already
becomes `str`.

**The orders.** Go compares the UTF-8 bytes, and Clojure's `compare`
is Java's `compareTo`, on UTF-16 units. Both give code point order,
except when a character above U+FFFF is compared with one from U+E000
to U+FFFF: in UTF-16 the first is a surrogate, below U+E000. The issue
says so in a sentence, since it's the one way the backends could
disagree.

**Workarounds today,** in [Niva notes](../niva-notes.md): compare
`Char code`s character by character, on both backends, or bind Go's
`strings.Compare`, on Go only.

The options are what the language should have. 1 is the smallest that
works, and the draft PR. 2 adds a three-way compare, which a comparator
(a `sortedWith:`) would want, but MPL doesn't need yet. 3 is no change,
there for completeness: it's what the workarounds are. `Char` has no
ordering either; the issue mentions it but leaves it out, to keep to
one decision.

Everything below the line is the issue as it would be filed.

---

**Title:** `String` has no `<` or `>`, so strings can't be ordered

Neither compiler can compare two strings by order:

```Scala
("x" < "y") echo
```

NIN, on both backends:

```
Searching for < method but its not found for type  Type(String) at main.niva:1:2
```

The JVM compiler:

```
Can't find binary message < for type String
```

The same goes for `>`, `<=` and `>=`, and for `Char`. `String` has `+`,
`==` and `!=` as binary messages:

https://github.com/gavr123456789/Niva/blob/4e80971675d6a66b46d4890ce2390df4daea7cd9/Niva/NivaInNiva/front/resolver/protocols/string.niva#L16-L19

I'm writing a computer algebra library whose simplifier keeps the
operands of a sum or product in a canonical order, which compares
symbol names. Without an ordering, that means comparing `Char code`s
one by one, or binding `strings.Compare` on Go.

The IR already makes every comparison a native operator:

https://github.com/gavr123456789/Niva/blob/4e80971675d6a66b46d4890ce2390df4daea7cd9/Niva/NivaInNiva/ir/fromTypedAst.niva#L937-L943

so on Go, `"x" < "y"` would be Go's own `<`, which orders strings.
Clojure's `<` takes only numbers, so there it would need `compare`, the
way `+` on strings becomes `str`:

https://github.com/gavr123456789/Niva/blob/4e80971675d6a66b46d4890ce2390df4daea7cd9/Niva/NivaInNiva/back/clojureBackend/cljEmit.niva#L24-L32

What should strings have?

1. **`<`, `>`, `<=` and `>=` on `String`,** in each backend's own
   string order: Go's operators, and `compare` on Clojure. The two
   agree except when a character above U+FFFF is compared with one from
   U+E000 to U+FFFF, since Go compares UTF-8 bytes and Clojure UTF-16
   units.
2. **1, and `compareTo:`,** giving a negative number, 0 or a positive
   one, as in Kotlin and Java, for when a comparator is needed (a
   `sortedWith:`, say).
3. **No operators:** strings are ordered in user code, by `Char code`
   or with a bind, and the docs say so.

I'd suggest 1: it's small, and what most languages have; 2 could follow
when there's a use for it. Draft PR #PR shows what 1 looks like, with
tests, so there's something concrete to look at. You don't have to take
it, and if another option fits better, that's fine. `Char` could get
the same, but I've left it out.

Niva `self-host` at `4e809716`, Go 1.26, OpenJDK 25, Babashka 1.13,
Linux. Found while writing a small computer algebra library in Niva.
Thanks for Niva!
