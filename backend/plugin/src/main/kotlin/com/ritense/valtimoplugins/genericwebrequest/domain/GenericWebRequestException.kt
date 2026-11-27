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

package com.ritense.valtimoplugins.genericwebrequest.domain

/**
 * Thrown when a request can't be built or isn't answered with a 2xx status. Thrown from a service task, it rolls back
 * the transaction; with 'asynchronous before' on the task, Operaton retries it and then raises an incident.
 */
open class GenericWebRequestException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class GenericWebRequestResponseException(
    method: RequestMethod,
    uri: String,
    val statusCode: Int,
    val responseBody: String?,
) : GenericWebRequestException(
        "$method $uri was answered with HTTP $statusCode" +
            (responseBody?.takeIf { it.isNotBlank() }?.let { ": ${it.take(MAX_BODY_IN_MESSAGE)}" } ?: ""),
    ) {
    companion object {
        const val MAX_BODY_IN_MESSAGE = 500
    }
}
