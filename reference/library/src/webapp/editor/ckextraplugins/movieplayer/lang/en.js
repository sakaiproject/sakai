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

CKEDITOR.addPluginLang('movieplayer','en',
    {
        'MoviePlayerBtn':'Movie',
        'MoviePlayerTooltip':'Insert/Edit Movie',
        'MoviePlayerDlgTitle':'Movie Properties',
        'MoviePlayerURL':'URL:',
        'MoviePlayerURLDesc':'Select a .mp3, .mp4, .flv, .wma, .avi or .mov file',
        'MoviePlayerWidth':'Width:',
        'MoviePlayerHeight':'Height:',
        'MoviePlayerAutoplay':'Auto Play:',
        'MoviePlayerNoUrl':'Please specify a movie URL.'
    }
);
