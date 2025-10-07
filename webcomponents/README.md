# Sakai Web Components

This module hosts a set of web components which are in use across several Sakai tools. We're using a
combination of **[Lerna](https://lerna.js.org/)** and
**[NPM Workspaces](https://docs.npmjs.com/cli/v7/using-npm/workspaces)** to manage the monorepo -
check out the configurations in tool/src/main/frontend. All the package dependencies are managed
by Lerna and are hosted in the top level node\_modules directory, not in each individual package.

**[Web Test Runner](https://modern-web.dev/docs/test-runner/overview/)** is used for testing and
test files are hosted in each package's **test** directory. Most of the tests make use of
**[FetchMock](https://www.wheresrhys.co.uk/fetch-mock/)** to mock network calls and some of the
tests use **[Sinon](https://sinonjs.org/)** to mock browser APIs.
**[ESBuild](https://esbuild.github.io/)** is used for creating the browser bundles used in various
locations in the Sakai codebase and linting is carried out by **[ESLint](https://eslint.org/)**. If
you want to lint all the components, just run `npm run lint` from  tool/src/main/frontend. If you
only want to lint a single component, run `npm run lint` inside the package directory.

# Creating a new component

*All paths will be from tool/src/main/frontend, for brevity.*

1. Create a new package directory in the **packages** directory. Use your new component's tag as the
   package name.
2. Create your component's js file in the **src** directory.
3. Create your test file and supporting data in the **test** directory.
4. Create your i18n strings, if you need to return any, in the **test/i18n** directory.
5. Write some test cases.
6. Write the code and test it!
7. Optional, add some api code to Sakai's webapi module to supply data to your component.
8. Take a look at the tool/src/main/frontend/bundle-entry-points files to see how to bundle your
   component.

# Testing

Web Test Runner comes with a set of testing utilities baked in, such as
**[Chai](https://www.chaijs.com/)** assertions. Take a look at one of the existing packages to see
how to use them in your own tests. Testing is simple with Web Test Runner so you should make good
use of it in your component. **[TDD](https://en.wikipedia.org/wiki/Test-driven_development)** is a
great way of working on your component. Write some tests to exercise your yet-to-be-written code,
then write the code. That way you'll write nicer components for the end users since you've already
"used" them in your tests.

Coverage is intentionally omitted from our testing setup, as it's debatable
if, as developers, we should be focusing on coverage metrics. We should instead be focusing on the
most commong pathways our users will be taking through our component, and edge cases later.
