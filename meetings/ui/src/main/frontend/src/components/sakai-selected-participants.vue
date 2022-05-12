<template>
  <div class="mt-3">
    <button
      class="wrapper p-3 w-100"
      role="button"
      data-bs-toggle="collapse"
      :data-bs-target="'#body' + id"
      :aria-expanded="open"
      :aria-controls="'body' + id"
    >
      <div :id="'header' + id" class="header d-flex gap-1">
        <span class="title">{{ title + " " }}</span>
        <span class="count">({{ users.length }}/{{ maxUsers }})</span>
        <sakai-icon
          iconkey="chevronUp"
          class="chevron ms-auto me-2"
        ></sakai-icon>
      </div>
      <div
        :id="'body' + id"
        class="body collapse show"
        :aria-labelledby="'header' + id"
      >
        <div class="mt-2"></div>
        <div class="d-flex flex-column gap-2">
          <div
            class="d-flex align-items-center"
            v-for="user in users"
            :key="user.userId"
          >
            <sakai-avatar
              aria-hidden="true"
              :userId="user.userId"
              size="30"
              class="me-2"
            />
            <div class="name">{{ user.userName }}</div>
          </div>
        </div>
      </div>
    </button>
  </div>
</template>

<style scoped lang="scss">
#meetings-tool {
.wrapper {
  background-color: var(--sakai-background-color-2);
  border-radius: 1rem;
  border: none;
}
.wrapper:not(.collapsed) .chevron {
  transform: rotate(-180deg);
}
.chevron {
  transition: transform 0.2s ease-in-out;
}
}
</style>

<script>
// eslint-disable-next-line
import { Collapse } from "bootstrap";
import { v4 as uuid } from "uuid";
import SakaiAvatar from "./sakai-avatar.vue";
import SakaiIcon from "./sakai-icon.vue";
export default {
  components: {
    SakaiAvatar,
    SakaiIcon,
  },
  props: {
    title: { Type: String, default: "" },
    users: { Type: Array, default: new Array() },
    maxUsers: { Type: Number },
  },
  created() {
    this.id = uuid().substring(8, 13); //random id '-34F4'
  },
};
</script>
