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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Tests the {@link NonBooleanPreperation}.
 * 
 * @author Adam
 */
public class NonBooleanPreparationTest {

    /**
     * Tests whether {@link NonBooleanPreperation#removeComments(String)} removes inline comments correctly.
     */
    @Test
    public void testRemoveInlineComments() {
        assertThat(NonBooleanPreperation.removeComments("A /* something */ B"), is("A  B"));
        assertThat(NonBooleanPreperation.removeComments("/* something */ B"), is(" B"));
        assertThat(NonBooleanPreperation.removeComments("A /* something */"), is("A "));
        assertThat(NonBooleanPreperation.removeComments("/* A B C *"), is(""));
        assertThat(NonBooleanPreperation.removeComments("A /* something // */ B"), is("A  B"));
    }
    
    /**
     * Tests whether {@link NonBooleanPreperation#removeComments(String)} removes line comments correctly.
     */
    @Test
    public void testRemoveLineComments() {
        assertThat(NonBooleanPreperation.removeComments("A B // C"), is("A B "));
    }
    
    /**
     * Tests whether {@link NonBooleanPreperation#removeComments(String)} correctly does nothing when no comments are
     * there.
     */
    @Test
    public void testRemoveNoComments() {
        assertThat(NonBooleanPreperation.removeComments("A B C"), is("A B C"));
        assertThat(NonBooleanPreperation.removeComments("A / B C"), is("A / B C"));
        assertThat(NonBooleanPreperation.removeComments("A */ B C"), is("A */ B C"));
        assertThat(NonBooleanPreperation.removeComments("A / / B C"), is("A / / B C"));
        assertThat(NonBooleanPreperation.removeComments("A B C /"), is("A B C /"));
    }
    
}
