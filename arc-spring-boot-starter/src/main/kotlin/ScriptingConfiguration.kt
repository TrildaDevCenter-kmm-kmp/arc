// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.arc.spring

import ai.ancf.lmos.arc.agents.dsl.AgentFactory
import ai.ancf.lmos.arc.agents.dsl.BeanProvider
import ai.ancf.lmos.arc.agents.events.EventPublisher
import ai.ancf.lmos.arc.scripting.ScriptHotReload
import ai.ancf.lmos.arc.scripting.agents.AgentScriptEngine
import ai.ancf.lmos.arc.scripting.agents.CompiledAgentLoader
import ai.ancf.lmos.arc.scripting.agents.KtsAgentScriptEngine
import ai.ancf.lmos.arc.scripting.agents.ScriptingAgentLoader
import ai.ancf.lmos.arc.scripting.functions.FunctionScriptEngine
import ai.ancf.lmos.arc.scripting.functions.KtsFunctionScriptEngine
import ai.ancf.lmos.arc.scripting.functions.ScriptingLLMFunctionLoader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import java.io.File
import java.time.Duration
import kotlin.time.toKotlinDuration

@ConditionalOnProperty("arc.scripts.enabled", havingValue = "true", matchIfMissing = true)
class ScriptingConfiguration {

    @Bean
    fun functionScriptEngine(): FunctionScriptEngine = KtsFunctionScriptEngine()

    @Bean
    fun agentScriptEngine(): AgentScriptEngine = KtsAgentScriptEngine()

    @Bean
    @ConditionalOnProperty("arc.scripts.hotReload.enable", havingValue = "true")
    fun scriptHotReload(
        @Value("\${arc.scripts.folder:/agents}") agentsFolders: List<File>,
        @Value("\${arc.scripts.hotReload.delay:PT3M}") hotReloadDelay: Duration,
        agentLoader: ScriptingAgentLoader,
        functionLoader: ScriptingLLMFunctionLoader,
    ): ScriptHotReload {
        agentsFolders.forEach { if (!it.exists()) error("Agents folder does not exist: $it!") }
        return ScriptHotReload(agentLoader, functionLoader, hotReloadDelay.toKotlinDuration()).also { r ->
            agentsFolders.forEach { folder -> folder.walk().filter { it.isDirectory }.forEach { r.start(it) } }
        }
    }

    @Bean
    fun scriptingAgentLoader(
        agentScriptEngine: AgentScriptEngine,
        agentFactory: AgentFactory<*>,
        compiledAgents: List<CompiledAgentLoader>?,
        @Value("\${arc.scripts.folder:/agents}") agentsFolder: File,
        eventPublisher: EventPublisher,
    ): ScriptingAgentLoader {
        return ScriptingAgentLoader(agentFactory, agentScriptEngine, eventPublisher).also { loader ->
            compiledAgents?.forEach(loader::loadCompiledAgent)
            if (agentsFolder.exists()) agentsFolder.walk().filter { it.isFile }.forEach { loader.loadAgents(it) }
        }
    }

    @Bean
    fun scriptingLLMFunctionProvider(
        functionScriptEngine: FunctionScriptEngine,
        beanProvider: BeanProvider,
        @Value("\${arc.scripts.folder:/agents}") agentsFolder: File,
        eventPublisher: EventPublisher,
    ): ScriptingLLMFunctionLoader {
        return ScriptingLLMFunctionLoader(beanProvider, functionScriptEngine, eventPublisher).also { loader ->
            if (agentsFolder.exists()) agentsFolder.walk().filter { it.isFile }.forEach { loader.loadFunctions(it) }
        }
    }
}