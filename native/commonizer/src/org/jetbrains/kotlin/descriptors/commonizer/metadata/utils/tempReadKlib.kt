/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.commonizer.metadata.utils

import kotlinx.metadata.klib.fqName
import java.io.File

fun main() {
    val klib = File("/Users/Dmitriy.Dolovov/IdeaProjects/nativeLib/build/classes/kotlin/native/main/nativeLib.klib")
    val metadata = TrivialLibraryProvider.readLibraryMetadata(klib)

    val fragments = metadata.fragments.filter { it.fqName == "sample" }
    val classes = fragments.flatMap { it.classes }.associateBy { it.name }
    val typeAliases = fragments.flatMap { it.pkg?.typeAliases.orEmpty() }.associateBy { it.name }
    val properties = fragments.flatMap { it.pkg?.properties.orEmpty() }.associateBy { it.name }
    val functions = fragments.flatMap { it.pkg?.functions.orEmpty() }.associateBy { it.name }

    println("Done.")
}
