// FIXME: need to add async tests
// FIXME: need to handle URL wrapping and test registration/running from URLs

// package system gunk. 
try{
	dojo.provide("doh.runner");
}catch(e){
	if(!this["doh"]){
		doh = {};
	}
}

//
// Utility Functions and Classes
//

doh.selfTest = false;

doh.hitch = function(/*Object*/thisObject, /*Function|String*/method /*, ...*/){
	var args = [];
	for(var x=2; x<arguments.length; x++){
		args.push(arguments[x]);
	}
	var fcn = ((typeof method == "string") ? thisObject[method] : method) || function(){};
	return function(){
		var ta = args.concat([]); // make a copy
		for(var x=0; x<arguments.length; x++){
			ta.push(arguments[x]);
		}
		return fcn.apply(thisObject, ta); // Function
	};
}

doh._mixin = function(/*Object*/ obj, /*Object*/ props){
	// summary:
	//		Adds all properties and methods of props to obj. This addition is
	//		"prototype extension safe", so that instances of objects will not
	//		pass along prototype defaults.
	var tobj = {};
	for(var x in props){
		// the "tobj" condition avoid copying properties in "props"
		// inherited from Object.prototype.  For example, if obj has a custom
		// toString() method, don't overwrite it with the toString() method
		// that props inherited from Object.protoype
		if((typeof tobj[x] == "undefined") || (tobj[x] != props[x])){
			obj[x] = props[x];
		}
	}
	// IE doesn't recognize custom toStrings in for..in
	if(	this["document"] 
		&& document.all
		&& (typeof props["toString"] == "function")
		&& (props["toString"] != obj["toString"])
		&& (props["toString"] != tobj["toString"])
	){
		obj.toString = props.toString;
	}
	return obj; // Object
}

doh.mixin = function(/*Object*/obj, /*Object...*/props){
	// summary:	Adds all properties and methods of props to obj. 
	for(var i=1, l=arguments.length; i<l; i++){
		doh._mixin(obj, arguments[i]);
	}
	return obj; // Object
}

doh.extend = function(/*Object*/ constructor, /*Object...*/ props){
	// summary:
	//		Adds all properties and methods of props to constructor's
	//		prototype, making them available to all instances created with
	//		constructor.
	for(var i=1, l=arguments.length; i<l; i++){
		doh._mixin(constructor.prototype, arguments[i]);
	}
	return constructor; // Object
}


doh._line = "------------------------------------------------------------";

/*
doh._delegate = function(obj, props){
	// boodman-crockford delegation
	function TMP(){};
	TMP.prototype = obj;
	var tmp = new TMP();
	if(props){
		dojo.lang.mixin(tmp, props);
	}
	return tmp;
}
*/

doh.debug = function(){
	// summary:
	//		takes any number of arguments and sends them to whatever debugging
	//		or logging facility is available in this environment

	// YOUR TEST RUNNER NEEDS TO IMPLEMENT THIS
}

doh._AssertFailure = function(msg){
	// idea for this as way of dis-ambiguating error types is from JUM. 
	// The JUM is dead! Long live the JUM!

	if(!(this instanceof doh._AssertFailure)){
		return new doh._AssertFailure(msg);
	}
	this.message = new String(msg||"");
	return this;
}
doh._AssertFailure.prototype = new Error();
doh._AssertFailure.prototype.constructor = doh._AssertFailure;
doh._AssertFailure.prototype.name = "doh._AssertFailure";

doh.Deferred = function(canceller){
	this.chain = [];
	this.id = this._nextId();
	this.fired = -1;
	this.paused = 0;
	this.results = [null, null];
	this.canceller = canceller;
	this.silentlyCancelled = false;
};

