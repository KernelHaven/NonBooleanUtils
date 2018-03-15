package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * A boolean conjunction of two {@link Result}s.
 *
 * @author Adam
 */
class BoolAnd extends BoolResult {

    private Result leftSide;
    
    private Result rightSide;
    
    /**
     * Creates this conjunction.
     * 
     * @param leftSide The left side.
     * @param rightSide The right side.
     */
    public BoolAnd(Result leftSide, Result rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }
    
    @Override
    public String toCppString() {
        return "(" + leftSide.toCppString() + ") && (" + rightSide.toCppString() + ")";
    }
    
    @Override
    public String toNonCppString() {
        return "(" + leftSide.toNonCppString() + ") && (" + rightSide.toNonCppString() + ")";
    }
    
    @Override
    public Formula toFormula() {
        return new Conjunction(leftSide.toFormula(), rightSide.toFormula());
    }
    
}
