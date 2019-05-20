/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.non_boolean;

import java.util.Arrays;
import java.util.Iterator;
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

}