doh.extend(doh.Deferred, {
	getTestCallback: function(cb, scope){
		var _this = this;
		return function(){
			try{
				cb.apply(scope||dojo.global||_this, arguments);
			}catch(e){
				_this.errback(e);
				return;
			}
			_this.callback(true);
		}
	},

	getFunctionFromArgs: function(){
		var a = arguments;
		if((a[0])&&(!a[1])){
			if(typeof a[0] == "function"){
				return a[0];
			}else if(typeof a[0] == "string"){
				return dj_global[a[0]];
			}
		}else if((a[0])&&(a[1])){
			return doh.hitch(a[0], a[1]);
		}
		return null;
	},

	makeCalled: function() {
		var deferred = new doh.Deferred();
		deferred.callback();
		return deferred;
	},

	_nextId: (function(){
		var n = 1;
		return function(){ return n++; };
	})(),

	cancel: function(){
		if(this.fired == -1){
			if (this.canceller){
				this.canceller(this);
			}else{
				this.silentlyCancelled = true;
			}
			if(this.fired == -1){
				this.errback(new Error("Deferred(unfired)"));
			}
		}else if(	(this.fired == 0)&&
					(this.results[0] instanceof doh.Deferred)){
			this.results[0].cancel();
		}
	},
			

	_pause: function(){
		this.paused++;
	},

	_unpause: function(){
		this.paused--;
		if ((this.paused == 0) && (this.fired >= 0)) {
			this._fire();
		}
	},

	_continue: function(res){
		this._resback(res);
		this._unpause();
	},

	_resback: function(res){
		this.fired = ((res instanceof Error) ? 1 : 0);
		this.results[this.fired] = res;
		this._fire();
	},

	_check: function(){
		if(this.fired != -1){
			if(!this.silentlyCancelled){
				throw new Error("already called!");
			}
			this.silentlyCancelled = false;
			return;
		}
	},

	callback: function(res){
		this._check();
		this._resback(res);
	},

	errback: function(res){
		this._check();
		if(!(res instanceof Error)){
			res = new Error(res);
		}
		this._resback(res);
	},

	addBoth: function(cb, cbfn){
		var enclosed = this.getFunctionFromArgs(cb, cbfn);
		if(arguments.length > 2){
			enclosed = doh.hitch(null, enclosed, arguments, 2);
		}
		return this.addCallbacks(enclosed, enclosed);
	},

	addCallback: function(cb, cbfn){
		var enclosed = this.getFunctionFromArgs(cb, cbfn);
		if(arguments.length > 2){
			enclosed = doh.hitch(null, enclosed, arguments, 2);
		}
		return this.addCallbacks(enclosed, null);
	},

	addErrback: function(cb, cbfn){
		var enclosed = this.getFunctionFromArgs(cb, cbfn);
		if(arguments.length > 2){
			enclosed = doh.hitch(null, enclosed, arguments, 2);
		}
		return this.addCallbacks(null, enclosed);
	},

	addCallbacks: function(cb, eb){
		this.chain.push([cb, eb])
		if(this.fired >= 0){
			this._fire();
		}
		return this;
	},

	_fire: function(){
		var chain = this.chain;
		var fired = this.fired;
		var res = this.results[fired];
		var self = this;
		var cb = null;
		while (chain.length > 0 && this.paused == 0) {
			// Array
			var pair = chain.shift();
			var f = pair[fired];
			if(f == null){
				continue;
			}
			try {
				res = f(res);
				fired = ((res instanceof Error) ? 1 : 0);
				if(res instanceof doh.Deferred){
					cb = function(res){
						self._continue(res);
					}
					this._pause();
				}
			}catch(err){
				fired = 1;
				res = err;
			}
		}
		this.fired = fired;
		this.results[fired] = res;
		if((cb)&&(this.paused)){
			res.addBoth(cb);
		}
	}
});

//
// State Keeping and Reporting
//

doh._testCount = 0;
doh._groupCount = 0;
doh._errorCount = 0;
doh._failureCount = 0;
doh._currentGroup = null;
doh._currentTest = null;
doh._paused = true;

doh._init = function(){
	this._currentGroup = null;
	this._currentTest = null;
	this._errorCount = 0;
	this._failureCount = 0;
	this.debug(this._testCount, "tests to run in", this._groupCount, "groups");
}

// doh._urls = [];
doh._groups = {};

//
// Test Registration
//

doh.registerTestNs = function(/*String*/ group, /*Object*/ ns){
	// summary:
	//		adds the passed namespace object to the list of objects to be
	//		searched for test groups. Only "public" functions (not prefixed
	//		with "_") will be added as tests to be run. If you'd like to use
	//		fixtures (setUp(), tearDown(), and runTest()), please use
	//		registerTest() or registerTests().
	for(var x in ns){
		if(	(x.charAt(0) == "_") &&
			(typeof ns[x] == "function") ){
			this.registerTest(group, ns[x]);
		}
	}
}

