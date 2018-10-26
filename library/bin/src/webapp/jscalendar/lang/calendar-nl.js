// ** I18N

// Calendar NL language
// Author: Mihai Bazon, <mishoo@infoiasi.ro>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Zondag",
 "Maandag",
 "Dinsdag",
 "Woensdag",
 "Donderdag",
 "Vrijdag",
 "Zaterdag",
 "Zondag");

Calendar._SDN = new Array
("Zon",
 "Maa",
 "Din",
 "Woe",
 "Don",
 "Vri",
 "Zat",
 "Zon");

// full month names
Calendar._MN = new Array
("Januari",
 "Februari",
 "Maart",
 "April",
 "Mei",
 "Juni",
 "Juli",
 "Augustus",
 "September",
 "Oktober",
 "November",
 "December"
);

// short month names
Calendar._SMN = new Array
("Jan",
 "Feb",
 "Maa",
 "Apr",
 "Mei",
 "Jun",
 "Jul",
 "Aug",
 "Sep",
 "Okt",
 "Nov",
 "Dec");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Over de kalender";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Voor de nieuwste versie: http://dynarch.com/mishoo/calendar.epl\n" +
"Gedistribueerd GNU LGPL.  Bezoek http://gnu.org/licenses/lgpl.html voor meer details." +
"\n\n" +
"Datum selectie:\n" +
"- Gebruik de \xab, \xbb knoppen om een jaar te selecteren\n" +
"- Gebruik de " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " knoppen om een maand te selecteren\n" +
"- Hou de muisknoppen ingedrukt boven elk van de bovenstaande knopen voor een snellere selectie.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Tijd selecteren:\n" +
"- Klik op een van de tijdonderdelen om ze te verhogen\n" +
"- of Shift-klik om te verlagen\n" +
"- of klik en sleep voor een snellere selectie.";

Calendar._TT["PREV_YEAR"] = "Vorig jaar (ingedrukt voor menu)";
Calendar._TT["PREV_MONTH"] = "Vorige maand (ingedrukt voor menu)";
Calendar._TT["GO_TODAY"] = "Ga naar Vandaag";
Calendar._TT["NEXT_MONTH"] = "Volgende maand (ingedrukt voor menu)";
Calendar._TT["NEXT_YEAR"] = "Volgend jaar (ingedrukt voor menu)";
Calendar._TT["SEL_DATE"] = "Selecteer datum";
Calendar._TT["DRAG_TO_MOVE"] = "Klik en sleep om te verplaatsen";
Calendar._TT["PART_TODAY"] = " (vandaag)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Toon %s eerst";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";
Calendar._TT["TIME_PART"] = "(Shift-)Click of sleep om de waarde te veranderen";


Calendar._TT["TOGGLE"] = "Selecteer de eerste week-dag";
Calendar._TT["CLOSE"] = "Sluiten";
Calendar._TT["TODAY"] = "Vandaag";

Calendar._TT["WEEKEND"] = "0,6";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "wk";
Calendar._TT["TIME"] = "Tijd:";
