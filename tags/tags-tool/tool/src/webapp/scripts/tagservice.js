
function TagServiceTag(uuid, csrf_token) {
  this.uuid = uuid;
  this.csrf_token = csrf_token;

  this.$tagContent = $('#tag-container-content');

  if (this.uuid) {
    this.showTag();
  }
};



