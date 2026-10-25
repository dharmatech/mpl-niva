# 008 simplifier

**Status:** The spec's seven increments are done and reviewed
(2026-10-25). What comes next is open; see below.

Cohen's automatic simplification, built up in small increments: the
order relation, flattening, canonical order, numbers and identities,
like terms, powers, like factors. Too big for one conversation, so it
runs as a charter, a spec and checkpoints.

- [charter.md](charter.md): the handoff. Where MPL is, what Niva gives
  it now, the design questions, candidate increments, and how to work.
- [spec.md](spec.md): what was decided, and the seven increments with
  their checks.
- `checkpoints/`: one per increment, written when the one before has
  landed, and implemented in a fresh conversation each. The
  conversation that wrote the spec manages them and reviews each
  commit, and the user sees each review before the next one starts.

## Increments

| # | Increment | Checkpoint | Status | Commit |
|---|---|---|---|---|
| 001 | order relation | [001](checkpoints/001-order-relation.md) | Done, reviewed (2026-10-25). | `c277081` |
| 002 | flattening | [002](checkpoints/002-flattening.md) | Done, reviewed (2026-10-25). | `68906c4` |
| 003 | canonical order | [003](checkpoints/003-canonical-order.md) | Done, reviewed (2026-10-25). | `35e803d` |
| 004 | numbers and identities | [004](checkpoints/004-numbers-and-identities.md) | Done, reviewed (2026-10-25). | `5dd2b01` |
| 005 | like terms | [005](checkpoints/005-like-terms.md) | Done, reviewed (2026-10-25). | `ad0bb27`, `f5a3efe` |
| 006 | powers | [006](checkpoints/006-powers.md) | Done, reviewed (2026-10-25). | `9bbdd02` |
| 007 | like factors | [007](checkpoints/007-like-factors.md) | Done, reviewed (2026-10-25). | `435dea7` |

## What's open

Left by the spec, to decide before any more work:

- **Undefined,** for `0^0` and `0^(-1)`, which stay as written.
- **Fractions,** with `gcd`, so `2^(-1)` is `1/2`, and `-` and `/`.
- **The known gap:** what two like operands make is taken as one
  operand, so a sum or product it makes isn't spliced into the one
  around it. Cohen's algorithm and mpl's have it too.
  [Simplifying](../../../simplifying.md) has the examples.
- **A display order,** such as `x + 5` for `5 + x`, or `-x` for
  `-1 * x`.
