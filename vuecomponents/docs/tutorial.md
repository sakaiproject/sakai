# Tutorial - Vue webcomponents in Sakai

This tutorial is made to help you with your first steps, working with vue components in Sakai. Improvements and feedback is very welcome.

## Getting Started with vue

If you dont have prior knowledge about vue, taking the [official vue tutorial](https://vuejs.org/tutorial/) is highly recommended. There you will learn the basics, in an interactive way. This is an vue 3 tutuorial, but the Options API (selected by default) works the same way as in vue 2 (what we are using currently).

For further documentation and to learn more, check out the [official guide](https://v2.vuejs.org/v2/guide/) and the [API reference](https://v2.vuejs.org/v2/api/). Also, there are a lot of third party resources out there, to learn more about vue, so take a look around and pick, what fits your learning style.

## Creating a new compomponent

To author vue componets for Sakai we use the Single File Component format (SFC). That means, to get started you will need to create a new `.vue` file in `vuecomponents/tool/src/main/frontend/src/components`. The component defined there, will later be globally available in sakai, automatically adding the `sakai-`
prefix: `avatar.vue` => `<sakai-avatar></sakai-avatar>` / `<sakai-avatar/>`

### Delevopment in Storybook

You might have a better development experience using storybook for developing your component and you can document it right away. Take a look at the [storybook tutorial](storybook.md)!

### Imporve workfolw in Sakai

It is possible to deploy your vue componentes directly to your tomcat on file save. To take advantage of this workflow, you will either need to set the TOMCAT_HOME enviornment variable or set the `tomcatPath`variable to point to your tomcat directory directly in the following script: `vuecomponents/tool/src/main/frontend/build-dev.mjs`.

To start auto-deploying your component changes you will need to run `npm run build-dev` (from frontend directory).

## Troubleshooting

Having problems, working on a component? Take a look at [the troobleshooting document](troubleshooting.md), to find a solution or to get ahead of proplems that might come up in the future. Please consider adding to it, if you run into problems.

## Options API vs Composition API

As the Options API the only one available in vue 2, we should consider it as the default. Though there is an way to use the vue 3 composition API in vue 2 aswell, if that is desired before an vue 3 upgrade, we should add it to the tutorial.
