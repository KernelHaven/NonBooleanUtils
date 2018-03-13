package net.ssehub.kernel_haven.non_boolean;


import java.io.File;
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
 * Tests for the {@link NonBooleanPreperation}, which are a bit more complex. Separated to simplify debuging of tests.
 * @author El-Sharkawy
 *
 */
public class NonBooleanPreperationScenarioTest {
    
    private static final File TESTDATA_FOLDER = new File(AllTests.TESTDATA, "nonBooleanPreparation");
    private static final File IN_FOLDER = new File(TESTDATA_FOLDER, "inDir/scenarioTests");
    private static final File OUT_FOLDER = new File(TESTDATA_FOLDER, "outDir/scenarioTests");
    
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
     * Tests scenario 1.
     * @throws SetUpException If setup fails, should not happen.
     */
    @Test
    public void testScenario1() throws SetUpException {
        NonBooleanPreperation preparator = new NonBooleanPreperation();
        Configuration config = createConfig(
            new FiniteIntegerVariable("A", "tristate", new int[] {0, 1, 2}),
            new FiniteIntegerVariable("B", "bool", new int[] {0, 1}));
        preparator.run(config);
        
        FileContentsAssertion.assertContents(new File(OUT_FOLDER, "scenario1.c"), 
            "#if defined(B_eq_0) \n"
            + "#if (defined(A_eq_0)) || (defined(A_eq_1)) \n"
            + "// Code\n"
            + "#endif \n"
            + "#endif \n"
            + "\n"
            + "#if defined(B_eq_1) \n"
            + "#if defined(A_eq_2) \n"
            + "// Code\n"
            + "#endif \n"
            + "#endif");
    }
    
    /**
     * Configures the {@link PipelineConfigurator} and creates the {@link Configuration}, which is needed
     * for testing the {@link NonBooleanPreperation}.
     * 
     * @param variables Should be <tt>null</tt> or empty if the preparation should be tested without a variability
     *     model or the complete list of relevant variables.
     * 
     * @return {@link CodeExtractorConfiguration}, which is needed for testing the {@link NonBooleanPreperation}.
     */
    private Configuration createConfig(VariabilityVariable... variables) {
        try {
            Configuration config = new TestConfiguration(new Properties());
            NonBooleanSettings.registerAllSettings(config);
            config.setValue(NonBooleanSettings.DESTINATION_DIR, OUT_FOLDER);
            config.setValue(NonBooleanSettings.VARIABLE_REGEX, Pattern.compile("\\p{Alpha}+\\w*"));
            config.setValue(DefaultSettings.SOURCE_TREE, IN_FOLDER);
            
            boolean usesVarModel = null != variables && variables.length > 0;
            if (usesVarModel) {
                PseudoVariabilityExtractor.configure(new File("this is a mock"), variables);
                config.setValue(DefaultSettings.VARIABILITY_EXTRACTOR_CLASS,
                        PseudoVariabilityExtractor.class.getName());
            } 
            
            PipelineConfigurator.instance().init(config);
            PipelineConfigurator.instance().instantiateExtractors();
            PipelineConfigurator.instance().createProviders();
            
            if (usesVarModel) {
                PipelineConfigurator.instance().getVmProvider().start();
            }
            
            return config;
            
        } catch (SetUpException e) {
            Assert.fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