doh._testRegistered = function(group, fixture){
	// slot to be filled in
}

doh._groupStarted = function(group){
	// slot to be filled in
}

doh._groupFinished = function(group, success){
	// slot to be filled in
}

doh._testStarted = function(group, fixture){
	// slot to be filled in
}

doh._testFinished = function(group, fixture, success){
	// slot to be filled in
}

doh.registerGroup = function(	/*String*/ group, 
								/*Array||Function||Object*/ tests, 
								/*Function*/ setUp, 
								/*Function*/ tearDown){
	// summary:
	//		registers an entire group of tests at once and provides a setUp and
	//		tearDown facility for groups. If you call this method with only
	//		setUp and tearDown parameters, they will replace previously
	//		installed setUp or tearDown functions for the group with the new
	//		methods.
	// group:
	//		string name of the group
	// tests:
	//		either a function or an object or an array of functions/objects. If
	//		an object, it must contain at *least* a "runTest" method, and may
	//		also contain "setUp" and "tearDown" methods. These will be invoked
	//		on either side of the "runTest" method (respectively) when the test
	//		is run. If an array, it must contain objects matching the above
	//		description or test functions.
	// setUp: a function for initializing the test group
	// tearDown: a function for initializing the test group
	if(tests){
		this.register(group, tests);
	}
	if(setUp){
		this._groups[group].setUp = setUp;
	}
	if(tearDown){
		this._groups[group].tearDown = tearDown;
	}
}

doh._getTestObj = function(group, test){
	var tObj = test;
	if(typeof test == "string"){
		if(test.substr(0, 4)=="url:"){
			return this.registerUrl(group, test);
		}else{
			tObj = {
				name: test.replace("/\s/g", "_")
			};
			tObj.runTest = new Function("t", test);
		}
	}else if(typeof test == "function"){
		// if we didn't get a fixture, wrap the function
		tObj = { "runTest": test };
		if(test["name"]){
			tObj.name = test.name;
		}else{
			try{
				var fStr = "function ";
				var ts = tObj.runTest+"";
				if(0 <= ts.indexOf(fStr)){
					tObj.name = ts.split(fStr)[1].split("(", 1)[0];
				}
				// doh.debug(tObj.runTest.toSource());
			}catch(e){
			}
		}
		// FIXME: try harder to get the test name here
	}
	return tObj;
}

doh.registerTest = function(/*String*/ group, /*Function||Object*/ test){
	// summary:
	//		add the provided test function or fixture object to the specified
	//		test group.
	// group:
	//		string name of the group to add the test to
	// test:
	//		either a function or an object. If an object, it must contain at
	//		*least* a "runTest" method, and may also contain "setUp" and
	//		"tearDown" methods. These will be invoked on either side of the
	//		"runTest" method (respectively) when the test is run.
	if(!this._groups[group]){
		this._groupCount++;
		this._groups[group] = [];
		this._groups[group].inFlight = 0;
	}
	var tObj = this._getTestObj(group, test);
	if(!tObj){ return; }
	this._groups[group].push(tObj);
	this._testCount++;
	this._testRegistered(group, tObj);
	return tObj;
}

doh.registerTests = function(/*String*/ group, /*Array*/ testArr){
	// summary:
	//		registers a group of tests, treating each element of testArr as
	//		though it were being (along with group) passed to the registerTest
	//		method.
	for(var x=0; x<testArr.length; x++){
		this.registerTest(group, testArr[x]);
	}
}

// FIXME: move implementation to _browserRunner?
doh.registerUrl = function(	/*String*/ group, 
								/*String*/ url, 
								/*Integer*/ timeout){
	this.debug("ERROR:");
	this.debug("\tNO registerUrl() METHOD AVAILABLE.");
	// this._urls.push(url);
}

doh.registerString = function(group, str){
}

