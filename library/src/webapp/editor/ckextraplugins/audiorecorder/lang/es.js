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

CKEDITOR.addPluginLang('audiorecorder','es',
    {
	'record':'Comenzar grabación',
	'stop':'Detener grabación',
	'play':'Previsualizar grabación',
	'error':'Error',
	'post':'Guardar grabación',
	'seconds_label':'segundos restantes',
	'posting':'Guardando grabación. Por favor, no cierre la ventana.',
	'complete':'Completando subida de audio. Cerrando esta ventana...',
	'intro':'Tienes un número de segundos limitado para realizar una grabación. Si el botón de "Comenzar grabación" no está habilitado, comprueba que tu navegador tiene los permisos adecuados. <strong>Permitir</strong> utilizar el micrófono. <span style=\'color: red\'>Se recomienda usar auriculares para realizar la grabación.</span>',
	'dlgtitle':'Grabación de audio',
	'tooltip':'Grabación de audio'
    }
);
