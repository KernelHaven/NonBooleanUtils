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
    
    /**
     * Returns the nested {@link Result}.
     * 
     * @return The nested {@link Result}.
     */
    public Result getNested() {
        return nested;
    }

    @Override
    public String toCppString() {
        return "!(" + nested.toCppString() + ")";
    }

}
