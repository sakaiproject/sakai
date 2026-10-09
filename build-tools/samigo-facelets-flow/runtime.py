"""Disposable local Sakai runtimes; never alter the source deployment."""
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import time
import urllib.request
import xml.etree.ElementTree as ET

JAVA_HOME = Path(os.environ.get('JAVA_HOME', '/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home'))
REPO = Path(__file__).resolve().parents[2]
ROOT = Path(__file__).resolve().parent


def compile_probe(source, output):
    output.mkdir(parents=True, exist_ok=True)
    jars = list((source / 'lib').rglob('*.jar')) + list((source / 'components').rglob('*.jar')) + list((source / 'webapps/samigo-app/WEB-INF/lib').glob('*.jar'))
    classpath = os.pathsep.join(map(str, [source / 'webapps/samigo-app/WEB-INF/classes', *jars]))
    classes = output / 'probe-classes'
    classes.mkdir(exist_ok=True)
    subprocess.run([str(JAVA_HOME / 'bin/javac'), '--release', '21', '-proc:none', '-cp', classpath, '-d', str(classes), *map(str, (ROOT / 'src/main/java').rglob('*.java'))], check=True)
    jar = output / 'samigo-equivalence-probe.jar'
    subprocess.run([str(JAVA_HOME / 'bin/jar'), 'cf', str(jar), '-C', str(classes), '.'], check=True)
    access = output / 'access-classes'
    access.mkdir(exist_ok=True)
    subprocess.run([str(JAVA_HOME / 'bin/javac'), '--release', '21', '-proc:none', '-cp', classpath, '-d', str(access), *map(str, (REPO / 'samigo/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/security').glob('*.java'))], check=True)
    lombok_jars = list((Path.home() / '.m2/repository/org/projectlombok/lombok').glob('*/lombok-*.jar'))
    if not lombok_jars: raise RuntimeError('Build samigo-app first to cache its Lombok dependency')
    lombok = max(lombok_jars, key=lambda p: tuple(int(n) for n in re.findall(r'\d+', p.parent.name)))
    tool_source = REPO / 'samigo/samigo-app/src/java/org/sakaiproject/jsf/util/SamigoJsfTool.java'
    subprocess.run([str(JAVA_HOME / 'bin/javac'), '--release', '21', '-cp', str(access) + os.pathsep + classpath + os.pathsep + str(lombok), '-processorpath', str(lombok), '-processor', 'lombok.launch.AnnotationProcessorHider$AnnotationProcessor', '-d', str(access), str(tool_source)], check=True)
    return jar, access


def prepare(source, output, label, port, probe, access, mysql_config=None, source_templates=True):
    base = output / ('runtime-' + label)
    if base.exists():
        raise RuntimeError('Refusing to replace existing runtime: ' + str(base))
    base.mkdir(parents=True)
    for name in ['conf', 'bin']:
        shutil.copytree(source / name, base / name)
    for name in ['lib', 'components']:
        (base / name).symlink_to(source / name, target_is_directory=True)
    for name in ['logs', 'temp', 'work', 'sakai', 'webapps', 'captures', 'browser']:
        (base / name).mkdir()
    for app in (source / 'webapps').iterdir():
        if app.name == 'samigo-app':
            shutil.copytree(app, base / 'webapps' / app.name)
        elif app.name != 'samigo-app.war':
            (base / 'webapps' / app.name).symlink_to(app, target_is_directory=app.is_dir())
    text = (source / 'sakai/sakai.properties').read_text()
    properties = dict(re.findall(r'^([^#\s][^=]*)=(.*)$', text, re.M))
    user = properties['username@javax.sql.BaseDataSource'].strip()
    password = properties['password@javax.sql.BaseDataSource'].strip()
    schema = f'sakai_jsf_{label}_{int(time.time() * 1000)}'
    # All names are generated locally; quote existing configured credentials as SQL literals.
    quote = lambda value: "'" + value.replace('\\', '\\\\').replace("'", "''") + "'"
    sql = f'CREATE DATABASE `{schema}` CHARACTER SET utf8mb4; CREATE USER IF NOT EXISTS {quote(user)}@\'localhost\' IDENTIFIED BY {quote(password)}; GRANT ALL ON `{schema}`.* TO {quote(user)}@\'localhost\';'
    mysql = ['mysql', '--defaults-extra-file=' + str(mysql_config)] if mysql_config else ['mysql', '--no-defaults', '--protocol=TCP', '-h127.0.0.1', '-P3306', '-uroot']
    subprocess.run(mysql, input=sql, text=True, check=True, stdout=subprocess.DEVNULL)
    url = f'http://127.0.0.1:{port}'
    overrides = {
        'url@javax.sql.BaseDataSource': f'jdbc:mariadb://127.0.0.1:3306/{schema}?useUnicode=true&characterEncoding=UTF-8',
        'serverUrl': url, 'accessUrl': url + '/access', 'serverName': '127.0.0.1', 'serverId': 'equivalence-' + label,
        'ignite.home': str(base / 'sakai/ignite'), 'ignite.address': '127.0.0.1',
        'ignite.port': str(49000 + (port % 100) * 10), 'ignite.range': '1',
    }
    for key, value in overrides.items():
        pattern = r'^' + re.escape(key) + r'=.*$'
        text = re.sub(pattern, lambda match: key + '=' + value, text, flags=re.M) if re.search(pattern, text, re.M) else text + '\n' + key + '=' + value
    config = base / 'sakai/sakai.properties'
    config.write_text(text + '\n')
    config.chmod(0o600)
    server = base / 'conf/server.xml'
    tree = ET.parse(server)
    tree.getroot().set('port', '-1')
    connector = next(c for c in tree.getroot().iter('Connector') if c.get('protocol', '').startswith('HTTP'))
    connector.set('port', str(port)); connector.set('address', '127.0.0.1')
    tree.write(server, encoding='utf-8', xml_declaration=True)
    setenv = base / 'bin/setenv.sh'
    script = setenv.read_text().replace('-Xms2g -Xmx3g', '-Xms512m -Xmx2g')
    script = re.sub(r'-Dsakai.home=\S+', '-Dsakai.home=' + str(base / 'sakai') + '/', script)
    script = script.replace('-DIGNITE_UPDATE_NOTIFIER=false', '-Dsamigo.equivalence.output=' + str(base / 'captures') + ' -DIGNITE_UPDATE_NOTIFIER=false')
    setenv.write_text(script)
    instrument(base / 'webapps/samigo-app', probe, access, copy_source=source_templates)
    return {'base': str(base), 'url': url, 'schema': schema}


