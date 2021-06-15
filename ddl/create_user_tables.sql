CREATE TABLE IF NOT EXISTS public.t_proxy
(
    host VARCHAR(15) NOT NULL,
    port SMALLINT    NOT NULL,

    PRIMARY KEY (host, port)
)