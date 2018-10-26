/*Helper functions to draw an indicator when caps lock is on*/

/*inspired by by Rajesh Pillai, Dave, user110902, and Arthur P
http://stackoverflow.com/questions/348792/how-do-you-tell-if-caps-lock-is-on-using-javascript*/

var browserDisplaysCapsLock = false;

/*Some browsers on Mac OS X have a caps-lock indicator built in for input fields with type="password" */
if (navigator.appVersion.indexOf("Mac") !== -1)
{
    var userAgent = navigator.userAgent.toLowerCase();
    if (userAgent.indexOf('safari') !== -1)
    {
        /*covers safari, chromium and chrome*/
        browserDisplaysCapsLock = true;
    }
}

if (!browserDisplaysCapsLock)
{
    $("input:password").keypress(function(e)
    {
        if ( isCapslock(e) )
        {
            if (this.className.indexOf("capsLockOnDisplay") === -1)
            {
                this.className += " capsLockOnDisplay";
            }
        }
        else
        {
            var className = this.className.replace("capsLockOnDisplay", "");
            this.className = className;
        }
    });
}

function isCapslock(e)
{
    e = (e) ? e: window.event;

    var charCode = false;
    if (e.which)
    {
        charCode = e.which;
    }
    else if (e.keyCode)
    {
        charCode = e.keyCode;
    }

    var shifton = false;
    if (e.shiftKey)
    {
        shifton = e.shiftKey;
    }
    else if (e.modifiers)
    {
        shifton = !!(e.modifiers & 4);
    }

    if (charCode >=97 && charCode <= 122 && shifton)
    {
        return true;
    }

    if (charCode >= 65 && charCode <= 90 && !shifton)
    {
        return true;
    }

    return false;
}
