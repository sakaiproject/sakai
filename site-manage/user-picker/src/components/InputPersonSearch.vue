<template>
  <div class="input-person-search">
    <div class="form-group" :class="error" v-on:keydown.enter="$emit('new')">
      <!-- This is the last block element we can be relative to for the dropdown -->
      <div class="input-group dropdown">

        <typeahead async="/sakai-ws/rest/add-user/search?q=" v-model="search"
                   placeholder="username / email / name"
                   :minLength="3"
                   :limit="9"
                   :template="userTemplate"
                   :onHit="convertUser"
                   :disabled="added"
                   @selected="selectUser"
                   @finished="typeUser"
                   @paste.native.prevent="parsePaste"
                   @keydown.native.delete="removeText"
        >
        </typeahead>
        <span class="input-group-addon" v-if="status">
                <span class="glyphicon" :class="status" aria-hidden="true"></span>
                <span class="username" v-if="user">{{ user.displayId }}</span>
                <span>{{ lookupMessage }}</span>
        </span>
        <div class="input-group-btn">
          <button v-if="added" type="button" class="btn btn-success" disabled="disabled">
            <span class="glyphicon glyphicon-ok" aria-label="Success"></span>
          </button>
          <button v-else type="button" class="btn btn-danger" v-on:click="removeItem" :disabled="disabled">
            <span class="glyphicon glyphicon-trash" aria-label="Remove"></span>
          </button>
        </div>
      </div>
      <span class="help-block">{{ message }}</span>
    </div>
  </div>
</template>

<script>
  import 'bootstrap/dist/css/bootstrap.css'
  import TypeAhead from '@/components/TypeAhead'
  import axios from 'axios'

  export default {

    components: {'typeahead': TypeAhead},
    name: 'input_person_search',
    props: {
      initialSearch: {
        type: String
      },
      message: {
        type: String
      },
      userTemplate: {
        type: String,
        default: '{{ item.displayName }}'
      },
      siteId: {
        type: String
      },
      // Once it's been added it makes the component read only in effect
      added: {
        type: Boolean,
        default: false
      },
      async: {
        type: Function,
        default: function (siteId, userId) { return `/sakai-ws/rest/add-user/site?siteId=${siteId}&userId=${userId}` }
      },
      userLookup: {
        type: Function,
        default: function (search) { return ` /sakai-ws/rest/add-user/lookup?search=${search}` }
      },
      disabled: {
        type: Boolean,
        default: false
      },
      // Just helpful with debugging
      id: Number
    },
    computed: {
      status: function () {
        if (this.loading) {
          return 'glyphicon-refresh-animate glyphicon-repeat'
        } else if (this.already || this.badUser) {
          return 'glyphicon-exclamation-sign'
        } else if (this.user) {
          return 'glyphicon-plus-sign'
        } else if (this.search) {
          return 'glyphicon-question-sign'
        }
      },
      error: function () {
        if (this.message) {
          return 'has-error'
        } else if (this.added) {
          return 'has-success'
        } else {
          return ''
        }
      }
    },
    watch: {
      user: function (value) {
        if (value) {
          this.$emit('change', value.id)
        }
      },
      search: function (value) {
        this.lookupMessage = undefined
        // You can't
//        this.message = undefined
        if (this.lookedUp !== value) {
          this.user = undefined
          this.already = false
          this.badUser = false
        }
      }
    },
    methods: {
      parsePaste: function (e) {
        const clipData = e.clipboardData.getData('text/plain')
        // Split on comma or newline
        const parts = clipData.split(/[,\n]/).filter((part) => { return part.trim().length > 0 })
        this.$data.search = parts.splice(0, 1)[0]
        // Create a new trailing input as well
        if (parts.length > 0) {
          this.$emit('new')
        }
        for (const part of parts.reverse()) {
          this.$emit('new', part)
        }
      },
      removeItem: function (e) {
        this.$emit('removeItem')
      },
      removeText: function (e) {
        if (!this.search) {
          this.removeItem(e)
        }
      },
      typeUser: function () {
        if (this.search && this.lookedUp !== this.search) {
          let query = this.search
          let url = this.userLookup(query)
          this.lookupMessage = undefined
          this.__userLookup = axios.get(url).then(response => {
            const data = response.data
            if (data.user) {
              this.user = data.user
              this.checkSite(data.user.id)
            } else {
              // Need to check if can add new users
              // Need to validate it's an email
              if (query.indexOf('@') === -1 && data.allowAdd) {
                this.badUser = true
                this.lookupMessage = 'user not found'
              } else {
                this.lookupMessage = 'creating user'
              }
              this.$emit('change', query)
            }
          })
        }
      },
      selectUser: function (user) {
        // Save the value we looked up
        this.user = user
        // We can't use the model (this.search) as it isn't yet updated
        this.lookedUp = user.displayName
        this.checkSite(user.id)
      },
      checkSite: function (userId) {
        // Only check membership if we have a valid user ID.
        if (!userId) {
          return
        }
        this.loading = true
        let url = this.async(this.siteId, userId)
        var source = axios.CancelToken.source()
        this.__lookup = source
        axios.get(url, { cancelToken: source.token }).then(response => {
          const data = response.data
          this.already = (!!data.roleId)
          if (this.already) {
            this.lookupMessage = 'already a participant'
          }
        }).catch((error) => {
          console.log(error)
        }).then(() => {
          this.loading = false
        })
      },
      convertUser: function (data) { return data.displayName }
    },
    data () {
      return {
        // Are we doing an AJAX lookup.
        loading: false,
        // Already a member of the site.
        already: false,
        // Not an email
        badUser: false,
        // The selected user.
        user: undefined,
        // A message from attempting to lookup the user
        lookupMessage: undefined,
        // The value of the box.
        search: this.initialSearch

      }
    }
  }
</script>

