// ** I18N

// Calendar EN language
// Author: Mihai Bazon, <mishoo@infoiasi.ro>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Ням",
 "Даваа",
 "Мягмар",
 "Лхагва",
 "Пүрэв",
 "Баасан",
 "Бямба",
 "Ням");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("Ням",
 "Даваа",
 "Мягмар",
 "Лхагва",
 "Пүрэв",
 "Баасан",
 "Бямба",
 "Ням");

// full month names
Calendar._MN = new Array
("Нэгдүгээр сар",
 "Хоёрдугаар сар",
 "Гуравдугаар сар",
 "Дөрөвдүгээр сар",
 "Тавдугаар сар",
 "Зуравдугаар сар",
 "Долдугаар сар",
 "Наймдугаар сар",
 "Есдүгээр сар",
 "Аравдугаар сар",
 "Арваннэгдүгээр сар",
 "Арванхоёхдугаар сар");

// short month names
Calendar._SMN = new Array
("1-р сар",
 "2-р сар",
 "3-р сар",
 "4-р сар",
 "5-р сар",
 "6-р сар",
 "7-р сар",
 "8-р сар",
 "9-р сар",
 "10-р сар",
 "11-р сар",
 "12-р сар");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Календарын талаар";

Calendar._TT["ABOUT"] =
"DHTML Огноо/Цаг сонгогч\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Сүүлийн хувилбарыг авах бол: http://dynarch.com/mishoo/calendar.epl\n" +
"Distributed under GNU LGPL. http://gnu.org/licenses/lgpl.html for details.-ыг хар" +
"\n\n" +
"Огноо сонгох:\n" +
"- Жилийг сонгохдоо \xab, \xbb товчуудыг ашигла\n" +
"- Сарыг сонгохдоо " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " товчийг ашигла\n" +
"- Хурдан сонгохын тулд дээрх товчуудын аль нэг дээр нь хулганыхаа товчийг дар.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Цагын сонголт:\n" +
"- Нэмэхийн тулд цагын аль ч хэсгийг хамаагүй дар\n" +
"- Эсвэл бууруулахын тулд нөгөө тийш нь дар\n" +
"- Эсвэл хурдан сонгохын тулд дараад чир.";

Calendar._TT["PREV_YEAR"] = "Өмнөх жил (цэсэнд багтсан)";
Calendar._TT["PREV_MONTH"] = "Өмнөх сар (цэсэнд багтсан)";
Calendar._TT["GO_TODAY"] = "Өнөөдрийнхрүү орох";
Calendar._TT["NEXT_MONTH"] = "Дараагийн сар (цэсэнд багтсан)";
Calendar._TT["NEXT_YEAR"] = "Дараагийн жил (цэсэнд багтсан)";
Calendar._TT["SEL_DATE"] = "Огноог сонго";
Calendar._TT["DRAG_TO_MOVE"] = "Зөөхөөр чирэх";
Calendar._TT["PART_TODAY"] = " (Өнөөдөр)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Эхлээд %-ыг харуул";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Хаах";
Calendar._TT["TODAY"] = "Өнөөдөр";
Calendar._TT["TIME_PART"] = "(Shift-)Үнэлгээг солихын тулд дарах юмуу чир";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "wk";
Calendar._TT["TIME"] = "Хугацаа:";
