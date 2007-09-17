if(!dojo._hasResource["dijit.tests.module"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.tests.module"] = true;
dojo.provide("dijit.tests.module");

try{
	dojo.require("dijit.tests._base.manager");
	dojo.require("dijit.tests._Templated");
	dojo.require("dijit.tests.widgetsInTemplate");
	dojo.require("dijit.tests.Container");
	dojo.require("dijit.tests.layout.ContentPane");
	dojo.require("dijit.tests.ondijitclick");
	dojo.require("dijit.tests.form.Form");
}catch(e){
	doh.debug(e);
}



}
