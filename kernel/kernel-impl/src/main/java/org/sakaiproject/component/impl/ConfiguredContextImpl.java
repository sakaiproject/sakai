package org.sakaiproject.component.impl;

import com.opencsv.CSVParser;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ConfiguredContext;
import org.sakaiproject.util.NumberUtil;
import org.sakaiproject.util.SakaiProperties;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.IntStream;

/**
 * Simple implementation of {@link ConfiguredContext} that wraps over
 * {@link org.sakaiproject.util.SakaiProperties}. This is useful in bridging
 * between classic configuration, which tends to use the
 * {@link org.sakaiproject.component.api.ServerConfigurationService} and more
 * modern configuration, which tends to inject the required values (which
 * allows their usage to be inspected).
 * <p>
 * The main problem with ServerConfigurationService is that the bean is quite
 * coupled to others, including an entire chain through the Hibernate config
 * to be able to provide dynamic lookup. This ContextConfiguration model relies
 * on the regular Spring conventions of property sources and does not reach
 * back out to collect more. That is, it assumes that the context has been
 * configured with the sources and values you need.
 * <p>
 * The {@link org.sakaiproject.modi.ModiSakaiProperties} implementation works
 * to provide the same hierarchy and placeholder/override behavior as the usual
 * SakaiProperties without the custom application context and lifecycle issues.
 * For now, this is primarily useful in untangling the component dependency
 * graph and working toward eliminating the ComponentManager entirely.
 */
@Slf4j
public class ConfiguredContextImpl implements ConfiguredContext {
    private final Properties properties;

    public ConfiguredContextImpl(SakaiProperties sakaiProperties) {
        this.properties = sakaiProperties.getProperties();
    }

    @Override
    public Optional<String> getString(String name) {
        return Optional.ofNullable(properties.getProperty(name));
    }

    @Override
    public String getString(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    @Override
    public Optional<Integer> getInt(String name) {
        return getString(name).flatMap(NumberUtil::toInteger);
    }

    @Override
    public Integer getInt(String name, Integer defaultValue) {
        return getString(name).flatMap(NumberUtil::toInteger).orElse(defaultValue);
    }

    @Override
    public Optional<Long> getLong(String name) {
        return getString(name).flatMap(NumberUtil::toLong);
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        return getString(name).flatMap(NumberUtil::toLong).orElse(defaultValue);
    }

    @Override
    public Optional<Boolean> getBoolean(String name) {
        return getString(name).map(Boolean::parseBoolean);
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        return getString(name).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    @Override
    public Optional<Object> get(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public Object get(String name, Object defaultValue) {
        return properties.getOrDefault(name, defaultValue);
    }

    @Override
    public List<String> getStrings(String name) {
        return multivaluedString(name)
                .or(() -> enumeratedStrings(name))
                .orElse(List.of());
    }

    Optional<List<String>> enumeratedStrings(String name) {
        var count = getInt(name + ".count").filter(n -> n > 0);
        if (count.isEmpty()) return Optional.empty();

        var keys = IntStream.rangeClosed(1, count.get()).mapToObj(i -> name + "." + i);
        return Optional.of(keys.map(key -> getString(key, "")).toList());
    }

    Optional<List<String>> multivaluedString(String name) {
        return getString(name)
                .flatMap(value -> parseCSV(name, value));
    }

    Optional<List<String>> parseCSV(String name, String value) {
        try {
            return Optional.of(List.of(new CSVParser().parseLine(value)));
        } catch (IOException e) {
            log.warn("Config property ({}) read as multi-valued string, but failure occurred while parsing: {}", name, e);
            return Optional.empty();
        }
    }

}