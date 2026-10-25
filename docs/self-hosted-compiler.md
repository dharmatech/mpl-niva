# The self-hosted compiler

This project is compiled by Niva's self-hosted compiler, NivaInNiva, from
the [`self-host` branch](https://github.com/gavr123456789/Niva/tree/self-host),
not by the Kotlin compiler that Niva's install guide sets up. That's
where Niva is headed, and feedback on it is more useful to its author.
[Series 001](design/implementation/001-self-hosted/README.md) has the
reasons.

It compiles Niva to Go or to Clojure. We use Go; the Clojure backend
can't yet handle a program in more than one file
([Niva notes](niva-notes.md)).

## Building it

```bash
tools/build-self-hosted-niva
```

You need a JDK (this was built with OpenJDK 25) and Go. Clojure output
also needs [Babashka](https://babashka.org) or the Clojure CLI.

The script:

1. Clones the `self-host` branch to `~/src/Niva-self-host`, or uses the
   clone that's there. `NIVA_SELF_HOST` picks another place.
2. Checks out the pinned commit, `2ad4f629` (2026-10-25). It won't move
   a clone that has local changes.
3. Builds the branch's Kotlin compiler with Gradle.
4. Uses that to compile NivaInNiva into `Niva/NivaInNiva/main.jar`.

The Kotlin compiler in step 4 keeps a project template under
`~/.niva`. The script gives it a home of its own in the clone's
`local/` folder instead, so an installed `niva` and its `~/.niva` aren't
touched. `local/` is listed in the clone's `.git/info/exclude`.

That home also holds the Kotlin project made for NivaInNiva, which the
jar reads to map its own stack traces. To run the Kotlin compiler on
another program, say to compare it with NIN, give it a copy of the home
(`JAVA_OPTS=-Duser.home=…`). Run with `local/home` itself, it replaces
that project, and every NIN error after that also prints "Cant find
niva line above … in file main/kotlin/Main.kt" and exits 1, until the
script is run again.

To move to a newer Niva, change `commit` in the script and run it again.

## Running it

`tools/nin` runs the jar with whatever arguments it's given:

```bash
tools/nin run --backend=go
```

```bash
tools/nin --help
```

Without `--backend=go` it uses Clojure on Babashka, its default. It
writes the generated code to `.niva_go/` or `.niva_clj/`, which git
ignores. After moving the pin, remove `.niva_clj/`: the Clojure runtime
in it, `niva/core.clj`, is only written when it's missing, so it would
stay the old compiler's (Niva feedback item 33). Go's is rewritten on
every run.

`tools/check` runs the checks, on Go unless told otherwise. It reads the
output rather than the exit code, which is 0 even when the build fails.

### On let-go

The Clojure output also runs on [let-go](https://github.com/nooga/let-go)
(`lg`), which the compiler doesn't know about.
[Series 002](design/implementation/002-let-go/README.md) has what we
found. Generate the code with `tools/nin run … --backend=clj`, then in
`.niva_clj/` give `lg` a file holding `(require 'main) (main/-main)`:

```bash
lg -source-paths generated:. run.lg
```

`lg` has no `-m` yet; we've asked for one in
[#1096](https://github.com/nooga/let-go/issues/1096)
([draft](let-go-feedback/000-lg-main-option.md)). `lg -e` works too but prints the last value, an extra
line of output. Build `lg` from let-go's `main`: Niva's runtime needs
[#1085](https://github.com/nooga/let-go/pull/1085), merged 2026-10-22.
