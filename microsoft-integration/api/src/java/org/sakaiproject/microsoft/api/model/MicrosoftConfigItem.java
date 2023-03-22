package org.sakaiproject.microsoft.api.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mc_config_item")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftConfigItem {

	@Id
	@Column(name = "item_key", nullable = false)
	private String key;

	@Column(name = "value", nullable = false)
	private String value;
	
	@Transient
	private Integer index;
}
