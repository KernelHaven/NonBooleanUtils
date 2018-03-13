package net.ssehub.kernel_haven.non_boolean.replacer;

public class BoolAnd extends BoolResult {

    private Result leftSide;
    
    private Result rightSide;
    
    public BoolAnd(Result leftSide, Result rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }
    
    public Result getLeftSide() {
        return leftSide;
    }
    
    public Result getRightSide() {
        return rightSide;
    }

    @Override
    public String toCppString() {
        return "(" + leftSide.toCppString() + ") && (" + rightSide.toCppString() + ")";
    }
    
}
