package net.ssehub.kernel_haven.non_boolean.parser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    CppLexerTest.class,
    CppParserTest.class,
    CppParserScenarioTests.class,
    })
public class AllParserTests {

}
