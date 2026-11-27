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

package com.ritense.valtimoplugins.genericwebrequest.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.valtimoplugins.genericwebrequest.client.GenericWebRequestClient
import com.ritense.valtimoplugins.genericwebrequest.domain.GenericWebRequestException
import com.ritense.valtimoplugins.genericwebrequest.domain.KeyValue
import com.ritense.valtimoplugins.genericwebrequest.domain.RequestMethod
import com.ritense.valtimoplugins.genericwebrequest.domain.WebRequest
import com.ritense.valtimoplugins.genericwebrequest.value.PropertyValueResolver
import com.ritense.valtimoplugins.httpclientauthentication.HttpClientAuthenticator
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * Sends an HTTP request to an external system. One configuration per system: the base URL and its authentication.
 * What to send (method, path, headers and body fields) is set per process link. Each header and body value is a
 * literal, a value resolver key such as `pv:besluitId`, or a text with resolver keys in it.
 */
@Plugin(
    key = GenericWebRequestPlugin.PLUGIN_KEY,
    title = "Generic Web Request",
    description = "Sends a configurable HTTP request to an external system",
)
open class GenericWebRequestPlugin(
    private val client: GenericWebRequestClient,
    private val propertyValueResolver: PropertyValueResolver,
    private val objectMapper: ObjectMapper,
) {
    @PluginProperty(key = "baseUrl", secret = false, required = true)
    lateinit var baseUrl: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false, required = true)
    lateinit var authenticationPluginConfiguration: HttpClientAuthenticator

    /**
     * Sends a request; the body fields are sent as a JSON object. Fails (and so stops the process) when a value can't
     * be resolved, a header or body field is given twice, or the response status isn't 2xx.
     */
    @PluginAction(
        key = "send-request",
        title = "Send request",
        description = "Sends an HTTP request to the configured system",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun sendRequest(
        execution: DelegateExecution,
        @PluginActionProperty method: RequestMethod,
        @PluginActionProperty path: String?,
        @PluginActionProperty headers: List<KeyValue>?,
        @PluginActionProperty body: List<KeyValue>?,
        @PluginActionProperty resultVariable: String?,
    ) {
        val headerFields = fields("header", headers)
        val bodyFields = fields("body field", body)
        val values =
            propertyValueResolver.resolve(
                execution,
                headerFields.map { it.value } + bodyFields.map { it.value },
            )

        val request =
            WebRequest(
                method = method,
                uri = buildUri(path),
                headers = headerFields.associate { it.key to values.getValue(it.value).toString() },
                body =
                    bodyFields
                        .takeIf { it.isNotEmpty() }
                        ?.associate { it.key to values.getValue(it.value) }
                        ?.let(objectMapper::writeValueAsString),
            )

        val response = client.send(request, authenticationPluginConfiguration)
        resultVariable?.takeIf { it.isNotBlank() }?.let { execution.setVariable(it.trim(), response.toVariable()) }
    }

    /** The rows with a key, trimmed. A key given twice is refused, rather than silently sending one of the values. */
    private fun fields(
        kind: String,
        rows: List<KeyValue>?,
    ): List<KeyValue> {
        val fields = rows.orEmpty().filter { it.key.isNotBlank() }.map { it.copy(key = it.key.trim()) }
        val duplicates = fields.groupBy { it.key.lowercase() }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw GenericWebRequestException(
                "The $kind(s) ${duplicates.joinToString { "'$it'" }} are given more than once",
            )
        }
        return fields
    }

    private fun buildUri(path: String?): URI {
        if (path.isNullOrBlank()) return baseUrl
        val base = baseUrl.toString().trimEnd('/')
        val relative = path.trim().trimStart('/')
        return UriComponentsBuilder
            .fromUriString("$base/$relative")
            .build()
            .encode()
            .toUri()
    }

    companion object {
        const val PLUGIN_KEY = "generic-web-request"
    }
}
