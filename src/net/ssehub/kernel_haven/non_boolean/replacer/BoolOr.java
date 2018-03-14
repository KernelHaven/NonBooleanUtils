package net.ssehub.kernel_haven.non_boolean.replacer;

/**
 * A boolean disjunction of two {@link Result}s.
 *
 * @author Adam
 */
class BoolOr extends BoolResult {

    private Result leftSide;
    
    private Result rightSide;
    
    /**
     * Creates this disjunction.
     * 
     * @param leftSide The left side.
     * @param rightSide The right side.
     */
    public BoolOr(Result leftSide, Result rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }
    
    @Override
    public String toCppString() {
        return "(" + leftSide.toCppString() + ") || (" + rightSide.toCppString() + ")";
    }
    
    @Override
    public String toNonCppString() {
        return "(" + leftSide.toNonCppString() + ") || (" + rightSide.toNonCppString() + ")";
    }
    
}
