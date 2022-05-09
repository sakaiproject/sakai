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

export default {
  components: { sakaiAvatar },
  data: function () {
    return {
      i18n: {
        and: "and",
        more: "more",
        connected_participants: "Connected participants: ",
      },
    };
  },
  props: {
    userlist: { type: Array, required: true },
    avatarsize: { type: Number },
    length: { type: Number },
  },
  computed: {
    shownUsers: function () {
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
    srText: function () {
      let text = this.i18n.connected_participants;
      this.shownUsers.forEach((element, index) => {
        text += `${
          element.name
            ? element.name
            : element.text
            ? `${this.i18n.and} ${element.text.replace("+", "")} ${
                this.i18n.more
              }.`
            : "error"
        }${index <= this.shownUsers.length - 2 ? "," : ""} `;
      });
      return text;
    },
  },
};
</script>
