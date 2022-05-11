<script setup>
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup
</script>

<template>
<div id="meetings-tool">
  <div v-if="errorMessage" class="sak-banner-error">
    {{ errorMessage }}
  </div>
  <router-view @showError="showErrorMessage" @clearError="clearErrors"></router-view>
</div>
</template>

<script>
export default {
    data() {
        return {
            errorMessage: null,
        };
    },
    methods: {
        showErrorMessage(message) {
            this.errorMessage = message;
        },
        clearErrors() {
            this.errorMessage = null;
        }
    },
    watch:{
        $route (to, from){
            this.clearErrors();
        }
    }
}
</script>

<style>
@import "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css";
</style>
