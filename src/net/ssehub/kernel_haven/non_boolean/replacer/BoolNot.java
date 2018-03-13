package net.ssehub.kernel_haven.non_boolean.replacer;

public class BoolNot extends BoolResult {
    
    private Result nested;
    
    public BoolNot(Result nested) {
        this.nested = nested;
    }
    
    public Result getNested() {
        return nested;
    }
    

    @Override
    public String toCppString() {
        return "!(" + nested.toCppString() + ")";
    }

}
