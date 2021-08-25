package org.sakaiproject.event.api;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table( name = "TINCANAPI_EVENT")
@NamedQuery( name = "getAllTincanapiEvent",
			 query = "from TincanapiEvent")

@Data
public class TincanapiEvent {

    @Id
    @Column(name = "EVENT")
    private String event;

    @Column(name = "VERB", length = 255, nullable = false)
    private String verb;

    @Column(name = "ORIGIN", length = 255, nullable = false)
    private String origin;

    @Column(name = "OBJECT", length = 255, nullable = false)
    private String object;
    
    @Column(name = "EVENT_SUPPLIER", length = 255)
    private String eventSupplier;
}