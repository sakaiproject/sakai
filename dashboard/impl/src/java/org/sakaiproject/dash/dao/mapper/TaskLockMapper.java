package org.sakaiproject.dash.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.logic.TaskLock;
import org.springframework.jdbc.core.RowMapper;

public class TaskLockMapper implements RowMapper {

	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		TaskLock taskLock = new TaskLock();
		
		// select id,task,server_id,claim_time,last_update,has_lock from dash_task_lock where task=? order by claim_time
		taskLock.setId(rs.getLong("id"));
		taskLock.setTask(rs.getString("task"));
		taskLock.setServerId(rs.getString("server_id"));
		taskLock.setClaimTime(rs.getTimestamp("claim_time"));
		taskLock.setLastUpdate(rs.getTimestamp("last_update"));
		taskLock.setHasLock(rs.getBoolean("has_lock"));
		
		return taskLock;
	}

}
