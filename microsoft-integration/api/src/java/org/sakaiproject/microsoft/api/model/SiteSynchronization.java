package org.sakaiproject.microsoft.api.model;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.sakaiproject.microsoft.api.converters.JpaConverterSynchronizationStatus;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.site.api.Site;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mc_site_synchronization", uniqueConstraints = { @UniqueConstraint(columnNames = { "site_id", "team_id" }) })
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class SiteSynchronization {

	@Id
	@Column(name = "id", length = 99, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "site_id", nullable = false)
	private String siteId;
	
	@Column(name = "team_id", nullable = false)
	private String teamId;
	
	@Column(name = "status")
	@Builder.Default
	@Convert(converter = JpaConverterSynchronizationStatus.class)
	private SynchronizationStatus status = SynchronizationStatus.NONE;
	
	@Column(name = "forced")
	private boolean forced;
	
	@Column(name = "status_updated_at")
	private ZonedDateTime statusUpdatedAt;
	
	@OneToMany(mappedBy = "siteSynchronization", fetch = FetchType.LAZY, orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	List<GroupSynchronization> groupSynchronizationsList;
	
	@Transient
	private Site site;
}
