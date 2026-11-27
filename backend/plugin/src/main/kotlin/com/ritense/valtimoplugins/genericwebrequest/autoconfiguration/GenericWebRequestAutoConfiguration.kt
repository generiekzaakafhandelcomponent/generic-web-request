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

package com.ritense.valtimoplugins.genericwebrequest.autoconfiguration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.service.PluginService
import com.ritense.valtimoplugins.genericwebrequest.client.GenericWebRequestClient
import com.ritense.valtimoplugins.genericwebrequest.plugin.GenericWebRequestPluginFactory
import com.ritense.valtimoplugins.genericwebrequest.value.PropertyValueResolver
import com.ritense.valueresolver.FixedValueResolverFactory
import com.ritense.valueresolver.ValueResolverFactory
import com.ritense.valueresolver.ValueResolverService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@AutoConfiguration
class GenericWebRequestAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(GenericWebRequestClient::class)
    fun genericWebRequestClient(restClientBuilder: RestClient.Builder): GenericWebRequestClient =
        GenericWebRequestClient(restClientBuilder)

    @Bean
    @ConditionalOnMissingBean(PropertyValueResolver::class)
    fun genericWebRequestPropertyValueResolver(
        valueResolverService: ValueResolverService,
        valueResolverFactories: List<ValueResolverFactory>,
    ): PropertyValueResolver =
        PropertyValueResolver(
            valueResolverService,
            valueResolverFactories.filterIsInstance<FixedValueResolverFactory>().map { it.supportedPrefix() }.toSet(),
        )

    @Bean
    @ConditionalOnMissingBean(GenericWebRequestPluginFactory::class)
    fun genericWebRequestPluginFactory(
        pluginService: PluginService,
        client: GenericWebRequestClient,
        propertyValueResolver: PropertyValueResolver,
        objectMapper: ObjectMapper,
    ): GenericWebRequestPluginFactory =
        GenericWebRequestPluginFactory(pluginService, client, propertyValueResolver, objectMapper)
}
