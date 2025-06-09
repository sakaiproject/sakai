import os
import sys
import re

# Add current directory to sys.path to allow importing translation_helpers
sys.path.append(os.getcwd())
try:
    from translation_helpers import synchronize_translations, generate_properties_content, parse_properties, create_new_translation
except ImportError:
    print("ERROR: Could not import translation_helpers. Make sure translation_helpers.py is in the current directory.", file=sys.stderr)
    sys.exit(1)

def find_properties_files(base_dir):
    """Finds all .properties files in the given directory."""
    properties_files = []
    for root, _, files in os.walk(base_dir):
        for file in files:
            if file.endswith(".properties"):
                properties_files.append(os.path.join(root, file))
    return properties_files

def process_directory(base_dir_path):
    all_files = find_properties_files(base_dir_path)
    all_files.sort() # Process in a consistent order

    default_files = {} # name -> path
    translation_files = {} # name_lang -> path

    # Regex to identify language-specific files like _en.properties, _fr_FR.properties etc.
    # This helps in identifying base names correctly.
    lang_suffix_pattern = re.compile(r"_[a-z]{2}(_[A-Z]{2})?\.properties$")

    for f_path in all_files:
        filename = os.path.basename(f_path)
        # Is it a translation file like X_zh_CN.properties?
        if filename.endswith("_zh_CN.properties"):
            base_name = filename[:-len("_zh_CN.properties")]
            translation_files[base_name + "_zh_CN"] = f_path
        # Is it a default file (e.g. X.properties) and not another language's file?
        elif not lang_suffix_pattern.search(filename):
            base_name = filename[:-len(".properties")]
            default_files[base_name] = f_path
        # else, it's a translation for another language, ignore for now.

    overwrite_commands = []
    report_messages = []

    processed_translations = set()

    # Section 1: Process existing pairs
    for base_name, default_path in default_files.items():
        expected_zh_cn_key = base_name + "_zh_CN"
        zh_cn_path = translation_files.get(expected_zh_cn_key)

        if zh_cn_path:
            processed_translations.add(zh_cn_path)
            try:
                with open(default_path, 'r', encoding='utf-8') as f:
                    default_content = f.read()
                with open(zh_cn_path, 'r', encoding='utf-8') as f:
                    translation_content = f.read()

                new_translation_map, sync_reports = synchronize_translations(default_content, translation_content)
                if new_translation_map: # Only update if there's something to write
                    updated_content = generate_properties_content(new_translation_map)
                    overwrite_commands.append(f"overwrite_file_with_block\n{zh_cn_path}\n{updated_content}")
                report_messages.append(f"--- Report for {zh_cn_path} (Processed against {default_path}) ---")
                report_messages.extend(sync_reports)

            except FileNotFoundError as e:
                report_messages.append(f"ERROR: File not found during processing pair for {base_name}: {e}")
            except Exception as e:
                report_messages.append(f"ERROR: Failed to process pair for {base_name} ({default_path}, {zh_cn_path}): {e}")
        else:
            # This default file does not have a _zh_CN.properties counterpart.
            # This will be handled by Section 2 logic later (creating new files).
            report_messages.append(f"INFO: Default file {default_path} does not have a corresponding _zh_CN.properties file. (Will be handled by creation logic if listed in Section 2)")


    # Check for orphaned _zh_CN files (exist but their default doesn't)
    for zh_cn_key, zh_cn_path in translation_files.items():
        if zh_cn_path not in processed_translations:
            base_name_for_orphan = zh_cn_key[:-len("_zh_CN")]
            if base_name_for_orphan not in default_files:
                report_messages.append(f"WARNING: Orphaned translation file found: {zh_cn_path}. No corresponding default file '{base_name_for_orphan}.properties' found in the scanned directory.")

    # Print overwrite commands for the agent to use
    if overwrite_commands:
        print("===OVERWRITE_COMMANDS_START===")
        for cmd in overwrite_commands:
            print(cmd)
            if cmd != overwrite_commands[-1]: # Add a separator between commands
                 print("===NEXT_FILE===")
        print("===OVERWRITE_COMMANDS_END===")
    else:
        print("No files need overwriting in this directory based on existing pairs.", file=sys.stderr)


    # Print report messages
    if report_messages:
        print("\n--- Overall Report ---", file=sys.stderr)
        for msg in report_messages:
            print(msg, file=sys.stderr)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python process_directory.py <directory_path>", file=sys.stderr)
        sys.exit(1)

    directory_to_process = sys.argv[1]
    if not os.path.isdir(directory_to_process):
        print(f"Error: Provided path '{directory_to_process}' is not a directory.", file=sys.stderr)
        sys.exit(1)

    process_directory(directory_to_process)
