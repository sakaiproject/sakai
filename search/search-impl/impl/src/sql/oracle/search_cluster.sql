CREATE TABLE search_segments
(
	name_ varchar(256) not null,
	version_ NUMBER(20.0) not null,
	size_ NUMBER(20.0) not null,
	packet_ BLOB
);
CREATE UNIQUE INDEX search_segments_index ON search_segments
(
        name_
);
