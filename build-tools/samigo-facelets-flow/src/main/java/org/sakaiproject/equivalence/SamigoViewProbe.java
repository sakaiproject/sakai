/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.equivalence;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.component.*;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.*;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.DoubleRangeValidator;
import jakarta.faces.validator.LongRangeValidator;
import jakarta.faces.validator.RegexValidator;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.convert.DateTimeConverter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.*;
import java.util.*;
import org.sakaiproject.serialization.MapperFactory;

/** Test-only observer: captures metadata without evaluating arbitrary model expressions. */
public final class SamigoViewProbe implements PhaseListener {
    private static final ObjectMapper MAPPER = MapperFactory.jsonBuilder().includeEmpty().build();
    private static final String TRACE = SamigoViewProbe.class.getName();

    public PhaseId getPhaseId() { return PhaseId.ANY_PHASE; }
    public void beforePhase(PhaseEvent event) { }
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        String output = System.getProperty("samigo.equivalence.output");
        String step = context.getExternalContext().getRequestHeaderMap().get("X-Sakai-Equivalence-Step");
        if (output == null || step == null || !step.matches("[a-z0-9-]+")) { return; }
        Map<String, Object> request = context.getExternalContext().getRequestMap();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trace = (List<Map<String, Object>>) request.computeIfAbsent(TRACE, key -> new ArrayList<>());
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("phase", event.getPhaseId().toString());
        snapshot.put("postback", context.isPostback());
        snapshot.put("validationFailed", context.isValidationFailed());
        snapshot.put("viewId", context.getViewRoot() == null ? null : context.getViewRoot().getViewId());
        boolean captureTree = !Boolean.getBoolean("samigo.equivalence.finalTreeOnly")
            || event.getPhaseId() == PhaseId.RENDER_RESPONSE || context.getResponseComplete();
        snapshot.put("tree", !captureTree || context.getViewRoot() == null ? null : tree(context, context.getViewRoot()));
        List<Map<String, Object>> messages = new ArrayList<>();
        Iterator<String> ids = context.getClientIdsWithMessages();
        while (ids.hasNext()) {
            String id = ids.next();
            Iterator<FacesMessage> iterator = context.getMessages(id);
            while (iterator.hasNext()) {
                FacesMessage message = iterator.next();
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("clientId", id); data.put("severity", message.getSeverity().toString());
                data.put("summary", message.getSummary()); data.put("detail", message.getDetail());
                messages.add(data);
            }
        }
        snapshot.put("messages", messages);
        trace.add(snapshot);
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE || context.getResponseComplete()) {
            try {
                Path directory = Path.of(output, step);
                Files.createDirectories(directory);
                MAPPER.writerWithDefaultPrettyPrinter().writeValue(directory.resolve(UUID.randomUUID() + ".json").toFile(), trace);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to write equivalence trace for " + step, exception);
            }
        }
    }

    private Map<String, Object> tree(FacesContext context, UIComponent component) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("class", component.getClass().getName()); node.put("family", component.getFamily());
        node.put("renderer", component.getRendererType()); node.put("id", component.getId());
        node.put("clientId", component.getClientId(context)); node.put("transient", component.isTransient());
        node.put("literalMarkup", component instanceof UIOutput && component.isTransient()
            && "jakarta.faces.Text".equals(component.getRendererType())
            && Boolean.FALSE.equals(component.getAttributes().get("escape"))
            && component.getValueExpression("value") == null);
        Map<String, String> expressions = new LinkedHashMap<>();
        for (String name : List.of("value", "rendered", "binding", "required", "disabled", "immediate")) {
            ValueExpression expression = component.getValueExpression(name);
            if (expression != null) { expressions.put(name, expression.getExpressionString()); }
        }
        node.put("expressions", expressions);
        if (component instanceof UIData) {
            UIData data = (UIData) component;
            node.put("rowVariable", data.getVar()); node.put("firstRow", data.getFirst());
            node.put("rows", data.getRows()); node.put("rowIndex", data.getRowIndex());
        }
        if (component instanceof ValueHolder) {
            jakarta.faces.convert.Converter converter = ((ValueHolder) component).getConverter();
            node.put("converter", converter == null ? null : converter.getClass().getName());
            Map<String, Object> options = new LinkedHashMap<>();
            if (converter instanceof NumberConverter) {
                NumberConverter number = (NumberConverter) converter;
                options.put("type", number.getType()); options.put("pattern", number.getPattern());
                options.put("locale", number.getLocale().toLanguageTag());
                options.put("currencyCode", number.getCurrencyCode()); options.put("currencySymbol", number.getCurrencySymbol());
                options.put("integerOnly", number.isIntegerOnly()); options.put("grouping", number.isGroupingUsed());
                options.put("minimumFractionDigits", number.getMinFractionDigits()); options.put("maximumFractionDigits", number.getMaxFractionDigits());
                options.put("minimumIntegerDigits", number.getMinIntegerDigits()); options.put("maximumIntegerDigits", number.getMaxIntegerDigits());
            } else if (converter instanceof DateTimeConverter) {
                DateTimeConverter date = (DateTimeConverter) converter;
                options.put("type", date.getType()); options.put("pattern", date.getPattern());
                options.put("locale", date.getLocale().toLanguageTag()); options.put("timezone", date.getTimeZone() == null ? null : date.getTimeZone().getID());
                options.put("dateStyle", date.getDateStyle()); options.put("timeStyle", date.getTimeStyle());
            }
            node.put("converterOptions", options);
        }
        if (component instanceof ActionSource2) {
            ActionSource2 action = (ActionSource2) component;
            node.put("action", action.getActionExpression() == null ? null : action.getActionExpression().getExpressionString());
            node.put("immediate", action.isImmediate());
            node.put("actionListeners", Arrays.stream(action.getActionListeners()).map(this::listenerType).toList());
            node.put("actionListenerExpressions", Arrays.stream(action.getActionListeners()).map(listener -> listenerExpressions(context, listener)).toList());
            node.put("actionListenerImplementations", Arrays.stream(action.getActionListeners()).map(listener -> listener.getClass().getName()).toList());
        }
        if (component instanceof UIInput) {
            UIInput input = (UIInput) component;
            node.put("required", input.isRequired()); node.put("valid", input.isValid());
            node.put("localValueSet", input.isLocalValueSet()); node.put("submittedValue", input.getSubmittedValue());
            node.put("converter", input.getConverter() == null ? null : input.getConverter().getClass().getName());
            List<Map<String, Object>> validators = new ArrayList<>();
            Arrays.stream(input.getValidators()).forEach(validator -> {
                Map<String, Object> data = new LinkedHashMap<>(); data.put("class", validator.getClass().getName());
                if (validator instanceof LengthValidator) {
                    data.put("minimum", ((LengthValidator) validator).getMinimum());
                    data.put("maximum", ((LengthValidator) validator).getMaximum());
                }
                if (validator instanceof DoubleRangeValidator) {
                    data.put("minimum", ((DoubleRangeValidator) validator).getMinimum()); data.put("maximum", ((DoubleRangeValidator) validator).getMaximum());
                } else if (validator instanceof LongRangeValidator) {
                    data.put("minimum", ((LongRangeValidator) validator).getMinimum()); data.put("maximum", ((LongRangeValidator) validator).getMaximum());
                } else if (validator instanceof RegexValidator) {
                    data.put("pattern", ((RegexValidator) validator).getPattern());
                }
                validators.add(data);
            });
            node.put("validators", validators);
            node.put("valueChangeListeners", Arrays.stream(input.getValueChangeListeners()).map(this::listenerType).toList());
            node.put("valueChangeListenerExpressions", Arrays.stream(input.getValueChangeListeners()).map(listener -> listenerExpressions(context, listener)).toList());
        }
        Map<String, Object> facets = new TreeMap<>();
        component.getFacets().forEach((name, facet) -> facets.put(name, tree(context, facet)));
        node.put("facets", facets);
        node.put("children", component.getChildren().stream().map(child -> tree(context, child)).toList());
        return node;
    }

    private List<String> listenerExpressions(FacesContext context, Object listener) {
        Set<String> expressions = new TreeSet<>();
        if (listener instanceof StateHolder) {
            collectExpressions(((StateHolder) listener).saveState(context), expressions);
        }
        if (listener.getClass().getName().equals("com.sun.faces.facelets.tag.jsf.core.SetPropertyActionListenerHandler$SetPropertyListener")) {
            // Observe public expression objects in the framework's serialization
            // graph. This includes literal values and uses no private fields.
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (ObjectOutputStream stream = new ObjectOutputStream(bytes) {
                    { enableReplaceObject(true); }
                    @Override
                    protected Object replaceObject(Object object) {
                        collectExpressions(object, expressions);
                        return object;
                    }
                }) { stream.writeObject(listener); }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to inspect property listener bindings", exception);
            }
        }
        return List.copyOf(expressions);
    }

    private void collectExpressions(Object state, Set<String> expressions) {
        if (state instanceof ValueExpression) { expressions.add(((ValueExpression) state).getExpressionString()); }
        else if (state instanceof MethodExpression) { expressions.add(((MethodExpression) state).getExpressionString()); }
        else if (state instanceof Object[]) {
            for (Object child : (Object[]) state) { collectExpressions(child, expressions); }
        }
    }

    private String listenerType(Object listener) {
        String implementation = listener.getClass().getName();
        if (Set.of("com.sun.faces.taglib.jsf_core.SetPropertyActionListenerImpl",
                "com.sun.faces.facelets.tag.jsf.core.SetPropertyActionListenerHandler$SetPropertyListener").contains(implementation)) {
            return "jakarta.faces.SetPropertyActionListener";
        }
        if (!Set.of("com.sun.faces.facelets.tag.jsf.core.ActionListenerHandler$LazyActionListener",
                "com.sun.faces.facelets.tag.jsf.core.ValueChangeListenerHandler$LazyValueChangeListener").contains(implementation)) {
            return implementation;
        }
        // Mojarra's Serializable lazy listener stores its declared type as a TC_STRING.
        // Read that public serialization contract without private-field reflection or invocation.
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream stream = new ObjectOutputStream(bytes)) { stream.writeObject(listener); }
            byte[] serialized = bytes.toByteArray();
            List<String> types = new ArrayList<>();
            for (int index = 0; index < serialized.length - 3; index++) {
                if (serialized[index] == 0x74) {
                    try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(serialized, index + 1, serialized.length - index - 1))) {
                        String candidate = stream.readUTF();
                        if (candidate.matches("org\\.sakaiproject\\.[A-Za-z0-9_.$]+")) { types.add(candidate); }
                    } catch (IOException notStringToken) {
                        // A byte with this value inside another serialized token is not TC_STRING.
                    }
                }
            }
            if (types.size() != 1) { throw new IllegalStateException("Ambiguous serialized lazy listener type"); }
            return types.get(0);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect serializable listener", exception);
        }
    }
}
