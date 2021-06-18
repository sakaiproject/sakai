# Sakai Web Components

This project hosts a set of cross cutting web components, in use across several Sakai tools. The
project can build Storybook to help with rapid prototyping, uses Karma, Jasmine and fetch-mock
for unit testing, and eslint for ... linting. We use the most excellent microframework,
(lit-element)[https://lit-element.polymer-project.org] for most of our web component development,
and lit-element in turn used lit-html as its rendering system. This gives use just enough binding
and highly performant dom updates.

## Storybook

To run Storybook and browse the components, the ones that have stories at least, first install Node
and NPM onto your machine from (here)[https://nodejs.org/en/download/]. Then:

    cd SAKAI_SRC/webcomponents/tool/src/main/frontend
    npm run storybook

## Testing

You should test your components. When you create a new component in the js tree, create a test in
the test tree. Take a look at the others in there to get an idea. Tests run in headless Chrome,
using the Karma test runner and Jasmine as the actual test framework. You can use fetch-mock to
mock your i18n strings, and also to mock any data your component fetches. To make your testing a
little quicker when not using maven to build the entire project, de-noding the bare paths using
Empathise happens in a separate package script:

    cd SAKAI_SRC/webcomponents/tool/src/main/frontend
    npm run test-setup

Now you can rerun your tests a lot quicker with just:

    npm run test

The tool's maven build does that for you, runs test-setup then test.

# Creating a new component

*All paths will be from SAKAI\_SRC/webcomponents/tool/src/main/frontend, for brevity.*

1. Create your component js file in the **js** directory.
2. Create your test file in the **test** directory.
3. Create your i18n strings, if you need to return any, in the **test/i18n** directory.
4. Create a stories file in the **stories** directory, if you want to enjoy some rapid dev
    using Storybook.

Try and write your component and test in parallel. That way you make it more likely you'll cover a
good proportion of your component's functionality.
