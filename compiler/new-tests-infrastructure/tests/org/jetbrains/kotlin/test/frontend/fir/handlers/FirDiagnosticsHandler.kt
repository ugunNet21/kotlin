/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir.handlers

import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnostic
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.DirectivesContainer
import org.jetbrains.kotlin.test.frontend.fir.FirSourceArtifact
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.*

class FirDiagnosticsHandler(testServices: TestServices) : FirAnalysisHandler(testServices) {
    private val globalMetadataInfoHandler: GlobalMetadataInfoHandler
        get() = testServices.globalMetadataInfoHandler

    private val diagnosticsService: DiagnosticsService
        get() = testServices.diagnosticsService

    override val directivesContainers: List<DirectivesContainer> =
        listOf(DiagnosticsDirectives)

    override val additionalServices: List<ServiceRegistrationData> =
        listOf(service(::DiagnosticsService))

    override fun processModule(module: TestModule, info: FirSourceArtifact) {
        val diagnosticsPerFile = info.firAnalyzerFacade.runCheckers()

        for (file in module.files) {
            val ktFile = info.firFiles[file] ?: continue
            val diagnostics = diagnosticsPerFile[ktFile] ?: continue
            val diagnosticsMetadataInfos = diagnostics.mapNotNull { diagnostic ->
                if (!diagnosticsService.shouldRenderDiagnostic(module, diagnostic.factory.name)) return@mapNotNull null
                diagnostic.toMetaInfo(file)
            }
            globalMetadataInfoHandler.addMetadataInfosForFile(file, diagnosticsMetadataInfos)
        }
    }

    private fun FirDiagnostic<*>.toMetaInfo(file: TestFile): FirDiagnosticCodeMetaInfo {
        val metaInfo = FirDiagnosticCodeMetaInfo(this, FirMetaInfoUtils.renderDiagnosticNoArgs)
        val existing = globalMetadataInfoHandler.getExistingMetaInfosForActualMetadata(file, metaInfo)
        if (existing.any { it.description != null }) {
            metaInfo.replaceRenderConfiguration(FirMetaInfoUtils.renderDiagnosticWithArgs)
        }
        return metaInfo
    }

    override fun processAfterAllModules() {}
}
