# Sakai UI | sui

## Built with

- lit
- [vite](https://vitejs.dev/)
  - [rollup](https://rollupjs.org/guide/en)
- [bootstrap](https://getbootstrap.com)

## NPM Scripts Examples

Using sakai-docker

copy to tomcat

```
cd webcomponents
TARGET=/home/mg/dev/profmikegreene/docker-sakai-builder/work/tomcat/deploy/webapps/webcomponents npm run --prefix=tool/src/main/frontend wc:copy-target
```

a development build

```
TARGET=/home/mg/dev/profmikegreene/docker-sakai-builder/work/tomcat/deploy/webapps/webcomponents VITE_BUILD=dev npm run --prefix=tool/src/main/frontend wc:build
```

### sui-table

- [tabulator](http://tabulator.info/)

## Links

- <https://open-wc.org/docs/linting/eslint-plugin-lit-a11y/overview/>
- <https://modern-web.dev/guides/test-runner/getting-started/>
- <https://github.com/webcomponents/gold-standard/wiki>
- <https://developers.google.com/web/fundamentals/web-components/best-practices>
- <https://github.com/vitejs/awesome-vite>
- <https://open-wc.org/guides>
- <https://modern-web.dev/guides/>
- <https://a11y-style-guide.com/style-guide/>
- <https://www.w3.org/WAI/tips/designing/>

## To Explore

- <https://testing-library.com/>
- [CRUX Sakai proposed ui updates](https://docs.google.com/presentation/d/1vkTOTFxQuTGqOL3sSIuN3nRfpo-gHqA4_R1gHP4O270/edit#slide=id.g39912ff795_0_343)
