/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.types.checker

import org.jetbrains.kotlin.resolve.constants.IntegerLiteralTypeConstructor
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.SimpleTypeMarker
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.refinement.TypeRefinement

open class ClassicTypeCheckerContext(
    val errorTypeEqualsToAnything: Boolean,
    val stubTypeEqualsToAnything: Boolean = true,
    val allowedTypeVariable: Boolean = true,
    val kotlinTypeRefiner: KotlinTypeRefiner = KotlinTypeRefiner.Default
) : ClassicTypeSystemContext, AbstractTypeCheckerContext() {

    override fun prepareType(type: KotlinTypeMarker): KotlinTypeMarker {
        require(type is KotlinType, type::errorMessage)
        return NewKotlinTypeChecker.Default.transformToNewType(type.unwrap())
    }

    @OptIn(TypeRefinement::class)
    override fun refineType(type: KotlinTypeMarker): KotlinTypeMarker {
        require(type is KotlinType, type::errorMessage)
        return kotlinTypeRefiner.refineType(type)
    }

    override val isErrorTypeEqualsToAnything: Boolean
        get() = errorTypeEqualsToAnything

    override val isStubTypeEqualsToAnything: Boolean
        get() = stubTypeEqualsToAnything

    override fun areEqualTypeConstructors(c1: TypeConstructorMarker, c2: TypeConstructorMarker): Boolean {
        require(c1 is TypeConstructor, c1::errorMessage)
        require(c2 is TypeConstructor, c2::errorMessage)
        return areEqualTypeConstructors(c1, c2)
    }

    open fun areEqualTypeConstructors(a: TypeConstructor, b: TypeConstructor): Boolean = when {
        /*
         * For integer literal types we have special rules for constructor's equality,
         *   so we have to check it manually
         * For example: Int in ILT.possibleTypes -> ILT == Int
         */
        a is IntegerLiteralTypeConstructor -> a.checkConstructor(b)
        b is IntegerLiteralTypeConstructor -> b.checkConstructor(a)
        else -> a == b
    }

    override fun substitutionSupertypePolicy(type: SimpleTypeMarker): SupertypesPolicy.DoCustomTransform {
        return classicSubstitutionSupertypePolicy(type)
    }

    override val KotlinTypeMarker.isAllowedTypeVariable: Boolean get() = this is UnwrappedType && allowedTypeVariable && constructor is NewTypeVariableConstructor

    companion object {
        fun ClassicTypeSystemContext.classicSubstitutionSupertypePolicy(type: SimpleTypeMarker): SupertypesPolicy.DoCustomTransform {
            require(type is SimpleType, type::errorMessage)
            val substitutor = TypeConstructorSubstitution.create(type).buildSubstitutor()

            return object : SupertypesPolicy.DoCustomTransform() {
                override fun transformType(context: AbstractTypeCheckerContext, type: KotlinTypeMarker): SimpleTypeMarker {
                    return substitutor.safeSubstitute(
                        type.lowerBoundIfFlexible() as KotlinType,
                        Variance.INVARIANT
                    ).asSimpleType()!!
                }
            }
        }
    }
}

private fun Any.errorMessage(): String {
    return "ClassicTypeCheckerContext couldn't handle ${this::class} $this"
}
