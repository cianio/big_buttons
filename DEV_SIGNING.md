# Development signing

`app/dev-debug.keystore` is intentionally a development-only key so GitHub-built debug APKs can update one another.

- store password: `bigbuttons`
- alias: `bigbuttons-debug`
- key password: `bigbuttons`

Do not use this public repository key for a production release. Create and securely back up a private production keystore before publishing BigButtons.
