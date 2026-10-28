# Which runtime for a live REPL

Measured on 2026-10-26, to pick an engine for a REPL with live state
rather than replay. MPL's REPL module comes from NIN at `7ac94536` with
the item 37 fix, our
[#464](https://github.com/gavr123456789/Niva/pull/464), merged after
the pin ([draft](../../../niva-feedback/012-clj-repl-types-first-pr.md)):
`tools/nin build main.niva --backend=clj --repl`, which gives one
`main.clj` beside the runtime's `niva/core.clj`.
[`tools/check-repl`](../014-check-repl/README.md) checks the output
on each.

## Loading the module and running the demo

Each run starts the runtime, loads the 564 lines and prints the demo's
34 lines, the same as `tools/nin run --backend=go` on each:

| Runtime | Command, in `.niva_clj/` | Time |
|---|---|---|
| `bb` 1.13.226 | `bb -cp . main.clj` | 0.08 s |
| JVM Clojure, OpenJDK 25 | `java -cp <clojure -Spath>:. clojure.main main.clj` | 1.4 s |
| the same, through the Clojure CLI 1.12.6 | `clojure -M -i main.clj` | 1.4 to 2.3 s |
| `lg`, let-go `18df3ad5` | `lg -source-paths . main.clj` | 0.055 s |

For comparison, the [Go replay](README.md#what-was-considered) takes
0.7 s a line, growing with the session, since every line runs again
the ones before it. `ixgo` interprets a whole program too, so it's also
a replay; it wasn't timed.

**let-go runs it.** As a program, MPL stops on `lg` at a record class
named from another namespace,
[let-go #1101](https://github.com/nooga/let-go/issues/1101). REPL mode
puts every package in one namespace, so nothing is named that way, and
`lg`'s output is the same as `bb`'s. NIN doesn't target let-go, so
keeping this working would be ours to do.

## Compiling a line

In a live session, each line also goes through NIN first:
`build --backend=clj --repl --repl-line=64` takes 0.6 s on MPL with the
jar, of which starting the JVM (`--help`) is 0.1 s. The rest is
checking the whole project again for one line. So per line, the
compiler costs more than any runtime takes to evaluate it. A NIN that
stays running between lines would cut that, as `nin watch` keeps a
compiler session for hot reload. The Go-built NIN doesn't help yet: it
takes 35 s to 3 min on MPL's REPL module.

## So

- **`bb` first:** it starts at once, keeps a live session, NIN targets
  it, and MPL passes all 151 checks on it.
- **JVM Clojure** pays 1.4 s once per session and then runs fastest,
  with the JIT: the one for heavy work, which MPL doesn't do yet.
- **let-go** starts fastest and is one binary, so it's the candidate
  to try as the engine once the loop works on `bb`. Its speed on heavy
  work wasn't measured.
- **The compiler** is the bigger cost per line, so a long-running NIN
  matters more than the choice of runtime.

Item 38, a line's records defined again in the session, came before
any of these. Its fix, our #466, is in the pin since
[series 016](../016-clj-repl-records-pin/README.md).
