package net.ssehub.kernel_haven.non_boolean;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.AllTests;
import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.test_utils.FileContentsAssertion;
import net.ssehub.kernel_haven.test_utils.PseudoVariabilityExtractor;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.Logger;
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
     * Inits the logger.
     */
    @BeforeClass
    public static void beforeClass() {
        Logger.init();
    }
    
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
            config.setValue(NonBooleanSettings.VARIABLE_REGEX, Pattern.compile("\\p{Alpha}+"));
            config.setValue(DefaultSettings.SOURCE_TREE, IN_FOLDER);
            
            return config;
            
        } catch (SetUpException e) {
            Assert.fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
