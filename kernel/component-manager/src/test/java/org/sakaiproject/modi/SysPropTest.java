/*
 * Copyright (c) 2003-2022 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.modi;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.sakaiproject.modi.SysProp.*;

public class SysPropTest {
    @Before
    public void clear() {
        System.clearProperty("sakai.home");
    }

    @Test
    public void givenAKnownKey_whenLookingUp_thenAPropIsReturned() {
        assertThat(SysProp.lookup("sakai.home").get()).isEqualTo(sakai_home);
    }

    @Test
    public void givenAnUnknownKey_whenLookingUp_thenEmptyIsReturned() {
        assertThat(SysProp.lookup("unknown")).isEmpty();
    }

    @Test
    public void givenAProp_thenItKnowsItsKey() {
        assertThat(sakai_home.getKey()).isEqualTo("sakai.home");
    }

    @Test
    public void givenAValue_whenGetting_thenItIsPresent() {
        System.setProperty("sakai.home", File.separator + "home");
        assertThat(sakai_home.get().get()).isEqualTo(File.separator + "home");
    }

    @Test
    public void givenAValue_whenGettingPath_thenItIsPresent() {
        System.setProperty("sakai.home", File.separator + "home");
        assertThat(sakai_home.getPath().get()).isEqualTo(Path.of(File.separator + "home"));
    }

    @Test
    public void givenAValue_whenGettingPathWithSuffix_thenItIsAppended() {
        System.setProperty("sakai.home", File.separator + "home");
        assertThat(sakai_home.getPathPlus("other").get()).isEqualTo(Path.of(File.separator + "home" + File.separator + "other"));
    }

    @Test
    public void givenAValue_whenGettingRawPathWithSuffix_thenItIsAppended() {
        System.setProperty("sakai.home", File.separator + "home");
        assertThat(sakai_home.getRawPathPlus("other")).isEqualTo(Path.of(File.separator + "home" + File.separator + "other"));
    }

    @Test
    public void givenAValue_whenGettingRaw_thenItIsMatches() {
        System.setProperty("sakai.home", File.separator + "home");
        assertThat(sakai_home.getRaw()).isEqualTo(File.separator + "home");
    }

    @Test
    public void givenAStringPath_whenGettingAsPath_thenItMatches() {
        System.setProperty("sakai.home", File.separator + "home");
        assertThat(sakai_home.getRawPath()).isEqualTo(Path.of(File.separator + "home"));
    }

    @Test
    public void givenAValue_whenSettingAString_thenItIsStoredInSystemProps() {
        System.setProperty("sakai.home", File.separator + "home");

        sakai_home.set(File.separator + "changed");

        assertThat(System.getProperty("sakai.home")).isEqualTo(File.separator + "changed");
    }

    @Test
    public void givenAValue_whenSettingAPath_thenItIsStoredInSystemProps() {
        System.setProperty("sakai.home", File.separator + "home");

        sakai_home.set(Path.of(File.separator + "changed"));

        assertThat(System.getProperty("sakai.home")).isEqualTo(File.separator + "changed");
    }

    @Test
    public void givenAMissingValue_whenGetting_thenItIsEmpty() {
        assertThat(sakai_home.get()).isEmpty();
    }

    @Test
    public void givenAMissingValue_whenGettingWithDefault_thenDefaultIsGivenInOptional() {
        assertThat(sakai_home.get("default").get()).isEqualTo("default");
    }

    @Test
    public void givenAMissingValue_whenGettingPathWithSuffix_thenItIsEmpty() {
        assertThat(sakai_home.getPathPlus("other")).isEmpty();
    }

    @Test
    public void givenAMissingValue_whenGettingRawWithDefault_thenDefaultIsGiven() {
        assertThat(sakai_home.getRaw("default")).isEqualTo("default");
    }

    @Test
    public void givenAMissingValue_whenGettingRawPathWithSuffix_thenItIsNull() {
        assertThat(sakai_home.getRawPathPlus("other")).isNull();
    }

}
