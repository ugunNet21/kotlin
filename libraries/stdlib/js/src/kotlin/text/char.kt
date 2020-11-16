/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

// actually \s is enough to match all whitespace, but \xA0 added because of different regexp behavior of Rhino used in Selenium tests
public actual fun Char.isWhitespace(): Boolean = toString().matches("[\\s\\xA0]")

/**
 * Converts this character to lower case using Unicode mapping rules of the invariant locale.
 *
 * This function performs one-to-one character mapping using case mapping information from the UnicodeData file,
 * to support one-to-many character mapping use the [lowercase] function.
 * If this character has no mapping equivalent, the character itself is returned.
 *
 * @sample samples.text.Chars.lowercase
 */
@SinceKotlin("1.4")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public actual inline fun Char.lowercaseChar(): Char = js("String.fromCharCode")(toInt()).toLowerCase().charCodeAt(0).unsafeCast<Int>().toChar()

/**
 * Converts this character to lower case using Unicode mapping rules of the invariant locale.
 *
 * If this character has no mapping equivalent, a [String] equal to [Char.toString] is returned.
 *
 * @sample samples.text.Chars.lowercase
 */
@SinceKotlin("1.4")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public actual inline fun Char.lowercase(): String = js("String.fromCharCode")(toInt()).toLowerCase() as String

/**
 * Converts this character to upper case using Unicode mapping rules of the invariant locale.
 *
 * This function performs one-to-one character mapping using case mapping information from the UnicodeData file,
 * to support one-to-many character mapping use the [uppercase] function.
 * If this character has no mapping equivalent, the character itself is returned.
 *
 * @sample samples.text.Chars.uppercase
 */
@SinceKotlin("1.4")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public actual inline fun Char.uppercaseChar(): Char = js("String.fromCharCode")(toInt()).toUpperCase().charCodeAt(0).unsafeCast<Int>().toChar()

/**
 * Converts this character to upper case using Unicode mapping rules of the invariant locale.
 *
 * If this character has no mapping equivalent, a [String] equal to [Char.toString] is returned.
 *
 * @sample samples.text.Chars.uppercase
 */
@SinceKotlin("1.4")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public actual inline fun Char.uppercase(): String = js("String.fromCharCode")(toInt()).toUpperCase() as String

/**
 * Returns `true` if this character is a Unicode high-surrogate code unit (also known as leading-surrogate code unit).
 */
public actual fun Char.isHighSurrogate(): Boolean = this in Char.MIN_HIGH_SURROGATE..Char.MAX_HIGH_SURROGATE

/**
 * Returns `true` if this character is a Unicode low-surrogate code unit (also known as trailing-surrogate code unit).
 */
public actual fun Char.isLowSurrogate(): Boolean = this in Char.MIN_LOW_SURROGATE..Char.MAX_LOW_SURROGATE
