var item =0;
var ta_editor =  [];
var hidden = [];
var textAreaNames = [];
var runFocus=true;


function initEditors(contextPath)
{
//alert("disabled");
return; // debug

//    alert("begin.");
    myDocumentElements=document.getElementsByTagName("textarea");
    for (i=0;i<myDocumentElements.length;i++)
    {
      name = myDocumentElements[i].name;
//			alert(name);
      if (name.indexOf("nowysiwyg") != -1)
      {
        continue; // any name attribute containing 'nowysiwyg' will be bypassed
      }
      textAreaNames[i] = name;
      editor =
        initEditorById(name, contextPath + "/htmlarea/", "three", true);
      ta_editor[i] = editor;
//      alert(name)
      wait(100);
      hideUnhide(name);
      wait(100);
      hideUnhide(name);
//      alert(editor._textArea.value);

//      alert("added " + name);
    }

    for (i=textAreaNames.length; i>0; name=textAreaNames[--i])
    {
      wait(200);
      setFocus(name);
      wait(100);
      break;
    }

}

function wait(delay)
{
     string="noop("+delay+");";
     setTimeout(string,delay);
}

function noop(delay)
{
//     alert("Ok "+delay/1000+" seconds have elapsed");
}

function setFocus(ta)
{
   var element = document.getElementById(ta);
   element.focus();
   return false;
}
