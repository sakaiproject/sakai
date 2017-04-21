function initDatepicker() {
	$('.datePicker').each(function(){
		if(!$(this).hasClass('hasDatepicker')) {
			var id = this.id;
			var val = $(this).val();
			$(this).val('');
			localDatePicker({
				input: $(this),
				useTime: 0,
				parseFormat: 'YYYY-MM-DD',
				allowEmptyDate: true,
				val: val,
				ashidden: {
					iso8601: id+'_ISO8601'
				}
			});
		}
	});
}

$(window).ready(function(){
	initDatepicker();
});
