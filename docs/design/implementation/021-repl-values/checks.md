# What series 021 checked

The checks behind [series 021](README.md)'s, at the pin, `dccd5143`,
with Clojure CLI 1.12.6 on OpenJDK 25. Each ran in a copy of the
project, so a session running at its root wasn't disturbed. "Before" is
`tools/repl-jvm` at `92a430e`.

The transcripts were taken on a terminal, under `script`, a line a
second, so each line shows after its prompt; the colours of NIN's
messages are taken out. Piped in, the same lines print the same output
without the prompts.

## The demo

`main.niva`'s 40 lines from `x = "x" sym`, piped in, print what
`tools/nin run --backend=go main.niva` prints, then the blank line the
REPL ends with: `diff` finds nothing, against Go's output or the
script's before. 7.4 s, against 7.5 s before.

## Values

```
niva> 10
10
niva> x = "x" sym
niva> x + x
mpl_tree_Sum terms: {mpl_tree_Sym name: "x" mpl_tree_Sym name: "x"}
niva> {1 2}
{1 2}
niva> res1 + 1
11
niva> x showSimplified
x   =>   x
niva> "" echo

niva> "hi" echo
hi
niva> "hi"
hi
niva> res2 echo
mpl_tree_Sum terms: {mpl_tree_Sym name: "x" mpl_tree_Sym name: "x"}
niva> mut n = 1
niva> n <- 5
niva> n
5
```

`res1` is `10` and `res2` is `x + x`. The echo lines and
`x showSimplified` print only their own output.

## More values

```
niva> x = "x" sym
niva> 10 // ten
10
niva> "a" echo // a comment
a
niva> ("b" echo)
b
niva> "c" echonl
cniva> x == x
true
niva> {1 2 3} map: [it / 0]
Divide by zero
(not kept)
niva> {1 2 3} map: [it * 2]
{2 4 6}
niva> {"a" "b"}
{"a" "b"}
niva> #{1 2}
#{1 2}
niva> res5
true
```

`echonl` prints no newline, so the next prompt follows it, as before.
`res5` is `x == x`: the four lines before it each took a number.

## Lines that fail

```
niva> x = "x" sym
niva> (1 / 0) echo
Divide by zero
(not kept)
niva> x foo
Searching for foo method but its not found for type  Type(Expr ) at repl.niva:2:1
(not kept)
niva> x frobnicate: 3
Searching for frobnicate method but its not found for type  Type(Expr ) at repl.niva:2:1
(not kept)
niva> (1 +
step: EOF
Can't locate source line of error from: parseManyBinary
(not kept)
niva> )
repl.niva:2:0  nextExpression: no primary1 (CloseParen()))
(not kept)
niva> x = "y" sym
variable x with type  Type(Expr ) was already declared, declare it with mut and use <- instead of = for mutation
(not kept)
niva> type Point x: Int y: Int
niva> type Point x: Int y: Int
Compiler bug: such type was already added to methodDB
(not kept)
niva> Point x: 1 y: 2
repl_Point x: 1 y: 2
niva> p = Point x: 1 y: 2
niva> p x
1
niva> Int twice -> Int = this + this
niva> Int twice -> Int = this * 2
Type  Type(Int) already has method (NivaMethod  twice() ->  Type(Int))
(not kept)
niva> 3 twice
6
niva> {1 2 3} at: 10
Idx out
(not kept)
niva> mut n = 1
niva> n <- n + 1
niva> n
2
niva> m <- 3
niva> "" echo

niva> "a" + 1
Resolver errors: repl.niva:13:7 wrong arg type for + expected  Type(String) but got  Type(Int)
(not kept)
niva> q
Cant find q in the current context: 
  x:  Type(Expr ), res1:  Type(Point x:  Type(Int) y:  Type(Int)), p:  Type(Point x:  Type(Int) y:  Type(Int)), res2:  Type(Int), res3:  Type(Int), n:  Type(Int), res4:  Type(Int), res5:  Type(String) repl.niva:13:1
(not kept)
niva> x showSimplified
x   =>   x
```

Piped in, before and after, `diff` finds only the four values now
printed, `repl_Point x: 1 y: 2`, `1`, `6` and `2`, and the variables
listed for `q`, which before were `x`, `p` and `n`. Every message is
the same, at the same line and column: it comes from building the line
as it is, as before. `m <- 3` builds with no `m` declared, before as
now; see [found in NIN](README.md#found-in-nin-on-the-way).

## The second build

Twenty lines of each kind, piped in, two runs each, the times for the
whole session, starting included:

| Lines | Before | After | More a line |
|---|---|---|---|
| None, only starting | 2.05, 1.97 s | 2.08, 1.98 s | |
| 20 methods, `Int fN -> Int = this + N` | 5.30, 5.21 s | 5.48, 5.28 s | about 6 ms |
| 20 types, `type TN a: Int` | 5.29, 5.44 s | 5.54, 5.52 s | about 8 ms |
| `mut n = 0`, then 20 of `n <- n + N` | 5.21, 5.26 s | 5.27, 5.19 s | none |
| 20 variables, `vN = N` | 5.67, 5.61 s | 5.68, 5.63 s | none |

A line takes about 160 ms. Assignments took 6.12 and 6.13 s, about
42 ms more a line, while they were tried as `resN = n <- …`, which
fails only in the Clojure backend; since then they're recognized by
their text, as variable declarations are.

## Starting and ending

- With a line added to `main.niva` that doesn't parse, the REPL prints
  NIN's message and exits 1, as before, leaving no `repl.niva`.
- After Ctrl-D on a terminal, `repl.niva` is gone.
