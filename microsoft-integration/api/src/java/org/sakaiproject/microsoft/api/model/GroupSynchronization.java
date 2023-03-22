package org.sakaiproject.microsoft.api.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.microsoft.api.converters.JpaConverterSynchronizationStatus;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mc_group_synchronization", uniqueConstraints = { @UniqueConstraint(columnNames = { "parentId", "group_id", "channel_id" }) })
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class GroupSynchronization {

	@Id
	@Column(name = "id", length = 99, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parentId")
	private SiteSynchronization siteSynchronization;

	@Column(name = "group_id", nullable = false)
	private String groupId;
	
	@Column(name = "channel_id", nullable = false)
	private String channelId;
	
	@Column(name = "status")
	@Builder.Default
	@Convert(converter = JpaConverterSynchronizationStatus.class)
	private SynchronizationStatus status = SynchronizationStatus.NONE;
	
	@Column(name = "status_updated_at")
	private ZonedDateTime statusUpdatedAt;
}
