# Example Application

This project also contains a working example application which is meant to showcase the plugin. Its example case
notifies an external API of a besluit, as described in the [plugin documentation](plugin.md#example-notify-an-api-of-a-besluit).

## Running the example application

All commands below should be run from the **project root** directory, unless stated otherwise.

### Prerequisites

- Java 21
- Node 20
- [Docker (Desktop)](https://www.docker.com/products/docker-desktop/)

### Start docker

Make sure docker is running.

Start with gradle script:

```shell
./gradlew :backend:app:composeUp
```

Besides Keycloak, the database and the ZGW services, this starts `web-request-mock` on port 8099. It stands in
for the Example API: it answers every request with HTTP 200 and logs what it received.

### Start backend

By gradle script:

```shell
./gradlew :backend:app:bootRun
```

### Start frontend

From the `frontend` directory:

```shell
nvm use 20
npm run clean
npm install
npm run build
npm start
```

### Keycloak users

The example application has a few test users that are preconfigured.

| Name         | Role           | Username  | Password  |
|--------------|----------------|-----------|-----------|
| James Vance  | ROLE_USER      | user      | user      |
| Asha Miller  | ROLE_ADMIN     | admin     | admin     |
| Morgan Finch | ROLE_DEVELOPER | developer | developer |

## Trying the plugin

On startup these are deployed automatically:

| What                                       | Where                                                                    |
|--------------------------------------------|--------------------------------------------------------------------------|
| Plugin configuration "Example API authentication" (HTTP Client Authentication) and "Example API" (Generic Web Request) | `backend/app/src/main/resources/config/plugin/generic-web-request.pluginconfig.json` |
| Process `example-process` with the service task "Notify of besluit" | `backend/app/src/main/resources/config/case/example/1-0-0/bpmn`  |
| The `send-request` action on that task     | `backend/app/src/main/resources/config/case/example/1-0-0/process-link` |

1. Log in as `admin` and start a new **Example** case.
2. Fill in a name and a **Besluit ID**. The besluit ID becomes the process variable `besluitId`.
3. The start event sets the process variable `aanmaakdatum` (today, `yyyy-MM-dd`). The process link sends
   `resourceUrl` as `${EXAMPLE_RESOURCE_URL}/pv:besluitId`: the importer fills in the environment variable on
   deployment (it is whitelisted with `valtimo.import.whitelistedPaths` in `application.yml`), and the plugin
   replaces `pv:besluitId` when it sends the request. `aanmaakdatum` is referred to as `pv:aanmaakdatum`;
   the other body fields are literal values.
4. The process sends the notification. Check what the mock received:

   ```shell
   docker logs web-request-mock
   ```

   The body has the format below, with your besluit ID and today's date:

   ```json
   {
       "hoofdObject": "Aanvraag",
       "resource": "Aanvraag",
       "resourceUrl": "https://example.com/resources/<besluitid>",
       "actie": "Create",
       "aanmaakdatum": "2026-10-28"
   }
   ```

5. The case's summary tab shows the besluit ID and the HTTP status the API answered with, and below it the
   response body. The end event copies them from the process variable `notificatieResultaat` (the action's
   `resultVariable`) to the case document as `notificatieStatus` and `notificatieResponse`. The response is shown in a
   read-only form (`form/notificatie-response.form.json`). When the response is JSON, the form shows it indented
   (with Form.io's `calculateValue`, in the browser only); any other response is shown as received.

The plugin configurations and the action can be viewed and changed in the admin menu, under **Plugins** and in the
process links of the example process.

### Sending to another API

The configurations read these environment variables. Set them in `.env.properties` in the project root, which
`bootRun` loads:

| Variable                     | Default                     | Description                                                                    |
|------------------------------|-----------------------------|--------------------------------------------------------------------------------|
| EXAMPLE_API_URL              | `http://localhost:8099/api` | Base URL; the action appends `/notifications`                                  |
| EXAMPLE_API_AUTH_TYPE        | `NONE`                      | Authentication type of HTTP Client Authentication, such as `HEADER` or `BASIC` |
| EXAMPLE_API_AUTH_HEADER_NAME | empty                       | Header name, for `HEADER`                                                      |
| EXAMPLE_API_AUTH_SECRET      | empty                       | The API key, password or token                                                 |
| EXAMPLE_RESOURCE_URL         | none, required              | Base of the `resourceUrl` in the body; the besluit ID is appended              |

For example, for an API that expects an API key header:

```properties
EXAMPLE_API_URL=https://api.example.com
EXAMPLE_API_AUTH_TYPE=HEADER
EXAMPLE_API_AUTH_HEADER_NAME=X-Api-Key
EXAMPLE_API_AUTH_SECRET=<api key>
```

Certificates (mutual TLS) and the other authentication types can be set on the authentication configuration in the
admin menu.

### When the request fails

When the API can't be reached or doesn't answer with 2xx, the service task fails. It runs asynchronously, so the
job executor retries it and then raises an incident on the process instance, with the HTTP status and the start of the
response in the message. Fix the cause and retry the incident.

To try this, stop the mock (`docker stop web-request-mock`) and start a case.

## Source code

The source code is split up into two modules:

1. [Frontend](/frontend)
2. [Backend](/backend)
