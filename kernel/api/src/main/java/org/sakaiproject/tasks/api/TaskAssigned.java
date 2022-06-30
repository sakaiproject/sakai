package org.sakaiproject.tasks.api;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "TASKS_ASSIGNED", indexes = {
    @Index(name = "IDX_TASKS_ASSIGNED", columnList = "TASK_ID")
})
public class TaskAssigned implements PersistableEntity<Long> {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "tasks_assigned_id_sequence")
	@SequenceGenerator(name = "tasks_assigned_id_sequence", sequenceName = "TASKS_ASSIGNED_S")
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne
	@JoinColumn(name = "TASK_ID", nullable = false)
	private Task task;

	@Column(name = "ASSIGNATION_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private AssignationType type;

	@Column(name = "OBJECT_ID", length = 99, nullable = true)
	private String objectId;
	
}
