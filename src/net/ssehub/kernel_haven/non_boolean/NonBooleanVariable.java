package net.ssehub.kernel_haven.non_boolean;

import java.util.Arrays;
import java.util.Set;

/**
 * A non-boolean variable. This variable has a name and a set of allowed integer values.
 * 
 * @author Adam
 */
public final class NonBooleanVariable {
    
    private String name;
    
    private boolean infinite;
    
    private long[] constants;
    
    /**
     * Creates this {@link NonBooleanVariable}.
     * 
     * @param name The name of this variable.
     * @param constants The allowed constants.
     */
    public NonBooleanVariable(String name, Set<Long> constants) {
        this(name, constants, false);
    }
    
    /**
     * Creates this {@link NonBooleanVariable}.
     * 
     * @param name The name of this variable.
     * @param constants The allowed constants.
     * @param infinite Whether this is an infinite integer or not. constants is ignored in this case.
     */
    public NonBooleanVariable(String name, Set<Long> constants, boolean infinite) {
        this.name = name;
        
        this.constants = new long[constants.size()];
        int i = 0;
        for (Long c : constants) {
            this.constants[i++] = c;
        }
        
        this.infinite = infinite;
    }
    
    /**
     * Returns the allowed constants of this variable.
     * 
     * @return The allowed constants.
     */
    public long[] getConstants() {
        return constants;
    }
    
    /**
     * Whether this is an infinite integer variable.
     * 
     * @return Whether this integer variable has no restrictions on possible values.
     */
    public boolean isInfinite() {
        return infinite;
    }
    
    @Override
    public String toString() {
        return name + Arrays.toString(constants);
    }
    
}
