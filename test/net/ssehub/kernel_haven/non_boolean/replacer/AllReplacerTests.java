package net.ssehub.kernel_haven.non_boolean.replacer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All tests in the replacer package.
 *
 * @author Adam
 */
@RunWith(Suite.class)
@SuiteClasses({
    CppReplacerTest.class,
    CppReplacerNegativeTest.class,
    
    NonCppReplacerTest.class,
    NonCppReplacerNegativeTest.class,
    
    ToFormulaTest.class,
    })
public class AllReplacerTests {

}
