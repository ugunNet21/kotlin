/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.commonizer.metadata.utils

import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmModuleFragment
import kotlinx.metadata.KmTypeAlias
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlinx.metadata.klib.fqName
import org.jetbrains.kotlin.konan.file.File as KFile
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import java.io.File

@Suppress("SpellCheckingInspection")
fun main() {
    val baseDir1 = File("/Users/Dmitriy.Dolovov/temp/2-master")
    val baseDir2 = File("/Users/Dmitriy.Dolovov/temp/2-master-relaxed-talu-type-ref2-compact-coll")

    val klibPaths1 = baseDir1.listKlibs
    val klibPaths2 = baseDir2.listKlibs
    check(klibPaths1 == klibPaths2) {
        """
            Two sets of KLIB paths differ:
            $klibPaths1
            $klibPaths2
        """.trimIndent()
    }

    for (klibPath in klibPaths1) {
        doCompare(klibPath, baseDir1, baseDir2, false)
    }
}

private val File.listKlibs: Set<File>
    get() = walkTopDown()
        .filter { it.isDirectory && (it.name == "common" || it.parentFile.name == "platform") }
        .map { it.relativeTo(this) }
        .toSet()

private fun doCompare(klibPath: File, baseDir1: File, baseDir2: File, printMatches: Boolean) {
    println("BEGIN $klibPath")

    val libs1: Map<String, KFile> =
        baseDir1.resolve(klibPath).listFiles().orEmpty().groupBy { it.name }.mapValues { KFile(it.value.single().absolutePath) }
    val libs2: Map<String, KFile> =
        baseDir2.resolve(klibPath).listFiles().orEmpty().groupBy { it.name }.mapValues { KFile(it.value.single().absolutePath) }

    val allLibs: Set<String> = libs1.keys intersect libs2.keys
    check(allLibs == libs1.keys)
    check(allLibs == libs2.keys)

    val allLibs1Fragments = mutableMapOf<String, MutableList<KmModuleFragment>>()
    allLibs.map { libs1.getValue(it) }
        .map { resolveSingleFileKlib(it, strategy = ToolingSingleFileKlibResolveStrategy) }
        .map { KlibModuleMetadata.read(TrivialLibraryProvider(it)) }
        .flatMap { it.fragments }
        .forEach { allLibs1Fragments.getOrPut(it.fqName!!) { mutableListOf() } += it }

    fun resolveTypeAlias(fullName: String): KmTypeAlias {
        val shortName = fullName.substringAfterLast('/')
        val packageName = fullName.substringBefore(shortName).trimEnd('/').replace('/', '.')

        return allLibs1Fragments.getValue(packageName)
            .flatMap { it.pkg?.typeAliases.orEmpty() }
            .first { it.name == shortName }
    }

    for (lib in allLibs.sorted()) {
        val lib1 = libs1.getValue(lib)
        val lib2 = libs2.getValue(lib)

        val klib1 = resolveSingleFileKlib(lib1, strategy = ToolingSingleFileKlibResolveStrategy)
        val klib2 = resolveSingleFileKlib(lib2, strategy = ToolingSingleFileKlibResolveStrategy)

        val metadata1 = KlibModuleMetadata.read(TrivialLibraryProvider(klib1))
        val metadata2 = KlibModuleMetadata.read(TrivialLibraryProvider(klib2))

        when (val result = MetadataDeclarationsComparator().compare(metadata1, metadata2)) {
            Result.Success -> if (printMatches) println("- [full match] $lib")
            is Result.Failure -> {
                val groupedByPath: MutableMap<List<String>, MutableList<Mismatch>> = result.mismatches.groupByTo(mutableMapOf()) { it.path }

                groupedByPath.values.flatten().forEach { mismatch ->
                    if (mismatch !is Mismatch.DifferentValues) return@forEach
                    if (mismatch.kind != "Classifier" || mismatch.path.last() != "UnderlyingType") return@forEach
                    if ((mismatch.valueB as? KmClassifier.Class)?.name !in arrayOf(
                            "kotlinx/cinterop/CPointer",
                            "kotlin/Function0",
                            "kotlin/Function1",
                            "kotlin/Function2",
                            "kotlin/Function3"
                        )
                    ) return@forEach

                    var fullTypeAliasName = (mismatch.valueA as? KmClassifier.TypeAlias)?.name ?: return@forEach

                    while (true) {
                        val typeAlias = resolveTypeAlias(fullTypeAliasName)

                        when (val underlyingTypeClassifier = typeAlias.underlyingType.classifier) {
                            is KmClassifier.Class -> {
                                if (underlyingTypeClassifier.name !in arrayOf(
                                        "kotlinx/cinterop/CPointer",
                                        "kotlin/Function0",
                                        "kotlin/Function1",
                                        "kotlin/Function2",
                                        "kotlin/Function3"
                                    )
                                ) {
                                    error("Unexpected class found: ${underlyingTypeClassifier.name}")
                                }
                            }
                            is KmClassifier.TypeAlias -> {
                                if (underlyingTypeClassifier.name !in arrayOf(
                                        "kotlinx/cinterop/CArrayPointer",
                                        "kotlinx/cinterop/COpaquePointer"
                                    )
                                ) {
                                    fullTypeAliasName = underlyingTypeClassifier.name
                                    continue
                                }
                            }
                        }

                        val relevantMismatches = groupedByPath.getValue(mismatch.path)
                        relevantMismatches -= mismatch
                        relevantMismatches.removeIf { it is Mismatch.MissingEntity && it.kind == "TypeProjection" }
                        break
                    }
                }

                groupedByPath.values.flatten().forEach { mismatch ->
                    if (mismatch !is Mismatch.DifferentValues) return@forEach
                    if (mismatch.kind != "Flag" || mismatch.name != "IS_NULLABLE") return@forEach
                    if (mismatch.path.last() !in arrayOf("UnderlyingType", "ExpandedType")) return@forEach
                    if (mismatch.valueA.toString() != "false" || mismatch.valueB.toString() != "true") return

                    groupedByPath.getValue(mismatch.path) -= mismatch
                }

                if ("linux_x64" in klibPath.path) {
                    groupedByPath.values.flatten().forEach { mismatch ->
                        if (mismatch !is Mismatch.MissingEntity) return@forEach
                        if (mismatch.kind != "TypeAlias") return@forEach
                        val options = listOf("caddr_t", "sig_t", "va_list")
                        if (mismatch.name !in options && mismatch.name.removeSuffix("Var") !in options) return@forEach

                        groupedByPath.getValue(mismatch.path) -= mismatch
                    }

                    groupedByPath.values.flatten().forEach { mismatch ->
                        if (mismatch !is Mismatch.MissingEntity) return@forEach
                        if (mismatch.kind != "Function") return@forEach
                        val function = mismatch.presentValue as? KmFunction ?: return@forEach
                        if (function.valueParameters.any {
                                (it.type?.classifier as? KmClassifier.Class)?.name == "kotlinx/cinterop/CPointer"
                                        && (it.type?.arguments?.singleOrNull()?.type?.classifier as? KmClassifier.Class)?.name?.endsWith("va_list_tag") == true
                            }) {
                            groupedByPath.getValue(mismatch.path) -= mismatch
                        }
                    }
                }

                groupedByPath.entries.removeIf { (_, values) -> values.isEmpty() }

                if (groupedByPath.isNotEmpty()) {
                    println("- [MISMATCHES] $lib")
                    groupedByPath.values.flatten().forEachIndexed { index, mismatch ->
                        println("  ${index + 1}. $mismatch")
                    }
                } else if (printMatches) println("- [match] $lib")
            }
        }
    }
    println("END $klibPath\n")
}
