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

$.datepicker._getPreferredSakaiDatetime = function () {

  if (portal.serverTimeMillis && portal.user && portal.user.offsetFromServerMillis) {
    let osTzOffset = (new Date()).getTimezoneOffset();
    return moment(parseInt(portal.serverTimeMillis))
      .add(portal.user.offsetFromServerMillis, 'ms')
      .add(osTzOffset, 'm')
      .toDate();
  } else {
    window.console && console.debug("No user timezone or server time set. Using agent's time and timezone for initial datetime");
    return new Date();
  }
};

$.datepicker._gotoToday = function (id) {

  var target = $( id ), inst = this._getInst( target[ 0 ] );

  let date = $.datepicker._getPreferredSakaiDatetime();

  if ( this._get( inst, "gotoCurrent" ) && inst.currentDay ) {
    inst.selectedDay = inst.currentDay;
    inst.drawMonth = inst.selectedMonth = inst.currentMonth;
    inst.drawYear = inst.selectedYear = inst.currentYear;
  } else {
    inst.selectedDay = date.getDate();
    inst.drawMonth = inst.selectedMonth = date.getMonth();
    inst.drawYear = inst.selectedYear = date.getFullYear();
  }
  this._notifyChange( inst );
  this._adjustDate( target );

  var inst = this._getInst($(id)[0]);

  var tp_inst = this._get(inst, 'timepicker');
  if (!tp_inst) {
    return;
  }

  var tzoffset = $.timepicker.timezoneOffsetNumber(tp_inst.timezone);
  let now = $.datepicker._getPreferredSakaiDatetime();
  this._setTime(inst, now);
  this._setDate(inst, now);
  tp_inst._onSelectHandler();
};

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
				var mh = moment(getHiddenFields(), moment.ISO_8601);
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
      if (options.onDateTimeSelected) {
        options.onDateTimeSelected(moment($(this).datepicker("getDate")).valueOf());
      }
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
				window.console && console.debug("string date: " + d + ";parseFormat: " + cfg.parseFormat);
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
				window.console && console.debug("date object: " + d);
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
			return date.iso?date.iso:(date.year+'-'+toDigit(date.month)+'-'+toDigit(date.day)+'T'+date.hour+':'+date.minute+' '+date.ampm).trim();
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
        if (!initVal) {
          initVal = $.datepicker._getPreferredSakaiDatetime();
        }
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
	return $(opts.input).sakaiDateTimePicker(opts);
}

