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

/**
 * Utility functions to handle CPP statements.
 * @author El-Sharkawy
 *
 */
public class CPPUtils {

    /**
     * Avoid instantiation.
     */
    private CPPUtils() {}
    
    /**
     * Checks if a line is a CPP <tt>if</tt> or <tt>elif</tt> line, but not a <tt>ifdef</tt> or <tt>ifndef</tt> line.
     * @param line The complete line to test.
     * @return <tt>true</tt> if the given line is a CPP <tt>if</tt> or <tt>elif</tt> line
     */
    public static boolean isIfOrElifStatement(String line) {
        boolean result = false;
        
        String trimmedLine = line.trim();
        result = (trimmedLine.startsWith("#if") || line.startsWith("#elif"))
                 && !trimmedLine.startsWith("#ifdef")
                 && !trimmedLine.startsWith("#ifndef");            
        
        return result;
    }
}
