package net.ssehub.kernel_haven.non_boolean;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.non_boolean.parser.AllParserTests;

/**
 * All tests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    AllParserTests.class,
    
    CPPUtilsTest.class,
    NonBooleanPreperationTest.class,
    NonBooleanPreperationScenarioTest.class,
    NumberUtilsTest.class,
    CppBufferedWriterTest.class
    })
public class AllTests {

}
