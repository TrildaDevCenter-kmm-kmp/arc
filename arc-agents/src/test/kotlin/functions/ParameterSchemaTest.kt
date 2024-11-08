// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.arc.agents.functions

import ai.ancf.lmos.arc.agents.TestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParameterSchemaTest : TestBase() {

    @Test
    fun `test toSchemaMap with string property`() {
        val parametersSchema = ParameterSchema(
            name = "test",
            description = "this is a test property",
            type = ParameterType(
                schemaType = "string",
            ),
            enum = emptyList(),
        )
        val schemaMap = parametersSchema.toSchemaMap()
        assertThat(schemaMap).isEqualTo(
            "test" to mapOf(
                "type" to "string",
                "description" to "this is a test property",
            ),
        )
    }
}