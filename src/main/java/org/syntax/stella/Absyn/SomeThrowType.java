// File generated by the BNF Converter (bnfc 2.9.4.1).

package org.syntax.stella.Absyn;

public class SomeThrowType  extends ThrowType {
  public final ListType listtype_;
  public int line_num, col_num, offset;
  public SomeThrowType(ListType p1) { listtype_ = p1; }

  public <R,A> R accept(Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof SomeThrowType) {
      SomeThrowType x = (SomeThrowType)o;
      return this.listtype_.equals(x.listtype_);
    }
    return false;
  }

  public int hashCode() {
    return this.listtype_.hashCode();
  }


}
