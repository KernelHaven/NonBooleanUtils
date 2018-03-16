package net.ssehub.kernel_haven.non_boolean;

import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * An Integer-based variability variable with a infinite (not restricted) domain.
 * 
 * @author Adam
 */
public class InfiniteIntegerVariable extends VariabilityVariable {

    /**
     * Creates this {@link InfiniteIntegerVariable}.
     * 
     * @param name The name of this variable.
     * @param type The type of this variable.
     */
    public InfiniteIntegerVariable(@NonNull String name, @NonNull String type) {
        super(name, type);
    }

}
