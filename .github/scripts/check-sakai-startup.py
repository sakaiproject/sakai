"""Check the deployed CI server without starting or stopping it."""

import argparse
import os
from pathlib import Path
import re
import time
import urllib.error
import urllib.request


FATAL = re.compile(
    r"\b(?:BeanCreationException|NoClassDefFoundError|NoSuchMethodError|"
    r"ServiceConfigurationError|ExceptionInInitializerError)\b|"
    r"One or more listeners failed to start|Context \[.*?\] startup failed|"
    r"Error deploying web application|Exception sending context initialized event"
)
LOG_ENTRY = re.compile(r"^(?:\d{4}-\d{2}-\d{2}[ T]|\d{2}-[A-Za-z]{3}-\d{4})")


def fatal_error(logs):
    for path, content in logs:
        lines = content.splitlines()
        for index, line in enumerate(lines):
            if not FATAL.search(line):
                continue
            end = index + 1
            while end < len(lines) and not LOG_ENTRY.match(lines[end]):
                end += 1
            block = lines[max(0, index - 2):end]
            causes = [entry.strip() for entry in block if "Caused by:" in entry]
            excerpt = block if len(block) <= 80 else block[:65] + ["... truncated ..."] + block[-14:]
            return (
                f"Fatal server error in {path.name}:{index + 1}",
                line.strip(),
                causes[-1] if causes else "No nested cause logged.",
                "\n".join(excerpt),
            )
    return None


def process_alive(pid_file):
    try:
        pid = int(pid_file.read_text().strip())
        if pid <= 0:
            return False
        os.kill(pid, 0)
        return True
    except (OSError, ValueError):
        return False


def portal_ready(url):
    try:
        with urllib.request.urlopen(url, timeout=5) as response:
            return 200 <= response.status < 300
    except (OSError, urllib.error.URLError):
        return False


def report_failure(report, title, first="", cause="", excerpt=""):
    report.parent.mkdir(parents=True, exist_ok=True)
    # Keep log content out of GitHub's workflow-command interpreter.
    print(f"::error title=Sakai server check failed::{title}")
    for line in (first + "\n" + cause + "\n" + excerpt).splitlines():
        print(f"| {line}")
    report.write_text(
        f"### Sakai server failure\n\n{title}\n\n"
        f"First failure:\n\n````text\n{first}\n````\n\n"
        f"Deepest logged cause:\n\n````text\n{cause}\n````\n\n"
        f"Log excerpt:\n\n````text\n{excerpt}\n````\n"
    )
    print(f"See {report} and the tomcat-log artifact for details.")
    return 1


def check_server(tomcat, report, wait_seconds=0, url="http://localhost:8080/portal/"):
    deadline = time.monotonic() + wait_seconds
    while True:
        paths = sorted((tomcat / "logs").glob("*"))
        # Prefer catalina.out, which contains the combined startup output.
        paths.sort(key=lambda path: path.name != "catalina.out")
        logs = [(path, path.read_text(errors="replace")) for path in paths if path.is_file()]
        failure = fatal_error(logs)
        if failure:
            return report_failure(report, *failure)
        if not process_alive(tomcat / "temp/catalina.pid"):
            return report_failure(report, "Tomcat is no longer running.")
        if not wait_seconds:
            if not logs:
                return report_failure(report, "No Tomcat logs were produced.")
            return 0
        if any("Server startup in" in content for _, content in logs) and portal_ready(url):
            # Recheck logs after the HTTP request, which may initialize components.
            return check_server(tomcat, report)
        if time.monotonic() >= deadline:
            tail = "\n".join(next((content for path, content in logs if path.name == "catalina.out"), "").splitlines()[-80:])
            return report_failure(report, "Sakai readiness timed out: startup completion and a working portal are required.", excerpt=tail)
        time.sleep(5)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tomcat", type=Path, default=Path("tomcat"))
    parser.add_argument("--report", type=Path, default=Path("sakai-server-failure.md"))
    parser.add_argument("--wait-seconds", type=int, default=0)
    args = parser.parse_args()
    raise SystemExit(check_server(args.tomcat, args.report, args.wait_seconds))
