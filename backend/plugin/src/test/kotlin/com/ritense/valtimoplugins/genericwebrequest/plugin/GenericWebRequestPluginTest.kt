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
import com.ritense.valtimoplugins.genericwebrequest.client.GenericWebRequestClient
import com.ritense.valtimoplugins.genericwebrequest.domain.GenericWebRequestException
import com.ritense.valtimoplugins.genericwebrequest.domain.GenericWebRequestResponseException
import com.ritense.valtimoplugins.genericwebrequest.domain.KeyValue
import com.ritense.valtimoplugins.genericwebrequest.domain.RequestMethod
import com.ritense.valtimoplugins.genericwebrequest.value.PropertyValueResolver
import com.ritense.valtimoplugins.httpclientauthentication.HttpClientAuthenticator
import com.ritense.valueresolver.ValueResolverService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.operaton.bpm.engine.delegate.VariableScope
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient
import java.net.URI
import javax.net.ssl.SSLContext
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GenericWebRequestPluginTest {
    private lateinit var server: MockWebServer
    private val objectMapper = ObjectMapper()
    private val valueResolverService: ValueResolverService = mock()
    private val execution: DelegateExecution = mock()
    private lateinit var plugin: GenericWebRequestPlugin

    @BeforeEach
    fun setUp() {
        server = MockWebServer().apply { start() }
        whenever(execution.processInstanceId).thenReturn("process-instance-id")
        whenever(
            valueResolverService.getValueResolvers(),
        ).thenReturn(listOf("doc", "case", "task", "http", "https", "pv"))
        whenever(valueResolverService.resolveValues(any<String>(), any<VariableScope>(), any<Collection<String>>()))
            .thenReturn(
                mapOf(
                    "pv:besluitId" to "b-123",
                    "pv:aanmaakdatum" to "2026-10-28",
                    "pv:correlationId" to "abc",
                ),
            )

        plugin =
            GenericWebRequestPlugin(
                GenericWebRequestClient(RestClient.builder()),
                PropertyValueResolver(valueResolverService, setOf("http", "https")),
                objectMapper,
            )
        plugin.baseUrl = URI(server.url("/api/").toString())
        plugin.authenticationPluginConfiguration = ApiKeyAuthenticator("X-Api-Key", "secret")
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sends the besluit notification to the gateway`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        sendNotification(resultVariable = "notificatieResultaat")

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/notifications", request.path)
        assertEquals("secret", request.getHeader("X-Api-Key"))
        assertEquals("abc", request.getHeader("X-Correlation-Id"))
        assertEquals("application/json", request.getHeader("Content-Type"))
        assertEquals(
            """{"hoofdObject":"Aanvraag","resource":"Aanvraag",""" +
                """"resourceUrl":"https://example.com/resources/b-123",""" +
                """"actie":"Create","aanmaakdatum":"2026-10-28"}""",
            request.body.readUtf8(),
        )
        verify(execution).setVariable("notificatieResultaat", mapOf("statusCode" to 200, "body" to """{"ok":true}"""))
    }

    @Test
    fun `keeps the type of a resolved value in the body`() {
        whenever(valueResolverService.resolveValues(any<String>(), any<VariableScope>(), any<Collection<String>>()))
            .thenReturn(mapOf("doc:/bedrag" to 12.5, "doc:/goedgekeurd" to true))
        server.enqueue(MockResponse().setResponseCode(201))

        plugin.sendRequest(
            execution,
            RequestMethod.PUT,
            "/items",
            null,
            listOf(KeyValue("bedrag", "doc:/bedrag"), KeyValue("goedgekeurd", "doc:/goedgekeurd")),
            null,
        )

        assertEquals("""{"bedrag":12.5,"goedgekeurd":true}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `throws on an error response and sets no result`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("gateway down"))

        val exception = assertThrows<GenericWebRequestResponseException> { sendNotification(resultVariable = "result") }

        assertEquals(500, exception.statusCode)
        assertEquals("gateway down", exception.responseBody)
        verify(execution, never()).setVariable(any(), anyOrNull())
    }

    @Test
    fun `throws when the server can't be reached`() {
        server.shutdown()

        assertThrows<GenericWebRequestException> { sendNotification() }
    }

    @Test
    fun `sends nothing when a body field is given twice`() {
        val body = listOf(KeyValue("actie", "Create"), KeyValue("actie", "Update"))

        assertThrows<GenericWebRequestException> {
            plugin.sendRequest(execution, RequestMethod.POST, "/notifications", null, body, null)
        }

        assertEquals(0, server.requestCount)
    }

    @Test
    fun `sends a GET request to the base URL without body`() {
        server.enqueue(MockResponse().setResponseCode(204))

        plugin.sendRequest(execution, RequestMethod.GET, null, null, null, "result")

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/", request.path)
        assertEquals(0, request.bodySize)
        assertNull(request.getHeader("Content-Type"))
        verify(execution).setVariable("result", mapOf("statusCode" to 204, "body" to null))
    }

    private fun sendNotification(resultVariable: String? = null) =
        plugin.sendRequest(
            execution = execution,
            method = RequestMethod.POST,
            path = "/notifications",
            headers = listOf(KeyValue("X-Correlation-Id", "pv:correlationId"), KeyValue("", "ignored")),
            body =
                listOf(
                    KeyValue("hoofdObject", "Aanvraag"),
                    KeyValue("resource", "Aanvraag"),
                    KeyValue(
                        "resourceUrl",
                        "https://example.com/resources/pv:besluitId",
                    ),
                    KeyValue("actie", "Create"),
                    KeyValue("aanmaakdatum", "pv:aanmaakdatum"),
                ),
            resultVariable = resultVariable,
        )

    /** Stands in for an http-client-authentication configuration of type HEADER. */
    private class ApiKeyAuthenticator(
        private val name: String,
        private val value: String,
    ) : HttpClientAuthenticator {
        override fun applyAuth(builder: RestClient.Builder): RestClient.Builder =
            builder.requestInitializer { it.headers.putAll(authenticationHeaders()) }

        override fun authenticationHeaders() = HttpHeaders().apply { set(name, value) }

        override fun sslContext(): SSLContext? = null
    }
}
