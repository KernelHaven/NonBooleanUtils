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
    
    private long[] constants;
    
    /**
     * Creates this {@link NonBooleanVariable}.
     * 
     * @param name The name of this variable.
     * @param constants The allowed constants.
     */
    public NonBooleanVariable(String name, Set<Long> constants) {
        this.name = name;
        this.constants = new long[constants.size()];
        int i = 0;
        for (Long c : constants) {
            this.constants[i++] = c;
        }
    }
    
    /**
     * Returns the allowed constants of this variable.
     * 
     * @return The allowed constants.
     */
    public long[] getConstants() {
        return constants;
    }
    
    @Override
    public String toString() {
        return name + Arrays.toString(constants);
    }
    
}
