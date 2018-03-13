package net.ssehub.kernel_haven.non_boolean.replacer;

public class LiteralBoolResult extends BoolResult {

    public static final LiteralBoolResult TRUE = new LiteralBoolResult(true);
    
    public static final LiteralBoolResult FALSE = new LiteralBoolResult(false);
    
    private boolean value;
    
    private LiteralBoolResult(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public String toCppString() {
        return value ? "1" : "0";
    }
    
}
