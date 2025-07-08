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

## JavaScript Development
- **Modern JavaScript**: Use clean, standard modern JavaScript where possible
- **Legacy Code**: The codebase contains legacy frameworks and libraries that should be gradually modernized
- **jQuery**: Update jQuery code to modern JavaScript when making changes, if the changes are minimal
- **ES6+**: Prefer ES6+ features (arrow functions, template literals, destructuring, etc.)
- **Modular Code**: Write modular, reusable JavaScript components
- **Avoid Global Scope**: Minimize use of global variables and functions

## Code Style Guidelines
- **Commit Messages**: `<issue key> <component> <brief description>` (e.g., `SAK-12345 Assignments add option x`)
- **Indentation**: Maintain consistent format (tabs/spaces) as in existing files
- **kebab-case**: Prefer kebab-case for values of HTML class and id attributes
- **Internationalization**: Ensure code supports different languages
- **Accessibility**: Follow accessibility best practices
- **Changes**: Make minimal changes, only modifying lines needed for the fix/feature
- **Single Issue**: One issue per pull request when possible
- **Tests**: Include tests where sensible/possible
- **Java Version**: Java 17 for trunk (Java 11 was used for Sakai 22 and Sakai 23)
- **Pull Request Workflow**: "Squash and Merge" for single issues, "Rebase and Merge" for multiple issues

## Push Notifications

### Platform Support
- **Android**: Full Web Push support through Chrome, Firefox, and Edge browsers
- **iOS**: Web Push support requires Progressive Web App (PWA) installation (iOS 16.4+)
- **Desktop**: Full Web Push support across all major browsers

### Implementation Architecture
- **Backend**: Uses Web Push Protocol with VAPID authentication in `UserMessagingServiceImpl.java`
- **Frontend**: Browser-specific handling in `sakai-push-utils.js`
- **Service Worker**: Background push handling in `sakai-service-worker.js`

### Platform-Specific Requirements

#### iOS Safari (iOS 16.4+)
- **PWA Required**: Users must add site to home screen before push notifications work
- **Web App Manifest**: Must serve `/manifest.json` with PWA configuration
- **User Interaction**: Permission requests require direct user interaction
- **Protocol**: Uses standard Web Push Protocol (no APNs certificate needed)

#### Android Chrome/Firefox
- **No PWA Required**: Can request push permissions immediately
- **Background Support**: Full background push notification support
- **Protocol**: Uses Web Push Protocol with VAPID keys
- **FCM Integration**: Chrome uses Firebase Cloud Messaging behind the scenes

#### Desktop Browsers
- **Universal Support**: Chrome, Firefox, Safari, Edge all support Web Push
- **Standard Implementation**: Uses Web Push Protocol with VAPID authentication

### Development Guidelines
- **Permission Timing**: Request push permissions after user engagement, not immediately on page load
- **Progressive Enhancement**: Detect browser capabilities and adjust UX accordingly
- **Graceful Degradation**: Provide fallbacks for unsupported browsers
- **Token Management**: Handle subscription updates and expirations properly
- **Internationalization**: PWA installation messages use `sakai-notifications.properties` for translations
