# Contributing

Use English commit messages following [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/):

```text
type(optional-scope): short description

Optional body explaining the problem, resulting behavior and validation.

Optional trailers.
```

Use `feat` for features, `fix` for bug fixes, `perf` for measured performance work, `refactor` for internal restructuring, `test` for verification and `docs`/`build`/`chore` for their corresponding changes. Prefer a concrete subsystem scope such as `crops`, `cables`, `recipes` or `network`.

Mark incompatible API changes with `!` and describe migration in a `BREAKING CHANGE:` footer. For example:

```text
refactor(runtime)!: move addon contracts to ic2ce.api

BREAKING CHANGE: Addons must recompile against ic2ce.api.
```

Keep unrelated changes in separate commits. Cite actual validation and retain unfavorable benchmark results alongside improvements. Gameplay tests require disposable instances; production JARs must not include destructive fixture classes or game/library code.
