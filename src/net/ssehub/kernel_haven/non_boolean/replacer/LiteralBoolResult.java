package net.ssehub.kernel_haven.non_boolean.replacer;

/**
 * A literal boolean value. Singleton with two instances, {@link #TRUE} and {@link #FALSE}.
 *
 * @author Adam
 */
class LiteralBoolResult extends BoolResult {

    public static final LiteralBoolResult TRUE = new LiteralBoolResult(true);
    
    public static final LiteralBoolResult FALSE = new LiteralBoolResult(false);
    
    private boolean value;
    
    /**
     * Creates a literal boolean value.
     * 
     * @param value The value.
     */
    private LiteralBoolResult(boolean value) {
        this.value = value;
    }
    
    @Override
    public String toCppString() {
        return value ? "1" : "0";
    }
    
    @Override
    public String toNonCppString() {
        return toCppString();
    }
    
}
