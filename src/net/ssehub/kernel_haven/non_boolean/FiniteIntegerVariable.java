package net.ssehub.kernel_haven.non_boolean;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.io.json.JsonElement;
import net.ssehub.kernel_haven.util.io.json.JsonList;
import net.ssehub.kernel_haven.util.io.json.JsonNumber;
import net.ssehub.kernel_haven.util.io.json.JsonObject;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * An Integer-based variability variable with a finite domain.
 * @author El-Sharkawy
 *
 */
public class FiniteIntegerVariable extends VariabilityVariable implements Iterable<Integer> {

    private int[] values;
    
    /**
     * Creates a new {@link FiniteIntegerVariable}.
     * 
     * @param name The name of the new variable. Must not be <tt>null</tt>.
     * @param type The type of the new variable, e.g., <tt>integer</tt>. Must not be <tt>null</tt>.
     */
    public FiniteIntegerVariable(String name, String type) {
        super(name, type);
        this.values = new int[0];
    }
    
    /**
     * Creates a new {@link FiniteIntegerVariable}.
     * 
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
    
    @Override
    protected @NonNull JsonObject toJson() {
        JsonObject result = super.toJson();
        
        JsonList valueList = new JsonList();
        for (int value : this.values) {
            valueList.addElement(new JsonNumber(value));
        }
        
        result.putElement("allowedValues", valueList);
        
        return result;
    }
    
    @Override
    protected void setJsonData(@NonNull JsonObject data, Map<@NonNull String, VariabilityVariable> vars)
            throws FormatException {
        super.setJsonData(data, vars);
        
        JsonList valueList = data.getList("allowedValues");
        this.values = new int[valueList.getSize()];
        int i = 0;
        for (JsonElement element : valueList) {
            if (!(element instanceof JsonNumber)) {
                throw new FormatException("Expected JsonNumber, but got " + element.getClass().getSimpleName());
            }
            values[i++] = ((JsonNumber) element).getValue().intValue();
        }
    }
    
    @Override
    protected @NonNull List<@NonNull String> getSerializationData() {
        List<@NonNull String> data = super.getSerializationData();
        
        for (int i = values.length - 1; i >= 0; i--) {
            data.add(0, String.valueOf(values[i]));
        }
        data.add(0, String.valueOf(values.length));
        
        return data;
    }
    
    @Override
    protected void setSerializationData(@NonNull List<@NonNull String> data,
            @NonNull Map<@NonNull String, VariabilityVariable> variables) throws FormatException {
        
        if (data.isEmpty()) {
            throw new FormatException("Expected at least one element");
        }
        
        try {
            int size = Integer.parseInt(data.remove(0));
        
            if (data.size() < size) {
                throw new FormatException("Expected at least " + size + " more elements");
            }
            
            this.values = new int[size];
            for (int i = 0; i < size; i++) {
                this.values[i] = Integer.parseInt(data.remove(0));
            }
            
        } catch (NumberFormatException e) {
            throw new FormatException(e);
        }
        
        super.setSerializationData(data, variables);
    }
    
}