// FIXME: remove the doh.add alias SRTL.
doh.register = doh.add = function(groupOrNs, testOrNull){
	// summary:
	// 		"magical" variant of registerTests, registerTest, and
	// 		registerTestNs. Will accept the calling arguments of any of these
	// 		methods and will correctly guess the right one to register with.
	if(	(arguments.length == 1)&&
		(typeof groupOrNs == "string") ){
		if(groupOrNs.substr(0, 4)=="url:"){
			this.registerUrl(groupOrNs);
		}else{
			this.registerTest("ungrouped", groupOrNs);
		}
	}
	if(arguments.length == 1){
		this.debug("invalid args passed to doh.register():", groupOrNs, ",", testOrNull);
		return;
	}
	if(typeof testOrNull == "string"){
		if(testOrNull.substr(0, 4)=="url:"){
			this.registerUrl(testOrNull);
		}else{
			this.registerTest(groupOrNs, testOrNull);
		}
		// this.registerTestNs(groupOrNs, testOrNull);
		return;
	}
	if(doh._isArray(testOrNull)){
		this.registerTests(groupOrNs, testOrNull);
		return;
	}
	this.registerTest(groupOrNs, testOrNull);
}

//
// Assertions and In-Test Utilities
//

doh.t = doh.assertTrue = function(/*Object*/ condition){
	// summary:
	//		is the passed item "truthy"?
	if(!eval(condition)){
		throw doh._AssertFailure("assertTrue('" + condition + "') failed");
	}
}

doh.f = doh.assertFalse = function(/*Object*/ condition){
	// summary:
	//		is the passed item "falsey"?
	if(eval(condition)){
		throw doh._AssertFailure("assertFalse('" + condition + "') failed");
	}
}

doh.is = doh.assertEqual = function(/*Object*/ expected, /*Object*/ actual){
	// summary:
	//		are the passed expected and actual objects/values deeply
	//		equivalent?
	if((expected == undefined)&&(actual == undefined)){ 
		return true;
	}
	if(expected === actual){
		return true;
	}
	if(expected == actual){ 
		return true;
	}
	if(	(this._isArray(expected) && this._isArray(actual))&&
		(this._arrayEq(expected, actual)) ){
		return true;
	}
	if( ((typeof expected == "object")&&((typeof actual == "object")))&&
		(this._objPropEq(expected, actual)) ){
		return true;
	}
	throw new doh._AssertFailure("assertEqual() failed: expected |"+expected+"| but got |"+actual+"|");
}

doh._arrayEq = function(expected, actual){
	if(expected.length != actual.length){ return false; }
	// FIXME: we're not handling circular refs. Do we care?
	for(var x=0; x<expected.length; x++){
		if(!doh.assertEqual(expected[x], actual[x])){ return false; }
	}
	return true;
}

doh._objPropEq = function(expected, actual){
	for(var x in expected){
		if(!doh.assertEqual(expected[x], actual[x])){
			return false;
		}
	}
	return true;
}

doh._isArray = function(it){
	return (it && it instanceof Array || typeof it == "array" || ((typeof dojo["NodeList"] != "undefined") && (it instanceof dojo.NodeList)));
}

//
// Runner-Wrapper
//

doh._setupGroupForRun = function(/*String*/ groupName, /*Integer*/ idx){
	var tg = this._groups[groupName];
	this.debug(this._line);
	this.debug("GROUP", "\""+groupName+"\"", "has", tg.length, "test"+((tg.length > 1) ? "s" : "")+" to run");
}

doh._handleFailure = function(groupName, fixture, e){
	// this.debug("FAILED test:", fixture.name);
	// mostly borrowed from JUM
	this._groups[groupName].failures++;
	var out = "";
	if(e instanceof this._AssertFailure){
		this._failureCount++;
		if(e["fileName"]){ out += e.fileName + ':'; }
		if(e["lineNumber"]){ out += e.lineNumber + ' '; }
		out += e+": "+e.message;
		this.debug("\t_AssertFailure:", out);
	}else{
		this._errorCount++;
	}
	this.debug(e);
	if(fixture.runTest["toSource"]){
		var ss = fixture.runTest.toSource();
		this.debug("\tERROR IN:\n\t\t", ss);
	}else{
		this.debug("\tERROR IN:\n\t\t", fixture.runTest);
	}
}

try{
	setTimeout(function(){}, 0);
}catch(e){
	setTimeout = function(func){
		return func();
	}
}

