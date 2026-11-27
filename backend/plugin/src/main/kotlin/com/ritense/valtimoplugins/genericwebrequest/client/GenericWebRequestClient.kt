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

package com.ritense.valtimoplugins.genericwebrequest.client

import com.ritense.valtimoplugins.genericwebrequest.domain.GenericWebRequestException
import com.ritense.valtimoplugins.genericwebrequest.domain.GenericWebRequestResponseException
import com.ritense.valtimoplugins.genericwebrequest.domain.WebRequest
import com.ritense.valtimoplugins.genericwebrequest.domain.WebResponse
import com.ritense.valtimoplugins.httpclientauthentication.HttpClientAuthenticator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

private val logger = KotlinLogging.logger {}

/**
 * Sends a [WebRequest], with its body as JSON, with the authentication (and certificates) of an [HttpClientAuthenticator].
 * Any status other than 2xx is thrown as a [GenericWebRequestResponseException].
 */
class GenericWebRequestClient(
    private val restClientBuilder: RestClient.Builder,
) {
    fun send(
        request: WebRequest,
        authentication: HttpClientAuthenticator,
    ): WebResponse {
        val builder = restClientBuilder.clone()
        val restClient = (authentication.applyAuth(builder) ?: builder).build()
        logger.info { "Sending ${request.method} ${request.uri}" }

        val response =
            try {
                restClient
                    .method(request.method.httpMethod)
                    .uri(request.uri)
                    .headers { headers -> request.headers.forEach { (name, value) -> headers.set(name, value) } }
                    .apply {
                        if (request.body != null) {
                            contentType(MediaType.APPLICATION_JSON)
                            body(request.body)
                        }
                    }.exchange { _, clientResponse ->
                        WebResponse(clientResponse.statusCode.value(), clientResponse.bodyTo(String::class.java))
                    }
            } catch (e: RestClientException) {
                throw GenericWebRequestException("${request.method} ${request.uri} could not be sent: ${e.message}", e)
            }

        logger.info { "${request.method} ${request.uri} was answered with HTTP ${response.statusCode}" }
        if (!HttpStatusCode.valueOf(response.statusCode).is2xxSuccessful) {
            throw GenericWebRequestResponseException(
                request.method,
                request.uri.toString(),
                response.statusCode,
                response.body,
            )
        }
        return response
    }
}
