package org.stella.typecheck

import org.syntax.stella.Absyn.*
import org.syntax.stella.PrettyPrinter
import java.util.LinkedList

class EmptyDequeException: Exception()

class TypeBase<T> {
    private val base = HashMap<String, ArrayDeque<T>>()

    fun put(key: String, value: T) {
        if (!this.base.contains(key)) {
            this.base[key] = ArrayDeque()
        }
        this.base[key]?.addFirst(value)
    }

    fun take(key: String): T {
        if (!this.base.contains(key)) {
            throw NoSuchElementException()
        } else if (this.base[key]!!.isEmpty()) {
            throw EmptyDequeException()
        }
        return this.base[key]?.first()!!
    }

    fun remove(keys: List<String>) {
        for (key in keys) {
            this.base[key]?.removeFirst()
        }
    }
}

object TypeCheck {
    private val nat = TypeNat()
    private val bool = TypeBool()
    private val varTypeBase = TypeBase<Type>()
    private val fnTypeBase = TypeBase<TypeFun>()
    private fun constructRecNatExpr3(type: Type): TypeFun {
        return TypeFun(ListType().let {
            it.add(nat)
            it
        }, TypeFun(ListType().let {
            it.add(type)
            it
        }, type))
    }

    private fun unwrapReturnType(type: ReturnType, decl: Decl): Type = when (type) {
        is SomeReturnType -> type.type_
        else -> throw Exception("Met NoReturnType at \n${PrettyPrinter.print(decl)}\n")
    }


    private fun checkTypes(type1: Type, type2: Type, expr: Expr) {
        if (type1 != type2) {
            throw Exception("İncorrect type for the \n${PrettyPrinter.print(expr)}\nGot $type1 and $type2.")
        }
    }

    private fun getVarType(expr: Var): Type {
        try {
            return varTypeBase.take(expr.stellaident_)
        } catch (e: NoSuchElementException) {
            throw Exception("Refer to an unknown variable ${expr.stellaident_} at \n${PrettyPrinter.print(expr)}\n")
        } catch (e: EmptyDequeException) {
            throw Exception("Refer to a variable ${expr.stellaident_} from a local scope that is currently unavailable at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getSuccType(expr: Succ): Type {
        checkTypes(getExprType(expr.expr_), nat, expr)
        return nat
    }

    private fun getIfType(expr: If): Type {
        checkTypes(getExprType(expr.expr_1), bool, expr)
        checkTypes(getExprType(expr.expr_2), getExprType(expr.expr_3), expr)
        return getExprType(expr.expr_3)
    }

    private fun getNatRecType(expr: NatRec): Type {
        checkTypes(getExprType(expr.expr_1), nat, expr)
        val t = getExprType(expr.expr_2)
        checkTypes(getExprType(expr.expr_3), constructRecNatExpr3(t), expr)
        return t
    }

    private fun getIsZeroType(expr: IsZero): Type {
        checkTypes(getExprType(expr.expr_), nat, expr)
        return bool
    }

    private fun getExprType(expr: Expr): Type = when (expr) {
        is Var -> getVarType(expr)
        is ConstTrue -> bool
        is ConstFalse -> bool
        is ConstInt -> nat
        is Succ -> getSuccType(expr)
        is If -> getIfType(expr)
        is NatRec -> getNatRecType(expr)
        is IsZero -> getIsZeroType(expr)
        is Abstraction -> getAndCheckTypeFun(expr.listparamdecl_, null, expr.expr_)
        is Application -> checkApplication(expr)
        else -> throw Exception("Unknown type at \n${PrettyPrinter.print(expr)}\n")
    }

    private fun checkApplication(expr: Expr): Type {
        return when (expr) {
            is Application -> {
                when (val typeFun = checkApplication(expr.expr_)) {
                    is TypeFun -> {
                        for (p in typeFun.listtype_.zip(expr.listexpr_)) {
                            checkTypes(getExprType(p.second), p.first, expr)
                        }
                        typeFun.type_
                    }
                    else -> throw Exception("Calling something which is not a function \n${PrettyPrinter.print(expr)}\n")
                }
            }
            is Var -> {
                try {
                    fnTypeBase.take(expr.stellaident_)
                } catch (e: NoSuchElementException) {
                    throw Exception("Refer to an unknown function ${expr.stellaident_} at \n${PrettyPrinter.print(expr)}\n")
                } catch (e: EmptyDequeException) {
                    throw Exception("Refer to a function ${expr.stellaident_} from a local scope that is currently unavailable at \n${PrettyPrinter.print(expr)}\n")
                }
            }
            else -> throw Exception("Unknown type for a function call \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getAndCheckTypeFun(params: ListParamDecl, returnType: Type?, expr: Expr): TypeFun {
        val names = LinkedList<String>()
        val types = ListType()
        // save parameters' types
        for (param in params) {
            when (param) {
                is AParamDecl -> {
                    varTypeBase.put(param.stellaident_, param.type_)
                    names.add(param.stellaident_)
                    types.add(param.type_)
                }
            }
        }
        // obtain the return type
        val type = TypeFun(types, getExprType(expr))
        // check the return type
        if (returnType != null) {
            checkTypes(returnType, getExprType(expr), expr)
        }
        // remove local scope variables
        varTypeBase.remove(names)
        return type
    }

    private fun typecheckDeclFun(decl: DeclFun) {
        val type = getAndCheckTypeFun(decl.listparamdecl_, unwrapReturnType(decl.returntype_, decl), decl.expr_)
        fnTypeBase.put(decl.stellaident_, type)
    }

    @Throws(Exception::class)
    fun typecheckProgram(program: Program?) {
        when (program) {
            is AProgram ->
                for (decl in program.listdecl_) {
                    when (decl) {
                        is DeclFun -> typecheckDeclFun(decl)
                    }
                }
        }
        return
    }
}
