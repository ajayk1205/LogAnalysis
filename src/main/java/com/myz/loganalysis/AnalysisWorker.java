package com.myz.loganalysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.myz.loganalysis.config.AnalysisConfigurator;
import com.myz.loganalysis.config.LogEntity;

public abstract class AnalysisWorker implements Runnable {
    private static Logger logger = Logger.getLogger(AnalysisWorker.class);

    protected static String SCRIPT_EXEC = AnalysisConfigurator.getInstance().getConfig().getScriptExec();

    
    protected LogEntity logentity;
    protected String file;

    public AnalysisWorker(LogEntity logentity, String file) {
        this.logentity = logentity;
        this.file = file;
    }

    protected abstract Process createAnalysisProcess() throws Exception;
    
    public void run() {
        
        logger.info("Analyzing file:" + file);
        try {
            Process process = createAnalysisProcess();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            HashMap<String, String> resultMap = new HashMap<String, String>();
            String line = reader.readLine();
            while (line != null) {
                if (!"".equals(line)) {
                    String[] arr = line.split("=");
                    if (arr != null && arr.length == 2) {
                        resultMap.put(arr[0], arr[1]);
                    }
                }

                logger.debug(line);

                line = reader.readLine();

            }

            LogAnalysisMonitor.getInstance().addLogStatistic(logentity, resultMap);

            logger.info("Analysis completed for file:" + file);
        } catch (Exception e) {
            logger.error("error when analyzing file:" + file, e);
        } finally {
            try {
                LogAnalysisMonitor.getInstance().countDown();
            } catch (Exception e) {
                logger.error("error when analyzing file:" + file, e);
            }
        }
    }

}
