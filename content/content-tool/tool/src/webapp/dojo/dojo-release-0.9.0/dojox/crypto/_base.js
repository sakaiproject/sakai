if(!dojo._hasResource["dojox.crypto._base"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.crypto._base"] = true;
dojo.provide("dojox.crypto._base");

(function(){
	var dxc=dojox.crypto;
	dxc.cipherModes={ 
		//	summary
		//	Enumeration for various cipher modes.
		ECB:0, CBC:1, PCBC:2, CFB:3, OFB:4, CTR:5 
	};
	dxc.outputTypes={ 
		//	summary
		//	Enumeration for input and output encodings.
		Base64:0, Hex:1, String:2, Raw:3 
	};
})();

}
