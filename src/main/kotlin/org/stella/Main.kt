package org.stella

import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.stella.eval.Eval
import org.stella.typecheck.TypeCheck
import org.syntax.stella.Absyn.Expr
import org.syntax.stella.Absyn.Program
import org.syntax.stella.PrettyPrinter
import org.syntax.stella.stellaLexer
import org.syntax.stella.stellaParser
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String> = emptyArray()) {
    val l: stellaLexer
    var p: stellaParser

    try {
        val input: Reader = if (args.isEmpty()) {
            InputStreamReader(System.`in`)
        } else  {
            FileReader(args[0])
        }
        l = stellaLexer(org.antlr.v4.runtime.ANTLRInputStream(input))
        l.addErrorListener(BNFCErrorListener())
    } catch (e: IOException) {
        System.err.println("Error: File not found: " + args[0])
        exitProcess(1)
    }
    p = stellaParser(org.antlr.v4.runtime.CommonTokenStream(l))
    p.addErrorListener(BNFCErrorListener())

//    val t = main(args)
    try {
        val ast: Program = p.start_Program().result
        val typechecker = TypeCheck
        val evaluator = Eval
        typechecker.typecheckProgram(ast)
        if (args.isNotEmpty()) {
            val l: stellaLexer = stellaLexer(org.antlr.v4.runtime.ANTLRInputStream(InputStreamReader(System.`in`)))
            l.addErrorListener(BNFCErrorListener())
            val p: stellaParser = stellaParser(org.antlr.v4.runtime.CommonTokenStream(l))
            p.addErrorListener(BNFCErrorListener())
            val ec: stellaParser.Start_ExprContext = p.start_Expr()
            val inputExpr: Expr = ec.result
            val resultExpr: Expr = evaluator.evalMainWith(ast, inputExpr)
            println(PrettyPrinter.print(resultExpr))
        }
    } catch (e: TestError) {
        System.err.println("At line " + e.line + ", column " + e.column + " :")
        System.err.println("     " + e.message)
        exitProcess(1)
    }
}

internal class TestError(msg: String?, var line: Int, var column: Int) : RuntimeException(msg)
internal class BNFCErrorListener : ANTLRErrorListener {
    override fun syntaxError(
        recognizer: org.antlr.v4.runtime.Recognizer<*, *>?,
        o: Any,
        i: Int,
        i1: Int,
        s: String,
        e: org.antlr.v4.runtime.RecognitionException
    ) {
        throw TestError(s, i, i1)
    }

    override fun reportAmbiguity(
        parser: org.antlr.v4.runtime.Parser,
        dfa: org.antlr.v4.runtime.dfa.DFA,
        i: Int,
        i1: Int,
        b: Boolean,
        bitSet: BitSet,
        atnConfigSet: ATNConfigSet
    ) {
        throw TestError("Ambiguity at", i, i1)
    }

    override fun reportAttemptingFullContext(
        parser: org.antlr.v4.runtime.Parser,
        dfa: org.antlr.v4.runtime.dfa.DFA,
        i: Int,
        i1: Int,
        bitSet: BitSet,
        atnConfigSet: ATNConfigSet
    ) {
        TODO()
    }

    override fun reportContextSensitivity(
        parser: org.antlr.v4.runtime.Parser,
        dfa: org.antlr.v4.runtime.dfa.DFA,
        i: Int,
        i1: Int,
        i2: Int,
        atnConfigSet: ATNConfigSet
    ) {
        TODO()
    }
}
