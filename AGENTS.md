# Sakai Development Guide

## Build Commands
- `mvn install` - Build the project
- `mvn clean install sakai:deploy -Dmaven.tomcat.home=/path/to/tomcat` - Deploy to Tomcat
- `mvn test -Dtest=TestClassName` - Run a single test class
- `mvn test -Dtest=TestClassName#testMethodName` - Run a single test method

## Frontend Commands
- `cd webcomponents/tool/src/main/frontend` - Change to frontend directory
- `npm run lint` - Run ESLint for JavaScript
- `npm run bundle` - Bundle JS files using esbuild
- `npm run analyze` - Run lit-analyzer for static type checking

## Architecture

### Kernel
- **Core Services**: The Kernel provides core services that should be used by all tools
- **User Management**: Services for fetching and managing User objects
- **Email Service**: Centralized email sending functionality
- **Authorization**: Security and permission services
- **Content Hosting**: File and resource management
- **Session Management**: User session handling
- **Service Location**: Use the Kernel's service location mechanisms to access these services
- **New Services**: New core services should be added to the Kernel, not to individual tools

### Web Components
- **Strategic Direction**: Web components are the strategic direction for Sakai frontend development
- **Lit Library**: Web components are built using the Lit library (lit.dev)
- **Component Creation**: Create reusable, encapsulated components with their own styling and behavior
- **Shadow DOM**: Leverage Shadow DOM for style encapsulation
- **Custom Elements**: Define custom HTML elements for Sakai-specific functionality
- **Integration**: Web components can be integrated into both new and existing tools

## Java Frameworks
- **Legacy Frameworks**: The codebase contains multiple Java frameworks from different eras
- **Spring**: Crucial framework used throughout the codebase for dependency injection, MVC, and services
- **Hibernate**: Critical ORM framework for database interactions, essential for future development
- **JSF 2.3**: JavaServer Faces is used in many tools
- **Wicket**: Used in several tools for component-based web development
- **ThymeLeaf**: Preferred template engine for new development
- **Apache Velocity**: Used in older parts of the codebase
- **RSF (Reasonable Server Faces)**: Avoid using this legacy framework for new development
- **Framework Selection**: For new tools, prefer Spring MVC/Boot with Hibernate and ThymeLeaf
- **Modernization**: When making substantial changes to a tool, consider migrating to more modern frameworks

## UI Framework
- **Bootstrap**: Bootstrap 5.2 is the preferred UI framework for styling
- **Responsive Design**: Ensure all UI components work across different screen sizes
- **Components**: Leverage Bootstrap 5 components for consistent UI/UX

- **Modern JavaScript**: Target evergreen browsers; assume ES2022+ features and browser APIs like `fetch` keepalive are present.
- **Lean Code Paths**: Avoid legacy branches, UA sniffing, or fallbacks unless a specific evergreen gap is documented.
- **jQuery**: Replace jQuery with modern DOM APIs when touching code; new work should not add jQuery dependencies.
- **Modular Code**: Compose Lit components, ES modules, and encapsulated helpers; keep state local and explicit.
- **No Global Side Channels**: Prefer module scope or class fields; expose intentional APIs instead of incidental globals.
- **Lit Component Encapsulation**: Internal reactive state stays prefixed with `_` and is only surfaced through getters/setters when required.

## Code Style Guidelines
- **Commit Messages**: `<issue key> <component> <brief description>` (e.g., `SAK-12345 Assignments add option x`)
- **Indentation**: Maintain consistent format (tabs/spaces) as in existing files
- **kebab-case**: Prefer kebab-case for values of HTML class and id attributes
- **Internationalization**: Ensure code supports different languages
- **Accessibility**: Follow accessibility best practices
- **Changes**: Make minimal changes, only modifying lines needed for the fix/feature
- **Single Issue**: One issue per pull request when possible
- **Tests**: Include tests where sensible/possible
- **UI Flow Changes**: When changing user-visible UI flows (navigation, forms, submissions, dialogs, or interactive behavior), add or update a Playwright test in `sakai-e2e-tests-java/src/test/java/org/sakaiproject/e2e/tests` that covers the changed flow. If a Playwright test is not practical, document why in the PR description.
- **Java Version**: Java 17 for trunk (Java 11 was used for Sakai 22 and Sakai 23)
- **Pull Request Workflow**: "Squash and Merge" for single issues, "Rebase and Merge" for multiple issues
- **No `var` in Java**: Do not use local variable type inference (`var`) in Java code. Always declare explicit types (e.g., `List<String> names = new ArrayList<>();` not `var names = new ArrayList<String>();`).
   - Enforced: The build runs a Checkstyle rule during `mvn validate` to fail on `var` usages. To bypass in emergencies only, run with `-Dcheckstyle.skip=true` (not recommended for commits).

## Push Notifications
- **Support Matrix**: Design for current evergreen builds of Chrome, Edge, Safari, and Firefox; no legacy branches.
- **Protocol**: Use Web Push with VAPID via `UserMessagingServiceImpl.java`; rely on the service worker in `sakai-service-worker.js`.
- **UX Timing**: Gate permission prompts behind explicit user interaction; avoid auto-prompts.
- **State Management**: Reconcile subscriptions proactively and keep localization strings in `sakai-notifications.properties`.
