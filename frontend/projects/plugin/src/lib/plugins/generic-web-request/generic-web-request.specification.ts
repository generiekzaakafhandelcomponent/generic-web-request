/*
 * Copyright 2026 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {PluginSpecification} from "@valtimo/plugin";
import {GenericWebRequestConfigurationComponent} from "./components/generic-web-request-configuration/generic-web-request-configuration.component";
import {SendRequestConfigurationComponent} from "./components/send-request-configuration/send-request-configuration.component";
import {GENERIC_WEB_REQUEST_LOGO_BASE64} from "./assets";

const genericWebRequestPluginSpecification: PluginSpecification = {
  pluginId: "generic-web-request",
  pluginConfigurationComponent: GenericWebRequestConfigurationComponent,
  pluginLogoBase64: GENERIC_WEB_REQUEST_LOGO_BASE64,
  functionConfigurationComponents: {
    "send-request": SendRequestConfigurationComponent,
  },
  pluginTranslations: {
    nl: {
      title: "Generiek webrequest",
      description: "Verstuurt een configureerbaar HTTP-verzoek naar een extern systeem.",
      "send-request": "Verzoek versturen",
      configurationTitle: "Configuratienaam",
      configurationTitleTooltip: "Een herkenbare naam voor deze configuratie, bijvoorbeeld de naam van het externe systeem.",
      baseUrl: "Basis-URL",
      baseUrlTooltip: "De URL van het externe systeem. Het pad van de actie wordt hierachter geplakt.",
      authenticationPluginConfiguration: "Authenticatie",
      authenticationPluginConfigurationTooltip:
        "Een configuratie van de HTTP Client Authentication-plugin. Kies daar 'NONE' als het systeem geen authenticatie vraagt.",
      actionDescription:
        "Verstuurt een HTTP-verzoek naar het geconfigureerde systeem, met de bodyvelden als JSON-object. " +
        "Elke waarde is letterlijke tekst of een verwijzing als pv:besluitId, doc:/pad of case:veld; " +
        "een verwijzing kan ook in een tekst staan, zoals https://example.com/pv:id. Als het systeem niet met 2xx antwoordt, faalt de taak.",
      method: "HTTP-methode",
      path: "Pad",
      pathTooltip: "Wordt achter de basis-URL geplakt, bijvoorbeeld /notifications.",
      headers: "Headers",
      headersTooltip: "Extra headers. Authenticatieheaders komen uit de authenticatieconfiguratie.",
      headerName: "Naam",
      headerValue: "Waarde",
      body: "Body",
      bodyTooltip:
        "De velden van het JSON-object dat verstuurd wordt. Een verwijzing (pv:, doc:, case:) behoudt het type " +
        "van de waarde, zoals een getal of lijst.",
      fieldName: "Veld",
      fieldValue: "Waarde of verwijzing",
      resultVariable: "Procesvariabele voor resultaat",
      resultVariableTooltip: "Optioneel. Slaat {statusCode, body} van het antwoord op onder deze naam.",
    },
    en: {
      title: "Generic Web Request",
      description: "Sends a configurable HTTP request to an external system.",
      "send-request": "Send request",
      configurationTitle: "Configuration name",
      configurationTitleTooltip: "A recognisable name for this configuration, such as the name of the external system.",
      baseUrl: "Base URL",
      baseUrlTooltip: "The URL of the external system. The path of the action is appended to it.",
      authenticationPluginConfiguration: "Authentication",
      authenticationPluginConfigurationTooltip:
        "A configuration of the HTTP Client Authentication plugin. Choose 'NONE' there when the system needs no authentication.",
      actionDescription:
        "Sends an HTTP request to the configured system, with the body fields as a JSON object. " +
        "Each value is literal text or a reference such as pv:besluitId, doc:/path or case:field; " +
        "references can also be part of a text, such as https://example.com/pv:id. The task fails when the system doesn't answer with 2xx.",
      method: "HTTP method",
      path: "Path",
      pathTooltip: "Appended to the base URL, for example /notifications.",
      headers: "Headers",
      headersTooltip: "Additional headers. Authentication headers come from the authentication configuration.",
      headerName: "Name",
      headerValue: "Value",
      body: "Body",
      bodyTooltip:
        "The fields of the JSON object that is sent. A reference (pv:, doc:, case:) keeps the type of its value, " +
        "such as a number or a list.",
      fieldName: "Field",
      fieldValue: "Value or reference",
      resultVariable: "Process variable for result",
      resultVariableTooltip: "Optional. Stores the response's {statusCode, body} under this name.",
    },
  },
};

export {genericWebRequestPluginSpecification};
