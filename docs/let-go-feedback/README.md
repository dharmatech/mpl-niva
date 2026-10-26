# let-go feedback

Issues and suggestions for [let-go](https://github.com/nooga/let-go)
found while running Niva's Clojure output on it
([series 002](../design/implementation/002-let-go/README.md)), drafted
here before they're filed on
[nooga/let-go](https://github.com/nooga/let-go/issues).

let-go has a team that works through issues quickly, so these don't
wait on each other the way [Niva feedback](../niva-feedback/README.md)
does. A draft waits only when filing it would bring up a Niva bug we
haven't reported yet.

Each draft is numbered in the order it was written. It starts with a
short header (status, the let-go commit it was checked against, how we
found it), and below a line, the issue's title and body as filed. Once
filed, the header links the issue.

| Draft | Status |
|---|---|
| [000 `lg -m`: run a namespace's `-main`](000-lg-main-option.md) | Filed as [#1096](https://github.com/nooga/let-go/issues/1096) (2026-10-23). |
| [001 A record's class name from another namespace](001-record-class-name.md) | Filed as [#1101](https://github.com/nooga/let-go/issues/1101) (2026-10-26). `lg` names another namespace's record only as `ns/Name`, which JVM Clojure and `bb` reject, so with Niva's fix for item 3 ([009](../niva-feedback/009-clj-record-names-pr.md)) MPL stops on `lg`. Filed right after that PR was opened as [#461](https://github.com/gavr123456789/Niva/pull/461). |

## Held

- **`lg` accepts a dotted name that Clojure reads as a class.**
  `(defrecord a.b_C [x]) (->a.b_C 1)` returns a record on `lg`;
  JVM Clojure throws `ClassNotFoundException`, and Babashka rejects the
  `defrecord`. Niva's Clojure output depends on this today (Niva
  feedback item 3), so MPL would stop running on `lg` if let-go turned
  strict first. It waited for Niva's fix,
  [009](../niva-feedback/009-clj-record-names-pr.md), merged on
  2026-10-26 as [#461](https://github.com/gavr123456789/Niva/pull/461),
  so it can be filed. MPL at our pin, `781c5b05`, still has the dotted
  names, until the pin moves past `6313be64`.
