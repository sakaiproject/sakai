/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Hook some Sakai-specific operations into the normal ApplicationContext
 * refresh cycle: read component manager configuration files, give creators of
 * PostProcessor objects (e.g., SakaiProperties) a chance to do their work,
 * and load a few central components before the rest.
 */
@Slf4j
public class SakaiApplicationContext extends GenericApplicationContext {
	private String[] initialSingletonNames;
	private String[] configLocations;

	public SakaiApplicationContext() {
		super(new DefaultListableBeanFactory());
	}
	
	/**
	 * Load component manager configurations. A more normal hook for this is
	 * the "refreshBeanFactory" method, but it's declared final by GenericApplicationContext.
	 */
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.prepareBeanFactory(beanFactory);
		try {
			loadBeanDefinitions(beanFactory);
		} catch (IOException e) {
			throw new ApplicationContextException("I/O error parsing XML document for application context [" + getDisplayName() + "]", e);
		}
	}

	protected void loadBeanDefinitions(ConfigurableListableBeanFactory beanFactory) throws IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)beanFactory);
		beanDefinitionReader.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		if (configLocations != null) {
			beanDefinitionReader.loadBeanDefinitions(configLocations);
		}
	}
	
	/**
	 * Before post-processing, load beans which have declared that they want to add post-processors
	 * dynamically.
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		invokePostProcessorCreators(beanFactory);
		super.postProcessBeanFactory(beanFactory);
	}

	@Override
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		if (initialSingletonNames != null) {
			for (int i = 0; i < initialSingletonNames.length; i++)	{
				beanFactory.getBean(initialSingletonNames[i]);
			}
		}
		super.finishBeanFactoryInitialization(beanFactory);
	}

	protected void onRefresh() throws BeansException {
		if (log.isDebugEnabled()) {
			generateBeanDiGraph();
			detectCycles();
		}
		super.onRefresh();
	}

	/**
	 * Add bean-created post processors.
	 * @param beanFactory
	 */
	public void invokePostProcessorCreators(ConfigurableListableBeanFactory beanFactory) {
		String[] postProcessorCreatorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessorCreator.class, false, false);
		for (int i = 0; i < postProcessorCreatorNames.length; i++) {
			BeanFactoryPostProcessorCreator postProcessorCreator = (BeanFactoryPostProcessorCreator)beanFactory.getBean(postProcessorCreatorNames[i]);
			for (BeanFactoryPostProcessor beanFactoryPostProcessor : postProcessorCreator.getBeanFactoryPostProcessors()) {
				addBeanFactoryPostProcessor(beanFactoryPostProcessor);
			}
		}
	}

	/**
	 * @param initialSingletonNames initial configuration beans to load, behaving
	 * as universal "dependsOn" targets
	 */
	public void setInitialSingletonNames(String[] initialSingletonNames) {
		this.initialSingletonNames = initialSingletonNames;
	}

	/**
	 * @param configLocations file paths of XML bean definition files to load before refresh
	 */
	public void setConfigLocations(String[] configLocations) {
		this.configLocations = configLocations;
	}

	/**
	 * Prints a dependency graph of beans in the application context to the console and writes it to a DOT file.
	 * @param ctx the application context to analyze
	 */
	public void generateBeanDiGraph() {
		log.info("=== PRE-REFRESH BEAN DEPENDENCY GRAPH ===");

		StringBuilder dot = new StringBuilder();
		dot.append("digraph SakaiKernelBeans {\n");
		dot.append("    rankdir=LR;\n");
		dot.append("    node [shape=box];\n\n");

		for (String beanName : getBeanDefinitionNames()) {
			BeanDefinition bd = getBeanDefinition(beanName);

			// collect all dependencies for this bean
			Set<String> deps = new LinkedHashSet<>();

			// explicit depends-on
			if (bd.getDependsOn() != null) {
				deps.addAll(Arrays.asList(bd.getDependsOn()));
			}

			// constructor-arg refs
			for (ConstructorArgumentValues.ValueHolder holder :
					bd.getConstructorArgumentValues().getGenericArgumentValues()) {
				if (holder.getValue() instanceof RuntimeBeanReference ref) {
					deps.add(ref.getBeanName());
				}
			}

			// property refs
			for (PropertyValue pv : bd.getPropertyValues().getPropertyValueList()) {
				if (pv.getValue() instanceof RuntimeBeanReference ref) {
					deps.add(ref.getBeanName());
				}
			}

			// add to dot graph
			String safeName = formatBeanName(beanName);
			for (String dep : deps) {
				dot.append("    ")
						.append(safeName)
						.append(" -> ")
						.append(formatBeanName(dep))
						.append(";\n");
			}
		}

		dot.append("}");

		try {
			Files.writeString(Path.of("sakai-bean-graph.dot"), dot.toString());
			log.info(">>> Graph written to sakai-bean-graph.dot");
			log.info(">>> Paste contents at: https://dreampuf.github.io/GraphvizOnline");
		} catch (IOException e) {
			log.warn(">>> Failed to write graph: {}", e.toString());
		}

		log.info("=== END PRE-REFRESH GRAPH ===");
	}

	private String formatBeanName(String name) {
		return "\"" + name + "\"";
	}

	public void detectCycles() {
		log.info("=== CYCLE DETECTION ===");

		// build adjacency map
		Map<String, Set<String>> graph = new LinkedHashMap<>();

		for (String beanName : getBeanDefinitionNames()) {
			BeanDefinition bd = getBeanDefinition(beanName);
			Set<String> deps = new LinkedHashSet<>();

			if (bd.getDependsOn() != null) {
				deps.addAll(Arrays.asList(bd.getDependsOn()));
			}
			for (PropertyValue pv : bd.getPropertyValues().getPropertyValueList()) {
				if (pv.getValue() instanceof RuntimeBeanReference ref) {
					deps.add(ref.getBeanName());
				}
			}
			for (ConstructorArgumentValues.ValueHolder holder :
					bd.getConstructorArgumentValues().getGenericArgumentValues()) {
				if (holder.getValue() instanceof RuntimeBeanReference ref) {
					deps.add(ref.getBeanName());
				}
			}
			graph.put(beanName, deps);
		}

		// DFS cycle detection
		Set<String> visited = new HashSet<>();
		Set<String> inStack = new HashSet<>();
		List<String> cycle = new ArrayList<>();

		for (String bean : graph.keySet()) {
			if (dfsCycleCheck(bean, graph, visited, inStack, cycle)) {
				log.warn("CYCLE DETECTED: {}", String.join(" -> ", cycle));
				cycle.clear();
			}
		}

		log.info("=== END CYCLE DETECTION ===");
	}

	private boolean dfsCycleCheck(String bean,
								  Map<String, Set<String>> graph,
								  Set<String> visited,
								  Set<String> inStack,
								  List<String> cycle) {
		if (inStack.contains(bean)) {
			cycle.add(bean);
			return true;
		}
		if (visited.contains(bean)) return false;

		visited.add(bean);
		inStack.add(bean);
		cycle.add(bean);

		for (String dep : graph.getOrDefault(bean, Collections.emptySet())) {
			if (dfsCycleCheck(dep, graph, visited, inStack, cycle)) {
				return true;
			}
		}

		inStack.remove(bean);
		cycle.remove(cycle.size() - 1);
		return false;
	}
}
