/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir.handlers

import org.jetbrains.kotlin.codeMetaInfo.model.CodeMetaInfo
import org.jetbrains.kotlin.codeMetaInfo.renderConfigurations.AbstractCodeMetaInfoRenderConfiguration
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDefaultErrorMessages
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnostic

object FirMetaInfoUtils {
    val renderDiagnosticNoArgs = FirDiagnosticCodeMetaRenderConfiguration().apply { renderParams = false }
    val renderDiagnosticWithArgs = FirDiagnosticCodeMetaRenderConfiguration().apply { renderParams = true }
}

class FirDiagnosticCodeMetaInfo(
    val diagnostic: FirDiagnostic<*>,
    renderConfiguration: FirDiagnosticCodeMetaRenderConfiguration
) : CodeMetaInfo {
    override var renderConfiguration: FirDiagnosticCodeMetaRenderConfiguration = renderConfiguration
        private set

    override val start: Int
        get() = diagnostic.element.startOffset
    override val end: Int
        get() = diagnostic.element.endOffset

    override val tag: String
        get() = renderConfiguration.getTag(this)

    override val attributes: MutableList<String> = mutableListOf()

    override fun asString(): String = renderConfiguration.asString(this)

    fun replaceRenderConfiguration(renderConfiguration: FirDiagnosticCodeMetaRenderConfiguration) {
        this.renderConfiguration = renderConfiguration
    }
}

class FirDiagnosticCodeMetaRenderConfiguration(
    val renderSeverity: Boolean = false,
) : AbstractCodeMetaInfoRenderConfiguration(renderParams = false) {
    private val crossPlatformLineBreak = """\r?\n""".toRegex()

    override fun asString(codeMetaInfo: CodeMetaInfo): String {
        if (codeMetaInfo !is FirDiagnosticCodeMetaInfo) return ""
        return (getTag(codeMetaInfo)
                + getPlatformsString(codeMetaInfo)
                + getParamsString(codeMetaInfo))
            .replace(crossPlatformLineBreak, "")
    }

    private fun getParamsString(codeMetaInfo: FirDiagnosticCodeMetaInfo): String {
        if (!renderParams) return ""
        val params = mutableListOf<String>()

        val diagnostic = codeMetaInfo.diagnostic

        val renderer = FirDefaultErrorMessages.getRendererForDiagnostic(diagnostic)
        if (renderer != null) {
            params.add(renderer.render(diagnostic))
        }

        if (renderSeverity)
            params.add("severity='${diagnostic.severity}'")

        params.add(getAdditionalParams(codeMetaInfo))

        return "(\"${params.filter { it.isNotEmpty() }.joinToString("; ")}\")"
    }

    fun getTag(codeMetaInfo: FirDiagnosticCodeMetaInfo): String {
        return codeMetaInfo.diagnostic.factory.name
    }
}
