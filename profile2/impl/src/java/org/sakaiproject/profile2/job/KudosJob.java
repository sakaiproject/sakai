package org.sakaiproject.profile2.job;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

/**
 * This is the Kudos calculation job.
 * 
 * <p>Certain items/events have weightings, these are calculated and summed to give a score.
 * <br />That score is then divided by the total number of possible items/weightings and converted to a percentage
 * to give the total kudos ranking for the user.
 * </p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class KudosJob implements StatefulJob {

	private static final Logger log = Logger.getLogger(KudosJob.class);
	
	private final String BEAN_ID = "org.sakaiproject.profile2.job.KudosJob";
	
	/**
	 * setup the rule map
	 */
	private final HashMap<String,BigDecimal> RULES = new HashMap<String,BigDecimal>() {
		{
			put("email", new BigDecimal(1));
			put("nickname", new BigDecimal(1));
			put("birthday", new BigDecimal(0.5));
			put("birthYear", new BigDecimal(0.5));

		}
	};
	
	//@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//abort if already running on THIS server node (cannot check other nodes)
		try{
			while(isJobCurrentlyRunning(context)) {
				String beanId = context.getJobDetail().getJobDataMap().getString(BEAN_ID);
				log.warn("Another instance of "+beanId+" is currently running - Execution aborted.");
				return;
			}
		}catch(SchedulerException e){
			log.error("Aborting job execution due to " +e.toString(), e);
			return;
		}
		
		log.error("KudosJob run");
		
		log.error("result:" + RULES.get("email"));
		log.error("result:" + RULES.get("nickname"));
		log.error("result:" + RULES.get("birthday"));
		log.error("result:" + RULES.get("birthYear"));
		
		BigDecimal total = getTotal();
		log.error("total:" + total);
		log.error("percent:" + getTotalAsPercentage(total));

		
	}
	
	/**
	 * Are multiples of this job currently running?
	 * @param context
	 * @return
	 * @throws SchedulerException
	 */
	private boolean isJobCurrentlyRunning(JobExecutionContext context) throws SchedulerException {
		String beanId = context.getJobDetail().getJobDataMap().getString(BEAN_ID);
		List<JobExecutionContext> jobsRunning = context.getScheduler().getCurrentlyExecutingJobs();
		
		int jobsCount = 0;
		for(JobExecutionContext j : jobsRunning)
			if(StringUtils.equals(beanId, j.getJobDetail().getJobDataMap().getString(BEAN_ID))) {
				jobsCount++; //this job=1, any more and they are multiples.
			}
		if(jobsCount > 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the total of all BigDecimals in the RULES map
	 * @param map
	 * @return
	 */
	public BigDecimal getTotal() {
		
		BigDecimal total = new BigDecimal(0);
		
		if(RULES != null) {
			for(Map.Entry<String,BigDecimal> entry : RULES.entrySet()) {
				total = total.add(entry.getValue());
			}
		}
		return total;
	}
	
	/**
	 * Gets the total as a percentage
	 * @param map
	 * @return
	 */
	public BigDecimal getTotalAsPercentage(BigDecimal total) {
		return total.divide(new BigDecimal(RULES.size())).multiply(new BigDecimal(100));
	}
	
	
	public void init(){
		log.info("KudosJob.init()");		
	}
	
}
