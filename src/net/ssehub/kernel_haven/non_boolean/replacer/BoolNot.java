package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;

/**
 * A boolean negation of a {@link Result}.
 *
 * @author Adam
 */
class BoolNot extends BoolResult {
    
    private Result nested;
    
    /**
     * Creates this negation.
     * 
     * @param nested The nested {@link Result}.
     */
    public BoolNot(Result nested) {
        this.nested = nested;
    }
    
    @Override
    public String toCppString() {
        String result;
        if (nested == LiteralBoolResult.FALSE) {
            result = "1"; // not false
        } else if (nested == LiteralBoolResult.TRUE) {
            result = "0"; // not true
        } else {
            result = "!(" + nested.toCppString() + ")";
        }
        return result;
    }
    
    @Override
    public String toNonCppString() {
        String result;
        if (nested == LiteralBoolResult.FALSE) {
            result = "1"; // not false
        } else if (nested == LiteralBoolResult.TRUE) {
            result = "0"; // not true
        } else {
            result = "!(" + nested.toNonCppString() + ")";
        }
        return result;
    }
    
    @Override
    public Formula toFormula() {
        return new Negation(nested.toFormula());
    }

}
