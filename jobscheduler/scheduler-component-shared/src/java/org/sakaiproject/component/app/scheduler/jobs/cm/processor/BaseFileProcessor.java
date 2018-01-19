/**
 * Copyright (c) 2003 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.scheduler.jobs.cm.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.app.scheduler.jobs.cm.util.FileArchiveUtil;

@Slf4j
public abstract class BaseFileProcessor extends BaseProcessor implements FileProcessor {

    @Getter
    @Setter
    private String filename;

    @Getter
    @Setter
    private int columns = -1;

    @Getter
    @Setter
    private boolean headerRowPresent;

    @Getter
    @Setter
    private boolean archive;

    @Getter
    @Setter
    private FileArchiveUtil fileArchiveUtil;

    public ProcessorState init(Map<String, Object> config) {
        final String cName = this.getClass().getCanonicalName();

        BaseFileProcessorState fps = new BaseFileProcessorState();
        fps.setConfiguration(config);

        String fName = (String) getPropertyOrNull(config, cName + ".filename");
        String tempFileName = "";

        if (StringUtils.isBlank(fName)) {
            tempFileName = filename;
            log.info("processor configured to load default file: {}", tempFileName);
        } else {
            String baseDir = (String) getPropertyOrNull(config, "path.base");
            if (baseDir != null) {
                tempFileName = baseDir;
                if (!baseDir.endsWith(File.separator)) {
                    tempFileName += File.separator;
                }
            }
            tempFileName += fName;

            log.info("processor configured to load override file: {}", tempFileName);
        }

        fps.setFilename(tempFileName);

        int numCols = getColumns();

        String cols = (String) config.get(cName + ".columns");
        if (StringUtils.isNotBlank(cols)) {
            try {
                numCols = Integer.parseInt(cols);
            } catch (NumberFormatException nfe) {
                log.error("improper number format specified in ProcessorState configuration for property {} .columns", cName);
            }
        }

        fps.setColumns(numCols);

        boolean headerRow = headerRowPresent;
        String hRows = (String) config.get(cName + ".headerRowPresent");
        if (StringUtils.isNotBlank(hRows)) {
            headerRow = Boolean.parseBoolean(hRows);
        }

        fps.setHeaderRowPresent(headerRow);

        return fps;
    }

    protected static final Object getPropertyOrNull(final Map<String, Object> config, final String key) {
        if (config != null) {
            return config.get(key);
        }
        return null;
    }

    public void process(ProcessorState ps) throws Exception {

        if (!FileProcessorState.class.isAssignableFrom(ps.getClass())) {
            log.error("could not proceed with ProcessorState of type {}; it is not of type FileProcessorState", ps.getClass().getCanonicalName());
            throw new Exception("process(...) called with a ProcessorState which is not a FileProcessorState");
        }

        final FileProcessorState state = (FileProcessorState) ps;

        state.setStartDate(new java.util.Date());

        log.info("{} started {}", getProcessorTitle(), state.getStartDate());

        File dataFile = null;
        String filename = state.getFilename();

        dataFile = new File(filename);

        if (!dataFile.exists()) {
            log.error("{} not found", filename);
            state.appendError(filename + " not found");
            log.error("{} ended {}", getProcessorTitle(), state.getEndDate());
            return;
        }

        try (BufferedReader fr = new BufferedReader(new FileReader(dataFile))) {
            processFormattedFile(fr, state);
        } catch (Exception e) {
            log.error("file processing aborted for {} due to errors", filename, e);
        }

        if (archive) {
            if (fileArchiveUtil != null) {
                File dir = fileArchiveUtil.createArchiveFolder();
                dataFile.renameTo(new File(dir.getAbsoluteFile() + System.getProperty("file.separator") + dataFile.getName()));
            }
        }

        state.setEndDate(new java.util.Date());

        log.info("{} ended {}", getProcessorTitle(), state.getEndDate());
        log.info(getReport(state));

        // the only output given is if logger is set to info or above.  error output should
        // be given in lower levels (eg. ERROR, WARN).  So if we are not in INFO level logging
        // and we have errors, just dump the errors and skip the rest of the report.
        if (state.getErrorCnt() > 0 && !log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("Errors were encountered while processing the integration file \"");
            sb.append(getFilename()).append("\":\n");

            for (String error : state.getErrorList()) {
                sb.append(error).append("\n");
            }

            log.error(sb.toString());
        }
    }

    public abstract void processFormattedFile(BufferedReader fr, FileProcessorState state) throws Exception;
}