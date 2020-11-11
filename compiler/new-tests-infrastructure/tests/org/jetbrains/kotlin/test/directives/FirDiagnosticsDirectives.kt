/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives

object FirDiagnosticsDirectives : SimpleDirectivesContainer() {
    val dumpCfg = directive(
        name = "DUMP_CFG",
        description = """
            Dumps control flow graphs of all declarations to `testName.dot` file
            This directive may be applied only to all modules
        """.trimIndent()
    )

    val dumpFir = directive(
        name = "FIR_DUMP",
        description = """
            Dumps resulting fir to `testName.fir` file
        """.trimIndent()
    )
}
