package net.ssehub.kernel_haven.non_boolean;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.ssehub.kernel_haven.AllTests;
import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.test_utils.FileContentsAssertion;
import net.ssehub.kernel_haven.test_utils.PseudoVariabilityExtractor;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * Tests for the {@link NonBooleanPreperation}.
 * @author El-Sharkawy
 *
 */
public class NonBooleanPreperationTest {
    
    private static final File TESTDATA_FOLDER = new File(AllTests.TESTDATA, "nonBooleanPreparation");
    private static final File IN_FOLDER = new File(TESTDATA_FOLDER, "inDir");
    private static final File OUT_FOLDER = new File(TESTDATA_FOLDER, "outDir");
    
    /**
     * Wipes the {@link #OUT_FOLDER} for testing.
     */
    @Before
    public void setUp() {
        if (!OUT_FOLDER.exists()) {
            OUT_FOLDER.mkdirs();
        } else {
            File[] elements = OUT_FOLDER.listFiles();
            for (int i = 0; i < elements.length; i++) {
                elements[i].delete();
            }
        }
    }
    
    /**
     * Tests the replacement of a comparison, without using a variability model.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testGreaterExpressionWithoutVarModel() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "comparison.c"), 
            "#if ((defined(a_eq_2))) \n"
            + "    // Do something\n"
            + "#endif");
    }
    
    /**
     * Tests the replacement of a comparison, with a variability model.
     * Both variables have the same range.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testGreaterExpressionWithVarModel() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(new FiniteIntegerVariable("a", "tristate",
            new int[] {1, 2, 3}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "comparison.c"), 
            "#if ((defined(a_eq_2) || defined(a_eq_3))) \n"
            + "    // Do something\n"
            + "#endif");
    }
    
    /**
     * Tests the replacement of a comparison, with a variability model.
     * Both variables have the same range.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnVarsWithVarModelSameRanges() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
            new FiniteIntegerVariable("a", "tristate", new int[] {1, 2, 3}),
            new FiniteIntegerVariable("b", "tristate", new int[] {1, 2, 3}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOnVars1.c"), 
            "#if (((defined(a_eq_1) && defined(b_eq_1)) || (defined(a_eq_2) && defined(b_eq_2)) "
            + "|| (defined(a_eq_3) && defined(b_eq_3)))) {\n"
            + "    // Do something\n"
            + "#endif");
    }
    
    /**
     * Tests the replacement of a comparison, with a variability model.
     * The ranges of the variables differ.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnVarsWithVarModelDifferentRanges() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("a", "tristate", new int[] {0, 1, 2}),
                new FiniteIntegerVariable("b", "tristate", new int[] {1, 2, 3}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOnVars1.c"), 
                "#if (((defined(a_eq_1) && defined(b_eq_1)) || (defined(a_eq_2) && defined(b_eq_2))"
                        + ")) {\n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Tests that a &lt; b is translated correctly.
     * 
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testLessThanOnTwoVars() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
            new FiniteIntegerVariable("a", "tristate", new int[] {1, 2}),
            new FiniteIntegerVariable("b", "tristate", new int[] {1, 2, 3}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "lessThan.c"), 
            "#if (((defined(a_eq_1) && defined(b_eq_2)) || (defined(a_eq_1) && defined(b_eq_3)) || "
                    + "(defined(a_eq_2) && defined(b_eq_3))))\n"
                    + "    // Do something\n"
                    + "#endif");
    }
    
    /**
     * Tests that a != b is translated correctly.
     * 
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testUnequalOnTwoVars() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("a", "tristate", new int[] {1, 2}),
                new FiniteIntegerVariable("b", "tristate", new int[] {1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "unequal.c"), 
                "#if (((defined(a_eq_1) && defined(b_eq_2)) || (defined(a_eq_2) && defined(b_eq_1))"
                        + "))\n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Tests that a != b is translated correctly.
     * 
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testGreaterThanOrEqualOnTwoVars() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("a", "tristate", new int[] {1, 2}),
                new FiniteIntegerVariable("b", "tristate", new int[] {1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "greaterOrEqual.c"), 
                "#if ((defined(a_eq_1) && defined(b_eq_1)) || (defined(a_eq_2) && defined(b_eq_1)) "
                        + "|| (defined(a_eq_2) && defined(b_eq_2)))\n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Tests the replacement of a constant (not end of line).
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnConstantNotEndOfLine() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation() {
            @Override
            protected Map<String, Integer> getConstants() {
                Map<String, Integer> constMap = new HashMap<String, Integer>();
                constMap.put("CONSTANT", 1);
                return constMap;
            }
        };
        Configuration config = createConfig(
            new FiniteIntegerVariable("a", "bool", new int[] {1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOnConstant1.c"), 
            "#if (defined(a_eq_1)) {\n"
            + "    // Do something\n"
            + "#endif");
    }
    
    /**
     * Tests the replacement of a constant at the end of line.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnConstantEndOfLine() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation() {
            @Override
            protected Map<String, Integer> getConstants() {
                Map<String, Integer> constMap = new HashMap<String, Integer>();
                constMap.put("CONSTANT", 1);
                return constMap;
            }
        };
        Configuration config = createConfig(
            new FiniteIntegerVariable("a", "bool", new int[] {1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOnConstant2.c"), 
            "#if defined(a_eq_1)\n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Tests the replacement of two constants.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnTwoConstants() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation() {
            @Override
            protected Map<String, Integer> getConstants() {
                Map<String, Integer> constMap = new HashMap<String, Integer>();
                constMap.put("CONSTANT", 1);
                return constMap;
            }
        };
        Configuration config = createConfig(
            new FiniteIntegerVariable("a", "bool", new int[] {1, 2}),
            new FiniteIntegerVariable("b", "bool", new int[] {1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOn2Constants.c"), 
            "#if (defined(a_eq_1) || defined(b_eq_1)) {\n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Tests the replacement of number equals variable (wrong order of elements).
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnNumberAndVar() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
            new FiniteIntegerVariable("VAR", "tristate", new int[] {0, 1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOnNumberAndVar.c"), 
            "#if defined(VAR_eq_2)\n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Tests the replacement of number not equals variable (wrong order of elements).
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testInequalityOnNumberAndVar() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
            new FiniteIntegerVariable("VAR", "tristate", new int[] {0, 1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "inequalityOnNumberAndVar.c"), 
            "#if !defined(VAR_eq_2)\n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Tests the replacement of number less than variable (wrong order of elements).
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testNumberLessThanVar() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("VAR", "tristate", new int[] {0, 1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "NumberLessThanVar.c"), 
            "#if (defined(VAR_eq_1) || defined(VAR_eq_2))\n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Tests the replacement of the following expression: <tt>((VAR1 == VAR2) && (VAR3 == 2))</tt>.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testComplexExpression() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("VAR1", "bool", new int[] {0, 1}),
                new FiniteIntegerVariable("VAR2", "bool", new int[] {0, 1}),
                new FiniteIntegerVariable("VAR3", "tristate", new int[] {0, 1, 2}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "complexExpression.c"), 
            "#if ((((defined(VAR1_eq_0) && defined(VAR2_eq_0)) || (defined(VAR1_eq_1) && defined(VAR2_eq_1)))) "
                + "&& (defined(VAR3_eq_2)))\n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Special case: Tests comparison between same constant.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfOne() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifOne.c"), 
            "#if 1 \n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Special case: Tests consideration of integer suffixes.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfOneUL() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifOneUL.c"), 
                "#if 1 \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Special case: Tests comparison between different constant numbers.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testEqualityOnNumbers() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "equalityOnNumbers.c"), 
                "#if (0) \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Special case: Tests greater than expression between two numbers.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfNumberGreaterThanNumberIsTrue() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifNumberGreaterThanNumberIsTrue.c"), 
            "#if 1 \n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Special case: Tests greater than expression between two numbers.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfNumberGreaterThanNumberIsFalse() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifNumberGreaterThanNumberIsFalse.c"), 
            "#if 0 \n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Special case: Tests greater than expression between two numbers.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfNumberLessThanNumberIsTrue() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifNumberLessThanNumberIsTrue.c"), 
            "#if 1 \n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Special case: Tests greater than expression between two numbers.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfNumberLessThanNumberIsFalse() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig();
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifNumberLessThanNumberIsFalse.c"), 
            "#if 0 \n"
                + "    // Do something\n"
                + "#endif");
    }
    
    /**
     * Special case: Tests handling of numeric variables in an <tt>if</tt> without a comparison.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testBooleanNumbersInIf() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
            new FiniteIntegerVariable("VAR1", "bool", new int[] {0, 1}),
            new FiniteIntegerVariable("VAR2", "bool", new int[] {0, 1}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "booleanNumbersInIf.c"), 
                "#if (!defined(VAR1_eq_0) || !defined(VAR2_eq_0)) \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Special case: Tests handling of numeric variables in an <tt>if</tt> without a comparison, but negated.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testBooleanNumbersInIfNegated() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("VAR1", "bool", new int[] {0, 1}),
                new FiniteIntegerVariable("VAR2", "bool", new int[] {0, 1}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "booleanNumbersInIfNegated.c"), 
                "#if (defined(VAR1_eq_0) || defined(VAR2_eq_0)) \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Tests that if defines are not replaced as they are already handled.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfdef() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("VAR1", "bool", new int[] {0, 1}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifdef.c"), 
                "#if defined(VAR1) \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Tests that if defines are not replaced but the variable without an surrounding defined or relational expression
     * is still handled.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testIfdefWeiredCombination() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("VAR1", "bool", new int[] {0, 1}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "ifdefWeiredCombination.c"), 
                "#if (defined(VAR1) || !defined(VAR1_eq_0)) \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Tests support for bit operations bitwise AND and containment of binary 2.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testBitOperationHas2() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
                new FiniteIntegerVariable("VAR", "enumeration", new int[] {0, 1, 2, 3, 4, 5, 6}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "bitOperationHas2.c"), 
                "#if ((defined(VAR_eq_2) || defined(VAR_eq_3) || defined(VAR_eq_6))) \n"
                        + "    // Do something\n"
                        + "#endif");
    }
    
    /**
     * Configures the {@link PipelineConfigurator} and creates the {@link Configuration}, which is needed
     * for testing the {@link NonBooleanPreperation}.
     * @param variables Should be <tt>null</tt> or empty if the preparation should be tested without a variability
     *     model or the complete list of relevant variables.
     * @return {@link CodeExtractorConfiguration}, which is needed for testing the {@link NonBooleanPreperation}.
     */
    private Configuration createConfig(VariabilityVariable... variables) {
        Properties prop = new Properties();
        boolean usesVarModel = null != variables && variables.length > 0;
        if (usesVarModel) {
            PseudoVariabilityExtractor.configure(new File("this is a mock"), variables);
            prop.put("variability.extractor.class", PseudoVariabilityExtractor.class.getName());
        } 
        
        prop.setProperty("source_tree", IN_FOLDER.getAbsolutePath());
        try {
            Configuration config = new TestConfiguration(prop);
            NonBooleanSettings.registerAllSettings(config);
            PipelineConfigurator.instance().init(config);
            PipelineConfigurator.instance().instantiateExtractors();
            PipelineConfigurator.instance().createProviders();
            
            if (usesVarModel) {
                PipelineConfigurator.instance().getVmProvider().start();
            }
            
            config.setValue(NonBooleanSettings.DESTINATION_DIR, OUT_FOLDER);
            config.setValue(NonBooleanSettings.VARIABLE_REGEX, Pattern.compile("\\p{Alpha}+\\w*"));
            config.setValue(DefaultSettings.SOURCE_TREE, IN_FOLDER);
            
            return config;
            
        } catch (SetUpException e) {
            Assert.fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
