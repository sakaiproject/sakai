window._ = function(msgid, interpolations) {
    /* dummy _ function for systems that don't want to use i18n */
    if (interpolations) {
        for (var id in interpolations) {
            var value = interpolations[id];
            var reg = new RegExp('\\\$\\\{' + id + '\\\}', 'g');
            msgid = msgid.replace(reg, ""+value);
        };
    };
    return msgid;
};
