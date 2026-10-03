/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.qti.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;

public class ExtractionHelperAttachmentPathTest {

  private Path temp;
  private Path root;
  private Path outside;

  @Before
  public void createPackageDirectory() throws IOException {
    temp = Files.createTempDirectory("qti-attachments");
    root = Files.createDirectories(temp.resolve("unzip"));
    outside = temp.resolve("outside.txt");
    Files.writeString(outside, "secret");
    Files.createDirectories(root.resolve("images"));
    Files.writeString(root.resolve("images/inside.txt"), "inside");
    Files.writeString(root.resolve("images/blue hill.txt"), "hill");
  }

  @After
  public void deletePackageDirectory() throws IOException {
    if (temp == null) {
      return;
    }
    try (Stream<Path> paths = Files.walk(temp)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(path);
      }
    }
  }

  @Test
  public void resolvesNestedAttachmentInsidePackage() throws IOException {
    assertEquals(root.resolve("images/inside.txt").toFile().getCanonicalPath(),
        helper().resolveImportedAttachmentPath("images/inside.txt"));
  }

  @Test
  public void resolvesUrlEncodedAttachmentName() throws IOException {
    assertEquals(root.resolve("images/blue hill.txt").toFile().getCanonicalPath(),
        helper().resolveImportedAttachmentPath("images/blue%20hill.txt"));
  }

  @Test
  public void resolvesUrlEncodedLiteralPercentAndPlusNames() throws IOException {
    Path percent = Files.writeString(root.resolve("images/100%complete.txt"), "percent");
    Path literal = Files.writeString(root.resolve("images/a+b%2e%2e.txt"), "literal");
    ExtractionHelper helper = helper();
    assertEquals(percent.toFile().getCanonicalPath(),
        helper.resolveImportedAttachmentPath("images/100%25complete.txt"));
    assertEquals(literal.toFile().getCanonicalPath(),
        helper.resolveImportedAttachmentPath("images/a%2Bb%252e%252e.txt"));
    // Force index fallback to check that real filenames are not URL-decoded.
    assertEquals(percent.toFile().getCanonicalPath(),
        helper.resolveImportedAttachmentPath("IMAGES/100%25COMPLETE.TXT"));
    assertEquals(literal.toFile().getCanonicalPath(),
        helper.resolveImportedAttachmentPath("IMAGES/A%2BB%252E%252E.TXT"));
  }

  @Test
  public void unrelatedLiteralPercentNameDoesNotAbortMissingAttachmentLookup() throws IOException {
    Files.writeString(root.resolve("unrelated%.txt"), "unrelated");
    assertNull(helper().resolveImportedAttachmentPath("missing.txt"));
  }

  @Test
  public void resolvesPathThatStaysInsideAfterNormalization() throws IOException {
    assertEquals(root.resolve("images/inside.txt").toFile().getCanonicalPath(),
        helper().resolveImportedAttachmentPath("images/../images/inside.txt"));
  }

  @Test
  public void rejectsAttachmentPathsOutsidePackage() throws IOException {
    ExtractionHelper helper = helper();
    assertNull(helper.resolveImportedAttachmentPath("../outside.txt"));
    assertNull(helper.resolveImportedAttachmentPath("images/../../outside.txt"));
    assertNull(helper.resolveImportedAttachmentPath("%2e%2e/outside.txt"));
    assertNull(helper.resolveImportedAttachmentPath("images/%2e%2e/%2e%2e/outside.txt"));
    assertNull(helper.resolveImportedAttachmentPath("%252e%252e/outside.txt"));
    assertNull(helper.resolveImportedAttachmentPath(outside.toString()));
    assertNull(helper.resolveImportedAttachmentPath("images\\..\\..\\outside.txt"));
    assertEquals("secret", Files.readString(outside));
  }

  @Test
  public void rejectsSymlinkThatLeavesPackage() throws IOException {
    createSymbolicLink(root.resolve("images/linked.txt"), outside);
    ExtractionHelper helper = helper();
    assertNull(helper.resolveImportedAttachmentPath("images/linked.txt"));
    assertNull(helper.resolveImportedAttachmentPath("IMAGES/LINKED.TXT"));
  }

  @Test
  public void resolvesSymlinkWhoseTargetStaysInsidePackage() throws IOException {
    Path target = root.resolve("images/inside.txt");
    createSymbolicLink(root.resolve("images/linked.txt"), target);
    assertEquals(target.toRealPath().toString(), helper().resolveImportedAttachmentPath("images/linked.txt"));
  }

  private void createSymbolicLink(Path link, Path target) {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException e) {
      assumeNoException("Filesystem does not support creating symbolic links", e);
    }
  }

  @Test
  public void skipsAssessmentSectionAndItemAttachmentsOutsidePackage() {
    ExtractionHelper helper = helper();
    AssessmentFacade assessment = new AssessmentFacade();
    assessment.addAssessmentAttachmentMetaData("../outside.txt|outside.txt|text/plain");
    helper.makeAssessmentAttachmentSet(assessment);

    Map<String, String> section = new HashMap<>();
    section.put("attachment", "%2e%2e/outside.txt|outside.txt|text/plain");
    helper.makeSectionAttachmentSet(new SectionFacade(), section);

    ItemFacade item = new ItemFacade();
    item.addItemAttachmentMetaData("images/../../outside.txt|outside.txt|text/plain");
    helper.makeItemAttachmentSet(item);

    assertEquals(List.of("../outside.txt", "%2e%2e/outside.txt", "images/../../outside.txt"),
        helper.getSkippedAttachments());
  }

  private ExtractionHelper helper() {
    ExtractionHelper helper = new ExtractionHelper(QTIVersion.VERSION_1_2);
    helper.setUnzipLocation(root.toString());
    return helper;
  }
}
