package net.ssehub.kernel_haven.non_boolean;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.non_boolean.heuristic.AllHeuristicTests;
import net.ssehub.kernel_haven.non_boolean.parser.AllParserTests;
import net.ssehub.kernel_haven.non_boolean.replacer.AllReplacerTests;

/**
 * All tests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    AllParserTests.class,
    AllReplacerTests.class,
    AllHeuristicTests.class,
    
    CPPUtilsTest.class,
    NonBooleanPreperationScenarioTest.class,
    NumberUtilsTest.class,
    CppBufferedWriterTest.class,
    NonBooleanPreparationTest.class,
    })
public class AllTests {

}
