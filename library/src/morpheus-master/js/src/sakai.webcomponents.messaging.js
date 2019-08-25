var sakai = sakai || {};

sakai.sendMessageToComponent = function (id, message) {
  document.getElementById(id).messageReceived(message);
};
