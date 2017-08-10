<i18n>
  {
  "en": {
  "add.users": "Add Users",
  "people": "People",
  "people.help": "Type in some users, press return to get enter another user. Paste multiple users, one per line.",
  "role": "Role",
  "role.help": "Choose a role for the new participants",
  "lookup": "Lookup",
  "bulk.lookup": "Bulk Lookup",
  "notify.new.members": "Notify new members",
  "add": "Add",
  "cancel": "Cancel",
  "add.label": "Add another person",
  "bulk.label": "All multiple person entry",

  "user_invalid_id": "Invalid email or user ID.",
  "user_exists": "Cannot create as user already exists.",
  "no_permission_create": "You don't have permission to create new users.",
  "user_email_bad_domain": "The domain used in email address isn't permitted.",
  "user_not_found": "Failed to find user matching this.",
  "user_external_not_allowed": "External users cannot be added to this site.",
  "user_already_member": "User is already a member of this site.",
  "unknown_error": "An unknown error occured."
  }
  }
</i18n>

<template>
  <div class="add-users">
    <h3>{{ $t('add.users') }}</h3>
    <fieldset :disabled="disabled">
      <h4>{{ $t('people') }}
        <small>
          <span class="glyphicon glyphicon-question-sign" aria-hidden="true"
                @click="showPeopleHelp = !showPeopleHelp"></span>
        </small>
      </h4>
      <transition name="fade">
        <p class="help-block" v-if="showPeopleHelp">{{ $t('people.help') }}</p>
      </transition>
      <input_person_search v-for="(search, index) in searches" :key="search.itemCounter" :initialSearch="search.query"
                           :id="index"
                           :added="search.added" :status="search.status" :message="search.message"
                           :siteId="siteId" @change="updateUser($event, index)" :disabled="searches.length <= 1"
                           v-on:new="insertItem($event, index)" v-on:removeItem="removeItem(index)">
      </input_person_search>
    </fieldset>
    <fieldset :disabled="disabled">
      <div class="btn-group pull-right">
        <button class="btn btn-default" @click="insertItem()" :title="$t('add.label')">
          <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
        </button>
        <button class="btn btn-default" v-bind:class="{ 'active': showBulk }" @click="showBulk = !showBulk"
                :title="$t('bulk.label')">
          <span class="glyphicon glyphicon-paste" aria-hidden="true"></span>
        </button>
      </div>
    </fieldset>
    <fieldset v-if="showBulk" :disabled="disabled">
      <label>{{ $t('bulk.lookup') }}</label>
      <div class="form-group">
        <textarea class="form-control" v-model="text"></textarea>
      </div>
      <div class="pull-right btn-group">
        <button class="btn" @click="lookupUsers">{{ $t('lookup') }}</button>
      </div>
    </fieldset>
    <fieldset :disabled="disabled">
      <div class="form-group">
        <label class="control-label">
          <h4>
            {{ $t('role') }}
            <small>
          <span class="glyphicon glyphicon-question-sign" aria-hidden="true"
                @click="showRoleHelp = !showRoleHelp"></span>
            </small>
          </h4>
        </label>
        <transition name="fade">
          <p class="role-block" v-if="showRoleHelp">{{ $t('role.help') }}</p>
        </transition>

        <role :siteId="siteId" v-model="role"></role>
      </div>
    </fieldset>
    <fieldset :disabled="disabled">
      <div class="checkbox">
        <label>
          <input v-model="notify" type="checkbox">
          {{ $t('notify.new.members') }}
        </label>
      </div>

      <button type="button" class="btn" @click="addUsers">{{ $t('add') }}</button>
      <button type="button" class="btn" @click="abort">{{ $t('cancel') }}</button>
    </fieldset>
  </div>
</template>

<script>
  import InputPersonSearch from '@/components/InputPersonSearch'
  import Roles from '@/components/Roles'
  import 'url-search-params-polyfill'
  import axios from 'axios'

  var itemCounter = 0

  export default {
    name: 'add-users',
    components: {
      input_person_search: InputPersonSearch,
      role: Roles
    },
    props: {
      url: {
        type: Function,
        default: function (siteId, roleId) {
          return `/sakai-ws/rest/add-user/adds`
        }
      },
      siteId: {
        type: String,
        required: true
      }
    },
    methods: {
      insertItem: function (newSearch = '', index = this.searches.length) {
        this.searches.splice(index + 1, 0, {query: newSearch, itemCounter: itemCounter++})
      },
      removeItem: function (index) {
        this.searches.splice(index, 1)
      },
      addUsers: function () {
        // Disable everything.
        this.disabled = true
        var params = new URLSearchParams()
        params.append('roleId', this.role)
        params.append('siteId', this.siteId)
        params.append('notify', this.notify)
        this.searches.forEach((e) => { params.append('userIds', (e.added) ? '' : e.user) })

        axios.post(this.url(this.siteId, this.roleId), params).then((response) => {
          let error = false
          for (let i = 0; i < this.searches.length; i++) {
            if (response.data[i] && response.data[i].userId) {
              this.searches[i].added = this.searches[i].added || response.data[i].added
              this.searches[i].message = (response.data[i].added) ? null : this.$t(response.data[i].message)
              error = error || !response.data[i].added
            }
          }
          // Don't sort the items, just move the failues to the bottom this means people have a better idea where
          // the items they added are end up in the page.
          for (var i = 0, end = this.searches.length; i < end; i++) {
            if (!this.searches[i].added) {
              this.searches.push(...this.searches.splice(i, 1))
              end--
              i--
            }
          }
          if (!error) {
            this.abort()
          }
        }).catch((response) => {
        }).then(() => {
          this.disabled = false
        })
      },
      lookupUsers: function () {
        if (this.text) {
          const parts = this.text.split(/[,\n]/)
          for (const part of parts) {
            // These need to be pushed back up to component tree.
            if (part && part.length > 0) {
              this.insertItem(part)
            }
          }
          this.insertItem()
        }
        this.text = ''
        this.showBulk = false
        // Add a new empty box at the end
      },
      updateUser: function (value, index) {
        this.searches[index].user = value
      },
      abort: function () {
        window.location.href = './finished'
      }
    },
    data () {
      return {
        /** Show people help */
        showPeopleHelp: false,
        /** Show role help */
        showRoleHelp: false,
        /** Show the bulk user add input */
        showBulk: false,
        role: undefined,
        notify: true,
        text: undefined,
        disabled: false,
        searches: [{
          itemCounter: itemCounter++,
          query: '',
          message: undefined,
          user: undefined,
          /** Have we found and added a user here */
          added: false
        }]
      }
    }
  }
</script>

<!-- Add " scoped
      " attribute to limit CSS to this component only -->
<style scoped>
  h1, h2 {
    font-weight: normal;
  }

  ul {
    list-style-type: none;
    padding: 0;
  }

  li {
    display: inline-block;
    margin: 0 10px;
  }

  .add-users {
    /* Don't allow the boxes to get too large */
    max-width: 60em;
  }

  a {
    color: #42b983;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }

  .fade-enter, .fade-leave-to {
    opacity: 0
  }
</style>
