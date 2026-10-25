# simplifier 006: powers

**Status:** Done at `9bbdd02` and reviewed (2026-10-25).

If you've been told to read this file, this is the whole assignment.
Build it, check it, commit it, report, and stop.

## Goal

A power simplifies by Cohen's rules for integers: `x^0` is `1`, `x^1`
is `x`, `1^x` is `1`, `2^3` is `8`, `(x^2)^3` is `x^6`, and
`(x * y)^2` is `x^2 * y^2`. Until now a power only simplified its base
and exponent. 007 will use this to combine like factors. Stop when the
checks pass and the commit is in.

## Read first

- [The spec](../spec.md): the decisions, and §006, which this
  checkpoint carries out.
- [Niva notes](../../../../niva-notes.md), before writing any Niva.
- `mpl/simplify.niva`: `simplify`, the frames, the pair rules as
  matches on `true`, and `isNum`, `numValue` and `listUnless:`.
- `~/src/mpl/sum-product-power.sls`: `^` and `simplify-power`. Cohen's
  `simplify_integer_power` is what the spec spells out below; mpl folds
  it into `^`.
- `tests/simplifyChecks.niva`, `main.niva`, and `docs/simplifying.md`.

## Starting point

`main` at the commit that adds this checkpoint. `tools/check` passes
its 110 checks. The compiler is built and pinned at `781c5b05`.

## Build

**`mpl/simplify.niva`**, in a "Powers" part after products:

- **`Expr simplifiedPower: Expr -> Expr`**, Cohen's `simplify_power`,
  on a simplified base (the receiver) and exponent `w`. The first that
  applies:
  - the base is `0`: `0` if `w` is a positive number, and otherwise the
    power as written, `0^0`, `0^(-1)` and `0^x` alike, since Cohen's
    Undefined isn't here yet;
  - the base is `1`: `1`;
  - `w` is a number: the base's `integerPower:` its value;
  - anything else: the power as written.
- **`Expr integerPower: Int -> Expr`**, Cohen's
  `simplify_integer_power`, for an integer `n`. The first that applies:
  - a number, with `n` not negative: worked out, so `2^3` is `8` and
    `(-2)^3` is `-8`. Int has no power that the notes know of, so a
    small helper on `Int` may be needed. With `n` negative it falls
    through to the last case, so `2^(-1)` stays as written until there
    are fractions;
  - `n` is 0: `1`; `n` is 1: the base itself;
  - a power `r^s`: `p` is the simplified product of `s` and `n`; if `p`
    is a number, `r integerPower:` its value, and otherwise `r^p`;
  - a product: each factor's `integerPower: n`, then the simplified
    product of those;
  - anything else: `Power base: this exp: n num`.
- **`simplify`'s `Power` branch** hands its simplified base and
  exponent to `simplifiedPower:`.
- Matches on `true`, one arm a case, as in the pair rules. Comments
  name Cohen's functions, and the file's top comment says what holds
  now.

A one-item list of an expression built in place needs parentheses,
`{(c num)}`, or a local first: `{c num}` is a list of two.

**`tests/simplifyChecks.niva`:** the spec's §006 table, as `got: (e
simplify) text want:` checks named like `"powers: x^0"`, in a group
after the like-terms checks. `pow:` binds loosest, so `(x^2)^3` is
`(x pow: 2 num) pow: 3 num`, and negative exponents are `-1 num`.
Also:

- `0^0` isn't `1`, as a `holds:` check with `==` and `not`. Don't pin
  what it is: Undefined will change it.
- `2^0` is `1`, and `(1 + 1)^3` is `8`: a base that simplifies to a
  number.
- `x^(2 + -2)` is `1`: an exponent that simplifies to 0.
- `(x^2)^(-1)` is `x^(-2)`, and `(x^y)^z` is as written.
- `(x^1)^2 + x^2` is `2 * x^2`: a power rule, then like terms.

Write each input so that its result is final: no product whose factors
share a base, which 007 would combine, and no negative power of a
number, which fractions would change. The checks from 002 to 005 must
still pass unchanged.

**The demo:** an example or two of powers, such as `(x * y)^2`, which
is `x^2 * y^2`, and `(x^2)^3`, which is `x^6`.

## Docs

- **`docs/simplifying.md`:** the power rules, an example each, and a
  sentence on what stays as written for now (`0^0`, `0^(-1)`, `2^(-1)`)
  and why. The line saying a power only simplifies its base and
  exponent goes.
- **`README.md`:** the status line says what simplifying does now.
- **The series [README](../README.md):** mark 006 done in its
  increments table. Leave the commit cell for the manager.
- **Niva findings** go in the
  [feedback list](../../../../niva-feedback/README.md) as the next
  item, with a small program, and the workaround in MPL gets a comment
  naming the item. Don't file issues or PRs with Niva, and don't post
  on Discord.

## Check and commit

- `tools/check` ends with `all N checks passed`, N being 110 plus
  yours.
- `tools/nin run --backend=go` shows the new examples.
- One commit on `main`, with the code, checks and docs, in the style of
  `git log`: a subject line, then a wrapped paragraph saying what and
  why. Don't push.

## Report

When done, say in a few lines: the commit, the number of checks,
anything in the spec that was wrong, and any Niva findings. Then stop;
increment 007 is a separate checkpoint.
