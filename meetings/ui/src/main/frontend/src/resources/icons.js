const prependBaseClass = (icons, baseClass) => {
  return Object.entries(icons).map((iconentry) => {
    let mod = iconentry;
    mod[1] = baseClass + " " + mod[1];
    return mod;
  });
};

const icons_fontawsome = {
  all: "fa-th-large",
  bell: "fa-bell",
  calendar: "fa-calendar-o",
  chevron_down: "fa-chevron-down",
  chevron_up: "fa-chevron-up",
  delete: "fa-trash",
  edit: "fa-pencil",
  error: "fa-ban",
  file_image: "fa-file-image-o",
  hourglass_emty: "fa-hourglass-o",
  link: "fa-link",
  maximize: "fa-arrows-alt",
  menu_kebab: "fa-ellipsis-v",
  permissions: "fa-lock",
  plus: "fa-plus",
  question: "fa-question",
  search: "fa-search",
  template: "fa-book",
  videocamera: "fa-video-camera",
  attachment: "fa-paperclip",
  chat: "fa-comments-o",
  close: "fa-times",
  live: "fa-circle-o",
  play: "fa-play",
  presentation: "fa-desktop",
  remove: "fa-trash"
};
const icons_bootstrap = {
};

const _fa = prependBaseClass(icons_fontawsome, "fa");
const _bi = prependBaseClass(icons_bootstrap, "bi");

export const fa = Object.fromEntries(_fa);
export const bi = Object.fromEntries(_bi);

export default { ...fa, ...bi };
