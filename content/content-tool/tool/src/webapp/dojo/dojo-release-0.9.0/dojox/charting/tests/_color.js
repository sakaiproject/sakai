if(!dojo._hasResource["dojox.charting.tests._color"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting.tests._color"] = true;
dojo.provide("dojox.charting.tests._color");
dojo.require("dojox.charting._color");

/*
	Note that there are some minor inaccuracies that
	can be introduced for comparison purposes; the
	formulae used in Photoshop may produce *slightly*
	different numbers.  But numbers will be off by
	1, if at all.
 */
(function(){
	var dxc=dojox.charting;
	var rgb=[
		{ r:0x4f, g:0xc8, b:0xd6 },
		{ r:0x40, g:0x9e, b:0x02 },
		{ r:0xff, g:0xfb, b:0x85 },
		{ r:0x7b, g:0x5a, b:0x7d }
	];
	var hsb=[
		{ h:186, s:63, b: 84 },
		{ h: 96, s:99, b: 62 },
		{ h: 58, s:48, b:100 },
		{ h:297, s:28, b: 49 }
	];
	tests.register("dojox.charting.tests._util", [
		function testToHsb(t){
			var c=rgb[0];
			var oHsb=dxc._color.toHsb(c.r, c.g, c.b);
			t.assertEqual(hsb[0].h, oHsb.h);
			t.assertEqual(hsb[0].s, oHsb.s);
			t.assertEqual(hsb[0].b, oHsb.b);

			var c=rgb[1];
			var oHsb=dxc._color.toHsb(c.r, c.g, c.b);
			t.assertEqual(hsb[1].h, oHsb.h);
			t.assertEqual(hsb[1].s, oHsb.s);
			t.assertEqual(hsb[1].b, oHsb.b);
			
			var c=rgb[2];
			var oHsb=dxc._color.toHsb(c.r, c.g, c.b);
			t.assertEqual(hsb[2].h, oHsb.h);
			t.assertEqual(hsb[2].s, oHsb.s);
			t.assertEqual(hsb[2].b, oHsb.b);

			var c=rgb[3];
			var oHsb=dxc._color.toHsb(c.r, c.g, c.b);
			t.assertEqual(hsb[3].h, oHsb.h);
			t.assertEqual(hsb[3].s, oHsb.s);
			t.assertEqual(hsb[3].b, oHsb.b);
		},
		
		function testFromHsb(t){
			var c1=dxc._color.fromHsb(hsb[0].h, hsb[0].s, hsb[0].b);
			var c2=rgb[0];
			t.assertEqual(c1.r, c2.r);
			t.assertEqual(c1.g, c2.g);
			t.assertEqual(c1.b, c2.b);

			var c1=dxc._color.fromHsb(hsb[1].h, hsb[1].s, hsb[1].b);
			var c2=rgb[1];
			t.assertEqual(c1.r, c2.r);
			t.assertEqual(c1.g, c2.g);
			t.assertEqual(c1.b, c2.b);

			var c1=dxc._color.fromHsb(hsb[2].h, hsb[2].s, hsb[2].b);
			var c2=rgb[2];
			t.assertEqual(c1.r, c2.r);
			t.assertEqual(c1.g, c2.g);
			t.assertEqual(c1.b, c2.b);

			var c1=dxc._color.fromHsb(hsb[3].h, hsb[3].s, hsb[3].b);
			var c2=rgb[3];
			t.assertEqual(c1.r, c2.r);
			t.assertEqual(c1.g, c2.g);
			t.assertEqual(c1.b, c2.b);
		}
	]);
})();

}
