CREATE TABLE IF NOT EXISTS public.t_proxy
(
    host VARCHAR(15) NOT NULL,
    port SMALLINT    NOT NULL,

    PRIMARY KEY (host, port)
);
CREATE TABLE IF NOT EXISTS public.t_proxy_source
(
    url      VARCHAR(2083) NOT NULL,
    selector VARCHAR(233)  NOT NULL,

    PRIMARY KEY (url, selector)
)