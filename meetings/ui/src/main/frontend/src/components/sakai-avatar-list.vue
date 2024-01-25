<template>
  <div class="avatar-list">
    <div class="d-flex gap-1 my-2">
      <sakai-avatar
        v-for="user in shownUsers"
        :key="user.userid"
        :userId="user.userid"
        :text="user.text"
        :userName="user.name"
        :size="avatarsize"
        :aria-hidden="true"
      />
    </div>
    <span class="sr-only">{{ srText }}</span>
  </div>
</template>
<script>
import sakaiAvatar from "./sakai-avatar.vue";
import i18nMixn from "../mixins/i18n-mixn.js";

export default {
  components: { sakaiAvatar },
  mixins: [i18nMixn],
  data() {
    return {
      i18nProps: "card"
    };
  },
  props: {
    userlist: { type: Array, required: true },
    avatarsize: { type: Number },
    length: { type: Number },
  },
  computed: {
    shownUsers() {
      let maxAvatars = Math.round(this.length);
      if (maxAvatars && this.userlist.length > maxAvatars) {
        let hidden = this.userlist.length - (maxAvatars - 1);
        let shown = this.userlist.slice(0, maxAvatars - 1);
        let plus = { text: "+" + hidden };
        shown.push(plus);
        return shown;
      } else {
        return this.userlist;
      }
    },
    srText() {
      let text = this.i18n.availableParticipants;
      if(this.shownUsers.length > 0){
        this.shownUsers.forEach((element, index) => {
          text += `${
            element.name
              ? element.name
              : element.text
              ? this.i18n.and_x_more?.replace('{}', element.text.replace("+", ""))
              : element.userid
              ? element.userid
              : "error"
          }${index <= this.shownUsers.length - 2 ? "," : ""} `;
        });
      }
      return text;
    },
  },
};
</script>
