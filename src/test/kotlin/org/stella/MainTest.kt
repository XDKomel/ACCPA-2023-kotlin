package org.stella

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.io.FileInputStream
import java.io.IOException

const val baseDir = "src/test/resources"
const val wellTypedDir = "$baseDir/well-typed"
const val illTypedDir = "$baseDir/ill-typed"

internal class MainTest {
    @ParameterizedTest(name = "{index} Typechecking well-typed program {0}")
    @ValueSource(strings = [
        "$wellTypedDir/factorial.stella",
        "$wellTypedDir/squares.stella",
        "$wellTypedDir/increment_twice.stella",
        "$wellTypedDir/logical-operators.stella"
    ])
    @Throws(
        IOException::class,
        Exception::class
    )
    fun testWellTyped(filepath: String) {
        val original = System.`in`
        val fips = FileInputStream(File(filepath))
        System.setIn(fips)
        main()
        System.setIn(original)
    }

    @ParameterizedTest(name = "{index} Typechecking ill-typed program {0}")
    @ValueSource(strings = [
        "$illTypedDir/bad-squares-1.stella",
        "$illTypedDir/bad-squares-2.stella"
    ])
    @Throws(
        IOException::class,
        Exception::class
    )
    fun testIllTyped(filepath: String) {
        val original = System.`in`
        val fips = FileInputStream(File(filepath))
        System.setIn(fips)
        val exception = assertThrows<Exception> {
            main()
            error("expected the typechecker to fail!")
        }
        // TODO: check that there is a type error actually, and not a problem with implementation
        System.setIn(original)
    }
}