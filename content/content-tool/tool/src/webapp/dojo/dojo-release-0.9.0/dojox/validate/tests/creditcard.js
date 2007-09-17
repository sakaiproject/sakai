if(!dojo._hasResource["dojox.validate.tests.creditcard"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.validate.tests.creditcard"] = true;
dojo.provide("dojox.validate.tests.creditcard");
dojo.require("dojox.validate.creditCard");

tests.register("dojox.validate.tests.creditcard",
	[{
		name:"isValidLuhn",
		runTests: function(tests) { 
			tests.t(dojox.validate.isValidLuhn('5105105105105100')); //test string input
			tests.t(dojox.validate.isValidLuhn('5105-1051 0510-5100')); //test string input with dashes and spaces (commonly used when entering card #'s)
			tests.t(dojox.validate.isValidLuhn(38520000023237)); //test numerical input as well
			tests.f(dojox.validate.isValidLuhn(3852000002323)); //testing failures
			tests.t(dojox.validate.isValidLuhn(18)); //length doesnt matter
			tests.f(dojox.validate.isValidLuhn(818181)); //short length failure
		}
	},
	{
		name:"isValidCvv", 
		runTests: function(tests) {
			tests.t(dojox.validate.isValidCvv('123','mc')); //string is ok
			tests.f(dojox.validate.isValidCvv('5AA','ec')); //invalid characters are not ok
			tests.t(dojox.validate.isValidCvv(723,'mc')); //numbers are ok too
			tests.f(dojox.validate.isValidCvv(7234,'mc')); //too long
			tests.t(dojox.validate.isValidCvv(612,'ec'));
			tests.t(dojox.validate.isValidCvv(421,'vi'));
			tests.t(dojox.validate.isValidCvv(543,'di'));
			tests.t(dojox.validate.isValidCvv('1234','ax')); 
			tests.t(dojox.validate.isValidCvv(4321,'ax'));
			tests.f(dojox.validate.isValidCvv(43215,'ax')); //too long
			tests.f(dojox.validate.isValidCvv(215,'ax')); //too short
		}
	},
	{
		name:"isValidCreditCard",
		runTests: function(tests) {
			//misc checks
			tests.t(dojox.validate.isValidCreditCard('5105105105105100','mc')); //test string input
			tests.t(dojox.validate.isValidCreditCard('5105-1051 0510-5100','mc')); //test string input with dashes and spaces (commonly used when entering card #'s)
			tests.t(dojox.validate.isValidCreditCard(5105105105105100,'mc')); //test numerical input as well
			tests.f(dojox.validate.isValidCreditCard('5105105105105100','vi')); //fails, wrong card type
			//Mastercard/Eurocard checks
			tests.t(dojox.validate.isValidCreditCard('5105105105105100','mc'));
			tests.t(dojox.validate.isValidCreditCard('5204105105105100','ec'));
			tests.t(dojox.validate.isValidCreditCard('5303105105105100','mc'));
			tests.t(dojox.validate.isValidCreditCard('5402105105105100','ec'));
			tests.t(dojox.validate.isValidCreditCard('5501105105105100','mc'));
			//Visa card checks
			tests.t(dojox.validate.isValidCreditCard('4111111111111111','vi'));
			tests.t(dojox.validate.isValidCreditCard('4111111111010','vi'));
			//American Express card checks
			tests.t(dojox.validate.isValidCreditCard('378 2822 4631 0005','ax'));
			tests.t(dojox.validate.isValidCreditCard('341-1111-1111-1111','ax'));
			//Diners Club/Carte Blanch card checks
			tests.t(dojox.validate.isValidCreditCard('36400000000000','dc'));
			tests.t(dojox.validate.isValidCreditCard('38520000023237','bl'));
			tests.t(dojox.validate.isValidCreditCard('30009009025904','dc'));
			tests.t(dojox.validate.isValidCreditCard('30108009025904','bl'));
			tests.t(dojox.validate.isValidCreditCard('30207009025904','dc'));
			tests.t(dojox.validate.isValidCreditCard('30306009025904','bl'));
			tests.t(dojox.validate.isValidCreditCard('30405009025904','dc'));
			tests.t(dojox.validate.isValidCreditCard('30504009025904','bl'));
			//Discover card checks
			tests.t(dojox.validate.isValidCreditCard('6011111111111117','di'));
			//JCB card checks
			tests.t(dojox.validate.isValidCreditCard('3530111333300000','jcb'));
			tests.t(dojox.validate.isValidCreditCard('213100000000001','jcb'));
			tests.t(dojox.validate.isValidCreditCard('180000000000002','jcb'));
			tests.f(dojox.validate.isValidCreditCard('1800000000000002','jcb')); //should fail, good checksum, good prefix, but wrong length'
			//Enroute card checks
			tests.t(dojox.validate.isValidCreditCard('201400000000000','er'));
			tests.t(dojox.validate.isValidCreditCard('214900000000000','er')); 
		}
	},
	{
		name:"isValidCreditCardNumber",
		runTests: function(tests) {
			//misc checks
			tests.t(dojox.validate.isValidCreditCardNumber('5105105105105100','mc')); //test string input
			tests.t(dojox.validate.isValidCreditCardNumber('5105-1051 0510-5100','mc')); //test string input with dashes and spaces (commonly used when entering card #'s)
			tests.t(dojox.validate.isValidCreditCardNumber(5105105105105100,'mc')); //test numerical input as well
			tests.f(dojox.validate.isValidCreditCardNumber('5105105105105100','vi')); //fails, wrong card type
			//Mastercard/Eurocard checks
			tests.is("mc|ec", dojox.validate.isValidCreditCardNumber('5100000000000000')); //should match 'mc|ec'
			tests.is("mc|ec", dojox.validate.isValidCreditCardNumber('5200000000000000')); //should match 'mc|ec'
			tests.is("mc|ec", dojox.validate.isValidCreditCardNumber('5300000000000000')); //should match 'mc|ec'
			tests.is("mc|ec", dojox.validate.isValidCreditCardNumber('5400000000000000')); //should match 'mc|ec'
			tests.is("mc|ec", dojox.validate.isValidCreditCardNumber('5500000000000000')); //should match 'mc|ec'
			tests.f(dojox.validate.isValidCreditCardNumber('55000000000000000')); //should fail, too long
			//Visa card checks
			tests.is("vi", dojox.validate.isValidCreditCardNumber('4111111111111111')); //should match 'vi'
			tests.is("vi", dojox.validate.isValidCreditCardNumber('4111111111010')); //should match 'vi'
			//American Express card checks
			tests.is("ax", dojox.validate.isValidCreditCardNumber('378 2822 4631 0005')); //should match 'ax'
			tests.is("ax", dojox.validate.isValidCreditCardNumber('341-1111-1111-1111')); //should match 'ax'
			//Diners Club/Carte Blanch card checks
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('36400000000000')); //should match 'dc|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('38520000023237')); //should match 'dc|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('30009009025904')); //should match 'di|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('30108009025904')); //should match 'di|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('30207009025904')); //should match 'di|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('30306009025904')); //should match 'di|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('30405009025904')); //should match 'di|bl'
			tests.is("dc|bl", dojox.validate.isValidCreditCardNumber('30504009025904')); //should match 'di|bl'
			//Discover card checks
			tests.is("di", dojox.validate.isValidCreditCardNumber('6011111111111117')); //should match 'di'
			//JCB card checks
			tests.is("jcb", dojox.validate.isValidCreditCardNumber('3530111333300000')); //should match 'jcb'
			tests.is("jcb", dojox.validate.isValidCreditCardNumber('213100000000001')); //should match 'jcb'
			tests.is("jcb", dojox.validate.isValidCreditCardNumber('180000000000002')); //should match 'jcb'
			tests.f(dojox.validate.isValidCreditCardNumber('1800000000000002')); //should fail, good checksum, good prefix, but wrong length'
			//Enroute card checks
			tests.is("er", dojox.validate.isValidCreditCardNumber('201400000000000')); //should match 'er'
			tests.is("er", dojox.validate.isValidCreditCardNumber('214900000000000')); //should match 'er'
		}
	}
]);

}
