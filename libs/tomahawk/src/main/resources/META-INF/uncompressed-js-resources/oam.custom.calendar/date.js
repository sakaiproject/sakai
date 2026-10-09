/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//----------------------------------------------------------------------------
// This file contains a javascript implementation of most of the
// java.text.SimpleDateFormat class, written for the purposes of implementing
// the Apache MyFaces Tomahawk calendar control.
//
// This file defines a javascript class, org_apache_myfaces_dateformat_SimpleDateFormat.
// An instance of this class can be constructed, then used to parse strings
// into dates, and format dates into strings.
//
// Note that there is one difference from SimpleDateFormat in the formatting
// string pattern; this code optionally supports ISO-8601 weeks, and the JODA
// "xxxx" pattern for weekYear.
//
// This code is a "port" of java class org.apache.myfaces.dateformat.SimpleDateFormatter
// from Apache Myfaces Tomahawk, with the minimum number of changes needed to
// make the code work in javascript. See that class for further documentation
// about the code in this file. Any "fixes" needed in the code below should
// first be implemented (and tested) in the base java version.
//
// The main differences between the java code and this javascript are that
// in java a lot of methods are declared static to ensure they do not
// accidentally change the object state. But this is very inconvenient to
// do in javascript as static function names must include the "namespace"
// prefix, eg org_apache_myfaces_dateformat_SimpleDateFormatter_foo().
// Instead, all these methods are implemented as non-static member methods.
// In addition, private methods have an underscore added to the start of
// the name. Note also that the constructor for a java.util.Date object takes
// a year relative to 1900, while javascript takes one relative to 0AD.
//----------------------------------------------------------------------------

// Constructor for DateFormatSymbols class
org_apache_myfaces_dateformat_DateFormatSymbols = function()
{
  this.eras = new Array('BC', 'AD');
  this.months = new Array(
    'January', 'February', 'March', 'April',
    'May', 'June', 'July', 'August', 'September', 'October',
    'November', 'December', 'Undecimber');
  this.shortMonths = new Array(
    'Jan', 'Feb', 'Mar', 'Apr',
    'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct',
    'Nov', 'Dec', 'Und');
  this.weekdays = new Array(
    'Sunday', 'Monday', 'Tuesday',
    'Wednesday', 'Thursday', 'Friday', 'Saturday');
  this.shortWeekdays = new Array(
    'Sun', 'Mon', 'Tue',
    'Wed', 'Thu', 'Fri', 'Sat');
  this.ampms = new Array('AM', 'PM');
  this.zoneStrings = new Array(new Array(0, 'long-name', 'short-name'));
  var threshold = new Date();
  threshold.setYear(threshold.getYear()-80);
  this.twoDigitYearStart = threshold;
}

// Constructor for ParserContext class
org_apache_myfaces_dateformat_ParserContext = function(dow)
{
  this.newIndex=0;
  this.invalid = false;
  this.firstDayOfWeek = dow;
  this.ambigousYear=false;
  this.ambigousWeekYear=false;
  
  this.year=0;
  this.month=0;
  this.day=1;
  this.dayOfWeek=0;
  this.hour=0;
  this.hourAmpm;
  this.min=0;
  this.sec=0;
  this.ampm=0;
  
  this.weekYear=0;
  this.weekOfWeekYear=0;
}

// constructor for WeekDate class
org_apache_myfaces_dateformat_WeekDate = function(year, week)
{
  this.year = year;
  this.week = week;
}

// constructor for StringBuffer class
org_apache_myfaces_dateformat_StringBuffer = function(s)
{
  if (s == null)
    this.str = "";
  else
    this.str = s;

  var proto = org_apache_myfaces_dateformat_StringBuffer.prototype;    
  proto.append = function(s)
  {
    this.str = this.str + s;
  }

  proto.toString = function()
  {
    return this.str;
  }
}

// Constructor for SimpleDateFormatter class
org_apache_myfaces_dateformat_SimpleDateFormatter = function(pattern, symbols, firstDayOfWeek)
{
  this._construct(pattern, symbols, firstDayOfWeek);
}

