// File generated by the BNF Converter (bnfc 2.9.4.1).

package org.syntax.stella.Absyn;

public class Application  extends Expr {
  public final Expr expr_;
  public final ListExpr listexpr_;
  public int line_num, col_num, offset;
  public Application(Expr p1, ListExpr p2) { expr_ = p1; listexpr_ = p2; }

  public <R,A> R accept(Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Application) {
      Application x = (Application)o;
      return this.expr_.equals(x.expr_) && this.listexpr_.equals(x.listexpr_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.expr_.hashCode())+this.listexpr_.hashCode();
  }


}