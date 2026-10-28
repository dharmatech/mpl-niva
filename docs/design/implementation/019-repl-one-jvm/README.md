# 019 repl-one-jvm

**Status:** Done; landed at `84ff3c2` (2026-10-28).

`tools/repl --backend=clj --runtime=jvm` runs NIN inside the session's
own JVM: the compiler and the session are one process, as in Clojure,
Scala or F# Interactive. NIN stays warm between lines, so a line takes
about 0.14 s instead of 0.6 s. `bb` and `lg` are as in
[series 018](../018-clj-repl-loop/README.md), with NIN started again
for each line. It's the default runtime since `<commit>`.

```
tools/repl --backend=clj                # the JVM
tools/repl --backend=clj --runtime=bb   # or bb, or lg
```

## Why

In series 018 a line cost 0.6 s, nearly all of it NIN starting over: a
new JVM, a cold JIT, the whole project checked again. Hosted languages
whose REPL is one process are the ones whose compiler runs on the host,
and NIN is JVM bytecode (`main.jar`, `mainNiva.MainKt`), so it can share
a JVM with Clojure. `bb` is a native image and can't load the jar, nor
can `lg`, so this is JVM-only.

An experiment came first: calling NIN's `main` in one JVM five times,
the build took 531 ms, then 183, then about 100. Over the demo's
lines, warm builds took 40 to 60 ms and loading the module about
80 ms.

## What was considered

- **A NIN that stays running beside `bb`,** two long-lived processes,
  as `nin watch` keeps a compiler session and drives a separate `bb`.
  `nin build` has no such mode, so it needs a change to Niva.
- **NIN inside `bb`:** NIN compiled to Clojure by its own backend and
  interpreted by SCI. Whether NIN compiles that way is unknown, and a
  type checker run by an interpreter would likely be slower than 0.6 s.
- **A Niva interpreter in `bb`:** a second implementation of the
  language, where Clojure's `eval` already runs what NIN emits.
- **The whole REPL in the JVM, without bash:** `clojure.main` has no
  line editing, and bash does only that and the session file, so it
  stays.

## Decisions

- **NIN's `main`, with the options `tools/nin` would get:** no API is
  needed. `build` never exits the process: a parse error, a stray `)`,
  an unknown method, a type error, a type or variable declared twice
  each come back as a message with no module written, and the JVM goes
  on. NIN's `System exitWith:` is only in `run`, and in one place after
  an error has already been thrown.
- **The session builds the line itself:** on JVM Clojure a request is
  the build's options, on `bb` and `lg` the path of a module `tools/repl`
  built. One loop serves all three, with a `run` for each.
- **What NIN prints is caught** by swapping `System/out` and
  `System/err` around the call, and becomes the error when no module
  was written. The line's own output is unaffected, since Clojure's
  `*out*` holds the stream it started with.
- **The JVM starts in the temporary folder,** since NIN writes
  `.niva_clj/` where its JVM runs and a JVM can't change folder. `bb`
  and `lg` start there too now, with `.niva_clj` as their path. That
  folder is made before the JVM starts, or it's left off the classpath.
- **The classpath:** Clojure's, from `clojure -Spath`; NIN's jar, as
  `tools/nin` finds it, `NIVA_SELF_HOST` included; and `.niva_clj`.
- **The JVM the default:** `bb` starts in 0.7 s against 1.9 s and
  holds about 120 MB against 470, but the JVM is ahead from the third
  line or so. `bb` stayed the default when this landed, and the user
  made it the JVM at `<commit>`.

## What's known not to work yet

- **Loading is now most of a line's cost** on the JVM, about 80 ms:
  every line's module sends MPL's functions again, about 530 lines,
  and JVM Clojure compiles each to bytecode. A session-aware REPL mode,
  step 4 of [the path to a sound REPL](../010-repl/sound-repl-path.md#the-steps),
  would send only the line.
- **NIN keeps no state between builds that showed:** after 40 builds
  and a failure in one JVM, the session's module built there was
  byte-identical to a fresh NIN's. That's not a proof, and if a long
  session shows otherwise, a new session is the way out.
- **NIN's main class is named:** `mainNiva.MainKt`, which a change to
  how NIN is built could rename.
- Series 018's gaps hold here too: a record whose fields change keeps
  its old fields.

## What changed

- [`tools/repl`](../../../../tools/repl): `--runtime=jvm` runs NIN in
  the session, and every runtime starts in the temporary folder. Then,
  at `<commit>`, `jvm` became the default.
- [The REPL](../../../repl.md) and the README say so, as do series
  [018](../018-clj-repl-loop/README.md) and 010's
  [runtimes](../010-repl/runtimes.md).

## What we checked

At the pin, `dccd5143`, with Clojure CLI 1.12.6 on OpenJDK 25,
Babashka 1.13.226 and let-go `18df3ad5`:

- **The demo, line by line:** `main.niva`'s 40 lines from `x = "x" sym`
  on print what `tools/nin run --backend=go` prints, on `jvm`, `bb` and
  `lg`, in 7.4 s on `jvm` and 24 s on `bb` and `lg`.
- **Time:** 1.9 s to start on `jvm`, then about 0.14 s a line; on
  `bb`, 0.7 s, then about 0.6 s a line.
- **Lines that fail:** twenty lines, among them each failure above, a
  division by zero, an index out of range, `mut`, and `"" echo`, print
  the same on `jvm` as on `bb`.
- **An edit during a session:** `showSimplified` changed in
  `main.niva` shows on the next line, since NIN reads the files again
  for each build.
- **Memory:** the session's JVM holds about 470 MB.
