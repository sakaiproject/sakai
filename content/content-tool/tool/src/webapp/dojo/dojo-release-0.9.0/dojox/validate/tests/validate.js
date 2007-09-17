if(!dojo._hasResource["dojox.validate.tests.validate"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.validate.tests.validate"] = true;
dojo.provide("dojox.validate.tests.validate"); 

dojo.require("dojox.validate._base");
dojo.require("dojox.validate.check");
dojo.require("dojox.validate.us");
dojo.require("dojox.validate.ca"); 
dojo.require("dojox.validate.web");

tests.register("dojox.validate.tests.validate",
	[{
		name: "isText",
		runTest: function(tests){
			tests.t(dojox.validate.isText('            x'));
			tests.t(dojox.validate.isText('x             '));
			tests.t(dojox.validate.isText('        x     '));
			tests.f(dojox.validate.isText('   '));
			tests.f(dojox.validate.isText(''));
		
			// test lengths
			tests.t(dojox.validate.isText('123456', {length: 6} ));
			tests.f(dojox.validate.isText('1234567', {length: 6} ));
			tests.t(dojox.validate.isText('1234567', {minlength: 6} ));
			tests.t(dojox.validate.isText('123456', {minlength: 6} ));
			tests.f(dojox.validate.isText('12345', {minlength: 6} ));
			tests.f(dojox.validate.isText('1234567', {maxlength: 6} ));
			tests.t(dojox.validate.isText('123456', {maxlength: 6} ));
		}
	},
	{
		name: "isIpAddress",
		runTest: function(tests){	
			tests.t(dojox.validate.isIpAddress('24.17.155.40'));
			tests.f(dojox.validate.isIpAddress('024.17.155.040'));       
			tests.t(dojox.validate.isIpAddress('255.255.255.255'));       
			tests.f(dojox.validate.isIpAddress('256.255.255.255'));       
			tests.f(dojox.validate.isIpAddress('255.256.255.255'));       
			tests.f(dojox.validate.isIpAddress('255.255.256.255'));       
			tests.f(dojox.validate.isIpAddress('255.255.255.256'));       

			// test dotted hex       
			tests.t(dojox.validate.isIpAddress('0x18.0x11.0x9b.0x28'));       
			tests.f(dojox.validate.isIpAddress('0x18.0x11.0x9b.0x28', {allowDottedHex: false}) );       
			tests.t(dojox.validate.isIpAddress('0x18.0x000000011.0x9b.0x28'));       
			tests.t(dojox.validate.isIpAddress('0xff.0xff.0xff.0xff'));       
			tests.f(dojox.validate.isIpAddress('0x100.0xff.0xff.0xff'));       

			// test dotted octal       
			tests.t(dojox.validate.isIpAddress('0030.0021.0233.0050'));       
			tests.f(dojox.validate.isIpAddress('0030.0021.0233.0050', {allowDottedOctal: false}) );
			tests.t(dojox.validate.isIpAddress('0030.0000021.0233.00000050'));       
			tests.t(dojox.validate.isIpAddress('0377.0377.0377.0377'));       
			tests.f(dojox.validate.isIpAddress('0400.0377.0377.0377'));       
			tests.f(dojox.validate.isIpAddress('0377.0378.0377.0377'));       
			tests.f(dojox.validate.isIpAddress('0377.0377.0380.0377'));       
			tests.f(dojox.validate.isIpAddress('0377.0377.0377.377'));       
		
			// test decimal       
			tests.t(dojox.validate.isIpAddress('3482223595'));       
			tests.t(dojox.validate.isIpAddress('0'));       
			tests.t(dojox.validate.isIpAddress('4294967295'));       
			tests.f(dojox.validate.isIpAddress('4294967296'));       
			tests.f(dojox.validate.isIpAddress('3482223595', {allowDecimal: false}));       
		
			// test hex       
			tests.t(dojox.validate.isIpAddress('0xCF8E83EB'));       
			tests.t(dojox.validate.isIpAddress('0x0'));       
			tests.t(dojox.validate.isIpAddress('0x00ffffffff'));       
			tests.f(dojox.validate.isIpAddress('0x100000000'));
			tests.f(dojox.validate.isIpAddress('0xCF8E83EB', {allowHex: false}));       
			
			// IPv6       
			tests.t(dojox.validate.isIpAddress('fedc:BA98:7654:3210:FEDC:BA98:7654:3210'));       
			tests.t(dojox.validate.isIpAddress('1080:0:0:0:8:800:200C:417A'));
			tests.f(dojox.validate.isIpAddress('1080:0:0:0:8:800:200C:417A', {allowIPv6: false}));
		
			// Hybrid of IPv6 and IPv4
			tests.t(dojox.validate.isIpAddress('0:0:0:0:0:0:13.1.68.3'));
			tests.t(dojox.validate.isIpAddress('0:0:0:0:0:FFFF:129.144.52.38'));
			tests.f(dojox.validate.isIpAddress('0:0:0:0:0:FFFF:129.144.52.38', {allowHybrid: false}));
		}
	},
	{
		name:"isUrl",
		runTests: function(tests){ 

			tests.t(dojox.validate.isUrl('www.yahoo.com'));
			tests.t(dojox.validate.isUrl('http://www.yahoo.com'));
			tests.t(dojox.validate.isUrl('https://www.yahoo.com'));
			tests.f(dojox.validate.isUrl('http://.yahoo.com'));
			tests.f(dojox.validate.isUrl('http://www.-yahoo.com'));
			tests.f(dojox.validate.isUrl('http://www.yahoo-.com'));
			tests.t(dojox.validate.isUrl('http://y-a---h-o-o.com'));
			tests.t(dojox.validate.isUrl('http://www.y.com'));
			tests.t(dojox.validate.isUrl('http://www.yahoo.museum'));
			tests.t(dojox.validate.isUrl('http://www.yahoo.co.uk'));
			tests.f(dojox.validate.isUrl('http://www.micro$oft.com'));
		
			tests.t(dojox.validate.isUrl('http://www.y.museum:8080'));
			tests.t(dojox.validate.isUrl('http://12.24.36.128:8080'));
			tests.f(dojox.validate.isUrl('http://12.24.36.128:8080', {allowIP: false} ));
			tests.t(dojox.validate.isUrl('www.y.museum:8080'));
			tests.f(dojox.validate.isUrl('www.y.museum:8080', {scheme: true} ));
			tests.t(dojox.validate.isUrl('localhost:8080', {allowLocal: true} ));
			tests.f(dojox.validate.isUrl('localhost:8080', {} ));
			tests.t(dojox.validate.isUrl('http://www.yahoo.com/index.html?a=12&b=hello%20world#anchor'));
			tests.f(dojox.validate.isUrl('http://www.yahoo.xyz'));
			tests.t(dojox.validate.isUrl('http://www.yahoo.com/index.html#anchor'));
			tests.t(dojox.validate.isUrl('http://cocoon.apache.org/2.1/'));
		}
	},
	{
		name: "isEmailAddress",
		runTests: function(tests) {
			tests.t(dojox.validate.isEmailAddress('x@yahoo.com'));
			tests.t(dojox.validate.isEmailAddress('x.y.z.w@yahoo.com'));
			tests.f(dojox.validate.isEmailAddress('x..y.z.w@yahoo.com'));
			tests.f(dojox.validate.isEmailAddress('x.@yahoo.com'));
			tests.t(dojox.validate.isEmailAddress('x@z.com'));
			tests.f(dojox.validate.isEmailAddress('x@yahoo.x'));
			tests.t(dojox.validate.isEmailAddress('x@yahoo.museum'));
			tests.t(dojox.validate.isEmailAddress("o'mally@yahoo.com"));
			tests.f(dojox.validate.isEmailAddress("'mally@yahoo.com"));
			tests.t(dojox.validate.isEmailAddress("fred&barney@stonehenge.com"));
			tests.f(dojox.validate.isEmailAddress("fred&&barney@stonehenge.com"));
		
			// local addresses
			tests.t(dojox.validate.isEmailAddress("fred&barney@localhost", {allowLocal: true} ));
			tests.f(dojox.validate.isEmailAddress("fred&barney@localhost"));
		
			// addresses with cruft
			tests.t(dojox.validate.isEmailAddress("mailto:fred&barney@stonehenge.com", {allowCruft: true} ));
			tests.t(dojox.validate.isEmailAddress("<fred&barney@stonehenge.com>", {allowCruft: true} ));
			tests.f(dojox.validate.isEmailAddress("mailto:fred&barney@stonehenge.com"));
			tests.f(dojox.validate.isEmailAddress("<fred&barney@stonehenge.com>"));
		
			// local addresses with cruft
			tests.t(dojox.validate.isEmailAddress("<mailto:fred&barney@localhost>", {allowLocal: true, allowCruft: true} ));
			tests.f(dojox.validate.isEmailAddress("<mailto:fred&barney@localhost>", {allowCruft: true} ));
			tests.f(dojox.validate.isEmailAddress("<mailto:fred&barney@localhost>", {allowLocal: true} ));
		}
	},
	{
		name: "isEmailsAddressList",
		runTests: function(tests) {
			tests.t(dojox.validate.isEmailAddressList(
				"x@yahoo.com \n x.y.z.w@yahoo.com ; o'mally@yahoo.com , fred&barney@stonehenge.com \n" )
			);
			tests.t(dojox.validate.isEmailAddressList(
				"x@yahoo.com \n x.y.z.w@localhost \n o'mally@yahoo.com \n fred&barney@localhost", 
				{allowLocal: true} )
			);
			tests.f(dojox.validate.isEmailAddressList(
				"x@yahoo.com; x.y.z.w@localhost; o'mally@yahoo.com; fred&barney@localhost", {listSeparator: ";"} )
			);
			tests.t(dojox.validate.isEmailAddressList(
					"mailto:x@yahoo.com; <x.y.z.w@yahoo.com>; <mailto:o'mally@yahoo.com>; fred&barney@stonehenge.com", 
					{allowCruft: true, listSeparator: ";"} )
			);
			tests.f(dojox.validate.isEmailAddressList(
					"mailto:x@yahoo.com; <x.y.z.w@yahoo.com>; <mailto:o'mally@yahoo.com>; fred&barney@stonehenge.com", 
					{listSeparator: ";"} )
			);
			tests.t(dojox.validate.isEmailAddressList(
					"mailto:x@yahoo.com; <x.y.z.w@localhost>; <mailto:o'mally@localhost>; fred&barney@localhost", 
					{allowLocal: true, allowCruft: true, listSeparator: ";"} )
			);
		}
	},
	{
		name: "getEmailAddressList",
		runTests: function(tests) {
			var list = "x@yahoo.com \n x.y.z.w@yahoo.com ; o'mally@yahoo.com , fred&barney@stonehenge.com";
			tests.assertEquals(4, dojox.validate.getEmailAddressList(list).length);

			var localhostList = "x@yahoo.com; x.y.z.w@localhost; o'mally@yahoo.com; fred&barney@localhost";
			tests.assertEquals(0, dojox.validate.getEmailAddressList(localhostList).length);
			tests.assertEquals(4, dojox.validate.getEmailAddressList(localhostList, {allowLocal: true} ).length);
		}
	},
	{
		name: "isInRange",
		runTests: function(tests) {
			// test integers
			tests.f(dojox.validate.isInRange( '0', {min: 1, max: 100} ));
			tests.t(dojox.validate.isInRange( '1', {min: 1, max: 100} ));
			tests.f(dojox.validate.isInRange( '-50', {min: 1, max: 100} ));
			tests.t(dojox.validate.isInRange( '+50', {min: 1, max: 100} ));
			tests.t(dojox.validate.isInRange( '100', {min: 1, max: 100} ));
			tests.f(dojox.validate.isInRange( '101', {min: 1, max: 100} ));
		
			//test real numbers
			tests.f(dojox.validate.isInRange( '0.9', {min: 1.0, max: 10.0} ));
			tests.t(dojox.validate.isInRange( '1.0', {min: 1.0, max: 10.0} ));
			tests.f(dojox.validate.isInRange( '-5.0', {min: 1.0, max: 10.0} ));
			tests.t(dojox.validate.isInRange( '+5.50', {min: 1.0, max: 10.0} ));
			tests.t(dojox.validate.isInRange( '10.0', {min: 1.0, max: 10.0} ));
			tests.f(dojox.validate.isInRange( '10.1', {min: 1.0, max: 10.0} ));
			tests.f(dojox.validate.isInRange( '5.566e28', {min: 5.567e28, max: 6.000e28} ));
			tests.t(dojox.validate.isInRange( '5.7e28', {min: 5.567e28, max: 6.000e28} ));
			tests.f(dojox.validate.isInRange( '6.00000001e28', {min: 5.567e28, max: 6.000e28} ));
			tests.f(dojox.validate.isInRange( '10.000.000,12345e-5', {decimal: ",", max: 10000000.1e-5} ));
			tests.f(dojox.validate.isInRange( '10.000.000,12345e-5', {decimal: ",", min: 10000000.2e-5} ));
			tests.t(dojox.validate.isInRange('1,500,000', {separator: ',', min: 0}));
			tests.f(dojox.validate.isInRange('1,500,000', {separator: ',', min: 1000, max: 20000}));
		
			// test currency
			tests.f(dojox.validate.isInRange('\u20AC123,456,789', {max: 123456788, symbol: '\u20AC'} ));
			tests.f(dojox.validate.isInRange('\u20AC123,456,789', { min: 123456790, symbol: '\u20AC'} ));
			tests.f(dojox.validate.isInRange('$123,456,789.07', { max: 123456789.06} ));
			tests.f(dojox.validate.isInRange('$123,456,789.07', { min: 123456789.08} ));
			tests.f(dojox.validate.isInRange('123.456.789,00 \u20AC',  {max: 123456788, decimal: ",", symbol: '\u20AC'} ));
			tests.f(dojox.validate.isInRange('123.456.789,00 \u20AC',  {min: 123456790, decimal: ",", symbol: '\u20AC'} ));
			tests.f(dojox.validate.isInRange('- T123 456 789-00', {decimal: "-", min:0} ));
			tests.t(dojox.validate.isInRange('\u20AC123,456,789', { max: 123456790, symbol: '\u20AC'} ));
			tests.t(dojox.validate.isInRange('$123,456,789.07', { min: 123456789.06} ));
		
			// test non number
			//tests.f("test25", dojox.validate.isInRange( 'a'));
		}
	},
	{	
		name: "isUsPhoneNumber",
		runTests: function(tests) {
			tests.t(dojox.validate.us.isPhoneNumber('(111) 111-1111'));
			tests.t(dojox.validate.us.isPhoneNumber('(111) 111 1111'));
			tests.t(dojox.validate.us.isPhoneNumber('111 111 1111'));
			tests.t(dojox.validate.us.isPhoneNumber('111.111.1111'));
			tests.t(dojox.validate.us.isPhoneNumber('111-111-1111'));
			tests.t(dojox.validate.us.isPhoneNumber('111/111-1111'));
			tests.f(dojox.validate.us.isPhoneNumber('111 111-1111'));
			tests.f(dojox.validate.us.isPhoneNumber('111-1111'));
			tests.f(dojox.validate.us.isPhoneNumber('(111)-111-1111'));
		
			// test extensions
			tests.t(dojox.validate.us.isPhoneNumber('111-111-1111 x1'));
			tests.t(dojox.validate.us.isPhoneNumber('111-111-1111 x12'));
			tests.t(dojox.validate.us.isPhoneNumber('111-111-1111 x1234'));
		}
	},
	{
		name:"isUsSocialSecurityNumber",
		runtests: function(tests) {
			tests.t(dojox.validate.us.isSocialSecurityNumber('123-45-6789'));
			tests.t(dojox.validate.us.isSocialSecurityNumber('123 45 6789'));
			tests.t(dojox.validate.us.isSocialSecurityNumber('123456789'));
			tests.f(dojox.validate.us.isSocialSecurityNumber('123-45 6789'));
			tests.f(dojox.validate.us.isSocialSecurityNumber('12345 6789'));
			tests.f(dojox.validate.us.isSocialSecurityNumber('123-456789'));
		}
	},
	{
		name:"isUsZipCode",
		runtests: function(tests) {
			tests.t(dojox.validate.us.isZipCode('12345-6789'));
			tests.t(dojox.validate.us.isZipCode('12345 6789'));
			tests.t(dojox.validate.us.isZipCode('123456789'));
			tests.t(dojox.validate.us.isZipCode('12345'));
		}
	},
	{
		name:"isCaZipCode",
		runtests: function(tests) {
			tests.t(dojox.validate.ca.isPostalCode('A1Z 3F3'));
			tests.f(dojox.validate.ca.isPostalCode('1AZ 3F3'));
			tests.t(dojox.validate.ca.isPostalCode('a1z 3f3'));
			tests.f(dojox.validate.ca.isPostalCode('xxxxxx'));
			tests.t(dojox.validate.ca.isPostalCode('A1Z3F3')); 
			
		}
	},
	{
		name:"isUsState",
		runtests: function(tests) {
			tests.t(dojox.validate.us.isState('CA'));
			tests.t(dojox.validate.us.isState('ne'));
			tests.t(dojox.validate.us.isState('PR'));
			tests.f(dojox.validate.us.isState('PR', {allowTerritories: false} ));
			tests.t(dojox.validate.us.isState('AA'));
			tests.f(dojox.validate.us.isState('AA', {allowMilitary: false} ));
		}
	},
	{
		name:"formCheck",
		runtests: function(tests) {
			var f = {
				// textboxes
				tx1: {type: "text", value: " 1001 ",  name: "tx1"},
				tx2: {type: "text", value: " x",  name: "tx2"},
				tx3: {type: "text", value: "10/19/2005",  name: "tx3"},
				tx4: {type: "text", value: "10/19/2005",  name: "tx4"},
				tx5: {type: "text", value: "Foo@Localhost",  name: "tx5"},
				tx6: {type: "text", value: "Foo@Localhost",  name: "tx6"},
				tx7: {type: "text", value: "<Foo@Gmail.Com>",  name: "tx7"},
				tx8: {type: "text", value: "   ",  name: "tx8"},
				tx9: {type: "text", value: "ca",  name: "tx9"},
				tx10: {type: "text", value: "homer SIMPSON",  name: "tx10"},
				tx11: {type: "text", value: "$1,000,000 (US)",  name: "tx11"},
				tx12: {type: "text", value: "as12.a13", name: "tx12"},
				tx13: {type: "text", value: "4.13", name: "tx13"},
				tx14: {type: "text", value: "15.681", name: "tx14"},
				tx15: {value: "1", name: "tx15"},
				cc_no: {type: "text", value: "5434 1111 1111 1111",  name: "cc_no"},
				cc_exp: {type: "text", value: "",  name: "cc_exp"},
				cc_type: {type: "text", value: "Visa",  name: "cc_type"},
				email: {type: "text", value: "foo@gmail.com",  name: "email"},
				email_confirm: {type: "text", value: "foo2@gmail.com",  name: "email_confirm"},
				// password
				pw1: {type: "password", value: "123456",  name: "pw1"},
				pw2: {type: "password", value: "123456",  name: "pw2"},
				// textarea - they have a type property, even though no html attribute
				ta1: {type: "textarea", value: "",  name: "ta1"},
				ta2: {type: "textarea", value: "",  name: "ta2"},
				// radio button groups
				rb1: [
					{type: "radio", value: "v0",  name: "rb1", checked: false},
					{type: "radio", value: "v1",  name: "rb1", checked: false},
					{type: "radio", value: "v2",  name: "rb1", checked: true}
				],
				rb2: [
					{type: "radio", value: "v0",  name: "rb2", checked: false},
					{type: "radio", value: "v1",  name: "rb2", checked: false},
					{type: "radio", value: "v2",  name: "rb2", checked: false}
				],
				rb3: [
					{type: "radio", value: "v0",  name: "rb3", checked: false},
					{type: "radio", value: "v1",  name: "rb3", checked: false},
					{type: "radio", value: "v2",  name: "rb3", checked: false}
				],
				// checkboxes
				cb1: {type: "checkbox", value: "cb1",  name: "cb1", checked: false},
				cb2: {type: "checkbox", value: "cb2",  name: "cb2", checked: false},
				// checkbox group with the same name
				cb3: [
					{type: "checkbox", value: "v0",  name: "cb3", checked: false},
					{type: "checkbox", value: "v1",  name: "cb3", checked: false},
					{type: "checkbox", value: "v2",  name: "cb3", checked: false}
				],
				doubledip: [
					{type: "checkbox", value: "vanilla",  name: "doubledip", checked: false},
					{type: "checkbox", value: "chocolate",  name: "doubledip", checked: false},
					{type: "checkbox", value: "chocolate chip",  name: "doubledip", checked: false},
					{type: "checkbox", value: "lemon custard",  name: "doubledip", checked: true},
					{type: "checkbox", value: "pistachio almond",  name: "doubledip", checked: false}
				],		
				// <select>
				s1: {
					type: "select-one", 
					name: "s1",
					selectedIndex: -1,
					options: [
						{text: "option 1", value: "v0", selected: false},
						{text: "option 2", value: "v1", selected: false},
						{text: "option 3", value: "v2", selected: false}
					]
				},
				// <select multiple>
				s2: {
					type: "select-multiple", 
					name: "s2",
					selectedIndex: 1,
					options: [
						{text: "option 1", value: "v0", selected: false},
						{text: "option 2", value: "v1", selected: true},
						{text: "option 3", value: "v2", selected: true}
					]
				},
				tripledip: {
					type: "select-multiple", 
					name: "tripledip",
					selectedIndex: 3,
					options: [
						{text: "option 1", value: "vanilla", selected: false},
						{text: "option 2", value: "chocolate", selected: false},
						{text: "option 3", value: "chocolate chip", selected: false},
						{text: "option 4", value: "lemon custard", selected: true},
						{text: "option 5", value: "pistachio almond", selected: true},
						{text: "option 6", value: "mocha almond chip", selected: false}
					]
				},
				doublea: {
					type: "select-multiple", 
					name: "doublea",
					selectedIndex: 2,
					options: [
						{text: "option 1", value: "vanilla", selected: false},
						{text: "option 2", value: "chocolate", selected: true},
						{text: "option 3", value: "", selected: true}
					]
				},
				// <select> null selection
				s3: {
					type: "select-one", 
					name: "s3",
					selectedIndex: 0,
					options: [
						{text: "option 1", value: "", selected: true},
						{text: "option 2", value: "v1", selected: false},
						{text: "option 3", value: "v2", selected: false}
					]
				},
				selectAlien: {
					name: "selectAlien",
					multiple: "multiple",
					id: "selectAlient",
					size: "10",
					length: 0,
					options: [],
					value:[]
				}
			};
		
			// Profile for form input
			var profile = {
				// filters
				trim: ["tx1", "tx2"],
				uppercase: ["tx9"],
				lowercase: ["tx5", "tx6", "tx7"],
				ucfirst: ["tx10"],
				digit: ["tx11"],
				// required fields
				required: ["tx2", "tx3", "tx4", "tx5", "tx6", "tx7", "tx8", "tx15", "pw1", "ta1", "rb1", "rb2", 
							"cb3", "s1", "s2", "s3",
					{"doubledip":2}, {"tripledip":3}, {"doublea":2} ],
				// dependant/conditional fields
				dependencies:	{
					cc_exp: "cc_no",
					cc_type: "cc_no"
				},
				// validated fields
				constraints: {
					tx1: dojox.validate.isInteger,
					tx2: dojox.validate.isInteger,
					tx3: [dojo.date.parse, {locale: 'en-us'}],
					tx4: [dojo.date.parse, {locale: 'fr-fr'}],
					tx5: [dojox.validate.isEmailAddress],
					tx6: [dojox.validate.isEmailAddress, {allowLocal: true}],
					tx7: [dojox.validate.isEmailAddress, {allowCruft: true}],
					tx8: dojox.validate.isURL,
					tx12: [[dojox.validate.isRealNumber],[dojox.validate.isInRange, {max:100.00,min:5.0}]],
					tx13: [[dojox.validate.isRealNumber],[dojox.validate.isInRange, {max:100.00,min:5.0}]],
					tx14: [[dojox.validate.isRealNumber],[dojox.validate.isInRange, {max:100.00,min:5.0}]]
				},
				// confirm fields
				confirm: {
					email_confirm: "email",	
					pw2: "pw1"
				}
			};
		
			// results object
			var results = dojox.validate.check(f, profile);
		
			// test filter stuff
			tests.asserEquals("1001", f.tx1.value );
			tests.asserEquals("x", f.tx2.value );
			tests.asserEquals("CA", f.tx9.value );
			tests.asserEquals("foo@localhost", f.tx5.value );
			tests.asserEquals("foo@localhost", f.tx6.value );
			tests.asserEquals("<foo@gmail.com>", f.tx7.value );
			tests.asserEquals("Homer Simpson", f.tx10.value );
			tests.asserEquals("1000000", f.tx11.value );
		
			// test missing stuff
			tests.f(results.isSuccessful() );
			tests.t(results.hasMissing() );
			tests.f(results.isMissing("tx1") );
			tests.f(results.isMissing("tx2") );
			tests.f(results.isMissing("tx3") );
			tests.f(results.isMissing("tx4") );
			tests.f(results.isMissing("tx5") );
			tests.f(results.isMissing("tx6") );
			tests.f(results.isMissing("tx7") );
			tests.t(results.isMissing("tx8") );
			tests.f(results.isMissing("pw1") );
			tests.f(results.isMissing("pw2") );
			tests.t(results.isMissing("ta1") );
			tests.f(results.isMissing("ta2") );
			tests.f(results.isMissing("rb1") );
			tests.t(results.isMissing("rb2") );
			tests.f(results.isMissing("rb3") );
			tests.t(results.isMissing("cb3") );
			tests.t(results.isMissing("s1") );
			tests.f(results.isMissing("s2") );
			tests.t(results.isMissing("s3"));
			tests.t(results.isMissing("doubledip") );
			tests.t(results.isMissing("tripledip") );
			tests.t(results.isMissing("doublea"));
			tests.f(results.isMissing("cc_no") );
			tests.t(results.isMissing("cc_exp") );
			tests.f(results.isMissing("cc_type") );
			// missing: tx8, ta1, rb2, cb3, s1, s3, doubledip, tripledip, cc_exp
			tests.asserEquals(10, results.getMissing().length );
		
			// test constraint stuff
			tests.t(results.hasInvalid() );
			tests.f(results.isInvalid("tx1") );
			tests.t(results.isInvalid("tx2") );
			tests.f(results.isInvalid("tx3") );
			tests.t(results.isInvalid("tx4") );
			tests.t(results.isInvalid("tx5") );
			tests.f(results.isInvalid("tx6") );
			tests.f(results.isInvalid("tx7") );
			tests.f(results.isInvalid("tx8") );
			tests.f(results.isInvalid("pw1") );
			tests.f(results.isInvalid("pw2") );
			tests.f(results.isInvalid("ta1") );
			tests.f(results.isInvalid("ta2") );
			tests.f(results.isInvalid("email") );
			tests.t(results.isInvalid("email_confirm") );
			
			// invlaid: txt2, txt4, txt5, email_confirm, selectAlien
			
			tests.asserEquals(7, results.getInvalid().length);
			tests.t(results.isInvalid("tx12"));
			tests.t(results.isInvalid("tx13"));
			tests.f(results.isInvalid("tx14"));
			tests.t(results.isInvalid("selectAlien"));
		}
	}
]);

}
