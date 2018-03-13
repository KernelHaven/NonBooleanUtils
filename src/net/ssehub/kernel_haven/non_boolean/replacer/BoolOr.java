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
    
    /**
     * Returns the left side.
     * 
     * @return The left side.
     */
    public Result getLeftSide() {
        return leftSide;
    }
    
    /**
     * Returns the right side.
     * 
     * @return The right side.
     */
    public Result getRightSide() {
        return rightSide;
    }
    
    @Override
    public String toCppString() {
        return "(" + leftSide.toCppString() + ") || (" + rightSide.toCppString() + ")";
    }
    
}
