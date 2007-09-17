if(!dojo._hasResource["tests._base._loader.hostenv_rhino"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["tests._base._loader.hostenv_rhino"] = true;
dojo.provide("tests._base._loader.hostenv_rhino");

tests.register("tests._base._loader.hostenv_rhino", 
	[
		function getText(t){
			var filePath = dojo.moduleUrl("tests._base._loader", "getText.txt");
			var text = (new String(readText(filePath)));
			//The Java file read seems to add a line return.
			text = text.replace(/[\r\n]+$/, "");
			t.assertEqual("dojo._getText() test data", text);
		}
	]
);

}