doh._runFixture = function(groupName, fixture){
	var tg = this._groups[groupName];
	this._testStarted(groupName, fixture);
	var threw = false;
	var err = null;
	// run it, catching exceptions and reporting them
	try{
		// let doh reference "this.group.thinger..." which can be set by
		// another test or group-level setUp function
		fixture.group = tg; 
		// only execute the parts of the fixture we've got
		if(fixture["setUp"]){ fixture.setUp(this); }
		if(fixture["runTest"]){  // should we error out of a fixture doesn't have a runTest?
			fixture.startTime = new Date();
			var ret = fixture.runTest(this); 
			fixture.endTime = new Date();
			// if we get a deferred back from the test runner, we know we're
			// gonna wait for an async result. It's up to the test code to trap
			// errors and give us an errback or callback.
			if(ret instanceof doh.Deferred){

				tg.inFlight++;
				ret.groupName = groupName;
				ret.fixture = fixture;

				ret.addErrback(function(err){
					doh._handleFailure(groupName, fixture, err);
				});

				var retEnd = function(){
					if(fixture["tearDown"]){ fixture.tearDown(doh); }
					tg.inFlight--;
					if((!tg.inFlight)&&(tg.iterated)){
						doh._groupFinished(groupName, (!tg.failures));
					}
					doh._testFinished(groupName, fixture, ret.results[0]);
					if(doh._paused){
						doh.run();
					}
				}

				var timer = setTimeout(function(){
					// ret.cancel();
					// retEnd();
					ret.errback(new Error("test timeout in "+fixture.name.toString()));
				}, fixture["timeout"]||1000);

				ret.addBoth(function(arg){
					clearTimeout(timer);
					retEnd();
				});
				if(ret.fired < 0){
					doh.pause();
				}
				return ret;
			}
		}
		if(fixture["tearDown"]){ fixture.tearDown(this); }
	}catch(e){
		threw = true;
		err = e;
		if(!fixture.endTime){
			fixture.endTime = new Date();
		}
	}
	var d = new doh.Deferred();
	setTimeout(this.hitch(this, function(){
		if(threw){
			this._handleFailure(groupName, fixture, err);
		}
		this._testFinished(groupName, fixture, (!threw));

		if((!tg.inFlight)&&(tg.iterated)){
			doh._groupFinished(groupName, (!tg.failures));
		}else if(tg.inFlight > 0){
			setTimeout(this.hitch(this, function(){
				doh.runGroup(groupName); // , idx);
			}), 100);
			this._paused = true;
		}
		if(doh._paused){
			doh.run();
		}
	}), 30);
	doh.pause();
	return d;
}

doh._testId = 0;
doh.runGroup = function(/*String*/ groupName, /*Integer*/ idx){
	// summary:
	//		runs the specified test group

	// the general structure of the algorithm is to run through the group's
	// list of doh, checking before and after each of them to see if we're in
	// a paused state. This can be caused by the test returning a deferred or
	// the user hitting the pause button. In either case, we want to halt
	// execution of the test until something external to us restarts it. This
	// means we need to pickle off enough state to pick up where we left off.

	// FIXME: need to make fixture execution async!!

	var tg = this._groups[groupName];
	if(tg.skip === true){ return; }
	if(this._isArray(tg)){
		if(idx<=tg.length){
			if((!tg.inFlight)&&(tg.iterated == true)){
				if(tg["tearDown"]){ tg.tearDown(this); }
				doh._groupFinished(groupName, (!tg.failures));
				return;
			}
		}
		if(!idx){
			tg.inFlight = 0;
			tg.iterated = false;
			tg.failures = 0;
		}
		doh._groupStarted(groupName);
		if(!idx){
			this._setupGroupForRun(groupName, idx);
			if(tg["setUp"]){ tg.setUp(this); }
		}
		for(var y=(idx||0); y<tg.length; y++){
			if(this._paused){
				this._currentTest = y;
				// this.debug("PAUSED at:", tg[y].name, this._currentGroup, this._currentTest);
				return;
			}
			doh._runFixture(groupName, tg[y]);
			if(this._paused){
				this._currentTest = y+1;
				if(this._currentTest == tg.length){
					tg.iterated = true;
				}
				// this.debug("PAUSED at:", tg[y].name, this._currentGroup, this._currentTest);
				return;
			}
		}
		tg.iterated = true;
		if(!tg.inFlight){
			if(tg["tearDown"]){ tg.tearDown(this); }
			doh._groupFinished(groupName, (!tg.failures));
		}
	}
}

