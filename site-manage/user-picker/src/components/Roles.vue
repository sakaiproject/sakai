<i18n>
  {
  "en": {
    "loading": "Loading...",
    "error.load": "Failed to load roles."
  }

  }
</i18n>
<template>
  <select class="form-control" :value="value" :disabled="loading" @input="updateValue($event.target.value)">
      <option v-if="loading">{{ $t('loading') }}</option>
      <option v-else v-for="role in siteRoles">{{ role }}</option>
    </select>
</template>

<script>
  import axios from 'axios'

  export default {
    props: {
      siteId: {type: String},
      value: {type: String},
      roles: Array,
      url: {
        type: Function,
        default: function (siteId) { return `/sakai-ws/rest/add-user/roles?siteId=${siteId}` }
      }
    },
    created: function () {
      if (this.siteId) {
        this.loading = true
        axios.get(this.url(this.siteId)).then((response) => {
          const data = response.data
          if (data && data.length > 0) {
            var roleIds = data.map((role) => { return role.id })
            this.siteRoles = roleIds.sort()
            this.updateValue(this.siteRoles[0])
          }
        }).catch(() => {
          this.$emit('error', this.$t('error.load'))
        }).then(() => {
          this.loading = false
        })
      }
    },
    methods: {
      updateValue: function (value) {
        this.$emit('input', value)
      }
    },
    data () {
      return {
        /** Are we still loading the roles. */
        loading: false,
        /** The roles available in the site. */
        siteRoles: this.roles
      }
    }
  }
</script>
