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

package com.ritense.valtimoplugins.genericwebrequest.value

import com.ritense.valtimoplugins.genericwebrequest.domain.GenericWebRequestException
import com.ritense.valueresolver.ValueResolverService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.operaton.bpm.engine.delegate.VariableScope
import kotlin.test.assertEquals

class PropertyValueResolverTest {
    private val valueResolverService: ValueResolverService = mock()
    private val execution: DelegateExecution = mock()
    private val resolver = PropertyValueResolver(valueResolverService, setOf("http", "https"))

    @BeforeEach
    fun setUp() {
        whenever(execution.processInstanceId).thenReturn("process-instance-id")
        whenever(
            valueResolverService.getValueResolvers(),
        ).thenReturn(listOf("doc", "case", "task", "http", "https", "pv"))
    }

    @Test
    fun `resolves whole resolver keys keeping their type and keeps literals as they are`() {
        whenever(
            valueResolverService.resolveValues(
                eq("process-instance-id"),
                eq(execution),
                eq(setOf("pv:besluitId", "doc:/bedrag")),
            ),
        ).thenReturn(mapOf("pv:besluitId" to "b-1", "doc:/bedrag" to 12.5))

        val values =
            resolver.resolve(execution, listOf("pv:besluitId", "doc:/bedrag", "Aanvraag", "https://example.com"))

        assertEquals(
            mapOf(
                "pv:besluitId" to "b-1",
                "doc:/bedrag" to 12.5,
                "Aanvraag" to "Aanvraag",
                "https://example.com" to "https://example.com",
            ),
            values,
        )
    }

    @Test
    fun `resolves resolver keys inside a text`() {
        whenever(
            valueResolverService.resolveValues(
                eq("process-instance-id"),
                eq(execution),
                eq(setOf("pv:besluitId", "pv:versie")),
            ),
        ).thenReturn(mapOf("pv:besluitId" to "b-1", "pv:versie" to 2))

        val values =
            resolver.resolve(
                execution,
                listOf(
                    "https://example.com/OphalenData/pv:besluitId",
                    "https://example.com/pv:besluitId?versie=pv:versie&showcase:x",
                ),
            )

        assertEquals(
            mapOf(
                "https://example.com/OphalenData/pv:besluitId" to "https://example.com/OphalenData/b-1",
                "https://example.com/pv:besluitId?versie=pv:versie&showcase:x" to
                    "https://example.com/b-1?versie=2&showcase:x",
            ),
            values,
        )
    }

    @Test
    fun `fails on a resolver key inside a text that resolves to nothing`() {
        whenever(valueResolverService.resolveValues(any<String>(), any<VariableScope>(), any<Collection<String>>()))
            .thenReturn(mapOf("pv:besluitId" to null))

        val exception =
            assertThrows<GenericWebRequestException> {
                resolver.resolve(execution, listOf("https://example.com/pv:besluitId"))
            }

        assertEquals("Could not resolve the value(s) 'pv:besluitId'", exception.message)
    }

    @Test
    fun `fails on a resolver key that resolves to nothing`() {
        whenever(valueResolverService.resolveValues(any<String>(), any<VariableScope>(), any<Collection<String>>()))
            .thenReturn(mapOf("pv:besluitId" to null))

        val exception =
            assertThrows<GenericWebRequestException> { resolver.resolve(execution, listOf("pv:besluitId")) }

        assertEquals("Could not resolve the value(s) 'pv:besluitId'", exception.message)
    }

    @Test
    fun `does not call the value resolvers for literals only`() {
        assertEquals(mapOf("Create" to "Create"), resolver.resolve(execution, listOf("Create")))

        verify(valueResolverService, never())
            .resolveValues(any<String>(), any<VariableScope>(), any<Collection<String>>())
    }
}
