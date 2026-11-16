const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');

const root = path.resolve(__dirname, '../../../..');
const context = vm.createContext({});
vm.runInContext(fs.readFileSync(path.join(root, 'library/src/webapp/js/trimpath-template-latest.js'), 'utf8'), context);
const { TrimPath } = context;
const gradeTable = fs.readFileSync(path.join(root,
  'gradebookng/tool/src/java/org/sakaiproject/gradebookng/tool/component/GbGradeTable.html'), 'utf8');

function render(id, values) {
  const source = gradeTable.match(new RegExp(`<script id="${id}"[^>]*>([\\s\\S]*?)</script>`))[1];
  return TrimPath.parseTemplate(source).process(values, { throwExceptions: true });
}

test('attribute escaping handles quotes, ampersands, brackets, and literal entities', () => {
  const template = TrimPath.parseTemplate('${title|escapeAttribute}');
  for (const [title, expected] of [
    ['"double" and \'single\'', '&quot;double&quot; and &#39;single&#39;'],
    ['A & B < C > D', 'A &amp; B &lt; C &gt; D'],
    ['Literal &amp;', 'Literal &amp;amp;'],
  ]) {
    assert.equal(template.process({ title }), expected);
  }
});

test('Gradebook header treats titles as text in both output contexts', () => {
  const title = '" onmouseover="alert(1) <img src=x onerror=alert(1)> &';
  const html = render('assignmentHeaderTemplate', {
    abbrevTitle: title, tooltip: title, extraCredit: false, externallyMaintained: false,
    hasAssociatedRubric: false, assignmentId: 1,
    settings: { gUid: 'site', isCategoriesEnabled: false },
    includedInCourseGrade: true, released: true,
    points: '10', dueDate: '',
  });
  assert.ok(html.includes('title="&quot; onmouseover=&quot;alert(1) &lt;img src=x onerror=alert(1)&gt; &amp;"'));
  assert.ok(!html.includes('<img'));
  assert.ok(!html.includes('title="" onmouseover='));
});

test('Gradebook cell metadata escapes the assignment title', () => {
  const title = '<img src=x onerror=alert(1)> & "quotes"';
  const html = render('metadataTemplate', {
    id: 'cell', student: { firstName: 'First', lastName: 'Last', eid: 'student' },
    assignment: { title, externallyMaintained: false }, notifications: [], readonly: false,
  });
  assert.ok(html.includes('<dd>&lt;img src=x onerror=alert(1)&gt; &amp; "quotes"</dd>'));
  assert.ok(!html.includes('<img'));
});
