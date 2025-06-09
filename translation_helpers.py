import re

def parse_properties(file_content):
    """Parses a .properties file content into a dictionary."""
    properties = {}
    lines = file_content.splitlines()
    for line in lines:
        line = line.strip()
        if not line or line.startswith('#') or line.startswith('!'):
            continue
        if '=' in line:
            key, value = line.split('=', 1)
            properties[key.strip()] = value.strip()
    return properties

def generate_properties_content(properties_map):
    """Generates content for a .properties file from a dictionary."""
    lines = []
    for key, value in sorted(properties_map.items()): # Sort for consistent output
        lines.append(f"{key}={value}")
    return "\n".join(lines)

def synchronize_translations(default_content, translation_content):
    """
    Synchronizes translation content with default content.
    Returns a tuple: (new_translation_map, report_messages)
    """
    default_props = parse_properties(default_content)
    translation_props = parse_properties(translation_content)
    new_translation_map = {}
    report_messages = []

    # Process keys from default file
    for key, default_value in default_props.items():
        if key in translation_props:
            new_translation_map[key] = translation_props[key]
        else:
            new_translation_map[key] = f"TODO_TRANSLATE:{default_value}"
            report_messages.append(f"INFO: Added missing key '{key}' to translation with TODO_TRANSLATE prefix.")

    # Identify obsolete keys (present in translation but not in default)
    for key in translation_props:
        if key not in default_props:
            report_messages.append(f"INFO: Removed obsolete key '{key}' from translation.")

    return new_translation_map, report_messages

def create_new_translation(default_content):
    """
    Creates new translation content from default content with TODO_TRANSLATE prefixes.
    Returns a tuple: (new_translation_map, report_messages)
    """
    default_props = parse_properties(default_content)
    new_translation_map = {}
    report_messages = []

    for key, default_value in default_props.items():
        new_translation_map[key] = f"TODO_TRANSLATE:{default_value}"
    report_messages.append("INFO: Created new translation file with all keys prefixed with TODO_TRANSLATE.")
    return new_translation_map, report_messages
