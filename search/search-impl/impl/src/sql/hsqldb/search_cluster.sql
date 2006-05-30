CREATE TABLE search_segments
(
	name_ varchar(255) not null,
	version_ BIGINT not null,
	size_ BIGINT not null,
	packet_ BINARY,
    CONSTRAINT search_segments_index UNIQUE (name_)
);
