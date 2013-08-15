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

CKEDITOR.addPluginLang('movieplayer','pt',
    {
        // Toolbar button
        'MoviePlayerBtn':'Vídeo',
        'MoviePlayerTooltip':'Inserir/Editar Vídeo',

        // Dialog
        'MoviePlayerDlgTitle':'Propriedades do Vídeo',
        'MoviePlayerURL':'URL:',
        'MoviePlayerURLDesc':'Seleccione um ficheiro .mp3, .mp4, .flv, .wma, .avi ou .mov',
        'MoviePlayerWidth':'Largura:',
        'MoviePlayerHeight':'Altura:',
        'MoviePlayerAutoplay':'Reproduzir Automaticamente:',

        // Dialog errors
        'MoviePlayerNoUrl':'Por favor especifique o endereço do vídeo (URL).'
    }
);
