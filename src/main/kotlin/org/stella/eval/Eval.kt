package org.stella.eval

import org.syntax.stella.Absyn.*

object Eval {
    @Throws(Exception::class)
    fun evalMainWith(program: Program?, inputExpr: Expr): Expr {
        println("interpreter is not implemented!")
        return inputExpr
    }
}