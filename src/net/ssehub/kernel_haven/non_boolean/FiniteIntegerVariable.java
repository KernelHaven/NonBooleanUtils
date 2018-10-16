package net.ssehub.kernel_haven.non_boolean;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;
import net.ssehub.kernel_haven.variability_model.VariabilityVariableSerializer;
import net.ssehub.kernel_haven.variability_model.VariabilityVariableSerializerFactory;

/**
 * An Integer-based variability variable with a finite domain.
 * @author El-Sharkawy
 *
 */
public class FiniteIntegerVariable extends VariabilityVariable implements Iterable<Integer> {

    static {
        // this block is called by the infrastructure, see loadClasses.txt
        
        VariabilityVariableSerializerFactory.INSTANCE.registerSerializer(FiniteIntegerVariable.class.getName(),
                new FiniteIntegerVariableSerializer());
    }
    
    private int[] values;
    
    /**
     * Sole constructor for this class.
     * @param name The name of the new variable. Must not be <tt>null</tt>.
     * @param type The type of the new variable, e.g., <tt>integer</tt>. Must not be <tt>null</tt>.
     * @param values The allowed values for this variable.
     */
    public FiniteIntegerVariable(String name, String type, int[] values) {
        super(name, type);
        if (null != values) {
            Arrays.sort(values);
            this.values = values;
        } else {
            this.values = new int[0];
        }
    }

    /**
     * Returns the number of possible values for this variable.
     * @return A number &ge; 0.
     */
    public int getSizeOfRange() {
        return values.length;
    }
    
    /**
     * Returns the specified values.
     * @param index A 0-based index of the element to return. 
     * @return The element at the specified position in this list
     * 
     * @see #getSizeOfRange()
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &ge; {@link #getSizeOfRange()}</tt>)
     */
    public int getValue(int index) {
        return values[index];
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return values.length > pos;
            }

            @Override
            public Integer next() {
                return values[pos++];
            }
        };
    }
    
    /**
     * Initialization method called by KernelHaven. See loadClasses.txt
     * 
     * @param config The global pipeline configuration.
     */
    public static void initialize(@NonNull Configuration config) {
        // everything already done in the static block
    }
    
    /**
     * A serializer for {@link FiniteIntegerVariable}s.
     */
    private static final class FiniteIntegerVariableSerializer extends VariabilityVariableSerializer {
        
        @Override
        protected @NonNull List<@NonNull String> serializeImpl(@NonNull VariabilityVariable variable) {
            FiniteIntegerVariable finVar = (FiniteIntegerVariable) variable;
            
            List<String> result = super.serializeImpl(variable);
            
            result.add(String.valueOf(finVar.values.length));
            for (int value :  finVar.values) {
                result.add(String.valueOf(value));
            }
            
            return result;
        }
        
        @Override
        protected void checkLength(@NonNull String @NonNull [] csv) throws FormatException {
            if (csv.length < DEFAULT_SIZE + 1) {
                throw new FormatException("Expected at least " + (DEFAULT_SIZE + 1) + " columns"); 
            }
            
            try {
                int length = Integer.parseInt(csv[DEFAULT_SIZE]);
                
                if (csv.length != DEFAULT_SIZE + 1 + length) {
                    throw new FormatException("Expected exactly " + (DEFAULT_SIZE + 1 + length) + " fields");
                }
                
            } catch (NumberFormatException e) {
                throw new FormatException(e);
            }
        }
        
        @Override
        protected @NonNull VariabilityVariable deserializeImpl(@NonNull String @NonNull [] csv) throws FormatException {
            VariabilityVariable variable = super.deserializeImpl(csv);
            try {
                int size = Integer.parseInt(csv[DEFAULT_SIZE]);
                int[] values = new int[size];
                
                for (int i = 0; i < size; i++) {
                    values[i] = Integer.parseInt(csv[i + DEFAULT_SIZE + 1]);
                }
                
                FiniteIntegerVariable result = new FiniteIntegerVariable(variable.getName(), variable.getType(),
                        values);
                
                return result;
            
            } catch (NumberFormatException e) {
                throw new FormatException(e);
            }
        }
        
    }
    
}