def instrument(app, probe, access, copy_source=True):
    web = REPO / 'samigo/samigo-app/src/webapp'
    # Same source templates/configuration in both variants, rather than stale deployed copies.
    if copy_source:
        for file in ['jsf/author/createAssessment_title.jsp', 'jsf/author/editAssessmentHeadings.jsp', 'jsf/author/permissionsNav.jsp', 'WEB-INF/web.xml', 'WEB-INF/faces-config.xml']:
            shutil.copy2(web / file, app / file)
    shutil.copy2(probe, app / 'WEB-INF/lib' / probe.name)
    shutil.copytree(access, app / 'WEB-INF/classes', dirs_exist_ok=True)
    faces = app / 'WEB-INF/faces-config.xml'
    text = faces.read_text()
    probe_listener = '<phase-listener>org.sakaiproject.equivalence.SamigoViewProbe</phase-listener>'
    support_listener = '<phase-listener>org.sakaiproject.tool.assessment.ui.facelets.SamigoViewPhaseListener</phase-listener>'
    text = text.replace(support_listener, probe_listener + support_listener) if support_listener in text else text.replace('</lifecycle>', probe_listener + '</lifecycle>')
    faces.write_text(text)
    descriptor = app / 'WEB-INF/web.xml'
    text = descriptor.read_text().replace('</web-app>', '''<filter><filter-name>equivalence-header</filter-name><filter-class>org.sakaiproject.equivalence.SamigoHeaderFilter</filter-class></filter>
<filter-mapping><filter-name>equivalence-header</filter-name><url-pattern>/*</url-pattern><dispatcher>REQUEST</dispatcher><dispatcher>FORWARD</dispatcher></filter-mapping>
</web-app>''')
    descriptor.write_text(text)


def start(runtime):
    base = Path(runtime['base'])
    env = dict(os.environ, JAVA_HOME=str(JAVA_HOME), CATALINA_BASE=str(base), CATALINA_HOME=str(base))
    log = (base / 'runtime.log').open('w')
    process = subprocess.Popen([str(base / 'bin/catalina.sh'), 'run'], env=env, stdout=log, stderr=subprocess.STDOUT)
    (base / 'pid').write_text(str(process.pid))
    return process, log


def align_unchanged_jsp_cache(baseline, candidate):
    """Reuse the same Jasper classes for unchanged templates; keep all generated IDs exact."""
    left = Path(baseline['base'])
    right = Path(candidate['base'])
    app = Path('webapps/samigo-app')
    templates = [p for p in (left / app).rglob('*') if p.is_file() and p.suffix in ['.jsp', '.jspf', '.inc']]
    for source in templates:
        destination = right / source.relative_to(left)
        if not destination.exists() or source.read_bytes() != destination.read_bytes():
            raise RuntimeError('Cannot reuse compiled JSP with changed source: ' + str(source.relative_to(left)))
    cache = Path('work/Catalina/localhost/samigo-app/org/apache/jsp')
    shutil.copytree(left / cache, right / cache, dirs_exist_ok=True)
    return {'checkedTemplates':len(templates), 'copiedCache':str(right / cache), 'reason':'Jasper ID prefixes differ by compilation context; unchanged JSP bytecode is reused rather than normalizing submitted IDs.'}


def ready(runtime, process, timeout=360):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if process.poll() is not None:
            raise RuntimeError('Runtime exited; inspect ' + runtime['base'] + '/runtime.log')
        try:
            with urllib.request.urlopen(runtime['url'] + '/portal', timeout=3) as response:
                if response.status == 200: return
        except (OSError, urllib.error.HTTPError):
            pass
        time.sleep(2)
    raise TimeoutError('Runtime not ready: ' + runtime['url'])


def stop(process, log):
    process.terminate()
    try: process.wait(timeout=30)
    except subprocess.TimeoutExpired: process.kill(); process.wait()
    log.close()
