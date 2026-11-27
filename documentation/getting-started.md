# Getting Started

This page is for developers working on the Generic Web Request plugin itself. To use the plugin in a GZAC/Valtimo
application, see the [plugin documentation](plugin.md).

## Prerequisites

- Java 21
- Node 20 (`nvm use 20`, see `frontend/.nvmrc`)
- [Docker (Desktop)](https://www.docker.com/products/docker-desktop/), for the integration tests and the example
  application

## Project structure

| Path                                     | Contents                                                                    |
|------------------------------------------|-----------------------------------------------------------------------------|
| `backend/plugin`                         | The plugin, published as `com.ritense.valtimoplugins:generic-web-request`    |
| `backend/app`                            | Example application to try the plugin, see [Example Application](example-application.md) |
| `frontend/projects/plugin`               | The plugin's frontend, published as `@valtimo-plugins/generic-web-request`   |
| `frontend/src`                           | Frontend of the example application                                         |

### Backend (`backend/plugin/src/main/kotlin/com/ritense/valtimoplugins/genericwebrequest`)

| Package             | Contents                                                                                      |
|---------------------|-----------------------------------------------------------------------------------------------|
| `plugin`            | `GenericWebRequestPlugin` (properties and the `send-request` action) and its factory          |
| `value`             | `PropertyValueResolver`: resolves header and body values that are a value resolver key (`pv:`, `doc:`, ...) |
| `client`            | `GenericWebRequestClient`: sends the request with the authentication of an `HttpClientAuthenticator` |
| `domain`            | Request, response and exception classes                                                       |
| `autoconfiguration` | Spring beans of the plugin                                                                    |

### Frontend (`frontend/projects/plugin/src/lib/plugins/generic-web-request`)

| Path                                         | Contents                                                     |
|----------------------------------------------|--------------------------------------------------------------|
| `generic-web-request.specification.ts`       | Plugin id, action components and NL/EN translations          |
| `components/generic-web-request-configuration` | Form for the plugin configuration                          |
| `components/send-request-configuration`      | Form for the `send-request` action                           |
| `models`                                     | Types of the configuration and action properties             |

The plugin key (`generic-web-request`), action key (`send-request`) and property names must be the same in the
backend and the frontend.

## Build and test

All commands are run from the project root, unless stated otherwise.

Backend tests (unit tests, and integration tests that start a Postgres container):

```shell
./gradlew :backend:plugin:test
```

Code style (ktlint):

```shell
./gradlew :backend:plugin:ktlintCheck   # check
./gradlew :backend:plugin:ktlintFormat  # fix
```

Frontend plugin library:

```shell
cd frontend
nvm use 20
npm install
npx ng build @valtimo-plugins/generic-web-request
```

## Releasing

Update the version in `backend/plugin/plugin.properties` and `frontend/projects/plugin/package.json`, and add the
changes to the [release notes](release-notes.md). The GitHub workflows `publish-backend` and `publish-frontend`
publish the packages.

For more information on how to build a plugin, see
the [Custom Plugin Definition](https://docs.valtimo.nl/features/plugins/plugins/custom-plugin-definition) documentation.