doh._onEnd = function(){}

doh._report = function(){
	// summary:
	//		a private method to be implemented/replaced by the "locally
	//		appropriate" test runner

	// this.debug("ERROR:");
	// this.debug("\tNO REPORTING OUTPUT AVAILABLE.");
	// this.debug("\tIMPLEMENT doh._report() IN YOUR TEST RUNNER");

	this.debug(this._line);
	this.debug("| TEST SUMMARY:");
	this.debug(this._line);
	this.debug("\t", this._testCount, "tests in", this._groupCount, "groups");
	this.debug("\t", this._errorCount, "errors");
	this.debug("\t", this._failureCount, "failures");
}

doh.togglePaused = function(){
	this[(this._paused) ? "run" : "pause"]();
}

doh.pause = function(){
	// summary:
	//		halt test run. Can be resumed.
	this._paused = true;
}

doh.run = function(){
	// summary:
	//		begins or resumes the test process.
	// this.debug("STARTING");
	this._paused = false;
	var cg = this._currentGroup;
	var ct = this._currentTest;
	var found = false;
	if(!cg){
		this._init(); // we weren't paused
		found = true;
	}
	this._currentGroup = null;
	this._currentTest = null;

	for(var x in this._groups){
		if(
			( (!found)&&(x == cg) )||( found )
		){
			if(this._paused){ return; }
			this._currentGroup = x;
			if(!found){
				found = true;
				this.runGroup(x, ct);
			}else{
				this.runGroup(x);
			}
			if(this._paused){ return; }
		}
	}
	this._currentGroup = null;
	this._currentTest = null;
	this._paused = false;
	this._onEnd();
	this._report();
}

tests = doh;

try{
	if(typeof dojo != "undefined"){
		dojo.platformRequire({
			browser: ["doh._browserRunner"],
			rhino: ["doh._rhinoRunner"],
			spidermonkey: ["doh._rhinoRunner"]
		});
		var _shouldRequire = (dojo.isBrowser) ? (dojo.global == dojo.global["parent"]) : true;
		if(_shouldRequire){
			if(dojo.isBrowser){
				dojo.addOnLoad(function(){
					if(dojo.byId("testList")){
						var _tm = ( (dojo.global.testModule && dojo.global.testModule.length) ? dojo.global.testModule : "dojo.tests.module");
						dojo.forEach(_tm.split(","), dojo.require, dojo);
						setTimeout(function(){
							doh.run();
						}, 500);
					}
				});
			}else{
				dojo.require("doh._base");
			}
		}
	}else{
		if(
			(typeof load == "function")&&
			(	(typeof Packages == "function")||
				(typeof Packages == "object")	)
		){
			throw new Error();
		}else if(typeof load == "function"){
			throw new Error();
		}
	}
}catch(e){
	print("\n"+doh._line);
	print("The Dojo Unit Test Harness, $Rev$");
	print("Copyright (c) 2007, The Dojo Foundation, All Rights Reserved");
	print(doh._line, "\n");

	load("_rhinoRunner.js");

	try{
		var dojoUrl = "../../dojo/dojo.js";
		var testUrl = "";
		var testModule = "dojo.tests.module";
		for(var x=0; x<arguments.length; x++){
			if(arguments[x].indexOf("=") > 0){
				var tp = arguments[x].split("=");
				if(tp[0] == "dojoUrl"){
					dojoUrl = tp[1];
				}
				if(tp[0] == "testUrl"){
					testUrl = tp[1];
				}
				if(tp[0] == "testModule"){
					testModule = tp[1];
				}
			}
		}
		if(dojoUrl.length){
			if(!this["djConfig"]){
				djConfig = {};
			}
			djConfig.baseUrl = dojoUrl.split("dojo.js")[0];
			load(dojoUrl);
		}
		if(testUrl.length){
			load(testUrl);
		}
		if(testModule.length){
			dojo.forEach(testModule.split(","), dojo.require, dojo);
		}
	}catch(e){
	}

	doh.run();
}
