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

import com.ritense.plugin.service.PluginService
import com.ritense.valtimoplugins.genericwebrequest.BaseIntegrationTest
import com.ritense.valtimoplugins.httpclientauthentication.HttpClientAuthenticator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@Transactional
internal class GenericWebRequestPluginDeploymentIT : BaseIntegrationTest() {
    @Autowired
    lateinit var pluginService: PluginService

    @Test
    fun `deploys the plugin with its properties and action`() {
        val definition = pluginService.getPluginDefinitions().single { it.key == GenericWebRequestPlugin.PLUGIN_KEY }

        val properties = definition.properties.associateBy { it.id.key }
        assertEquals(setOf("baseUrl", "authenticationPluginConfiguration"), properties.keys)
        assertEquals(
            HttpClientAuthenticator::class.java.name,
            properties.getValue("authenticationPluginConfiguration").fieldType,
        )
        assertEquals(
            listOf("send-request"),
            pluginService.getPluginDefinitionActions(GenericWebRequestPlugin.PLUGIN_KEY, null).map { it.key },
        )
    }
}
