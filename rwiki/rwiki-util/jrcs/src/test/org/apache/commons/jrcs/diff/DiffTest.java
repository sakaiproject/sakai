/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.jrcs.diff;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public abstract class DiffTest extends TestCase
{
	static final int LARGE = 2 * 1024;

	protected DiffAlgorithm algorithm;

	public DiffTest(String testName, DiffAlgorithm algorithm)
	{
		super(testName);
		this.algorithm = algorithm;
	}

	public static Test suite()
	{
		return new TestSuite(DiffTest.class);
	}

	Object[] empty = new Object[] {};

	Object[] original = new String[] { "[1] one", "[2] two", "[3] three",
			"[4] four", "[5] five", "[6] six", "[7] seven", "[8] eight",
			"[9] nine" };

	// lines 3 and 9 deleted
	Object[] rev1 = new String[] { "[1] one", "[2] two", "[4] four",
			"[5] five", "[6] six", "[7] seven", "[8] eight", };

	// lines 7 and 8 changed, 9 deleted
	Object[] rev2 = new String[] { "[1] one", "[2] two", "[3] three",
			"[4] four", "[5] five", "[6] six", "[7] seven revised",
			"[8] eight revised", };

	public void testCompare()
	{
		assertTrue(!Diff.compare(original, empty));
		assertTrue(!Diff.compare(empty, original));
		assertTrue(Diff.compare(empty, empty));
		assertTrue(Diff.compare(original, original));
	}

	public void testEmptySequences() throws DifferentiationFailedException
	{
		String[] emptyOrig = {};
		String[] emptyRev = {};
		Revision revision = Diff.diff(emptyOrig, emptyRev, algorithm);

		assertEquals("revision size is not zero", 0, revision.size());
	}

	public void testOriginalEmpty() throws DifferentiationFailedException
	{
		String[] emptyOrig = {};
		String[] rev = { "1", "2", "3" };
		Revision revision = Diff.diff(emptyOrig, rev, algorithm);

		assertEquals("revision size should be one", 1, revision.size());
		assertTrue(revision.getDelta(0) instanceof AddDelta);
	}

	public void testRevisedEmpty() throws DifferentiationFailedException
	{
		String[] orig = { "1", "2", "3" };
		String[] emptyRev = {};
		Revision revision = Diff.diff(orig, emptyRev, algorithm);

		assertEquals("revision size should be one", 1, revision.size());
		assertTrue(revision.getDelta(0) instanceof DeleteDelta);
	}

	public void testDeleteAll() throws DifferentiationFailedException,
			PatchFailedException
	{
		Revision revision = Diff.diff(original, empty, algorithm);
		assertEquals(1, revision.size());
		assertEquals(DeleteDelta.class, revision.getDelta(0).getClass());
		assertTrue(Diff.compare(revision.patch(original), empty));
	}

	public void testTwoDeletes() throws DifferentiationFailedException,
			PatchFailedException
	{
		Revision revision = Diff.diff(original, rev1, algorithm);
		assertEquals(2, revision.size());
		assertEquals(DeleteDelta.class, revision.getDelta(0).getClass());
		assertEquals(DeleteDelta.class, revision.getDelta(1).getClass());
		assertTrue(Diff.compare(revision.patch(original), rev1));
		assertEquals("3d2" + Diff.NL + "< [3] three" + Diff.NL + "9d7"
				+ Diff.NL + "< [9] nine" + Diff.NL, revision.toString());
	}

	public void testChangeAtTheEnd() throws DifferentiationFailedException,
			PatchFailedException
	{
		Revision revision = Diff.diff(original, rev2, algorithm);
		assertEquals(1, revision.size());
		assertEquals(ChangeDelta.class, revision.getDelta(0).getClass());
		assertTrue(Diff.compare(revision.patch(original), rev2));
		assertEquals("d7 3" + Diff.NL + "a9 2" + Diff.NL + "[7] seven revised"
				+ Diff.NL + "[8] eight revised" + Diff.NL, revision
				.toRCSString());
	}

	public void testPatchFailed() throws DifferentiationFailedException
	{
		try
		{
			Revision revision = Diff.diff(original, rev2, algorithm);
			assertTrue(!Diff.compare(revision.patch(rev1), rev2));
			fail("PatchFailedException not thrown");
		}
		catch (PatchFailedException e)
		{
		}
	}

	public void testPreviouslyFailedShuffle()
			throws DifferentiationFailedException, PatchFailedException
	{
		Object[] orig = new String[] { "[1] one", "[2] two", "[3] three",
				"[4] four", "[5] five", "[6] six" };

		Object[] rev = new String[] { "[3] three", "[1] one", "[5] five",
				"[2] two", "[6] six", "[4] four" };
		Revision revision = Diff.diff(orig, rev, algorithm);
		Object[] patched = revision.patch(orig);
		assertTrue(Diff.compare(patched, rev));
	}

	public void testEdit5() throws DifferentiationFailedException,
			PatchFailedException
	{
		Object[] orig = new String[] { "[1] one", "[2] two", "[3] three",
				"[4] four", "[5] five", "[6] six" };

		Object[] rev = new String[] { "one revised", "two revised", "[2] two",
				"[3] three", "five revised", "six revised", "[5] five" };
		Revision revision = Diff.diff(orig, rev, algorithm);
		Object[] patched = revision.patch(orig);
		assertTrue(Diff.compare(patched, rev));
	}

	public void testShuffle() throws DifferentiationFailedException,
			PatchFailedException
	{
		Object[] orig = new String[] { "[1] one", "[2] two", "[3] three",
				"[4] four", "[5] five", "[6] six" };

		for (int seed = 0; seed < 10; seed++)
		{
			Object[] shuffle = Diff.shuffle(orig);
			Revision revision = Diff.diff(orig, shuffle, algorithm);
			Object[] patched = revision.patch(orig);
			if (!Diff.compare(patched, shuffle))
			{
				fail("iter " + seed + " revisions differ after patch");
			}
		}
	}

	public void testRandomEdit() throws DifferentiationFailedException,
			PatchFailedException
	{
		Object[] orig = original;
		for (int seed = 0; seed < 10; seed++)
		{
			Object[] random = Diff.randomEdit(orig, seed);
			Revision revision = Diff.diff(orig, random, algorithm);
			Object[] patched = revision.patch(orig);
			if (!Diff.compare(patched, random))
			{
				fail("iter " + seed + " revisions differ after patch");
			}
			orig = random;
		}
	}

	public void testVisitor()
	{
		Object[] orig = new String[] { "[1] one", "[2] two", "[3] three",
				"[4] four", "[5] five", "[6] six" };
		Object[] rev = new String[] { "[1] one", "[2] two revised",
				"[3] three", "[4] four revised", "[5] five", "[6] six" };

		class Visitor implements RevisionVisitor
		{

			StringBuffer sb = new StringBuffer();

			public void visit(Revision revision)
			{
				sb.append("visited Revision\n");
			}

			public void visit(DeleteDelta delta)
			{
				visit((Delta) delta);
			}

			public void visit(ChangeDelta delta)
			{
				visit((Delta) delta);
			}

			public void visit(AddDelta delta)
			{
				visit((Delta) delta);
			}

			public void visit(Delta delta)
			{
				sb.append(delta.getRevised());
				sb.append("\n");
			}

			public String toString()
			{
				return sb.toString();
			}
		}

		Visitor visitor = new Visitor();
		try
		{
			Diff.diff(orig, rev, algorithm).accept(visitor);
			assertEquals(visitor.toString(), "visited Revision\n"
					+ "[2] two revised\n" + "[4] four revised\n");
		}
		catch (Exception e)
		{
			fail(e.toString());
		}
	}

	public void testAlternativeAlgorithm()
			throws DifferentiationFailedException, PatchFailedException
	{
		Revision revision = Diff.diff(original, rev2, new SimpleDiff());
		assertEquals(1, revision.size());
		assertEquals(ChangeDelta.class, revision.getDelta(0).getClass());
		assertTrue(Diff.compare(revision.patch(original), rev2));
		assertEquals("d7 3" + Diff.NL + "a9 2" + Diff.NL + "[7] seven revised"
				+ Diff.NL + "[8] eight revised" + Diff.NL, revision
				.toRCSString());
	}

	public void testLargeShuffles() throws DifferentiationFailedException,
			PatchFailedException
	{
		Object[] orig = Diff.randomSequence(LARGE);
		for (int seed = 0; seed < 3; seed++)
		{
			Object[] rev = Diff.shuffle(orig);
			Revision revision = Diff.diff(orig, rev, algorithm);
			Object[] patched = revision.patch(orig);
			if (!Diff.compare(patched, rev))
			{
				fail("iter " + seed + " revisions differ after patch");
			}
			orig = rev;
		}
	}

	public void testLargeShuffleEdits() throws DifferentiationFailedException,
			PatchFailedException
	{
		Object[] orig = Diff.randomSequence(LARGE);
		for (int seed = 0; seed < 3; seed++)
		{
			Object[] rev = Diff.randomEdit(orig, seed);
			Revision revision = Diff.diff(orig, rev, algorithm);
			Object[] patched = revision.patch(orig);
			if (!Diff.compare(patched, rev))
			{
				fail("iter " + seed + " revisions differ after patch");
			}
		}
	}

	public void testLargeAllEdited() throws DifferentiationFailedException,
			PatchFailedException
	{
		Object[] orig = Diff.randomSequence(LARGE);
		Object[] rev = Diff.editAll(orig);
		Revision revision = Diff.diff(orig, rev, algorithm);
		Object[] patched = revision.patch(orig);
		if (!Diff.compare(patched, rev))
		{
			fail("revisions differ after patch");
		}

	}
}
