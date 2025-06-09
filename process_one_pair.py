import os
import sys
# Add current directory to sys.path to allow importing translation_helpers
sys.path.append(os.getcwd())
try:
    from translation_helpers import synchronize_translations, generate_properties_content
except ImportError:
    print("ERROR: Could not import translation_helpers. Make sure translation_helpers.py is in the current directory.", file=sys.stderr)
    sys.exit(1)

default_content = os.environ.get("DEFAULT_CONTENT")
translation_content = os.environ.get("TRANSLATION_CONTENT")
filepath = os.environ.get("TRANSLATION_FILEPATH")

if default_content is None or translation_content is None:
    print("ERROR: DEFAULT_CONTENT or TRANSLATION_CONTENT environment variables not set.", file=sys.stderr)
    sys.exit(1)

new_translation_map, report_messages = synchronize_translations(default_content, translation_content)
updated_translation_content = generate_properties_content(new_translation_map)

print("===UPDATED_TRANSLATION_CONTENT_START===")
print(updated_translation_content)
print("===UPDATED_TRANSLATION_CONTENT_END===")

if report_messages:
    print(f"Report for {filepath}:", file=sys.stderr)
    for msg in report_messages:
        print(msg, file=sys.stderr)
