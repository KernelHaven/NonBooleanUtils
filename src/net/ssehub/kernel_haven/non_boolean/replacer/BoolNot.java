package net.ssehub.kernel_haven.non_boolean.replacer;

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

}
