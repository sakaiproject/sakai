package org.sakaiproject.modi;

import org.junit.Before;
import org.junit.Test;

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
        System.setProperty("sakai.home", "/home");
        assertThat(sakai_home.get().get()).isEqualTo("/home");
    }

    @Test
    public void givenAValue_whenGettingPath_thenItIsPresent() {
        System.setProperty("sakai.home", "/home");
        assertThat(sakai_home.getPath().get()).isEqualTo(Path.of("/home"));
    }

    @Test
    public void givenAValue_whenGettingPathWithSuffix_thenItIsAppended() {
        System.setProperty("sakai.home", "/home");
        assertThat(sakai_home.getPathPlus("other").get()).isEqualTo(Path.of("/home/other"));
    }

    @Test
    public void givenAValue_whenGettingRawPathWithSuffix_thenItIsAppended() {
        System.setProperty("sakai.home", "/home");
        assertThat(sakai_home.getRawPathPlus("other")).isEqualTo(Path.of("/home/other"));
    }

    @Test
    public void givenAValue_whenGettingRaw_thenItIsMatches() {
        System.setProperty("sakai.home", "/home");
        assertThat(sakai_home.getRaw()).isEqualTo("/home");
    }

    @Test
    public void givenAStringPath_whenGettingAsPath_thenItMatches() {
        System.setProperty("sakai.home", "/home");
        assertThat(sakai_home.getRawPath()).isEqualTo(Path.of("/home"));
    }

    @Test
    public void givenAValue_whenSettingAString_thenItIsStoredInSystemProps() {
        System.setProperty("sakai.home", "/home");

        sakai_home.set("/changed");

        assertThat(System.getProperty("sakai.home")).isEqualTo("/changed");
    }

    @Test
    public void givenAValue_whenSettingAPath_thenItIsStoredInSystemProps() {
        System.setProperty("sakai.home", "/home");

        sakai_home.set(Path.of("/changed"));

        assertThat(System.getProperty("sakai.home")).isEqualTo("/changed");
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
