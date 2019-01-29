/**************************************************************************************
 *                    Connection Poll Javascript                                       
 *************************************************************************************/
function ConnectionPoll($message) {
    this.PING_INTERVAL = 1000*30; // 30 seconds
    this.PING_TIMEOUT = 1000*10; // 10 seconds
    this.PING_URL = "/direct/gbng/ping";

    this.$message = $message;

    this.poll();
};


ConnectionPoll.prototype.poll = function() {
  var self = this;

  self._interval = setInterval(function() {
    self.ping();
  }, self.PING_INTERVAL);
};


ConnectionPoll.prototype.ping = function() {
  $.ajax({
    type: "GET",
    url: this.PING_URL,
    data: {
      auto: true // indicate that the request is automatic, not from a user action
    },
    timeout: this.PING_TIMEOUT,
    cache: false,
    success: $.proxy(this.onSuccess, this),
    error: $.proxy(this.onTimeout, this)
  });
};

ConnectionPoll.prototype.onTimeout = function() {
  this.$message.show();
};

ConnectionPoll.prototype.onSuccess = function() {
  this.$message.hide();
};

$(document).ready(function() {
  new ConnectionPoll($("#gbConnectionTimeoutFeedback"));
});
