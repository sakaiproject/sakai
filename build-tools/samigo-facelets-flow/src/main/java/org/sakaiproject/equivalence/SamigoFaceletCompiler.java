/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 * http://opensource.org/licenses/ecl2
 */
package org.sakaiproject.equivalence;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.facelets.impl.DefaultFaceletFactory;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.sakaiproject.serialization.MapperFactory;

/** Test-only compilation audit against the actual configured Mojarra libraries. */
public final class SamigoFaceletCompiler implements PhaseListener {
    private static final String MARKER = SamigoFaceletCompiler.class.getName();

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    @Override
    public void afterPhase(PhaseEvent event) {
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        if (!Boolean.getBoolean("samigo.equivalence.compileViews")) {
            return;
        }
        FacesContext context = event.getFacesContext();
        ExternalContext external = context.getExternalContext();
        if (external.getApplicationMap().putIfAbsent(MARKER, Boolean.TRUE) != null) {
            return;
        }
        DefaultFaceletFactory factory = ApplicationAssociate.getCurrentInstance().getFaceletFactory();
        List<Map<String, String>> results = new ArrayList<>();
        Set<String> templates = views(external, "/jsf/");
        templates.addAll(views(external, "/WEB-INF/samigo-views/"));
        for (String path : templates) {
            Map<String, String> result = new LinkedHashMap<>();
            result.put("view", path);
            try {
                factory.getFacelet(context, path);
                result.put("status", "compiled");
            } catch (IOException | RuntimeException exception) {
                result.put("status", "failed");
                result.put("error", exception.toString());
            }
            results.add(result);
        }
        Path destination = Path.of(System.getProperty("samigo.equivalence.output"), "facelet-compilation.json");
        try {
            Files.createDirectories(destination.getParent());
            MapperFactory.jsonBuilder().build().writerWithDefaultPrettyPrinter().writeValue(destination.toFile(), results);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Facelet compilation audit", exception);
        }
    }

    private Set<String> views(ExternalContext external, String directory) {
        Set<String> found = new TreeSet<>();
        Set<String> resources = external.getResourcePaths(directory);
        if (resources != null) {
            for (String resource : resources) {
                if (resource.endsWith("/")) {
                    found.addAll(views(external, resource));
                } else if (resource.endsWith(".xhtml")) {
                    found.add(resource);
                }
            }
        }
        return found;
    }
}
