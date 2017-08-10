import Vue from 'vue'
import Router from 'vue-router'
import AddUsers from '@/components/AddUsers'
import { getQueryVariable } from '@/components/utils/utils'

Vue.use(Router)

// Get the siteId from the actual query string or have a default
let siteId = getQueryVariable('siteId') || '7aa15d62-be3c-4414-b871-fb7e613591ff'

export default new Router({
  routes: [
    {
      path: '/',
      name: 'AddUsers',
      component: AddUsers,
      props: (route) => ({ siteId: siteId })
    }
  ]
})
