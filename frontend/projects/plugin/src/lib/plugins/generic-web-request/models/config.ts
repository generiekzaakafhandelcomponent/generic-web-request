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

import {PluginConfigurationData} from "@valtimo/plugin";

type RequestMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

/** A header or body field. The value is a literal, or a value resolver key such as pv:besluitId. */
interface KeyValue {
  key: string;
  value: string;
}

interface GenericWebRequestConfig extends PluginConfigurationData {
  baseUrl: string;
  authenticationPluginConfiguration: string;
}

interface SendRequestConfig {
  method: RequestMethod;
  path?: string;
  headers?: KeyValue[];
  body?: KeyValue[];
  resultVariable?: string;
}

export {RequestMethod, KeyValue, GenericWebRequestConfig, SendRequestConfig};
