// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.arc.graphql

import com.expediagroup.graphql.server.spring.GraphQLAutoConfiguration
import dev.openfeature.sdk.FeatureProvider
import features.FeatureAgentResolver
import org.eclipse.lmos.arc.agents.AgentProvider
import org.eclipse.lmos.arc.agents.dsl.BeanProvider
import org.eclipse.lmos.arc.agents.features.FeatureFlags
import org.eclipse.lmos.arc.agents.functions.LLMFunctionProvider
import org.eclipse.lmos.arc.graphql.features.OpenFeatureFlags
import org.eclipse.lmos.arc.graphql.inbound.AccessControlHeaders
import org.eclipse.lmos.arc.graphql.inbound.AgentQuery
import org.eclipse.lmos.arc.graphql.inbound.AgentSubscription
import org.eclipse.lmos.arc.graphql.inbound.ToolMutation
import org.eclipse.lmos.arc.graphql.inbound.ToolQuery
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

@AutoConfiguration
@AutoConfigureBefore(GraphQLAutoConfiguration::class)
@Import(EventsConfiguration::class)
@PropertySource("classpath:arc.properties")
open class AgentGraphQLAutoConfiguration {

    @Bean
    fun agentQuery(agentProvider: AgentProvider) = AgentQuery(agentProvider)

    @Bean
    @ConditionalOnProperty("arc.tools.query.enabled", havingValue = "true")
    fun toolQuery(functionProvider: LLMFunctionProvider) = ToolQuery(functionProvider)

    @Bean
    @ConditionalOnBean(FeatureProvider::class)
    fun openFeatureFlags(featureProvider: FeatureProvider): FeatureFlags = OpenFeatureFlags(featureProvider)

    @Bean
    @ConditionalOnMissingBean(AgentResolver::class)
    @ConditionalOnBean(FeatureFlags::class)
    fun featureAgentResolver(
        features: FeatureFlags,
        agentProvider: AgentProvider,
    ) = FeatureAgentResolver(features, agentProvider)

    @Bean
    @ConditionalOnProperty("arc.tools.mutation.enabled", havingValue = "true")
    fun toolMutation(functionProvider: LLMFunctionProvider, beans: BeanProvider) = ToolMutation(functionProvider, beans)

    @Bean
    fun injectToolsFromRequest(functionProvider: LLMFunctionProvider) = InjectToolsFromRequest(functionProvider)

    @Bean
    fun agentSubscription(
        agentProvider: AgentProvider,
        errorHandler: ErrorHandler? = null,
        contextHandlers: List<ContextHandler>? = null,
        agentResolver: AgentResolver? = null,
        @Value("\${arc.agent.handover.limit:20}") agentHandoverRecursionLimit: Int,
    ) = AgentSubscription(
        agentProvider,
        errorHandler,
        contextHandlers ?: emptyList(),
        agentResolver,
        agentHandoverRecursionLimit,
    )

    @Bean
    @ConditionalOnProperty("arc.cors.enabled", havingValue = "true")
    fun accessControlHeaders(
        @Value("\${arc.cors.allow-origin:*}") allowOrigin: String,
        @Value("\${arc.cors.allow-methods:POST}") allowMethods: String,
        @Value("\${arc.cors.allow-headers:Content-Type}") allowHeaders: String,
    ) = AccessControlHeaders(allowOrigin, allowMethods, allowHeaders)
}
