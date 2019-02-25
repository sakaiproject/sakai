(function() {
    this.SakaiReminder = function() {
    }

    SakaiReminder.prototype = {
        init: function() {
        },

        getAll: function() {
            return JSON.parse(localStorage.getItem(portal.user.id + '-sakaiReminder'));
        },

        new: function(text) {
            var predictions = JSON.parse(localStorage.getItem(portal.user.id + '-sakaiReminder'));
            if (predictions == null) predictions = [text];
            else if(!predictions.includes(text)) predictions.push(text);
            localStorage.setItem(portal.user.id + '-sakaiReminder', JSON.stringify(predictions));
        },

        removeAll: function() {
            return localStorage.removeItem(portal.user.id + '-sakaiReminder');
        },
    }
})();

