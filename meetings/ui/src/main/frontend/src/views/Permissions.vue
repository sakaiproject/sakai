<template>
  <div>
    <p>Here you will be able to set Permissions</p>
    <div class="d-flex mt-5">
      <SakaiButton
        text="Save"
        @click="handleSave"
        :primary="true"
        class="me-2"
      />
      <SakaiButton text="Cancel" @click="handleCancel" />
    </div>
  </div>
</template>

<script>
import SakaiButton from "../components/sakai-button.vue";

export default {
  components: {
    SakaiButton,
  },
  data() {
    return {};
  },
  methods: {
    handleSave: function () {
      this.$router.push({ path: "/" });
    },
    handleCancel: function () {
      this.$router.push({ path: "/" });
    },
  }
};
</script>
<style></style>
