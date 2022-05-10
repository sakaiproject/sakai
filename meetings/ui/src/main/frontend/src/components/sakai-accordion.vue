<template>
  <div class="accordion" :class="flush ? 'accordion-flush' : ''" :id="id">
    <slot></slot>
  </div>
</template>

<script>
import { v4 as uuid } from "uuid";
export default {
  name: "sakai-accordion",
  data() {
    return {
      id: "item",
    };
  },
  props: {
    flush: { Type: Boolean, default: false },
    independent: { Type: Boolean, default: false },
  },
  created: function () {
    this.id = "accordion" + uuid().substring(8, 13); //random id '-34F4'
  },
};
</script>
