// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package io.github.lmos.arc.agents.events

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

/**
 * EventPublisher interface.
 */
fun interface EventPublisher {
    fun publish(event: Event)
}

/**
 * Add EventHandlers to the implementation of this event to receive events.
 */
fun interface EventListeners {
    fun add(handler: EventHandler<out Event>)
}

fun EventListeners.addAll(newHandlers: List<EventHandler<out Event>>) {
    newHandlers.forEach { add(it) }
}

/**
 * Basic EventPublisher.
 */
class BasicEventPublisher : EventPublisher, EventListeners {
    private val handlers = ConcurrentHashMap<Type, List<EventHandler<out Event>>>()

    override fun publish(event: Event) {
        handlers.forEach { (key, value) ->
            if ((key as Class<*>).isAssignableFrom(event::class.java)) {
                value.forEach { handler ->
                    handler::class.java.methods.first { it.name == handler::onEvent.name }.invoke(handler, event)
                }
            }
        }
    }

    override fun add(handler: EventHandler<out Event>) {
        val eventType = handler::class.java.genericInterfaces.first {
            it is ParameterizedType && it.rawType == EventHandler::class.java
        }.let { (it as ParameterizedType).actualTypeArguments[0] }
        val handlerList = handlers[eventType] ?: emptyList()
        handlers[eventType] = handlerList + handler
    }
}