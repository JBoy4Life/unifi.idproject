SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = attendance, pg_catalog;


COPY schedule (client_id, schedule_id, name) FROM stdin;
ucl-mgmt	MSING022	MSING022 Organisational Behaviour
ucl-mgmt	MSING025	MSING025 Corporate Strategy Group B2
ucl-mgmt	MSING007	MSING007 Entrepreneurial Finance
ucl-mgmt	MSING028-B1	MSING028 Investment Management Group B1
ucl-mgmt	MSING028-B2	MSING028 Investment Management Group B2
ucl-mgmt	MSING052	MSING052 Marketing Analytics
\.


COPY assignment (client_id, client_reference, schedule_id) FROM stdin;
ucl-mgmt	WDAL17	MSING022
ucl-mgmt	WDAL17	MSING025
ucl-mgmt	TCOB17	MSING022
ucl-mgmt	TCOB17	MSING025
ucl-mgmt	TCOB17	MSING007
ucl-mgmt	JHEN17	MSING022
ucl-mgmt	JHEN17	MSING028-B1
ucl-mgmt	JHEN17	MSING052
ucl-mgmt	AWAL17	MSING007
ucl-mgmt	AWAL17	MSING028-B2
ucl-mgmt	AWAL17	MSING052
ucl-mgmt	HHAR17	MSING007
ucl-mgmt	HHAR17	MSING052
ucl-mgmt	HHAR17	MSING028-B1
ucl-mgmt	LSHE17	MSING028-B2
ucl-mgmt	LSHE17	MSING007
ucl-mgmt	LSHE17	MSING025
ucl-mgmt	LSHE17	MSING022
ucl-mgmt	ERAN17	MSING025
ucl-mgmt	ERAN17	MSING007
\.


COPY block (client_id, schedule_id, block_id) FROM stdin;
\.


COPY attendance (client_id, client_reference, schedule_id, block_id) FROM stdin;
\.


COPY block_time (client_id, schedule_id, block_id, start_time, end_time, since) FROM stdin;
\.


COPY block_zone (client_id, schedule_id, block_id, site_id, zone_id, since) FROM stdin;
\.
