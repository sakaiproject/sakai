/* 
* Sakai internationalized date/time picker
* https://jira.sakaiproject.org/browse/SAK-23662
* Built on top of jquery-ui and jQuery-Timepicker-Addon
* Plus custom integration code for Sakai
* Translations generated from jquery-ui Git checkout: cat ui/i18n/jquery.ui.datepicker-*|grep -v "\/\*"|grep -v jQuery|grep -v setDefaults|grep -v "^})"
* Please submit any translation issues to the upstream project: http://bugs.jqueryui.com/ or https://github.com/trentrichardson/jQuery-Timepicker-Addon
* Order of components in this file:
* * jquery-ui i18n translations
* * jquery-timepicker-addon minified code
* * jquery-timepicker-addon translations
* * moment.js (minified) to parse and validate dates (http://momentjs.com/)
* * jQuery UI Touch Punch 0.2.2 
* * Sakai integration code
*/


/*! jQuery UI - v1.10.3 - 2013-10-20
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
		monthNames: ['كانون الثاني', 'شباط', 'آذار', 'نيسان', 'مايو', 'حزيران',
		'تموز', 'آب', 'أيلول',	'تشرين الأول', 'تشرين الثاني', 'كانون الأول'],
		monthNamesShort: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
		dayNames: ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'],
		dayNamesShort: ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'],
		dayNamesMin: ['ح', 'ن', 'ث', 'ر', 'خ', 'ج', 'س'],
		weekHeader: 'أسبوع',
		dateFormat: 'dd/mm/yy',
		firstDay: 6,
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
		currentText: 'Τρέχων Μήνας',
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
		'jul','ogo','sep','oct','nov','dic'],
		dayNames: ['domingo','lunes','martes','miércoles','jueves','viernes','sábado'],
		dayNamesShort: ['dom','lun','mar','mié','juv','vie','sáb'],
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
			'فروردين',
			'ارديبهشت',
			'خرداد',
			'تير',
			'مرداد',
			'شهريور',
			'مهر',
			'آبان',
			'آذر',
			'دی',
			'بهمن',
			'اسفند'
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
		monthNamesShort: ['janv.', 'févr.', 'mars', 'avril', 'mai', 'juin',
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
		dateFormat: 'dd/mm/yy',
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

/*! jQuery Timepicker Addon - v1.4.1 - 2013-11-10
* http://trentrichardson.com/examples/timepicker
* Copyright (c) 2013 Trent Richardson; Licensed MIT */
(function($){if($.ui.timepicker=$.ui.timepicker||{},!$.ui.timepicker.version){$.extend($.ui,{timepicker:{version:"1.4.1"}});var Timepicker=function(){this.regional=[],this.regional[""]={currentText:"Now",closeText:"Done",amNames:["AM","A"],pmNames:["PM","P"],timeFormat:"HH:mm",timeSuffix:"",timeOnlyTitle:"Choose Time",timeText:"Time",hourText:"Hour",minuteText:"Minute",secondText:"Second",millisecText:"Millisecond",microsecText:"Microsecond",timezoneText:"Time Zone",isRTL:!1},this._defaults={showButtonPanel:!0,timeOnly:!1,showHour:null,showMinute:null,showSecond:null,showMillisec:null,showMicrosec:null,showTimezone:null,showTime:!0,stepHour:1,stepMinute:1,stepSecond:1,stepMillisec:1,stepMicrosec:1,hour:0,minute:0,second:0,millisec:0,microsec:0,timezone:null,hourMin:0,minuteMin:0,secondMin:0,millisecMin:0,microsecMin:0,hourMax:23,minuteMax:59,secondMax:59,millisecMax:999,microsecMax:999,minDateTime:null,maxDateTime:null,onSelect:null,hourGrid:0,minuteGrid:0,secondGrid:0,millisecGrid:0,microsecGrid:0,alwaysSetTime:!0,separator:" ",altFieldTimeOnly:!0,altTimeFormat:null,altSeparator:null,altTimeSuffix:null,pickerTimeFormat:null,pickerTimeSuffix:null,showTimepicker:!0,timezoneList:null,addSliderAccess:!1,sliderAccessArgs:null,controlType:"slider",defaultValue:null,parse:"strict"},$.extend(this._defaults,this.regional[""])};$.extend(Timepicker.prototype,{$input:null,$altInput:null,$timeObj:null,inst:null,hour_slider:null,minute_slider:null,second_slider:null,millisec_slider:null,microsec_slider:null,timezone_select:null,hour:0,minute:0,second:0,millisec:0,microsec:0,timezone:null,hourMinOriginal:null,minuteMinOriginal:null,secondMinOriginal:null,millisecMinOriginal:null,microsecMinOriginal:null,hourMaxOriginal:null,minuteMaxOriginal:null,secondMaxOriginal:null,millisecMaxOriginal:null,microsecMaxOriginal:null,ampm:"",formattedDate:"",formattedTime:"",formattedDateTime:"",timezoneList:null,units:["hour","minute","second","millisec","microsec"],support:{},control:null,setDefaults:function(e){return extendRemove(this._defaults,e||{}),this},_newInst:function($input,opts){var tp_inst=new Timepicker,inlineSettings={},fns={},overrides,i;for(var attrName in this._defaults)if(this._defaults.hasOwnProperty(attrName)){var attrValue=$input.attr("time:"+attrName);if(attrValue)try{inlineSettings[attrName]=eval(attrValue)}catch(err){inlineSettings[attrName]=attrValue}}overrides={beforeShow:function(e,t){return $.isFunction(tp_inst._defaults.evnts.beforeShow)?tp_inst._defaults.evnts.beforeShow.call($input[0],e,t,tp_inst):void 0},onChangeMonthYear:function(e,t,i){tp_inst._updateDateTime(i),$.isFunction(tp_inst._defaults.evnts.onChangeMonthYear)&&tp_inst._defaults.evnts.onChangeMonthYear.call($input[0],e,t,i,tp_inst)},onClose:function(e,t){tp_inst.timeDefined===!0&&""!==$input.val()&&tp_inst._updateDateTime(t),$.isFunction(tp_inst._defaults.evnts.onClose)&&tp_inst._defaults.evnts.onClose.call($input[0],e,t,tp_inst)}};for(i in overrides)overrides.hasOwnProperty(i)&&(fns[i]=opts[i]||null);tp_inst._defaults=$.extend({},this._defaults,inlineSettings,opts,overrides,{evnts:fns,timepicker:tp_inst}),tp_inst.amNames=$.map(tp_inst._defaults.amNames,function(e){return e.toUpperCase()}),tp_inst.pmNames=$.map(tp_inst._defaults.pmNames,function(e){return e.toUpperCase()}),tp_inst.support=detectSupport(tp_inst._defaults.timeFormat+(tp_inst._defaults.pickerTimeFormat?tp_inst._defaults.pickerTimeFormat:"")+(tp_inst._defaults.altTimeFormat?tp_inst._defaults.altTimeFormat:"")),"string"==typeof tp_inst._defaults.controlType?("slider"===tp_inst._defaults.controlType&&$.ui.slider===void 0&&(tp_inst._defaults.controlType="select"),tp_inst.control=tp_inst._controls[tp_inst._defaults.controlType]):tp_inst.control=tp_inst._defaults.controlType;var timezoneList=[-720,-660,-600,-570,-540,-480,-420,-360,-300,-270,-240,-210,-180,-120,-60,0,60,120,180,210,240,270,300,330,345,360,390,420,480,525,540,570,600,630,660,690,720,765,780,840];null!==tp_inst._defaults.timezoneList&&(timezoneList=tp_inst._defaults.timezoneList);var tzl=timezoneList.length,tzi=0,tzv=null;if(tzl>0&&"object"!=typeof timezoneList[0])for(;tzl>tzi;tzi++)tzv=timezoneList[tzi],timezoneList[tzi]={value:tzv,label:$.timepicker.timezoneOffsetString(tzv,tp_inst.support.iso8601)};return tp_inst._defaults.timezoneList=timezoneList,tp_inst.timezone=null!==tp_inst._defaults.timezone?$.timepicker.timezoneOffsetNumber(tp_inst._defaults.timezone):-1*(new Date).getTimezoneOffset(),tp_inst.hour=tp_inst._defaults.hour<tp_inst._defaults.hourMin?tp_inst._defaults.hourMin:tp_inst._defaults.hour>tp_inst._defaults.hourMax?tp_inst._defaults.hourMax:tp_inst._defaults.hour,tp_inst.minute=tp_inst._defaults.minute<tp_inst._defaults.minuteMin?tp_inst._defaults.minuteMin:tp_inst._defaults.minute>tp_inst._defaults.minuteMax?tp_inst._defaults.minuteMax:tp_inst._defaults.minute,tp_inst.second=tp_inst._defaults.second<tp_inst._defaults.secondMin?tp_inst._defaults.secondMin:tp_inst._defaults.second>tp_inst._defaults.secondMax?tp_inst._defaults.secondMax:tp_inst._defaults.second,tp_inst.millisec=tp_inst._defaults.millisec<tp_inst._defaults.millisecMin?tp_inst._defaults.millisecMin:tp_inst._defaults.millisec>tp_inst._defaults.millisecMax?tp_inst._defaults.millisecMax:tp_inst._defaults.millisec,tp_inst.microsec=tp_inst._defaults.microsec<tp_inst._defaults.microsecMin?tp_inst._defaults.microsecMin:tp_inst._defaults.microsec>tp_inst._defaults.microsecMax?tp_inst._defaults.microsecMax:tp_inst._defaults.microsec,tp_inst.ampm="",tp_inst.$input=$input,tp_inst._defaults.altField&&(tp_inst.$altInput=$(tp_inst._defaults.altField).css({cursor:"pointer"}).focus(function(){$input.trigger("focus")})),(0===tp_inst._defaults.minDate||0===tp_inst._defaults.minDateTime)&&(tp_inst._defaults.minDate=new Date),(0===tp_inst._defaults.maxDate||0===tp_inst._defaults.maxDateTime)&&(tp_inst._defaults.maxDate=new Date),void 0!==tp_inst._defaults.minDate&&tp_inst._defaults.minDate instanceof Date&&(tp_inst._defaults.minDateTime=new Date(tp_inst._defaults.minDate.getTime())),void 0!==tp_inst._defaults.minDateTime&&tp_inst._defaults.minDateTime instanceof Date&&(tp_inst._defaults.minDate=new Date(tp_inst._defaults.minDateTime.getTime())),void 0!==tp_inst._defaults.maxDate&&tp_inst._defaults.maxDate instanceof Date&&(tp_inst._defaults.maxDateTime=new Date(tp_inst._defaults.maxDate.getTime())),void 0!==tp_inst._defaults.maxDateTime&&tp_inst._defaults.maxDateTime instanceof Date&&(tp_inst._defaults.maxDate=new Date(tp_inst._defaults.maxDateTime.getTime())),tp_inst.$input.bind("focus",function(){tp_inst._onFocus()}),tp_inst},_addTimePicker:function(e){var t=this.$altInput&&this._defaults.altFieldTimeOnly?this.$input.val()+" "+this.$altInput.val():this.$input.val();this.timeDefined=this._parseTime(t),this._limitMinMaxDateTime(e,!1),this._injectTimePicker()},_parseTime:function(e,t){if(this.inst||(this.inst=$.datepicker._getInst(this.$input[0])),t||!this._defaults.timeOnly){var i=$.datepicker._get(this.inst,"dateFormat");try{var s=parseDateTimeInternal(i,this._defaults.timeFormat,e,$.datepicker._getFormatConfig(this.inst),this._defaults);if(!s.timeObj)return!1;$.extend(this,s.timeObj)}catch(a){return $.timepicker.log("Error parsing the date/time string: "+a+"\ndate/time string = "+e+"\ntimeFormat = "+this._defaults.timeFormat+"\ndateFormat = "+i),!1}return!0}var n=$.datepicker.parseTime(this._defaults.timeFormat,e,this._defaults);return n?($.extend(this,n),!0):!1},_injectTimePicker:function(){var e=this.inst.dpDiv,t=this.inst.settings,i=this,s="",a="",n=null,r={},l={},o=null,c=0,u=0;if(0===e.find("div.ui-timepicker-div").length&&t.showTimepicker){var m=' style="display:none;"',d='<div class="ui-timepicker-div'+(t.isRTL?" ui-timepicker-rtl":"")+'"><dl>'+'<dt class="ui_tpicker_time_label"'+(t.showTime?"":m)+">"+t.timeText+"</dt>"+'<dd class="ui_tpicker_time"'+(t.showTime?"":m)+"></dd>";for(c=0,u=this.units.length;u>c;c++){if(s=this.units[c],a=s.substr(0,1).toUpperCase()+s.substr(1),n=null!==t["show"+a]?t["show"+a]:this.support[s],r[s]=parseInt(t[s+"Max"]-(t[s+"Max"]-t[s+"Min"])%t["step"+a],10),l[s]=0,d+='<dt class="ui_tpicker_'+s+'_label"'+(n?"":m)+">"+t[s+"Text"]+"</dt>"+'<dd class="ui_tpicker_'+s+'"><div class="ui_tpicker_'+s+'_slider"'+(n?"":m)+"></div>",n&&t[s+"Grid"]>0){if(d+='<div style="padding-left: 1px"><table class="ui-tpicker-grid-label"><tr>',"hour"===s)for(var p=t[s+"Min"];r[s]>=p;p+=parseInt(t[s+"Grid"],10)){l[s]++;var h=$.datepicker.formatTime(this.support.ampm?"hht":"HH",{hour:p},t);d+='<td data-for="'+s+'">'+h+"</td>"}else for(var _=t[s+"Min"];r[s]>=_;_+=parseInt(t[s+"Grid"],10))l[s]++,d+='<td data-for="'+s+'">'+(10>_?"0":"")+_+"</td>";d+="</tr></table></div>"}d+="</dd>"}var f=null!==t.showTimezone?t.showTimezone:this.support.timezone;d+='<dt class="ui_tpicker_timezone_label"'+(f?"":m)+">"+t.timezoneText+"</dt>",d+='<dd class="ui_tpicker_timezone" '+(f?"":m)+"></dd>",d+="</dl></div>";var g=$(d);for(t.timeOnly===!0&&(g.prepend('<div class="ui-widget-header ui-helper-clearfix ui-corner-all"><div class="ui-datepicker-title">'+t.timeOnlyTitle+"</div>"+"</div>"),e.find(".ui-datepicker-header, .ui-datepicker-calendar").hide()),c=0,u=i.units.length;u>c;c++)s=i.units[c],a=s.substr(0,1).toUpperCase()+s.substr(1),n=null!==t["show"+a]?t["show"+a]:this.support[s],i[s+"_slider"]=i.control.create(i,g.find(".ui_tpicker_"+s+"_slider"),s,i[s],t[s+"Min"],r[s],t["step"+a]),n&&t[s+"Grid"]>0&&(o=100*l[s]*t[s+"Grid"]/(r[s]-t[s+"Min"]),g.find(".ui_tpicker_"+s+" table").css({width:o+"%",marginLeft:t.isRTL?"0":o/(-2*l[s])+"%",marginRight:t.isRTL?o/(-2*l[s])+"%":"0",borderCollapse:"collapse"}).find("td").click(function(){var e=$(this),t=e.html(),a=parseInt(t.replace(/[^0-9]/g),10),n=t.replace(/[^apm]/gi),r=e.data("for");"hour"===r&&(-1!==n.indexOf("p")&&12>a?a+=12:-1!==n.indexOf("a")&&12===a&&(a=0)),i.control.value(i,i[r+"_slider"],s,a),i._onTimeChange(),i._onSelectHandler()}).css({cursor:"pointer",width:100/l[s]+"%",textAlign:"center",overflow:"hidden"}));if(this.timezone_select=g.find(".ui_tpicker_timezone").append("<select></select>").find("select"),$.fn.append.apply(this.timezone_select,$.map(t.timezoneList,function(e){return $("<option />").val("object"==typeof e?e.value:e).text("object"==typeof e?e.label:e)})),this.timezone!==void 0&&null!==this.timezone&&""!==this.timezone){var M=-1*new Date(this.inst.selectedYear,this.inst.selectedMonth,this.inst.selectedDay,12).getTimezoneOffset();M===this.timezone?selectLocalTimezone(i):this.timezone_select.val(this.timezone)}else this.hour!==void 0&&null!==this.hour&&""!==this.hour?this.timezone_select.val(t.timezone):selectLocalTimezone(i);this.timezone_select.change(function(){i._onTimeChange(),i._onSelectHandler()});var v=e.find(".ui-datepicker-buttonpane");if(v.length?v.before(g):e.append(g),this.$timeObj=g.find(".ui_tpicker_time"),null!==this.inst){var k=this.timeDefined;this._onTimeChange(),this.timeDefined=k}if(this._defaults.addSliderAccess){var T=this._defaults.sliderAccessArgs,D=this._defaults.isRTL;T.isRTL=D,setTimeout(function(){if(0===g.find(".ui-slider-access").length){g.find(".ui-slider:visible").sliderAccess(T);var e=g.find(".ui-slider-access:eq(0)").outerWidth(!0);e&&g.find("table:visible").each(function(){var t=$(this),i=t.outerWidth(),s=(""+t.css(D?"marginRight":"marginLeft")).replace("%",""),a=i-e,n=s*a/i+"%",r={width:a,marginRight:0,marginLeft:0};r[D?"marginRight":"marginLeft"]=n,t.css(r)})}},10)}i._limitMinMaxDateTime(this.inst,!0)}},_limitMinMaxDateTime:function(e,t){var i=this._defaults,s=new Date(e.selectedYear,e.selectedMonth,e.selectedDay);if(this._defaults.showTimepicker){if(null!==$.datepicker._get(e,"minDateTime")&&void 0!==$.datepicker._get(e,"minDateTime")&&s){var a=$.datepicker._get(e,"minDateTime"),n=new Date(a.getFullYear(),a.getMonth(),a.getDate(),0,0,0,0);(null===this.hourMinOriginal||null===this.minuteMinOriginal||null===this.secondMinOriginal||null===this.millisecMinOriginal||null===this.microsecMinOriginal)&&(this.hourMinOriginal=i.hourMin,this.minuteMinOriginal=i.minuteMin,this.secondMinOriginal=i.secondMin,this.millisecMinOriginal=i.millisecMin,this.microsecMinOriginal=i.microsecMin),e.settings.timeOnly||n.getTime()===s.getTime()?(this._defaults.hourMin=a.getHours(),this.hour<=this._defaults.hourMin?(this.hour=this._defaults.hourMin,this._defaults.minuteMin=a.getMinutes(),this.minute<=this._defaults.minuteMin?(this.minute=this._defaults.minuteMin,this._defaults.secondMin=a.getSeconds(),this.second<=this._defaults.secondMin?(this.second=this._defaults.secondMin,this._defaults.millisecMin=a.getMilliseconds(),this.millisec<=this._defaults.millisecMin?(this.millisec=this._defaults.millisecMin,this._defaults.microsecMin=a.getMicroseconds()):(this.microsec<this._defaults.microsecMin&&(this.microsec=this._defaults.microsecMin),this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.secondMin=this.secondMinOriginal,this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.minuteMin=this.minuteMinOriginal,this._defaults.secondMin=this.secondMinOriginal,this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)):(this._defaults.hourMin=this.hourMinOriginal,this._defaults.minuteMin=this.minuteMinOriginal,this._defaults.secondMin=this.secondMinOriginal,this._defaults.millisecMin=this.millisecMinOriginal,this._defaults.microsecMin=this.microsecMinOriginal)}if(null!==$.datepicker._get(e,"maxDateTime")&&void 0!==$.datepicker._get(e,"maxDateTime")&&s){var r=$.datepicker._get(e,"maxDateTime"),l=new Date(r.getFullYear(),r.getMonth(),r.getDate(),0,0,0,0);(null===this.hourMaxOriginal||null===this.minuteMaxOriginal||null===this.secondMaxOriginal||null===this.millisecMaxOriginal)&&(this.hourMaxOriginal=i.hourMax,this.minuteMaxOriginal=i.minuteMax,this.secondMaxOriginal=i.secondMax,this.millisecMaxOriginal=i.millisecMax,this.microsecMaxOriginal=i.microsecMax),e.settings.timeOnly||l.getTime()===s.getTime()?(this._defaults.hourMax=r.getHours(),this.hour>=this._defaults.hourMax?(this.hour=this._defaults.hourMax,this._defaults.minuteMax=r.getMinutes(),this.minute>=this._defaults.minuteMax?(this.minute=this._defaults.minuteMax,this._defaults.secondMax=r.getSeconds(),this.second>=this._defaults.secondMax?(this.second=this._defaults.secondMax,this._defaults.millisecMax=r.getMilliseconds(),this.millisec>=this._defaults.millisecMax?(this.millisec=this._defaults.millisecMax,this._defaults.microsecMax=r.getMicroseconds()):(this.microsec>this._defaults.microsecMax&&(this.microsec=this._defaults.microsecMax),this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.secondMax=this.secondMaxOriginal,this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.minuteMax=this.minuteMaxOriginal,this._defaults.secondMax=this.secondMaxOriginal,this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)):(this._defaults.hourMax=this.hourMaxOriginal,this._defaults.minuteMax=this.minuteMaxOriginal,this._defaults.secondMax=this.secondMaxOriginal,this._defaults.millisecMax=this.millisecMaxOriginal,this._defaults.microsecMax=this.microsecMaxOriginal)}if(void 0!==t&&t===!0){var o=parseInt(this._defaults.hourMax-(this._defaults.hourMax-this._defaults.hourMin)%this._defaults.stepHour,10),c=parseInt(this._defaults.minuteMax-(this._defaults.minuteMax-this._defaults.minuteMin)%this._defaults.stepMinute,10),u=parseInt(this._defaults.secondMax-(this._defaults.secondMax-this._defaults.secondMin)%this._defaults.stepSecond,10),m=parseInt(this._defaults.millisecMax-(this._defaults.millisecMax-this._defaults.millisecMin)%this._defaults.stepMillisec,10),d=parseInt(this._defaults.microsecMax-(this._defaults.microsecMax-this._defaults.microsecMin)%this._defaults.stepMicrosec,10);this.hour_slider&&(this.control.options(this,this.hour_slider,"hour",{min:this._defaults.hourMin,max:o}),this.control.value(this,this.hour_slider,"hour",this.hour-this.hour%this._defaults.stepHour)),this.minute_slider&&(this.control.options(this,this.minute_slider,"minute",{min:this._defaults.minuteMin,max:c}),this.control.value(this,this.minute_slider,"minute",this.minute-this.minute%this._defaults.stepMinute)),this.second_slider&&(this.control.options(this,this.second_slider,"second",{min:this._defaults.secondMin,max:u}),this.control.value(this,this.second_slider,"second",this.second-this.second%this._defaults.stepSecond)),this.millisec_slider&&(this.control.options(this,this.millisec_slider,"millisec",{min:this._defaults.millisecMin,max:m}),this.control.value(this,this.millisec_slider,"millisec",this.millisec-this.millisec%this._defaults.stepMillisec)),this.microsec_slider&&(this.control.options(this,this.microsec_slider,"microsec",{min:this._defaults.microsecMin,max:d}),this.control.value(this,this.microsec_slider,"microsec",this.microsec-this.microsec%this._defaults.stepMicrosec))}}},_onTimeChange:function(){if(this._defaults.showTimepicker){var e=this.hour_slider?this.control.value(this,this.hour_slider,"hour"):!1,t=this.minute_slider?this.control.value(this,this.minute_slider,"minute"):!1,i=this.second_slider?this.control.value(this,this.second_slider,"second"):!1,s=this.millisec_slider?this.control.value(this,this.millisec_slider,"millisec"):!1,a=this.microsec_slider?this.control.value(this,this.microsec_slider,"microsec"):!1,n=this.timezone_select?this.timezone_select.val():!1,r=this._defaults,l=r.pickerTimeFormat||r.timeFormat,o=r.pickerTimeSuffix||r.timeSuffix;"object"==typeof e&&(e=!1),"object"==typeof t&&(t=!1),"object"==typeof i&&(i=!1),"object"==typeof s&&(s=!1),"object"==typeof a&&(a=!1),"object"==typeof n&&(n=!1),e!==!1&&(e=parseInt(e,10)),t!==!1&&(t=parseInt(t,10)),i!==!1&&(i=parseInt(i,10)),s!==!1&&(s=parseInt(s,10)),a!==!1&&(a=parseInt(a,10)),n!==!1&&(n=parseInt(n,10));var c=r[12>e?"amNames":"pmNames"][0],u=e!==this.hour||t!==this.minute||i!==this.second||s!==this.millisec||a!==this.microsec||this.ampm.length>0&&12>e!=(-1!==$.inArray(this.ampm.toUpperCase(),this.amNames))||null!==this.timezone&&n!==this.timezone;u&&(e!==!1&&(this.hour=e),t!==!1&&(this.minute=t),i!==!1&&(this.second=i),s!==!1&&(this.millisec=s),a!==!1&&(this.microsec=a),n!==!1&&(this.timezone=n),this.inst||(this.inst=$.datepicker._getInst(this.$input[0])),this._limitMinMaxDateTime(this.inst,!0)),this.support.ampm&&(this.ampm=c),this.formattedTime=$.datepicker.formatTime(r.timeFormat,this,r),this.$timeObj&&(l===r.timeFormat?this.$timeObj.text(this.formattedTime+o):this.$timeObj.text($.datepicker.formatTime(l,this,r)+o)),this.timeDefined=!0,u&&this._updateDateTime()}},_onSelectHandler:function(){var e=this._defaults.onSelect||this.inst.settings.onSelect,t=this.$input?this.$input[0]:null;e&&t&&e.apply(t,[this.formattedDateTime,this])},_updateDateTime:function(e){e=this.inst||e;var t=e.currentYear>0?new Date(e.currentYear,e.currentMonth,e.currentDay):new Date(e.selectedYear,e.selectedMonth,e.selectedDay),i=$.datepicker._daylightSavingAdjust(t),s=$.datepicker._get(e,"dateFormat"),a=$.datepicker._getFormatConfig(e),n=null!==i&&this.timeDefined;this.formattedDate=$.datepicker.formatDate(s,null===i?new Date:i,a);var r=this.formattedDate;if(""===e.lastVal&&(e.currentYear=e.selectedYear,e.currentMonth=e.selectedMonth,e.currentDay=e.selectedDay),this._defaults.timeOnly===!0?r=this.formattedTime:this._defaults.timeOnly!==!0&&(this._defaults.alwaysSetTime||n)&&(r+=this._defaults.separator+this.formattedTime+this._defaults.timeSuffix),this.formattedDateTime=r,this._defaults.showTimepicker)if(this.$altInput&&this._defaults.timeOnly===!1&&this._defaults.altFieldTimeOnly===!0)this.$altInput.val(this.formattedTime),this.$input.val(this.formattedDate);else if(this.$altInput){this.$input.val(r);var l="",o=this._defaults.altSeparator?this._defaults.altSeparator:this._defaults.separator,c=this._defaults.altTimeSuffix?this._defaults.altTimeSuffix:this._defaults.timeSuffix;this._defaults.timeOnly||(l=this._defaults.altFormat?$.datepicker.formatDate(this._defaults.altFormat,null===i?new Date:i,a):this.formattedDate,l&&(l+=o)),l+=this._defaults.altTimeFormat?$.datepicker.formatTime(this._defaults.altTimeFormat,this,this._defaults)+c:this.formattedTime+c,this.$altInput.val(l)}else this.$input.val(r);else this.$input.val(this.formattedDate);this.$input.trigger("change")},_onFocus:function(){if(!this.$input.val()&&this._defaults.defaultValue){this.$input.val(this._defaults.defaultValue);var e=$.datepicker._getInst(this.$input.get(0)),t=$.datepicker._get(e,"timepicker");if(t&&t._defaults.timeOnly&&e.input.val()!==e.lastVal)try{$.datepicker._updateDatepicker(e)}catch(i){$.timepicker.log(i)}}},_controls:{slider:{create:function(e,t,i,s,a,n,r){var l=e._defaults.isRTL;return t.prop("slide",null).slider({orientation:"horizontal",value:l?-1*s:s,min:l?-1*n:a,max:l?-1*a:n,step:r,slide:function(t,s){e.control.value(e,$(this),i,l?-1*s.value:s.value),e._onTimeChange()},stop:function(){e._onSelectHandler()}})},options:function(e,t,i,s,a){if(e._defaults.isRTL){if("string"==typeof s)return"min"===s||"max"===s?void 0!==a?t.slider(s,-1*a):Math.abs(t.slider(s)):t.slider(s);var n=s.min,r=s.max;return s.min=s.max=null,void 0!==n&&(s.max=-1*n),void 0!==r&&(s.min=-1*r),t.slider(s)}return"string"==typeof s&&void 0!==a?t.slider(s,a):t.slider(s)},value:function(e,t,i,s){return e._defaults.isRTL?void 0!==s?t.slider("value",-1*s):Math.abs(t.slider("value")):void 0!==s?t.slider("value",s):t.slider("value")}},select:{create:function(e,t,i,s,a,n,r){for(var l='<select class="ui-timepicker-select" data-unit="'+i+'" data-min="'+a+'" data-max="'+n+'" data-step="'+r+'">',o=e._defaults.pickerTimeFormat||e._defaults.timeFormat,c=a;n>=c;c+=r)l+='<option value="'+c+'"'+(c===s?" selected":"")+">",l+="hour"===i?$.datepicker.formatTime($.trim(o.replace(/[^ht ]/gi,"")),{hour:c},e._defaults):"millisec"===i||"microsec"===i||c>=10?c:"0"+(""+c),l+="</option>";return l+="</select>",t.children("select").remove(),$(l).appendTo(t).change(function(){e._onTimeChange(),e._onSelectHandler()}),t},options:function(e,t,i,s,a){var n={},r=t.children("select");if("string"==typeof s){if(void 0===a)return r.data(s);n[s]=a}else n=s;return e.control.create(e,t,r.data("unit"),r.val(),n.min||r.data("min"),n.max||r.data("max"),n.step||r.data("step"))},value:function(e,t,i,s){var a=t.children("select");return void 0!==s?a.val(s):a.val()}}}}),$.fn.extend({timepicker:function(e){e=e||{};var t=Array.prototype.slice.call(arguments);return"object"==typeof e&&(t[0]=$.extend(e,{timeOnly:!0})),$(this).each(function(){$.fn.datetimepicker.apply($(this),t)})},datetimepicker:function(e){e=e||{};var t=arguments;return"string"==typeof e?"getDate"===e?$.fn.datepicker.apply($(this[0]),t):this.each(function(){var e=$(this);e.datepicker.apply(e,t)}):this.each(function(){var t=$(this);t.datepicker($.timepicker._newInst(t,e)._defaults)})}}),$.datepicker.parseDateTime=function(e,t,i,s,a){var n=parseDateTimeInternal(e,t,i,s,a);if(n.timeObj){var r=n.timeObj;n.date.setHours(r.hour,r.minute,r.second,r.millisec),n.date.setMicroseconds(r.microsec)}return n.date},$.datepicker.parseTime=function(e,t,i){var s=extendRemove(extendRemove({},$.timepicker._defaults),i||{});-1!==e.replace(/\'.*?\'/g,"").indexOf("Z");var a=function(e,t,i){var s,a=function(e,t){var i=[];return e&&$.merge(i,e),t&&$.merge(i,t),i=$.map(i,function(e){return e.replace(/[.*+?|()\[\]{}\\]/g,"\\$&")}),"("+i.join("|")+")?"},n=function(e){var t=e.toLowerCase().match(/(h{1,2}|m{1,2}|s{1,2}|l{1}|c{1}|t{1,2}|z|'.*?')/g),i={h:-1,m:-1,s:-1,l:-1,c:-1,t:-1,z:-1};if(t)for(var s=0;t.length>s;s++)-1===i[(""+t[s]).charAt(0)]&&(i[(""+t[s]).charAt(0)]=s+1);return i},r="^"+(""+e).replace(/([hH]{1,2}|mm?|ss?|[tT]{1,2}|[zZ]|[lc]|'.*?')/g,function(e){var t=e.length;switch(e.charAt(0).toLowerCase()){case"h":return 1===t?"(\\d?\\d)":"(\\d{"+t+"})";case"m":return 1===t?"(\\d?\\d)":"(\\d{"+t+"})";case"s":return 1===t?"(\\d?\\d)":"(\\d{"+t+"})";case"l":return"(\\d?\\d?\\d)";case"c":return"(\\d?\\d?\\d)";case"z":return"(z|[-+]\\d\\d:?\\d\\d|\\S+)?";case"t":return a(i.amNames,i.pmNames);default:return"("+e.replace(/\'/g,"").replace(/(\.|\$|\^|\\|\/|\(|\)|\[|\]|\?|\+|\*)/g,function(e){return"\\"+e})+")?"}}).replace(/\s/g,"\\s?")+i.timeSuffix+"$",l=n(e),o="";s=t.match(RegExp(r,"i"));var c={hour:0,minute:0,second:0,millisec:0,microsec:0};return s?(-1!==l.t&&(void 0===s[l.t]||0===s[l.t].length?(o="",c.ampm=""):(o=-1!==$.inArray(s[l.t].toUpperCase(),i.amNames)?"AM":"PM",c.ampm=i["AM"===o?"amNames":"pmNames"][0])),-1!==l.h&&(c.hour="AM"===o&&"12"===s[l.h]?0:"PM"===o&&"12"!==s[l.h]?parseInt(s[l.h],10)+12:Number(s[l.h])),-1!==l.m&&(c.minute=Number(s[l.m])),-1!==l.s&&(c.second=Number(s[l.s])),-1!==l.l&&(c.millisec=Number(s[l.l])),-1!==l.c&&(c.microsec=Number(s[l.c])),-1!==l.z&&void 0!==s[l.z]&&(c.timezone=$.timepicker.timezoneOffsetNumber(s[l.z])),c):!1},n=function(e,t,i){try{var s=new Date("2012-01-01 "+t);if(isNaN(s.getTime())&&(s=new Date("2012-01-01T"+t),isNaN(s.getTime())&&(s=new Date("01/01/2012 "+t),isNaN(s.getTime()))))throw"Unable to parse time with native Date: "+t;return{hour:s.getHours(),minute:s.getMinutes(),second:s.getSeconds(),millisec:s.getMilliseconds(),microsec:s.getMicroseconds(),timezone:-1*s.getTimezoneOffset()}}catch(n){try{return a(e,t,i)}catch(r){$.timepicker.log("Unable to parse \ntimeString: "+t+"\ntimeFormat: "+e)}}return!1};return"function"==typeof s.parse?s.parse(e,t,s):"loose"===s.parse?n(e,t,s):a(e,t,s)},$.datepicker.formatTime=function(e,t,i){i=i||{},i=$.extend({},$.timepicker._defaults,i),t=$.extend({hour:0,minute:0,second:0,millisec:0,microsec:0,timezone:null},t);var s=e,a=i.amNames[0],n=parseInt(t.hour,10);return n>11&&(a=i.pmNames[0]),s=s.replace(/(?:HH?|hh?|mm?|ss?|[tT]{1,2}|[zZ]|[lc]|'.*?')/g,function(e){switch(e){case"HH":return("0"+n).slice(-2);case"H":return n;case"hh":return("0"+convert24to12(n)).slice(-2);case"h":return convert24to12(n);case"mm":return("0"+t.minute).slice(-2);case"m":return t.minute;case"ss":return("0"+t.second).slice(-2);case"s":return t.second;case"l":return("00"+t.millisec).slice(-3);case"c":return("00"+t.microsec).slice(-3);case"z":return $.timepicker.timezoneOffsetString(null===t.timezone?i.timezone:t.timezone,!1);case"Z":return $.timepicker.timezoneOffsetString(null===t.timezone?i.timezone:t.timezone,!0);case"T":return a.charAt(0).toUpperCase();case"TT":return a.toUpperCase();case"t":return a.charAt(0).toLowerCase();case"tt":return a.toLowerCase();default:return e.replace(/'/g,"")}})},$.datepicker._base_selectDate=$.datepicker._selectDate,$.datepicker._selectDate=function(e,t){var i=this._getInst($(e)[0]),s=this._get(i,"timepicker");s?(s._limitMinMaxDateTime(i,!0),i.inline=i.stay_open=!0,this._base_selectDate(e,t),i.inline=i.stay_open=!1,this._notifyChange(i),this._updateDatepicker(i)):this._base_selectDate(e,t)},$.datepicker._base_updateDatepicker=$.datepicker._updateDatepicker,$.datepicker._updateDatepicker=function(e){var t=e.input[0];if(!($.datepicker._curInst&&$.datepicker._curInst!==e&&$.datepicker._datepickerShowing&&$.datepicker._lastInput!==t||"boolean"==typeof e.stay_open&&e.stay_open!==!1)){this._base_updateDatepicker(e);var i=this._get(e,"timepicker");i&&i._addTimePicker(e)}},$.datepicker._base_doKeyPress=$.datepicker._doKeyPress,$.datepicker._doKeyPress=function(e){var t=$.datepicker._getInst(e.target),i=$.datepicker._get(t,"timepicker");if(i&&$.datepicker._get(t,"constrainInput")){var s=i.support.ampm,a=null!==i._defaults.showTimezone?i._defaults.showTimezone:i.support.timezone,n=$.datepicker._possibleChars($.datepicker._get(t,"dateFormat")),r=(""+i._defaults.timeFormat).replace(/[hms]/g,"").replace(/TT/g,s?"APM":"").replace(/Tt/g,s?"AaPpMm":"").replace(/tT/g,s?"AaPpMm":"").replace(/T/g,s?"AP":"").replace(/tt/g,s?"apm":"").replace(/t/g,s?"ap":"")+" "+i._defaults.separator+i._defaults.timeSuffix+(a?i._defaults.timezoneList.join(""):"")+i._defaults.amNames.join("")+i._defaults.pmNames.join("")+n,l=String.fromCharCode(void 0===e.charCode?e.keyCode:e.charCode);return e.ctrlKey||" ">l||!n||r.indexOf(l)>-1}return $.datepicker._base_doKeyPress(e)},$.datepicker._base_updateAlternate=$.datepicker._updateAlternate,$.datepicker._updateAlternate=function(e){var t=this._get(e,"timepicker");if(t){var i=t._defaults.altField;if(i){var s=(t._defaults.altFormat||t._defaults.dateFormat,this._getDate(e)),a=$.datepicker._getFormatConfig(e),n="",r=t._defaults.altSeparator?t._defaults.altSeparator:t._defaults.separator,l=t._defaults.altTimeSuffix?t._defaults.altTimeSuffix:t._defaults.timeSuffix,o=null!==t._defaults.altTimeFormat?t._defaults.altTimeFormat:t._defaults.timeFormat;n+=$.datepicker.formatTime(o,t,t._defaults)+l,t._defaults.timeOnly||t._defaults.altFieldTimeOnly||null===s||(n=t._defaults.altFormat?$.datepicker.formatDate(t._defaults.altFormat,s,a)+r+n:t.formattedDate+r+n),$(i).val(n)}}else $.datepicker._base_updateAlternate(e)},$.datepicker._base_doKeyUp=$.datepicker._doKeyUp,$.datepicker._doKeyUp=function(e){var t=$.datepicker._getInst(e.target),i=$.datepicker._get(t,"timepicker");if(i&&i._defaults.timeOnly&&t.input.val()!==t.lastVal)try{$.datepicker._updateDatepicker(t)}catch(s){$.timepicker.log(s)}return $.datepicker._base_doKeyUp(e)},$.datepicker._base_gotoToday=$.datepicker._gotoToday,$.datepicker._gotoToday=function(e){var t=this._getInst($(e)[0]),i=t.dpDiv;this._base_gotoToday(e);var s=this._get(t,"timepicker");selectLocalTimezone(s);var a=new Date;this._setTime(t,a),$(".ui-datepicker-today",i).click()},$.datepicker._disableTimepickerDatepicker=function(e){var t=this._getInst(e);if(t){var i=this._get(t,"timepicker");$(e).datepicker("getDate"),i&&(t.settings.showTimepicker=!1,i._defaults.showTimepicker=!1,i._updateDateTime(t))}},$.datepicker._enableTimepickerDatepicker=function(e){var t=this._getInst(e);if(t){var i=this._get(t,"timepicker");$(e).datepicker("getDate"),i&&(t.settings.showTimepicker=!0,i._defaults.showTimepicker=!0,i._addTimePicker(t),i._updateDateTime(t))}},$.datepicker._setTime=function(e,t){var i=this._get(e,"timepicker");if(i){var s=i._defaults;i.hour=t?t.getHours():s.hour,i.minute=t?t.getMinutes():s.minute,i.second=t?t.getSeconds():s.second,i.millisec=t?t.getMilliseconds():s.millisec,i.microsec=t?t.getMicroseconds():s.microsec,i._limitMinMaxDateTime(e,!0),i._onTimeChange(),i._updateDateTime(e)}},$.datepicker._setTimeDatepicker=function(e,t,i){var s=this._getInst(e);if(s){var a=this._get(s,"timepicker");if(a){this._setDateFromField(s);var n;t&&("string"==typeof t?(a._parseTime(t,i),n=new Date,n.setHours(a.hour,a.minute,a.second,a.millisec),n.setMicroseconds(a.microsec)):(n=new Date(t.getTime()),n.setMicroseconds(t.getMicroseconds())),"Invalid Date"==""+n&&(n=void 0),this._setTime(s,n))}}},$.datepicker._base_setDateDatepicker=$.datepicker._setDateDatepicker,$.datepicker._setDateDatepicker=function(e,t){var i=this._getInst(e);if(i){"string"==typeof t&&(t=new Date(t),t.getTime()||$.timepicker.log("Error creating Date object from string."));var s,a=this._get(i,"timepicker");t instanceof Date?(s=new Date(t.getTime()),s.setMicroseconds(t.getMicroseconds())):s=t,a&&(a.support.timezone||null!==a._defaults.timezone||(a.timezone=-1*s.getTimezoneOffset()),t=$.timepicker.timezoneAdjust(t,a.timezone),s=$.timepicker.timezoneAdjust(s,a.timezone)),this._updateDatepicker(i),this._base_setDateDatepicker.apply(this,arguments),this._setTimeDatepicker(e,s,!0)}},$.datepicker._base_getDateDatepicker=$.datepicker._getDateDatepicker,$.datepicker._getDateDatepicker=function(e,t){var i=this._getInst(e);if(i){var s=this._get(i,"timepicker");if(s){void 0===i.lastVal&&this._setDateFromField(i,t);var a=this._getDate(i);return a&&s._parseTime($(e).val(),s.timeOnly)&&(a.setHours(s.hour,s.minute,s.second,s.millisec),a.setMicroseconds(s.microsec),null!=s.timezone&&(s.support.timezone||null!==s._defaults.timezone||(s.timezone=-1*a.getTimezoneOffset()),a=$.timepicker.timezoneAdjust(a,s.timezone))),a
}return this._base_getDateDatepicker(e,t)}},$.datepicker._base_parseDate=$.datepicker.parseDate,$.datepicker.parseDate=function(e,t,i){var s;try{s=this._base_parseDate(e,t,i)}catch(a){if(!(a.indexOf(":")>=0))throw a;s=this._base_parseDate(e,t.substring(0,t.length-(a.length-a.indexOf(":")-2)),i),$.timepicker.log("Error parsing the date string: "+a+"\ndate string = "+t+"\ndate format = "+e)}return s},$.datepicker._base_formatDate=$.datepicker._formatDate,$.datepicker._formatDate=function(e){var t=this._get(e,"timepicker");return t?(t._updateDateTime(e),t.$input.val()):this._base_formatDate(e)},$.datepicker._base_optionDatepicker=$.datepicker._optionDatepicker,$.datepicker._optionDatepicker=function(e,t,i){var s,a=this._getInst(e);if(!a)return null;var n=this._get(a,"timepicker");if(n){var r,l=null,o=null,c=null,u=n._defaults.evnts,m={};if("string"==typeof t){if("minDate"===t||"minDateTime"===t)l=i;else if("maxDate"===t||"maxDateTime"===t)o=i;else if("onSelect"===t)c=i;else if(u.hasOwnProperty(t)){if(i===void 0)return u[t];m[t]=i,s={}}}else if("object"==typeof t){t.minDate?l=t.minDate:t.minDateTime?l=t.minDateTime:t.maxDate?o=t.maxDate:t.maxDateTime&&(o=t.maxDateTime);for(r in u)u.hasOwnProperty(r)&&t[r]&&(m[r]=t[r])}for(r in m)m.hasOwnProperty(r)&&(u[r]=m[r],s||(s=$.extend({},t)),delete s[r]);if(s&&isEmptyObject(s))return;l?(l=0===l?new Date:new Date(l),n._defaults.minDate=l,n._defaults.minDateTime=l):o?(o=0===o?new Date:new Date(o),n._defaults.maxDate=o,n._defaults.maxDateTime=o):c&&(n._defaults.onSelect=c)}return void 0===i?this._base_optionDatepicker.call($.datepicker,e,t):this._base_optionDatepicker.call($.datepicker,e,s||t,i)};var isEmptyObject=function(e){var t;for(t in e)if(e.hasOwnProperty(t))return!1;return!0},extendRemove=function(e,t){$.extend(e,t);for(var i in t)(null===t[i]||void 0===t[i])&&(e[i]=t[i]);return e},detectSupport=function(e){var t=e.replace(/'.*?'/g,"").toLowerCase(),i=function(e,t){return-1!==e.indexOf(t)?!0:!1};return{hour:i(t,"h"),minute:i(t,"m"),second:i(t,"s"),millisec:i(t,"l"),microsec:i(t,"c"),timezone:i(t,"z"),ampm:i(t,"t")&&i(e,"h"),iso8601:i(e,"Z")}},convert24to12=function(e){return e%=12,0===e&&(e=12),e+""},computeEffectiveSetting=function(e,t){return e&&e[t]?e[t]:$.timepicker._defaults[t]},splitDateTime=function(e,t){var i=computeEffectiveSetting(t,"separator"),s=computeEffectiveSetting(t,"timeFormat"),a=s.split(i),n=a.length,r=e.split(i),l=r.length;return l>1?{dateString:r.splice(0,l-n).join(i),timeString:r.splice(0,n).join(i)}:{dateString:e,timeString:""}},parseDateTimeInternal=function(e,t,i,s,a){var n,r,l;if(r=splitDateTime(i,a),n=$.datepicker._base_parseDate(e,r.dateString,s),""===r.timeString)return{date:n};if(l=$.datepicker.parseTime(t,r.timeString,a),!l)throw"Wrong time format";return{date:n,timeObj:l}},selectLocalTimezone=function(e,t){if(e&&e.timezone_select){var i=t||new Date;e.timezone_select.val(-i.getTimezoneOffset())}};$.timepicker=new Timepicker,$.timepicker.timezoneOffsetString=function(e,t){if(isNaN(e)||e>840||-720>e)return e;var i=e,s=i%60,a=(i-s)/60,n=t?":":"",r=(i>=0?"+":"-")+("0"+Math.abs(a)).slice(-2)+n+("0"+Math.abs(s)).slice(-2);return"+00:00"===r?"Z":r},$.timepicker.timezoneOffsetNumber=function(e){var t=(""+e).replace(":","");return"Z"===t.toUpperCase()?0:/^(\-|\+)\d{4}$/.test(t)?("-"===t.substr(0,1)?-1:1)*(60*parseInt(t.substr(1,2),10)+parseInt(t.substr(3,2),10)):e},$.timepicker.timezoneAdjust=function(e,t){var i=$.timepicker.timezoneOffsetNumber(t);return isNaN(i)||e.setMinutes(e.getMinutes()+-e.getTimezoneOffset()-i),e},$.timepicker.timeRange=function(e,t,i){return $.timepicker.handleRange("timepicker",e,t,i)},$.timepicker.datetimeRange=function(e,t,i){$.timepicker.handleRange("datetimepicker",e,t,i)},$.timepicker.dateRange=function(e,t,i){$.timepicker.handleRange("datepicker",e,t,i)},$.timepicker.handleRange=function(e,t,i,s){function a(a,n){var r=t[e]("getDate"),l=i[e]("getDate"),o=a[e]("getDate");if(null!==r){var c=new Date(r.getTime()),u=new Date(r.getTime());c.setMilliseconds(c.getMilliseconds()+s.minInterval),u.setMilliseconds(u.getMilliseconds()+s.maxInterval),s.minInterval>0&&c>l?i[e]("setDate",c):s.maxInterval>0&&l>u?i[e]("setDate",u):r>l&&n[e]("setDate",o)}}function n(t,i,a){if(t.val()){var n=t[e].call(t,"getDate");null!==n&&s.minInterval>0&&("minDate"===a&&n.setMilliseconds(n.getMilliseconds()+s.minInterval),"maxDate"===a&&n.setMilliseconds(n.getMilliseconds()-s.minInterval)),n.getTime&&i[e].call(i,"option",a,n)}}return s=$.extend({},{minInterval:0,maxInterval:0,start:{},end:{}},s),$.fn[e].call(t,$.extend({onClose:function(){a($(this),i)},onSelect:function(){n($(this),i,"minDate")}},s,s.start)),$.fn[e].call(i,$.extend({onClose:function(){a($(this),t)},onSelect:function(){n($(this),t,"maxDate")}},s,s.end)),a(t,i),n(t,i,"minDate"),n(i,t,"maxDate"),$([t.get(0),i.get(0)])},$.timepicker.log=function(e){window.console&&window.console.log(e)},$.timepicker._util={_extendRemove:extendRemove,_isEmptyObject:isEmptyObject,_convert24to12:convert24to12,_detectSupport:detectSupport,_selectLocalTimezone:selectLocalTimezone,_computeEffectiveSetting:computeEffectiveSetting,_splitDateTime:splitDateTime,_parseDateTimeInternal:parseDateTimeInternal},Date.prototype.getMicroseconds||(Date.prototype.microseconds=0,Date.prototype.getMicroseconds=function(){return this.microseconds},Date.prototype.setMicroseconds=function(e){return this.setMilliseconds(this.getMilliseconds()+Math.floor(e/1e3)),this.microseconds=e%1e3,this}),$.timepicker.version="1.4.1"}})(jQuery);

/* 
* Language list generated from Git source of timepicker addon
* https://github.com/trentrichardson/jQuery-Timepicker-Addon
* Any issues with the translation should be submitted as upstream issues or pull requests
*/
(function($) {
$.timepicker.regional['af']={
timeOnlyTitle:'KiesTyd',
timeText:'Tyd',
hourText:'Ure',
minuteText:'Minute',
secondText:'Sekondes',
millisecText:'Millisekondes',
microsecText:'Mikrosekondes',
timezoneText:'Tydsone',
currentText:'HuidigeTyd',
closeText:'Klaar',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['bg']={
timeOnlyTitle:'Изберетевреме',
timeText:'Време',
hourText:'Час',
minuteText:'Минути',
secondText:'Секунди',
millisecText:'Милисекунди',
microsecText:'Микросекунди',
timezoneText:'Часовипояс',
currentText:'Сега',
closeText:'Затвори',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['ca']={
timeOnlyTitle:'Escollirunahora',
timeText:'Hora',
hourText:'Hores',
minuteText:'Minuts',
secondText:'Segons',
millisecText:'Milisegons',
microsecText:'Microsegons',
timezoneText:'Fushorari',
currentText:'Ara',
closeText:'Tancar',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['cs']={
timeOnlyTitle:'Vybertečas',
timeText:'Čas',
hourText:'Hodiny',
minuteText:'Minuty',
secondText:'Vteřiny',
millisecText:'Milisekundy',
microsecText:'Mikrosekundy',
timezoneText:'Časovépásmo',
currentText:'Nyní',
closeText:'Zavřít',
timeFormat:'HH:mm',
amNames:['dop.','AM','A'],
pmNames:['odp.','PM','P'],
isRTL:false
};
$.timepicker.regional['da']={
timeOnlyTitle:'Vælgtid',
timeText:'Tid',
hourText:'Time',
minuteText:'Minut',
secondText:'Sekund',
millisecText:'Millisekund',
microsecText:'Mikrosekund',
timezoneText:'Tidszone',
currentText:'Nu',
closeText:'Luk',
timeFormat:'HH:mm',
amNames:['am','AM','A'],
pmNames:['pm','PM','P'],
isRTL:false
};
$.timepicker.regional['de']={
timeOnlyTitle:'ZeitWählen',
timeText:'Zeit',
hourText:'Stunde',
minuteText:'Minute',
secondText:'Sekunde',
millisecText:'Millisekunde',
microsecText:'Mikrosekunde',
timezoneText:'Zeitzone',
currentText:'Jetzt',
closeText:'Fertig',
timeFormat:'HH:mm',
amNames:['vorm.','AM','A'],
pmNames:['nachm.','PM','P'],
isRTL:false
};
$.timepicker.regional['el']={
timeOnlyTitle:'Επιλογήώρας',
timeText:'Ώρα',
hourText:'Ώρες',
minuteText:'Λεπτά',
secondText:'Δευτερόλεπτα',
millisecText:'μιλιδευτερόλεπτο',
microsecText:'Microseconds',
timezoneText:'Ζώνηώρας',
currentText:'Τώρα',
closeText:'Κλείσιμο',
timeFormat:'HH:mm',
amNames:['π.μ.','AM','A'],
pmNames:['μ.μ.','PM','P'],
isRTL:false
};
$.timepicker.regional['es']={
timeOnlyTitle:'Elegirunahora',
timeText:'Hora',
hourText:'Horas',
minuteText:'Minutos',
secondText:'Segundos',
millisecText:'Milisegundos',
microsecText:'Microsegundos',
timezoneText:'Husohorario',
currentText:'Ahora',
closeText:'Cerrar',
timeFormat:'HH:mm',
amNames:['a.m.','AM','A'],
pmNames:['p.m.','PM','P'],
isRTL:false
};
$.timepicker.regional['et']={
timeOnlyTitle:'Valiaeg',
timeText:'Aeg',
hourText:'Tund',
minuteText:'Minut',
secondText:'Sekund',
millisecText:'Millisekundis',
microsecText:'Mikrosekundis',
timezoneText:'Ajavöönd',
currentText:'Praegu',
closeText:'Valmis',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['eu']={
timeOnlyTitle:'Aukeratuordua',
timeText:'Ordua',
hourText:'Orduak',
minuteText:'Minutuak',
secondText:'Segunduak',
millisecText:'Milisegunduak',
microsecText:'Mikrosegundotan',
timezoneText:'Ordu-eremua',
currentText:'Orain',
closeText:'Itxi',
timeFormat:'HH:mm',
amNames:['a.m.','AM','A'],
pmNames:['p.m.','PM','P'],
isRTL:false
};
$.timepicker.regional['fi']={
timeOnlyTitle:'Valitseaika',
timeText:'Aika',
hourText:'Tunti',
minuteText:'Minuutti',
secondText:'Sekunti',
millisecText:'Millisekunnin',
microsecText:'Mikrosekuntia',
timezoneText:'Aikavyöhyke',
currentText:'Nyt',
closeText:'Sulje',
timeFormat:'HH:mm',
amNames:['ap.','AM','A'],
pmNames:['ip.','PM','P'],
isRTL:false
};
$.timepicker.regional['fr']={
timeOnlyTitle:'Choisiruneheure',
timeText:'Heure',
hourText:'Heures',
minuteText:'Minutes',
secondText:'Secondes',
millisecText:'Millisecondes',
microsecText:'Microsecondes',
timezoneText:'Fuseauhoraire',
currentText:'Maintenant',
closeText:'Terminé',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['gl']={
timeOnlyTitle:'Elixirunhahora',
timeText:'Hora',
hourText:'Horas',
minuteText:'Minutos',
secondText:'Segundos',
millisecText:'Milisegundos',
microsecText:'Microssegundos',
timezoneText:'Fusohorario',
currentText:'Agora',
closeText:'Pechar',
timeFormat:'HH:mm',
amNames:['a.m.','AM','A'],
pmNames:['p.m.','PM','P'],
isRTL:false
};
$.timepicker.regional["he"]={
timeOnlyTitle:"בחירתזמן",
timeText:"שעה",
hourText:"שעות",
minuteText:"דקות",
secondText:"שניות",
millisecText:"אלפיתהשנייה",
microsecText:"מיקרו",
timezoneText:"אזורזמן",
currentText:"עכשיו",
closeText:"סגור",
timeFormat:"HH:mm",
amNames:['לפנה"צ','AM','A'],
pmNames:['אחה"צ','PM','P'],
isRTL:true
};
$.timepicker.regional['hr']={
timeOnlyTitle:'Odaberivrijeme',
timeText:'Vrijeme',
hourText:'Sati',
minuteText:'Minute',
secondText:'Sekunde',
millisecText:'Milisekunde',
microsecText:'Mikrosekunde',
timezoneText:'Vremenskazona',
currentText:'Sada',
closeText:'Gotovo',
timeFormat:'HH:mm',
amNames:['a.m.','AM','A'],
pmNames:['p.m.','PM','P'],
isRTL:false
};
$.timepicker.regional['hu']={
timeOnlyTitle:'Válasszonidőpontot',
timeText:'Idő',
hourText:'Óra',
minuteText:'Perc',
secondText:'Másodperc',
millisecText:'Milliszekundumos',
microsecText:'Ezredmásodperc',
timezoneText:'Időzóna',
currentText:'Most',
closeText:'Kész',
timeFormat:'HH:mm',
amNames:['de.','AM','A'],
pmNames:['du.','PM','P'],
isRTL:false
};
$.timepicker.regional['id']={
timeOnlyTitle:'PilihWaktu',
timeText:'Waktu',
hourText:'Pukul',
minuteText:'Menit',
secondText:'Detik',
millisecText:'Milidetik',
microsecText:'Mikrodetik',
timezoneText:'ZonaWaktu',
currentText:'Sekarang',
closeText:'OK',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['it']={
timeOnlyTitle:'Scegliorario',
timeText:'Orario',
hourText:'Ora',
minuteText:'Minuti',
secondText:'Secondi',
millisecText:'Millisecondi',
microsecText:'Microsecondi',
timezoneText:'Fusoorario',
currentText:'Adesso',
closeText:'Chiudi',
timeFormat:'HH:mm',
amNames:['m.','AM','A'],
pmNames:['p.','PM','P'],
isRTL:false
};
$.timepicker.regional['ja']={
timeOnlyTitle:'時間を選択',
timeText:'時間',
hourText:'時',
minuteText:'分',
secondText:'秒',
millisecText:'ミリ秒',
microsecText:'マイクロ秒',
timezoneText:'タイムゾーン',
currentText:'現時刻',
closeText:'閉じる',
timeFormat:'HH:mm',
amNames:['午前','AM','A'],
pmNames:['午後','PM','P'],
isRTL:false
};
$.timepicker.regional['ko']={
timeOnlyTitle:'시간선택',
timeText:'시간',
hourText:'시',
minuteText:'분',
secondText:'초',
millisecText:'밀리초',
microsecText:'마이크로',
timezoneText:'표준시간대',
currentText:'현재시각',
closeText:'닫기',
timeFormat:'tth:mm',
amNames:['오전','AM','A'],
pmNames:['오후','PM','P'],
isRTL:false
};
$.timepicker.regional['lt']={
timeOnlyTitle:'Pasirinkitelaiką',
timeText:'Laikas',
hourText:'Valandos',
minuteText:'Minutės',
secondText:'Sekundės',
millisecText:'Milisekundės',
microsecText:'Mikrosekundės',
timezoneText:'Laikozona',
currentText:'Dabar',
closeText:'Uždaryti',
timeFormat:'HH:mm',
amNames:['priešpiet','AM','A'],
pmNames:['popiet','PM','P'],
isRTL:false
};
$.timepicker.regional['nl']={
timeOnlyTitle:'Tijdstip',
timeText:'Tijd',
hourText:'Uur',
minuteText:'Minuut',
secondText:'Seconde',
millisecText:'Milliseconde',
microsecText:'Microseconde',
timezoneText:'Tijdzone',
currentText:'Vandaag',
closeText:'Sluiten',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['no']={
timeOnlyTitle:'Velgtid',
timeText:'Tid',
hourText:'Time',
minuteText:'Minutt',
secondText:'Sekund',
millisecText:'Millisekund',
microsecText:'mikrosekund',
timezoneText:'Tidssone',
currentText:'Nå',
closeText:'Lukk',
timeFormat:'HH:mm',
amNames:['am','AM','A'],
pmNames:['pm','PM','P'],
isRTL:false
};
$.timepicker.regional['pl']={
timeOnlyTitle:'Wybierzgodzinę',
timeText:'Czas',
hourText:'Godzina',
minuteText:'Minuta',
secondText:'Sekunda',
millisecText:'Milisekunda',
microsecText:'Mikrosekunda',
timezoneText:'Strefaczasowa',
currentText:'Teraz',
closeText:'Gotowe',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['pt-BR']={
timeOnlyTitle:'Escolhaohorário',
timeText:'Horário',
hourText:'Hora',
minuteText:'Minutos',
secondText:'Segundos',
millisecText:'Milissegundos',
microsecText:'Microssegundos',
timezoneText:'Fusohorário',
currentText:'Agora',
closeText:'Fechar',
timeFormat:'HH:mm',
amNames:['a.m.','AM','A'],
pmNames:['p.m.','PM','P'],
isRTL:false
};
$.timepicker.regional['pt']={
timeOnlyTitle:'Escolhaumahora',
timeText:'Hora',
hourText:'Horas',
minuteText:'Minutos',
secondText:'Segundos',
millisecText:'Milissegundos',
microsecText:'Microssegundos',
timezoneText:'Fusohorário',
currentText:'Agora',
closeText:'Fechar',
timeFormat:'HH:mm',
amNames:['a.m.','AM','A'],
pmNames:['p.m.','PM','P'],
isRTL:false
};
$.timepicker.regional['ro']={
timeOnlyTitle:'Alegeţiooră',
timeText:'Timp',
hourText:'Ore',
minuteText:'Minute',
secondText:'Secunde',
millisecText:'Milisecunde',
microsecText:'Microsecunde',
timezoneText:'Fusorar',
currentText:'Acum',
closeText:'Închide',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['ru']={
timeOnlyTitle:'Выберитевремя',
timeText:'Время',
hourText:'Часы',
minuteText:'Минуты',
secondText:'Секунды',
millisecText:'Миллисекунды',
microsecText:'Микросекунды',
timezoneText:'Часовойпояс',
currentText:'Сейчас',
closeText:'Закрыть',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['sk']={
timeOnlyTitle:'Zvoľtečas',
timeText:'Čas',
hourText:'Hodiny',
minuteText:'Minúty',
secondText:'Sekundy',
millisecText:'Milisekundy',
microsecText:'Mikrosekundy',
timezoneText:'Časovépásmo',
currentText:'Teraz',
closeText:'Zavrieť',
timeFormat:'H:m',
amNames:['dop.','AM','A'],
pmNames:['pop.','PM','P'],
isRTL:false
};
$.timepicker.regional['sr-RS']={
timeOnlyTitle:'Одаберитевреме',
timeText:'Време',
hourText:'Сати',
minuteText:'Минути',
secondText:'Секунде',
millisecText:'Милисекунде',
microsecText:'Микросекунде',
timezoneText:'Временсказона',
currentText:'Сада',
closeText:'Затвори',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['sr-YU']={
timeOnlyTitle:'Odaberitevreme',
timeText:'Vreme',
hourText:'Sati',
minuteText:'Minuti',
secondText:'Sekunde',
millisecText:'Milisekunde',
microsecText:'Mikrosekunde',
timezoneText:'Vremenskazona',
currentText:'Sada',
closeText:'Zatvori',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['sv']={
timeOnlyTitle:'Väljentid',
timeText:'Tid',
hourText:'Timme',
minuteText:'Minut',
secondText:'Sekund',
millisecText:'Millisekund',
microsecText:'Mikrosekund',
timezoneText:'Tidszon',
currentText:'Nu',
closeText:'Stäng',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['th']={
timeOnlyTitle:'เลือกเวลา',
timeText:'เวลา',
hourText:'ชั่วโมง',
minuteText:'นาที',
secondText:'วินาที',
millisecText:'มิลลิวินาที',
microsecText:'ไมโคริวินาที',
timezoneText:'เขตเวลา',
currentText:'เวลาปัจจุบัน',
closeText:'ปิด',
timeFormat:'hh:mmtt'
};
$.timepicker.regional['tr']={
timeOnlyTitle:'ZamanSeçiniz',
timeText:'Zaman',
hourText:'Saat',
minuteText:'Dakika',
secondText:'Saniye',
millisecText:'Milisaniye',
microsecText:'Mikrosaniye',
timezoneText:'ZamanDilimi',
currentText:'Şuan',
closeText:'Tamam',
timeFormat:'HH:mm',
amNames:['ÖÖ','Ö'],
pmNames:['ÖS','S'],
isRTL:false
};
$.timepicker.regional['uk']={
timeOnlyTitle:'Виберітьчас',
timeText:'Час',
hourText:'Години',
minuteText:'Хвилини',
secondText:'Секунди',
millisecText:'Мілісекунди',
microsecText:'Мікросекунди',
timezoneText:'Часовийпояс',
currentText:'Зараз',
closeText:'Закрити',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['vi']={
timeOnlyTitle:'Chọngiờ',
timeText:'Thờigian',
hourText:'Giờ',
minuteText:'Phút',
secondText:'Giây',
millisecText:'Phầnnghìngiây',
microsecText:'Miligiây',
timezoneText:'Múigiờ',
currentText:'Hiệnthời',
closeText:'Đóng',
timeFormat:'H:m',
amNames:['SA','AM','A'],
pmNames:['CH','PM','P'],
isRTL:false
};
$.timepicker.regional['zh-CN']={
timeOnlyTitle:'选择时间',
timeText:'时间',
hourText:'小时',
minuteText:'分钟',
secondText:'秒钟',
millisecText:'微秒',
microsecText:'微秒',
timezoneText:'时区',
currentText:'现在时间',
closeText:'关闭',
timeFormat:'HH:mm',
amNames:['AM','A'],
pmNames:['PM','P'],
isRTL:false
};
$.timepicker.regional['zh-TW']={
timeOnlyTitle:'選擇時分秒',
timeText:'時間',
hourText:'時',
minuteText:'分',
secondText:'秒',
millisecText:'毫秒',
microsecText:'微秒',
timezoneText:'時區',
currentText:'現在時間',
closeText:'確定',
timeFormat:'HH:mm',
amNames:['上午','AM','A'],
pmNames:['下午','PM','P'],
isRTL:false
};
})(jQuery);

// moment.js
// version : 2.1.0
// author : Tim Wood
// license : MIT
// momentjs.com
!function(t){function e(t,e){return function(n){return u(t.call(this,n),e)}}function n(t,e){return function(n){return this.lang().ordinal(t.call(this,n),e)}}function s(){}function i(t){a(this,t)}function r(t){var e=t.years||t.year||t.y||0,n=t.months||t.month||t.M||0,s=t.weeks||t.week||t.w||0,i=t.days||t.day||t.d||0,r=t.hours||t.hour||t.h||0,a=t.minutes||t.minute||t.m||0,o=t.seconds||t.second||t.s||0,u=t.milliseconds||t.millisecond||t.ms||0;this._input=t,this._milliseconds=u+1e3*o+6e4*a+36e5*r,this._days=i+7*s,this._months=n+12*e,this._data={},this._bubble()}function a(t,e){for(var n in e)e.hasOwnProperty(n)&&(t[n]=e[n]);return t}function o(t){return 0>t?Math.ceil(t):Math.floor(t)}function u(t,e){for(var n=t+"";n.length<e;)n="0"+n;return n}function h(t,e,n,s){var i,r,a=e._milliseconds,o=e._days,u=e._months;a&&t._d.setTime(+t._d+a*n),(o||u)&&(i=t.minute(),r=t.hour()),o&&t.date(t.date()+o*n),u&&t.month(t.month()+u*n),a&&!s&&H.updateOffset(t),(o||u)&&(t.minute(i),t.hour(r))}function d(t){return"[object Array]"===Object.prototype.toString.call(t)}function c(t,e){var n,s=Math.min(t.length,e.length),i=Math.abs(t.length-e.length),r=0;for(n=0;s>n;n++)~~t[n]!==~~e[n]&&r++;return r+i}function f(t){return t?ie[t]||t.toLowerCase().replace(/(.)s$/,"$1"):t}function l(t,e){return e.abbr=t,x[t]||(x[t]=new s),x[t].set(e),x[t]}function _(t){if(!t)return H.fn._lang;if(!x[t]&&A)try{require("./lang/"+t)}catch(e){return H.fn._lang}return x[t]}function m(t){return t.match(/\[.*\]/)?t.replace(/^\[|\]$/g,""):t.replace(/\\/g,"")}function y(t){var e,n,s=t.match(E);for(e=0,n=s.length;n>e;e++)s[e]=ue[s[e]]?ue[s[e]]:m(s[e]);return function(i){var r="";for(e=0;n>e;e++)r+=s[e]instanceof Function?s[e].call(i,t):s[e];return r}}function M(t,e){function n(e){return t.lang().longDateFormat(e)||e}for(var s=5;s--&&N.test(e);)e=e.replace(N,n);return re[e]||(re[e]=y(e)),re[e](t)}function g(t,e){switch(t){case"DDDD":return V;case"YYYY":return X;case"YYYYY":return $;case"S":case"SS":case"SSS":case"DDD":return I;case"MMM":case"MMMM":case"dd":case"ddd":case"dddd":return R;case"a":case"A":return _(e._l)._meridiemParse;case"X":return B;case"Z":case"ZZ":return j;case"T":return q;case"MM":case"DD":case"YY":case"HH":case"hh":case"mm":case"ss":case"M":case"D":case"d":case"H":case"h":case"m":case"s":return J;default:return new RegExp(t.replace("\\",""))}}function p(t){var e=(j.exec(t)||[])[0],n=(e+"").match(ee)||["-",0,0],s=+(60*n[1])+~~n[2];return"+"===n[0]?-s:s}function D(t,e,n){var s,i=n._a;switch(t){case"M":case"MM":i[1]=null==e?0:~~e-1;break;case"MMM":case"MMMM":s=_(n._l).monthsParse(e),null!=s?i[1]=s:n._isValid=!1;break;case"D":case"DD":case"DDD":case"DDDD":null!=e&&(i[2]=~~e);break;case"YY":i[0]=~~e+(~~e>68?1900:2e3);break;case"YYYY":case"YYYYY":i[0]=~~e;break;case"a":case"A":n._isPm=_(n._l).isPM(e);break;case"H":case"HH":case"h":case"hh":i[3]=~~e;break;case"m":case"mm":i[4]=~~e;break;case"s":case"ss":i[5]=~~e;break;case"S":case"SS":case"SSS":i[6]=~~(1e3*("0."+e));break;case"X":n._d=new Date(1e3*parseFloat(e));break;case"Z":case"ZZ":n._useUTC=!0,n._tzm=p(e)}null==e&&(n._isValid=!1)}function Y(t){var e,n,s=[];if(!t._d){for(e=0;7>e;e++)t._a[e]=s[e]=null==t._a[e]?2===e?1:0:t._a[e];s[3]+=~~((t._tzm||0)/60),s[4]+=~~((t._tzm||0)%60),n=new Date(0),t._useUTC?(n.setUTCFullYear(s[0],s[1],s[2]),n.setUTCHours(s[3],s[4],s[5],s[6])):(n.setFullYear(s[0],s[1],s[2]),n.setHours(s[3],s[4],s[5],s[6])),t._d=n}}function w(t){var e,n,s=t._f.match(E),i=t._i;for(t._a=[],e=0;e<s.length;e++)n=(g(s[e],t).exec(i)||[])[0],n&&(i=i.slice(i.indexOf(n)+n.length)),ue[s[e]]&&D(s[e],n,t);i&&(t._il=i),t._isPm&&t._a[3]<12&&(t._a[3]+=12),t._isPm===!1&&12===t._a[3]&&(t._a[3]=0),Y(t)}function k(t){var e,n,s,r,o,u=99;for(r=0;r<t._f.length;r++)e=a({},t),e._f=t._f[r],w(e),n=new i(e),o=c(e._a,n.toArray()),n._il&&(o+=n._il.length),u>o&&(u=o,s=n);a(t,s)}function v(t){var e,n=t._i,s=K.exec(n);if(s){for(t._f="YYYY-MM-DD"+(s[2]||" "),e=0;4>e;e++)if(te[e][1].exec(n)){t._f+=te[e][0];break}j.exec(n)&&(t._f+=" Z"),w(t)}else t._d=new Date(n)}function T(e){var n=e._i,s=G.exec(n);n===t?e._d=new Date:s?e._d=new Date(+s[1]):"string"==typeof n?v(e):d(n)?(e._a=n.slice(0),Y(e)):e._d=n instanceof Date?new Date(+n):new Date(n)}function b(t,e,n,s,i){return i.relativeTime(e||1,!!n,t,s)}function S(t,e,n){var s=W(Math.abs(t)/1e3),i=W(s/60),r=W(i/60),a=W(r/24),o=W(a/365),u=45>s&&["s",s]||1===i&&["m"]||45>i&&["mm",i]||1===r&&["h"]||22>r&&["hh",r]||1===a&&["d"]||25>=a&&["dd",a]||45>=a&&["M"]||345>a&&["MM",W(a/30)]||1===o&&["y"]||["yy",o];return u[2]=e,u[3]=t>0,u[4]=n,b.apply({},u)}function F(t,e,n){var s,i=n-e,r=n-t.day();return r>i&&(r-=7),i-7>r&&(r+=7),s=H(t).add("d",r),{week:Math.ceil(s.dayOfYear()/7),year:s.year()}}function O(t){var e=t._i,n=t._f;return null===e||""===e?null:("string"==typeof e&&(t._i=e=_().preparse(e)),H.isMoment(e)?(t=a({},e),t._d=new Date(+e._d)):n?d(n)?k(t):w(t):T(t),new i(t))}function z(t,e){H.fn[t]=H.fn[t+"s"]=function(t){var n=this._isUTC?"UTC":"";return null!=t?(this._d["set"+n+e](t),H.updateOffset(this),this):this._d["get"+n+e]()}}function C(t){H.duration.fn[t]=function(){return this._data[t]}}function L(t,e){H.duration.fn["as"+t]=function(){return+this/e}}for(var H,P,U="2.1.0",W=Math.round,x={},A="undefined"!=typeof module&&module.exports,G=/^\/?Date\((\-?\d+)/i,Z=/(\-)?(\d*)?\.?(\d+)\:(\d+)\:(\d+)\.?(\d{3})?/,E=/(\[[^\[]*\])|(\\)?(Mo|MM?M?M?|Do|DDDo|DD?D?D?|ddd?d?|do?|w[o|w]?|W[o|W]?|YYYYY|YYYY|YY|gg(ggg?)?|GG(GGG?)?|e|E|a|A|hh?|HH?|mm?|ss?|SS?S?|X|zz?|ZZ?|.)/g,N=/(\[[^\[]*\])|(\\)?(LT|LL?L?L?|l{1,4})/g,J=/\d\d?/,I=/\d{1,3}/,V=/\d{3}/,X=/\d{1,4}/,$=/[+\-]?\d{1,6}/,R=/[0-9]*['a-z\u00A0-\u05FF\u0700-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+|[\u0600-\u06FF\/]+(\s*?[\u0600-\u06FF]+){1,2}/i,j=/Z|[\+\-]\d\d:?\d\d/i,q=/T/i,B=/[\+\-]?\d+(\.\d{1,3})?/,K=/^\s*\d{4}-\d\d-\d\d((T| )(\d\d(:\d\d(:\d\d(\.\d\d?\d?)?)?)?)?([\+\-]\d\d:?\d\d)?)?/,Q="YYYY-MM-DDTHH:mm:ssZ",te=[["HH:mm:ss.S",/(T| )\d\d:\d\d:\d\d\.\d{1,3}/],["HH:mm:ss",/(T| )\d\d:\d\d:\d\d/],["HH:mm",/(T| )\d\d:\d\d/],["HH",/(T| )\d\d/]],ee=/([\+\-]|\d\d)/gi,ne="Date|Hours|Minutes|Seconds|Milliseconds".split("|"),se={Milliseconds:1,Seconds:1e3,Minutes:6e4,Hours:36e5,Days:864e5,Months:2592e6,Years:31536e6},ie={ms:"millisecond",s:"second",m:"minute",h:"hour",d:"day",w:"week",M:"month",y:"year"},re={},ae="DDD w W M D d".split(" "),oe="M D H h m s w W".split(" "),ue={M:function(){return this.month()+1},MMM:function(t){return this.lang().monthsShort(this,t)},MMMM:function(t){return this.lang().months(this,t)},D:function(){return this.date()},DDD:function(){return this.dayOfYear()},d:function(){return this.day()},dd:function(t){return this.lang().weekdaysMin(this,t)},ddd:function(t){return this.lang().weekdaysShort(this,t)},dddd:function(t){return this.lang().weekdays(this,t)},w:function(){return this.week()},W:function(){return this.isoWeek()},YY:function(){return u(this.year()%100,2)},YYYY:function(){return u(this.year(),4)},YYYYY:function(){return u(this.year(),5)},gg:function(){return u(this.weekYear()%100,2)},gggg:function(){return this.weekYear()},ggggg:function(){return u(this.weekYear(),5)},GG:function(){return u(this.isoWeekYear()%100,2)},GGGG:function(){return this.isoWeekYear()},GGGGG:function(){return u(this.isoWeekYear(),5)},e:function(){return this.weekday()},E:function(){return this.isoWeekday()},a:function(){return this.lang().meridiem(this.hours(),this.minutes(),!0)},A:function(){return this.lang().meridiem(this.hours(),this.minutes(),!1)},H:function(){return this.hours()},h:function(){return this.hours()%12||12},m:function(){return this.minutes()},s:function(){return this.seconds()},S:function(){return~~(this.milliseconds()/100)},SS:function(){return u(~~(this.milliseconds()/10),2)},SSS:function(){return u(this.milliseconds(),3)},Z:function(){var t=-this.zone(),e="+";return 0>t&&(t=-t,e="-"),e+u(~~(t/60),2)+":"+u(~~t%60,2)},ZZ:function(){var t=-this.zone(),e="+";return 0>t&&(t=-t,e="-"),e+u(~~(10*t/6),4)},z:function(){return this.zoneAbbr()},zz:function(){return this.zoneName()},X:function(){return this.unix()}};ae.length;)P=ae.pop(),ue[P+"o"]=n(ue[P],P);for(;oe.length;)P=oe.pop(),ue[P+P]=e(ue[P],2);for(ue.DDDD=e(ue.DDD,3),s.prototype={set:function(t){var e,n;for(n in t)e=t[n],"function"==typeof e?this[n]=e:this["_"+n]=e},_months:"January_February_March_April_May_June_July_August_September_October_November_December".split("_"),months:function(t){return this._months[t.month()]},_monthsShort:"Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec".split("_"),monthsShort:function(t){return this._monthsShort[t.month()]},monthsParse:function(t){var e,n,s;for(this._monthsParse||(this._monthsParse=[]),e=0;12>e;e++)if(this._monthsParse[e]||(n=H([2e3,e]),s="^"+this.months(n,"")+"|^"+this.monthsShort(n,""),this._monthsParse[e]=new RegExp(s.replace(".",""),"i")),this._monthsParse[e].test(t))return e},_weekdays:"Sunday_Monday_Tuesday_Wednesday_Thursday_Friday_Saturday".split("_"),weekdays:function(t){return this._weekdays[t.day()]},_weekdaysShort:"Sun_Mon_Tue_Wed_Thu_Fri_Sat".split("_"),weekdaysShort:function(t){return this._weekdaysShort[t.day()]},_weekdaysMin:"Su_Mo_Tu_We_Th_Fr_Sa".split("_"),weekdaysMin:function(t){return this._weekdaysMin[t.day()]},weekdaysParse:function(t){var e,n,s;for(this._weekdaysParse||(this._weekdaysParse=[]),e=0;7>e;e++)if(this._weekdaysParse[e]||(n=H([2e3,1]).day(e),s="^"+this.weekdays(n,"")+"|^"+this.weekdaysShort(n,"")+"|^"+this.weekdaysMin(n,""),this._weekdaysParse[e]=new RegExp(s.replace(".",""),"i")),this._weekdaysParse[e].test(t))return e},_longDateFormat:{LT:"h:mm A",L:"MM/DD/YYYY",LL:"MMMM D YYYY",LLL:"MMMM D YYYY LT",LLLL:"dddd, MMMM D YYYY LT"},longDateFormat:function(t){var e=this._longDateFormat[t];return!e&&this._longDateFormat[t.toUpperCase()]&&(e=this._longDateFormat[t.toUpperCase()].replace(/MMMM|MM|DD|dddd/g,function(t){return t.slice(1)}),this._longDateFormat[t]=e),e},isPM:function(t){return"p"===(t+"").toLowerCase()[0]},_meridiemParse:/[ap]\.?m?\.?/i,meridiem:function(t,e,n){return t>11?n?"pm":"PM":n?"am":"AM"},_calendar:{sameDay:"[Today at] LT",nextDay:"[Tomorrow at] LT",nextWeek:"dddd [at] LT",lastDay:"[Yesterday at] LT",lastWeek:"[Last] dddd [at] LT",sameElse:"L"},calendar:function(t,e){var n=this._calendar[t];return"function"==typeof n?n.apply(e):n},_relativeTime:{future:"in %s",past:"%s ago",s:"a few seconds",m:"a minute",mm:"%d minutes",h:"an hour",hh:"%d hours",d:"a day",dd:"%d days",M:"a month",MM:"%d months",y:"a year",yy:"%d years"},relativeTime:function(t,e,n,s){var i=this._relativeTime[n];return"function"==typeof i?i(t,e,n,s):i.replace(/%d/i,t)},pastFuture:function(t,e){var n=this._relativeTime[t>0?"future":"past"];return"function"==typeof n?n(e):n.replace(/%s/i,e)},ordinal:function(t){return this._ordinal.replace("%d",t)},_ordinal:"%d",preparse:function(t){return t},postformat:function(t){return t},week:function(t){return F(t,this._week.dow,this._week.doy).week},_week:{dow:0,doy:6}},H=function(t,e,n){return O({_i:t,_f:e,_l:n,_isUTC:!1})},H.utc=function(t,e,n){return O({_useUTC:!0,_isUTC:!0,_l:n,_i:t,_f:e})},H.unix=function(t){return H(1e3*t)},H.duration=function(t,e){var n,s,i=H.isDuration(t),a="number"==typeof t,o=i?t._input:a?{}:t,u=Z.exec(t);return a?e?o[e]=t:o.milliseconds=t:u&&(n="-"===u[1]?-1:1,o={y:0,d:~~u[2]*n,h:~~u[3]*n,m:~~u[4]*n,s:~~u[5]*n,ms:~~u[6]*n}),s=new r(o),i&&t.hasOwnProperty("_lang")&&(s._lang=t._lang),s},H.version=U,H.defaultFormat=Q,H.updateOffset=function(){},H.lang=function(t,e){return t?(e?l(t,e):x[t]||_(t),H.duration.fn._lang=H.fn._lang=_(t),void 0):H.fn._lang._abbr},H.langData=function(t){return t&&t._lang&&t._lang._abbr&&(t=t._lang._abbr),_(t)},H.isMoment=function(t){return t instanceof i},H.isDuration=function(t){return t instanceof r},H.fn=i.prototype={clone:function(){return H(this)},valueOf:function(){return+this._d+6e4*(this._offset||0)},unix:function(){return Math.floor(+this/1e3)},toString:function(){return this.format("ddd MMM DD YYYY HH:mm:ss [GMT]ZZ")},toDate:function(){return this._offset?new Date(+this):this._d},toISOString:function(){return M(H(this).utc(),"YYYY-MM-DD[T]HH:mm:ss.SSS[Z]")},toArray:function(){var t=this;return[t.year(),t.month(),t.date(),t.hours(),t.minutes(),t.seconds(),t.milliseconds()]},isValid:function(){return null==this._isValid&&(this._isValid=this._a?!c(this._a,(this._isUTC?H.utc(this._a):H(this._a)).toArray()):!isNaN(this._d.getTime())),!!this._isValid},utc:function(){return this.zone(0)},local:function(){return this.zone(0),this._isUTC=!1,this},format:function(t){var e=M(this,t||H.defaultFormat);return this.lang().postformat(e)},add:function(t,e){var n;return n="string"==typeof t?H.duration(+e,t):H.duration(t,e),h(this,n,1),this},subtract:function(t,e){var n;return n="string"==typeof t?H.duration(+e,t):H.duration(t,e),h(this,n,-1),this},diff:function(t,e,n){var s,i,r=this._isUTC?H(t).zone(this._offset||0):H(t).local(),a=6e4*(this.zone()-r.zone());return e=f(e),"year"===e||"month"===e?(s=432e5*(this.daysInMonth()+r.daysInMonth()),i=12*(this.year()-r.year())+(this.month()-r.month()),i+=(this-H(this).startOf("month")-(r-H(r).startOf("month")))/s,i-=6e4*(this.zone()-H(this).startOf("month").zone()-(r.zone()-H(r).startOf("month").zone()))/s,"year"===e&&(i/=12)):(s=this-r,i="second"===e?s/1e3:"minute"===e?s/6e4:"hour"===e?s/36e5:"day"===e?(s-a)/864e5:"week"===e?(s-a)/6048e5:s),n?i:o(i)},from:function(t,e){return H.duration(this.diff(t)).lang(this.lang()._abbr).humanize(!e)},fromNow:function(t){return this.from(H(),t)},calendar:function(){var t=this.diff(H().startOf("day"),"days",!0),e=-6>t?"sameElse":-1>t?"lastWeek":0>t?"lastDay":1>t?"sameDay":2>t?"nextDay":7>t?"nextWeek":"sameElse";return this.format(this.lang().calendar(e,this))},isLeapYear:function(){var t=this.year();return 0===t%4&&0!==t%100||0===t%400},isDST:function(){return this.zone()<this.clone().month(0).zone()||this.zone()<this.clone().month(5).zone()},day:function(t){var e=this._isUTC?this._d.getUTCDay():this._d.getDay();return null!=t?"string"==typeof t&&(t=this.lang().weekdaysParse(t),"number"!=typeof t)?this:this.add({d:t-e}):e},month:function(t){var e,n=this._isUTC?"UTC":"";return null!=t?"string"==typeof t&&(t=this.lang().monthsParse(t),"number"!=typeof t)?this:(e=this.date(),this.date(1),this._d["set"+n+"Month"](t),this.date(Math.min(e,this.daysInMonth())),H.updateOffset(this),this):this._d["get"+n+"Month"]()},startOf:function(t){switch(t=f(t)){case"year":this.month(0);case"month":this.date(1);case"week":case"day":this.hours(0);case"hour":this.minutes(0);case"minute":this.seconds(0);case"second":this.milliseconds(0)}return"week"===t&&this.weekday(0),this},endOf:function(t){return this.startOf(t).add(t,1).subtract("ms",1)},isAfter:function(t,e){return e="undefined"!=typeof e?e:"millisecond",+this.clone().startOf(e)>+H(t).startOf(e)},isBefore:function(t,e){return e="undefined"!=typeof e?e:"millisecond",+this.clone().startOf(e)<+H(t).startOf(e)},isSame:function(t,e){return e="undefined"!=typeof e?e:"millisecond",+this.clone().startOf(e)===+H(t).startOf(e)},min:function(t){return t=H.apply(null,arguments),this>t?this:t},max:function(t){return t=H.apply(null,arguments),t>this?this:t},zone:function(t){var e=this._offset||0;return null==t?this._isUTC?e:this._d.getTimezoneOffset():("string"==typeof t&&(t=p(t)),Math.abs(t)<16&&(t=60*t),this._offset=t,this._isUTC=!0,e!==t&&h(this,H.duration(e-t,"m"),1,!0),this)},zoneAbbr:function(){return this._isUTC?"UTC":""},zoneName:function(){return this._isUTC?"Coordinated Universal Time":""},daysInMonth:function(){return H.utc([this.year(),this.month()+1,0]).date()},dayOfYear:function(t){var e=W((H(this).startOf("day")-H(this).startOf("year"))/864e5)+1;return null==t?e:this.add("d",t-e)},weekYear:function(t){var e=F(this,this.lang()._week.dow,this.lang()._week.doy).year;return null==t?e:this.add("y",t-e)},isoWeekYear:function(t){var e=F(this,1,4).year;return null==t?e:this.add("y",t-e)},week:function(t){var e=this.lang().week(this);return null==t?e:this.add("d",7*(t-e))},isoWeek:function(t){var e=F(this,1,4).week;return null==t?e:this.add("d",7*(t-e))},weekday:function(t){var e=(this._d.getDay()+7-this.lang()._week.dow)%7;return null==t?e:this.add("d",t-e)},isoWeekday:function(t){return null==t?this.day()||7:this.day(this.day()%7?t:t-7)},lang:function(e){return e===t?this._lang:(this._lang=_(e),this)}},P=0;P<ne.length;P++)z(ne[P].toLowerCase().replace(/s$/,""),ne[P]);z("year","FullYear"),H.fn.days=H.fn.day,H.fn.months=H.fn.month,H.fn.weeks=H.fn.week,H.fn.isoWeeks=H.fn.isoWeek,H.fn.toJSON=H.fn.toISOString,H.duration.fn=r.prototype={_bubble:function(){var t,e,n,s,i=this._milliseconds,r=this._days,a=this._months,u=this._data;u.milliseconds=i%1e3,t=o(i/1e3),u.seconds=t%60,e=o(t/60),u.minutes=e%60,n=o(e/60),u.hours=n%24,r+=o(n/24),u.days=r%30,a+=o(r/30),u.months=a%12,s=o(a/12),u.years=s},weeks:function(){return o(this.days()/7)},valueOf:function(){return this._milliseconds+864e5*this._days+2592e6*(this._months%12)+31536e6*~~(this._months/12)},humanize:function(t){var e=+this,n=S(e,!t,this.lang());return t&&(n=this.lang().pastFuture(e,n)),this.lang().postformat(n)},add:function(t,e){var n=H.duration(t,e);return this._milliseconds+=n._milliseconds,this._days+=n._days,this._months+=n._months,this._bubble(),this},subtract:function(t,e){var n=H.duration(t,e);return this._milliseconds-=n._milliseconds,this._days-=n._days,this._months-=n._months,this._bubble(),this},get:function(t){return t=f(t),this[t.toLowerCase()+"s"]()},as:function(t){return t=f(t),this["as"+t.charAt(0).toUpperCase()+t.slice(1)+"s"]()},lang:H.fn.lang};for(P in se)se.hasOwnProperty(P)&&(L(P,se[P]),C(P.toLowerCase()));L("Weeks",6048e5),H.duration.fn.asMonths=function(){return(+this-31536e6*this.years())/2592e6+12*this.years()},H.lang("en",{ordinal:function(t){var e=t%10,n=1===~~(t%100/10)?"th":1===e?"st":2===e?"nd":3===e?"rd":"th";return t+n}}),A&&(module.exports=H),"undefined"==typeof ender&&(this.moment=H),"function"==typeof define&&define.amd&&define("moment",[],function(){return H})}.call(this);

/*
 * jQuery UI Touch Punch 0.2.2
 *
 * Copyright 2011, Dave Furfero
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * Depends:
 *  jquery.ui.widget.js
 *  jquery.ui.mouse.js
 */
(function(b){b.support.touch="ontouchend" in document;if(!b.support.touch){return;}var c=b.ui.mouse.prototype,e=c._mouseInit,a;function d(g,h){if(g.originalEvent.touches.length>1){return;}g.preventDefault();var i=g.originalEvent.changedTouches[0],f=document.createEvent("MouseEvents");f.initMouseEvent(h,true,true,window,1,i.screenX,i.screenY,i.clientX,i.clientY,false,false,false,false,0,null);g.target.dispatchEvent(f);}c._touchStart=function(g){var f=this;if(a||!f._mouseCapture(g.originalEvent.changedTouches[0])){return;}a=true;f._touchMoved=false;d(g,"mouseover");d(g,"mousemove");d(g,"mousedown");};c._touchMove=function(f){if(!a){return;}this._touchMoved=true;d(f,"mousemove");};c._touchEnd=function(f){if(!a){return;}d(f,"mouseup");d(f,"mouseout");if(!this._touchMoved){d(f,"click");}a=false;};c._mouseInit=function(){var f=this;f.element.bind("touchstart",b.proxy(f,"_touchStart")).bind("touchmove",b.proxy(f,"_touchMove")).bind("touchend",b.proxy(f,"_touchEnd"));e.call(f);};})(jQuery);


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
		useTime: false
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

		var localDTPicker;

		cfg.showOn = (options.icon === 0) ? "focus" : "both";
		cfg.buttonImage = (options.icon === 0) ? null : "/library/image/silk/calendar.png";
		cfg.buttonImageOnly = true;

		cfg.stepMinute = options.stepMinute;
		cfg.altField = options.altField;
		cfg.altFormat = options.altFormat;
		cfg.altFieldTimeOnly = false;
		cfg.showTimepicker = options.useTime;
		cfg.parseFormat = options.parseFormat;

		// disable the input field on show as to not display a tablet keyboard
		cfg.onClose = function(dateText, inst) {
			$(this).removeProp("disabled");
		};
		// re-enables input field
		cfg.beforeShow = function(input, inst) {
			$(this).prop("disabled", 'disabled');
		};
		// on select, runs our custom method for setting dates
		cfg.onSelect = function(dtObj, dpInst) {
			setHiddenFields($(this).datepicker("getDate"), options, dpInst);
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

				// formatList can be added to as needed. Refer to Moment Docs for reference
				// http://momentjs.com/docs/#/parsing/string-format/
				var formatList = [];

				if (typeof cfg.parseFormat !== 'undefined') {
					parseDate = new Date(moment(d, cfg.parseFormat));
				} else {
					parseDate = new Date(moment(d, cfg.parseFormat));
				};

			} else {
				window.console && console.log("date object: " + d);
				parseDate = d;
			};

			var roundDate = roundMinutes(parseDate)
			window.console && console.log('parsed and rounded date: ' + roundDate);

			return roundDate;
		}


		/**
		 * Utility method to round a date object to the closest stepMinute (15 by default) minutes
		 * 
		 * @param  {object} date Javascript Date Object
		 * @return {object}      rounded javascript date object
		 */
		function roundMinutes(date) {
			var rounder = Math.floor(date.getMinutes()/options.stepMinute);

			if (date.getMinutes()%options.stepMinute > (options.stepMinute/2)) {
				rounder = rounder + 1;
			}

			var min = rounder*options.stepMinute;

			date.setMinutes(min);

			return date;
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
					switch(i) {
						case "month":
						  jQuery('#' + h).val(d.getMonth() + 1);
						  break;
						case "day":
						  jQuery('#' + h).val(d.getDate());
						  break;
						case "year":
						  jQuery('#' + h).val(d.getFullYear());
						  break;
						case "hour":
						  jQuery('#' + h).val((o.ampm == true) ? moment(d).format('hh') : moment(d).format('HH'));
						  break;
						case "minute":
						  jQuery('#' + h).val(moment(d).format('mm'));
						  break;
						case "ampm":
						  jQuery('#' + h).val(moment(d).format('A').toLowerCase());
						  break;
						case "iso8601":
						  jQuery('#' + h).val(moment(d).format());
						  break;
					}
				});
			}
		}

		/**
		 * Initiallizes the base date for the picker
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
			if (!initVal) initVal = new Date();

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
		 * Event handler for language ajax call, loading a particular language
		 * 
		 * @param  {object} ll 2 variables are loaded in to the scrip: lang and timeLang
		 */
		var langLoaded = function(ll) {

			// set the initial date for the picker
			initDateTime();

			// instantiate datetimepicker
			localDTPicker = $(options.input).css('min-width','200px').datetimepicker(cfg);

			// set the datepicker date if we've got a pre-set value
			if (typeof options.val !== 'undefined' || typeof options.getval !== 'undefined' || typeof options.duration !== 'undefined') {
				localDTPicker.datetimepicker("setDate", localDate);
				// Handle duration, which relies on the datepicker being set
				handleDuration();
			}

			// if the picker is reliant in hudden fields, set them
			setHiddenFields(localDate); 
		};

		/**
		 * Init the app by making a request for a language file.
		 * Failing the locale, we look for the base language
		 */
		var userLocale = document.getElementById("locale").innerHTML.replace("_", "-");
		var localeIndex = userLocale.indexOf("-");
		if (localeIndex > 0)
		    userLang = userLocale.substring(0, localeIndex);
		else
		    userLang = userLocale;

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

         langLoaded(true);
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

