package net.ssehub.kernel_haven.non_boolean;

import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;
import net.ssehub.kernel_haven.variability_model.VariabilityVariableSerializer;
import net.ssehub.kernel_haven.variability_model.VariabilityVariableSerializerFactory;

/**
 * An Integer-based variability variable with a infinite (not restricted) domain.
 * 
 * @author Adam
 */
public class InfiniteIntegerVariable extends VariabilityVariable {

    static {
        // this block is called by the infrastructure, see loadClasses.txt
        VariabilityVariableSerializerFactory.INSTANCE.registerSerializer(InfiniteIntegerVariable.class.getName(),
                new VariabilityVariableSerializer()); // use default serializer
    }
    
    /**
     * Creates this {@link InfiniteIntegerVariable}.
     * 
     * @param name The name of this variable.
     * @param type The type of this variable.
     */
    public InfiniteIntegerVariable(@NonNull String name, @NonNull String type) {
        super(name, type);
    }
    
    /**
     * Initialization method called by KernelHaven. See loadClasses.txt
     * 
     * @param config The global pipeline configuration.
     */
    public static void initialize(@NonNull Configuration config) {
        // everything already done in the static block
    }

}
