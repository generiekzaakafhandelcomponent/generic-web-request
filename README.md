# Generic Web Request plugin

A GZAC/Valtimo plugin that sends a configurable HTTP request to an external system from a BPMN service task.
Built from the GZAC Plugin Template.

## Features

- **One configuration per external system**: a base URL and an
  [HTTP Client Authentication](https://github.com/valtimo-platform/valtimo-plugins) configuration (none, API key header,
  basic, bearer, token, ZGW JWT, token exchange, SAML token or mutual TLS).
- **Action `send-request`**: method (GET, POST, PUT, PATCH, DELETE), path, headers and body set per process link.
  Headers and body fields are key/value pairs; the body is sent as a JSON object.
- **Values from the case**: each value is literal text, a value resolver key such as `pv:id` or `doc:/path` (keeping its
  type), or text with keys in it, such as `https://example.com/items/pv:id`. Environment variables can be used as
  `${NAME}` through Valtimo's import whitelist.
- **Result and errors**: the response (`statusCode`, `body`) can be stored in a process variable. The task fails on a
  value that can't be resolved, an unreachable server or a non-2xx response, so it can be retried as an incident.

## Compatibility

| Component                   | Version  |
|-----------------------------|----------|
| GZAC/Valtimo                | 13.48.0  |
| HTTP Client Authentication  | 3.1.1    |
| Java                        | 21       |
| Angular (frontend)          | 19       |

## Installation

Backend:

```kotlin
implementation("com.ritense.valtimoplugins:generic-web-request:0.0.1")
```

Frontend:

```shell
npm install @valtimo-plugins/generic-web-request@0.0.1 @valtimo-plugins/http-client-authentication@3.1.1
```

Then register both plugin modules and specifications in `app.module.ts`, as described in the
[plugin documentation](documentation/plugin.md#frontend).

## Example

A process link that posts a notification with a value from the process:

```json
{
    "method": "POST",
    "path": "/notifications",
    "body": [
        {"key": "actie", "value": "Create"},
        {"key": "resourceUrl", "value": "${EXAMPLE_RESOURCE_URL}/pv:besluitId"}
    ],
    "resultVariable": "notificatieResultaat"
}
```

The [example application](documentation/example-application.md) runs this against a local mock API.

## Documentation

- [Generic Web Request plugin](documentation/plugin.md): configuration, actions, values and error handling
- [Example Application](documentation/example-application.md): running the example app locally
- [Getting Started](documentation/getting-started.md): project structure, building and testing, releasing
- [Release notes](documentation/release-notes.md): changes per version

## Contact

-- naam contactpersoon (bedrijfsnaam)
