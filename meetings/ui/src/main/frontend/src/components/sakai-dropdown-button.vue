<template>
  <div class="dropdown">
    <SakaiDropdown :items="items" :menuid="id" ref="dropdown">
      <template #activation>
        <SakaiButton
          @click="handleClick"
          aria-haspopup="true"
          :aria-controls="id"
          :aria-expanded="expanded ? 'true' : 'false'"
          :text="text"
          :clear="clear"
          :link="link"
          :circle="circle"
          :textHidden="textHidden"
          :primary="primary"
          :disabled="disabled"
        >
          <template #prepend>
            <slot name="prepend" />
          </template>
          <template #append>
            <slot name="append">
              <SakaiIcon
                class="ms-1"
                :iconkey="expanded ? 'chevronUp' : 'chevronDown'"
              />
            </slot>
          </template>
        </SakaiButton>
      </template>
    </SakaiDropdown>
  </div>
</template>

<script>
import { v4 as uuid } from "uuid";
import SakaiIcon from "./sakai-icon.vue";
import SakaiDropdown from "./sakai-dropdown.vue";
import SakaiButton from "./sakai-button.vue";
export default {
  components: {
    SakaiIcon,
    SakaiButton,
    SakaiDropdown,
  },
  props: {
    items: {
      type: Array,
      default: () => [
        {
          id: 0,
          icon: "error",
          string: "emty",
          route: "/",
        },
      ],
    },
    text: {
      type: String,
      default: "Button",
    },
    clear: {
      type: Boolean,
      default: false,
    },
    circle: {
      type: Boolean,
      default: false,
    },
    link: {
      type: Boolean,
      default: false,
    },
    textHidden: {
      type: Boolean,
      default: false,
    },
    primary: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      id: "drop",
      selectedId: null,
      expanded: false,
    };
  },
  computed: {},
  methods: {
    handleClick() {
      this.expanded = !this.expanded;
    },
  },
  created() {
    this.id += uuid().substring(8, 13); //random id '-34F4'
  },
  mounted() {
    this.$watch("$refs.dropdown.expanded", (newValue) => {
      this.expanded = newValue;
    });
  },
};
</script>
