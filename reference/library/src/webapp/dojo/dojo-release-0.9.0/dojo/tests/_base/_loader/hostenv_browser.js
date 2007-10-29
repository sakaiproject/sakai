if(!dojo._hasResource["tests._base._loader.hostenv_browser"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["tests._base._loader.hostenv_browser"] = true;
dojo.provide("tests._base._loader.hostenv_browser");

tests.register("tests._base._loader.hostenv_browser", 
	[
		function getText(t){
			var filePath = dojo.moduleUrl("tests._base._loader", "getText.txt");
			var text = dojo._getText(filePath);
			t.assertEqual("dojo._getText() test data", text);
		}
	]
);

}
