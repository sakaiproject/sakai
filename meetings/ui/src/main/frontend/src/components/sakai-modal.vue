<template>
  <div>
    <div
      @click="show"
      style="width: fit-content"
    >
      <slot name="activator"> </slot>
    </div>

    <div
      class="modal"
      ref="modal"
      tabindex="-1"
      :aria-labelledby="`modal-${this.uid}-title`"
      aria-hidden="true"
      v-on="{
        'show.bs.modal': $emit('modal:show'),
        'hide.bs.modal': $emit('modal:hide')
      }"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 
              class="modal-title"
              :id="`modal-${this.uid}-title`"
            >
              {{ title }}
            </h5>
            <sakai-button
              data-bs-dismiss="modal"
              text="Close"
              :textHidden="true"
              :clear="true"
              :circle="true"
            >
              <template #append>
                <sakai-icon iconkey="close" />
              </template>
            </sakai-button>
          </div>
          <div v-if="$slots.body" class="modal-body">
            <slot name="body"></slot>
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer"> </slot>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// eslint-disable-next-line
import { Modal } from "bootstrap";
import SakaiButton from "./sakai-button.vue";
import SakaiIcon from "./sakai-icon.vue";
import uidMixin from "../mixins/uid-mixin.js";

export default {
  components: { SakaiButton, SakaiIcon },
  mixins: [uidMixin],
  props: {
    title: {
      type: String,
      default: "Modal title",
    },
    open: {
      type: Boolean,
      default: false
    }
  },
  mounted() {
    this.modal = new Modal(this.$refs.modal);
    if(this.open) {
      this.show();
    }
  },
  watch: {
    open() {
      this.toggle();
    }
  },
  methods: {
    toggle() {
      this.modal.toggle();
    },
    show() {
      this.modal.show();
    },
    hide() {
      this.modal.hide();
    }
  }
};
</script>

<style scoped lang="scss">
#meetings-tool {
.modal-content {
  background-color: var(--sakai-background-color-1);
}
.modal-header {
  border-bottom: none !important;
}
.modal-footer {
  border-top: none !important;
}
}
</style>
