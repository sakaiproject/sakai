import importlib.util
import contextlib
import io
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch


spec = importlib.util.spec_from_file_location("startup", Path(__file__).with_name("check-sakai-startup.py"))
startup = importlib.util.module_from_spec(spec)
spec.loader.exec_module(startup)


class ServerCheckTest(unittest.TestCase):
    def setUp(self):
        self.enterContext(contextlib.redirect_stdout(io.StringIO()))
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.tomcat = Path(self.directory.name)
        (self.tomcat / "logs").mkdir()
        (self.tomcat / "temp").mkdir()
        (self.tomcat / "temp/catalina.pid").write_text(str(os.getpid()))
        self.log = self.tomcat / "logs/catalina.out"
        self.report = self.tomcat / "failure.md"

    def check(self, wait=0):
        return startup.check_server(self.tomcat, self.report, wait)

    def test_joda_failure_reports_deepest_cause_and_bounded_trace(self):
        self.log.write_text(
            "2026-10-16 12:00:00 ERROR org.springframework.beans.factory.BeanCreationException: search\n"
            + "\tat example.Frame.method(Frame.java:1)\n" * 120
            + "Caused by: java.util.ServiceConfigurationError: XContentOpenSearchExtension\n"
            + "Caused by: java.lang.NoClassDefFoundError: org/joda/time/ReadableInstant\n"
            + "Caused by: java.lang.ClassNotFoundException: org.joda.time.ReadableInstant\n"
            + "2026-10-16 12:00:01 INFO unrelated next record\n"
        )
        self.assertEqual(self.check(wait=1800), 1)
        report = self.report.read_text()
        self.assertIn("catalina.out:1", report)
        self.assertIn("Deepest logged cause:\n\n````text\nCaused by: java.lang.ClassNotFoundException", report)
        self.assertIn("... truncated ...", report)
        self.assertNotIn("unrelated next record", report)
        self.assertLess(report.count("example.Frame"), 80)

    def test_each_fatal_signature_fails_even_after_successful_startup(self):
        for failure in (
            "BeanCreationException", "NoClassDefFoundError", "NoSuchMethodError",
            "ServiceConfigurationError", "ExceptionInInitializerError",
            "One or more listeners failed to start",
            "Context [/portal] startup failed due to previous errors",
            "Error deploying web application archive [tool.war]",
            "Exception sending context initialized event to listener",
        ):
            with self.subTest(failure=failure):
                self.log.write_text("Server startup in [12345] milliseconds\n" + failure)
                self.assertEqual(self.check(), 1)

    def test_checks_other_tomcat_logs(self):
        self.log.write_text("Server startup in [12345] milliseconds\n")
        (self.tomcat / "logs/localhost.2026-10-16.log").write_text("Context [/tool] startup failed")
        self.assertEqual(self.check(), 1)

    def test_generic_error_or_exception_does_not_fail(self):
        self.log.write_text("ERROR Expected validation error\nHandled IllegalArgumentException\n")
        self.assertEqual(self.check(), 0)

    def test_dead_tomcat_fails_without_waiting(self):
        self.log.write_text("Starting\n")
        with patch.object(startup, "process_alive", return_value=False), patch.object(startup.time, "sleep") as sleep:
            self.assertEqual(self.check(wait=1800), 1)
            sleep.assert_not_called()

    def test_portal_and_startup_marker_are_both_required(self):
        for log, portal in (("Starting\n", True), ("Server startup in [1] milliseconds\n", False)):
            with self.subTest(log=log, portal=portal):
                self.log.write_text(log)
                with patch.object(startup, "portal_ready", return_value=portal), patch.object(startup.time, "monotonic", side_effect=[0, 1]):
                    self.assertEqual(self.check(wait=1), 1)
                    self.assertIn("readiness timed out", self.report.read_text())

    def test_healthy_startup_passes(self):
        self.log.write_text("Server startup in [1] milliseconds\n")
        with patch.object(startup, "portal_ready", return_value=True):
            self.assertEqual(self.check(wait=1800), 0)

    def test_http_request_initializing_broken_component_is_caught(self):
        self.log.write_text("Server startup in [1] milliseconds\n")

        def initialize_component(url):
            with self.log.open("a") as output:
                output.write("NoClassDefFoundError: missing.LazyComponent\n")
            return True

        with patch.object(startup, "portal_ready", side_effect=initialize_component):
            self.assertEqual(self.check(wait=1800), 1)

    def test_missing_logs_fail_final_check(self):
        self.assertEqual(self.check(), 1)

    def test_missing_or_invalid_pid_is_not_alive(self):
        pid_file = self.tomcat / "missing.pid"
        self.assertFalse(startup.process_alive(pid_file))
        for value in ("", "not-a-pid", "0", "-1"):
            pid_file.write_text(value)
            self.assertFalse(startup.process_alive(pid_file))

    def test_portal_probe_has_timeout_and_rejects_http_failures(self):
        with patch.object(startup.urllib.request, "urlopen", side_effect=TimeoutError) as request:
            self.assertFalse(startup.portal_ready("http://example.invalid/portal/"))
            request.assert_called_once_with("http://example.invalid/portal/", timeout=5)
        with patch.object(startup.urllib.request, "urlopen", side_effect=startup.urllib.error.HTTPError("url", 503, "Unavailable", {}, None)):
            self.assertFalse(startup.portal_ready("http://example.invalid/portal/"))


if __name__ == "__main__":
    unittest.main()
