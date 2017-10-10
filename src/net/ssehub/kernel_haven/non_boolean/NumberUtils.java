package net.ssehub.kernel_haven.non_boolean;

import java.math.BigInteger;

/**
 * Utility functions for string to number operations.
 * 
 * @author El-Sharkawy
 *
 */
public class NumberUtils {

    /**
     * Checks if a given String is an integer value.
     * @param str The string to test.
     * @param radix the radix (usually 10, or 16 for hex values).
     * 
     * @return <tt>true</tt> if the String is an Integer.
     * @see <a href="https://stackoverflow.com/a/5439547">https://stackoverflow.com/a/5439547</a>
     */
    public static boolean isInteger(String str, int radix) {
        boolean result = false;
        if (!str.isEmpty()) {
            result = true;
            for (int i = 0; i < str.length() && result; i++) {
                if (i == 0 && str.charAt(i) == '-') {
                    if (str.length() == 1) {
                        result = false;
                    }
                }
                if (Character.digit(str.charAt(i), radix) < 0) {
                    result = false;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Converts a string into a number (considering correct sub class, e.g., Integer or Double).
     * @param str The string to convert.
     * @return A number or <tt>null</tt> if it could not be converted.
     */
    public static Number convertToNumber(String str) {
        Number result = null;
        
        if (isInteger(str, 10)) {
            // Convert integer
            result = Integer.valueOf(str);
        } else if (str.startsWith("0x") && isInteger(str.substring(2), 16)) {
            // Convert hex value
            BigInteger tmpResult = new BigInteger(str.substring(2), 16);
            try {
                // Cast to Integer if possible
                result = tmpResult.intValueExact();
            } catch (ArithmeticException exc) {
                result = tmpResult;
            }
        } else {
            // Convert it into a double
            try {
                Double tmpResult = Double.valueOf(str);
                if ((tmpResult == Math.floor(tmpResult)) && !Double.isInfinite(tmpResult)) {
                 // Cast to Integer if possible (e.g., if it ends with .0)
                    result = tmpResult.intValue();
                } else {
                    result = tmpResult;
                }
            } catch (NumberFormatException exc) {
                // Not critical, ignore
            }
        }
        
        return result;
    }

}
