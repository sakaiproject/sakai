# JDK 21 Migration Plan

1. **Freeze the Current Baseline**  
   - Record the existing build status on JDK 17 (`mvn install`) and capture failing modules/tests as a reference point.  
   - Document the active Java-specific settings in `master/pom.xml` (e.g., `<sakai.jdk.version>17</sakai.jdk.version>`, compiler args, toolchains) so regressions can be traced.  
   - Audit CI pipelines and deployment scripts to understand where JDK 17 is provisioned, including Docker images, Ansible playbooks, and hosted runners.

2. **Provision JDK 21 Tooling Across Environments**  
   - Install Temurin (or chosen distribution) JDK 21 for all developers, CI agents, and deployment servers; add version managers (SDKMAN!, asdf) instructions.  
   - Refresh container images (`docker/`, `deploy/`) and local scripts (`mvnw`, `setenv.sh`) to download/export JDK 21.  
   - Update documentation (`README.md`, `Sakai Development Guide`, onboarding docs) so new contributors default to JDK 21.

3. **Revise Maven Configuration for Java 21**  
   - Change the `sakai.jdk.version` property in `master/pom.xml` from 17 to 21 and align any other source/target properties in child POMs.  
   - Upgrade build plugins to Java-21-ready releases: `maven-compiler-plugin`, `maven-surefire-plugin`, `maven-failsafe-plugin`, `maven-enforcer-plugin`, `animal-sniffer`, `jacoco`, `spotbugs`, `frontend-maven-plugin`, etc.  
   - Revisit toolchain definitions or CI environment variables (`JAVA_HOME`, `MAVEN_OPTS`) to guarantee Maven invokes the 21 toolchain.  
   - Ensure reproducible build settings (build timestamp, enforcer rules) remain compatible after property changes.

4. **Eliminate Reliance on JDK Internal APIs**  
   - Catalogue each `--add-exports` and `--add-opens` flag in the shared compiler/test configurations and locate the code paths that trigger them.  
   - Refactor offending code to use supported APIs (e.g., `java.nio` channels, public reflection helpers) or supported third-party libraries; only retain `--add-exports` where absolutely unavoidable and proven safe on JDK 21.  
   - Run `jdeps`/`jdeprscan` against core modules to confirm no other illegal-access or removed APIs remain.

5. **Upgrade Critical Frameworks and Libraries**  
   - **Spring Framework core (LOE: High · Priority: P0):** move from Spring Framework 5.3.x (OSS EOL: June 2023) to 6.2.x+ so the platform stays on a supported line with native Java 21 runtime support. This requires adopting Jakarta EE 9+ namespaces across tools and aligning Spring Data, Web, and integration starters with the new baseline.  
   - **Spring Security platform (LOE: High · Priority: P0):** upgrade the security stack to Spring Security 6.2+ so authentication/authorization layers are certified on Java 21, and so the project inherits the Spring Framework 6.1/6.2 baselines. Plan for method-security annotation refactors, new defaults (deferred `SecurityContext` saving), and Project Reactor alignment for reactive modules.  
   - **Authentication & SAML (LOE: High · Priority: P0):** replace end-of-life `spring-security-saml2-core` 1.0.10.RELEASE and CAS 3.6.x usage in `login/login-tool` with Spring Security’s supported SAML 2.1+ service-provider features (OpenSAML 4 baseline). Expect to rework XML metadata handling, bootstrapping, and certificate rollover, and to refit any custom filters around the new APIs.  
   - **Hibernate / persistence (LOE: High · Priority: P0):** migrate from Hibernate ORM 5.6.x (limited support, only certified through Java 18) to Hibernate 6.5+ for official Java 21 compatibility, new SQL generation, and updated Jakarta Persistence APIs. Account for query syntax differences, identifier handling, and batch fetching changes.  
   - **Jakarta Faces / JSF stack (LOE: Medium · Priority: P1):** inventory JSF 2.3-era code (Mojarra/MyFaces, OmniFaces) still on `javax.*`. Target Jakarta Faces 4.1 (Java 17+ minimum, validated on Java 21) and refresh component libraries to Jakarta EE 10+ so templated tools (e.g., legacy JSF portlets) continue to run after the JDK uplift.  
   - **Wicket modules (LOE: Medium · Priority: P1):** converge the mix of Wicket 6/8/9 modules on Apache Wicket 10.x, which is built for Java 17+ and tested on Java 21. Use the OpenRewrite migration recipes to modernize component packages and remove legacy servlet dependencies before the JDK switch.  
   - **Velocity templating (LOE: Medium · Priority: P1):** retire Apache Velocity 1.6.4 (impacted by CVE-2020-13936) and move templated code to Velocity 2.3+ or alternate templating engines compatible with Java 21. Review any sandbox customizations and ensure template inputs remain validated after the upgrade.  
   - **Legacy XML toolchain (LOE: Medium · Priority: P1):** inventory modules that relied on `simple-xml` 2.7.x and Apache XMLBeans 4.0.0 (`samigo/samlite-impl`). `simple-xml` has been unmaintained since 2014 and only targets Java 8-era APIs, while XMLBeans 5.3.1 (released March 2024) is actively maintained and runs on JDK 8+. Immediate playbook:  
      1. Generate a full usage report (`rg "simple-xml"` and `rg "xmlbeans"`) and capture deserialization entry points plus tests that guard them.  
      2. Prototype migrating one representative `simple-xml` consumer (e.g., `kernel/api/src/main/java/org/sakaiproject/emailtemplateservice/api/model/EmailTemplate.java`) to Jackson data-binding; measure serialization diffs and add regression fixtures. Leverage the existing Jackson BOM defined in `master/pom.xml` (`com.fasterxml.jackson.core:jackson-databind`, `jackson-dataformat-xml`, `jackson-module-jaxb-annotations`) and mirrored in `deploy/pom.xml` so we stay on the central `${sakai.jackson.version}` property. *(Status: EmailTemplateService converted to Jackson and `org.simpleframework` dependencies removed from kernel and deploy modules. SamLite now generates QTI via Jackson and the xmlbeans dependency is removed with regression coverage in place.)*  
      3. Document why XMLBeans exists: `samigo/samlite-impl` uses XMLBeans-generated IMS QTI 1.2 bindings (`org.imsglobal.xsd.imsQtiasiv1P2.*`) to parse Samigo Lite assessment uploads. Capture the schema source, generation script (if any), and the parser entry points (`SamLiteServiceImpl.parse`). Evaluate whether Jackson’s XML module plus JAXB-annotated POJOs (generated via `xjc`) can replace the XMLBeans layer long-term.  
      4. Upgrade XMLBeans to 5.3.1 in `samigo/samlite-impl/pom.xml`, regenerate the IMS QTI bindings, and run `mvn -pl samigo/samlite-impl test` plus a Samigo Lite import smoke test to verify compatibility.  
      5. Draft a rollout checklist (schema regeneration scripts, regression test suites, QA sign-off) so remaining modules can follow the same pattern without blocking the wider JDK migration.  
   - **Application container & shared services (LOE: Medium · Priority: P1):** confirm the Tomcat baseline supports the latest Jakarta Servlet APIs required by upgraded frameworks (Tomcat 10.1+/11 for Java 21) and update shared services (email, content hosting, job scheduler) that rely on container-specific APIs.  
   - **Security-adjacent libraries (LOE: Medium · Priority: P2):** modernize Nimbus JOSE, jjwt, BouncyCastle, Apache Commons, and other cryptography/IO packages to releases tested on Java 21, coordinating with platform security reviews.  
   - **GenericDAO abstraction (LOE: High · Priority: P1):** audit usages of `org.sakaiproject.genericdao` across hierarchy, signup, polls, entitybroker, and other tools to ensure compatibility with Hibernate 6.x. The custom DAO layer relies on Hibernate 5-era session APIs; plan either to adapt it to Hibernate 6.5 or to migrate high-value modules onto Spring Data repositories. Document extension points and identify modules that can be retired to reduce maintenance surface.  
   - Track dependency upgrades in a spreadsheet or issue tracker to coordinate retesting across tools, highlighting blockers discovered during module test passes.

   Recommended sequencing: finish the P0 items (Spring core, Spring Security, SAML/login, Hibernate) before advancing to the P1 framework refreshes (Jakarta Faces, Wicket, Velocity, legacy XML toolchain, Tomcat, GenericDAO) and finally the P2 security-adjacent library updates.

