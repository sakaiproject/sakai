/**
 * Miscellaneous Utils
 */

function f_scrollTop(){
    return f_filterResults(window.pageYOffset ? window.pageYOffset : 0, document.documentElement ? document.documentElement.scrollTop : 0, document.body ? document.body.scrollTop : 0);
}

function f_filterResults(n_win, n_docel, n_body){
    var n_result = n_win ? n_win : 0;
    if (n_docel && (!n_result || (n_result > n_docel))) 
        n_result = n_docel;
    return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}

$PBJQ(document).ready(function(){
	$PBJQ('input, textarea', '#content').each( function(){
		if( $(this).prop('disabled') ){
			$(this).parent('label').addClass('disabled');
		}
	});
});