/* 
* Sakai internationalized date/time picker
* https://jira.sakaiproject.org/browse/SAK-23662
* Built on top of jquery-ui and jQuery-Timepicker-Addon
* Plus custom integration code for Sakai
* Translations generated from jquery-ui Git checkout: cat ui/i18n/datepicker-*|grep -v "\/\*"|grep -v jQuery|grep -v setDefaults|grep -v "^})"
* Please submit any translation issues to the upstream project: http://bugs.jqueryui.com/ or https://github.com/trentrichardson/jQuery-Timepicker-Addon
* Order of components in this file:
* * jquery-ui i18n translations
* * jquery-timepicker-addon minified code
* * jquery-timepicker-addon translations
* * moment.js (minified) to parse and validate dates (http://momentjs.com/)
* * jQuery UI Touch Punch 0.2.2 
* * Sakai integration code
*/

/*! jQuery UI - v1.11.3 
* http://jqueryui.com
* Copyright 2013 jQuery Foundation and other contributors; Licensed MIT 
*/
(function($) {
$.datepicker.regional['af'] = {
closeText: 'Selekteer',
prevText: 'Vorige',
nextText: 'Volgende',
currentText: 'Vandag',
monthNames: ['Januarie','Februarie','Maart','April','Mei','Junie',
'Julie','Augustus','September','Oktober','November','Desember'],
monthNamesShort: ['Jan', 'Feb', 'Mrt', 'Apr', 'Mei', 'Jun',
'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Des'],
dayNames: ['Sondag', 'Maandag', 'Dinsdag', 'Woensdag', 'Donderdag', 'Vrydag', 'Saterdag'],
dayNamesShort: ['Son', 'Maa', 'Din', 'Woe', 'Don', 'Vry', 'Sat'],
dayNamesMin: ['So','Ma','Di','Wo','Do','Vr','Sa'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ar-DZ'] = {
closeText: 'إغلاق',
prevText: '&#x3C;السابق',
nextText: 'التالي&#x3E;',
currentText: 'اليوم',
monthNames: ['جانفي', 'فيفري', 'مارس', 'أفريل', 'ماي', 'جوان',
'جويلية', 'أوت', 'سبتمبر','أكتوبر', 'نوفمبر', 'ديسمبر'],
monthNamesShort: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
dayNames: ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'],
dayNamesShort: ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'],
dayNamesMin: ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'],
weekHeader: 'أسبوع',
dateFormat: 'dd/mm/yy',
firstDay: 6,
isRTL: true,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ar'] = {
closeText: 'إغلاق',
prevText: '&#x3C;السابق',
nextText: 'التالي&#x3E;',
currentText: 'اليوم',
monthNames: ['يناير', 'فبراير', 'مارس', 'أبريل', 'مايو', 'يونيو',
'يوليو', 'أغسطس', 'سبتمبر', 'أكتوبر', 'نوفمبر', 'ديسمبر'],
monthNamesShort: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
dayNames: ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'],
dayNamesShort: ['أحد', 'اثنين', 'ثلاثاء', 'أربعاء', 'خميس', 'جمعة', 'سبت'],
dayNamesMin: ['ح', 'ن', 'ث', 'ر', 'خ', 'ج', 'س'],
weekHeader: 'أسبوع',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: true,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['az'] = {
closeText: 'Bağla',
prevText: '&#x3C;Geri',
nextText: 'İrəli&#x3E;',
currentText: 'Bugün',
monthNames: ['Yanvar','Fevral','Mart','Aprel','May','İyun',
'İyul','Avqust','Sentyabr','Oktyabr','Noyabr','Dekabr'],
monthNamesShort: ['Yan','Fev','Mar','Apr','May','İyun',
'İyul','Avq','Sen','Okt','Noy','Dek'],
dayNames: ['Bazar','Bazar ertəsi','Çərşənbə axşamı','Çərşənbə','Cümə axşamı','Cümə','Şənbə'],
dayNamesShort: ['B','Be','Ça','Ç','Ca','C','Ş'],
dayNamesMin: ['B','B','Ç','С','Ç','C','Ş'],
weekHeader: 'Hf',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['be'] = {
closeText: 'Зачыніць',
prevText: '&larr;Папяр.',
nextText: 'Наст.&rarr;',
currentText: 'Сёньня',
monthNames: ['Студзень','Люты','Сакавік','Красавік','Травень','Чэрвень',
'Ліпень','Жнівень','Верасень','Кастрычнік','Лістапад','Сьнежань'],
monthNamesShort: ['Сту','Лют','Сак','Кра','Тра','Чэр',
'Ліп','Жні','Вер','Кас','Ліс','Сьн'],
dayNames: ['нядзеля','панядзелак','аўторак','серада','чацьвер','пятніца','субота'],
dayNamesShort: ['ндз','пнд','аўт','срд','чцв','птн','сбт'],
dayNamesMin: ['Нд','Пн','Аў','Ср','Чц','Пт','Сб'],
weekHeader: 'Тд',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['bg'] = {
closeText: 'затвори',
prevText: '&#x3C;назад',
nextText: 'напред&#x3E;',
nextBigText: '&#x3E;&#x3E;',
currentText: 'днес',
monthNames: ['Януари','Февруари','Март','Април','Май','Юни',
'Юли','Август','Септември','Октомври','Ноември','Декември'],
monthNamesShort: ['Яну','Фев','Мар','Апр','Май','Юни',
'Юли','Авг','Сеп','Окт','Нов','Дек'],
dayNames: ['Неделя','Понеделник','Вторник','Сряда','Четвъртък','Петък','Събота'],
dayNamesShort: ['Нед','Пон','Вто','Сря','Чет','Пет','Съб'],
dayNamesMin: ['Не','По','Вт','Ср','Че','Пе','Съ'],
weekHeader: 'Wk',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['bs'] = {
closeText: 'Zatvori',
prevText: '&#x3C;',
nextText: '&#x3E;',
currentText: 'Danas',
monthNames: ['Januar','Februar','Mart','April','Maj','Juni',
'Juli','August','Septembar','Oktobar','Novembar','Decembar'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
'Jul','Aug','Sep','Okt','Nov','Dec'],
dayNames: ['Nedelja','Ponedeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
dayNamesShort: ['Ned','Pon','Uto','Sri','Čet','Pet','Sub'],
dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
weekHeader: 'Wk',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ca'] = {
closeText: 'Tanca',
prevText: 'Anterior',
nextText: 'Següent',
currentText: 'Avui',
monthNames: ['gener','febrer','març','abril','maig','juny',
'juliol','agost','setembre','octubre','novembre','desembre'],
monthNamesShort: ['gen','feb','març','abr','maig','juny',
'jul','ag','set','oct','nov','des'],
dayNames: ['diumenge','dilluns','dimarts','dimecres','dijous','divendres','dissabte'],
dayNamesShort: ['dg','dl','dt','dc','dj','dv','ds'],
dayNamesMin: ['dg','dl','dt','dc','dj','dv','ds'],
weekHeader: 'Set',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['cs'] = {
closeText: 'Zavřít',
prevText: '&#x3C;Dříve',
nextText: 'Později&#x3E;',
currentText: 'Nyní',
monthNames: ['leden','únor','březen','duben','květen','červen',
'červenec','srpen','září','říjen','listopad','prosinec'],
monthNamesShort: ['led','úno','bře','dub','kvě','čer',
'čvc','srp','zář','říj','lis','pro'],
dayNames: ['neděle', 'pondělí', 'úterý', 'středa', 'čtvrtek', 'pátek', 'sobota'],
dayNamesShort: ['ne', 'po', 'út', 'st', 'čt', 'pá', 'so'],
dayNamesMin: ['ne','po','út','st','čt','pá','so'],
weekHeader: 'Týd',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['cy-GB'] = {
closeText: 'Done',
prevText: 'Prev',
nextText: 'Next',
currentText: 'Today',
monthNames: ['Ionawr','Chwefror','Mawrth','Ebrill','Mai','Mehefin',
'Gorffennaf','Awst','Medi','Hydref','Tachwedd','Rhagfyr'],
monthNamesShort: ['Ion', 'Chw', 'Maw', 'Ebr', 'Mai', 'Meh',
'Gor', 'Aws', 'Med', 'Hyd', 'Tac', 'Rha'],
dayNames: ['Dydd Sul', 'Dydd Llun', 'Dydd Mawrth', 'Dydd Mercher', 'Dydd Iau', 'Dydd Gwener', 'Dydd Sadwrn'],
dayNamesShort: ['Sul', 'Llu', 'Maw', 'Mer', 'Iau', 'Gwe', 'Sad'],
dayNamesMin: ['Su','Ll','Ma','Me','Ia','Gw','Sa'],
weekHeader: 'Wy',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['da'] = {
closeText: 'Luk',
prevText: '&#x3C;Forrige',
nextText: 'Næste&#x3E;',
currentText: 'Idag',
monthNames: ['Januar','Februar','Marts','April','Maj','Juni',
'Juli','August','September','Oktober','November','December'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
'Jul','Aug','Sep','Okt','Nov','Dec'],
dayNames: ['Søndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','Lørdag'],
dayNamesShort: ['Søn','Man','Tir','Ons','Tor','Fre','Lør'],
dayNamesMin: ['Sø','Ma','Ti','On','To','Fr','Lø'],
weekHeader: 'Uge',
dateFormat: 'dd-mm-yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['de'] = {
closeText: 'Schließen',
prevText: '&#x3C;Zurück',
nextText: 'Vor&#x3E;',
currentText: 'Heute',
monthNames: ['Januar','Februar','März','April','Mai','Juni',
'Juli','August','September','Oktober','November','Dezember'],
monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
'Jul','Aug','Sep','Okt','Nov','Dez'],
dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
weekHeader: 'KW',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['el'] = {
closeText: 'Κλείσιμο',
prevText: 'Προηγούμενος',
nextText: 'Επόμενος',
currentText: 'Σήμερα',
monthNames: ['Ιανουάριος','Φεβρουάριος','Μάρτιος','Απρίλιος','Μάιος','Ιούνιος',
'Ιούλιος','Αύγουστος','Σεπτέμβριος','Οκτώβριος','Νοέμβριος','Δεκέμβριος'],
monthNamesShort: ['Ιαν','Φεβ','Μαρ','Απρ','Μαι','Ιουν',
'Ιουλ','Αυγ','Σεπ','Οκτ','Νοε','Δεκ'],
dayNames: ['Κυριακή','Δευτέρα','Τρίτη','Τετάρτη','Πέμπτη','Παρασκευή','Σάββατο'],
dayNamesShort: ['Κυρ','Δευ','Τρι','Τετ','Πεμ','Παρ','Σαβ'],
dayNamesMin: ['Κυ','Δε','Τρ','Τε','Πε','Πα','Σα'],
weekHeader: 'Εβδ',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['en-AU'] = {
closeText: 'Done',
prevText: 'Prev',
nextText: 'Next',
currentText: 'Today',
monthNames: ['January','February','March','April','May','June',
'July','August','September','October','November','December'],
monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['en-GB'] = {
closeText: 'Done',
prevText: 'Prev',
nextText: 'Next',
currentText: 'Today',
monthNames: ['January','February','March','April','May','June',
'July','August','September','October','November','December'],
monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['en-NZ'] = {
closeText: 'Done',
prevText: 'Prev',
nextText: 'Next',
currentText: 'Today',
monthNames: ['January','February','March','April','May','June',
'July','August','September','October','November','December'],
monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['en-ZA'] = {
closeText: 'Done',
prevText: 'Prev',
nextText: 'Next',
currentText: 'Today',
monthNames: ['January','February','March','April','May','June',
'July','August','September','October','November','December'],
monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['eo'] = {
closeText: 'Fermi',
prevText: '&#x3C;Anta',
nextText: 'Sekv&#x3E;',
currentText: 'Nuna',
monthNames: ['Januaro','Februaro','Marto','Aprilo','Majo','Junio',
'Julio','Aŭgusto','Septembro','Oktobro','Novembro','Decembro'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
'Jul','Aŭg','Sep','Okt','Nov','Dec'],
dayNames: ['Dimanĉo','Lundo','Mardo','Merkredo','Ĵaŭdo','Vendredo','Sabato'],
dayNamesShort: ['Dim','Lun','Mar','Mer','Ĵaŭ','Ven','Sab'],
dayNamesMin: ['Di','Lu','Ma','Me','Ĵa','Ve','Sa'],
weekHeader: 'Sb',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['es'] = {
closeText: 'Cerrar',
prevText: '&#x3C;Ant',
nextText: 'Sig&#x3E;',
currentText: 'Hoy',
monthNames: ['enero','febrero','marzo','abril','mayo','junio',
'julio','agosto','septiembre','octubre','noviembre','diciembre'],
monthNamesShort: ['ene','feb','mar','abr','may','jun',
'jul','ago','sep','oct','nov','dic'],
dayNames: ['domingo','lunes','martes','miércoles','jueves','viernes','sábado'],
dayNamesShort: ['dom','lun','mar','mié','jue','vie','sáb'],
dayNamesMin: ['D','L','M','X','J','V','S'],
weekHeader: 'Sm',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['et'] = {
closeText: 'Sulge',
prevText: 'Eelnev',
nextText: 'Järgnev',
currentText: 'Täna',
monthNames: ['Jaanuar','Veebruar','Märts','Aprill','Mai','Juuni',
'Juuli','August','September','Oktoober','November','Detsember'],
monthNamesShort: ['Jaan', 'Veebr', 'Märts', 'Apr', 'Mai', 'Juuni',
'Juuli', 'Aug', 'Sept', 'Okt', 'Nov', 'Dets'],
dayNames: ['Pühapäev', 'Esmaspäev', 'Teisipäev', 'Kolmapäev', 'Neljapäev', 'Reede', 'Laupäev'],
dayNamesShort: ['Pühap', 'Esmasp', 'Teisip', 'Kolmap', 'Neljap', 'Reede', 'Laup'],
dayNamesMin: ['P','E','T','K','N','R','L'],
weekHeader: 'näd',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['eu'] = {
closeText: 'Egina',
prevText: '&#x3C;Aur',
nextText: 'Hur&#x3E;',
currentText: 'Gaur',
monthNames: ['urtarrila','otsaila','martxoa','apirila','maiatza','ekaina',
'uztaila','abuztua','iraila','urria','azaroa','abendua'],
monthNamesShort: ['urt.','ots.','mar.','api.','mai.','eka.',
'uzt.','abu.','ira.','urr.','aza.','abe.'],
dayNames: ['igandea','astelehena','asteartea','asteazkena','osteguna','ostirala','larunbata'],
dayNamesShort: ['ig.','al.','ar.','az.','og.','ol.','lr.'],
dayNamesMin: ['ig','al','ar','az','og','ol','lr'],
weekHeader: 'As',
dateFormat: 'yy-mm-dd',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['fa'] = {
closeText: 'بستن',
prevText: '&#x3C;قبلی',
nextText: 'بعدی&#x3E;',
currentText: 'امروز',
monthNames: [
'ژانویه',
'فوریه',
'مارس',
'آوریل',
'مه',
'ژوئن',
'ژوئیه',
'اوت',
'سپتامبر',
'اکتبر',
'نوامبر',
'دسامبر'
],
monthNamesShort: ['1','2','3','4','5','6','7','8','9','10','11','12'],
dayNames: [
'يکشنبه',
'دوشنبه',
'سه‌شنبه',
'چهارشنبه',
'پنجشنبه',
'جمعه',
'شنبه'
],
dayNamesShort: [
'ی',
'د',
'س',
'چ',
'پ',
'ج',
'ش'
],
dayNamesMin: [
'ی',
'د',
'س',
'چ',
'پ',
'ج',
'ش'
],
weekHeader: 'هف',
dateFormat: 'yy/mm/dd',
firstDay: 6,
isRTL: true,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['fi'] = {
closeText: 'Sulje',
prevText: '&#xAB;Edellinen',
nextText: 'Seuraava&#xBB;',
currentText: 'Tänään',
monthNames: ['Tammikuu','Helmikuu','Maaliskuu','Huhtikuu','Toukokuu','Kesäkuu',
'Heinäkuu','Elokuu','Syyskuu','Lokakuu','Marraskuu','Joulukuu'],
monthNamesShort: ['Tammi','Helmi','Maalis','Huhti','Touko','Kesä',
'Heinä','Elo','Syys','Loka','Marras','Joulu'],
dayNamesShort: ['Su','Ma','Ti','Ke','To','Pe','La'],
dayNames: ['Sunnuntai','Maanantai','Tiistai','Keskiviikko','Torstai','Perjantai','Lauantai'],
dayNamesMin: ['Su','Ma','Ti','Ke','To','Pe','La'],
weekHeader: 'Vk',
dateFormat: 'd.m.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['fo'] = {
closeText: 'Lat aftur',
prevText: '&#x3C;Fyrra',
nextText: 'Næsta&#x3E;',
currentText: 'Í dag',
monthNames: ['Januar','Februar','Mars','Apríl','Mei','Juni',
'Juli','August','September','Oktober','November','Desember'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Mei','Jun',
'Jul','Aug','Sep','Okt','Nov','Des'],
dayNames: ['Sunnudagur','Mánadagur','Týsdagur','Mikudagur','Hósdagur','Fríggjadagur','Leyardagur'],
dayNamesShort: ['Sun','Mán','Týs','Mik','Hós','Frí','Ley'],
dayNamesMin: ['Su','Má','Tý','Mi','Hó','Fr','Le'],
weekHeader: 'Vk',
dateFormat: 'dd-mm-yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['fr-CA'] = {
closeText: 'Fermer',
prevText: 'Précédent',
nextText: 'Suivant',
currentText: 'Aujourd\'hui',
monthNames: ['janvier', 'février', 'mars', 'avril', 'mai', 'juin',
'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'],
monthNamesShort: ['janv.', 'févr.', 'mars', 'avril', 'mai', 'juin',
'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.'],
dayNames: ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'],
dayNamesShort: ['dim.', 'lun.', 'mar.', 'mer.', 'jeu.', 'ven.', 'sam.'],
dayNamesMin: ['D', 'L', 'M', 'M', 'J', 'V', 'S'],
weekHeader: 'Sem.',
dateFormat: 'yy-mm-dd',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''
};
$.datepicker.regional['fr-CH'] = {
closeText: 'Fermer',
prevText: '&#x3C;Préc',
nextText: 'Suiv&#x3E;',
currentText: 'Courant',
monthNames: ['janvier', 'février', 'mars', 'avril', 'mai', 'juin',
'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'],
monthNamesShort: ['janv.', 'févr.', 'mars', 'avril', 'mai', 'juin',
'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.'],
dayNames: ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'],
dayNamesShort: ['dim.', 'lun.', 'mar.', 'mer.', 'jeu.', 'ven.', 'sam.'],
dayNamesMin: ['D', 'L', 'M', 'M', 'J', 'V', 'S'],
weekHeader: 'Sm',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['fr'] = {
closeText: 'Fermer',
prevText: 'Précédent',
nextText: 'Suivant',
currentText: 'Aujourd\'hui',
monthNames: ['janvier', 'février', 'mars', 'avril', 'mai', 'juin',
'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'],
monthNamesShort: ['janv.', 'févr.', 'mars', 'avr.', 'mai', 'juin',
'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.'],
dayNames: ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'],
dayNamesShort: ['dim.', 'lun.', 'mar.', 'mer.', 'jeu.', 'ven.', 'sam.'],
dayNamesMin: ['D','L','M','M','J','V','S'],
weekHeader: 'Sem.',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['gl'] = {
closeText: 'Pechar',
prevText: '&#x3C;Ant',
nextText: 'Seg&#x3E;',
currentText: 'Hoxe',
monthNames: ['Xaneiro','Febreiro','Marzo','Abril','Maio','Xuño',
'Xullo','Agosto','Setembro','Outubro','Novembro','Decembro'],
monthNamesShort: ['Xan','Feb','Mar','Abr','Mai','Xuñ',
'Xul','Ago','Set','Out','Nov','Dec'],
dayNames: ['Domingo','Luns','Martes','Mércores','Xoves','Venres','Sábado'],
dayNamesShort: ['Dom','Lun','Mar','Mér','Xov','Ven','Sáb'],
dayNamesMin: ['Do','Lu','Ma','Mé','Xo','Ve','Sá'],
weekHeader: 'Sm',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['he'] = {
closeText: 'סגור',
prevText: '&#x3C;הקודם',
nextText: 'הבא&#x3E;',
currentText: 'היום',
monthNames: ['ינואר','פברואר','מרץ','אפריל','מאי','יוני',
'יולי','אוגוסט','ספטמבר','אוקטובר','נובמבר','דצמבר'],
monthNamesShort: ['ינו','פבר','מרץ','אפר','מאי','יוני',
'יולי','אוג','ספט','אוק','נוב','דצמ'],
dayNames: ['ראשון','שני','שלישי','רביעי','חמישי','שישי','שבת'],
dayNamesShort: ['א\'','ב\'','ג\'','ד\'','ה\'','ו\'','שבת'],
dayNamesMin: ['א\'','ב\'','ג\'','ד\'','ה\'','ו\'','שבת'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: true,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['hi'] = {
closeText: 'बंद',
prevText: 'पिछला',
nextText: 'अगला',
currentText: 'आज',
monthNames: ['जनवरी ','फरवरी','मार्च','अप्रेल','मई','जून',
'जूलाई','अगस्त ','सितम्बर','अक्टूबर','नवम्बर','दिसम्बर'],
monthNamesShort: ['जन', 'फर', 'मार्च', 'अप्रेल', 'मई', 'जून',
'जूलाई', 'अग', 'सित', 'अक्ट', 'नव', 'दि'],
dayNames: ['रविवार', 'सोमवार', 'मंगलवार', 'बुधवार', 'गुरुवार', 'शुक्रवार', 'शनिवार'],
dayNamesShort: ['रवि', 'सोम', 'मंगल', 'बुध', 'गुरु', 'शुक्र', 'शनि'],
dayNamesMin: ['रवि', 'सोम', 'मंगल', 'बुध', 'गुरु', 'शुक्र', 'शनि'],
weekHeader: 'हफ्ता',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['hr'] = {
closeText: 'Zatvori',
prevText: '&#x3C;',
nextText: '&#x3E;',
currentText: 'Danas',
monthNames: ['Siječanj','Veljača','Ožujak','Travanj','Svibanj','Lipanj',
'Srpanj','Kolovoz','Rujan','Listopad','Studeni','Prosinac'],
monthNamesShort: ['Sij','Velj','Ožu','Tra','Svi','Lip',
'Srp','Kol','Ruj','Lis','Stu','Pro'],
dayNames: ['Nedjelja','Ponedjeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
dayNamesShort: ['Ned','Pon','Uto','Sri','Čet','Pet','Sub'],
dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
weekHeader: 'Tje',
dateFormat: 'dd.mm.yy.',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['hu'] = {
closeText: 'bezár',
prevText: 'vissza',
nextText: 'előre',
currentText: 'ma',
monthNames: ['Január', 'Február', 'Március', 'Április', 'Május', 'Június',
'Július', 'Augusztus', 'Szeptember', 'Október', 'November', 'December'],
monthNamesShort: ['Jan', 'Feb', 'Már', 'Ápr', 'Máj', 'Jún',
'Júl', 'Aug', 'Szep', 'Okt', 'Nov', 'Dec'],
dayNames: ['Vasárnap', 'Hétfő', 'Kedd', 'Szerda', 'Csütörtök', 'Péntek', 'Szombat'],
dayNamesShort: ['Vas', 'Hét', 'Ked', 'Sze', 'Csü', 'Pén', 'Szo'],
dayNamesMin: ['V', 'H', 'K', 'Sze', 'Cs', 'P', 'Szo'],
weekHeader: 'Hét',
dateFormat: 'yy.mm.dd.',
firstDay: 1,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: ''};
$.datepicker.regional['hy'] = {
closeText: 'Փակել',
prevText: '&#x3C;Նախ.',
nextText: 'Հաջ.&#x3E;',
currentText: 'Այսօր',
monthNames: ['Հունվար','Փետրվար','Մարտ','Ապրիլ','Մայիս','Հունիս',
'Հուլիս','Օգոստոս','Սեպտեմբեր','Հոկտեմբեր','Նոյեմբեր','Դեկտեմբեր'],
monthNamesShort: ['Հունվ','Փետր','Մարտ','Ապր','Մայիս','Հունիս',
'Հուլ','Օգս','Սեպ','Հոկ','Նոյ','Դեկ'],
dayNames: ['կիրակի','եկուշաբթի','երեքշաբթի','չորեքշաբթի','հինգշաբթի','ուրբաթ','շաբաթ'],
dayNamesShort: ['կիր','երկ','երք','չրք','հնգ','ուրբ','շբթ'],
dayNamesMin: ['կիր','երկ','երք','չրք','հնգ','ուրբ','շբթ'],
weekHeader: 'ՇԲՏ',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['id'] = {
closeText: 'Tutup',
prevText: '&#x3C;mundur',
nextText: 'maju&#x3E;',
currentText: 'hari ini',
monthNames: ['Januari','Februari','Maret','April','Mei','Juni',
'Juli','Agustus','September','Oktober','Nopember','Desember'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Mei','Jun',
'Jul','Agus','Sep','Okt','Nop','Des'],
dayNames: ['Minggu','Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'],
dayNamesShort: ['Min','Sen','Sel','Rab','kam','Jum','Sab'],
dayNamesMin: ['Mg','Sn','Sl','Rb','Km','jm','Sb'],
weekHeader: 'Mg',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['is'] = {
closeText: 'Loka',
prevText: '&#x3C; Fyrri',
nextText: 'Næsti &#x3E;',
currentText: 'Í dag',
monthNames: ['Janúar','Febrúar','Mars','Apríl','Maí','Júní',
'Júlí','Ágúst','September','Október','Nóvember','Desember'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maí','Jún',
'Júl','Ágú','Sep','Okt','Nóv','Des'],
dayNames: ['Sunnudagur','Mánudagur','Þriðjudagur','Miðvikudagur','Fimmtudagur','Föstudagur','Laugardagur'],
dayNamesShort: ['Sun','Mán','Þri','Mið','Fim','Fös','Lau'],
dayNamesMin: ['Su','Má','Þr','Mi','Fi','Fö','La'],
weekHeader: 'Vika',
dateFormat: 'dd.mm.yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['it-CH'] = {
closeText: 'Chiudi',
prevText: '&#x3C;Prec',
nextText: 'Succ&#x3E;',
currentText: 'Oggi',
monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
'Lug','Ago','Set','Ott','Nov','Dic'],
dayNames: ['Domenica','Lunedì','Martedì','Mercoledì','Giovedì','Venerdì','Sabato'],
dayNamesShort: ['Dom','Lun','Mar','Mer','Gio','Ven','Sab'],
dayNamesMin: ['Do','Lu','Ma','Me','Gi','Ve','Sa'],
weekHeader: 'Sm',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['it'] = {
closeText: 'Chiudi',
prevText: '&#x3C;Prec',
nextText: 'Succ&#x3E;',
currentText: 'Oggi',
monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
'Lug','Ago','Set','Ott','Nov','Dic'],
dayNames: ['Domenica','Lunedì','Martedì','Mercoledì','Giovedì','Venerdì','Sabato'],
dayNamesShort: ['Dom','Lun','Mar','Mer','Gio','Ven','Sab'],
dayNamesMin: ['Do','Lu','Ma','Me','Gi','Ve','Sa'],
weekHeader: 'Sm',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ja'] = {
closeText: '閉じる',
prevText: '&#x3C;前',
nextText: '次&#x3E;',
currentText: '今日',
monthNames: ['1月','2月','3月','4月','5月','6月',
'7月','8月','9月','10月','11月','12月'],
monthNamesShort: ['1月','2月','3月','4月','5月','6月',
'7月','8月','9月','10月','11月','12月'],
dayNames: ['日曜日','月曜日','火曜日','水曜日','木曜日','金曜日','土曜日'],
dayNamesShort: ['日','月','火','水','木','金','土'],
dayNamesMin: ['日','月','火','水','木','金','土'],
weekHeader: '週',
dateFormat: 'yy/mm/dd',
firstDay: 0,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: '年'};
$.datepicker.regional['ka'] = {
closeText: 'დახურვა',
prevText: '&#x3c; წინა',
nextText: 'შემდეგი &#x3e;',
currentText: 'დღეს',
monthNames: ['იანვარი','თებერვალი','მარტი','აპრილი','მაისი','ივნისი', 'ივლისი','აგვისტო','სექტემბერი','ოქტომბერი','ნოემბერი','დეკემბერი'],
monthNamesShort: ['იან','თებ','მარ','აპრ','მაი','ივნ', 'ივლ','აგვ','სექ','ოქტ','ნოე','დეკ'],
dayNames: ['კვირა','ორშაბათი','სამშაბათი','ოთხშაბათი','ხუთშაბათი','პარასკევი','შაბათი'],
dayNamesShort: ['კვ','ორშ','სამ','ოთხ','ხუთ','პარ','შაბ'],
dayNamesMin: ['კვ','ორშ','სამ','ოთხ','ხუთ','პარ','შაბ'],
weekHeader: 'კვირა',
dateFormat: 'dd-mm-yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['kk'] = {
closeText: 'Жабу',
prevText: '&#x3C;Алдыңғы',
nextText: 'Келесі&#x3E;',
currentText: 'Бүгін',
monthNames: ['Қаңтар','Ақпан','Наурыз','Сәуір','Мамыр','Маусым',
'Шілде','Тамыз','Қыркүйек','Қазан','Қараша','Желтоқсан'],
monthNamesShort: ['Қаң','Ақп','Нау','Сәу','Мам','Мау',
'Шіл','Там','Қыр','Қаз','Қар','Жел'],
dayNames: ['Жексенбі','Дүйсенбі','Сейсенбі','Сәрсенбі','Бейсенбі','Жұма','Сенбі'],
dayNamesShort: ['жкс','дсн','ссн','срс','бсн','жма','снб'],
dayNamesMin: ['Жк','Дс','Сс','Ср','Бс','Жм','Сн'],
weekHeader: 'Не',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['km'] = {
closeText: 'ធ្វើ​រួច',
prevText: 'មុន',
nextText: 'បន្ទាប់',
currentText: 'ថ្ងៃ​នេះ',
monthNames: ['មករា','កុម្ភៈ','មីនា','មេសា','ឧសភា','មិថុនា',
'កក្កដា','សីហា','កញ្ញា','តុលា','វិច្ឆិកា','ធ្នូ'],
monthNamesShort: ['មករា','កុម្ភៈ','មីនា','មេសា','ឧសភា','មិថុនា',
'កក្កដា','សីហា','កញ្ញា','តុលា','វិច្ឆិកា','ធ្នូ'],
dayNames: ['អាទិត្យ', 'ចន្ទ', 'អង្គារ', 'ពុធ', 'ព្រហស្បតិ៍', 'សុក្រ', 'សៅរ៍'],
dayNamesShort: ['អា', 'ច', 'អ', 'ពុ', 'ព្រហ', 'សុ', 'សៅ'],
dayNamesMin: ['អា', 'ច', 'អ', 'ពុ', 'ព្រហ', 'សុ', 'សៅ'],
weekHeader: 'សប្ដាហ៍',
dateFormat: 'dd-mm-yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ko'] = {
closeText: '닫기',
prevText: '이전달',
nextText: '다음달',
currentText: '오늘',
monthNames: ['1월','2월','3월','4월','5월','6월',
'7월','8월','9월','10월','11월','12월'],
monthNamesShort: ['1월','2월','3월','4월','5월','6월',
'7월','8월','9월','10월','11월','12월'],
dayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
dayNamesShort: ['일','월','화','수','목','금','토'],
dayNamesMin: ['일','월','화','수','목','금','토'],
weekHeader: 'Wk',
dateFormat: 'yy-mm-dd',
firstDay: 0,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: '년'};
$.datepicker.regional['ky'] = {
closeText: 'Жабуу',
prevText: '&#x3c;Мур',
nextText: 'Кий&#x3e;',
currentText: 'Бүгүн',
monthNames: ['Январь','Февраль','Март','Апрель','Май','Июнь',
'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'],
monthNamesShort: ['Янв','Фев','Мар','Апр','Май','Июн',
'Июл','Авг','Сен','Окт','Ноя','Дек'],
dayNames: ['жекшемби', 'дүйшөмбү', 'шейшемби', 'шаршемби', 'бейшемби', 'жума', 'ишемби'],
dayNamesShort: ['жек', 'дүй', 'шей', 'шар', 'бей', 'жум', 'ише'],
dayNamesMin: ['Жк','Дш','Шш','Шр','Бш','Жм','Иш'],
weekHeader: 'Жум',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''
};
$.datepicker.regional['lb'] = {
closeText: 'Fäerdeg',
prevText: 'Zréck',
nextText: 'Weider',
currentText: 'Haut',
monthNames: ['Januar','Februar','Mäerz','Abrëll','Mee','Juni',
'Juli','August','September','Oktober','November','Dezember'],
monthNamesShort: ['Jan', 'Feb', 'Mäe', 'Abr', 'Mee', 'Jun',
'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
dayNames: ['Sonndeg', 'Méindeg', 'Dënschdeg', 'Mëttwoch', 'Donneschdeg', 'Freideg', 'Samschdeg'],
dayNamesShort: ['Son', 'Méi', 'Dën', 'Mët', 'Don', 'Fre', 'Sam'],
dayNamesMin: ['So','Mé','Dë','Më','Do','Fr','Sa'],
weekHeader: 'W',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['lt'] = {
closeText: 'Uždaryti',
prevText: '&#x3C;Atgal',
nextText: 'Pirmyn&#x3E;',
currentText: 'Šiandien',
monthNames: ['Sausis','Vasaris','Kovas','Balandis','Gegužė','Birželis',
'Liepa','Rugpjūtis','Rugsėjis','Spalis','Lapkritis','Gruodis'],
monthNamesShort: ['Sau','Vas','Kov','Bal','Geg','Bir',
'Lie','Rugp','Rugs','Spa','Lap','Gru'],
dayNames: ['sekmadienis','pirmadienis','antradienis','trečiadienis','ketvirtadienis','penktadienis','šeštadienis'],
dayNamesShort: ['sek','pir','ant','tre','ket','pen','šeš'],
dayNamesMin: ['Se','Pr','An','Tr','Ke','Pe','Še'],
weekHeader: 'SAV',
dateFormat: 'yy-mm-dd',
firstDay: 1,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: ''};
$.datepicker.regional['lv'] = {
closeText: 'Aizvērt',
prevText: 'Iepr.',
nextText: 'Nāk.',
currentText: 'Šodien',
monthNames: ['Janvāris','Februāris','Marts','Aprīlis','Maijs','Jūnijs',
'Jūlijs','Augusts','Septembris','Oktobris','Novembris','Decembris'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jūn',
'Jūl','Aug','Sep','Okt','Nov','Dec'],
dayNames: ['svētdiena','pirmdiena','otrdiena','trešdiena','ceturtdiena','piektdiena','sestdiena'],
dayNamesShort: ['svt','prm','otr','tre','ctr','pkt','sst'],
dayNamesMin: ['Sv','Pr','Ot','Tr','Ct','Pk','Ss'],
weekHeader: 'Ned.',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['mk'] = {
closeText: 'Затвори',
prevText: '&#x3C;',
nextText: '&#x3E;',
currentText: 'Денес',
monthNames: ['Јануари','Февруари','Март','Април','Мај','Јуни',
'Јули','Август','Септември','Октомври','Ноември','Декември'],
monthNamesShort: ['Јан','Фев','Мар','Апр','Мај','Јун',
'Јул','Авг','Сеп','Окт','Ное','Дек'],
dayNames: ['Недела','Понеделник','Вторник','Среда','Четврток','Петок','Сабота'],
dayNamesShort: ['Нед','Пон','Вто','Сре','Чет','Пет','Саб'],
dayNamesMin: ['Не','По','Вт','Ср','Че','Пе','Са'],
weekHeader: 'Сед',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ml'] = {
closeText: 'ശരി',
prevText: 'മുന്നത്തെ',
nextText: 'അടുത്തത് ',
currentText: 'ഇന്ന്',
monthNames: ['ജനുവരി','ഫെബ്രുവരി','മാര്‍ച്ച്','ഏപ്രില്‍','മേയ്','ജൂണ്‍',
'ജൂലൈ','ആഗസ്റ്റ്','സെപ്റ്റംബര്‍','ഒക്ടോബര്‍','നവംബര്‍','ഡിസംബര്‍'],
monthNamesShort: ['ജനു', 'ഫെബ്', 'മാര്‍', 'ഏപ്രി', 'മേയ്', 'ജൂണ്‍',
'ജൂലാ', 'ആഗ', 'സെപ്', 'ഒക്ടോ', 'നവം', 'ഡിസ'],
dayNames: ['ഞായര്‍', 'തിങ്കള്‍', 'ചൊവ്വ', 'ബുധന്‍', 'വ്യാഴം', 'വെള്ളി', 'ശനി'],
dayNamesShort: ['ഞായ', 'തിങ്ക', 'ചൊവ്വ', 'ബുധ', 'വ്യാഴം', 'വെള്ളി', 'ശനി'],
dayNamesMin: ['ഞാ','തി','ചൊ','ബു','വ്യാ','വെ','ശ'],
weekHeader: 'ആ',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ms'] = {
closeText: 'Tutup',
prevText: '&#x3C;Sebelum',
nextText: 'Selepas&#x3E;',
currentText: 'hari ini',
monthNames: ['Januari','Februari','Mac','April','Mei','Jun',
'Julai','Ogos','September','Oktober','November','Disember'],
monthNamesShort: ['Jan','Feb','Mac','Apr','Mei','Jun',
'Jul','Ogo','Sep','Okt','Nov','Dis'],
dayNames: ['Ahad','Isnin','Selasa','Rabu','Khamis','Jumaat','Sabtu'],
dayNamesShort: ['Aha','Isn','Sel','Rab','kha','Jum','Sab'],
dayNamesMin: ['Ah','Is','Se','Ra','Kh','Ju','Sa'],
weekHeader: 'Mg',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['nb'] = {
closeText: 'Lukk',
prevText: '&#xAB;Forrige',
nextText: 'Neste&#xBB;',
currentText: 'I dag',
monthNames: ['januar','februar','mars','april','mai','juni','juli','august','september','oktober','november','desember'],
monthNamesShort: ['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des'],
dayNamesShort: ['søn','man','tir','ons','tor','fre','lør'],
dayNames: ['søndag','mandag','tirsdag','onsdag','torsdag','fredag','lørdag'],
dayNamesMin: ['sø','ma','ti','on','to','fr','lø'],
weekHeader: 'Uke',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''
};
$.datepicker.regional['nl-BE'] = {
closeText: 'Sluiten',
prevText: '←',
nextText: '→',
currentText: 'Vandaag',
monthNames: ['januari', 'februari', 'maart', 'april', 'mei', 'juni',
'juli', 'augustus', 'september', 'oktober', 'november', 'december'],
monthNamesShort: ['jan', 'feb', 'mrt', 'apr', 'mei', 'jun',
'jul', 'aug', 'sep', 'okt', 'nov', 'dec'],
dayNames: ['zondag', 'maandag', 'dinsdag', 'woensdag', 'donderdag', 'vrijdag', 'zaterdag'],
dayNamesShort: ['zon', 'maa', 'din', 'woe', 'don', 'vri', 'zat'],
dayNamesMin: ['zo', 'ma', 'di', 'wo', 'do', 'vr', 'za'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional.nl = {
closeText: 'Sluiten',
prevText: '←',
nextText: '→',
currentText: 'Vandaag',
monthNames: ['januari', 'februari', 'maart', 'april', 'mei', 'juni',
'juli', 'augustus', 'september', 'oktober', 'november', 'december'],
monthNamesShort: ['jan', 'feb', 'mrt', 'apr', 'mei', 'jun',
'jul', 'aug', 'sep', 'okt', 'nov', 'dec'],
dayNames: ['zondag', 'maandag', 'dinsdag', 'woensdag', 'donderdag', 'vrijdag', 'zaterdag'],
dayNamesShort: ['zon', 'maa', 'din', 'woe', 'don', 'vri', 'zat'],
dayNamesMin: ['zo', 'ma', 'di', 'wo', 'do', 'vr', 'za'],
weekHeader: 'Wk',
dateFormat: 'dd-mm-yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['nn'] = {
closeText: 'Lukk',
prevText: '&#xAB;Førre',
nextText: 'Neste&#xBB;',
currentText: 'I dag',
monthNames: ['januar','februar','mars','april','mai','juni','juli','august','september','oktober','november','desember'],
monthNamesShort: ['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des'],
dayNamesShort: ['sun','mån','tys','ons','tor','fre','lau'],
dayNames: ['sundag','måndag','tysdag','onsdag','torsdag','fredag','laurdag'],
dayNamesMin: ['su','må','ty','on','to','fr','la'],
weekHeader: 'Veke',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''
};
$.datepicker.regional['no'] = {
closeText: 'Lukk',
prevText: '&#xAB;Forrige',
nextText: 'Neste&#xBB;',
currentText: 'I dag',
monthNames: ['januar','februar','mars','april','mai','juni','juli','august','september','oktober','november','desember'],
monthNamesShort: ['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des'],
dayNamesShort: ['søn','man','tir','ons','tor','fre','lør'],
dayNames: ['søndag','mandag','tirsdag','onsdag','torsdag','fredag','lørdag'],
dayNamesMin: ['sø','ma','ti','on','to','fr','lø'],
weekHeader: 'Uke',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''
};
$.datepicker.regional['pl'] = {
closeText: 'Zamknij',
prevText: '&#x3C;Poprzedni',
nextText: 'Następny&#x3E;',
currentText: 'Dziś',
monthNames: ['Styczeń','Luty','Marzec','Kwiecień','Maj','Czerwiec',
'Lipiec','Sierpień','Wrzesień','Październik','Listopad','Grudzień'],
monthNamesShort: ['Sty','Lu','Mar','Kw','Maj','Cze',
'Lip','Sie','Wrz','Pa','Lis','Gru'],
dayNames: ['Niedziela','Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota'],
dayNamesShort: ['Nie','Pn','Wt','Śr','Czw','Pt','So'],
dayNamesMin: ['N','Pn','Wt','Śr','Cz','Pt','So'],
weekHeader: 'Tydz',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['pt-BR'] = {
closeText: 'Fechar',
prevText: '&#x3C;Anterior',
nextText: 'Próximo&#x3E;',
currentText: 'Hoje',
monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
'Jul','Ago','Set','Out','Nov','Dez'],
dayNames: ['Domingo','Segunda-feira','Terça-feira','Quarta-feira','Quinta-feira','Sexta-feira','Sábado'],
dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
dayNamesMin: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
weekHeader: 'Sm',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['pt'] = {
closeText: 'Fechar',
prevText: 'Anterior',
nextText: 'Seguinte',
currentText: 'Hoje',
monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
'Jul','Ago','Set','Out','Nov','Dez'],
dayNames: ['Domingo','Segunda-feira','Terça-feira','Quarta-feira','Quinta-feira','Sexta-feira','Sábado'],
dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
dayNamesMin: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
weekHeader: 'Sem',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['rm'] = {
closeText: 'Serrar',
prevText: '&#x3C;Suandant',
nextText: 'Precedent&#x3E;',
currentText: 'Actual',
monthNames: ['Schaner','Favrer','Mars','Avrigl','Matg','Zercladur', 'Fanadur','Avust','Settember','October','November','December'],
monthNamesShort: ['Scha','Fev','Mar','Avr','Matg','Zer', 'Fan','Avu','Sett','Oct','Nov','Dec'],
dayNames: ['Dumengia','Glindesdi','Mardi','Mesemna','Gievgia','Venderdi','Sonda'],
dayNamesShort: ['Dum','Gli','Mar','Mes','Gie','Ven','Som'],
dayNamesMin: ['Du','Gl','Ma','Me','Gi','Ve','So'],
weekHeader: 'emna',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ro'] = {
closeText: 'Închide',
prevText: '&#xAB; Luna precedentă',
nextText: 'Luna următoare &#xBB;',
currentText: 'Azi',
monthNames: ['Ianuarie','Februarie','Martie','Aprilie','Mai','Iunie',
'Iulie','August','Septembrie','Octombrie','Noiembrie','Decembrie'],
monthNamesShort: ['Ian', 'Feb', 'Mar', 'Apr', 'Mai', 'Iun',
'Iul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
dayNames: ['Duminică', 'Luni', 'Marţi', 'Miercuri', 'Joi', 'Vineri', 'Sâmbătă'],
dayNamesShort: ['Dum', 'Lun', 'Mar', 'Mie', 'Joi', 'Vin', 'Sâm'],
dayNamesMin: ['Du','Lu','Ma','Mi','Jo','Vi','Sâ'],
weekHeader: 'Săpt',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ru'] = {
closeText: 'Закрыть',
prevText: '&#x3C;Пред',
nextText: 'След&#x3E;',
currentText: 'Сегодня',
monthNames: ['Январь','Февраль','Март','Апрель','Май','Июнь',
'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'],
monthNamesShort: ['Янв','Фев','Мар','Апр','Май','Июн',
'Июл','Авг','Сен','Окт','Ноя','Дек'],
dayNames: ['воскресенье','понедельник','вторник','среда','четверг','пятница','суббота'],
dayNamesShort: ['вск','пнд','втр','срд','чтв','птн','сбт'],
dayNamesMin: ['Вс','Пн','Вт','Ср','Чт','Пт','Сб'],
weekHeader: 'Нед',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['sk'] = {
closeText: 'Zavrieť',
prevText: '&#x3C;Predchádzajúci',
nextText: 'Nasledujúci&#x3E;',
currentText: 'Dnes',
monthNames: ['január','február','marec','apríl','máj','jún',
'júl','august','september','október','november','december'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Máj','Jún',
'Júl','Aug','Sep','Okt','Nov','Dec'],
dayNames: ['nedeľa','pondelok','utorok','streda','štvrtok','piatok','sobota'],
dayNamesShort: ['Ned','Pon','Uto','Str','Štv','Pia','Sob'],
dayNamesMin: ['Ne','Po','Ut','St','Št','Pia','So'],
weekHeader: 'Ty',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['sl'] = {
closeText: 'Zapri',
prevText: '&#x3C;Prejšnji',
nextText: 'Naslednji&#x3E;',
currentText: 'Trenutni',
monthNames: ['Januar','Februar','Marec','April','Maj','Junij',
'Julij','Avgust','September','Oktober','November','December'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
'Jul','Avg','Sep','Okt','Nov','Dec'],
dayNames: ['Nedelja','Ponedeljek','Torek','Sreda','Četrtek','Petek','Sobota'],
dayNamesShort: ['Ned','Pon','Tor','Sre','Čet','Pet','Sob'],
dayNamesMin: ['Ne','Po','To','Sr','Če','Pe','So'],
weekHeader: 'Teden',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['sq'] = {
closeText: 'mbylle',
prevText: '&#x3C;mbrapa',
nextText: 'Përpara&#x3E;',
currentText: 'sot',
monthNames: ['Janar','Shkurt','Mars','Prill','Maj','Qershor',
'Korrik','Gusht','Shtator','Tetor','Nëntor','Dhjetor'],
monthNamesShort: ['Jan','Shk','Mar','Pri','Maj','Qer',
'Kor','Gus','Sht','Tet','Nën','Dhj'],
dayNames: ['E Diel','E Hënë','E Martë','E Mërkurë','E Enjte','E Premte','E Shtune'],
dayNamesShort: ['Di','Hë','Ma','Më','En','Pr','Sh'],
dayNamesMin: ['Di','Hë','Ma','Më','En','Pr','Sh'],
weekHeader: 'Ja',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['sr'] = {
closeText: 'Затвори',
prevText: '&#x3C;',
nextText: '&#x3E;',
currentText: 'Данас',
monthNames: ['Јануар','Фебруар','Март','Април','Мај','Јун',
'Јул','Август','Септембар','Октобар','Новембар','Децембар'],
monthNamesShort: ['Јан','Феб','Мар','Апр','Мај','Јун',
'Јул','Авг','Сеп','Окт','Нов','Дец'],
dayNames: ['Недеља','Понедељак','Уторак','Среда','Четвртак','Петак','Субота'],
dayNamesShort: ['Нед','Пон','Уто','Сре','Чет','Пет','Суб'],
dayNamesMin: ['Не','По','Ут','Ср','Че','Пе','Су'],
weekHeader: 'Сед',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['sr-SR'] = {
closeText: 'Zatvori',
prevText: '&#x3C;',
nextText: '&#x3E;',
currentText: 'Danas',
monthNames: ['Januar','Februar','Mart','April','Maj','Jun',
'Jul','Avgust','Septembar','Oktobar','Novembar','Decembar'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
'Jul','Avg','Sep','Okt','Nov','Dec'],
dayNames: ['Nedelja','Ponedeljak','Utorak','Sreda','Četvrtak','Petak','Subota'],
dayNamesShort: ['Ned','Pon','Uto','Sre','Čet','Pet','Sub'],
dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
weekHeader: 'Sed',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['sv'] = {
closeText: 'Stäng',
prevText: '&#xAB;Förra',
nextText: 'Nästa&#xBB;',
currentText: 'Idag',
monthNames: ['Januari','Februari','Mars','April','Maj','Juni',
'Juli','Augusti','September','Oktober','November','December'],
monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
'Jul','Aug','Sep','Okt','Nov','Dec'],
dayNamesShort: ['Sön','Mån','Tis','Ons','Tor','Fre','Lör'],
dayNames: ['Söndag','Måndag','Tisdag','Onsdag','Torsdag','Fredag','Lördag'],
dayNamesMin: ['Sö','Må','Ti','On','To','Fr','Lö'],
weekHeader: 'Ve',
dateFormat: 'yy-mm-dd',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['ta'] = {
closeText: 'மூடு',
prevText: 'முன்னையது',
nextText: 'அடுத்தது',
currentText: 'இன்று',
monthNames: ['தை','மாசி','பங்குனி','சித்திரை','வைகாசி','ஆனி',
'ஆடி','ஆவணி','புரட்டாசி','ஐப்பசி','கார்த்திகை','மார்கழி'],
monthNamesShort: ['தை','மாசி','பங்','சித்','வைகா','ஆனி',
'ஆடி','ஆவ','புர','ஐப்','கார்','மார்'],
dayNames: ['ஞாயிற்றுக்கிழமை','திங்கட்கிழமை','செவ்வாய்க்கிழமை','புதன்கிழமை','வியாழக்கிழமை','வெள்ளிக்கிழமை','சனிக்கிழமை'],
dayNamesShort: ['ஞாயிறு','திங்கள்','செவ்வாய்','புதன்','வியாழன்','வெள்ளி','சனி'],
dayNamesMin: ['ஞா','தி','செ','பு','வி','வெ','ச'],
weekHeader: 'Не',
dateFormat: 'dd/mm/yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['th'] = {
closeText: 'ปิด',
prevText: '&#xAB;&#xA0;ย้อน',
nextText: 'ถัดไป&#xA0;&#xBB;',
currentText: 'วันนี้',
monthNames: ['มกราคม','กุมภาพันธ์','มีนาคม','เมษายน','พฤษภาคม','มิถุนายน',
'กรกฎาคม','สิงหาคม','กันยายน','ตุลาคม','พฤศจิกายน','ธันวาคม'],
monthNamesShort: ['ม.ค.','ก.พ.','มี.ค.','เม.ย.','พ.ค.','มิ.ย.',
'ก.ค.','ส.ค.','ก.ย.','ต.ค.','พ.ย.','ธ.ค.'],
dayNames: ['อาทิตย์','จันทร์','อังคาร','พุธ','พฤหัสบดี','ศุกร์','เสาร์'],
dayNamesShort: ['อา.','จ.','อ.','พ.','พฤ.','ศ.','ส.'],
dayNamesMin: ['อา.','จ.','อ.','พ.','พฤ.','ศ.','ส.'],
weekHeader: 'Wk',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['tj'] = {
closeText: 'Идома',
prevText: '&#x3c;Қафо',
nextText: 'Пеш&#x3e;',
currentText: 'Имрӯз',
monthNames: ['Январ','Феврал','Март','Апрел','Май','Июн',
'Июл','Август','Сентябр','Октябр','Ноябр','Декабр'],
monthNamesShort: ['Янв','Фев','Мар','Апр','Май','Июн',
'Июл','Авг','Сен','Окт','Ноя','Дек'],
dayNames: ['якшанбе','душанбе','сешанбе','чоршанбе','панҷшанбе','ҷумъа','шанбе'],
dayNamesShort: ['якш','душ','сеш','чор','пан','ҷум','шан'],
dayNamesMin: ['Як','Дш','Сш','Чш','Пш','Ҷм','Шн'],
weekHeader: 'Хф',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['tr'] = {
closeText: 'kapat',
prevText: '&#x3C;geri',
nextText: 'ileri&#x3e',
currentText: 'bugün',
monthNames: ['Ocak','Şubat','Mart','Nisan','Mayıs','Haziran',
'Temmuz','Ağustos','Eylül','Ekim','Kasım','Aralık'],
monthNamesShort: ['Oca','Şub','Mar','Nis','May','Haz',
'Tem','Ağu','Eyl','Eki','Kas','Ara'],
dayNames: ['Pazar','Pazartesi','Salı','Çarşamba','Perşembe','Cuma','Cumartesi'],
dayNamesShort: ['Pz','Pt','Sa','Ça','Pe','Cu','Ct'],
dayNamesMin: ['Pz','Pt','Sa','Ça','Pe','Cu','Ct'],
weekHeader: 'Hf',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['uk'] = {
closeText: 'Закрити',
prevText: '&#x3C;',
nextText: '&#x3E;',
currentText: 'Сьогодні',
monthNames: ['Січень','Лютий','Березень','Квітень','Травень','Червень',
'Липень','Серпень','Вересень','Жовтень','Листопад','Грудень'],
monthNamesShort: ['Січ','Лют','Бер','Кві','Тра','Чер',
'Лип','Сер','Вер','Жов','Лис','Гру'],
dayNames: ['неділя','понеділок','вівторок','середа','четвер','п’ятниця','субота'],
dayNamesShort: ['нед','пнд','вів','срд','чтв','птн','сбт'],
dayNamesMin: ['Нд','Пн','Вт','Ср','Чт','Пт','Сб'],
weekHeader: 'Тиж',
dateFormat: 'dd.mm.yy',
firstDay: 1,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['vi'] = {
closeText: 'Đóng',
prevText: '&#x3C;Trước',
nextText: 'Tiếp&#x3E;',
currentText: 'Hôm nay',
monthNames: ['Tháng Một', 'Tháng Hai', 'Tháng Ba', 'Tháng Tư', 'Tháng Năm', 'Tháng Sáu',
'Tháng Bảy', 'Tháng Tám', 'Tháng Chín', 'Tháng Mười', 'Tháng Mười Một', 'Tháng Mười Hai'],
monthNamesShort: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'],
dayNames: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'],
dayNamesShort: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
dayNamesMin: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
weekHeader: 'Tu',
dateFormat: 'dd/mm/yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: false,
yearSuffix: ''};
$.datepicker.regional['zh-CN'] = {
closeText: '关闭',
prevText: '&#x3C;上月',
nextText: '下月&#x3E;',
currentText: '今天',
monthNames: ['一月','二月','三月','四月','五月','六月',
'七月','八月','九月','十月','十一月','十二月'],
monthNamesShort: ['一月','二月','三月','四月','五月','六月',
'七月','八月','九月','十月','十一月','十二月'],
dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
dayNamesMin: ['日','一','二','三','四','五','六'],
weekHeader: '周',
dateFormat: 'yy-mm-dd',
firstDay: 1,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: '年'};
$.datepicker.regional['zh-HK'] = {
closeText: '關閉',
prevText: '&#x3C;上月',
nextText: '下月&#x3E;',
currentText: '今天',
monthNames: ['一月','二月','三月','四月','五月','六月',
'七月','八月','九月','十月','十一月','十二月'],
monthNamesShort: ['一月','二月','三月','四月','五月','六月',
'七月','八月','九月','十月','十一月','十二月'],
dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
dayNamesMin: ['日','一','二','三','四','五','六'],
weekHeader: '周',
dateFormat: 'dd-mm-yy',
firstDay: 0,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: '年'};
$.datepicker.regional['zh-TW'] = {
closeText: '關閉',
prevText: '&#x3C;上月',
nextText: '下月&#x3E;',
currentText: '今天',
monthNames: ['一月','二月','三月','四月','五月','六月',
'七月','八月','九月','十月','十一月','十二月'],
monthNamesShort: ['一月','二月','三月','四月','五月','六月',
'七月','八月','九月','十月','十一月','十二月'],
dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
dayNamesMin: ['日','一','二','三','四','五','六'],
weekHeader: '周',
dateFormat: 'yy/mm/dd',
firstDay: 1,
isRTL: false,
showMonthAfterYear: true,
yearSuffix: '年'};
})(jQuery);

/*
 * jQuery UI Slider Access
 * By: Trent Richardson [http://trentrichardson.com]
 * Version 0.3
 * Last Modified: 10/20/2012
 * 
 * Copyright 2011 Trent Richardson
 * Dual licensed under the MIT and GPL licenses.
 * http://trentrichardson.com/Impromptu/GPL-LICENSE.txt
 * http://trentrichardson.com/Impromptu/MIT-LICENSE.txt
 * 
 */
!function(r){r.fn.extend({sliderAccess:function(u){return(u=u||{}).touchonly=void 0===u.touchonly||u.touchonly,!0!==u.touchonly||"ontouchend"in document?r(this).each(function(t,e){var c=r(this),n=r.extend({},{where:"after",step:c.slider("option","step"),upIcon:"ui-icon-plus",downIcon:"ui-icon-minus",text:!1,upText:"+",downText:"-",buttonset:!0,buttonsetTag:"span",isRTL:!1},u),o=r("<"+n.buttonsetTag+' class="ui-slider-access"><button data-icon="'+n.downIcon+'" data-step="'+(n.isRTL?n.step:-1*n.step)+'">'+n.downText+'</button><button data-icon="'+n.upIcon+'" data-step="'+(n.isRTL?-1*n.step:n.step)+'">'+n.upText+"</button></"+n.buttonsetTag+">");o.children("button").each(function(t,e){var a=r(this);a.button({text:n.text,icons:{primary:a.data("icon")}}).click(function(t){var e=a.data("step"),n=c.slider("value"),o=n+=1*e,i=c.slider("option","min"),s=c.slider("option","max"),u=c.slider("option","slide")||function(){},r=c.slider("option","stop")||function(){};t.preventDefault(),o<i||s<o||(c.slider("value",o),u.call(c,null,{value:o}),r.call(c,null,{value:o}))})}),c[n.where](o),n.buttonset&&(o.removeClass("ui-corner-right").removeClass("ui-corner-left").buttonset(),o.eq(0).addClass("ui-corner-left"),o.eq(1).addClass("ui-corner-right"));var i=o.css({marginLeft:"after"==n.where&&!n.isRTL||"before"==n.where&&n.isRTL?10:0,marginRight:"before"==n.where&&!n.isRTL||"after"==n.where&&n.isRTL?10:0}).outerWidth(!0)+5,s=c.outerWidth(!0);c.css("display","inline-block").width(s-i)}):r(this)}})}(jQuery);

/*! jQuery Timepicker Addon - v1.6.3 - 2016-04-20
* http://trentrichardson.com/examples/timepicker
* Copyright (c) 2016 Trent Richardson; Licensed MIT */
!function(a){"function"==typeof define&&define.amd?define(["jquery","jquery-ui"],a):a(jQuery)}(function($){if($.ui.timepicker=$.ui.timepicker||{},!$.ui.timepicker.version){$.extend($.ui,{timepicker:{version:"1.6.3"}});var Timepicker=function(){this.regional=[],this.regional[""]={currentText:"Now",closeText:"Done",amNames:["AM","A"],pmNames:["PM","P"],timeFormat:"HH:mm",timeSuffix:"",timeOnlyTitle:"Choose Time",timeText:"Time",hourText:"Hour",minuteText:"Minute",secondText:"Second",millisecText:"Millisecond",microsecText:"Microsecond",timezoneText:"Time Zone",isRTL:!1},this._defaults={showButtonPanel:!0,timeOnly:!1,timeOnlyShowDate:!1,showHour:null,showMinute:null,showSecond:null,showMillisec:null,showMicrosec:null,showTimezone:null,showTime:!0,stepHour:1,stepMinute:1,stepSecond:1,stepMillisec:1,stepMicrosec:1,hour:0,minute:0,second:0,millisec:0,microsec:0,timezone:null,hourMin:0,minuteMin:0,secondMin:0,millisecMin:0,microsecMin:0,hourMax:23,minuteMax:59,secondMax:59,millisecMax:999,microsecMax:999,minDateTime:null,maxDateTime:null,maxTime:null,minTime:null,onSelect:null,hourGrid:0,minuteGrid:0,secondGrid:0,millisecGrid:0,microsecGrid:0,alwaysSetTime:!0,separator:" ",altFieldTimeOnly:!0,altTimeFormat:null,altSeparator:null,altTimeSuffix:null,altRedirectFocus:!0,pickerTimeFormat:null,pickerTimeSuffix:null,showTimepicker:!0,timezoneList:null,addSliderAccess:!1,sliderAccessArgs:null,controlType:"slider",oneLine:!1,defaultValue:null,parse:"strict",afterInject:null},$.extend(this._defaults,this.regional[""])};$.extend(Timepicker.prototype,{$input:null,$altInput:null,$timeObj:null,inst:null,hour_slider:null,minute_slider:null,second_slider:null,millisec_slider:null,microsec_slider:null,timezone_select:null,maxTime:null,minTime:null,hour:0,minute:0,second:0,millisec:0,microsec:0,timezone:null,hourMinOriginal:null,minuteMinOriginal:null,secondMinOriginal:null,millisecMinOriginal:null,microsecMinOriginal:null,hourMaxOriginal:null,minuteMaxOriginal:null,secondMaxOriginal:null,millisecMaxOriginal:null,microsecMaxOriginal:null,ampm:"",formattedDate:"",formattedTime:"",formattedDateTime:"",timezoneList:null,units:["hour","minute","second","millisec","microsec"],support:{},control:null,setDefaults:function(a){return extendRemove(this._defaults,a||{}),this},_newInst:function($input,opts){var tp_inst=new Timepicker,inlineSettings={},fns={},overrides,i;for(var attrName in this._defaults)if(this._defaults.hasOwnProperty(attrName)){var attrValue=$input.attr("time:"+attrName);if(attrValue)try{inlineSettings[attrName]=eval(attrValue)}catch(err){inlineSettings[attrName]=attrValue}}overrides={beforeShow:function(a,b){return $.isFunction(tp_inst._defaults.evnts.beforeShow)?tp_inst._defaults.evnts.beforeShow.call($input[0],a,b,tp_inst):void 0},onChangeMonthYear:function(a,b,c){$.isFunction(tp_inst._defaults.evnts.onChangeMonthYear)&&tp_inst._defaults.evnts.onChangeMonthYear.call($input[0],a,b,c,tp_inst)},onClose:function(a,b){tp_inst.timeDefined===!0&&""!==$input.val()&&tp_inst._updateDateTime(b),$.isFunction(tp_inst._defaults.evnts.onClose)&&tp_inst._defaults.evnts.onClose.call($input[0],a,b,tp_inst)}};for(i in overrides)overrides.hasOwnProperty(i)&&(fns[i]=opts[i]||this._defaults[i]||null);tp_inst._defaults=$.extend({},this._defaults,inlineSettings,opts,overrides,{evnts:fns,timepicker:tp_inst}),tp_inst.amNames=$.map(tp_inst._defaults.amNames,function(a){return a.toUpperCase()}),tp_inst.pmNames=$.map(tp_inst._defaults.pmNames,function(a){return a.toUpperCase()}),tp_inst.support=detectSupport(tp_inst._defaults.timeFormat+(tp_inst._defaults.pickerTimeFormat?tp_inst._defaults.pickerTimeFormat:"")+(tp_inst._defaults.altTimeFormat?tp_inst._defaults.altTimeFormat:"")),"string"==typeof tp_inst._defaults.controlType?("slider"===tp_inst._defaults.controlType&&"undefined"==typeof $.ui.slider&&(tp_inst._defaults.controlType="select"),tp_inst.control=tp_inst._controls[tp_inst._defaults.controlType]):tp_inst.control=tp_inst._defaults.controlType;var timezoneList=[-720,-660,-600,-570,-540,-480,-420,-360,-300,-270,-240,-210,-180,-120,-60,0,60,120,180,210,240,270,300,330,345,360,390,420,480,525,540,570,600,630,660,690,720,765,780,840];null!==tp_inst._defaults.timezoneList&&(timezoneList=tp_inst._defaults.timezoneList);var tzl=timezoneList.length,tzi=0,tzv=null;if(tzl>0&&"object"!=typeof timezoneList[0])for(;tzl>tzi;tzi++)tzv=timezoneList[tzi],timezoneList[tzi]={value:tzv,label:$.timepicker.timezoneOffsetString(tzv,tp_inst.support.iso8601)};return tp_inst._defaults.timezoneList=timezoneList,tp_inst.timezone=null!==tp_inst._defaults.timezone?$.timepicker.timezoneOffsetNumber(tp_inst._defaults.timezone):-1*(new Date).getTimezoneOffset(),tp_inst.hour=tp_inst._defaults.hour<tp_inst._defaults.hourMin?tp_inst._defaults.hourMin:tp_inst._defaults.hour>tp_inst._defaults.hourMax?tp_inst._defaults.hourMax:tp_inst._defaults.hour,tp_inst.minute=tp_inst._defaults.minute<tp_inst._defaults.minuteMin?tp_inst._defaults.minuteMin:tp_inst._defaults.minute>tp_inst._defaults.minuteMax?tp_inst._defaults.minuteMax:tp_inst._defaults.minute,tp_inst.second=tp_inst._defaults.second<tp_inst._defaults.secondMin?tp_inst._defaults.secondMin:tp_inst._defaults.second>tp_inst._defaults.secondMax?tp_inst._defaults.secondMax:tp_inst._defaults.second,tp_inst.millisec=tp_inst._defaults.millisec<tp_inst._defaults.millisecMin?tp_inst._defaults.millisecMin:tp_inst._defaults.millisec>tp_inst._defaults.millisecMax?tp_inst._defaults.millisecMax:tp_inst._defaults.millisec,tp_inst.microsec=tp_inst._defaults.microsec<tp_inst._defaults.microsecMin?tp_inst._defaults.microsecMin:tp_inst._defaults.microsec>tp_inst._defaults.microsecMax?tp_inst._defaults.microsecMax:tp_inst._defaults.microsec,tp_inst.ampm="",tp_inst.$input=$input,tp_inst._defaults.altField&&(tp_inst.$altInput=$(tp_inst._defaults.altField),tp_inst._defaults.altRedirectFocus===!0&&tp_inst.$altInput.css({cursor:"pointer"}).focus(function(){$input.trigger("focus")})),(0===tp_inst._defaults.minDate||0===tp_inst._defaults.minDateTime)&&(tp_inst._defaults.minDate=new Date),(0===tp_inst._defaults.maxDate||0===tp_inst._defaults.maxDateTime)&&(tp_inst._defaults.maxDate=new Date),void 0!==tp_inst._defaults.minDate&&tp_inst._defaults.minDate instanceof Date&&(tp_inst._defaults.minDateTime=new Date(tp_inst._defaults.minDate.getTime())),void 0!==tp_inst._defaults.minDateTime&&tp_inst._defaults.minDateTime instanceof Date&&(tp_inst._defaults.minDate=new Date(tp_inst._defaults.minDateTime.getTime())),void 0!==tp_inst._defaults.maxDate&&tp_inst._defaults.maxDate instanceof Date&&(tp_inst._defaults.maxDateTime=new Date(tp_inst._defaults.maxDate.getTime())),void 0!==tp_inst._defaults.maxDateTime&&tp_inst._defaults.maxDateTime instanceof Date&&(tp_inst._defaults.maxDate=new Date(tp_inst._defaults.maxDateTime.getTime())),tp_inst.$input.bind("focus",function(){tp_inst._onFocus()}),tp_inst},_addTimePicker:function(a){var b=$.trim(this.$altInput&&this._defaults.altFieldTimeOnly?this.$input.val()+" "+this.$altInput.val():this.$input.val());this.timeDefined=this._parseTime(b),this._limitMinMaxDateTime(a,!1),this._injectTimePicker(),this._afterInject()},_parseTime:function(a,b){if(this.inst||(this.inst=$.datepicker._getInst(this.$input[0])),b||!this._defaults.timeOnly){var c=$.datepicker._get(this.inst,"dateFormat");try{var d=parseDateTimeInternal(c,this._defaults.timeFormat,a,$.datepicker._getFormatConfig(this.inst),this._defaults);if(!d.timeObj)return!1;$.extend(this,d.timeObj)}catch(e){return $.timepicker.log("Error parsing the date/time string: "+e+"\ndate/time string = "+a+"\ntimeFormat = "+this._defaults.timeFormat+"\ndateFormat = "+c),!1}return!0}var f=$.datepicker.parseTime(this._defaults.timeFormat,a,this._defaults);return f?($.extend(this,f),!0):!1},_afterInject:function(){var a=this.inst.settings;$.isFunction(a.afterInject)&&a.afterInject.call(this)},_injectTimePicker:function(){var a=this.inst.dpDiv,b=this.inst.settings,c=this,d="",e="",f=null,g={},h={},i=null,j=0,k=0;if(0===a.find("div.ui-timepicker-div").length&&b.showTimepicker){var l=" ui_tpicker_unit_hide",m='<div class="ui-timepicker-div'+(b.isRTL?" ui-timepicker-rtl":"")+(b.oneLine&&"select"===b.controlType?" ui-timepicker-oneLine":"")+'"><dl><dt class="ui_tpicker_time_label'+(b.showTime?"":l)+'">'+b.timeText+'</dt><dd class="ui_tpicker_time '+(b.showTime?"":l)+'"><input class="ui_tpicker_time_input" '+(b.timeInput?"":"disabled")+"/></dd>";for(j=0,k=this.units.length;k>j;j++){if(d=this.units[j],e=d.substr(0,1).toUpperCase()+d.substr(1),f=null!==b["show"+e]?b["show"+e]:this.support[d],g[d]=parseInt(b[d+"Max"]-(b[d+"Max"]-b[d+"Min"])%b["step"+e],10),h[d]=0,m+='<dt class="ui_tpicker_'+d+"_label"+(f?"":l)+'">'+b[d+"Text"]+'</dt><dd class="ui_tpicker_'+d+(f?"":l)+'"><div class="ui_tpicker_'+d+"_slider"+(f?"":l)+'"></div>',f&&b[d+"Grid"]>0){if(m+='<div style="padding-left: 1px"><table class="ui-tpicker-grid-label"><tr>',"hour"===d)for(var n=b[d+"Min"];n<=g[d];n+=parseInt(b[d+"Grid"],10)){h[d]++;var o=$.datepicker.formatTime(this.support.ampm?"hht":"HH",{hour:n},b);m+='<td data-for="'+d+'">'+o+"</td>"}else for(var p=b[d+"Min"];p<=g[d];p+=parseInt(b[d+"Grid"],10))h[d]++,m+='<td data-for="'+d+'">'+(10>p?"0":"")+p+"</td>";m+="</tr></table></div>"}m+="</dd>"}var q=null!==b.showTimezone?b.showTimezone:this.support.timezone;m+='<dt class="ui_tpicker_timezone_label'+(q?"":l)+'">'+b.timezoneText+"</dt>",m+='<dd class="ui_tpicker_timezone'+(q?"":l)+'"></dd>',m+="</dl></div>";var r=$(m);for(b.timeOnly===!0&&(r.prepend('<div class="ui-widget-header ui-helper-clearfix ui-corner-all"><div class="ui-datepicker-title">'+b.timeOnlyTitle+"</div></div>"),a.find(".ui-datepicker-header, .ui-datepicker-calendar").hide()),j=0,k=c.units.length;k>j;j++)d=c.units[j],e=d.substr(0,1).toUpperCase()+d.substr(1),f=null!==b["show"+e]?b["show"+e]:this.support[d],c[d+"_slider"]=c.control.create(c,r.find(".ui_tpicker_"+d+"_slider"),d,c[d],b[d+"Min"],g[d],b["step"+e]),f&&b[d+"Grid"]>0&&(i=100*h[d]*b[d+"Grid"]/(g[d]-b[d+"Min"]),r.find(".ui_tpicker_"+d+" table").css({width:i+"%",marginLeft:b.isRTL?"0":i/(-2*h[d])+"%",marginRight:b.isRTL?i/(-2*h[d])+"%":"0",borderCollapse:"collapse"}).find("td").click(function(a){var b=$(this),e=b.html(),f=parseInt(e.replace(/[^0-9]/g),10),g=e.replace(/[^apm]/gi),h=b.data("for");"hour"===h&&(-1!==g.indexOf("p")&&12>f?f+=12:-1!==g.indexOf("a")&&12===f&&(f=0)),c.control.value(c,c[h+"_slider"],d,f),c._onTimeChange(),c._onSelectHandler()}).css({cursor:"pointer",width:100/h[d]+"%",textAlign:"center",overflow:"hidden"}));if(this.timezone_select=r.find(".ui_tpicker_timezone").append("<select></select>").find("select"),$.fn.append.apply(this.timezone_select,$.map(b.timezoneList,function(a,b){return $("<option />").val("object"==typeof a?a.value:a).text("object"==typeof a?a.label:a)})),"undefined"!=typeof this.timezone&&null!==this.timezone&&""!==this.timezone){var s=-1*new Date(this.inst.selectedYear,this.inst.selectedMonth,this.inst.selectedDay,12).getTimezoneOffset();s===this.timezone?selectLocalTimezone(c):this.timezone_select.val(this.timezone)}else"undefined"!=typeof this.hour&&null!==this.hour&&""!==this.hour?this.timezone_select.val(b.timezone):selectLocalTimezone(c);this.timezone_select.change(function(){c._onTimeChange(),c._onSelectHandler(),c._afterInject()});var t=a.find(".ui-datepicker-buttonpane");if(t.length?t.before(r):a.append(r),this.$timeObj=r.find(".ui_tpicker_time_input"),this.$timeObj.change(function(){var a=c.inst.settings.timeFormat,b=$.datepicker.parseTime(a,this.value),d=new Date;b?(d.setHours(b.hour),d.setMinutes(b.minute),d.setSeconds(b.second),$.datepicker._setTime(c.inst,d)):(this.value=c.formattedTime,this.blur())}),null!==this.inst){var u=this.timeDefined;this._onTimeChange(),this.timeDefined=u}if(this._defaults.addSliderAccess){var v=this._defaults.sliderAccessArgs,w=this._defaults.isRTL;v.isRTL=w,setTimeout(function(){if(0===r.find(".ui-slider-access").length){r.find(".ui-slider:visible").sliderAccess(v);var a=r.find(".ui-slider-access:eq(0)").outerWidth(!0);a&&r.find("table:visible").each(function(){var b=$(this),c=b.outerWidth(),d=b.css(w?"marginRight":"marginLeft").toString().replace("%",""),e=c-a,f=d*e/c+"%",g={width:e,marginRight:0,marginLeft:0};g[w?"marginRight":"marginLeft"]=f,b.css(g)})}},10)}c._limitMinMaxDateTime(this.inst,!0)}},_limitMinMaxDateTime:function(a,b){var c=this._defaults,d=new Date(a.selectedYear,a.selectedMonth,a.selectedDay);if(this._defaults.showTimepicker){if(null!==$.datepicker._get(a,"minDateTime")&&void 0!==$.datepicker._get(a,"minDateTime")&&d){var e=$.datepicker._get(a,"minDateTime"),f=new Date(e.getFullYear(),e.getMonth(),e.getDate(),0,0,0,0);(null===this.hourMinOriginal||null===this.minuteMinOriginal||null===this.secondMinOriginal||null===this.millisecMinOriginal||null===this.microsecMinOriginal)&&(this.hourMinOriginal=c.hourMin,this.minuteMinOriginal=c.minuteMin,this.secondMinOriginal=c.secondMin,this.millisecMinOriginal=c.millisecMin,this.microsecMinOriginal=c.microsecMin),a.settings.timeOnly||f.getTime()===d.getTime()?(this._defaults.hourMin=e.getHours(),this.hour<=this._defaults.hourMin?(this.hour=this._defaults.hourMin,this._defaults.minuteMin=e.getMinutes(),this.minute<=this._defaults.minuteMin?(this.minute=this._defaults.minuteMin,this._defaults.secondMin=e.getSeconds(),this.second<=this._defaults.secondMin?(this.second=this._defaults.secondMin,this._defaults.millisecMin=e.getMilliseconds(),this.millisec<=this._defaults.millisecMin?(this.millisec=this._defaults.millisecMin,this._defaults.microsecMin=e.getMicroseconds()):(this.microsec<this._defaults.microsecMin&&(this.microsec=this._defaults.microsecMin),this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.secondMin=this.secondMinOriginal,this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.minuteMin=this.minuteMinOriginal,this._defaults.secondMin=this.secondMinOriginal,this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.hourMin=this.hourMinOriginal,this._defaults.minuteMin=this.minuteMinOriginal,this._defaults.secondMin=this.secondMinOriginal,this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)}if(null!==$.datepicker._get(a,"maxDateTime")&&void 0!==$.datepicker._get(a,"maxDateTime")&&d){var g=$.datepicker._get(a,"maxDateTime"),h=new Date(g.getFullYear(),g.getMonth(),g.getDate(),0,0,0,0);(null===this.hourMaxOriginal||null===this.minuteMaxOriginal||null===this.secondMaxOriginal||null===this.millisecMaxOriginal)&&(this.hourMaxOriginal=c.hourMax,this.minuteMaxOriginal=c.minuteMax,this.secondMaxOriginal=c.secondMax,this.millisecMaxOriginal=c.millisecMax,this.microsecMaxOriginal=c.microsecMax),a.settings.timeOnly||h.getTime()===d.getTime()?(this._defaults.hourMax=g.getHours(),this.hour>=this._defaults.hourMax?(this.hour=this._defaults.hourMax,this._defaults.minuteMax=g.getMinutes(),this.minute>=this._defaults.minuteMax?(this.minute=this._defaults.minuteMax,this._defaults.secondMax=g.getSeconds(),this.second>=this._defaults.secondMax?(this.second=this._defaults.secondMax,this._defaults.millisecMax=g.getMilliseconds(),this.millisec>=this._defaults.millisecMax?(this.millisec=this._defaults.millisecMax,this._defaults.microsecMax=g.getMicroseconds()):(this.microsec>this._defaults.microsecMax&&(this.microsec=this._defaults.microsecMax),this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.secondMax=this.secondMaxOriginal,this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.minuteMax=this.minuteMaxOriginal,this._defaults.secondMax=this.secondMaxOriginal,this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.hourMax=this.hourMaxOriginal,this._defaults.minuteMax=this.minuteMaxOriginal,this._defaults.secondMax=this.secondMaxOriginal,this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)}if(null!==a.settings.minTime){var i=new Date("01/01/1970 "+a.settings.minTime);this.hour<i.getHours()?(this.hour=this._defaults.hourMin=i.getHours(),this.minute=this._defaults.minuteMin=i.getMinutes()):this.hour===i.getHours()&&this.minute<i.getMinutes()?this.minute=this._defaults.minuteMin=i.getMinutes():this._defaults.hourMin<i.getHours()?(this._defaults.hourMin=i.getHours(),this._defaults.minuteMin=i.getMinutes()):this._defaults.hourMin===i.getHours()===this.hour&&this._defaults.minuteMin<i.getMinutes()?this._defaults.minuteMin=i.getMinutes():this._defaults.minuteMin=0}if(null!==a.settings.maxTime){var j=new Date("01/01/1970 "+a.settings.maxTime);this.hour>j.getHours()?(this.hour=this._defaults.hourMax=j.getHours(),this.minute=this._defaults.minuteMax=j.getMinutes()):this.hour===j.getHours()&&this.minute>j.getMinutes()?this.minute=this._defaults.minuteMax=j.getMinutes():this._defaults.hourMax>j.getHours()?(this._defaults.hourMax=j.getHours(),this._defaults.minuteMax=j.getMinutes()):this._defaults.hourMax===j.getHours()===this.hour&&this._defaults.minuteMax>j.getMinutes()?this._defaults.minuteMax=j.getMinutes():this._defaults.minuteMax=59}if(void 0!==b&&b===!0){var k=parseInt(this._defaults.hourMax-(this._defaults.hourMax-this._defaults.hourMin)%this._defaults.stepHour,10),l=parseInt(this._defaults.minuteMax-(this._defaults.minuteMax-this._defaults.minuteMin)%this._defaults.stepMinute,10),m=parseInt(this._defaults.secondMax-(this._defaults.secondMax-this._defaults.secondMin)%this._defaults.stepSecond,10),n=parseInt(this._defaults.millisecMax-(this._defaults.millisecMax-this._defaults.millisecMin)%this._defaults.stepMillisec,10),o=parseInt(this._defaults.microsecMax-(this._defaults.microsecMax-this._defaults.microsecMin)%this._defaults.stepMicrosec,10);this.hour_slider&&(this.control.options(this,this.hour_slider,"hour",{min:this._defaults.hourMin,max:k,step:this._defaults.stepHour}),this.control.value(this,this.hour_slider,"hour",this.hour-this.hour%this._defaults.stepHour)),this.minute_slider&&(this.control.options(this,this.minute_slider,"minute",{min:this._defaults.minuteMin,max:l,step:this._defaults.stepMinute}),this.control.value(this,this.minute_slider,"minute",this.minute-this.minute%this._defaults.stepMinute)),this.second_slider&&(this.control.options(this,this.second_slider,"second",{min:this._defaults.secondMin,max:m,step:this._defaults.stepSecond}),this.control.value(this,this.second_slider,"second",this.second-this.second%this._defaults.stepSecond)),this.millisec_slider&&(this.control.options(this,this.millisec_slider,"millisec",{min:this._defaults.millisecMin,max:n,step:this._defaults.stepMillisec}),this.control.value(this,this.millisec_slider,"millisec",this.millisec-this.millisec%this._defaults.stepMillisec)),this.microsec_slider&&(this.control.options(this,this.microsec_slider,"microsec",{min:this._defaults.microsecMin,max:o,step:this._defaults.stepMicrosec}),this.control.value(this,this.microsec_slider,"microsec",this.microsec-this.microsec%this._defaults.stepMicrosec))}}},_onTimeChange:function(){if(this._defaults.showTimepicker){var a=this.hour_slider?this.control.value(this,this.hour_slider,"hour"):!1,b=this.minute_slider?this.control.value(this,this.minute_slider,"minute"):!1,c=this.second_slider?this.control.value(this,this.second_slider,"second"):!1,d=this.millisec_slider?this.control.value(this,this.millisec_slider,"millisec"):!1,e=this.microsec_slider?this.control.value(this,this.microsec_slider,"microsec"):!1,f=this.timezone_select?this.timezone_select.val():!1,g=this._defaults,h=g.pickerTimeFormat||g.timeFormat,i=g.pickerTimeSuffix||g.timeSuffix;"object"==typeof a&&(a=!1),"object"==typeof b&&(b=!1),"object"==typeof c&&(c=!1),"object"==typeof d&&(d=!1),"object"==typeof e&&(e=!1),"object"==typeof f&&(f=!1),a!==!1&&(a=parseInt(a,10)),b!==!1&&(b=parseInt(b,10)),c!==!1&&(c=parseInt(c,10)),d!==!1&&(d=parseInt(d,10)),e!==!1&&(e=parseInt(e,10)),f!==!1&&(f=f.toString());var j=g[12>a?"amNames":"pmNames"][0],k=a!==parseInt(this.hour,10)||b!==parseInt(this.minute,10)||c!==parseInt(this.second,10)||d!==parseInt(this.millisec,10)||e!==parseInt(this.microsec,10)||this.ampm.length>0&&12>a!=(-1!==$.inArray(this.ampm.toUpperCase(),this.amNames))||null!==this.timezone&&f!==this.timezone.toString();if(k&&(a!==!1&&(this.hour=a),b!==!1&&(this.minute=b),c!==!1&&(this.second=c),d!==!1&&(this.millisec=d),e!==!1&&(this.microsec=e),f!==!1&&(this.timezone=f),this.inst||(this.inst=$.datepicker._getInst(this.$input[0])),this._limitMinMaxDateTime(this.inst,!0)),this.support.ampm&&(this.ampm=j),this.formattedTime=$.datepicker.formatTime(g.timeFormat,this,g),this.$timeObj&&(this.$timeObj.val(h===g.timeFormat?this.formattedTime+i:$.datepicker.formatTime(h,this,g)+i),this.$timeObj[0].setSelectionRange)){var l=this.$timeObj[0].selectionStart,m=this.$timeObj[0].selectionEnd;this.$timeObj[0].setSelectionRange(l,m)}this.timeDefined=!0,k&&this._updateDateTime()}},_onSelectHandler:function(){var a=this._defaults.onSelect||this.inst.settings.onSelect,b=this.$input?this.$input[0]:null;a&&b&&a.apply(b,[this.formattedDateTime,this])},_updateDateTime:function(a){a=this.inst||a;var b=a.currentYear>0?new Date(a.currentYear,a.currentMonth,a.currentDay):new Date(a.selectedYear,a.selectedMonth,a.selectedDay),c=$.datepicker._daylightSavingAdjust(b),d=$.datepicker._get(a,"dateFormat"),e=$.datepicker._getFormatConfig(a),f=null!==c&&this.timeDefined;this.formattedDate=$.datepicker.formatDate(d,null===c?new Date:c,e);var g=this.formattedDate;if(""===a.lastVal&&(a.currentYear=a.selectedYear,a.currentMonth=a.selectedMonth,a.currentDay=a.selectedDay),this._defaults.timeOnly===!0&&this._defaults.timeOnlyShowDate===!1?g=this.formattedTime:(this._defaults.timeOnly!==!0&&(this._defaults.alwaysSetTime||f)||this._defaults.timeOnly===!0&&this._defaults.timeOnlyShowDate===!0)&&(g+=this._defaults.separator+this.formattedTime+this._defaults.timeSuffix),this.formattedDateTime=g,this._defaults.showTimepicker)if(this.$altInput&&this._defaults.timeOnly===!1&&this._defaults.altFieldTimeOnly===!0)this.$altInput.val(this.formattedTime),this.$input.val(this.formattedDate);else if(this.$altInput){this.$input.val(g);var h="",i=null!==this._defaults.altSeparator?this._defaults.altSeparator:this._defaults.separator,j=null!==this._defaults.altTimeSuffix?this._defaults.altTimeSuffix:this._defaults.timeSuffix;this._defaults.timeOnly||(h=this._defaults.altFormat?$.datepicker.formatDate(this._defaults.altFormat,null===c?new Date:c,e):this.formattedDate,h&&(h+=i)),h+=null!==this._defaults.altTimeFormat?$.datepicker.formatTime(this._defaults.altTimeFormat,this,this._defaults)+j:this.formattedTime+j,this.$altInput.val(h)}else this.$input.val(g);else this.$input.val(this.formattedDate);this.$input.trigger("change")},_onFocus:function(){if(!this.$input.val()&&this._defaults.defaultValue){this.$input.val(this._defaults.defaultValue);var a=$.datepicker._getInst(this.$input.get(0)),b=$.datepicker._get(a,"timepicker");if(b&&b._defaults.timeOnly&&a.input.val()!==a.lastVal)try{$.datepicker._updateDatepicker(a)}catch(c){$.timepicker.log(c)}}},_controls:{slider:{create:function(a,b,c,d,e,f,g){var h=a._defaults.isRTL;return b.prop("slide",null).slider({orientation:"horizontal",value:h?-1*d:d,min:h?-1*f:e,max:h?-1*e:f,step:g,slide:function(b,d){a.control.value(a,$(this),c,h?-1*d.value:d.value),a._onTimeChange()},stop:function(b,c){a._onSelectHandler()}})},options:function(a,b,c,d,e){if(a._defaults.isRTL){if("string"==typeof d)return"min"===d||"max"===d?void 0!==e?b.slider(d,-1*e):Math.abs(b.slider(d)):b.slider(d);var f=d.min,g=d.max;return d.min=d.max=null,void 0!==f&&(d.max=-1*f),void 0!==g&&(d.min=-1*g),b.slider(d)}return"string"==typeof d&&void 0!==e?b.slider(d,e):b.slider(d)},value:function(a,b,c,d){return a._defaults.isRTL?void 0!==d?b.slider("value",-1*d):Math.abs(b.slider("value")):void 0!==d?b.slider("value",d):b.slider("value")}},select:{create:function(a,b,c,d,e,f,g){for(var h='<select class="ui-timepicker-select ui-state-default ui-corner-all" data-unit="'+c+'" data-min="'+e+'" data-max="'+f+'" data-step="'+g+'">',i=a._defaults.pickerTimeFormat||a._defaults.timeFormat,j=e;f>=j;j+=g)h+='<option value="'+j+'"'+(j===d?" selected":"")+">",h+="hour"===c?$.datepicker.formatTime($.trim(i.replace(/[^ht ]/gi,"")),{hour:j},a._defaults):"millisec"===c||"microsec"===c||j>=10?j:"0"+j.toString(),h+="</option>";return h+="</select>",b.children("select").remove(),$(h).appendTo(b).change(function(b){a._onTimeChange(),a._onSelectHandler(),a._afterInject()}),b},options:function(a,b,c,d,e){var f={},g=b.children("select");if("string"==typeof d){if(void 0===e)return g.data(d);f[d]=e}else f=d;return a.control.create(a,b,g.data("unit"),g.val(),f.min>=0?f.min:g.data("min"),f.max||g.data("max"),f.step||g.data("step"))},value:function(a,b,c,d){var e=b.children("select");return void 0!==d?e.val(d):e.val()}}}}),$.fn.extend({timepicker:function(a){a=a||{};var b=Array.prototype.slice.call(arguments);return"object"==typeof a&&(b[0]=$.extend(a,{timeOnly:!0})),$(this).each(function(){$.fn.datetimepicker.apply($(this),b)})},datetimepicker:function(a){a=a||{};var b=arguments;return"string"==typeof a?"getDate"===a||"option"===a&&2===b.length&&"string"==typeof b[1]?$.fn.datepicker.apply($(this[0]),b):this.each(function(){var a=$(this);a.datepicker.apply(a,b)}):this.each(function(){var b=$(this);b.datepicker($.timepicker._newInst(b,a)._defaults)})}}),$.datepicker.parseDateTime=function(a,b,c,d,e){var f=parseDateTimeInternal(a,b,c,d,e);if(f.timeObj){var g=f.timeObj;f.date.setHours(g.hour,g.minute,g.second,g.millisec),f.date.setMicroseconds(g.microsec)}return f.date},$.datepicker.parseTime=function(a,b,c){var d=extendRemove(extendRemove({},$.timepicker._defaults),c||{}),e=(-1!==a.replace(/\'.*?\'/g,"").indexOf("Z"),function(a,b,c){var d,e=function(a,b){var c=[];return a&&$.merge(c,a),b&&$.merge(c,b),c=$.map(c,function(a){return a.replace(/[.*+?|()\[\]{}\\]/g,"\\$&")}),"("+c.join("|")+")?"},f=function(a){var b=a.toLowerCase().match(/(h{1,2}|m{1,2}|s{1,2}|l{1}|c{1}|t{1,2}|z|'.*?')/g),c={h:-1,m:-1,s:-1,l:-1,c:-1,t:-1,z:-1};if(b)for(var d=0;d<b.length;d++)-1===c[b[d].toString().charAt(0)]&&(c[b[d].toString().charAt(0)]=d+1);return c},g="^"+a.toString().replace(/([hH]{1,2}|mm?|ss?|[tT]{1,2}|[zZ]|[lc]|'.*?')/g,function(a){var b=a.length;switch(a.charAt(0).toLowerCase()){case"h":return 1===b?"(\\d?\\d)":"(\\d{"+b+"})";case"m":return 1===b?"(\\d?\\d)":"(\\d{"+b+"})";case"s":return 1===b?"(\\d?\\d)":"(\\d{"+b+"})";case"l":return"(\\d?\\d?\\d)";case"c":return"(\\d?\\d?\\d)";case"z":return"(z|[-+]\\d\\d:?\\d\\d|\\S+)?";case"t":return e(c.amNames,c.pmNames);default:return"("+a.replace(/\'/g,"").replace(/(\.|\$|\^|\\|\/|\(|\)|\[|\]|\?|\+|\*)/g,function(a){return"\\"+a})+")?"}}).replace(/\s/g,"\\s?")+c.timeSuffix+"$",h=f(a),i="";d=b.match(new RegExp(g,"i"));var j={hour:0,minute:0,second:0,millisec:0,microsec:0};return d?(-1!==h.t&&(void 0===d[h.t]||0===d[h.t].length?(i="",j.ampm=""):(i=-1!==$.inArray(d[h.t].toUpperCase(),$.map(c.amNames,function(a,b){return a.toUpperCase()}))?"AM":"PM",j.ampm=c["AM"===i?"amNames":"pmNames"][0])),-1!==h.h&&("AM"===i&&"12"===d[h.h]?j.hour=0:"PM"===i&&"12"!==d[h.h]?j.hour=parseInt(d[h.h],10)+12:j.hour=Number(d[h.h])),-1!==h.m&&(j.minute=Number(d[h.m])),-1!==h.s&&(j.second=Number(d[h.s])),-1!==h.l&&(j.millisec=Number(d[h.l])),-1!==h.c&&(j.microsec=Number(d[h.c])),-1!==h.z&&void 0!==d[h.z]&&(j.timezone=$.timepicker.timezoneOffsetNumber(d[h.z])),j):!1}),f=function(a,b,c){try{var d=new Date("2012-01-01 "+b);if(isNaN(d.getTime())&&(d=new Date("2012-01-01T"+b),isNaN(d.getTime())&&(d=new Date("01/01/2012 "+b),isNaN(d.getTime()))))throw"Unable to parse time with native Date: "+b;return{hour:d.getHours(),minute:d.getMinutes(),second:d.getSeconds(),millisec:d.getMilliseconds(),microsec:d.getMicroseconds(),timezone:-1*d.getTimezoneOffset()}}catch(f){try{return e(a,b,c)}catch(g){$.timepicker.log("Unable to parse \ntimeString: "+b+"\ntimeFormat: "+a)}}return!1};return"function"==typeof d.parse?d.parse(a,b,d):"loose"===d.parse?f(a,b,d):e(a,b,d)},$.datepicker.formatTime=function(a,b,c){c=c||{},c=$.extend({},$.timepicker._defaults,c),b=$.extend({hour:0,minute:0,second:0,millisec:0,microsec:0,timezone:null},b);var d=a,e=c.amNames[0],f=parseInt(b.hour,10);return f>11&&(e=c.pmNames[0]),d=d.replace(/(?:HH?|hh?|mm?|ss?|[tT]{1,2}|[zZ]|[lc]|'.*?')/g,function(a){switch(a){case"HH":return("0"+f).slice(-2);case"H":return f;case"hh":return("0"+convert24to12(f)).slice(-2);case"h":return convert24to12(f);case"mm":return("0"+b.minute).slice(-2);case"m":return b.minute;case"ss":return("0"+b.second).slice(-2);case"s":return b.second;case"l":return("00"+b.millisec).slice(-3);case"c":return("00"+b.microsec).slice(-3);case"z":return $.timepicker.timezoneOffsetString(null===b.timezone?c.timezone:b.timezone,!1);case"Z":return $.timepicker.timezoneOffsetString(null===b.timezone?c.timezone:b.timezone,!0);case"T":return e.charAt(0).toUpperCase();case"TT":return e.toUpperCase();case"t":return e.charAt(0).toLowerCase();case"tt":return e.toLowerCase();default:return a.replace(/'/g,"")}})},$.datepicker._base_selectDate=$.datepicker._selectDate,$.datepicker._selectDate=function(a,b){var c,d=this._getInst($(a)[0]),e=this._get(d,"timepicker");e&&d.settings.showTimepicker?(e._limitMinMaxDateTime(d,!0),c=d.inline,d.inline=d.stay_open=!0,this._base_selectDate(a,b),d.inline=c,d.stay_open=!1,this._notifyChange(d),this._updateDatepicker(d)):this._base_selectDate(a,b)},$.datepicker._base_updateDatepicker=$.datepicker._updateDatepicker,$.datepicker._updateDatepicker=function(a){var b=a.input[0];if(!($.datepicker._curInst&&$.datepicker._curInst!==a&&$.datepicker._datepickerShowing&&$.datepicker._lastInput!==b||"boolean"==typeof a.stay_open&&a.stay_open!==!1)){this._base_updateDatepicker(a);var c=this._get(a,"timepicker");c&&c._addTimePicker(a)}},$.datepicker._base_doKeyPress=$.datepicker._doKeyPress,$.datepicker._doKeyPress=function(a){var b=$.datepicker._getInst(a.target),c=$.datepicker._get(b,"timepicker");if(c&&$.datepicker._get(b,"constrainInput")){var d=c.support.ampm,e=null!==c._defaults.showTimezone?c._defaults.showTimezone:c.support.timezone,f=$.datepicker._possibleChars($.datepicker._get(b,"dateFormat")),g=c._defaults.timeFormat.toString().replace(/[hms]/g,"").replace(/TT/g,d?"APM":"").replace(/Tt/g,d?"AaPpMm":"").replace(/tT/g,d?"AaPpMm":"").replace(/T/g,d?"AP":"").replace(/tt/g,d?"apm":"").replace(/t/g,d?"ap":"")+" "+c._defaults.separator+c._defaults.timeSuffix+(e?c._defaults.timezoneList.join(""):"")+c._defaults.amNames.join("")+c._defaults.pmNames.join("")+f,h=String.fromCharCode(void 0===a.charCode?a.keyCode:a.charCode);return a.ctrlKey||" ">h||!f||g.indexOf(h)>-1}return $.datepicker._base_doKeyPress(a)},$.datepicker._base_updateAlternate=$.datepicker._updateAlternate,$.datepicker._updateAlternate=function(a){var b=this._get(a,"timepicker");if(b){var c=b._defaults.altField;if(c){var d=(b._defaults.altFormat||b._defaults.dateFormat,this._getDate(a)),e=$.datepicker._getFormatConfig(a),f="",g=b._defaults.altSeparator?b._defaults.altSeparator:b._defaults.separator,h=b._defaults.altTimeSuffix?b._defaults.altTimeSuffix:b._defaults.timeSuffix,i=null!==b._defaults.altTimeFormat?b._defaults.altTimeFormat:b._defaults.timeFormat;f+=$.datepicker.formatTime(i,b,b._defaults)+h,b._defaults.timeOnly||b._defaults.altFieldTimeOnly||null===d||(f=b._defaults.altFormat?$.datepicker.formatDate(b._defaults.altFormat,d,e)+g+f:b.formattedDate+g+f),$(c).val(a.input.val()?f:"")}}else $.datepicker._base_updateAlternate(a)},$.datepicker._base_doKeyUp=$.datepicker._doKeyUp,$.datepicker._doKeyUp=function(a){var b=$.datepicker._getInst(a.target),c=$.datepicker._get(b,"timepicker");
if(c&&c._defaults.timeOnly&&b.input.val()!==b.lastVal)try{$.datepicker._updateDatepicker(b)}catch(d){$.timepicker.log(d)}return $.datepicker._base_doKeyUp(a)},$.datepicker._base_gotoToday=$.datepicker._gotoToday,$.datepicker._gotoToday=function(a){var b=this._getInst($(a)[0]);this._base_gotoToday(a);var c=this._get(b,"timepicker");if(c){var d=$.timepicker.timezoneOffsetNumber(c.timezone),e=new Date;e.setMinutes(e.getMinutes()+e.getTimezoneOffset()+parseInt(d,10)),this._setTime(b,e),this._setDate(b,e),c._onSelectHandler()}},$.datepicker._disableTimepickerDatepicker=function(a){var b=this._getInst(a);if(b){var c=this._get(b,"timepicker");$(a).datepicker("getDate"),c&&(b.settings.showTimepicker=!1,c._defaults.showTimepicker=!1,c._updateDateTime(b))}},$.datepicker._enableTimepickerDatepicker=function(a){var b=this._getInst(a);if(b){var c=this._get(b,"timepicker");$(a).datepicker("getDate"),c&&(b.settings.showTimepicker=!0,c._defaults.showTimepicker=!0,c._addTimePicker(b),c._updateDateTime(b))}},$.datepicker._setTime=function(a,b){var c=this._get(a,"timepicker");if(c){var d=c._defaults;c.hour=b?b.getHours():d.hour,c.minute=b?b.getMinutes():d.minute,c.second=b?b.getSeconds():d.second,c.millisec=b?b.getMilliseconds():d.millisec,c.microsec=b?b.getMicroseconds():d.microsec,c._limitMinMaxDateTime(a,!0),c._onTimeChange(),c._updateDateTime(a)}},$.datepicker._setTimeDatepicker=function(a,b,c){var d=this._getInst(a);if(d){var e=this._get(d,"timepicker");if(e){this._setDateFromField(d);var f;b&&("string"==typeof b?(e._parseTime(b,c),f=new Date,f.setHours(e.hour,e.minute,e.second,e.millisec),f.setMicroseconds(e.microsec)):(f=new Date(b.getTime()),f.setMicroseconds(b.getMicroseconds())),"Invalid Date"===f.toString()&&(f=void 0),this._setTime(d,f))}}},$.datepicker._base_setDateDatepicker=$.datepicker._setDateDatepicker,$.datepicker._setDateDatepicker=function(a,b){var c=this._getInst(a),d=b;if(c){"string"==typeof b&&(d=new Date(b),d.getTime()||(this._base_setDateDatepicker.apply(this,arguments),d=$(a).datepicker("getDate")));var e,f=this._get(c,"timepicker");d instanceof Date?(e=new Date(d.getTime()),e.setMicroseconds(d.getMicroseconds())):e=d,f&&e&&(f.support.timezone||null!==f._defaults.timezone||(f.timezone=-1*e.getTimezoneOffset()),d=$.timepicker.timezoneAdjust(d,$.timepicker.timezoneOffsetString(-d.getTimezoneOffset()),f.timezone),e=$.timepicker.timezoneAdjust(e,$.timepicker.timezoneOffsetString(-e.getTimezoneOffset()),f.timezone)),this._updateDatepicker(c),this._base_setDateDatepicker.apply(this,arguments),this._setTimeDatepicker(a,e,!0)}},$.datepicker._base_getDateDatepicker=$.datepicker._getDateDatepicker,$.datepicker._getDateDatepicker=function(a,b){var c=this._getInst(a);if(c){var d=this._get(c,"timepicker");if(d){void 0===c.lastVal&&this._setDateFromField(c,b);var e=this._getDate(c),f=null;return f=d.$altInput&&d._defaults.altFieldTimeOnly?d.$input.val()+" "+d.$altInput.val():"INPUT"!==d.$input.get(0).tagName&&d.$altInput?d.$altInput.val():d.$input.val(),e&&d._parseTime(f,!c.settings.timeOnly)&&(e.setHours(d.hour,d.minute,d.second,d.millisec),e.setMicroseconds(d.microsec),null!=d.timezone&&(d.support.timezone||null!==d._defaults.timezone||(d.timezone=-1*e.getTimezoneOffset()),e=$.timepicker.timezoneAdjust(e,d.timezone,$.timepicker.timezoneOffsetString(-e.getTimezoneOffset())))),e}return this._base_getDateDatepicker(a,b)}},$.datepicker._base_parseDate=$.datepicker.parseDate,$.datepicker.parseDate=function(a,b,c){var d;try{d=this._base_parseDate(a,b,c)}catch(e){if(!(e.indexOf(":")>=0))throw e;d=this._base_parseDate(a,b.substring(0,b.length-(e.length-e.indexOf(":")-2)),c),$.timepicker.log("Error parsing the date string: "+e+"\ndate string = "+b+"\ndate format = "+a)}return d},$.datepicker._base_formatDate=$.datepicker._formatDate,$.datepicker._formatDate=function(a,b,c,d){var e=this._get(a,"timepicker");return e?(e._updateDateTime(a),e.$input.val()):this._base_formatDate(a)},$.datepicker._base_optionDatepicker=$.datepicker._optionDatepicker,$.datepicker._optionDatepicker=function(a,b,c){var d,e=this._getInst(a);if(!e)return null;var f=this._get(e,"timepicker");if(f){var g,h,i,j,k=null,l=null,m=null,n=f._defaults.evnts,o={};if("string"==typeof b){if("minDate"===b||"minDateTime"===b)k=c;else if("maxDate"===b||"maxDateTime"===b)l=c;else if("onSelect"===b)m=c;else if(n.hasOwnProperty(b)){if("undefined"==typeof c)return n[b];o[b]=c,d={}}}else if("object"==typeof b){b.minDate?k=b.minDate:b.minDateTime?k=b.minDateTime:b.maxDate?l=b.maxDate:b.maxDateTime&&(l=b.maxDateTime);for(g in n)n.hasOwnProperty(g)&&b[g]&&(o[g]=b[g])}for(g in o)o.hasOwnProperty(g)&&(n[g]=o[g],d||(d=$.extend({},b)),delete d[g]);if(d&&isEmptyObject(d))return;if(k?(k=0===k?new Date:new Date(k),f._defaults.minDate=k,f._defaults.minDateTime=k):l?(l=0===l?new Date:new Date(l),f._defaults.maxDate=l,f._defaults.maxDateTime=l):m&&(f._defaults.onSelect=m),k||l)return j=$(a),i=j.datetimepicker("getDate"),h=this._base_optionDatepicker.call($.datepicker,a,d||b,c),j.datetimepicker("setDate",i),h}return void 0===c?this._base_optionDatepicker.call($.datepicker,a,b):this._base_optionDatepicker.call($.datepicker,a,d||b,c)};var isEmptyObject=function(a){var b;for(b in a)if(a.hasOwnProperty(b))return!1;return!0},extendRemove=function(a,b){$.extend(a,b);for(var c in b)(null===b[c]||void 0===b[c])&&(a[c]=b[c]);return a},detectSupport=function(a){var b=a.replace(/'.*?'/g,"").toLowerCase(),c=function(a,b){return-1!==a.indexOf(b)?!0:!1};return{hour:c(b,"h"),minute:c(b,"m"),second:c(b,"s"),millisec:c(b,"l"),microsec:c(b,"c"),timezone:c(b,"z"),ampm:c(b,"t")&&c(a,"h"),iso8601:c(a,"Z")}},convert24to12=function(a){return a%=12,0===a&&(a=12),String(a)},computeEffectiveSetting=function(a,b){return a&&a[b]?a[b]:$.timepicker._defaults[b]},splitDateTime=function(a,b){var c=computeEffectiveSetting(b,"separator"),d=computeEffectiveSetting(b,"timeFormat"),e=d.split(c),f=e.length,g=a.split(c),h=g.length;return h>1?{dateString:g.splice(0,h-f).join(c),timeString:g.splice(0,f).join(c)}:{dateString:a,timeString:""}},parseDateTimeInternal=function(a,b,c,d,e){var f,g,h;if(g=splitDateTime(c,e),f=$.datepicker._base_parseDate(a,g.dateString,d),""===g.timeString)return{date:f};if(h=$.datepicker.parseTime(b,g.timeString,e),!h)throw"Wrong time format";return{date:f,timeObj:h}},selectLocalTimezone=function(a,b){if(a&&a.timezone_select){var c=b||new Date;a.timezone_select.val(-c.getTimezoneOffset())}};$.timepicker=new Timepicker,$.timepicker.timezoneOffsetString=function(a,b){if(isNaN(a)||a>840||-720>a)return a;var c=a,d=c%60,e=(c-d)/60,f=b?":":"",g=(c>=0?"+":"-")+("0"+Math.abs(e)).slice(-2)+f+("0"+Math.abs(d)).slice(-2);return"+00:00"===g?"Z":g},$.timepicker.timezoneOffsetNumber=function(a){var b=a.toString().replace(":","");return"Z"===b.toUpperCase()?0:/^(\-|\+)\d{4}$/.test(b)?("-"===b.substr(0,1)?-1:1)*(60*parseInt(b.substr(1,2),10)+parseInt(b.substr(3,2),10)):parseInt(a,10)},$.timepicker.timezoneAdjust=function(a,b,c){var d=$.timepicker.timezoneOffsetNumber(b),e=$.timepicker.timezoneOffsetNumber(c);return isNaN(e)||a.setMinutes(a.getMinutes()+-d- -e),a},$.timepicker.timeRange=function(a,b,c){return $.timepicker.handleRange("timepicker",a,b,c)},$.timepicker.datetimeRange=function(a,b,c){$.timepicker.handleRange("datetimepicker",a,b,c)},$.timepicker.dateRange=function(a,b,c){$.timepicker.handleRange("datepicker",a,b,c)},$.timepicker.handleRange=function(a,b,c,d){function e(e,f){var g=b[a]("getDate"),h=c[a]("getDate"),i=e[a]("getDate");if(null!==g){var j=new Date(g.getTime()),k=new Date(g.getTime());j.setMilliseconds(j.getMilliseconds()+d.minInterval),k.setMilliseconds(k.getMilliseconds()+d.maxInterval),d.minInterval>0&&j>h?c[a]("setDate",j):d.maxInterval>0&&h>k?c[a]("setDate",k):g>h&&f[a]("setDate",i)}}function f(b,c,e){if(b.val()){var f=b[a].call(b,"getDate");null!==f&&d.minInterval>0&&("minDate"===e&&f.setMilliseconds(f.getMilliseconds()+d.minInterval),"maxDate"===e&&f.setMilliseconds(f.getMilliseconds()-d.minInterval)),f.getTime&&c[a].call(c,"option",e,f)}}d=$.extend({},{minInterval:0,maxInterval:0,start:{},end:{}},d);var g=!1;return"timepicker"===a&&(g=!0,a="datetimepicker"),$.fn[a].call(b,$.extend({timeOnly:g,onClose:function(a,b){e($(this),c)},onSelect:function(a){f($(this),c,"minDate")}},d,d.start)),$.fn[a].call(c,$.extend({timeOnly:g,onClose:function(a,c){e($(this),b)},onSelect:function(a){f($(this),b,"maxDate")}},d,d.end)),e(b,c),f(b,c,"minDate"),f(c,b,"maxDate"),$([b.get(0),c.get(0)])},$.timepicker.log=function(){window.console&&window.console.log&&window.console.log.apply&&window.console.log.apply(window.console,Array.prototype.slice.call(arguments))},$.timepicker._util={_extendRemove:extendRemove,_isEmptyObject:isEmptyObject,_convert24to12:convert24to12,_detectSupport:detectSupport,_selectLocalTimezone:selectLocalTimezone,_computeEffectiveSetting:computeEffectiveSetting,_splitDateTime:splitDateTime,_parseDateTimeInternal:parseDateTimeInternal},Date.prototype.getMicroseconds||(Date.prototype.microseconds=0,Date.prototype.getMicroseconds=function(){return this.microseconds},Date.prototype.setMicroseconds=function(a){return this.setMilliseconds(this.getMilliseconds()+Math.floor(a/1e3)),this.microseconds=a%1e3,this}),$.timepicker.version="1.6.3"}});

/*! jQuery Timepicker Addon - v1.6.3 - 2016-04-20
* http://trentrichardson.com/examples/timepicker
* Copyright (c) 2016 Trent Richardson; Licensed MIT */
!function(a){a.timepicker.regional.af={timeOnlyTitle:"Kies Tyd",timeText:"Tyd ",hourText:"Ure ",minuteText:"Minute",secondText:"Sekondes",millisecText:"Millisekondes",microsecText:"Mikrosekondes",timezoneText:"Tydsone",currentText:"Huidige Tyd",closeText:"Klaar",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.am={timeOnlyTitle:"Ընտրեք ժամանակը",timeText:"Ժամանակը",hourText:"Ժամ",minuteText:"Րոպե",secondText:"Վարկյան",millisecText:"Միլիվարկյան",microsecText:"Միկրովարկյան",timezoneText:"Ժամային գոտին",currentText:"Այժմ",closeText:"Փակել",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.bg={timeOnlyTitle:"Изберете време",timeText:"Време",hourText:"Час",minuteText:"Минути",secondText:"Секунди",millisecText:"Милисекунди",microsecText:"Микросекунди",timezoneText:"Часови пояс",currentText:"Сега",closeText:"Затвори",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.ca={timeOnlyTitle:"Escollir una hora",timeText:"Hora",hourText:"Hores",minuteText:"Minuts",secondText:"Segons",millisecText:"Milisegons",microsecText:"Microsegons",timezoneText:"Fus horari",currentText:"Ara",closeText:"Tancar",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.cs={timeOnlyTitle:"Vyberte čas",timeText:"Čas",hourText:"Hodiny",minuteText:"Minuty",secondText:"Vteřiny",millisecText:"Milisekundy",microsecText:"Mikrosekundy",timezoneText:"Časové pásmo",currentText:"Nyní",closeText:"Zavřít",timeFormat:"HH:mm",timeSuffix:"",amNames:["dop.","AM","A"],pmNames:["odp.","PM","P"],isRTL:!1},a.timepicker.regional.da={timeOnlyTitle:"Vælg tid",timeText:"Tid",hourText:"Time",minuteText:"Minut",secondText:"Sekund",millisecText:"Millisekund",microsecText:"Mikrosekund",timezoneText:"Tidszone",currentText:"Nu",closeText:"Luk",timeFormat:"HH:mm",timeSuffix:"",amNames:["am","AM","A"],pmNames:["pm","PM","P"],isRTL:!1},a.timepicker.regional.de={timeOnlyTitle:"Zeit wählen",timeText:"Zeit",hourText:"Stunde",minuteText:"Minute",secondText:"Sekunde",millisecText:"Millisekunde",microsecText:"Mikrosekunde",timezoneText:"Zeitzone",currentText:"Jetzt",closeText:"Fertig",timeFormat:"HH:mm",timeSuffix:"",amNames:["vorm.","AM","A"],pmNames:["nachm.","PM","P"],isRTL:!1},a.timepicker.regional.el={timeOnlyTitle:"Επιλογή ώρας",timeText:"Ώρα",hourText:"Ώρες",minuteText:"Λεπτά",secondText:"Δευτερόλεπτα",millisecText:"Χιλιοστοδευτερόλεπτα",microsecText:"Μικροδευτερόλεπτα",timezoneText:"Ζώνη ώρας",currentText:"Τώρα",closeText:"Κλείσιμο",timeFormat:"HH:mm",timeSuffix:"",amNames:["π.μ.","AM","A"],pmNames:["μ.μ.","PM","P"],isRTL:!1},a.timepicker.regional.es={timeOnlyTitle:"Elegir una hora",timeText:"Hora",hourText:"Horas",minuteText:"Minutos",secondText:"Segundos",millisecText:"Milisegundos",microsecText:"Microsegundos",timezoneText:"Uso horario",currentText:"Hoy",closeText:"Cerrar",timeFormat:"HH:mm",timeSuffix:"",amNames:["a.m.","AM","A"],pmNames:["p.m.","PM","P"],isRTL:!1},a.timepicker.regional.et={timeOnlyTitle:"Vali aeg",timeText:"Aeg",hourText:"Tund",minuteText:"Minut",secondText:"Sekund",millisecText:"Millisekundis",microsecText:"Mikrosekundis",timezoneText:"Ajavöönd",currentText:"Praegu",closeText:"Valmis",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.eu={timeOnlyTitle:"Aukeratu ordua",timeText:"Ordua",hourText:"Orduak",minuteText:"Minutuak",secondText:"Segundoak",millisecText:"Milisegundoak",microsecText:"Mikrosegundoak",timezoneText:"Ordu-eremua",currentText:"Orain",closeText:"Itxi",timeFormat:"HH:mm",timeSuffix:"",amNames:["a.m.","AM","A"],pmNames:["p.m.","PM","P"],isRTL:!1},a.timepicker.regional.fa={timeOnlyTitle:"انتخاب زمان",timeText:"زمان",hourText:"ساعت",minuteText:"دقیقه",secondText:"ثانیه",millisecText:"میلی ثانیه",microsecText:"میکرو ثانیه",timezoneText:"منطقه زمانی",currentText:"الان",closeText:"انتخاب",timeFormat:"HH:mm",timeSuffix:"",amNames:["قبل ظهر","AM","A"],pmNames:["بعد ظهر","PM","P"],isRTL:!0},a.timepicker.regional.fi={timeOnlyTitle:"Valitse aika",timeText:"Aika",hourText:"Tunti",minuteText:"Minuutti",secondText:"Sekunti",millisecText:"Millisekunnin",microsecText:"Mikrosekuntia",timezoneText:"Aikavyöhyke",currentText:"Nyt",closeText:"Sulje",timeFormat:"HH:mm",timeSuffix:"",amNames:["ap.","AM","A"],pmNames:["ip.","PM","P"],isRTL:!1},a.timepicker.regional.fr={timeOnlyTitle:"Choisir une heure",timeText:"Heure",hourText:"Heures",minuteText:"Minutes",secondText:"Secondes",millisecText:"Millisecondes",microsecText:"Microsecondes",timezoneText:"Fuseau horaire",currentText:"Maintenant",closeText:"Terminé",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.gl={timeOnlyTitle:"Elixir unha hora",timeText:"Hora",hourText:"Horas",minuteText:"Minutos",secondText:"Segundos",millisecText:"Milisegundos",microsecText:"Microssegundos",timezoneText:"Fuso horario",currentText:"Agora",closeText:"Pechar",timeFormat:"HH:mm",timeSuffix:"",amNames:["a.m.","AM","A"],pmNames:["p.m.","PM","P"],isRTL:!1},a.timepicker.regional.he={timeOnlyTitle:"בחירת זמן",timeText:"שעה",hourText:"שעות",minuteText:"דקות",secondText:"שניות",millisecText:"אלפית השנייה",microsecText:"מיקרו",timezoneText:"אזור זמן",currentText:"עכשיו",closeText:"סגור",timeFormat:"HH:mm",timeSuffix:"",amNames:['לפנה"צ',"AM","A"],pmNames:['אחה"צ',"PM","P"],isRTL:!0},a.timepicker.regional.hr={timeOnlyTitle:"Odaberi vrijeme",timeText:"Vrijeme",hourText:"Sati",minuteText:"Minute",secondText:"Sekunde",millisecText:"Milisekunde",microsecText:"Mikrosekunde",timezoneText:"Vremenska zona",currentText:"Sada",closeText:"Gotovo",timeFormat:"HH:mm",timeSuffix:"",amNames:["a.m.","AM","A"],pmNames:["p.m.","PM","P"],isRTL:!1},a.timepicker.regional.hu={timeOnlyTitle:"Válasszon időpontot",timeText:"Idő",hourText:"Óra",minuteText:"Perc",secondText:"Másodperc",millisecText:"Milliszekundumos",microsecText:"Ezredmásodperc",timezoneText:"Időzóna",currentText:"Most",closeText:"Kész",timeFormat:"HH:mm",timeSuffix:"",amNames:["de.","AM","A"],pmNames:["du.","PM","P"],isRTL:!1},a.timepicker.regional.id={timeOnlyTitle:"Pilih Waktu",timeText:"Waktu",hourText:"Pukul",minuteText:"Menit",secondText:"Detik",millisecText:"Milidetik",microsecText:"Mikrodetik",timezoneText:"Zona Waktu",currentText:"Sekarang",closeText:"OK",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.it={timeOnlyTitle:"Scegli orario",timeText:"Orario",hourText:"Ora",minuteText:"Minuti",secondText:"Secondi",millisecText:"Millisecondi",microsecText:"Microsecondi",timezoneText:"Fuso orario",currentText:"Adesso",closeText:"Chiudi",timeFormat:"HH:mm",timeSuffix:"",amNames:["m.","AM","A"],pmNames:["p.","PM","P"],isRTL:!1},a.timepicker.regional.ja={timeOnlyTitle:"時間を選択",timeText:"時間",hourText:"時",minuteText:"分",secondText:"秒",millisecText:"ミリ秒",microsecText:"マイクロ秒",timezoneText:"タイムゾーン",currentText:"現時刻",closeText:"閉じる",timeFormat:"HH:mm",timeSuffix:"",amNames:["午前","AM","A"],pmNames:["午後","PM","P"],isRTL:!1},a.timepicker.regional.ko={timeOnlyTitle:"시간 선택",timeText:"시간",hourText:"시",minuteText:"분",secondText:"초",millisecText:"밀리초",microsecText:"마이크로",timezoneText:"표준 시간대",currentText:"현재 시각",closeText:"닫기",timeFormat:"tt h:mm",timeSuffix:"",amNames:["오전","AM","A"],pmNames:["오후","PM","P"],isRTL:!1},a.timepicker.regional.lt={timeOnlyTitle:"Pasirinkite laiką",timeText:"Laikas",hourText:"Valandos",minuteText:"Minutės",secondText:"Sekundės",millisecText:"Milisekundės",microsecText:"Mikrosekundės",timezoneText:"Laiko zona",currentText:"Dabar",closeText:"Uždaryti",timeFormat:"HH:mm",timeSuffix:"",amNames:["priešpiet","AM","A"],pmNames:["popiet","PM","P"],isRTL:!1},a.timepicker.regional.lv={timeOnlyTitle:"Ievadiet laiku",timeText:"Laiks",hourText:"Stundas",minuteText:"Minūtes",secondText:"Sekundes",millisecText:"Milisekundes",microsecText:"Mikrosekundes",timezoneText:"Laika josla",currentText:"Tagad",closeText:"Aizvērt",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","AM","A"],pmNames:["PM","PM","P"],isRTL:!1},a.timepicker.regional.mk={timeOnlyTitle:"Одберете време",timeText:"Време",hourText:"Час",minuteText:"Минути",secondText:"Секунди",millisecText:"Милисекунди",microsecText:"Микросекунди",timezoneText:"Временска зона",currentText:"Сега",closeText:"Затвори",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.nl={timeOnlyTitle:"Tijdstip",timeText:"Tijd",hourText:"Uur",minuteText:"Minuut",secondText:"Seconde",millisecText:"Milliseconde",microsecText:"Microseconde",timezoneText:"Tijdzone",currentText:"Vandaag",closeText:"Sluiten",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.no={timeOnlyTitle:"Velg tid",timeText:"Tid",hourText:"Time",minuteText:"Minutt",secondText:"Sekund",millisecText:"Millisekund",microsecText:"mikrosekund",timezoneText:"Tidssone",currentText:"Nå",closeText:"Lukk",timeFormat:"HH:mm",timeSuffix:"",amNames:["am","AM","A"],pmNames:["pm","PM","P"],isRTL:!1},a.timepicker.regional.pl={timeOnlyTitle:"Wybierz godzinę",timeText:"Czas",hourText:"Godzina",minuteText:"Minuta",secondText:"Sekunda",millisecText:"Milisekunda",microsecText:"Mikrosekunda",timezoneText:"Strefa czasowa",currentText:"Teraz",closeText:"Gotowe",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional["pt-BR"]={timeOnlyTitle:"Escolha o horário",timeText:"Horário",hourText:"Hora",minuteText:"Minutos",secondText:"Segundos",millisecText:"Milissegundos",microsecText:"Microssegundos",timezoneText:"Fuso horário",currentText:"Agora",closeText:"Fechar",timeFormat:"HH:mm",timeSuffix:"",amNames:["a.m.","AM","A"],pmNames:["p.m.","PM","P"],isRTL:!1},a.timepicker.regional.pt={timeOnlyTitle:"Escolha uma hora",timeText:"Hora",hourText:"Horas",minuteText:"Minutos",secondText:"Segundos",millisecText:"Milissegundos",microsecText:"Microssegundos",timezoneText:"Fuso horário",currentText:"Agora",closeText:"Fechar",timeFormat:"HH:mm",timeSuffix:"",amNames:["a.m.","AM","A"],pmNames:["p.m.","PM","P"],isRTL:!1},a.timepicker.regional.ro={timeOnlyTitle:"Alegeţi o oră",timeText:"Timp",hourText:"Ore",minuteText:"Minute",secondText:"Secunde",millisecText:"Milisecunde",microsecText:"Microsecunde",timezoneText:"Fus orar",currentText:"Acum",closeText:"Închide",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.ru={timeOnlyTitle:"Выберите время",timeText:"Время",hourText:"Часы",minuteText:"Минуты",secondText:"Секунды",millisecText:"Миллисекунды",microsecText:"Микросекунды",timezoneText:"Часовой пояс",currentText:"Сейчас",closeText:"Закрыть",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.sk={timeOnlyTitle:"Zvoľte čas",timeText:"Čas",hourText:"Hodiny",minuteText:"Minúty",secondText:"Sekundy",millisecText:"Milisekundy",microsecText:"Mikrosekundy",timezoneText:"Časové pásmo",currentText:"Teraz",closeText:"Zavrieť",timeFormat:"H:m",timeSuffix:"",amNames:["dop.","AM","A"],pmNames:["pop.","PM","P"],isRTL:!1},a.timepicker.regional.sl={timeOnlyTitle:"Izberite čas",timeText:"Čas",hourText:"Ura",minuteText:"Minute",secondText:"Sekunde",millisecText:"Milisekunde",microsecText:"Mikrosekunde",timezoneText:"Časovni pas",currentText:"Sedaj",closeText:"Zapri",timeFormat:"HH:mm",timeSuffix:"",amNames:["dop.","AM","A"],pmNames:["pop.","PM","P"],isRTL:!1},a.timepicker.regional.sq={timeOnlyTitle:"Zgjidh orarin",timeText:"Orari",hourText:"Ora",minuteText:"Minuta",secondText:"Sekonda",millisecText:"Minisekonda",microsecText:"Mikrosekonda",timezoneText:"Zona kohore",currentText:"Tani",closeText:"Mbyll",timeFormat:"HH:mm",timeSuffix:"",amNames:["m.","AM","A"],pmNames:["p.","PM","P"],isRTL:!1},a.timepicker.regional["sr-RS"]={timeOnlyTitle:"Одаберите време",timeText:"Време",hourText:"Сати",minuteText:"Минути",secondText:"Секунде",millisecText:"Милисекунде",microsecText:"Микросекунде",timezoneText:"Временска зона",currentText:"Сада",closeText:"Затвори",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional["sr-YU"]={timeOnlyTitle:"Odaberite vreme",timeText:"Vreme",hourText:"Sati",minuteText:"Minuti",secondText:"Sekunde",millisecText:"Milisekunde",microsecText:"Mikrosekunde",timezoneText:"Vremenska zona",currentText:"Sada",closeText:"Zatvori",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.sv={timeOnlyTitle:"Välj en tid",timeText:"Tid",hourText:"Timme",minuteText:"Minut",secondText:"Sekund",millisecText:"Millisekund",microsecText:"Mikrosekund",timezoneText:"Tidszon",currentText:"Nu",closeText:"Stäng",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.th={timeOnlyTitle:"เลือกเวลา",timeText:"เวลา ",hourText:"ชั่วโมง ",minuteText:"นาที",secondText:"วินาที",millisecText:"มิลลิวินาที",microsecText:"ไมโคริวินาที",timezoneText:"เขตเวลา",currentText:"เวลาปัจจุบัน",closeText:"ปิด",timeFormat:"hh:mm tt",timeSuffix:""},a.timepicker.regional.tr={timeOnlyTitle:"Zaman Seçiniz",timeText:"Zaman",hourText:"Saat",minuteText:"Dakika",secondText:"Saniye",millisecText:"Milisaniye",microsecText:"Mikrosaniye",timezoneText:"Zaman Dilimi",currentText:"Şu an",closeText:"Tamam",timeFormat:"HH:mm",timeSuffix:"",amNames:["ÖÖ","Ö"],pmNames:["ÖS","S"],isRTL:!1},a.timepicker.regional.uk={timeOnlyTitle:"Виберіть час",timeText:"Час",hourText:"Години",minuteText:"Хвилини",secondText:"Секунди",millisecText:"Мілісекунди",microsecText:"Мікросекунди",timezoneText:"Часовий пояс",currentText:"Зараз",closeText:"Закрити",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional.vi={timeOnlyTitle:"Chọn giờ",timeText:"Thời gian",hourText:"Giờ",minuteText:"Phút",secondText:"Giây",millisecText:"Mili giây",microsecText:"Micrô giây",timezoneText:"Múi giờ",currentText:"Hiện thời",closeText:"Đóng",timeFormat:"HH:mm",timeSuffix:"",amNames:["SA","S"],pmNames:["CH","C"],isRTL:!1},a.timepicker.regional["zh-CN"]={timeOnlyTitle:"选择时间",timeText:"时间",hourText:"小时",minuteText:"分钟",secondText:"秒钟",millisecText:"毫秒",microsecText:"微秒",timezoneText:"时区",currentText:"现在时间",closeText:"关闭",timeFormat:"HH:mm",timeSuffix:"",amNames:["AM","A"],pmNames:["PM","P"],isRTL:!1},a.timepicker.regional["zh-TW"]={timeOnlyTitle:"選擇時分秒",timeText:"時間",hourText:"時",minuteText:"分",secondText:"秒",millisecText:"毫秒",microsecText:"微秒",timezoneText:"時區",currentText:"現在時間",closeText:"確定",timeFormat:"HH:mm",timeSuffix:"",amNames:["上午","AM","A"],pmNames:["下午","PM","P"],isRTL:!1}}(jQuery);
//! moment.js
//! version : 2.22.2
//! authors : Tim Wood, Iskren Chernev, Moment.js contributors
//! license : MIT
//! momentjs.com
!function(e,t){"object"==typeof exports&&"undefined"!=typeof module?module.exports=t():"function"==typeof define&&define.amd?define(t):e.moment=t()}(this,function(){"use strict";var e,i;function c(){return e.apply(null,arguments)}function o(e){return e instanceof Array||"[object Array]"===Object.prototype.toString.call(e)}function u(e){return null!=e&&"[object Object]"===Object.prototype.toString.call(e)}function l(e){return void 0===e}function d(e){return"number"==typeof e||"[object Number]"===Object.prototype.toString.call(e)}function h(e){return e instanceof Date||"[object Date]"===Object.prototype.toString.call(e)}function f(e,t){var n,s=[];for(n=0;n<e.length;++n)s.push(t(e[n],n));return s}function m(e,t){return Object.prototype.hasOwnProperty.call(e,t)}function _(e,t){for(var n in t)m(t,n)&&(e[n]=t[n]);return m(t,"toString")&&(e.toString=t.toString),m(t,"valueOf")&&(e.valueOf=t.valueOf),e}function y(e,t,n,s){return Ot(e,t,n,s,!0).utc()}function g(e){return null==e._pf&&(e._pf={empty:!1,unusedTokens:[],unusedInput:[],overflow:-2,charsLeftOver:0,nullInput:!1,invalidMonth:null,invalidFormat:!1,userInvalidated:!1,iso:!1,parsedDateParts:[],meridiem:null,rfc2822:!1,weekdayMismatch:!1}),e._pf}function p(e){if(null==e._isValid){var t=g(e),n=i.call(t.parsedDateParts,function(e){return null!=e}),s=!isNaN(e._d.getTime())&&t.overflow<0&&!t.empty&&!t.invalidMonth&&!t.invalidWeekday&&!t.weekdayMismatch&&!t.nullInput&&!t.invalidFormat&&!t.userInvalidated&&(!t.meridiem||t.meridiem&&n);if(e._strict&&(s=s&&0===t.charsLeftOver&&0===t.unusedTokens.length&&void 0===t.bigHour),null!=Object.isFrozen&&Object.isFrozen(e))return s;e._isValid=s}return e._isValid}function v(e){var t=y(NaN);return null!=e?_(g(t),e):g(t).userInvalidated=!0,t}i=Array.prototype.some?Array.prototype.some:function(e){for(var t=Object(this),n=t.length>>>0,s=0;s<n;s++)if(s in t&&e.call(this,t[s],s,t))return!0;return!1};var r=c.momentProperties=[];function w(e,t){var n,s,i;if(l(t._isAMomentObject)||(e._isAMomentObject=t._isAMomentObject),l(t._i)||(e._i=t._i),l(t._f)||(e._f=t._f),l(t._l)||(e._l=t._l),l(t._strict)||(e._strict=t._strict),l(t._tzm)||(e._tzm=t._tzm),l(t._isUTC)||(e._isUTC=t._isUTC),l(t._offset)||(e._offset=t._offset),l(t._pf)||(e._pf=g(t)),l(t._locale)||(e._locale=t._locale),0<r.length)for(n=0;n<r.length;n++)l(i=t[s=r[n]])||(e[s]=i);return e}var t=!1;function M(e){w(this,e),this._d=new Date(null!=e._d?e._d.getTime():NaN),this.isValid()||(this._d=new Date(NaN)),!1===t&&(t=!0,c.updateOffset(this),t=!1)}function S(e){return e instanceof M||null!=e&&null!=e._isAMomentObject}function D(e){return e<0?Math.ceil(e)||0:Math.floor(e)}function k(e){var t=+e,n=0;return 0!==t&&isFinite(t)&&(n=D(t)),n}function a(e,t,n){var s,i=Math.min(e.length,t.length),r=Math.abs(e.length-t.length),a=0;for(s=0;s<i;s++)(n&&e[s]!==t[s]||!n&&k(e[s])!==k(t[s]))&&a++;return a+r}function Y(e){!1===c.suppressDeprecationWarnings&&"undefined"!=typeof console&&console.warn&&console.warn("Deprecation warning: "+e)}function n(i,r){var a=!0;return _(function(){if(null!=c.deprecationHandler&&c.deprecationHandler(null,i),a){for(var e,t=[],n=0;n<arguments.length;n++){if(e="","object"==typeof arguments[n]){for(var s in e+="\n["+n+"] ",arguments[0])e+=s+": "+arguments[0][s]+", ";e=e.slice(0,-2)}else e=arguments[n];t.push(e)}Y(i+"\nArguments: "+Array.prototype.slice.call(t).join("")+"\n"+(new Error).stack),a=!1}return r.apply(this,arguments)},r)}var s,O={};function T(e,t){null!=c.deprecationHandler&&c.deprecationHandler(e,t),O[e]||(Y(t),O[e]=!0)}function x(e){return e instanceof Function||"[object Function]"===Object.prototype.toString.call(e)}function b(e,t){var n,s=_({},e);for(n in t)m(t,n)&&(u(e[n])&&u(t[n])?(s[n]={},_(s[n],e[n]),_(s[n],t[n])):null!=t[n]?s[n]=t[n]:delete s[n]);for(n in e)m(e,n)&&!m(t,n)&&u(e[n])&&(s[n]=_({},s[n]));return s}function P(e){null!=e&&this.set(e)}c.suppressDeprecationWarnings=!1,c.deprecationHandler=null,s=Object.keys?Object.keys:function(e){var t,n=[];for(t in e)m(e,t)&&n.push(t);return n};var W={};function H(e,t){var n=e.toLowerCase();W[n]=W[n+"s"]=W[t]=e}function R(e){return"string"==typeof e?W[e]||W[e.toLowerCase()]:void 0}function C(e){var t,n,s={};for(n in e)m(e,n)&&(t=R(n))&&(s[t]=e[n]);return s}var F={};function L(e,t){F[e]=t}function U(e,t,n){var s=""+Math.abs(e),i=t-s.length;return(0<=e?n?"+":"":"-")+Math.pow(10,Math.max(0,i)).toString().substr(1)+s}var N=/(\[[^\[]*\])|(\\)?([Hh]mm(ss)?|Mo|MM?M?M?|Do|DDDo|DD?D?D?|ddd?d?|do?|w[o|w]?|W[o|W]?|Qo?|YYYYYY|YYYYY|YYYY|YY|gg(ggg?)?|GG(GGG?)?|e|E|a|A|hh?|HH?|kk?|mm?|ss?|S{1,9}|x|X|zz?|ZZ?|.)/g,G=/(\[[^\[]*\])|(\\)?(LTS|LT|LL?L?L?|l{1,4})/g,V={},E={};function I(e,t,n,s){var i=s;"string"==typeof s&&(i=function(){return this[s]()}),e&&(E[e]=i),t&&(E[t[0]]=function(){return U(i.apply(this,arguments),t[1],t[2])}),n&&(E[n]=function(){return this.localeData().ordinal(i.apply(this,arguments),e)})}function A(e,t){return e.isValid()?(t=j(t,e.localeData()),V[t]=V[t]||function(s){var e,i,t,r=s.match(N);for(e=0,i=r.length;e<i;e++)E[r[e]]?r[e]=E[r[e]]:r[e]=(t=r[e]).match(/\[[\s\S]/)?t.replace(/^\[|\]$/g,""):t.replace(/\\/g,"");return function(e){var t,n="";for(t=0;t<i;t++)n+=x(r[t])?r[t].call(e,s):r[t];return n}}(t),V[t](e)):e.localeData().invalidDate()}function j(e,t){var n=5;function s(e){return t.longDateFormat(e)||e}for(G.lastIndex=0;0<=n&&G.test(e);)e=e.replace(G,s),G.lastIndex=0,n-=1;return e}var Z=/\d/,z=/\d\d/,$=/\d{3}/,q=/\d{4}/,J=/[+-]?\d{6}/,B=/\d\d?/,Q=/\d\d\d\d?/,X=/\d\d\d\d\d\d?/,K=/\d{1,3}/,ee=/\d{1,4}/,te=/[+-]?\d{1,6}/,ne=/\d+/,se=/[+-]?\d+/,ie=/Z|[+-]\d\d:?\d\d/gi,re=/Z|[+-]\d\d(?::?\d\d)?/gi,ae=/[0-9]{0,256}['a-z\u00A0-\u05FF\u0700-\uD7FF\uF900-\uFDCF\uFDF0-\uFF07\uFF10-\uFFEF]{1,256}|[\u0600-\u06FF\/]{1,256}(\s*?[\u0600-\u06FF]{1,256}){1,2}/i,oe={};function ue(e,n,s){oe[e]=x(n)?n:function(e,t){return e&&s?s:n}}function le(e,t){return m(oe,e)?oe[e](t._strict,t._locale):new RegExp(de(e.replace("\\","").replace(/\\(\[)|\\(\])|\[([^\]\[]*)\]|\\(.)/g,function(e,t,n,s,i){return t||n||s||i})))}function de(e){return e.replace(/[-\/\\^$*+?.()|[\]{}]/g,"\\$&")}var he={};function ce(e,n){var t,s=n;for("string"==typeof e&&(e=[e]),d(n)&&(s=function(e,t){t[n]=k(e)}),t=0;t<e.length;t++)he[e[t]]=s}function fe(e,i){ce(e,function(e,t,n,s){n._w=n._w||{},i(e,n._w,n,s)})}var me=0,_e=1,ye=2,ge=3,pe=4,ve=5,we=6,Me=7,Se=8;function De(e){return ke(e)?366:365}function ke(e){return e%4==0&&e%100!=0||e%400==0}I("Y",0,0,function(){var e=this.year();return e<=9999?""+e:"+"+e}),I(0,["YY",2],0,function(){return this.year()%100}),I(0,["YYYY",4],0,"year"),I(0,["YYYYY",5],0,"year"),I(0,["YYYYYY",6,!0],0,"year"),H("year","y"),L("year",1),ue("Y",se),ue("YY",B,z),ue("YYYY",ee,q),ue("YYYYY",te,J),ue("YYYYYY",te,J),ce(["YYYYY","YYYYYY"],me),ce("YYYY",function(e,t){t[me]=2===e.length?c.parseTwoDigitYear(e):k(e)}),ce("YY",function(e,t){t[me]=c.parseTwoDigitYear(e)}),ce("Y",function(e,t){t[me]=parseInt(e,10)}),c.parseTwoDigitYear=function(e){return k(e)+(68<k(e)?1900:2e3)};var Ye,Oe=Te("FullYear",!0);function Te(t,n){return function(e){return null!=e?(be(this,t,e),c.updateOffset(this,n),this):xe(this,t)}}function xe(e,t){return e.isValid()?e._d["get"+(e._isUTC?"UTC":"")+t]():NaN}function be(e,t,n){e.isValid()&&!isNaN(n)&&("FullYear"===t&&ke(e.year())&&1===e.month()&&29===e.date()?e._d["set"+(e._isUTC?"UTC":"")+t](n,e.month(),Pe(n,e.month())):e._d["set"+(e._isUTC?"UTC":"")+t](n))}function Pe(e,t){if(isNaN(e)||isNaN(t))return NaN;var n,s=(t%(n=12)+n)%n;return e+=(t-s)/12,1===s?ke(e)?29:28:31-s%7%2}Ye=Array.prototype.indexOf?Array.prototype.indexOf:function(e){var t;for(t=0;t<this.length;++t)if(this[t]===e)return t;return-1},I("M",["MM",2],"Mo",function(){return this.month()+1}),I("MMM",0,0,function(e){return this.localeData().monthsShort(this,e)}),I("MMMM",0,0,function(e){return this.localeData().months(this,e)}),H("month","M"),L("month",8),ue("M",B),ue("MM",B,z),ue("MMM",function(e,t){return t.monthsShortRegex(e)}),ue("MMMM",function(e,t){return t.monthsRegex(e)}),ce(["M","MM"],function(e,t){t[_e]=k(e)-1}),ce(["MMM","MMMM"],function(e,t,n,s){var i=n._locale.monthsParse(e,s,n._strict);null!=i?t[_e]=i:g(n).invalidMonth=e});var We=/D[oD]?(\[[^\[\]]*\]|\s)+MMMM?/,He="January_February_March_April_May_June_July_August_September_October_November_December".split("_");var Re="Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec".split("_");function Ce(e,t){var n;if(!e.isValid())return e;if("string"==typeof t)if(/^\d+$/.test(t))t=k(t);else if(!d(t=e.localeData().monthsParse(t)))return e;return n=Math.min(e.date(),Pe(e.year(),t)),e._d["set"+(e._isUTC?"UTC":"")+"Month"](t,n),e}function Fe(e){return null!=e?(Ce(this,e),c.updateOffset(this,!0),this):xe(this,"Month")}var Le=ae;var Ue=ae;function Ne(){function e(e,t){return t.length-e.length}var t,n,s=[],i=[],r=[];for(t=0;t<12;t++)n=y([2e3,t]),s.push(this.monthsShort(n,"")),i.push(this.months(n,"")),r.push(this.months(n,"")),r.push(this.monthsShort(n,""));for(s.sort(e),i.sort(e),r.sort(e),t=0;t<12;t++)s[t]=de(s[t]),i[t]=de(i[t]);for(t=0;t<24;t++)r[t]=de(r[t]);this._monthsRegex=new RegExp("^("+r.join("|")+")","i"),this._monthsShortRegex=this._monthsRegex,this._monthsStrictRegex=new RegExp("^("+i.join("|")+")","i"),this._monthsShortStrictRegex=new RegExp("^("+s.join("|")+")","i")}function Ge(e){var t=new Date(Date.UTC.apply(null,arguments));return e<100&&0<=e&&isFinite(t.getUTCFullYear())&&t.setUTCFullYear(e),t}function Ve(e,t,n){var s=7+t-n;return-((7+Ge(e,0,s).getUTCDay()-t)%7)+s-1}function Ee(e,t,n,s,i){var r,a,o=1+7*(t-1)+(7+n-s)%7+Ve(e,s,i);return o<=0?a=De(r=e-1)+o:o>De(e)?(r=e+1,a=o-De(e)):(r=e,a=o),{year:r,dayOfYear:a}}function Ie(e,t,n){var s,i,r=Ve(e.year(),t,n),a=Math.floor((e.dayOfYear()-r-1)/7)+1;return a<1?s=a+Ae(i=e.year()-1,t,n):a>Ae(e.year(),t,n)?(s=a-Ae(e.year(),t,n),i=e.year()+1):(i=e.year(),s=a),{week:s,year:i}}function Ae(e,t,n){var s=Ve(e,t,n),i=Ve(e+1,t,n);return(De(e)-s+i)/7}I("w",["ww",2],"wo","week"),I("W",["WW",2],"Wo","isoWeek"),H("week","w"),H("isoWeek","W"),L("week",5),L("isoWeek",5),ue("w",B),ue("ww",B,z),ue("W",B),ue("WW",B,z),fe(["w","ww","W","WW"],function(e,t,n,s){t[s.substr(0,1)]=k(e)});I("d",0,"do","day"),I("dd",0,0,function(e){return this.localeData().weekdaysMin(this,e)}),I("ddd",0,0,function(e){return this.localeData().weekdaysShort(this,e)}),I("dddd",0,0,function(e){return this.localeData().weekdays(this,e)}),I("e",0,0,"weekday"),I("E",0,0,"isoWeekday"),H("day","d"),H("weekday","e"),H("isoWeekday","E"),L("day",11),L("weekday",11),L("isoWeekday",11),ue("d",B),ue("e",B),ue("E",B),ue("dd",function(e,t){return t.weekdaysMinRegex(e)}),ue("ddd",function(e,t){return t.weekdaysShortRegex(e)}),ue("dddd",function(e,t){return t.weekdaysRegex(e)}),fe(["dd","ddd","dddd"],function(e,t,n,s){var i=n._locale.weekdaysParse(e,s,n._strict);null!=i?t.d=i:g(n).invalidWeekday=e}),fe(["d","e","E"],function(e,t,n,s){t[s]=k(e)});var je="Sunday_Monday_Tuesday_Wednesday_Thursday_Friday_Saturday".split("_");var Ze="Sun_Mon_Tue_Wed_Thu_Fri_Sat".split("_");var ze="Su_Mo_Tu_We_Th_Fr_Sa".split("_");var $e=ae;var qe=ae;var Je=ae;function Be(){function e(e,t){return t.length-e.length}var t,n,s,i,r,a=[],o=[],u=[],l=[];for(t=0;t<7;t++)n=y([2e3,1]).day(t),s=this.weekdaysMin(n,""),i=this.weekdaysShort(n,""),r=this.weekdays(n,""),a.push(s),o.push(i),u.push(r),l.push(s),l.push(i),l.push(r);for(a.sort(e),o.sort(e),u.sort(e),l.sort(e),t=0;t<7;t++)o[t]=de(o[t]),u[t]=de(u[t]),l[t]=de(l[t]);this._weekdaysRegex=new RegExp("^("+l.join("|")+")","i"),this._weekdaysShortRegex=this._weekdaysRegex,this._weekdaysMinRegex=this._weekdaysRegex,this._weekdaysStrictRegex=new RegExp("^("+u.join("|")+")","i"),this._weekdaysShortStrictRegex=new RegExp("^("+o.join("|")+")","i"),this._weekdaysMinStrictRegex=new RegExp("^("+a.join("|")+")","i")}function Qe(){return this.hours()%12||12}function Xe(e,t){I(e,0,0,function(){return this.localeData().meridiem(this.hours(),this.minutes(),t)})}function Ke(e,t){return t._meridiemParse}I("H",["HH",2],0,"hour"),I("h",["hh",2],0,Qe),I("k",["kk",2],0,function(){return this.hours()||24}),I("hmm",0,0,function(){return""+Qe.apply(this)+U(this.minutes(),2)}),I("hmmss",0,0,function(){return""+Qe.apply(this)+U(this.minutes(),2)+U(this.seconds(),2)}),I("Hmm",0,0,function(){return""+this.hours()+U(this.minutes(),2)}),I("Hmmss",0,0,function(){return""+this.hours()+U(this.minutes(),2)+U(this.seconds(),2)}),Xe("a",!0),Xe("A",!1),H("hour","h"),L("hour",13),ue("a",Ke),ue("A",Ke),ue("H",B),ue("h",B),ue("k",B),ue("HH",B,z),ue("hh",B,z),ue("kk",B,z),ue("hmm",Q),ue("hmmss",X),ue("Hmm",Q),ue("Hmmss",X),ce(["H","HH"],ge),ce(["k","kk"],function(e,t,n){var s=k(e);t[ge]=24===s?0:s}),ce(["a","A"],function(e,t,n){n._isPm=n._locale.isPM(e),n._meridiem=e}),ce(["h","hh"],function(e,t,n){t[ge]=k(e),g(n).bigHour=!0}),ce("hmm",function(e,t,n){var s=e.length-2;t[ge]=k(e.substr(0,s)),t[pe]=k(e.substr(s)),g(n).bigHour=!0}),ce("hmmss",function(e,t,n){var s=e.length-4,i=e.length-2;t[ge]=k(e.substr(0,s)),t[pe]=k(e.substr(s,2)),t[ve]=k(e.substr(i)),g(n).bigHour=!0}),ce("Hmm",function(e,t,n){var s=e.length-2;t[ge]=k(e.substr(0,s)),t[pe]=k(e.substr(s))}),ce("Hmmss",function(e,t,n){var s=e.length-4,i=e.length-2;t[ge]=k(e.substr(0,s)),t[pe]=k(e.substr(s,2)),t[ve]=k(e.substr(i))});var et,tt=Te("Hours",!0),nt={calendar:{sameDay:"[Today at] LT",nextDay:"[Tomorrow at] LT",nextWeek:"dddd [at] LT",lastDay:"[Yesterday at] LT",lastWeek:"[Last] dddd [at] LT",sameElse:"L"},longDateFormat:{LTS:"h:mm:ss A",LT:"h:mm A",L:"MM/DD/YYYY",LL:"MMMM D, YYYY",LLL:"MMMM D, YYYY h:mm A",LLLL:"dddd, MMMM D, YYYY h:mm A"},invalidDate:"Invalid date",ordinal:"%d",dayOfMonthOrdinalParse:/\d{1,2}/,relativeTime:{future:"in %s",past:"%s ago",s:"a few seconds",ss:"%d seconds",m:"a minute",mm:"%d minutes",h:"an hour",hh:"%d hours",d:"a day",dd:"%d days",M:"a month",MM:"%d months",y:"a year",yy:"%d years"},months:He,monthsShort:Re,week:{dow:0,doy:6},weekdays:je,weekdaysMin:ze,weekdaysShort:Ze,meridiemParse:/[ap]\.?m?\.?/i},st={},it={};function rt(e){return e?e.toLowerCase().replace("_","-"):e}function at(e){var t=null;if(!st[e]&&"undefined"!=typeof module&&module&&module.exports)try{t=et._abbr,require("./locale/"+e),ot(t)}catch(e){}return st[e]}function ot(e,t){var n;return e&&((n=l(t)?lt(e):ut(e,t))?et=n:"undefined"!=typeof console&&console.warn&&console.warn("Locale "+e+" not found. Did you forget to load it?")),et._abbr}function ut(e,t){if(null!==t){var n,s=nt;if(t.abbr=e,null!=st[e])T("defineLocaleOverride","use moment.updateLocale(localeName, config) to change an existing locale. moment.defineLocale(localeName, config) should only be used for creating a new locale See http://momentjs.com/guides/#/warnings/define-locale/ for more info."),s=st[e]._config;else if(null!=t.parentLocale)if(null!=st[t.parentLocale])s=st[t.parentLocale]._config;else{if(null==(n=at(t.parentLocale)))return it[t.parentLocale]||(it[t.parentLocale]=[]),it[t.parentLocale].push({name:e,config:t}),null;s=n._config}return st[e]=new P(b(s,t)),it[e]&&it[e].forEach(function(e){ut(e.name,e.config)}),ot(e),st[e]}return delete st[e],null}function lt(e){var t;if(e&&e._locale&&e._locale._abbr&&(e=e._locale._abbr),!e)return et;if(!o(e)){if(t=at(e))return t;e=[e]}return function(e){for(var t,n,s,i,r=0;r<e.length;){for(t=(i=rt(e[r]).split("-")).length,n=(n=rt(e[r+1]))?n.split("-"):null;0<t;){if(s=at(i.slice(0,t).join("-")))return s;if(n&&n.length>=t&&a(i,n,!0)>=t-1)break;t--}r++}return et}(e)}function dt(e){var t,n=e._a;return n&&-2===g(e).overflow&&(t=n[_e]<0||11<n[_e]?_e:n[ye]<1||n[ye]>Pe(n[me],n[_e])?ye:n[ge]<0||24<n[ge]||24===n[ge]&&(0!==n[pe]||0!==n[ve]||0!==n[we])?ge:n[pe]<0||59<n[pe]?pe:n[ve]<0||59<n[ve]?ve:n[we]<0||999<n[we]?we:-1,g(e)._overflowDayOfYear&&(t<me||ye<t)&&(t=ye),g(e)._overflowWeeks&&-1===t&&(t=Me),g(e)._overflowWeekday&&-1===t&&(t=Se),g(e).overflow=t),e}function ht(e,t,n){return null!=e?e:null!=t?t:n}function ct(e){var t,n,s,i,r,a=[];if(!e._d){var o,u;for(o=e,u=new Date(c.now()),s=o._useUTC?[u.getUTCFullYear(),u.getUTCMonth(),u.getUTCDate()]:[u.getFullYear(),u.getMonth(),u.getDate()],e._w&&null==e._a[ye]&&null==e._a[_e]&&function(e){var t,n,s,i,r,a,o,u;if(null!=(t=e._w).GG||null!=t.W||null!=t.E)r=1,a=4,n=ht(t.GG,e._a[me],Ie(Tt(),1,4).year),s=ht(t.W,1),((i=ht(t.E,1))<1||7<i)&&(u=!0);else{r=e._locale._week.dow,a=e._locale._week.doy;var l=Ie(Tt(),r,a);n=ht(t.gg,e._a[me],l.year),s=ht(t.w,l.week),null!=t.d?((i=t.d)<0||6<i)&&(u=!0):null!=t.e?(i=t.e+r,(t.e<0||6<t.e)&&(u=!0)):i=r}s<1||s>Ae(n,r,a)?g(e)._overflowWeeks=!0:null!=u?g(e)._overflowWeekday=!0:(o=Ee(n,s,i,r,a),e._a[me]=o.year,e._dayOfYear=o.dayOfYear)}(e),null!=e._dayOfYear&&(r=ht(e._a[me],s[me]),(e._dayOfYear>De(r)||0===e._dayOfYear)&&(g(e)._overflowDayOfYear=!0),n=Ge(r,0,e._dayOfYear),e._a[_e]=n.getUTCMonth(),e._a[ye]=n.getUTCDate()),t=0;t<3&&null==e._a[t];++t)e._a[t]=a[t]=s[t];for(;t<7;t++)e._a[t]=a[t]=null==e._a[t]?2===t?1:0:e._a[t];24===e._a[ge]&&0===e._a[pe]&&0===e._a[ve]&&0===e._a[we]&&(e._nextDay=!0,e._a[ge]=0),e._d=(e._useUTC?Ge:function(e,t,n,s,i,r,a){var o=new Date(e,t,n,s,i,r,a);return e<100&&0<=e&&isFinite(o.getFullYear())&&o.setFullYear(e),o}).apply(null,a),i=e._useUTC?e._d.getUTCDay():e._d.getDay(),null!=e._tzm&&e._d.setUTCMinutes(e._d.getUTCMinutes()-e._tzm),e._nextDay&&(e._a[ge]=24),e._w&&void 0!==e._w.d&&e._w.d!==i&&(g(e).weekdayMismatch=!0)}}var ft=/^\s*((?:[+-]\d{6}|\d{4})-(?:\d\d-\d\d|W\d\d-\d|W\d\d|\d\d\d|\d\d))(?:(T| )(\d\d(?::\d\d(?::\d\d(?:[.,]\d+)?)?)?)([\+\-]\d\d(?::?\d\d)?|\s*Z)?)?$/,mt=/^\s*((?:[+-]\d{6}|\d{4})(?:\d\d\d\d|W\d\d\d|W\d\d|\d\d\d|\d\d))(?:(T| )(\d\d(?:\d\d(?:\d\d(?:[.,]\d+)?)?)?)([\+\-]\d\d(?::?\d\d)?|\s*Z)?)?$/,_t=/Z|[+-]\d\d(?::?\d\d)?/,yt=[["YYYYYY-MM-DD",/[+-]\d{6}-\d\d-\d\d/],["YYYY-MM-DD",/\d{4}-\d\d-\d\d/],["GGGG-[W]WW-E",/\d{4}-W\d\d-\d/],["GGGG-[W]WW",/\d{4}-W\d\d/,!1],["YYYY-DDD",/\d{4}-\d{3}/],["YYYY-MM",/\d{4}-\d\d/,!1],["YYYYYYMMDD",/[+-]\d{10}/],["YYYYMMDD",/\d{8}/],["GGGG[W]WWE",/\d{4}W\d{3}/],["GGGG[W]WW",/\d{4}W\d{2}/,!1],["YYYYDDD",/\d{7}/]],gt=[["HH:mm:ss.SSSS",/\d\d:\d\d:\d\d\.\d+/],["HH:mm:ss,SSSS",/\d\d:\d\d:\d\d,\d+/],["HH:mm:ss",/\d\d:\d\d:\d\d/],["HH:mm",/\d\d:\d\d/],["HHmmss.SSSS",/\d\d\d\d\d\d\.\d+/],["HHmmss,SSSS",/\d\d\d\d\d\d,\d+/],["HHmmss",/\d\d\d\d\d\d/],["HHmm",/\d\d\d\d/],["HH",/\d\d/]],pt=/^\/?Date\((\-?\d+)/i;function vt(e){var t,n,s,i,r,a,o=e._i,u=ft.exec(o)||mt.exec(o);if(u){for(g(e).iso=!0,t=0,n=yt.length;t<n;t++)if(yt[t][1].exec(u[1])){i=yt[t][0],s=!1!==yt[t][2];break}if(null==i)return void(e._isValid=!1);if(u[3]){for(t=0,n=gt.length;t<n;t++)if(gt[t][1].exec(u[3])){r=(u[2]||" ")+gt[t][0];break}if(null==r)return void(e._isValid=!1)}if(!s&&null!=r)return void(e._isValid=!1);if(u[4]){if(!_t.exec(u[4]))return void(e._isValid=!1);a="Z"}e._f=i+(r||"")+(a||""),kt(e)}else e._isValid=!1}var wt=/^(?:(Mon|Tue|Wed|Thu|Fri|Sat|Sun),?\s)?(\d{1,2})\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s(\d{2,4})\s(\d\d):(\d\d)(?::(\d\d))?\s(?:(UT|GMT|[ECMP][SD]T)|([Zz])|([+-]\d{4}))$/;function Mt(e,t,n,s,i,r){var a=[function(e){var t=parseInt(e,10);{if(t<=49)return 2e3+t;if(t<=999)return 1900+t}return t}(e),Re.indexOf(t),parseInt(n,10),parseInt(s,10),parseInt(i,10)];return r&&a.push(parseInt(r,10)),a}var St={UT:0,GMT:0,EDT:-240,EST:-300,CDT:-300,CST:-360,MDT:-360,MST:-420,PDT:-420,PST:-480};function Dt(e){var t,n,s,i=wt.exec(e._i.replace(/\([^)]*\)|[\n\t]/g," ").replace(/(\s\s+)/g," ").replace(/^\s\s*/,"").replace(/\s\s*$/,""));if(i){var r=Mt(i[4],i[3],i[2],i[5],i[6],i[7]);if(t=i[1],n=r,s=e,t&&Ze.indexOf(t)!==new Date(n[0],n[1],n[2]).getDay()&&(g(s).weekdayMismatch=!0,!(s._isValid=!1)))return;e._a=r,e._tzm=function(e,t,n){if(e)return St[e];if(t)return 0;var s=parseInt(n,10),i=s%100;return(s-i)/100*60+i}(i[8],i[9],i[10]),e._d=Ge.apply(null,e._a),e._d.setUTCMinutes(e._d.getUTCMinutes()-e._tzm),g(e).rfc2822=!0}else e._isValid=!1}function kt(e){if(e._f!==c.ISO_8601)if(e._f!==c.RFC_2822){e._a=[],g(e).empty=!0;var t,n,s,i,r,a,o,u,l=""+e._i,d=l.length,h=0;for(s=j(e._f,e._locale).match(N)||[],t=0;t<s.length;t++)i=s[t],(n=(l.match(le(i,e))||[])[0])&&(0<(r=l.substr(0,l.indexOf(n))).length&&g(e).unusedInput.push(r),l=l.slice(l.indexOf(n)+n.length),h+=n.length),E[i]?(n?g(e).empty=!1:g(e).unusedTokens.push(i),a=i,u=e,null!=(o=n)&&m(he,a)&&he[a](o,u._a,u,a)):e._strict&&!n&&g(e).unusedTokens.push(i);g(e).charsLeftOver=d-h,0<l.length&&g(e).unusedInput.push(l),e._a[ge]<=12&&!0===g(e).bigHour&&0<e._a[ge]&&(g(e).bigHour=void 0),g(e).parsedDateParts=e._a.slice(0),g(e).meridiem=e._meridiem,e._a[ge]=function(e,t,n){var s;if(null==n)return t;return null!=e.meridiemHour?e.meridiemHour(t,n):(null!=e.isPM&&((s=e.isPM(n))&&t<12&&(t+=12),s||12!==t||(t=0)),t)}(e._locale,e._a[ge],e._meridiem),ct(e),dt(e)}else Dt(e);else vt(e)}function Yt(e){var t,n,s,i,r=e._i,a=e._f;return e._locale=e._locale||lt(e._l),null===r||void 0===a&&""===r?v({nullInput:!0}):("string"==typeof r&&(e._i=r=e._locale.preparse(r)),S(r)?new M(dt(r)):(h(r)?e._d=r:o(a)?function(e){var t,n,s,i,r;if(0===e._f.length)return g(e).invalidFormat=!0,e._d=new Date(NaN);for(i=0;i<e._f.length;i++)r=0,t=w({},e),null!=e._useUTC&&(t._useUTC=e._useUTC),t._f=e._f[i],kt(t),p(t)&&(r+=g(t).charsLeftOver,r+=10*g(t).unusedTokens.length,g(t).score=r,(null==s||r<s)&&(s=r,n=t));_(e,n||t)}(e):a?kt(e):l(n=(t=e)._i)?t._d=new Date(c.now()):h(n)?t._d=new Date(n.valueOf()):"string"==typeof n?(s=t,null===(i=pt.exec(s._i))?(vt(s),!1===s._isValid&&(delete s._isValid,Dt(s),!1===s._isValid&&(delete s._isValid,c.createFromInputFallback(s)))):s._d=new Date(+i[1])):o(n)?(t._a=f(n.slice(0),function(e){return parseInt(e,10)}),ct(t)):u(n)?function(e){if(!e._d){var t=C(e._i);e._a=f([t.year,t.month,t.day||t.date,t.hour,t.minute,t.second,t.millisecond],function(e){return e&&parseInt(e,10)}),ct(e)}}(t):d(n)?t._d=new Date(n):c.createFromInputFallback(t),p(e)||(e._d=null),e))}function Ot(e,t,n,s,i){var r,a={};return!0!==n&&!1!==n||(s=n,n=void 0),(u(e)&&function(e){if(Object.getOwnPropertyNames)return 0===Object.getOwnPropertyNames(e).length;var t;for(t in e)if(e.hasOwnProperty(t))return!1;return!0}(e)||o(e)&&0===e.length)&&(e=void 0),a._isAMomentObject=!0,a._useUTC=a._isUTC=i,a._l=n,a._i=e,a._f=t,a._strict=s,(r=new M(dt(Yt(a))))._nextDay&&(r.add(1,"d"),r._nextDay=void 0),r}function Tt(e,t,n,s){return Ot(e,t,n,s,!1)}c.createFromInputFallback=n("value provided is not in a recognized RFC2822 or ISO format. moment construction falls back to js Date(), which is not reliable across all browsers and versions. Non RFC2822/ISO date formats are discouraged and will be removed in an upcoming major release. Please refer to http://momentjs.com/guides/#/warnings/js-date/ for more info.",function(e){e._d=new Date(e._i+(e._useUTC?" UTC":""))}),c.ISO_8601=function(){},c.RFC_2822=function(){};var xt=n("moment().min is deprecated, use moment.max instead. http://momentjs.com/guides/#/warnings/min-max/",function(){var e=Tt.apply(null,arguments);return this.isValid()&&e.isValid()?e<this?this:e:v()}),bt=n("moment().max is deprecated, use moment.min instead. http://momentjs.com/guides/#/warnings/min-max/",function(){var e=Tt.apply(null,arguments);return this.isValid()&&e.isValid()?this<e?this:e:v()});function Pt(e,t){var n,s;if(1===t.length&&o(t[0])&&(t=t[0]),!t.length)return Tt();for(n=t[0],s=1;s<t.length;++s)t[s].isValid()&&!t[s][e](n)||(n=t[s]);return n}var Wt=["year","quarter","month","week","day","hour","minute","second","millisecond"];function Ht(e){var t=C(e),n=t.year||0,s=t.quarter||0,i=t.month||0,r=t.week||0,a=t.day||0,o=t.hour||0,u=t.minute||0,l=t.second||0,d=t.millisecond||0;this._isValid=function(e){for(var t in e)if(-1===Ye.call(Wt,t)||null!=e[t]&&isNaN(e[t]))return!1;for(var n=!1,s=0;s<Wt.length;++s)if(e[Wt[s]]){if(n)return!1;parseFloat(e[Wt[s]])!==k(e[Wt[s]])&&(n=!0)}return!0}(t),this._milliseconds=+d+1e3*l+6e4*u+1e3*o*60*60,this._days=+a+7*r,this._months=+i+3*s+12*n,this._data={},this._locale=lt(),this._bubble()}function Rt(e){return e instanceof Ht}function Ct(e){return e<0?-1*Math.round(-1*e):Math.round(e)}function Ft(e,n){I(e,0,0,function(){var e=this.utcOffset(),t="+";return e<0&&(e=-e,t="-"),t+U(~~(e/60),2)+n+U(~~e%60,2)})}Ft("Z",":"),Ft("ZZ",""),ue("Z",re),ue("ZZ",re),ce(["Z","ZZ"],function(e,t,n){n._useUTC=!0,n._tzm=Ut(re,e)});var Lt=/([\+\-]|\d\d)/gi;function Ut(e,t){var n=(t||"").match(e);if(null===n)return null;var s=((n[n.length-1]||[])+"").match(Lt)||["-",0,0],i=60*s[1]+k(s[2]);return 0===i?0:"+"===s[0]?i:-i}function Nt(e,t){var n,s;return t._isUTC?(n=t.clone(),s=(S(e)||h(e)?e.valueOf():Tt(e).valueOf())-n.valueOf(),n._d.setTime(n._d.valueOf()+s),c.updateOffset(n,!1),n):Tt(e).local()}function Gt(e){return 15*-Math.round(e._d.getTimezoneOffset()/15)}function Vt(){return!!this.isValid()&&(this._isUTC&&0===this._offset)}c.updateOffset=function(){};var Et=/^(\-|\+)?(?:(\d*)[. ])?(\d+)\:(\d+)(?:\:(\d+)(\.\d*)?)?$/,It=/^(-|\+)?P(?:([-+]?[0-9,.]*)Y)?(?:([-+]?[0-9,.]*)M)?(?:([-+]?[0-9,.]*)W)?(?:([-+]?[0-9,.]*)D)?(?:T(?:([-+]?[0-9,.]*)H)?(?:([-+]?[0-9,.]*)M)?(?:([-+]?[0-9,.]*)S)?)?$/;function At(e,t){var n,s,i,r=e,a=null;return Rt(e)?r={ms:e._milliseconds,d:e._days,M:e._months}:d(e)?(r={},t?r[t]=e:r.milliseconds=e):(a=Et.exec(e))?(n="-"===a[1]?-1:1,r={y:0,d:k(a[ye])*n,h:k(a[ge])*n,m:k(a[pe])*n,s:k(a[ve])*n,ms:k(Ct(1e3*a[we]))*n}):(a=It.exec(e))?(n="-"===a[1]?-1:(a[1],1),r={y:jt(a[2],n),M:jt(a[3],n),w:jt(a[4],n),d:jt(a[5],n),h:jt(a[6],n),m:jt(a[7],n),s:jt(a[8],n)}):null==r?r={}:"object"==typeof r&&("from"in r||"to"in r)&&(i=function(e,t){var n;if(!e.isValid()||!t.isValid())return{milliseconds:0,months:0};t=Nt(t,e),e.isBefore(t)?n=Zt(e,t):((n=Zt(t,e)).milliseconds=-n.milliseconds,n.months=-n.months);return n}(Tt(r.from),Tt(r.to)),(r={}).ms=i.milliseconds,r.M=i.months),s=new Ht(r),Rt(e)&&m(e,"_locale")&&(s._locale=e._locale),s}function jt(e,t){var n=e&&parseFloat(e.replace(",","."));return(isNaN(n)?0:n)*t}function Zt(e,t){var n={milliseconds:0,months:0};return n.months=t.month()-e.month()+12*(t.year()-e.year()),e.clone().add(n.months,"M").isAfter(t)&&--n.months,n.milliseconds=+t-+e.clone().add(n.months,"M"),n}function zt(s,i){return function(e,t){var n;return null===t||isNaN(+t)||(T(i,"moment()."+i+"(period, number) is deprecated. Please use moment()."+i+"(number, period). See http://momentjs.com/guides/#/warnings/add-inverted-param/ for more info."),n=e,e=t,t=n),$t(this,At(e="string"==typeof e?+e:e,t),s),this}}function $t(e,t,n,s){var i=t._milliseconds,r=Ct(t._days),a=Ct(t._months);e.isValid()&&(s=null==s||s,a&&Ce(e,xe(e,"Month")+a*n),r&&be(e,"Date",xe(e,"Date")+r*n),i&&e._d.setTime(e._d.valueOf()+i*n),s&&c.updateOffset(e,r||a))}At.fn=Ht.prototype,At.invalid=function(){return At(NaN)};var qt=zt(1,"add"),Jt=zt(-1,"subtract");function Bt(e,t){var n=12*(t.year()-e.year())+(t.month()-e.month()),s=e.clone().add(n,"months");return-(n+(t-s<0?(t-s)/(s-e.clone().add(n-1,"months")):(t-s)/(e.clone().add(n+1,"months")-s)))||0}function Qt(e){var t;return void 0===e?this._locale._abbr:(null!=(t=lt(e))&&(this._locale=t),this)}c.defaultFormat="YYYY-MM-DDTHH:mm:ssZ",c.defaultFormatUtc="YYYY-MM-DDTHH:mm:ss[Z]";var Xt=n("moment().lang() is deprecated. Instead, use moment().localeData() to get the language configuration. Use moment().locale() to change languages.",function(e){return void 0===e?this.localeData():this.locale(e)});function Kt(){return this._locale}function en(e,t){I(0,[e,e.length],0,t)}function tn(e,t,n,s,i){var r;return null==e?Ie(this,s,i).year:((r=Ae(e,s,i))<t&&(t=r),function(e,t,n,s,i){var r=Ee(e,t,n,s,i),a=Ge(r.year,0,r.dayOfYear);return this.year(a.getUTCFullYear()),this.month(a.getUTCMonth()),this.date(a.getUTCDate()),this}.call(this,e,t,n,s,i))}I(0,["gg",2],0,function(){return this.weekYear()%100}),I(0,["GG",2],0,function(){return this.isoWeekYear()%100}),en("gggg","weekYear"),en("ggggg","weekYear"),en("GGGG","isoWeekYear"),en("GGGGG","isoWeekYear"),H("weekYear","gg"),H("isoWeekYear","GG"),L("weekYear",1),L("isoWeekYear",1),ue("G",se),ue("g",se),ue("GG",B,z),ue("gg",B,z),ue("GGGG",ee,q),ue("gggg",ee,q),ue("GGGGG",te,J),ue("ggggg",te,J),fe(["gggg","ggggg","GGGG","GGGGG"],function(e,t,n,s){t[s.substr(0,2)]=k(e)}),fe(["gg","GG"],function(e,t,n,s){t[s]=c.parseTwoDigitYear(e)}),I("Q",0,"Qo","quarter"),H("quarter","Q"),L("quarter",7),ue("Q",Z),ce("Q",function(e,t){t[_e]=3*(k(e)-1)}),I("D",["DD",2],"Do","date"),H("date","D"),L("date",9),ue("D",B),ue("DD",B,z),ue("Do",function(e,t){return e?t._dayOfMonthOrdinalParse||t._ordinalParse:t._dayOfMonthOrdinalParseLenient}),ce(["D","DD"],ye),ce("Do",function(e,t){t[ye]=k(e.match(B)[0])});var nn=Te("Date",!0);I("DDD",["DDDD",3],"DDDo","dayOfYear"),H("dayOfYear","DDD"),L("dayOfYear",4),ue("DDD",K),ue("DDDD",$),ce(["DDD","DDDD"],function(e,t,n){n._dayOfYear=k(e)}),I("m",["mm",2],0,"minute"),H("minute","m"),L("minute",14),ue("m",B),ue("mm",B,z),ce(["m","mm"],pe);var sn=Te("Minutes",!1);I("s",["ss",2],0,"second"),H("second","s"),L("second",15),ue("s",B),ue("ss",B,z),ce(["s","ss"],ve);var rn,an=Te("Seconds",!1);for(I("S",0,0,function(){return~~(this.millisecond()/100)}),I(0,["SS",2],0,function(){return~~(this.millisecond()/10)}),I(0,["SSS",3],0,"millisecond"),I(0,["SSSS",4],0,function(){return 10*this.millisecond()}),I(0,["SSSSS",5],0,function(){return 100*this.millisecond()}),I(0,["SSSSSS",6],0,function(){return 1e3*this.millisecond()}),I(0,["SSSSSSS",7],0,function(){return 1e4*this.millisecond()}),I(0,["SSSSSSSS",8],0,function(){return 1e5*this.millisecond()}),I(0,["SSSSSSSSS",9],0,function(){return 1e6*this.millisecond()}),H("millisecond","ms"),L("millisecond",16),ue("S",K,Z),ue("SS",K,z),ue("SSS",K,$),rn="SSSS";rn.length<=9;rn+="S")ue(rn,ne);function on(e,t){t[we]=k(1e3*("0."+e))}for(rn="S";rn.length<=9;rn+="S")ce(rn,on);var un=Te("Milliseconds",!1);I("z",0,0,"zoneAbbr"),I("zz",0,0,"zoneName");var ln=M.prototype;function dn(e){return e}ln.add=qt,ln.calendar=function(e,t){var n=e||Tt(),s=Nt(n,this).startOf("day"),i=c.calendarFormat(this,s)||"sameElse",r=t&&(x(t[i])?t[i].call(this,n):t[i]);return this.format(r||this.localeData().calendar(i,this,Tt(n)))},ln.clone=function(){return new M(this)},ln.diff=function(e,t,n){var s,i,r;if(!this.isValid())return NaN;if(!(s=Nt(e,this)).isValid())return NaN;switch(i=6e4*(s.utcOffset()-this.utcOffset()),t=R(t)){case"year":r=Bt(this,s)/12;break;case"month":r=Bt(this,s);break;case"quarter":r=Bt(this,s)/3;break;case"second":r=(this-s)/1e3;break;case"minute":r=(this-s)/6e4;break;case"hour":r=(this-s)/36e5;break;case"day":r=(this-s-i)/864e5;break;case"week":r=(this-s-i)/6048e5;break;default:r=this-s}return n?r:D(r)},ln.endOf=function(e){return void 0===(e=R(e))||"millisecond"===e?this:("date"===e&&(e="day"),this.startOf(e).add(1,"isoWeek"===e?"week":e).subtract(1,"ms"))},ln.format=function(e){e||(e=this.isUtc()?c.defaultFormatUtc:c.defaultFormat);var t=A(this,e);return this.localeData().postformat(t)},ln.from=function(e,t){return this.isValid()&&(S(e)&&e.isValid()||Tt(e).isValid())?At({to:this,from:e}).locale(this.locale()).humanize(!t):this.localeData().invalidDate()},ln.fromNow=function(e){return this.from(Tt(),e)},ln.to=function(e,t){return this.isValid()&&(S(e)&&e.isValid()||Tt(e).isValid())?At({from:this,to:e}).locale(this.locale()).humanize(!t):this.localeData().invalidDate()},ln.toNow=function(e){return this.to(Tt(),e)},ln.get=function(e){return x(this[e=R(e)])?this[e]():this},ln.invalidAt=function(){return g(this).overflow},ln.isAfter=function(e,t){var n=S(e)?e:Tt(e);return!(!this.isValid()||!n.isValid())&&("millisecond"===(t=R(l(t)?"millisecond":t))?this.valueOf()>n.valueOf():n.valueOf()<this.clone().startOf(t).valueOf())},ln.isBefore=function(e,t){var n=S(e)?e:Tt(e);return!(!this.isValid()||!n.isValid())&&("millisecond"===(t=R(l(t)?"millisecond":t))?this.valueOf()<n.valueOf():this.clone().endOf(t).valueOf()<n.valueOf())},ln.isBetween=function(e,t,n,s){return("("===(s=s||"()")[0]?this.isAfter(e,n):!this.isBefore(e,n))&&(")"===s[1]?this.isBefore(t,n):!this.isAfter(t,n))},ln.isSame=function(e,t){var n,s=S(e)?e:Tt(e);return!(!this.isValid()||!s.isValid())&&("millisecond"===(t=R(t||"millisecond"))?this.valueOf()===s.valueOf():(n=s.valueOf(),this.clone().startOf(t).valueOf()<=n&&n<=this.clone().endOf(t).valueOf()))},ln.isSameOrAfter=function(e,t){return this.isSame(e,t)||this.isAfter(e,t)},ln.isSameOrBefore=function(e,t){return this.isSame(e,t)||this.isBefore(e,t)},ln.isValid=function(){return p(this)},ln.lang=Xt,ln.locale=Qt,ln.localeData=Kt,ln.max=bt,ln.min=xt,ln.parsingFlags=function(){return _({},g(this))},ln.set=function(e,t){if("object"==typeof e)for(var n=function(e){var t=[];for(var n in e)t.push({unit:n,priority:F[n]});return t.sort(function(e,t){return e.priority-t.priority}),t}(e=C(e)),s=0;s<n.length;s++)this[n[s].unit](e[n[s].unit]);else if(x(this[e=R(e)]))return this[e](t);return this},ln.startOf=function(e){switch(e=R(e)){case"year":this.month(0);case"quarter":case"month":this.date(1);case"week":case"isoWeek":case"day":case"date":this.hours(0);case"hour":this.minutes(0);case"minute":this.seconds(0);case"second":this.milliseconds(0)}return"week"===e&&this.weekday(0),"isoWeek"===e&&this.isoWeekday(1),"quarter"===e&&this.month(3*Math.floor(this.month()/3)),this},ln.subtract=Jt,ln.toArray=function(){var e=this;return[e.year(),e.month(),e.date(),e.hour(),e.minute(),e.second(),e.millisecond()]},ln.toObject=function(){var e=this;return{years:e.year(),months:e.month(),date:e.date(),hours:e.hours(),minutes:e.minutes(),seconds:e.seconds(),milliseconds:e.milliseconds()}},ln.toDate=function(){return new Date(this.valueOf())},ln.toISOString=function(e){if(!this.isValid())return null;var t=!0!==e,n=t?this.clone().utc():this;return n.year()<0||9999<n.year()?A(n,t?"YYYYYY-MM-DD[T]HH:mm:ss.SSS[Z]":"YYYYYY-MM-DD[T]HH:mm:ss.SSSZ"):x(Date.prototype.toISOString)?t?this.toDate().toISOString():new Date(this.valueOf()+60*this.utcOffset()*1e3).toISOString().replace("Z",A(n,"Z")):A(n,t?"YYYY-MM-DD[T]HH:mm:ss.SSS[Z]":"YYYY-MM-DD[T]HH:mm:ss.SSSZ")},ln.inspect=function(){if(!this.isValid())return"moment.invalid(/* "+this._i+" */)";var e="moment",t="";this.isLocal()||(e=0===this.utcOffset()?"moment.utc":"moment.parseZone",t="Z");var n="["+e+'("]',s=0<=this.year()&&this.year()<=9999?"YYYY":"YYYYYY",i=t+'[")]';return this.format(n+s+"-MM-DD[T]HH:mm:ss.SSS"+i)},ln.toJSON=function(){return this.isValid()?this.toISOString():null},ln.toString=function(){return this.clone().locale("en").format("ddd MMM DD YYYY HH:mm:ss [GMT]ZZ")},ln.unix=function(){return Math.floor(this.valueOf()/1e3)},ln.valueOf=function(){return this._d.valueOf()-6e4*(this._offset||0)},ln.creationData=function(){return{input:this._i,format:this._f,locale:this._locale,isUTC:this._isUTC,strict:this._strict}},ln.year=Oe,ln.isLeapYear=function(){return ke(this.year())},ln.weekYear=function(e){return tn.call(this,e,this.week(),this.weekday(),this.localeData()._week.dow,this.localeData()._week.doy)},ln.isoWeekYear=function(e){return tn.call(this,e,this.isoWeek(),this.isoWeekday(),1,4)},ln.quarter=ln.quarters=function(e){return null==e?Math.ceil((this.month()+1)/3):this.month(3*(e-1)+this.month()%3)},ln.month=Fe,ln.daysInMonth=function(){return Pe(this.year(),this.month())},ln.week=ln.weeks=function(e){var t=this.localeData().week(this);return null==e?t:this.add(7*(e-t),"d")},ln.isoWeek=ln.isoWeeks=function(e){var t=Ie(this,1,4).week;return null==e?t:this.add(7*(e-t),"d")},ln.weeksInYear=function(){var e=this.localeData()._week;return Ae(this.year(),e.dow,e.doy)},ln.isoWeeksInYear=function(){return Ae(this.year(),1,4)},ln.date=nn,ln.day=ln.days=function(e){if(!this.isValid())return null!=e?this:NaN;var t,n,s=this._isUTC?this._d.getUTCDay():this._d.getDay();return null!=e?(t=e,n=this.localeData(),e="string"!=typeof t?t:isNaN(t)?"number"==typeof(t=n.weekdaysParse(t))?t:null:parseInt(t,10),this.add(e-s,"d")):s},ln.weekday=function(e){if(!this.isValid())return null!=e?this:NaN;var t=(this.day()+7-this.localeData()._week.dow)%7;return null==e?t:this.add(e-t,"d")},ln.isoWeekday=function(e){if(!this.isValid())return null!=e?this:NaN;if(null!=e){var t=(n=e,s=this.localeData(),"string"==typeof n?s.weekdaysParse(n)%7||7:isNaN(n)?null:n);return this.day(this.day()%7?t:t-7)}return this.day()||7;var n,s},ln.dayOfYear=function(e){var t=Math.round((this.clone().startOf("day")-this.clone().startOf("year"))/864e5)+1;return null==e?t:this.add(e-t,"d")},ln.hour=ln.hours=tt,ln.minute=ln.minutes=sn,ln.second=ln.seconds=an,ln.millisecond=ln.milliseconds=un,ln.utcOffset=function(e,t,n){var s,i=this._offset||0;if(!this.isValid())return null!=e?this:NaN;if(null!=e){if("string"==typeof e){if(null===(e=Ut(re,e)))return this}else Math.abs(e)<16&&!n&&(e*=60);return!this._isUTC&&t&&(s=Gt(this)),this._offset=e,this._isUTC=!0,null!=s&&this.add(s,"m"),i!==e&&(!t||this._changeInProgress?$t(this,At(e-i,"m"),1,!1):this._changeInProgress||(this._changeInProgress=!0,c.updateOffset(this,!0),this._changeInProgress=null)),this}return this._isUTC?i:Gt(this)},ln.utc=function(e){return this.utcOffset(0,e)},ln.local=function(e){return this._isUTC&&(this.utcOffset(0,e),this._isUTC=!1,e&&this.subtract(Gt(this),"m")),this},ln.parseZone=function(){if(null!=this._tzm)this.utcOffset(this._tzm,!1,!0);else if("string"==typeof this._i){var e=Ut(ie,this._i);null!=e?this.utcOffset(e):this.utcOffset(0,!0)}return this},ln.hasAlignedHourOffset=function(e){return!!this.isValid()&&(e=e?Tt(e).utcOffset():0,(this.utcOffset()-e)%60==0)},ln.isDST=function(){return this.utcOffset()>this.clone().month(0).utcOffset()||this.utcOffset()>this.clone().month(5).utcOffset()},ln.isLocal=function(){return!!this.isValid()&&!this._isUTC},ln.isUtcOffset=function(){return!!this.isValid()&&this._isUTC},ln.isUtc=Vt,ln.isUTC=Vt,ln.zoneAbbr=function(){return this._isUTC?"UTC":""},ln.zoneName=function(){return this._isUTC?"Coordinated Universal Time":""},ln.dates=n("dates accessor is deprecated. Use date instead.",nn),ln.months=n("months accessor is deprecated. Use month instead",Fe),ln.years=n("years accessor is deprecated. Use year instead",Oe),ln.zone=n("moment().zone is deprecated, use moment().utcOffset instead. http://momentjs.com/guides/#/warnings/zone/",function(e,t){return null!=e?("string"!=typeof e&&(e=-e),this.utcOffset(e,t),this):-this.utcOffset()}),ln.isDSTShifted=n("isDSTShifted is deprecated. See http://momentjs.com/guides/#/warnings/dst-shifted/ for more information",function(){if(!l(this._isDSTShifted))return this._isDSTShifted;var e={};if(w(e,this),(e=Yt(e))._a){var t=e._isUTC?y(e._a):Tt(e._a);this._isDSTShifted=this.isValid()&&0<a(e._a,t.toArray())}else this._isDSTShifted=!1;return this._isDSTShifted});var hn=P.prototype;function cn(e,t,n,s){var i=lt(),r=y().set(s,t);return i[n](r,e)}function fn(e,t,n){if(d(e)&&(t=e,e=void 0),e=e||"",null!=t)return cn(e,t,n,"month");var s,i=[];for(s=0;s<12;s++)i[s]=cn(e,s,n,"month");return i}function mn(e,t,n,s){"boolean"==typeof e?d(t)&&(n=t,t=void 0):(t=e,e=!1,d(n=t)&&(n=t,t=void 0)),t=t||"";var i,r=lt(),a=e?r._week.dow:0;if(null!=n)return cn(t,(n+a)%7,s,"day");var o=[];for(i=0;i<7;i++)o[i]=cn(t,(i+a)%7,s,"day");return o}hn.calendar=function(e,t,n){var s=this._calendar[e]||this._calendar.sameElse;return x(s)?s.call(t,n):s},hn.longDateFormat=function(e){var t=this._longDateFormat[e],n=this._longDateFormat[e.toUpperCase()];return t||!n?t:(this._longDateFormat[e]=n.replace(/MMMM|MM|DD|dddd/g,function(e){return e.slice(1)}),this._longDateFormat[e])},hn.invalidDate=function(){return this._invalidDate},hn.ordinal=function(e){return this._ordinal.replace("%d",e)},hn.preparse=dn,hn.postformat=dn,hn.relativeTime=function(e,t,n,s){var i=this._relativeTime[n];return x(i)?i(e,t,n,s):i.replace(/%d/i,e)},hn.pastFuture=function(e,t){var n=this._relativeTime[0<e?"future":"past"];return x(n)?n(t):n.replace(/%s/i,t)},hn.set=function(e){var t,n;for(n in e)x(t=e[n])?this[n]=t:this["_"+n]=t;this._config=e,this._dayOfMonthOrdinalParseLenient=new RegExp((this._dayOfMonthOrdinalParse.source||this._ordinalParse.source)+"|"+/\d{1,2}/.source)},hn.months=function(e,t){return e?o(this._months)?this._months[e.month()]:this._months[(this._months.isFormat||We).test(t)?"format":"standalone"][e.month()]:o(this._months)?this._months:this._months.standalone},hn.monthsShort=function(e,t){return e?o(this._monthsShort)?this._monthsShort[e.month()]:this._monthsShort[We.test(t)?"format":"standalone"][e.month()]:o(this._monthsShort)?this._monthsShort:this._monthsShort.standalone},hn.monthsParse=function(e,t,n){var s,i,r;if(this._monthsParseExact)return function(e,t,n){var s,i,r,a=e.toLocaleLowerCase();if(!this._monthsParse)for(this._monthsParse=[],this._longMonthsParse=[],this._shortMonthsParse=[],s=0;s<12;++s)r=y([2e3,s]),this._shortMonthsParse[s]=this.monthsShort(r,"").toLocaleLowerCase(),this._longMonthsParse[s]=this.months(r,"").toLocaleLowerCase();return n?"MMM"===t?-1!==(i=Ye.call(this._shortMonthsParse,a))?i:null:-1!==(i=Ye.call(this._longMonthsParse,a))?i:null:"MMM"===t?-1!==(i=Ye.call(this._shortMonthsParse,a))?i:-1!==(i=Ye.call(this._longMonthsParse,a))?i:null:-1!==(i=Ye.call(this._longMonthsParse,a))?i:-1!==(i=Ye.call(this._shortMonthsParse,a))?i:null}.call(this,e,t,n);for(this._monthsParse||(this._monthsParse=[],this._longMonthsParse=[],this._shortMonthsParse=[]),s=0;s<12;s++){if(i=y([2e3,s]),n&&!this._longMonthsParse[s]&&(this._longMonthsParse[s]=new RegExp("^"+this.months(i,"").replace(".","")+"$","i"),this._shortMonthsParse[s]=new RegExp("^"+this.monthsShort(i,"").replace(".","")+"$","i")),n||this._monthsParse[s]||(r="^"+this.months(i,"")+"|^"+this.monthsShort(i,""),this._monthsParse[s]=new RegExp(r.replace(".",""),"i")),n&&"MMMM"===t&&this._longMonthsParse[s].test(e))return s;if(n&&"MMM"===t&&this._shortMonthsParse[s].test(e))return s;if(!n&&this._monthsParse[s].test(e))return s}},hn.monthsRegex=function(e){return this._monthsParseExact?(m(this,"_monthsRegex")||Ne.call(this),e?this._monthsStrictRegex:this._monthsRegex):(m(this,"_monthsRegex")||(this._monthsRegex=Ue),this._monthsStrictRegex&&e?this._monthsStrictRegex:this._monthsRegex)},hn.monthsShortRegex=function(e){return this._monthsParseExact?(m(this,"_monthsRegex")||Ne.call(this),e?this._monthsShortStrictRegex:this._monthsShortRegex):(m(this,"_monthsShortRegex")||(this._monthsShortRegex=Le),this._monthsShortStrictRegex&&e?this._monthsShortStrictRegex:this._monthsShortRegex)},hn.week=function(e){return Ie(e,this._week.dow,this._week.doy).week},hn.firstDayOfYear=function(){return this._week.doy},hn.firstDayOfWeek=function(){return this._week.dow},hn.weekdays=function(e,t){return e?o(this._weekdays)?this._weekdays[e.day()]:this._weekdays[this._weekdays.isFormat.test(t)?"format":"standalone"][e.day()]:o(this._weekdays)?this._weekdays:this._weekdays.standalone},hn.weekdaysMin=function(e){return e?this._weekdaysMin[e.day()]:this._weekdaysMin},hn.weekdaysShort=function(e){return e?this._weekdaysShort[e.day()]:this._weekdaysShort},hn.weekdaysParse=function(e,t,n){var s,i,r;if(this._weekdaysParseExact)return function(e,t,n){var s,i,r,a=e.toLocaleLowerCase();if(!this._weekdaysParse)for(this._weekdaysParse=[],this._shortWeekdaysParse=[],this._minWeekdaysParse=[],s=0;s<7;++s)r=y([2e3,1]).day(s),this._minWeekdaysParse[s]=this.weekdaysMin(r,"").toLocaleLowerCase(),this._shortWeekdaysParse[s]=this.weekdaysShort(r,"").toLocaleLowerCase(),this._weekdaysParse[s]=this.weekdays(r,"").toLocaleLowerCase();return n?"dddd"===t?-1!==(i=Ye.call(this._weekdaysParse,a))?i:null:"ddd"===t?-1!==(i=Ye.call(this._shortWeekdaysParse,a))?i:null:-1!==(i=Ye.call(this._minWeekdaysParse,a))?i:null:"dddd"===t?-1!==(i=Ye.call(this._weekdaysParse,a))?i:-1!==(i=Ye.call(this._shortWeekdaysParse,a))?i:-1!==(i=Ye.call(this._minWeekdaysParse,a))?i:null:"ddd"===t?-1!==(i=Ye.call(this._shortWeekdaysParse,a))?i:-1!==(i=Ye.call(this._weekdaysParse,a))?i:-1!==(i=Ye.call(this._minWeekdaysParse,a))?i:null:-1!==(i=Ye.call(this._minWeekdaysParse,a))?i:-1!==(i=Ye.call(this._weekdaysParse,a))?i:-1!==(i=Ye.call(this._shortWeekdaysParse,a))?i:null}.call(this,e,t,n);for(this._weekdaysParse||(this._weekdaysParse=[],this._minWeekdaysParse=[],this._shortWeekdaysParse=[],this._fullWeekdaysParse=[]),s=0;s<7;s++){if(i=y([2e3,1]).day(s),n&&!this._fullWeekdaysParse[s]&&(this._fullWeekdaysParse[s]=new RegExp("^"+this.weekdays(i,"").replace(".","\\.?")+"$","i"),this._shortWeekdaysParse[s]=new RegExp("^"+this.weekdaysShort(i,"").replace(".","\\.?")+"$","i"),this._minWeekdaysParse[s]=new RegExp("^"+this.weekdaysMin(i,"").replace(".","\\.?")+"$","i")),this._weekdaysParse[s]||(r="^"+this.weekdays(i,"")+"|^"+this.weekdaysShort(i,"")+"|^"+this.weekdaysMin(i,""),this._weekdaysParse[s]=new RegExp(r.replace(".",""),"i")),n&&"dddd"===t&&this._fullWeekdaysParse[s].test(e))return s;if(n&&"ddd"===t&&this._shortWeekdaysParse[s].test(e))return s;if(n&&"dd"===t&&this._minWeekdaysParse[s].test(e))return s;if(!n&&this._weekdaysParse[s].test(e))return s}},hn.weekdaysRegex=function(e){return this._weekdaysParseExact?(m(this,"_weekdaysRegex")||Be.call(this),e?this._weekdaysStrictRegex:this._weekdaysRegex):(m(this,"_weekdaysRegex")||(this._weekdaysRegex=$e),this._weekdaysStrictRegex&&e?this._weekdaysStrictRegex:this._weekdaysRegex)},hn.weekdaysShortRegex=function(e){return this._weekdaysParseExact?(m(this,"_weekdaysRegex")||Be.call(this),e?this._weekdaysShortStrictRegex:this._weekdaysShortRegex):(m(this,"_weekdaysShortRegex")||(this._weekdaysShortRegex=qe),this._weekdaysShortStrictRegex&&e?this._weekdaysShortStrictRegex:this._weekdaysShortRegex)},hn.weekdaysMinRegex=function(e){return this._weekdaysParseExact?(m(this,"_weekdaysRegex")||Be.call(this),e?this._weekdaysMinStrictRegex:this._weekdaysMinRegex):(m(this,"_weekdaysMinRegex")||(this._weekdaysMinRegex=Je),this._weekdaysMinStrictRegex&&e?this._weekdaysMinStrictRegex:this._weekdaysMinRegex)},hn.isPM=function(e){return"p"===(e+"").toLowerCase().charAt(0)},hn.meridiem=function(e,t,n){return 11<e?n?"pm":"PM":n?"am":"AM"},ot("en",{dayOfMonthOrdinalParse:/\d{1,2}(th|st|nd|rd)/,ordinal:function(e){var t=e%10;return e+(1===k(e%100/10)?"th":1===t?"st":2===t?"nd":3===t?"rd":"th")}}),c.lang=n("moment.lang is deprecated. Use moment.locale instead.",ot),c.langData=n("moment.langData is deprecated. Use moment.localeData instead.",lt);var _n=Math.abs;function yn(e,t,n,s){var i=At(t,n);return e._milliseconds+=s*i._milliseconds,e._days+=s*i._days,e._months+=s*i._months,e._bubble()}function gn(e){return e<0?Math.floor(e):Math.ceil(e)}function pn(e){return 4800*e/146097}function vn(e){return 146097*e/4800}function wn(e){return function(){return this.as(e)}}var Mn=wn("ms"),Sn=wn("s"),Dn=wn("m"),kn=wn("h"),Yn=wn("d"),On=wn("w"),Tn=wn("M"),xn=wn("y");function bn(e){return function(){return this.isValid()?this._data[e]:NaN}}var Pn=bn("milliseconds"),Wn=bn("seconds"),Hn=bn("minutes"),Rn=bn("hours"),Cn=bn("days"),Fn=bn("months"),Ln=bn("years");var Un=Math.round,Nn={ss:44,s:45,m:45,h:22,d:26,M:11};var Gn=Math.abs;function Vn(e){return(0<e)-(e<0)||+e}function En(){if(!this.isValid())return this.localeData().invalidDate();var e,t,n=Gn(this._milliseconds)/1e3,s=Gn(this._days),i=Gn(this._months);t=D((e=D(n/60))/60),n%=60,e%=60;var r=D(i/12),a=i%=12,o=s,u=t,l=e,d=n?n.toFixed(3).replace(/\.?0+$/,""):"",h=this.asSeconds();if(!h)return"P0D";var c=h<0?"-":"",f=Vn(this._months)!==Vn(h)?"-":"",m=Vn(this._days)!==Vn(h)?"-":"",_=Vn(this._milliseconds)!==Vn(h)?"-":"";return c+"P"+(r?f+r+"Y":"")+(a?f+a+"M":"")+(o?m+o+"D":"")+(u||l||d?"T":"")+(u?_+u+"H":"")+(l?_+l+"M":"")+(d?_+d+"S":"")}var In=Ht.prototype;return In.isValid=function(){return this._isValid},In.abs=function(){var e=this._data;return this._milliseconds=_n(this._milliseconds),this._days=_n(this._days),this._months=_n(this._months),e.milliseconds=_n(e.milliseconds),e.seconds=_n(e.seconds),e.minutes=_n(e.minutes),e.hours=_n(e.hours),e.months=_n(e.months),e.years=_n(e.years),this},In.add=function(e,t){return yn(this,e,t,1)},In.subtract=function(e,t){return yn(this,e,t,-1)},In.as=function(e){if(!this.isValid())return NaN;var t,n,s=this._milliseconds;if("month"===(e=R(e))||"year"===e)return t=this._days+s/864e5,n=this._months+pn(t),"month"===e?n:n/12;switch(t=this._days+Math.round(vn(this._months)),e){case"week":return t/7+s/6048e5;case"day":return t+s/864e5;case"hour":return 24*t+s/36e5;case"minute":return 1440*t+s/6e4;case"second":return 86400*t+s/1e3;case"millisecond":return Math.floor(864e5*t)+s;default:throw new Error("Unknown unit "+e)}},In.asMilliseconds=Mn,In.asSeconds=Sn,In.asMinutes=Dn,In.asHours=kn,In.asDays=Yn,In.asWeeks=On,In.asMonths=Tn,In.asYears=xn,In.valueOf=function(){return this.isValid()?this._milliseconds+864e5*this._days+this._months%12*2592e6+31536e6*k(this._months/12):NaN},In._bubble=function(){var e,t,n,s,i,r=this._milliseconds,a=this._days,o=this._months,u=this._data;return 0<=r&&0<=a&&0<=o||r<=0&&a<=0&&o<=0||(r+=864e5*gn(vn(o)+a),o=a=0),u.milliseconds=r%1e3,e=D(r/1e3),u.seconds=e%60,t=D(e/60),u.minutes=t%60,n=D(t/60),u.hours=n%24,o+=i=D(pn(a+=D(n/24))),a-=gn(vn(i)),s=D(o/12),o%=12,u.days=a,u.months=o,u.years=s,this},In.clone=function(){return At(this)},In.get=function(e){return e=R(e),this.isValid()?this[e+"s"]():NaN},In.milliseconds=Pn,In.seconds=Wn,In.minutes=Hn,In.hours=Rn,In.days=Cn,In.weeks=function(){return D(this.days()/7)},In.months=Fn,In.years=Ln,In.humanize=function(e){if(!this.isValid())return this.localeData().invalidDate();var t,n,s,i,r,a,o,u,l,d,h,c=this.localeData(),f=(n=!e,s=c,i=At(t=this).abs(),r=Un(i.as("s")),a=Un(i.as("m")),o=Un(i.as("h")),u=Un(i.as("d")),l=Un(i.as("M")),d=Un(i.as("y")),(h=r<=Nn.ss&&["s",r]||r<Nn.s&&["ss",r]||a<=1&&["m"]||a<Nn.m&&["mm",a]||o<=1&&["h"]||o<Nn.h&&["hh",o]||u<=1&&["d"]||u<Nn.d&&["dd",u]||l<=1&&["M"]||l<Nn.M&&["MM",l]||d<=1&&["y"]||["yy",d])[2]=n,h[3]=0<+t,h[4]=s,function(e,t,n,s,i){return i.relativeTime(t||1,!!n,e,s)}.apply(null,h));return e&&(f=c.pastFuture(+this,f)),c.postformat(f)},In.toISOString=En,In.toString=En,In.toJSON=En,In.locale=Qt,In.localeData=Kt,In.toIsoString=n("toIsoString() is deprecated. Please use toISOString() instead (notice the capitals)",En),In.lang=Xt,I("X",0,0,"unix"),I("x",0,0,"valueOf"),ue("x",se),ue("X",/[+-]?\d+(\.\d{1,3})?/),ce("X",function(e,t,n){n._d=new Date(1e3*parseFloat(e,10))}),ce("x",function(e,t,n){n._d=new Date(k(e))}),c.version="2.22.2",e=Tt,c.fn=ln,c.min=function(){return Pt("isBefore",[].slice.call(arguments,0))},c.max=function(){return Pt("isAfter",[].slice.call(arguments,0))},c.now=function(){return Date.now?Date.now():+new Date},c.utc=y,c.unix=function(e){return Tt(1e3*e)},c.months=function(e,t){return fn(e,t,"months")},c.isDate=h,c.locale=ot,c.invalid=v,c.duration=At,c.isMoment=S,c.weekdays=function(e,t,n){return mn(e,t,n,"weekdays")},c.parseZone=function(){return Tt.apply(null,arguments).parseZone()},c.localeData=lt,c.isDuration=Rt,c.monthsShort=function(e,t){return fn(e,t,"monthsShort")},c.weekdaysMin=function(e,t,n){return mn(e,t,n,"weekdaysMin")},c.defineLocale=ut,c.updateLocale=function(e,t){if(null!=t){var n,s,i=nt;null!=(s=at(e))&&(i=s._config),(n=new P(t=b(i,t))).parentLocale=st[e],st[e]=n,ot(e)}else null!=st[e]&&(null!=st[e].parentLocale?st[e]=st[e].parentLocale:null!=st[e]&&delete st[e]);return st[e]},c.locales=function(){return s(st)},c.weekdaysShort=function(e,t,n){return mn(e,t,n,"weekdaysShort")},c.normalizeUnits=R,c.relativeTimeRounding=function(e){return void 0===e?Un:"function"==typeof e&&(Un=e,!0)},c.relativeTimeThreshold=function(e,t){return void 0!==Nn[e]&&(void 0===t?Nn[e]:(Nn[e]=t,"s"===e&&(Nn.ss=t-1),!0))},c.calendarFormat=function(e,t){var n=e.diff(t,"days",!0);return n<-6?"sameElse":n<-1?"lastWeek":n<0?"lastDay":n<1?"sameDay":n<2?"nextDay":n<7?"nextWeek":"sameElse"},c.prototype=ln,c.HTML5_FMT={DATETIME_LOCAL:"YYYY-MM-DDTHH:mm",DATETIME_LOCAL_SECONDS:"YYYY-MM-DDTHH:mm:ss",DATETIME_LOCAL_MS:"YYYY-MM-DDTHH:mm:ss.SSS",DATE:"YYYY-MM-DD",TIME:"HH:mm",TIME_SECONDS:"HH:mm:ss",TIME_MS:"HH:mm:ss.SSS",WEEK:"YYYY-[W]WW",MONTH:"YYYY-MM"},c});
/*!
 * jQuery UI Touch Punch 0.2.3
 *
 * Copyright 2011–2014, Dave Furfero
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * Depends:
 *  jquery.ui.widget.js
 *  jquery.ui.mouse.js
 */
!function(a){function f(a,b){if(!(a.originalEvent.touches.length>1)){a.preventDefault();var c=a.originalEvent.changedTouches[0],d=document.createEvent("MouseEvents");d.initMouseEvent(b,!0,!0,window,1,c.screenX,c.screenY,c.clientX,c.clientY,!1,!1,!1,!1,0,null),a.target.dispatchEvent(d)}}if(a.support.touch="ontouchend"in document,a.support.touch){var e,b=a.ui.mouse.prototype,c=b._mouseInit,d=b._mouseDestroy;b._touchStart=function(a){var b=this;!e&&b._mouseCapture(a.originalEvent.changedTouches[0])&&(e=!0,b._touchMoved=!1,f(a,"mouseover"),f(a,"mousemove"),f(a,"mousedown"))},b._touchMove=function(a){e&&(this._touchMoved=!0,f(a,"mousemove"))},b._touchEnd=function(a){e&&(f(a,"mouseup"),f(a,"mouseout"),this._touchMoved||f(a,"click"),e=!1)},b._mouseInit=function(){var b=this;b.element.bind({touchstart:a.proxy(b,"_touchStart"),touchmove:a.proxy(b,"_touchMove"),touchend:a.proxy(b,"_touchEnd")}),c.call(b)},b._mouseDestroy=function(){var b=this;b.element.unbind({touchstart:a.proxy(b,"_touchStart"),touchmove:a.proxy(b,"_touchMove"),touchend:a.proxy(b,"_touchEnd")}),d.call(b)}}}(jQuery);

/*
 *  Project: sakaiDateTimePicker
 *  Description: global date and time picker for Sakai
 *  Author: Phillip Ball
 */


(function ( $, window, undefined ) {

  // Create the defaults once
  var pluginName = 'sakaiDateTimePicker',
	document = window.document,
	defaults = {
		// populate an alternate field? If yes, selector.
		altField: false,
		// alternate field format
		altFormat: "yy-mm-dd",
		// use an icon?
		icon: 1,
		// minute increments
		stepMinute: 5,
		// use time
		useTime: false,
		// Slider options
		addSliderAccess: true,
		sliderAccessArgs:
			{ touchonly: false }
	},
	options = {};

  // The actual plugin constructor
  function Plugin(element, options) {
	this.element = element;

	this.options = $.extend({}, defaults, options);

	this._defaults = defaults;
	this._name = pluginName;

	// removes previous onClicks on the icon, if it exists
	jQuery(options.input).siblings('img').removeAttr('onClick').on('click', function() {
		jQuery(this).siblings(options.input).trigger('focus');
	});

	// if we have hidden fields we're setting, build them in the DOM
	// after our date input
	if(this.options.ashidden !== undefined) {
		var inp = this.options.input;
		jQuery.each(this.options.ashidden, function(i, h) {
			if ($('#' + h).length < 1) {
				jQuery(inp).after('<input type="hidden" name="' + h + '" id="' + h + '" value="">');
			}
		});
	}

	this.init();
  }

	Plugin.prototype.init = function() {
		var options = this.options,
			cfg = {};

		// This is the instance of the dateTimePicker
		var localDTPicker;

		cfg.showOn = (options.icon === 0) ? "focus" : "both";
		//Use an image instead of font-awesome
		//cfg.buttonImage = (options.icon === 0) ? null : "/library/image/silk/calendar.png";
		//cfg.buttonImageOnly = true;
		//Use font-awesome instead of an image
		cfg.buttonText = (options.icon === 0) ? null : "<span class='fa fa-calendar' aria-hidden='true'></span>";

		cfg.stepMinute = options.stepMinute;
		cfg.altField = options.altField;
		cfg.altFormat = options.altFormat;
		cfg.altFieldTimeOnly = false;
		cfg.showTimepicker = options.useTime;
		cfg.addSliderAccess = options.addSliderAccess;
		cfg.sliderAccessArgs = options.sliderAccessArgs;
		cfg.parseFormat = options.parseFormat;
		cfg.timeFormat = options.timeFormat;

		//Enable the dropdowns for month and year SAK-27733
		cfg.changeMonth = true;
		cfg.changeYear = true;

		// add blur event to allow edit input field
		cfg.beforeShow = function(input, inst) {
			$(input).unbind('blur');
			$(input).on('blur', function(){
				var momentDateFormat = $(this).datepicker("option","dateFormat").replace('yy','yyyy').toUpperCase();
				var momentTimeFormat = $(this).datepicker("option","timeFormat").replace('tt','a');
				var mc = moment($(input).val(),momentDateFormat+' '+momentTimeFormat);
				if (mc!=null && mc.isValid() && !mc.diff(moment($(this).datepicker("getDate")))) {
					setHiddenFields($(this).datepicker("getDate"), options, input);
				}
				var mh = moment(getHiddenFields());
				if (!mc.isValid() && options.allowEmptyDate){
					$(input).val("");
				}else{
					var stringDay = $.datepicker.formatDate($(this).datepicker("option","dateFormat"),mh.toDate());
					var stringTime = $.datepicker.formatTime($(this).datepicker("option","timeFormat"),{hour:mh.format("HH"),minute:mh.format("mm")});
					if(cfg.showTimepicker){
						$(input).val(stringDay + ' ' + stringTime);
					} else {
						$(input).val(stringDay);
					}
				}
			});
		};
		// on select, runs our custom method for setting dates
		cfg.onSelect = function(dtObj, dpInst) {
			setHiddenFields($(this).datepicker("getDate"), options, dpInst);
		};

		// When the picker allows empty dates, it should detect when the date is removed from the input, and update the hidden value.
		cfg.onClose = function(dtObj, dpInst) {
			if (dtObj == '' && options.allowEmptyDate){
				setHiddenFields($(this).datepicker("getDate"), options, dpInst);
			}
		};

		/**
		 * takes a date string and parses it using the moment.js library
		 * @param  {string} d date string
		 * @param  {string} f helper string used to parse the date by (if needed)
		 * @return {object}   JavaScript Date object
		 */
		var getDate = function(d, df, tf) {
			var parseDate;

			if (typeof d == 'string') {
				window.console && console.log("string date: " + d + ";parseFormat: " + cfg.parseFormat);
				if (d == "" && options.allowEmptyDate){
					parseDate="";
				}else{
					// formatList can be added to as needed. Refer to Moment Docs for reference
					// http://momentjs.com/docs/#/parsing/string-format/
					var formatList = [];

					if (typeof cfg.parseFormat !== 'undefined') {
						parseDate = new Date(moment(d, cfg.parseFormat));
					} else {
						parseDate = new Date(moment(d, cfg.parseFormat));
					};
				}

			} else {
				window.console && console.log("date object: " + d);
				parseDate = d;
			};

			return parseDate;
		}

		/**
		* Get the date, from hidden fields.
		* Handles many conditions do to the variety of tool implimentations 
		*/
		var getHiddenFields = function () {
			var o = options;
			var date = {month:'',day:'',year:'',hour:'',minute:'',ampm:''};
			// If we have hidden fields, set them.
			if(o.ashidden !== undefined) {
				jQuery.each(o.ashidden, function(i, h) {
					switch(i) {
						case "month":
						  date.month = jQuery('#' + h).val();
						  break;
						case "day":
						  date.day = jQuery('#' + h).val();
						  break;
						case "year":
						  date.year = jQuery('#' + h).val();
						  break;
						case "hour":
						  date.hour = jQuery('#' + h).val();
						  break;
						case "minute":
						  date.minute = jQuery('#' + h).val();
						  break;
						case "ampm":
						  date.ampm = (o.ampm)?jQuery('#' + h).val():'';
						  break;
						case "iso8601":
						  date.iso = jQuery('#' + h).val();
						  break;
					}
				});
			}
			return date.iso?date.iso:(date.year+'-'+toDigit(date.month)+'-'+toDigit(date.day)+' '+date.hour+':'+date.minute+' '+date.ampm).trim();
		}

		function toDigit(s) {
			return (s-0)<10?'0'+s:s;
		}
		
		/**
		* Sets the date, both on init and datetimepicker selection.
		* Handles many conditions do to the variety of tool implimentations 
		* 
		* @param {object} d JavaScript Date object
		*/
		var setHiddenFields = function (d) {
			var o = options;
			// If we have hidden fields, set them.
			if(o.ashidden !== undefined) {
				jQuery.each(o.ashidden, function(i, h) {
					var oldValue = jQuery('#' + h).val();
					var newValue = '';
					if(d != null){
						switch(i) {
							case "month":
							  newValue = d.getMonth() + 1;
							  break;
							case "day":
							  newValue = d.getDate();
							  break;
							case "year":
							  newValue = d.getFullYear();
							  break;
							case "hour":
							  newValue = (o.ampm == true) ? moment(d).format('hh') : moment(d).format('HH');
							  break;
							case "minute":
							  newValue = moment(d).format('mm');
							  break;
							case "ampm":
							  newValue = moment(d).format('A').toLowerCase();
							  break;
							case "iso8601":
							  newValue = moment(d).format();
							  break;
						}
					}
					jQuery('#' + h).val(newValue);
					// If new value is different from the previous one, launch change event on hidden input
					if (oldValue != newValue) {
						jQuery('#' + h).change();
					}
				});
			}
		}

		/**
		 * Initiallizes the base date for the picker.
		 * This gets the date that should be displayed to the user.
		 * 
		 */
		var initDateTime = function () {
			var initVal;

 			// If val is undefined, we assume we're to use the input value 
			if (typeof options.val === 'undefined' && $(options.input).val() !== '') {
				initVal = $(options.input).val();
			}

			// if val is set, use it
			if (typeof options.val !== 'undefined') {
				initVal = options.val;
			}

			// if getval is set, this will override val
			if (typeof options.getval !== 'undefined') {
				initVal = $(options.getval).val();
			}

			// finally trim the initVal and make sure it is set so we don't Dec 1969 the user
			initVal = jQuery.trim(initVal);

			if (!(initVal == "" && options.allowEmptyDate)){
				if (!initVal) initVal = new Date();
			}


			// set localDate to the time to use, predefined or current date/time
			localDate = getDate(initVal);
		};

		/**
		 * Parses the current time and adds the hours and minutes to derive the end time and date
		 * 
		 * @return {string} The end time as a formatted, language specific string
		 */
		var getEndDate = function () {
			var endDay = $.datepicker.formatDate(cfg.dateFormat ,
								 moment( localDTPicker.datetimepicker("getDate") )
								 .add('hours', $('#'+options.duration.hour).val())
								 .add('minutes', $('#'+options.duration.minute).val())
								 .toDate());

			var m = moment( localDTPicker.datetimepicker("getDate") );
			var hour = m.add('hours', $('#'+options.duration.hour).val()).format("HH");
			var minute = m.add('minutes', $('#'+options.duration.minute).val()).format("mm");

			var endTime = $.datepicker.formatTime('hh:mm tt', {hour:hour, minute:minute} );

			return endDay + ' ' + endTime;
		}

		/**
		 * handles the change events from soecified select menus for selecting hours and minutes,
		 * calculating and displaying and end-time within a specified element on the screen.
		 * 
		 * @method handleDuration
		 */
		var handleDuration = function () {
			if(options.duration !== undefined) {
				
				// initially sets the end-time-date based on duration dropdowns
				$('#'+options.duration.update).text(getEndDate());

				// listens for duration change, and re-applies end-date
				$('#'+options.duration.hour+', #'+options.duration.minute+', '+options.input).on('change',function(e){
					$('#'+options.duration.update).text(getEndDate());
				});
			}
		};

		/**
		 * Sets up the datetimepicker.
		 */
		var init = function() {

			// set the initial date for the picker
			initDateTime();

			// instantiate datetimepicker
			localDTPicker = $(options.input).css('min-width','200px').datetimepicker(cfg);

			if ((localDate != "") || (!(options.allowEmptyDate))){
				// set the datepicker date if we've got a pre-set value
				if (typeof options.val !== 'undefined' || typeof options.getval !== 'undefined' || typeof options.duration !== 'undefined') {
					localDTPicker.datetimepicker("setDate", localDate);
					// At this point we can go and ask the picker what it has rounded the time to.
					localDate = localDTPicker.datetimepicker("getDate");
					// Handle duration, which relies on the datepicker being set
					handleDuration();
				}

				// if the picker is reliant in hidden fields, set them
				setHiddenFields(localDate);
			}
		};

		/**
		 * Init the app by making a request for a language file.
		 * Failing the locale, we look for the base language
		 */
         var userLocale = 'en-US';
         var userLang = 'en';
         if(window.top.sakai){
             if(window.top.sakai.locale){
                 if(window.top.sakai.locale.userLocale){
                     userLocale = window.top.sakai.locale.userLocale.replace("_", "-");
                 }
                 if(window.top.sakai.locale.userLanguage){
                     userLang = window.top.sakai.locale.userLanguage;
                 }
             }
         }

         // First try the full locale (fr-CA) then try just the language (fr)
         if (typeof $.datepicker.regional[userLocale] !== "undefined") {
           $.datepicker.setDefaults($.datepicker.regional[userLocale]);
         } else if (typeof $.datepicker.regional[userLang] !== "undefined") {
           $.datepicker.setDefaults($.datepicker.regional[userLang]);
         } else {
           $.datepicker.setDefaults($.datepicker.regional[""]);
         }

         // Load the localization for the timepicker if the timepicker is being used
         if (typeof $.timepicker !== "undefined") {
           if (userLocale == "en-US") {
             // Special case for the USA and AM/PM
             $.timepicker.setDefaults({timeFormat: "hh:mm tt"});
           } else if (typeof $.timepicker.regional[userLocale] !== "undefined") {
             $.timepicker.setDefaults($.timepicker.regional[userLocale]); 
           } else if (typeof $.timepicker.regional[userLang] !== "undefined") {
             $.timepicker.setDefaults($.timepicker.regional[userLang]); 
           } 
           else {
             $.timepicker.setDefaults($.timepicker.regional[""]); 
           }
         }

         init();
	};

	// A really lightweight plugin wrapper around the constructor, 
	// preventing against multiple instantiations
	$.fn[pluginName] = function(options) {
		return this.each(function() {
			if(!$.data(this, 'plugin_' + pluginName)) {
				$.data(this, 'plugin_' + pluginName, new Plugin(this, options));
			}
		});
	}

}(jQuery, window));

// wrapper function for sakaidatepicker plugin. keeps the $ off the page
var localDatePicker = function(opts) {
	$(opts.input).sakaiDateTimePicker(opts);
}