6. **Modernize Test Tooling**  
   - Update Mockito (≥5.x), JUnit (vintage vs. Jupiter), and other test libraries to versions certified on Java 21.  
   - Replace or refactor usages of PowerMock 2.0.9 and outdated bytecode instrumentation that break under the new JVM.  
   - Confirm integration/e2e harnesses (e.g., Selenium, Playwright) and service test containers build on JDK 21 images.

7. **Rebuild and Validate Legacy Modules**  
   - For each major tool (Assignments, Gradebook, Samigo, etc.) run module-specific builds/tests to surface Java 21 regressions early.  
   - Pay special attention to legacy frameworks (JSF, RSF, Velocity) where bytecode or reflection hacks are common.  
   - Triage compilation/runtime failures, open migration tickets, and prioritize fixes that unblock shared modules first (kernel, webcomponents, assignment).

8. **Verify Frontend Build Parity**  
   - Ensure that Node/npm tooling launched via Maven (`frontend-maven-plugin`) continues to run when Maven itself uses Java 21.  
   - Re-run `npm run lint`, `npm run bundle`, and `npm run analyze` in `webcomponents/tool/src/main/frontend` to confirm no toolchain regressions.  
   - Update any Java-based bundlers or analyzers (e.g., Closure Compiler, esbuild Java wrappers) that may embed older ASM bytecode libraries.

9. **Revise Deployment & Runtime Configurations**  
   - Update production/staging server provisioning (Tomcat JVM flags, systemd services, container orchestration manifests) to point to JDK 21 paths and module system options.  
   - Revisit JVM flags, garbage collectors, and memory settings; evaluate using the default G1 enhancements or migrating to ZGC/Generational ZGC where appropriate.  
   - Validate integration with external services (email, SAML, LTI) under the new JVM through smoke tests.

10. **Comprehensive QA & Release Readiness**  
    - Run full test suites (`mvn install`, targeted `mvn test -Dtest=...`) on JDK 21, plus functional, performance, and accessibility checks.  
    - Capture metrics versus the JDK 17 baseline (startup time, memory, throughput) to report improvements or regressions.  
    - Update release notes, upgrade guides, and training materials; communicate timelines and required actions to stakeholders.  
    - Freeze a release candidate, perform staged rollouts, and monitor production logs closely after the cutover.
