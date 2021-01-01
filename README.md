# digdag-plugin-sentry
[![Digdag](https://img.shields.io/badge/digdag-v0.9.42-brightgreen.svg)](https://github.com/treasure-data/digdag/releases/tag/v0.9.42)


# Development

## 1) build

```sh
./gradlew publish
```

Artifacts are build on local repos: `./build/repo`.

## 2) run an example in local mode

```sh
digdag selfupdate

digdag run --no-save --project sample \
  example_local.dig \
  -p repos=`pwd`/build/repo \
  -p dsn=https://your_sentry_dsn@project.ingest.sentry.io/0000000
```

## 3) run an example in server mode with secrets

```sh
digdag selfupdate

digdag server --memory --config sample/server.properties
```

then, from another terminal:

```sh
digdag push --project sample sample

# set your dsn
digdag secrets --project sample --set sentry.dsn

digdag start sample example_server --session now
```
