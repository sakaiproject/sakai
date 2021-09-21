sakaidifflib = {
    SakaiSequenceMatcher: function(e, c, d) {
        this.set_seqs = function (g, f) {
            this.set_seq1(g);
            this.set_seq2(f);
        };
        this.set_seq1 = function (b) {
            if (b == this.a) {
                return;
            }
            this.a = b;
            this.matching_blocks = this.opcodes = null;
        };
        this.set_seq2 = function (a) {
            if (a == this.b) {
                return;
            }
            this.b = a;
            this.matching_blocks = this.opcodes = this.fullbcount = null;
            this.__chain_b();
        };
        this.__chain_b = function () {
            var o = this.b;
            var g = o.length;
            var h = (this.b2j = {});
            var j = {};
            for (var m = 0; m < o.length; m++) {
                var l = o[m];
                if (l in h) {
                    var p = h[l];
                    if (g >= 200 && p.length * 100 > g) {
                        j[l] = 1;
                        delete h[l];
                    } else {
                        p.push(m);
                    }
                } else {
                    h[l] = [m];
                }
            }
            for (var l in j) {
                delete h[l];
            }
            var f = this.isjunk;
            var a = {};
            if (f) {
                for (var l in j) {
                    if (f(l)) {
                        a[l] = 1;
                        delete j[l];
                    }
                }
                for (var l in h) {
                    if (f(l)) {
                        a[l] = 1;
                        delete h[l];
                    }
                }
            }
            this.isbjunk = difflib.__isindict(a);
            this.isbpopular = difflib.__isindict(j);
        };
        this.find_longest_match = function (g, w, q, n) {
            var y = this.a;
            var x = this.b;
            var z = this.b2j;
            var f = this.isbjunk;
            var o = g;
            var m = q;
            var l = 0;
            var t = null;
            var s = {};
            var p = [];
            for (var u = g; u < w; u++) {
                var h = {};
                var r = difflib.__dictget(z, y[u], p);
		if (r.length > 0) { // hornersa - Insert a check here for r to ensure it's empty. Without it the for loop might continue. 
                for (var v in r) {
		    // hornersa - Insert a check for 'v' whereby we should only be iterating on integer values 
		    if (! Number.isInteger(v)) {
			continue; // break here might be ok if r's integer values aren't interleaved with other var types 
		    }
		    // hornersa - In Sakai the remaining code in this loop run without the inserted conditionals above. ?!?
                    t = r[v];
                    if (t < q) {
                        continue;
                    }
                    if (t >= n) {
                        break;
                    }
                    h[t] = k = difflib.__dictget(s, t - 1, 0) + 1;
                    if (k > l) {
                        o = u - k + 1;
                        m = t - k + 1;
                        l = k;
                    }
                }
	      } // end of hornersa conditional
                s = h;
            }
            while (o > g && m > q && !f(x[m - 1]) && y[o - 1] == x[m - 1]) {
                o--;
                m--;
                l++;
            }
            while (o + l < w && m + l < n && !f(x[m + l]) && y[o + l] == x[m + l]) {
                l++;
            }
            while (o > g && m > q && f(x[m - 1]) && y[o - 1] == x[m - 1]) {
                o--;
                m--;
                l++;
            }
            while (o + l < w && m + l < n && f(x[m + l]) && y[o + l] == x[m + l]) {
                l++;
            }
            return [o, m, l];
        };
        this.get_matching_blocks = function () {
            if (this.matching_blocks != null) {
                return this.matching_blocks;
            }
            var f = this.a.length;
            var b = this.b.length;
            var q = [[0, f, 0, b]];
            var l = [];
            var s, m, v, r, a, p, n, h, t;
            while (q.length) {
                a = q.pop();
                s = a[0];
                m = a[1];
                v = a[2];
                r = a[3];
                t = this.find_longest_match(s, m, v, r);
                p = t[0];
                n = t[1];
                h = t[2];
                if (h) {
                    l.push(t);
                    if (s < p && v < n) {
                        q.push([s, p, v, n]);
                    }
                    if (p + h < m && n + h < r) {
                        q.push([p + h, m, n + h, r]);
                    }
                }
            }
            l.sort(difflib.__ntuplecomp);
            var g = (j1 = k1 = block = 0);
            var o = [];
            for (var u in l) {
                block = l[u];
                i2 = block[0];
                j2 = block[1];
                k2 = block[2];
                if (g + k1 == i2 && j1 + k1 == j2) {
                    k1 += k2;
                } else {
                    if (k1) {
                        o.push([g, j1, k1]);
                    }
                    g = i2;
                    j1 = j2;
                    k1 = k2;
                }
            }
            if (k1) {
                o.push([g, j1, k1]);
            }
            o.push([f, b, 0]);
            this.matching_blocks = o;
            return this.matching_blocks;
        };
        this.get_opcodes = function () {
            if (this.opcodes != null) {
                return this.opcodes;
            }
            var h = 0;
            var g = 0;
            var n = [];
            this.opcodes = n;
            var f, l, b, o, p;
            var a = this.get_matching_blocks();
            for (var m in a) {
                f = a[m];
                l = f[0];
                b = f[1];
                o = f[2];
                p = "";
                if (h < l && g < b) {
                    p = "replace";
                } else {
                    if (h < l) {
                        p = "delete";
                    } else {
                        if (g < b) {
                            p = "insert";
                        }
                    }
                }
                if (p) {
                    n.push([p, h, l, g, b]);
                }
                h = l + o;
                g = b + o;
                if (o) {
                    n.push(["equal", l, h, b, g]);
                }
            }
            return n;
        };
        this.get_grouped_opcodes = function (g) {
            if (!g) {
                g = 3;
            }
            var a = this.get_opcodes();
            if (!a) {
                a = [["equal", 0, 1, 0, 1]];
            }
            var b, p, i, h, o, m;
            if (a[0][0] == "equal") {
                b = a[0];
                p = b[0];
                i = b[1];
                h = b[2];
                o = b[3];
                m = b[4];
                a[0] = [p, Math.max(i, h - g), h, Math.max(o, m - g), m];
            }
            if (a[a.length - 1][0] == "equal") {
                b = a[a.length - 1];
                p = b[0];
                i = b[1];
                h = b[2];
                o = b[3];
                m = b[4];
                a[a.length - 1] = [p, i, Math.min(h, i + g), o, Math.min(m, o + g)];
            }
            var l = g + g;
            var f = [];
            for (var j in a) {
                b = a[j];
                p = b[0];
                i = b[1];
                h = b[2];
                o = b[3];
                m = b[4];
                if (p == "equal" && h - i > l) {
                    f.push([p, i, Math.min(h, i + g), o, Math.min(m, o + g)]);
                    i = Math.max(i, h - g);
                    o = Math.max(o, m - g);
                }
                f.push([p, i, h, o, m]);
            }
            if (f && f[f.length - 1][0] == "equal") {
                f.pop();
            }
            return f;
        };
        this.ratio = function () {
            matches = difflib.__reduce(
                function (a, b) {
                    return a + b[b.length - 1];
                },
                this.get_matching_blocks(),
                0
            );
            return difflib.__calculate_ratio(matches, this.a.length + this.b.length);
        };
        this.quick_ratio = function () {
            var a, b;
            if (this.fullbcount == null) {
                this.fullbcount = a = {};
                for (var g = 0; g < this.b.length; g++) {
                    b = this.b[g];
                    a[b] = difflib.__dictget(a, b, 0) + 1;
                }
            }
            a = this.fullbcount;
            var j = {};
            var f = difflib.__isindict(j);
            var h = (numb = 0);
            for (var g = 0; g < this.a.length; g++) {
                b = this.a[g];
                if (f(b)) {
                    numb = j[b];
                } else {
                    numb = difflib.__dictget(a, b, 0);
                }
                j[b] = numb - 1;
                if (numb > 0) {
                    h++;
                }
            }
            return difflib.__calculate_ratio(h, this.a.length + this.b.length);
        };
        this.real_quick_ratio = function () {
            var b = this.a.length;
            var a = this.b.length;
            return _calculate_ratio(Math.min(b, a), b + a);
        };
        this.isjunk = d ? d : difflib.defaultJunkFunction;
        this.a = this.b = null;
        this.set_seqs(e, c);
    }
};
