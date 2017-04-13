CKEDITOR.addPluginLang = function( plugin, lang, obj )
{
    // v3 using feature detection
    if (CKEDITOR.skins)
    {
        var newObj = {};
        newObj[ plugin ] = obj;
        obj = newObj;
    }
    CKEDITOR.plugins.setLang( plugin, lang, obj );
}

CKEDITOR.addPluginLang('audiorecorder','en',
    {
	'record':'Start recording',
	'stop':'Stop recording',
	'play':'Preview recording',
	'error':'Error',
	'post':'Post recording',
	'seconds_label':'seconds remaining',
	'posting':'Posting response. Do not close this window.',
	'complete':'Upload is complete. Closing this window.',
	'intro':'You have a limited number of seconds (indicated below) to record your response. If the record button isn\'t enabled, check the top bar of your browser or the area below to <strong>Allow</strong> use of your microphone. <span style=\'color: red\'>Using headphones while recording is recommended.</span>',
	'dlgtitle':'Audio recorder',
	'tooltip':'Record Audio Clip'
    }
);
