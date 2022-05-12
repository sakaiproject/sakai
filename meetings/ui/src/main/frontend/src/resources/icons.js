const prependBaseClass = (icons, baseClass) => {
  return Object.entries(icons).map((iconentry) => {
    let mod = iconentry;
    mod[1] = baseClass + " " + mod[1];
    return mod;
  });
};

const iconsFontawsome = {
  all: "fa-th-large",
  bell: "fa-bell",
  calendar: "fa-calendar-o",
  chevronDown: "fa-chevron-down",
  chevronUp: "fa-chevron-up",
  delete: "fa-trash",
  edit: "fa-pencil",
  error: "fa-ban",
  fileImage: "fa-file-image-o",
  hourglassEmty: "fa-hourglass-o",
  link: "fa-link",
  maximize: "fa-arrows-alt",
  menuKebab: "fa-ellipsis-v",
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
const iconsBootstrap = {
};

const _fa = prependBaseClass(iconsFontawsome, "fa");
const _bi = prependBaseClass(iconsBootstrap, "bi");

export const fa = Object.fromEntries(_fa);
export const bi = Object.fromEntries(_bi);

export default { ...fa, ...bi };
