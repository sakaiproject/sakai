if(!dojo._hasResource["dojox.uuid.tests.uuid"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.uuid.tests.uuid"] = true;
dojo.provide("dojox.uuid.tests.uuid");
dojo.require("dojox.uuid");
dojo.require("dojox.uuid.Uuid");
dojo.require("dojox.uuid.generateRandomUuid");
dojo.require("dojox.uuid.generateTimeBasedUuid");

dojox.uuid.tests.uuid.checkValidityOfUuidString = function(/*String*/uuidString){
	// summary:
	//		A helper function that's used by the registered test functions
	var NIL_UUID = "00000000-0000-0000-0000-000000000000";
	if (uuidString == NIL_UUID) {
		// We'll consider the Nil UUID to be valid, so now 
		// we can just return, with not further checks.
		return;
	}
	
	doh.assertTrue(uuidString.length == 36); // UUIDs have 36 characters

	var validCharacters = "0123456789abcedfABCDEF-";
	var character;
	var position;
	for(var i = 0; i < 36; ++i){
		character = uuidString.charAt(i);
		position = validCharacters.indexOf(character);
		doh.assertTrue(position != -1); // UUIDs have only valid characters
	}

	var arrayOfParts = uuidString.split("-");
	doh.assertTrue(arrayOfParts.length == 5); // UUIDs have 5 sections separated by 4 hyphens
	doh.assertTrue(arrayOfParts[0].length == 8); // Section 0 has 8 characters
	doh.assertTrue(arrayOfParts[1].length == 4); // Section 1 has 4 characters
	doh.assertTrue(arrayOfParts[2].length == 4); // Section 2 has 4 characters
	doh.assertTrue(arrayOfParts[3].length == 4); // Section 3 has 4 characters
	doh.assertTrue(arrayOfParts[4].length == 12); // Section 4 has 8 characters

	// check to see that the "UUID variant code" starts with the binary bits '10'
	var section3 = arrayOfParts[3];
	var HEX_RADIX = 16;
	var hex3 = parseInt(section3, HEX_RADIX);
	var binaryString = hex3.toString(2);
	// alert("section3 = " + section3 + "\n binaryString = " + binaryString);
	doh.assertTrue(binaryString.length == 16); // section 3 has 16 bits
	doh.assertTrue(binaryString.charAt(0) == '1'); // first bit of section 3 is 1
	doh.assertTrue(binaryString.charAt(1) == '0'); // second bit of section 3 is 0
}

dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString = function(/*String*/uuidString){
	// summary:
	//		A helper function that's used by the registered test functions
	dojox.uuid.tests.uuid.checkValidityOfUuidString(uuidString);
	var arrayOfParts = uuidString.split("-");
	var section2 = arrayOfParts[2];
	doh.assertTrue(section2.charAt(0) == "1"); // Section 2 starts with a 1
}

dojox.uuid.tests.uuid.checkForPseudoNodeBitInTimeBasedUuidString = function(/*String*/uuidString){
	// summary:
	//		A helper function that's used by the registered test functions
	var arrayOfParts = uuidString.split("-");
	var section4 = arrayOfParts[4];
	var firstChar = section4.charAt(0);
	var HEX_RADIX = 16;
	var hexFirstChar = parseInt(firstChar, HEX_RADIX);
	var binaryString = hexFirstChar.toString(2);
	var firstBit;
	if(binaryString.length == 4){
		firstBit = binaryString.charAt(0);
	}else{
		firstBit = '0';
	}
	doh.assertTrue(firstBit == '1'); // first bit of section 4 is 1
}

doh.register("dojox.uuid.tests.uuid", 
	[
		/*
		function test_uuid_performance(){
			var start = new Date();
			var startMS = start.valueOf();
			var nowMS = startMS;
			var i;
			var now;
			var numTrials = 100000;
		
			while(nowMS == startMS){
				now = new Date();
				nowMS = now.valueOf();
			}
			
			startMS = nowMS;
			for(i = 0; i < numTrials; ++i){
				var a = dojox.uuid.LightweightGenerator.generate();
			}
			now = new Date();
			nowMS = now.valueOf();
			var elapsedMS = nowMS - startMS;
			// dojo.log.debug("created " + numTrials + " UUIDs in " + elapsedMS + " milliseconds");
		},
		*/

		function test_uuid_capitalization(){
			var randomLowercaseString = "3b12f1df-5232-4804-897e-917bf397618a";
			var randomUppercaseString = "3B12F1DF-5232-4804-897E-917BF397618A";
			
			var timebasedLowercaseString = "b4308fb0-86cd-11da-a72b-0800200c9a66";
			var timebasedUppercaseString = "B4308FB0-86CD-11DA-A72B-0800200C9A66";
			
			var uuidRL = new dojox.uuid.Uuid(randomLowercaseString);
			var uuidRU = new dojox.uuid.Uuid(randomUppercaseString);
			
			var uuidTL = new dojox.uuid.Uuid(timebasedLowercaseString);
			var uuidTU = new dojox.uuid.Uuid(timebasedUppercaseString);
			
			doh.assertTrue(uuidRL.isEqual(uuidRU));
			doh.assertTrue(uuidRU.isEqual(uuidRL));
			
			doh.assertTrue(uuidTL.isEqual(uuidTU));
			doh.assertTrue(uuidTU.isEqual(uuidTL));
		},
	
		function test_uuid_constructor(){
			var uuid, uuidToo;
			
			var nilUuid = '00000000-0000-0000-0000-000000000000';
			uuid = new dojox.uuid.Uuid();
			doh.assertTrue(uuid == nilUuid); // 'new dojox.uuid.Uuid()' returns the Nil UUID
			
			var randomUuidString = "3b12f1df-5232-4804-897e-917bf397618a";
			uuid = new dojox.uuid.Uuid(randomUuidString);
			doh.assertTrue(uuid.isValid());
			doh.assertTrue(uuid.getVariant() == dojox.uuid.variant.DCE);
			doh.assertTrue(uuid.getVersion() == dojox.uuid.version.RANDOM);
			uuidToo = new dojox.uuid.Uuid(new String(randomUuidString));
			doh.assertTrue(uuid.isEqual(uuidToo));	
		
			var timeBasedUuidString = "b4308fb0-86cd-11da-a72b-0800200c9a66";
			uuid = new dojox.uuid.Uuid(timeBasedUuidString);
			doh.assertTrue(uuid.isValid());
			doh.assertTrue(uuid.getVariant() == dojox.uuid.variant.DCE);
			doh.assertTrue(uuid.getVersion() == dojox.uuid.version.TIME_BASED);
			doh.assertTrue(uuid.getNode() == "0800200c9a66");
			var timestamp = uuid.getTimestamp();
			var date = uuid.getTimestamp(Date);
			var dateString = uuid.getTimestamp(String);
			var hexString = uuid.getTimestamp("hex");
			var now = new Date();
			doh.assertTrue(timestamp.valueOf() == date.valueOf());
			doh.assertTrue(hexString == "1da86cdb4308fb0");
			doh.assertTrue(timestamp < now);
		},
		
		function test_uuid_generators(){
			var generators = [
				dojox.uuid.generateNilUuid,
				dojox.uuid.generateRandomUuid,
				dojox.uuid.generateTimeBasedUuid
			];
			
			for(var i in generators){
				var generator = generators[i];
				var uuidString = generator();

				doh.assertTrue((typeof uuidString) == 'string');
				dojox.uuid.tests.uuid.checkValidityOfUuidString(uuidString);

				var uuid = new dojox.uuid.Uuid(uuidString);
				if(generator != dojox.uuid.generateNilUuid){
					doh.assertTrue(uuid.getVariant() == dojox.uuid.variant.DCE);
				}

				doh.assertTrue(uuid.isEqual(uuid));
				doh.assertTrue(uuid.compare(uuid) == 0);
				doh.assertTrue(dojox.uuid.Uuid.compare(uuid, uuid) == 0);
				dojox.uuid.tests.uuid.checkValidityOfUuidString(uuid.toString());
				doh.assertTrue(uuid.toString().length == 36);
		
				if(generator != dojox.uuid.generateNilUuid){
					var uuidStringOne = generator();
					var uuidStringTwo = generator();
					doh.assertTrue(uuidStringOne != uuidStringTwo);
					
					dojox.uuid.Uuid.setGenerator(generator);
					var uuidOne = new dojox.uuid.Uuid();
					var uuidTwo = new dojox.uuid.Uuid();
					doh.assertTrue(generator === dojox.uuid.Uuid.getGenerator());
					dojox.uuid.Uuid.setGenerator(null);
					doh.assertTrue(uuidOne != uuidTwo);
					doh.assertTrue(!uuidOne.isEqual(uuidTwo));
					doh.assertTrue(!uuidTwo.isEqual(uuidOne));
					
					var oneVsTwo = dojox.uuid.Uuid.compare(uuidOne, uuidTwo); // either 1 or -1
					var twoVsOne = dojox.uuid.Uuid.compare(uuidTwo, uuidOne); // either -1 or 1
					doh.assertTrue(oneVsTwo + twoVsOne == 0);
					doh.assertTrue(oneVsTwo != 0);
					doh.assertTrue(twoVsOne != 0);
					
					doh.assertTrue(!uuidTwo.isEqual(uuidOne));
				}
				
				if(generator == dojox.uuid.generateRandomUuid){
					doh.assertTrue(uuid.getVersion() == dojox.uuid.version.RANDOM);
				}
				
				if(generator == dojox.uuid.generateTimeBasedUuid){
					dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(uuid.toString());
					doh.assertTrue(uuid.getVersion() == dojox.uuid.version.TIME_BASED);
					doh.assertTrue(dojo.isString(uuid.getNode()));
					doh.assertTrue(uuid.getNode().length == 12);
					var timestamp = uuid.getTimestamp();
					var date = uuid.getTimestamp(Date);
					var dateString = uuid.getTimestamp(String);
					var hexString = uuid.getTimestamp("hex");
					doh.assertTrue(date instanceof Date);
					doh.assertTrue(timestamp.valueOf() == date.valueOf());
					doh.assertTrue(hexString.length == 15);
				}
			}
		},
		
		function test_uuid_nilGenerator(){
			var nilUuidString = '00000000-0000-0000-0000-000000000000';
			var uuidString = dojox.uuid.generateNilUuid();
			doh.assertTrue(uuidString == nilUuidString);
		},
		
		function test_uuid_timeBasedGenerator(){
			var uuid;   // an instance of dojox.uuid.Uuid
			var string; // a simple string literal
			var generator = dojox.uuid.generateTimeBasedUuid;

			var string1 = generator();
			var uuid2    = new dojox.uuid.Uuid(generator());
			var string3 = generator("017bf397618a");         // hardwareNode
			var string4 = generator("f17bf397618a");         // pseudoNode
			var string5 = generator(new String("017BF397618A"));
			
			dojox.uuid.generateTimeBasedUuid.setNode("017bf397618a");
			var string6 = generator(); // the generated UUID has node == "017bf397618a"
			var uuid7   = new dojox.uuid.Uuid(generator()); // the generated UUID has node == "017bf397618a"
			var returnedNode = dojox.uuid.generateTimeBasedUuid.getNode();
			doh.assertTrue(returnedNode == "017bf397618a");
		
			function getNode(string){
				var arrayOfStrings = string.split('-');
				return arrayOfStrings[4];
			}
			dojox.uuid.tests.uuid.checkForPseudoNodeBitInTimeBasedUuidString(string1);
			dojox.uuid.tests.uuid.checkForPseudoNodeBitInTimeBasedUuidString(uuid2.toString());
			dojox.uuid.tests.uuid.checkForPseudoNodeBitInTimeBasedUuidString(string4);
			
			doh.assertTrue(getNode(string3) == "017bf397618a");
			doh.assertTrue(getNode(string4) == "f17bf397618a");
			doh.assertTrue(getNode(string5) == "017bf397618a");
			doh.assertTrue(getNode(string6) == "017bf397618a");
			doh.assertTrue(uuid7.getNode() == "017bf397618a");
			
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(string1);
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(uuid2.toString());
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(string3);
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(string4);
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(string5);
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(string6);
			dojox.uuid.tests.uuid.checkValidityOfTimeBasedUuidString(uuid7.toString());
		},

		function test_uuid_invalidUuids(){
			var uuidStrings = [];
			uuidStrings.push("Hello world!");                          // not a UUID
			uuidStrings.push("3B12F1DF-5232-1804-897E-917BF39761");    // too short
			uuidStrings.push("3B12F1DF-5232-1804-897E-917BF39761-8A"); // extra '-'
			uuidStrings.push("3B12F1DF-5232-1804-897E917BF39761-8A");  // last '-' in wrong place
			uuidStrings.push("HB12F1DF-5232-1804-897E-917BF397618A");  // "HB12F1DF" is not a hex string
		
			var numberOfFailures = 0;
			for(var i in uuidStrings){
				var uuidString = uuidStrings[i];
				try{
					new dojox.uuid.Uuid(uuidString);
				}catch (e){
					++numberOfFailures;
				}
			}
			doh.assertTrue(numberOfFailures == uuidStrings.length);
		}
	]
);



/*
function test_uuid_get64bitArrayFromFloat(){
	// summary:
	//		This is a test we'd like to be able to run, but we can't run it
	//		because it tests a function which is private in generateTimeBasedUuid
	var x = Math.pow(2, 63) + Math.pow(2, 15);
	var result = dojox.uuid.generateTimeBasedUuid._get64bitArrayFromFloat(x);
	doh.assertTrue(result[0] === 0x8000);
	doh.assertTrue(result[1] === 0x0000);
	doh.assertTrue(result[2] === 0x0000);
	doh.assertTrue(result[3] === 0x8000);

	var date = new Date();
	x = date.valueOf();
	result = dojox.uuid.generateTimeBasedUuid._get64bitArrayFromFloat(x);
	var reconstructedFloat = result[0];
	reconstructedFloat *= 0x10000;
	reconstructedFloat += result[1];
	reconstructedFloat *= 0x10000;
	reconstructedFloat += result[2];
	reconstructedFloat *= 0x10000;
	reconstructedFloat += result[3];

	doh.assertTrue(reconstructedFloat === x);
}

function test_uuid_addTwo64bitArrays(){
	// summary:
	//		This is a test we'd like to be able to run, but we can't run it
	//		because it tests a function which is private in generateTimeBasedUuid
	var a = [0x0000, 0x0000, 0x0000, 0x0001];
	var b = [0x0FFF, 0xFFFF, 0xFFFF, 0xFFFF];
	var result = dojox.uuid.generateTimeBasedUuid._addTwo64bitArrays(a, b);
	doh.assertTrue(result[0] === 0x1000);
	doh.assertTrue(result[1] === 0x0000);
	doh.assertTrue(result[2] === 0x0000);
	doh.assertTrue(result[3] === 0x0000);

	a = [0x4000, 0x8000, 0x8000, 0x8000];
	b = [0x8000, 0x8000, 0x8000, 0x8000];
	result = dojox.uuid.generateTimeBasedUuid._addTwo64bitArrays(a, b);
	doh.assertTrue(result[0] === 0xC001);
	doh.assertTrue(result[1] === 0x0001);
	doh.assertTrue(result[2] === 0x0001);
	doh.assertTrue(result[3] === 0x0000);

	a = [7, 6, 2, 5];
	b = [1, 0, 3, 4];
	result = dojox.uuid.generateTimeBasedUuid._addTwo64bitArrays(a, b);
	doh.assertTrue(result[0] === 8);
	doh.assertTrue(result[1] === 6);
	doh.assertTrue(result[2] === 5);
	doh.assertTrue(result[3] === 9);
}

function test_uuid_multiplyTwo64bitArrays(){
	// summary:
	//		This is a test we'd like to be able to run, but we can't run it
	//		because it tests a function which is private in generateTimeBasedUuid
	var a = [     0, 0x0000, 0x0000, 0x0003];
	var b = [0x1111, 0x1234, 0x0000, 0xFFFF];
	var result = dojox.uuid.generateTimeBasedUuid._multiplyTwo64bitArrays(a, b);
	doh.assertTrue(result[0] === 0x3333);
	doh.assertTrue(result[1] === 0x369C);
	doh.assertTrue(result[2] === 0x0002);
	doh.assertTrue(result[3] === 0xFFFD);

	a = [0, 0, 0, 5];
	b = [0, 0, 0, 4];
	result = dojox.uuid.generateTimeBasedUuid._multiplyTwo64bitArrays(a, b);
	doh.assertTrue(result[0] === 0);
	doh.assertTrue(result[1] === 0);
	doh.assertTrue(result[2] === 0);
	doh.assertTrue(result[3] === 20);

	a = [0, 0, 2, 5];
	b = [0, 0, 3, 4];
	result = dojox.uuid.generateTimeBasedUuid._multiplyTwo64bitArrays(a, b);
	doh.assertTrue(result[0] === 0);
	doh.assertTrue(result[1] === 6);
	doh.assertTrue(result[2] === 23);
	doh.assertTrue(result[3] === 20);
}
*/

}
