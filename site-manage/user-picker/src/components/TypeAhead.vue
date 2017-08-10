<!--
https://github.com/yuche/vue-strap
The MIT License (MIT)

Copyright (c) 2015 yuche

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-->
<template>
  <div :class="{open:showDropdown}">
    <!-- Used for positioning the spinner -->
    <div style="position: relative">
      <input class="form-control" autocomplete="off" ref="search"
             v-model="val"
             :disabled="disabled"
             :placeholder="placeholder"
             :type.once="type"
             @blur="finished"
             @keydown.down.prevent="down"
             @keydown.enter="hit"
             @keydown.esc="reset"
             @keydown.up.prevent="up"
      />
      <span class="glyphicon glyphicon-refresh form-control-feedback glyphicon-refresh-animate"
            v-bind:class="{invisible: !loading} "></span>
    </div>
    <ul class="dropdown-menu" ref="dropdown">
      <li v-for="(item, i) in items" :class="{active: isActive(i)}">
        <a @mousedown.prevent="hit" @mousemove="setActive(i)">
          <component :is="templateComp" :item="item"></component>
        </a>
      </li>
      <li v-if="trimmed" class="text-center">
        <span>…</span>
      </li>
    </ul>
  </div>
</template>

<script>
  import { delayer } from './utils/utils.js'
  import axios from 'axios'

  var DELAY = 300
  export default {
    mounted () {
      // The last one added gets the focus
      this.$refs.search.focus()
    },
    props: {
      async: {type: String},
      data: {type: Array},
      delay: {type: Number, default: DELAY},
      disabled: {type: Boolean, default: false},
      asyncKey: {type: String, default: null},
      limit: {type: Number, default: 8},
      matchCase: {type: Boolean, default: false},
      matchStart: {type: Boolean, default: false},
      // Don't start typeahead until we have this many characters.
      minLength: {type: Number, default: 0},
      onHit: {
        type: Function,
        default (item) { return item }
      },
      placeholder: {type: String},
      template: {type: String},
      type: {type: String, default: 'text'},
      value: {type: String, default: ''}
    },
    data () {
      return {
        // Used to track if this is a complete and so prevent an lookup on setting the value.
        asign: '',
        showDropdown: false,
        noResults: true,
        current: 0,
        items: [],
        loading: false,
        trimmed: false,
        val: this.value
      }
    },
    computed: {
      templateComp () {
        return {
          template: typeof this.template === 'string' ? '<span>' + this.template + '</span>' : '<strong v-html="item"></strong>',
          props: {item: {default: null}}
        }
      }
    },
    watch: {
      val (val, old) {
        this.$emit('input', val)
        if (val !== old && val !== this.asign) {
          if (val.length >= this.minLength) {
            this.__update.start()
          } else {
            this.cancel()
          }
        }
      },
      value (val) {
        // If set from calling code we don't want to be doing a lookup
        this.asign = val
        if (this.val !== val) { this.val = val }
      }
    },
    methods: {
      setItems (data) {
        if (this.async) {
          this.items = this.asyncKey ? data[this.asyncKey] : data
          this.trimmed = this.items.length > this.limit
          this.items = this.items.slice(0, this.limit)
        } else {
          this.items = (data || []).filter(value => {
            if (typeof value === 'object') { return true }
            value = this.matchCase ? value : value.toLowerCase()
            var query = this.matchCase ? this.val : this.val.toLowerCase()
            return this.matchStart ? value.indexOf(query) === 0 : value.indexOf(query) !== -1
          })
          this.trimmed = this.items.length > this.limit
          this.items = this.items.slice(0, this.limit)
        }
        this.showDropdown = this.items.length > 0
      },
      finished () {
        this.cancel()
        this.$emit('finished')
      },
      cancel () {
        this.showDropdown = false
        this.__update.cancel()
        if (this.__loading) {
          this.__loading.cancel()
        }
      },
      setValue (value) {
        this.asign = value
        this.val = value
        this.items = []
        this.loading = false
        this.showDropdown = false
      },
      reset () { this.setValue(null) },
      setActive (index) { this.current = index },
      isActive (index) { return this.current === index },
      hit (e) {
        // When we are showing the dropdown we handle internally, otherwise we should push the event back up
        // to allow handling by the calling code
        if (this.showDropdown) {
          e.stopPropagation()
          let item = this.items[this.current]
          let value = this.onHit(item, this)
          this.setValue(value)
          this.$emit('selected', item)
        }
      },
      up () {
        if (this.current > 0) { this.current-- } else { this.current = this.items.length - 1 }
      },
      down () {
        if (this.current < this.items.length - 1) { this.current++ } else { this.current = 0 }
      }
    },
    created () {
      this.__update = delayer(() => {
        if (!this.val) {
          this.reset()
          return
        }
        this.asign = ''
        this.loading = true
        if (this.async) {
          if (this.__loading) {
            this.__loading.cancel()
          }
          var source = axios.CancelToken.source()
          this.__loading = source
          axios.get(this.async + this.val, {cancelToken: source.token}).then(response => {
            const data = response.data
            this.setItems(data)
          }).catch((error) => {
            console.log(error)
          }).then(() => {
            this.loading = false
          })
        } else if (this.data) {
          this.loading = false
          this.setItems(this.data)
        }
      }, 'delay', DELAY)
      this.__update.start()
    }
  }
</script>

<style>
  .dropdown-menu > li > a {
    cursor: pointer;
  }

  .form-control-feedback.glyphicon {
    /* This lifts it above the focused input and so allows it to be used for AJAX progress.
       The extra class on the selector allows it to override the order mismatch issue in Sakai
     */
    z-index: 3;
  }

  .glyphicon-refresh-animate {
    -animation: spin 1.4s infinite linear;
    -webkit-animation: spinWebkit 1.4s infinite linear;
    -moz-animation: spinMoz 1.4s infinite linear;
  }

  @-webkit-keyframes spinWebkit {
    from {
      -webkit-transform: rotate(0deg);
    }
    to {
      -webkit-transform: rotate(360deg);
    }
  }

  @keyframes spinMoz {
    from {
      transform: scale(1) rotate(0deg);
    }
    to {
      transform: scale(1) rotate(360deg);
    }
  }

  @keyframes spin {
    from {
      transform: scale(1) rotate(0deg);
    }
    to {
      transform: scale(1) rotate(360deg);
    }
  }
</style>
