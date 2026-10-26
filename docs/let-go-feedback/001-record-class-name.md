# 001 A record's class name from another namespace

**Status:** Drafted 2026-10-26, not filed. Ready to file: Niva PR
[009](../niva-feedback/009-clj-record-names-pr.md), whose change it's
about, is open as [#461](https://github.com/gavr123456789/Niva/pull/461), and the last paragraph cites it.
Filed before it, it would have brought up Niva feedback item 3 before
Gavr had it.
**Repository:** [nooga/let-go](https://github.com/nooga/let-go/issues)
**Checked against:** let-go `main` at `18df3ad5` (2026-10-22), still
`main` on 2026-10-26

Found while fixing Niva feedback item 3 in
[009](../niva-feedback/009-clj-record-names-pr.md). Niva's Clojure
output named a record from another namespace with `/`,
`niva.generated.model/model_Circle`, which only `lg` resolves. The fix
names it as Clojure does, `niva.generated.model.model_Circle`, which
only `bb` and JVM Clojure resolve. So with the fix, MPL builds on `bb`
and JVM Clojure and stops on `lg`. The user chose that on 2026-10-26:
fix Niva for its own runtimes first, and ask let-go for the class name.

No existing issue covered it on 2026-10-26 (searched for defrecord,
record, import, fully qualified, `instance?`, "Can't resolve").
let-go's own plans cover the `:import` half: `docs/jvm-compat-plan.md`
says the clause is "Currently silently ignored", and step 4 of
`docs/clojure-compat-roadmap.md` has it refer the matching var. Nothing
there names a record by its class.

It's framed as a gap in Clojure compatibility, not as something Niva
needs: the forms `lg` rejects are documented Clojure, and code with
nothing to do with Niva hits it too, such as `extend-protocol` on
another namespace's record. let-go's stated target is full Clojure
compatibility on the JVM-interop surface (`docs/contribution-policy.md`).
The quote is from `defrecord`'s docstring, `(doc defrecord)` on Clojure
1.12.

The comparison was run with `clojure` (CLI 1.12.6.1673, JVM), `bb`
1.13.226 and `lg` at `18df3ad5`, from the files below. `lg` ran
`(require 'app) (app/-main)` from a `run.lg`, since it has no `-m` yet
([000](000-lg-main-option.md)). In `lg`'s compiler, a symbol that
doesn't resolve falls back to `rt.LookupHostClass`, which already maps
names like `java.util.Map` to a type, and each `defrecord` makes a
`vm.RecordType` named only `Circle`.

Left out, as in [Held](README.md#held): that `lg` accepts a dotted
record name. That waits until 009 is merged.

Everything below the line is the issue as it would be filed.

---

**Title:** Clojure compatibility: a record from another namespace can't be named by its class, `ns.Record`

## Summary

In Clojure, `defrecord` makes a class "in a package with the same name
as the current namespace". So another namespace names the record by
that class, `geo.shapes.Circle`, or by the short name an
`(:import [geo.shapes Circle])` brings in, as on JVM Clojure and
Babashka. `lg` resolves neither. It takes only the var form,
`geo.shapes/Circle`, which the other two reject, so code that names
another namespace's record can't run on both `lg` and Clojure.

```clojure
;; src/geo/shapes.clj
(ns geo.shapes)
(defrecord Circle [radius])
```

```clojure
;; src/app.clj
(ns app (:require [geo.shapes]))
(defn -main [& _]
  (println (instance? geo.shapes.Circle (geo.shapes/->Circle 1))))
```

| `app.clj` names the record | `clojure -M -m app` | `bb -cp src -m app` | `lg` |
|---|---|---|---|
| `geo.shapes.Circle` | `true` | `true` | Can't resolve geo.shapes.Circle in this context |
| `Circle`, with `(:import [geo.shapes Circle])` in the `ns` | `true` | `true` | Can't resolve Circle in this context |
| `geo.shapes/Circle` | No such var: geo.shapes/Circle | Unable to resolve symbol: geo.shapes/Circle | `true` |
| `geo.shapes.Circle` in `extend-protocol`, below | `12` | `12` | Can't resolve geo.shapes.Circle in this context |

The last row is a protocol extended to the record, in place of the
`instance?`:

```clojure
;; src/app.clj
(ns app (:require [geo.shapes]))
(defprotocol Area (area [s]))
(extend-protocol Area geo.shapes.Circle
  (area [c] (* 3 (:radius c) (:radius c))))
(defn -main [& _] (println (area (geo.shapes/->Circle 2))))
```

There, the imported `Circle` and `geo.shapes/Circle` give what they give
for `instance?`.

`lg` ran `(require 'app) (app/-main)` from a `run.lg`, with
`lg -source-paths src run.lg`.

## Proposal

1. Resolve a dotted symbol `a.b.C` that names no host class as the var
   `C` in namespace `a.b`, when that var holds a record type. For a
   namespace with a dash, the class has `_` in its place, as Clojure
   munges it: in `my-geo`, `Circle` is `my_geo.Circle`.
2. Have `(:import [a.b C])` refer it, as `docs/clojure-compat-roadmap.md`
   (step 4) already plans for host classes. Today the clause is ignored
   without a warning.

The host-class fallback for a bare symbol in `compileForm`, through
`rt.LookupHostClass`, looks like the place for 1.

A design point for you to decide: `(type (geo.shapes/->Circle 1))`
prints `Circle` on `lg` and `geo.shapes.Circle` on the other two, so
two records with the same name in two namespaces print the same.

## Context

We found this through Niva. Its Clojure backend is changing to name
another namespace's record by its class, as JVM Clojure and Babashka
need (gavr123456789/Niva#461). It
used to emit the var form, which only `lg` resolved, so a Niva program
in several files that matches on records ran on `lg` and nowhere else.
After that change it runs on `bb` and JVM Clojure, and with 1 it would
run on `lg` too. Our case is MPL, a small computer algebra simplifier
in Niva, which ran on `lg` before the change.
