var RSF_Calendar = function() {

    /* _Cal is **IMPL** for Calendar */

    YAHOO.widget.Calendar2up_PUC_Cal = function(id, containerId, monthyear, selected) {
      if (arguments.length > 0)
      {
        this.init(id, containerId, monthyear, selected);
      }
    }

    YAHOO.widget.Calendar2up_PUC_Cal.prototype = new YAHOO.widget.Calendar2up_Cal();

	function copyArray(array) {
	  var togo = new Array(array.length);
	  for (var i = 0; i < array.length; ++ i) {
	    togo[i] = array[i];
	    }
	  return togo;
	  }

    /** "Forward" reference here to localisation block rendered into the 
    HTML document **/
    YAHOO.widget.Calendar2up_PUC_Cal.prototype.customConfig = function() {
      this.Config.Locale.MONTHS_SHORT = copyArray(PUC_MONTHS_SHORT);
      this.Config.Locale.MONTHS_LONG =  copyArray(PUC_MONTHS_LONG);
      this.Config.Locale.WEEKDAYS_1CHAR = copyArray(PUC_WEEKDAYS_1CHAR);
      this.Config.Locale.WEEKDAYS_SHORT = copyArray(PUC_WEEKDAYS_SHORT);
      this.Config.Locale.WEEKDAYS_MEDIUM = copyArray(PUC_WEEKDAYS_MEDIUM);
      this.Config.Locale.WEEKDAYS_LONG = copyArray(PUC_WEEKDAYS_LONG);
      this.Config.Options.START_WEEKDAY = PUC_FIRST_DAY_OF_WEEK;
      }

    /** Plain object is **UI** for Calendar, of type YAHOO.widget **/

    YAHOO.widget.Calendar2up_PUC = function(id, containerId, monthyear, selected) {
      if (arguments.length > 0) {
        this.buildWrapper(containerId);
        this.init(2, id, containerId, monthyear, selected);
      }
    }

    YAHOO.widget.Calendar2up_PUC.prototype = new YAHOO.widget.Calendar2up();

    YAHOO.widget.Calendar2up_PUC.prototype.constructChild = 
      function(id, containerId, monthyear, selected) {
        var cal = new YAHOO.widget.Calendar2up_PUC_Cal(id, containerId, monthyear, selected);
        return cal;
      };

    /* End YAHOO setup defs */

  function yahoo_showCalendar(cal, dateLink) {
      
    var pos = YAHOO.util.Dom.getXY(dateLink);
    cal.outerContainer.style.display='block';
    YAHOO.util.Dom.addClass(cal.outerContainer, "annotation-front");
    YAHOO.util.Dom.setXY(cal.outerContainer, [pos[0],pos[1]+dateLink.offsetHeight+1]);
    RSF.getDOMModifyFirer().fireEvent();
  }

  function yahoo_setDate_Dropdowns(cal, selMonth, selDay) {
    var date1 = cal.getSelectedDates()[0];
    selMonth.selectedIndex = date1.getMonth();
    selDay.selectedIndex = date1.getDate() - 1;
    cal.hide();
  }

  function yahoo_changeDate_Dropdowns(cal, selMonth, selDay) {
    var month = selMonth.selectedIndex;
    var day = selDay.selectedIndex + 1;
      // NB this line specific for 2up - uses undocumented list "pages" of Calendar
    var year = cal.pages[0].pageDate.getFullYear();

    cal.select((month+1) + "/" + day + "/" + year);
    cal.setMonth(month);
    cal.render();
  }

  function $it(elementID) {
    return document.getElementById(elementID);
    }
  function keyArray(o) {
    var togo = [];
    for (var i in o) {
      togo.push(i);
      }
    return togo;
    }
  function arrayToKeys(a) {
    var togo = {};
    for (var i in a) {
      togo[a[i]] = 1;
      }
    return togo;
    }

/** An object coordinating updates of the textual field value. trueDate and
    dateField are both <input>, annotation is a <div> **/
  var registerDateFieldUpdateHandler = function (newcal, nameBase, transitbase, AJAXURL) {
    var annotationField = $it(nameBase + "date-annotation");
    var timeAnnotationField = $it(nameBase + "time-annotation");
    var trueValueFieldID = nameBase + "true-date";
    var trueValueField = $it(trueValueFieldID);
    var dateFieldID = nameBase + "date-field";
    var dateField = $it(dateFieldID);
    var timeFieldID = nameBase + "time-field";
    var timeField = $it(timeFieldID);
    var shortbinding = transitbase + "short";
    var longbinding = transitbase + "long";
    var truebinding = transitbase + "ISO8601TZ";
    var timebinding = transitbase + "time";
    var longtimebinding = transitbase + "longTime";
    var calField = newcal.outerContainer;
    
    var dateformat = annotationField.innerHTML;
    var timeformat = timeAnnotationField? timeAnnotationField.innerHTML : "";
    
    dateField.onfocus = function() {
      YAHOO.util.Dom.replaceClass(annotationField, "annotation-inactive", "annotation-active");
      }
    dateField.onblur = function() {
      YAHOO.util.Dom.replaceClass(annotationField, "annotation-active", "annotation-inactive");
      }
    if (timeField) {
      timeField.onfocus = function() {
        YAHOO.util.Dom.replaceClass(timeAnnotationField, "annotation-inactive", "annotation-active");
      }
      timeField.onblur = function() {
        YAHOO.util.Dom.replaceClass(timeAnnotationField, "annotation-active", "annotation-inactive");
        }
      }
   
    var updateTrueValue = RSF.getModelFirer(trueValueField);
    var updateDateFieldValue = RSF.getModelFirer(dateField);
    var updateTimeFieldValue = RSF.getModelFirer(timeField);

	var updateAnnotation = function(annotField, isvalid, longvalue, defvalue) {
	  if (isvalid) {
	    annotField.innerHTML = longvalue;
	    YAHOO.util.Dom.replaceClass(annotField, "annotation-incomplete", "annotation-complete");
	    }
	  else {
	    annotField.innerHTML = defvalue;
	    YAHOO.util.Dom.replaceClass(annotField, "annotation-complete", "annotation-incomplete");
	    }
      };
// sourceField is dateField, trueValueField or timeField.
	function getAJAXUpdater(sourceField, AJAXURL) {
  	var sourceFields = [sourceField];
	  var allbindings = arrayToKeys([truebinding, longbinding, shortbinding]);
	  if (timeField) {
	    allbindings[longtimebinding] = 1;
	    if (sourceField == timeField) {
	      sourceFields.push(dateField);
	      }
	    else if (sourceField == dateField) {
	      sourceFields.push(timeField);
	      }
	    // No other field can update time, do not backpropagate from true
	    }
	  if (sourceField == dateField) {
	    delete allbindings[shortbinding];
	    }
	  else if (sourceField == trueValueField) {
	    delete allbindings[truebinding];
	    }
	  var bindings = keyArray(allbindings);
	 
	  return RSF.getAJAXUpdater(sourceFields, AJAXURL, bindings,
	    function(UVB) {
          var longresult = UVB.EL[longbinding];
          var trueresult = UVB.EL[truebinding];
          var shortresult = UVB.EL[shortbinding];
          var longtimeresult = UVB.EL[longtimebinding];
          updateAnnotation(annotationField, !RSF.hasUVBError(UVB, nameBase), longresult, dateformat);
          if (trueresult) {
            updateTrueValue(false, trueresult);
            }
          if (shortresult) {
            updateDateFieldValue(false, shortresult);
            }
          if (longtimeresult) {
            updateAnnotation(timeAnnotationField, !RSF.hasUVBError(UVB, nameBase), longtimeresult, timeformat);
     //       if (sourceField != timeField) {
     //         updateTimeFieldValue(false, timeresult);
     //         }
            }
          }
        );
	  }

    if (AJAXURL) {      
      var fieldValueChanged = getAJAXUpdater(dateField, AJAXURL);
      var trueValueChanged = getAJAXUpdater(trueValueField, AJAXURL);
      if (timeField) {
	    var timeFieldValueChanged = getAJAXUpdater(timeField, AJAXURL);
	    }
      }
    else {
      var fieldValueChanged = function() {
        var isvalid = dateField.value == "08/11/06"       
        updateAnnotation(annotationField, isvalid, "08 November 2006", dateformat);
        if (isvalid) {
          var trueDate = new Date(2006, 11, 8);
          var fixedDate= FixedDate.fromDate(trueDate);
          updateTrueValue(false, FixedDate.renderISO8601(fixedDate));
          }
        };
      var trueValueChanged = function() {
        updateAnnotation(annotationField, true, "08 November 2006", dateformat);
        updateDateFieldValue(false, "08/11/06");
        };
      var timeFieldValueChanged = function() {
        var isvalid = timeField.value == "09:00";
          YAHOO.log("timeFieldValueChange " + isvalid);
        updateAnnotation(timeAnnotationField, isvalid, "09:00", timeformat);
        }
      }
    // track the "old" value in outer scope. Do not initialise to ensure initial fire
    var datefieldvalue;
    //primitive event handler for changes to field value
    var dateFieldChange = function() {
      updateDateFieldValue(true, dateField.value, datefieldvalue);
      datefieldvalue = dateField.value;
      };
    
    dateField.onkeyup = dateFieldChange;
    dateField.onchange = dateFieldChange;
    
    var dateFieldFireExclusions = [trueValueField];
    var trueValueFireExclusions = [dateField];
    
    if (timeField) {
      var timefieldvalue;
      var timeFieldChange = function() {
        YAHOO.log("timeFieldChange");
        updateTimeFieldValue(true, timeField.value, timefieldvalue);
        timefieldvalue = timeField.value;
        };
      timeField.onkeyup = timeFieldChange;
      timeField.onchange = timeFieldChange;
      RSF.addElementListener(timeField, timeFieldValueChanged, [trueValueField, dateField]);
      dateFieldFireExclusions.push(timeField);
      trueValueFireExclusions.push(timeField);
    }
    
    RSF.addElementListener(dateField, fieldValueChanged, dateFieldFireExclusions);
    RSF.addElementListener(trueValueField, trueValueChanged, trueValueFireExclusions);
    RSF.addElementListener(trueValueField, function() {
      YAHOO.log("Fire cal change");
      YAHOO.log("Cal change for " + trueValueField.id + " value " + trueValueField.value);
      var date = FixedDate.parseISO8601(trueValueField.value);
      var year = date.fullYear;
      var day = date.day;
      var month = date.month;
      YAHOO.log("Cal id " + newcal.outerContainer.id + " controlDate before: " + newcal.getSelectedDates()[0]);
      YAHOO.log("Cal selecting " + month + "/" + day + "/" + year);
      newcal.select(month + "/" + day + "/" + year);     
      newcal.setMonth(month - 1);
      newcal.setYear(year);
      newcal.render();
      YAHOO.log("controlDate after: " + newcal.getSelectedDates()[0]);
      }, [trueValueField]);
    
    newcal.setChildFunction("onSelect", 
      function() {
        newcal.hide();
        var controlDate = newcal.getSelectedDates()[0];
        var normDate = FixedDate.fromDate(controlDate);
        YAHOO.log("controlDate " + controlDate);
        var converted = FixedDate.renderISO8601(normDate);
        YAHOO.log("Converted " + converted);
        // do not trash time value in underlying date!
        var fused = converted.substring(0, 10) + trueValueField.value.substring(10);
        updateTrueValue(true, fused);
        if (RSF.primaryFirerCount() == 0) {
          // only update focus if this is a direct event
          dateField.focus();
          }
        }
      );
      
    
    }


/** Base initialisation for the calendar controls.
 * Parses a JS Date object into a form suitable to initialise the control,
 * Adds event handler to the launching link, and adds the global "dismiss"
 * event listener to the global document. 
 * Returns the constructed JS widget object.
 */

  function initYahooCalendar_base(value, nameBase, controlIDs, title) {
    var containerID = nameBase + "date-container";
    var container = $it(containerID);
    if (container && container.childNodes && container.childNodes.length > 0) {
      // already rendered
      return null;
      }

    var thisMonth = value.month;
    var thisDay = value.day;
    var thisYear = value.fullYear;
  
    var newcal = 
      new YAHOO.widget.Calendar2up_PUC("YAHOO.calendar."+containerID,
      containerID, (thisMonth)+"/"+thisYear, (thisMonth)+"/"+thisDay+"/"+thisYear);
  
    var dateLinkID = nameBase + "date-link";
    var dateLink = $it(dateLinkID);
  
    dateLink.onclick = function() { 
      yahoo_showCalendar(newcal, this);
    };
    
    controlIDs.push(dateLinkID);
    controlIDs.push(containerID);
	    
    newcal.title = title;
	 
    var conthash = new Object();
    for (var x in controlIDs) {
      conthash[controlIDs[x]] = 1;
      }

    YAHOO.util.Event.addListener(document, "click", 
      function(e) {
        yahoo_documentClick(e, newcal, conthash);
      });
  
    return newcal;
    }

  function initYahooCalendar_Dropdowns(nameBase, title) {
    var today = new Date();

    var thisMonth = today.month;
    var thisDay = today.day;
    var thisYear = today.fullYear;

    var selMonthID = nameBase + "select-month";
    var selDayID = nameBase + "select-day";

    var selMonth = $it(selMonthID);
    var selDay = $it(selDayID);

    var controlIDs = [selMonthID, selDayID];

    var newcal = initYahooCalendar_base(today, nameBase, controlIDs, title);
    if (!newcal) {
      return;
      }

    newcal.setChildFunction("onSelect", 
      function() {
        yahoo_setDate_Dropdowns(newcal, selMonth, selDay);
        });
          
    selMonth.selectedIndex = thisMonth;
    selDay.selectedIndex = thisDay;

    selMonth.onchange = function() {
	  yahoo_changeDate_Dropdowns(newcal, this, selDay);
	  };
	    
	selDay.onchange = function() {
	  yahoo_changeDate_Dropdowns(newcal, selMonth, this);
	  };

//      YAHOO.example.calendar.cal1.addRenderer("1/1,1/6,5/1,8/15,10/3,10/31,12/25,12/26", YAHOO.example.calendar.cal1.pages[0].renderCellStyleHighlight1);
     newcal.render();
   }

/** The global "dismiss" listener which ensures that any click away
from the controls of an active calendar dismisses its popup **/

  function yahoo_documentClick(e, cal, IDs) { 
    var target = YAHOO.util.Event.getTarget(e);
	  
    var found = false;
    while(target) {
      if (IDs[target.id]) {
	      found = true;
	      }
	    target = target.parentNode;
      }
    if (!found) {
      cal.hide();
      }
//	alert("click " + e); 
    }

  return {	
    initYahooCalendar_Datefield: function(nameBase, title, transitbase, AJAXURL) {
      var dateFieldID = nameBase + "date-field";
      var dateField = $it(dateFieldID);

	  var timeFieldID = nameBase + "time-field";
      var trueDate = $it(nameBase + "true-date");
      var valuestring = trueDate.value;
      var value = FixedDate.fromDate(new Date());
      if (valuestring.length != 0) {
        value = FixedDate.parseISO8601(valuestring);
        }
  
      controlIDs = [dateFieldID, timeFieldID];
  
      var newcal = initYahooCalendar_base(value, nameBase, controlIDs, title);
      if (!newcal) {
        return;
        }
    
//    var annotation = $it(nameBase + "date-annotation");
      var dateLink = $it(nameBase + "date-link");
      dateLink.style.display = "inline";
    
      registerDateFieldUpdateHandler(newcal, nameBase, transitbase, AJAXURL);
      dateField.onchange();
  
      newcal.render();
      }
    }
  }();
