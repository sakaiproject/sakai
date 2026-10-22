# CLAUDE.md for Attendance Tool

## Build Commands
- Build entire project: `mvn clean install`
- Build a specific module: `mvn clean install -pl [module-name]` (e.g., `-pl api`, `-pl impl`, `-pl tool`)
- Run single test: `mvn test -Dtest=TestClassName#testMethodName`
- Deploy to Sakai: Copy generated WAR files to Sakai's webapps directory

## Code Style Guidelines
- Java code uses standard Java naming conventions (CamelCase for classes, lowerCamelCase for methods/variables)
- Lombok annotations (`@Data`, `@NoArgsConstructor`, etc.) used extensively for model classes
- Copyright headers required on all files with ECL-2.0 license
- Serializable required for model classes with serialVersionUID
- Wicket framework for UI components, follow component-based architecture
- Use existing patterns for new features (see similar classes for reference)
- Always handle null values safely in getters (return default values instead of null)
- Document classes with JavaDoc, including author tags
- Organize imports with no wildcards, grouped by project/third-party/Java standard libraries
