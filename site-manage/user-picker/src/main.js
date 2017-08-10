// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import VueI18n from 'vue-i18n'
import App from './App'
import router from './router'
import portal from './sakai'

Vue.config.productionTip = false

Vue.use(VueI18n)

// At the moment the i18n code doesn't support falling back through locales
const locale = portal.locale.replace(/[-_].*/, '')

const i18n = new VueI18n({
  locale: locale,
  fallbackLocale: 'en'
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  i18n,
  template: '<App/>',
  components: { App }
})