// static initialisation block; requires constructor for SimpleDateFormatter
// to run first in order to create the prototype object that this block
// attaches methods and constants to.
{
var proto = org_apache_myfaces_dateformat_SimpleDateFormatter.prototype;

proto.MSECS_PER_SEC = 1000;
proto.MSECS_PER_MIN = 60 * proto.MSECS_PER_SEC;
proto.MSECS_PER_HOUR = 60 * proto.MSECS_PER_MIN;
proto.MSECS_PER_DAY = 24 * proto.MSECS_PER_HOUR;
proto.MSECS_PER_WEEK = 7 * proto.MSECS_PER_DAY;
  
proto.MONTH_LEN = 
[
  0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334
];

proto._getIsoWeekDate = function(date)
{
  var year = date.getFullYear();
  var month = date.getMonth() + 1;
  var day = date.getDate();

  var a,b,c,d,e,f,g,s,n;

  if (month <= 2)
  {
    a = year - 1;
    b = Math.floor(a/4) - Math.floor(a/100) + Math.floor(a/400);
    c = Math.floor((a-1)/4) - Math.floor((a-1)/100) + Math.floor((a-1)/400);
    s = b - c;
    e = 0;
    f = day - 1 + 31*(month-1);
  }
  else
  {
    a = year;
    b = Math.floor(a/4) - Math.floor(a/100) + Math.floor(a/400);
    c = Math.floor((a-1)/4) - Math.floor((a-1)/100) + Math.floor((a-1)/400);
    s = b - c;
    e = s + 1;
    f = day + Math.floor((153*(month-3) + 2)/5) + 58 + s;       
  }

  g = (a + b) % 7;
  d = (f + g - e) % 7;
  n = f + 3 - d;

  if (n<0)
  {
    // previous year
    var resultWeek = 53 - Math.floor((g-s)/5);
    return new org_apache_myfaces_dateformat_WeekDate(year-1, resultWeek);
  }
  else if (n > (364+s))
  {
    // next year
    var resultWeek = 1;
    return new org_apache_myfaces_dateformat_WeekDate(year+1, resultWeek);
  }
  else
  {
    // current year
    var resultWeek = Math.floor(n/7) + 1;
    return new org_apache_myfaces_dateformat_WeekDate(year, resultWeek);
  }
}

proto._isLeapYear = function(year)
{
  return ((year%4 == 0) && (year%100 != 0)) || (year%400 == 0);
}

proto._dayOfWeek = function(year, month, day)
{
  month -= 2;
  if (month < 1)
  {
    month += 12;
    --year;
  }

  var cent = Math.floor(year / 100);
  year %= 100;
  
  var base =
    Math.floor((26 * month - 2) / 10)
    + day
    + year
      + Math.floor(year/4)
      + Math.floor(cent/4)
      + (5*cent);

  var dow = base % 7;
  return dow;  
}

proto._getWeekDate = function(date, firstDayOfWeek)
{
  var year = date.getFullYear();
  var month = date.getMonth() + 1;
  var day = date.getDate();
  
  var thisIsLeapYear = this._isLeapYear(year);

  var dayOfYear = day + this.MONTH_LEN[month-1];
  if (thisIsLeapYear && (month>2))
  {
    ++dayOfYear;
  }

  var jan1Weekday = this._dayOfWeek(year, 1, 1);

  var pivotOffset = firstDayOfWeek - jan1Weekday;
  if (pivotOffset > 3)
  {
    pivotOffset -= 7;
  }
  else if (pivotOffset < -3)
  {
    pivotOffset += 7;
  }
  
  var dayOffset = dayOfYear-1;
  if (dayOffset < pivotOffset)
  {
    var prevIsLeapYear = this._isLeapYear(year-1);
    if ((pivotOffset==3) || ((pivotOffset==2) && prevIsLeapYear))
    {
      return new org_apache_myfaces_dateformat_WeekDate(year-1, 53);
    }
    return new org_apache_myfaces_dateformat_WeekDate(year-1, 52);
  }

  var daysFromFirstWeekStart = (dayOfYear - 1 - pivotOffset);
  var weekNumber = Math.floor(daysFromFirstWeekStart/7) + 1;  

  if ((weekNumber < 53)
      || (pivotOffset == -3)
      || (pivotOffset == -2 && thisIsLeapYear))
  {
    return new org_apache_myfaces_dateformat_WeekDate(year, weekNumber);
  }
  else
  {
    return new org_apache_myfaces_dateformat_WeekDate(year+1, 1);
  }
}

proto._getStartOfWeekYear = function(year, firstDayOfWeek)
{
  var d1 = new Date(year, 0, 1, 0, 0, 0);
  var firstDayOfYear = d1.getDay();
  var dayDiff = firstDayOfWeek - firstDayOfYear;
  var dayShift;
  if (dayDiff >= 4)
  {
    dayShift = 7 - dayShift;
  }
  else if (dayDiff >= 0)
  {
    dayShift = dayDiff;
  }
  else if (dayDiff >= -3)
  {
    dayShift = dayDiff;
  }
  else
  {
    dayShift = 7 + dayDiff;
  }

  var weekYearStartMsecs = d1.getTime() + (dayShift * this.MSECS_PER_DAY);
  return weekYearStartMsecs;
}

proto._getDateForWeekDate = function(
  year, week, day,
  hour, min, sec,
  firstDayOfWeek)
{
  var msecsBase = this._getStartOfWeekYear(year, firstDayOfWeek);
  var msecsOffset = (week-1) * this.MSECS_PER_WEEK;
  msecsOffset += (day-1) * this.MSECS_PER_DAY;
  msecsOffset += hour * this.MSECS_PER_HOUR;
  msecsOffset += min * this.MSECS_PER_MIN;
  msecsOffset += sec * this.MSECS_PER_SEC;
  
  var finalDate = new Date();
  finalDate.setTime(msecsBase + msecsOffset);
  return finalDate;
}

proto._fullYearFromDate = function(year)
{
  if (year < 1900)
  {
    return year+1900;
  }
  else
  {
    return year;
  }
}

proto._createDateFromContext = function(context)
{
  var date;
  if (context.weekOfWeekYear != 0)
  {
    date = this._getDateForWeekDate(
      context.weekYear, context.weekOfWeekYear, context.day,
      context.hour, context.min, context.sec,
      context.firstDayOfWeek);
  }
  else
  {
    date = new Date(
      context.year, context.month, context.day,
      context.hour, context.min, context.sec);
  }
  return date;
}

proto._substr = function(s, start, len)
{
  var s2 = s.substring(start);
  if (s2.length <= len)
    return s2;
  else
    return s2.substring(0, len);
}

proto._parseOps = function(symbols, yearIsWeekYear, firstDayOfWeek, ops, dateStr)
{
  var context = new org_apache_myfaces_dateformat_ParserContext(firstDayOfWeek);

  var dateIndex = 0;
  var dateStrLen = dateStr.length;
  for(var i=0; (i<ops.length) && (dateIndex < dateStrLen); ++i)
  {
    var op = ops[i];
    var optype = op.substring(0, 2);
    var opval = op.substring(2);

    if (optype == "f:")
    {
      this._parsePattern(symbols, yearIsWeekYear, context, opval, dateStr, dateIndex);

      if ((context.newIndex < 0) || context.invalid)
        break;

      dateIndex = context.newIndex;
    }
    else if (optype =="q:" || optype == "l:")
    {
      var oplen = opval.length;
      var s = this._substr(dateStr, dateIndex, oplen);
      if (opval != s)
      {
        context.invalid = true;
        break;
      }
      dateIndex += oplen;
    }
  }

  return context;
}

proto._parsePattern = function(
  symbols, yearIsWeekYear, context, patternSub, dateStr, dateIndex)
{
  var c = patternSub.charAt(0);
  var patlen = patternSub.length;

  if (c == 'y')
  {
    var year = this._parseNum(context, dateStr, 4, dateIndex);
    if ((context.newIndex-dateIndex) < 4)
    {
      context.year = year;
      context.ambiguousYear = true;
    }
    else
    {
      context.year = year;
      context.ambiguousYear = false;
    }

    if (yearIsWeekYear)
    {
      // There is a "ww" pattern present, so set weekYear as well as year.
      context.weekYear = context.year;
      context.ambiguousWeekYear = context.ambiguousYear;
    }
  }
  else if (c == 'x')
  {
    // extension to standard java.text.SimpleDateFormat class, to support the
    // JODA "weekYear" formatter.
    var year = this._parseNum(context, dateStr, 4, dateIndex);
    if ((context.newIndex-dateIndex) < 4)
    {
      context.weekYear = year;
      context.ambiguousWeekYear = true;
    }
    else
    {
      context.weekYear = year;
      context.ambiguousWeekYear = false;
    }
  }
  else if (c == 'M')
  {
    if (patlen == 3)
    {
      var fragment = this._substr(dateStr, dateIndex, 3);
      var index = this._parseIndexOf(context, symbols.shortMonths, fragment);
      if (index != -1)
      {
        context.month = index;
      }
    }
    else if (patlen >= 4)
    {
      var fragment = dateStr.substring(dateIndex);
      var index = this._parsePrefixOf(context, symbols.months, fragment);
      if (index != -1)
      {
        context.month = index;
      }
    }
    else
    {
      context.month = this._parseNum(context, dateStr, 2, dateIndex) - 1;
    }
  }
  else if (c == 'd')
  {
    context.day = this._parseNum(context, dateStr, 2, dateIndex);
  }
  else if (c == 'E')
  {
    if (patlen <= 3)
    {
      var fragment = dateStr.substring(dateIndex, dateIndex+3);
      var index = this._parseIndexOf(context, symbols.shortWeekdays, fragment);
      if (index != -1)
      {
        context.dayOfWeek = index;
      }
    }
    else
    {
      var fragment = dateStr.substring(dateIndex);
      var index = this._parsePrefixOf(context, symbols.weekdays, fragment);
      if (index != -1)
      {
        context.dayOfWeek = index;
      }
    }
  }
  else if (c == 'H')
  {
    context.hour = this._parseNum(context, dateStr, 2, dateIndex);
  }
  else if (c == 'h')
  {
    context.hourAmpm = this._parseNum(context, dateStr, 2, dateIndex);
  }
  else if (c == 'm')
  {
    context.min = this._parseNum(context, dateStr, 2, dateIndex);
  }
  else if (c == 's')
  {
    context.sec = this._parseNum(context, dateStr, 2, dateIndex);
  }
  else if (c == 'a')
  {
    context.ampm = this._parseString(context, dateStr, dateIndex, symbols.ampms);
  }
  else if (c == 'w')
  {
    context.weekOfWeekYear = this._parseNum(context, dateStr, 2, dateIndex);
  }
  else
  {
    context.invalid = true;
  }
}

proto._parseInt = function(value)
{
  var sum = 0;
  for(var i=0;i<value.length;i++)
  {
    var c = value.charAt(i);

    if(c<'0'||c>'9')
    {
      return -1;
    }
    sum = sum*10+(c-'0');
  }
  return sum;
}

proto._parseNum = function(context, dateStr, nChars, dateIndex)
{
  // Try to convert the most possible characters (posCount). If that fails,
  // then try again without the last character. Repeat until successful
  // numeric conversion occurs.
  var nToParse = Math.min(nChars, dateStr.length - dateIndex);
  for(var i=nToParse;i>0;i--)
  {
    var numStr = dateStr.substring(dateIndex,dateIndex+i);
    var value = this._parseInt(numStr);
    if(value == -1)
      continue;

    context.newIndex = dateIndex+i;
    return value;
  }

  context.newIndex = -1;
  context.invalid = true;
  return -1;
}

proto._parseIndexOf = function(context, array, value)
{
  for (var i = 0; i < array.length; ++i)
  {
    var s = array[i];
    if (value == s)
    {
      context.newIndex += s.length;
      return i;
    }
  }
  context.invalid = true;
  context.newIndex = -1;
  return -1;
}

proto._parsePrefixOf = function(context, array, value)
{
  for (var i=0; i <array.length; ++i)
  {
    var s = array[i];
    if (value.indexOf(s) == 0)
    {
      context.newIndex += s.length;
      return i;
    }
  }
  context.invalid = true;
  context.newIndex = -1;
  return -1;
}

proto._parseString = function(context, dateStr, dateIndex, strings)
{
  var fragment = dateStr.substr(dateIndex);
  return this._parsePrefixOf(context, strings, fragment);
}

proto._parsePostProcess = function(symbols, context)
{
  if (context.ambiguousYear)
  {
    context.year += 1900;
    var date = this._createDateFromContext(context);
    var threshold = symbols.twoDigitYearStart;
    if (date.getTime() < threshold.getTime())
    {
      context.year += 100;
    }
  }
  
  if (context.year <= 0) {
    context.year = new Date().getFullYear();
  }

  if (context.hourAmpm > 0)
  {
    if (context.ampm == 1)
    {
      context.hour = context.hourAmpm + 12;
      if (context.hour == 24)
        context.hour = 0;
    }
    else
    {
      context.hour = context.hourAmpm;
    }
  }
}

proto._formatOps = function(symbols, yearIsWeekYear, firstDayOfWeek, ops, date)
{
  var context = new org_apache_myfaces_dateformat_ParserContext(firstDayOfWeek);

  context.year = date.getFullYear();
  context.month = date.getMonth();
  context.day = date.getDate();
  context.dayOfWeek = date.getDay();
  context.hour = date.getHours();
  context.min = date.getMinutes();
  context.sec = date.getSeconds();
  
  context.ampm = (context.hour < 12) ? 0 : 1;
  
  var weekDate = this._getWeekDate(date, firstDayOfWeek);
  context.weekYear = weekDate.year;
  context.weekOfWeekYear = weekDate.week;
  
  var str = new org_apache_myfaces_dateformat_StringBuffer();
  for(var i=0; i<ops.length; ++i)
  {
    var op = ops[i];
    var optype = op.substring(0, 2);
    var opval = op.substring(2);

    if (optype == "f:")
    {
      this._formatPattern(symbols, context, opval, yearIsWeekYear, str);
      if (context.invalid)
        break;
    }
    else if (optype == "l:")
    {
      str.append(opval);
    }
    else if (optype == "q:")
    {
      str.append(opval);
    }
  }

  if (context.invalid)
    return null;
  else
    return str.toString();  
}

proto._formatPattern = function(
  symbols, context, patternSub, yearIsWeekYear, out)
{
  var c = patternSub.charAt(0);
  var patlen = patternSub.length;

  if (c=='y')
  {
    if (!yearIsWeekYear)
      this._formatNum(context.year, patlen <= 3 ? 2 : 4, true, out);
    else
      this._formatNum(context.weekYear, patlen <= 3 ? 2 : 4, true, out);
  }
  else if (c=='x')
  {
    this._formatNum(context.weekYear,patlen <= 3 ? 2 : 4, true, out);
  }
  else if (c=='M')
  {
     if (patlen == 3)
     {
       out.append(symbols.shortMonths[context.month]);
     }
     else if (patlen >= 4) {
       out.append(symbols.months[context.month]);
     }
     else
     {
       this._formatNum(context.month+1,patlen, false, out);
     }
  }
  else if (c=='d')
  {
    this._formatNum(context.day,patlen, false, out);
  }
  else if (c=='E')
  {
      if (patlen <= 3)
      {
        out.append(symbols.shortWeekdays[context.dayOfWeek]);
      }
      else
      {
        out.append(symbols.weekdays[context.dayOfWeek]);
      }
  }
  else if(c=='H')
  {
    this._formatNum(context.hour, patlen, false, out);
  }
  else if (c=='h')
  {
    var hour = context.hour;
    if (hour == 0) 
    {
      hour = 12;
    }
    else if (hour > 12)
    {
      hour = hour - 12;
    }
    this._formatNum(hour, patlen, false, out);
  }
  else if (c=='m')
  {
    this._formatNum(context.min, patlen, false, out);
  }
  else if (c=='s')
  {
    this._formatNum(context.sec, patlen, false, out);
  }
  else if (c=='a')
  {
    out.append(symbols.ampms[context.ampm]);
  }
  else if (c=='w')
  {
    this._formatNum(context.weekOfWeekYear, patlen, false, out);
  }
  else
  {
    context.invalid = true;
  }
}

proto._formatNum = function(num, length, ensureLength, out)
{
  var str = "" + num;

  while(str.length<length)
  {
    str = "0" + str;
  }

  if (ensureLength && str.length > length)
  {
    str = str.substr(str.length - length);
  }

  out.append(str);
}

proto._appendToArray = function(array, obj)
{
  array[array.length] = obj;
}

proto._isLetter = function(c)
{
  if ((c>= 'a') && (c<='z')) return true;
  if ((c>='A') && (c<='Z'))  return true;
  return false;
}

proto._analysePattern = function(pattern)
{
  var patternIndex = 0;
  var patternLen = pattern.length;
  var lastChar = 0;
  var patternSub = null;
  var quoteMode = false;

  var ops = new Array();

  while (patternIndex < patternLen)
  {
      var currentChar = pattern.charAt(patternIndex);
      var nextChar;

      if (patternIndex < patternLen - 1)
      {
          nextChar = pattern.charAt(patternIndex + 1);
      }
      else
      {
          nextChar = 0;
      }

      if (currentChar == '\'' && lastChar != '\\')
      {
          if (patternSub != null)
          {
              this._appendToArray(ops, patternSub.toString());
              patternSub = null;
          }
          quoteMode = !quoteMode;
      }
      else if (quoteMode)
      {
          if (patternSub == null)
          {
              patternSub = new org_apache_myfaces_dateformat_StringBuffer("q:");
          }
          patternSub.append(currentChar);
      }
      else
      {
          if (currentChar == '\\' && lastChar != '\\')
          {
              // do nothing
          }
          else
          {
              if (patternSub == null)
              {
                  if (this._isLetter(currentChar))
                  {
                      patternSub = new org_apache_myfaces_dateformat_StringBuffer("f:");
                  }
                  else
                  {
                      patternSub = new org_apache_myfaces_dateformat_StringBuffer("l:");
                  }
              }

              patternSub.append(currentChar);
              if (currentChar != nextChar)
              {
                  this._appendToArray(ops, patternSub.toString());
                  patternSub = null;
              }
          }
      }

      patternIndex++;
      lastChar = currentChar;
  }

  if (patternSub != null)
  {
      this._appendToArray(ops, patternSub.toString());
  }

  return ops;
}

proto._hasWeekPattern = function(ops)
{
  var wwPresent = false;
  var xxPresent = false;
  for(var i=0; i<ops.length; ++i)
  {
    var s = ops[i];
    wwPresent = wwPresent || (s.indexOf("f:ww")==0);
    xxPresent = xxPresent || (s.indexOf("f:xx")==0);
  }
  
  return wwPresent && !xxPresent;
}

// Constructor for SimpleDateFormatter class
proto._construct = function(pattern, symbols, firstDayOfWeek)
{
  if (symbols == null)
  {
    this.symbols = new org_apache_myfaces_dateformat_DateFormatSymbols();
  }
  else
  {
    this.symbols = symbols;
  }

  this.ops = this._analysePattern(pattern);
  this.yearIsWeekYear = this._hasWeekPattern(this.ops);
  
  if (firstDayOfWeek != null)
    this.firstDayOfWeek = firstDayOfWeek;
  else
    this.firstDayOfWeek = 1;
}

proto.setFirstDayOfWeek = function(dow)
{
  this.firstDayOfWeek = dow;
}

proto.parse = function(dateStr)
{
  if ((dateStr == null) || (dateStr.length == 0))
    return null;
    
  var context = this._parseOps(
    this.symbols, this.yearIsWeekYear, this.firstDayOfWeek, this.ops,
    dateStr); 

  if (context.invalid)
  {
    return null;
  }
  
  this._parsePostProcess(this.symbols, context);
  return this._createDateFromContext(context);
}

proto.format = function(date)
{
  return this._formatOps(
    this.symbols, this.yearIsWeekYear, this.firstDayOfWeek, this.ops,
    date);
}

proto.getWeekDate = function(date)
{
  return this._getWeekDate(date, this.firstDayOfWeek);
}

proto.getDateForWeekDate = function(wdate)
{
  return this._getDateForWeekDate(wdate.year, wdate.week, 1, 0, 0, 0, this.firstDayOfWeek);
}

} // end of static init block for SimpleDateFormatter class