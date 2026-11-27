# Generic Web Request plugin

## Overview

Sends a configurable HTTP request to an external system from a BPMN service task. The plugin knows nothing about
the system it calls: the base URL and authentication are set once per system in the plugin configuration, and what
to send (method, path, headers and body) is set per process link.

Authentication is delegated to the [HTTP Client Authentication](https://github.com/valtimo-platform/valtimo-plugins)
plugin, which supports no authentication, API key headers, basic, bearer, token, ZGW JWT, token exchange, SAML token
and mutual TLS.

## Dependencies

### Backend

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:generic-web-request:0.0.1")
}
```

`com.ritense.valtimoplugins:http-client-authentication` comes with it as a transitive dependency.

### Frontend

```json
{
  "dependencies": {
    "@valtimo-plugins/generic-web-request": "0.0.1",
    "@valtimo-plugins/http-client-authentication": "3.1.1"
  }
}
```

In your `app.module.ts`:

```typescript
import {GenericWebRequestPluginModule, genericWebRequestPluginSpecification} from '@valtimo-plugins/generic-web-request';
import {
    HttpClientAuthenticationPluginModule,
    httpClientAuthenticationPluginSpecification,
} from '@valtimo-plugins/http-client-authentication';

@NgModule({
    imports: [
        GenericWebRequestPluginModule,
        HttpClientAuthenticationPluginModule,
    ],
    providers: [
        {
            provide: PLUGIN_TOKEN,
            useValue: [
                genericWebRequestPluginSpecification,
                httpClientAuthenticationPluginSpecification,
            ]
        }
    ]
})
```

## Configuration

First create an **HTTP Client Authentication** configuration for the system (use `NONE` when it needs no
authentication), then a **Generic Web Request** configuration:

| Property                          | Type   | Required | Description                                                     |
|-----------------------------------|--------|----------|-----------------------------------------------------------------|
| baseUrl                           | URI    | Yes      | URL of the external system. The action's path is appended to it |
| authenticationPluginConfiguration | UUID   | Yes      | An HTTP Client Authentication configuration                     |

## Actions

### Send request (`send-request`)

Available on the start of a service task.

| Property       | Type                          | Required | Description                                                                           |
|----------------|-------------------------------|----------|---------------------------------------------------------------------------------------|
| method         | GET, POST, PUT, PATCH, DELETE | Yes      | HTTP method                                                                           |
| path           | string                        | No       | Appended to `baseUrl`                                                                 |
| headers        | list of `{key, value}`        | No       | Additional headers. Authentication headers come from the authentication configuration |
| body           | list of `{key, value}`        | No       | The fields of the JSON object that is sent (`Content-Type: application/json`)         |
| resultVariable | string                        | No       | Process variable to store the response in, as `{statusCode, body}`                    |

#### Values

Each header and body value is one of:

- **literal text**, such as `Aanvraag` or `https://example.com`, sent as-is;
- **a value resolver key** as the whole value, such as `pv:besluitId`, `doc:/besluit/id` or `case:assignee`, replaced
  by what it resolves to. In the body, the resolved value keeps its type: a number stays a number, a list a list;
- **text with value resolver keys in it**, such as `https://example.com/items/pv:id`. Each key is replaced by its
  value as text. A key inside a text runs from its prefix up to the next `/`, `?`, `&`, `#`, quote or whitespace, so a
  key that contains a `/` itself (such as `doc:/besluit/id`) only works as the whole value.

The `path` is resolved by Valtimo, which only supports literal text or a resolver key as the whole value.

#### Environment variables

Valtimo's importer fills in `${NAME}` and `${NAME:default}` in the process link when it is deployed, for names that
match `valtimo.import.whitelistedPaths` (default `valtimo.*`). Add your own pattern to use environment-specific values:

```yaml
valtimo:
  import:
    whitelistedPaths:
      - "valtimo.*"
      - "EXAMPLE_.*"
```

A placeholder whose name isn't whitelisted is left as is, and is then sent literally.

#### Error handling

The action fails, and with it the service task, when:

- a resolver key resolves to nothing (for example, the process variable doesn't exist);
- a header or body field is given more than once (nothing is sent);
- the server can't be reached;
- the response status isn't 2xx. The message contains the status and the start of the response body.

Set **Asynchronous before** on the service task, so a failure is retried by the job executor and then shows up as an
incident that can be retried from the process instance once the cause is fixed.

#### Example: notify an API of a besluit

Configuration: `baseUrl` = `https://api.example.com`.

Process link:

```json
{
    "method": "POST",
    "path": "/notifications",
    "body": [
        {"key": "hoofdObject", "value": "Aanvraag"},
        {"key": "resource", "value": "Aanvraag"},
        {"key": "resourceUrl", "value": "${EXAMPLE_RESOURCE_URL}/pv:besluitId"},
        {"key": "actie", "value": "Create"},
        {"key": "aanmaakdatum", "value": "pv:aanmaakdatum"}
    ],
    "resultVariable": "notificatieResultaat"
}
```

`EXAMPLE_RESOURCE_URL` (`https://example.com/resources`) is filled in on
deployment (see [Environment variables](#environment-variables)); `pv:besluitId` is resolved when the request is sent.
The resourceUrl can be on another host than the API, so it doesn't come from `baseUrl`.

The process sets `aanmaakdatum` beforehand, for example with an execution listener:

```xml
<camunda:executionListener event="end"
    expression="${execution.setVariable('aanmaakdatum', dateTime().toString('yyyy-MM-dd'))}"/>
```

This sends:

```json
{
    "hoofdObject": "Aanvraag",
    "resource": "Aanvraag",
    "resourceUrl": "https://example.com/resources/<besluitid>",
    "actie": "Create",
    "aanmaakdatum": "2026-10-28"
}
```

## Example application

The example case sends this notification to a local mock API. See
[Example Application](example-application.md) for how to run it and how to
[send it to another API](example-application.md#sending-to-another-api).
