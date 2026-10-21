# CLAUDE.md

Spring Boot 3.2 / Java 21 service. See README.md.

- Test: `gradle test`
- Build: `gradle build`
- Dependency changes: edit `build.gradle.kts`, then `gradle dependencies --write-locks`; commit both.
- Schema changes go in a new Flyway migration (`src/main/resources/db/migration/V<n>__<what>.sql`); never edit an applied one.
- Branches `fix/<topic>`; PRs via `gh pr create`; security fixes name the CVE in the PR title.
