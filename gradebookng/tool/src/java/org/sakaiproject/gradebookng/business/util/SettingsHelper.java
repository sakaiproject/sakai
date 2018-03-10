package org.sakaiproject.gradebookng.business.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;

/**
 * Helpers for the settings pages
 */
public class SettingsHelper {

	/**
	 * Examines the grading schema list for duplicates
	 *
	 * @param schemaList list of GbGradingSchemaEntry. This should be data from the model as duplicates are not persisted.
	 * @return true if duplicate letter grades, false if not.
	 */
	public static boolean hasDuplicates(final List<GbGradingSchemaEntry> schemaList) {
		final List<String> letterGrades = schemaList.stream().map(GbGradingSchemaEntry::getGrade).collect(Collectors.toList());
		return !letterGrades.stream().filter(i -> Collections.frequency(letterGrades, i) > 1)
				.collect(Collectors.toSet()).isEmpty();
	}

	/**
	 * Convert map into list of objects which is easier to work with in the views
	 *
	 * @param bottomPercents map
	 * @return list of {@link GbGradingSchemaEntry}
	 */
	public static List<GbGradingSchemaEntry> asList(final Map<String, Double> bottomPercents) {
		final List<GbGradingSchemaEntry> rval = new ArrayList<>();
		bottomPercents.forEach((k, v) -> rval.add(new GbGradingSchemaEntry(k, v)));
		return rval;
	}

	/**
	 * Convert list of {@link GbGradingSchemaEntry} into a map.
	 *
	 * Note that new entries may be null so they need to be excluded. Also note that duplicates may be present so we cater for that also.
	 */
	public static Map<String, Double> asMap(final List<GbGradingSchemaEntry> gbGradingSchemaEntries) {
		return gbGradingSchemaEntries.stream()
				.filter(e -> StringUtils.isNotBlank(e.getGrade()))
				.filter(e -> e.getMinPercent() != null)
				.collect(Collectors.toMap(
						GbGradingSchemaEntry::getGrade,
						GbGradingSchemaEntry::getMinPercent,
						(k1, k2) -> k2));
	}
}

