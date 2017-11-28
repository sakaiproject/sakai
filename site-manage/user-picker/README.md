# user-picker

> A user picker for Sakai

There's no point in doing a progress meter as we ideally want the add to be done in a bulk action because saving a site
in Sakai is a heavyweight operation and if the user is adding 50 people we want just one transaction ideally. This has
a separate input box for each user so if we want to start displaying more information such as photos we have the space
to do it.

## TODOS

- ~~i18n~~ (working)
- Error handling
- ~~Help text~~ (done)
- bootstrap exclusion for Sakai. (done using loader that isn't yet in upstream)
- cleanup components
- documentation.
- filter roles to only include applicable ones.
- ~~bulk adding to site.~~
- ~~notifications.~~
- ~~handling not allowing external users.~~
- ~~handling not allowing add users.~~
- ~~failure detection on adding and stay on page.~~
- Support setting firstname/surname when adding the person (current format is	email address,first name,last name)
- tool tips.
- correctly index people (todo example)
- ~~CSRF. Need to look for header and set on all requests.~~

## Build Setup

In development you can just use npm and the key URLs are proxied through to Sakai.

### npm

``` bash
# install dependencies
npm install

# serve with hot reload at localhost:8080
npm run dev

# build for production with minification
npm run build

# build for production and view the bundle analyzer report
npm run build --report

# run unit tests
npm run unit

# run e2e tests
npm run e2e

# run all tests
npm test
```

### mvn

``` bash
# install war
mvn install sakai:deploy
```

For detailed explanation on how things work, checkout the [guide](http://vuejs-templates.github.io/webpack/) and [docs for vue-loader](http://vuejs.github.io/vue-loader).
