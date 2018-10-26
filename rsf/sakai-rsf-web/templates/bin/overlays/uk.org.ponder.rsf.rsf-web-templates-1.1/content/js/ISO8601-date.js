/** Standard ISO 8601 format dates for Javascript - taken from 
 * http://delete.me.uk/2005/03/iso8601.html
 * Originally written by Paul Sowden
 * Acquired by Antranig Basman 03/11/2006
 * Corrected to ALWAYS supply one or more fractional second digits in format 6 as
 * per the actual standard http://www.w3.org/TR/NOTE-datetime
 * Adjusted further to ignore ALL timezone information as this may not reliably
 * be processed in a Javascript environment.
 */

var FixedDate = {
  fromDate: function(date) {
    var togo = new Object();
    togo.fullYear = Number(date.getFullYear());
    togo.month = Number(date.getMonth() + 1);
    togo.day = Number(date.getDate());
    togo.hours = Number(date.getHours());
    togo.minutes = Number(date.getMinutes());
    togo.seconds = Number(date.getSeconds());
    return togo;
    },

  parseISO8601: function (string) {
    
    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
        "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
        "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";
    var d = string.match(new RegExp(regexp));

    var offset = 0;
    var date = new Object();
    date.fullYear = Number(d[1]);
    date.month = Number(d[3]);
    date.day = Number(d[5]);
    date.hours = Number(d[7]);
    date.minutes = Number(d[8]);
    date.seconds = Number(d[10]);
    date.milliseconds = Number(d[12]);
    return date;
  },


  renderISO8601: function (fixeddate, format, offset) {
    /* accepted values for the format [1-6]:
     1 Year:
       YYYY (eg 1997)
     2 Year and month:
       YYYY-MM (eg 1997-07)
     3 Complete date:
       YYYY-MM-DD (eg 1997-07-16)
     4 Complete date plus hours and minutes:
       YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
     5 Complete date plus hours, minutes and seconds:
       YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
     6 Complete date plus hours, minutes, seconds and a decimal
       fraction of a second
       YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
    */
    if (!format) { var format = 6; }
    if (!offset) {
        var offset = 'Z';
    } else {
        var d = offset.match(/([-+])([0-9]{2}):([0-9]{2})/);
        var offsetnum = (Number(d[2]) * 60) + Number(d[3]);
        offsetnum *= ((d[1] == '-') ? -1 : 1);
    }

    var zeropad = function (num, width) {
      if (!width) width = 2;
      var numstr = (num == undefined? "" : num.toString());
      return "00000".substring(5 - width + numstr.length) + numstr;
      }
      
    var str = "";
    str += fixeddate.fullYear;
    if (format > 1) { str += "-" + zeropad(fixeddate.month); }
    if (format > 2) { str += "-" + zeropad(fixeddate.day); }
    if (format > 3) {
        str += "T" + zeropad(fixeddate.hours) +
               ":" + zeropad(fixeddate.minutes);
    }

    if (format > 4) { 
      str += ":" + zeropad(fixeddate.seconds); 
      if (format == 6) {
        str += "." + zeropad(fixeddate.milliseconds, 3);
        }
      }

    if (format > 3) { str += offset; }
    return str;
}
};