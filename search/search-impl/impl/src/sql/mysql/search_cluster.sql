CREATE TABLE search_segments
(
	name_ varchar(255) not null,
	version_ BIGINT not null,
	size_ BIGINT not null,
	packet_ LONGBLOB
);
CREATE UNIQUE INDEX search_segments_index ON search_segments
(
        name_
);
