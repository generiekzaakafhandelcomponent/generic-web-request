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
import org.operaton.bpm.engine.delegate.DelegateExecution

/**
 * Resolves header and body values:
 * - a value that is a value resolver key as a whole, such as `pv:besluitId`, is replaced by what it resolves to,
 *   keeping its type (a number stays a number);
 * - resolver keys inside a longer text, such as `https://example.com/OphalenData/pv:besluitId`, are replaced by
 *   their value as text. Such a key runs from its prefix up to the next `/`, `?`, `&`, `#`, quote or whitespace;
 * - any other text, such as `Aanvraag` or `https://example.com`, is used as-is.
 *
 * Valtimo resolves action properties itself, but only top-level ones: not the values inside the header and body lists.
 */
class PropertyValueResolver(
    private val valueResolverService: ValueResolverService,
    /** Prefixes of resolvers that return the value itself, such as `http` and `https`: text, not keys. */
    private val literalPrefixes: Set<String>,
) {
    /** Returns every value of [values] mapped to its resolved value. Fails when a resolver key resolves to nothing. */
    fun resolve(
        execution: DelegateExecution,
        values: Collection<String>,
    ): Map<String, Any> {
        // A URL is literal text here, so the resolver keys inside it are found
        val prefixes = valueResolverService.getValueResolvers().filterNot { it in literalPrefixes }
        if (prefixes.isEmpty()) {
            return values.associateWith { it }
        }
        val wholeKeys = values.filter { value -> prefixes.any { value.startsWith("$it:") } }.toSet()
        val embeddedKeyPattern = embeddedKeyPattern(prefixes)
        val embeddedKeys =
            values
                .filterNot { it in wholeKeys }
                .flatMap { value -> embeddedKeyPattern.findAll(value).map { it.value } }
                .toSet()
        val keys = wholeKeys + embeddedKeys

        val resolved =
            if (keys.isEmpty()) {
                emptyMap()
            } else {
                valueResolverService.resolveValues(execution.processInstanceId, execution, keys)
            }

        val unresolved = keys.filter { resolved[it] == null }
        if (unresolved.isNotEmpty()) {
            throw GenericWebRequestException(
                "Could not resolve the value(s) ${unresolved.sorted().joinToString { "'$it'" }}",
            )
        }
        return values.associateWith { value ->
            if (value in wholeKeys) {
                resolved.getValue(value)!!
            } else {
                embeddedKeyPattern.replace(value) { match -> resolved.getValue(match.value).toString() }
            }
        }
    }

    private fun embeddedKeyPattern(prefixes: Collection<String>): Regex {
        val prefixGroup = prefixes.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        return Regex("""(?<![A-Za-z0-9_])(?:$prefixGroup):[^/?&#"'\s]+""")
    }
}
