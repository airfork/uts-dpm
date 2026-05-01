create unique index if not exists dpms_active_w2w_color_id_uindex
    on dpms (w2w_color_id)
    where active and w2w_color_id is not null;
