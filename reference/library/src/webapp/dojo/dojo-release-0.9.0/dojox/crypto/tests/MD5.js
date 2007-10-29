if(!dojo._hasResource["dojox.crypto.tests.MD5"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.crypto.tests.MD5"] = true;
dojo.provide("dojox.crypto.tests.MD5");
dojo.require("dojox.crypto.MD5");

(function(){
	var message="The rain in Spain falls mainly on the plain.";
	var base64="OUhxbVZ1Mtmu4zx9LzS5cA==";
	var hex="3948716d567532d9aee33c7d2f34b970";
	var s="9HqmVu2\xD9\xAE\xE3<}/4\xB9p";
	var dxc=dojox.crypto;

	tests.register("dojox.crypto.tests.MD5", [
		function testBase64Compute(t){
			t.assertEqual(base64, dxc.MD5.compute(message));
		},
		function testHexCompute(t){
			t.assertEqual(hex, dxc.MD5.compute(message, dxc.outputTypes.Hex)); 
		},
		function testStringCompute(t){
			t.assertEqual(s, dxc.MD5.compute(message, dxc.outputTypes.String)); 
		}
	]);
})();

}
