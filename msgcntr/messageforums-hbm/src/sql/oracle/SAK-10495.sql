delete from onc.MFR_TOPIC_T where PF_SURROGATEKEY in (select f11.ID from onc.MFR_PRIVATE_FORUM_T f11, onc.MFR_PRIVATE_FORUM_T f22
    where f11.ID != f22.ID 
    and f11.OWNER = f22.OWNER and f11.SURROGATEKEY = f22.SURROGATEKEY
    and f11.SURROGATEKEY is not null and f22.SURROGATEKEY is not null
    and f11.id not in (
        select min(id) from onc.MFR_PRIVATE_FORUM_T where ID in (select f1.ID from onc.MFR_PRIVATE_FORUM_T f1, onc.MFR_PRIVATE_FORUM_T f2
        where f1.ID != f2.ID 
        and f1.OWNER = f2.OWNER and f1.SURROGATEKEY = f2.SURROGATEKEY
        and f1.SURROGATEKEY is not null and f2.SURROGATEKEY is not null
    )
    group by OWNER, SURROGATEKEY
))

delete from onc.MFR_PRIVATE_FORUM_T where ID in
(select f11.ID from onc.MFR_PRIVATE_FORUM_T f11, onc.MFR_PRIVATE_FORUM_T f22
    where f11.ID != f22.ID 
    and f11.OWNER = f22.OWNER and f11.SURROGATEKEY = f22.SURROGATEKEY
    and f11.SURROGATEKEY is not null and f22.SURROGATEKEY is not null
    and f11.id not in (
        select min(id) from onc.MFR_PRIVATE_FORUM_T where ID in (select f1.ID from onc.MFR_PRIVATE_FORUM_T f1, onc.MFR_PRIVATE_FORUM_T f2
        where f1.ID != f2.ID 
        and f1.OWNER = f2.OWNER and f1.SURROGATEKEY = f2.SURROGATEKEY
        and f1.SURROGATEKEY is not null and f2.SURROGATEKEY is not null        
    )
    group by OWNER, SURROGATEKEY
))

delete from onc.MFR_TOPIC_T where PF_SURROGATEKEY in (
select f11.ID from onc.MFR_PRIVATE_FORUM_T f11, onc.MFR_PRIVATE_FORUM_T f22
    where f11.ID != f22.ID 
    and f11.OWNER = f22.OWNER
    and f11.SURROGATEKEY is null and f22.SURROGATEKEY is null
    and f11.id not in (select min(id) from onc.MFR_PRIVATE_FORUM_T where ID in (select f1.ID from onc.MFR_PRIVATE_FORUM_T f1, onc.MFR_PRIVATE_FORUM_T f2
        where f1.ID != f2.ID 
        and f1.OWNER = f2.OWNER
        and f1.SURROGATEKEY is null and f2.SURROGATEKEY is null
    )
    group by OWNER, SURROGATEKEY
))

delete from onc.MFR_PRIVATE_FORUM_T where ID in
(select f11.ID from onc.MFR_PRIVATE_FORUM_T f11, onc.MFR_PRIVATE_FORUM_T f22
    where f11.ID != f22.ID 
    and f11.OWNER = f22.OWNER
    and f11.SURROGATEKEY is null and f22.SURROGATEKEY is null
    and f11.id not in (select min(id) from onc.MFR_PRIVATE_FORUM_T where ID in (select f1.ID from onc.MFR_PRIVATE_FORUM_T f1, onc.MFR_PRIVATE_FORUM_T f2
        where f1.ID != f2.ID 
        and f1.OWNER = f2.OWNER
        and f1.SURROGATEKEY is null and f2.SURROGATEKEY is null
    )
    group by OWNER, SURROGATEKEY
))

alter table onc.MFR_PRIVATE_FORUM_T add constraint unique_mfr_pf_user_site unique (OWNER, SURROGATEKEY);