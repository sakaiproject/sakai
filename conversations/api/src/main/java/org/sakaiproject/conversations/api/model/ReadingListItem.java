package org.sakaiproject.conversations.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;

@Entity
@Table(name = "CONV_READINGLIST", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "USER_ID", "TOPIC_ID" })
})
@Getter
public class ReadingListItem {

    @Id
    @GeneratedValue
    @Column(name = "READINGLIST_ID")
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    //@JoinColumn(name = "USER_ID")
    private String userId;

    //@JoinTable(name = "CONV_READINGLIST_TOPIC")
    //@JoinColumn(name = "TOPIC_ID")
    @OneToOne
    @JoinColumn(name = "TOPIC_ID", nullable = false)
    //private List<Topic> topics = new ArrayList<>();
    private Topic topic;
}
