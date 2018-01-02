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
ucl-mgmt	MSING062	MSING062 Financial Decision Making
ucl-mgmt	MSING024B	MSING024B Influence and Negotiations
\.


COPY block (client_id, schedule_id, block_id, name) FROM stdin;
ucl-mgmt	MSING062	01	Lecture 1
ucl-mgmt	MSING062	02	Lecture 2
ucl-mgmt	MSING062	03	Lecture 3
ucl-mgmt	MSING062	04	Lecture 4
ucl-mgmt	MSING062	05	Lecture 5
ucl-mgmt	MSING062	06	Lecture 6
ucl-mgmt	MSING062	07	Lecture 7
ucl-mgmt	MSING062	08	Lecture 8
ucl-mgmt	MSING062	09	Lecture 9
ucl-mgmt	MSING062	10	Lecture 10
ucl-mgmt	MSING062	11	Lecture 11
ucl-mgmt	MSING024B	01	Lecture 1
ucl-mgmt	MSING024B	02	Lecture 2
ucl-mgmt	MSING024B	03	Lecture 3
ucl-mgmt	MSING024B	04	Lecture 4
ucl-mgmt	MSING024B	05	Lecture 5
ucl-mgmt	MSING024B	06	Lecture 6
ucl-mgmt	MSING024B	07	Lecture 7
ucl-mgmt	MSING024B	08	Lecture 8
ucl-mgmt	MSING024B	09	Lecture 9
ucl-mgmt	MSING024B	10	Lecture 10
ucl-mgmt	MSING024B	11	Lecture 11
\.


COPY block_time (client_id, schedule_id, block_id, start_time, end_time, since) FROM stdin;
ucl-mgmt	MSING062	01	2018-01-09 09:00:00	2018-01-09 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	02	2018-01-16 09:00:00	2018-01-16 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	03	2018-01-23 09:00:00	2018-01-23 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	04	2018-01-30 09:00:00	2018-01-30 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	05	2018-02-06 09:00:00	2018-02-06 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	06	2018-02-13 09:00:00	2018-02-13 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	07	2018-02-20 09:00:00	2018-02-20 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	08	2018-02-27 09:00:00	2018-02-27 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	09	2018-03-06 09:00:00	2018-03-06 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	10	2018-03-13 09:00:00	2018-03-13 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING062	11	2018-03-20 09:00:00	2018-03-20 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	01	2018-01-11 09:00:00	2018-01-11 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	02	2018-01-18 09:00:00	2018-01-18 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	03	2018-01-25 09:00:00	2018-01-25 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	04	2018-02-01 09:00:00	2018-02-01 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	05	2018-02-08 09:00:00	2018-02-08 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	06	2018-02-15 09:00:00	2018-02-15 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	07	2018-02-22 09:00:00	2018-02-22 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	08	2018-03-01 09:00:00	2018-03-01 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	09	2018-03-08 09:00:00	2018-03-08 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	10	2018-03-15 09:00:00	2018-03-15 12:00:00	2017-12-31 18:00:00
ucl-mgmt	MSING024B	11	2018-03-22 09:00:00	2018-03-22 12:00:00	2017-12-31 18:00:00
\.


COPY block_zone (client_id, schedule_id, block_id, site_id, zone_id, since) FROM stdin;
ucl-mgmt	MSING062	01	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	02	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	03	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	04	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	05	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	06	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	07	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	08	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	09	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	10	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING062	11	level38	north-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	01	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	02	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	03	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	04	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	05	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	06	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	07	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	08	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	09	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	10	level38	south-east	2017-12-31 18:00:00
ucl-mgmt	MSING024B	11	level38	south-east	2017-12-31 18:00:00
\.


COPY assignment (client_id, client_reference, schedule_id) FROM stdin;
ucl-mgmt	WDAL17	MSING022
ucl-mgmt	WDAL17	MSING025
ucl-mgmt	WDAL17	MSING062
ucl-mgmt	WDAL17	MSING024B
ucl-mgmt	TCOB17	MSING022
ucl-mgmt	TCOB17	MSING025
ucl-mgmt	TCOB17	MSING007
ucl-mgmt	TCOB17	MSING062
ucl-mgmt	JHEN17	MSING022
ucl-mgmt	JHEN17	MSING028-B1
ucl-mgmt	JHEN17	MSING052
ucl-mgmt	JHEN17	MSING024B
ucl-mgmt	AWAL17	MSING007
ucl-mgmt	AWAL17	MSING028-B2
ucl-mgmt	AWAL17	MSING052
ucl-mgmt	AWAL17	MSING024B
ucl-mgmt	HHAR17	MSING007
ucl-mgmt	HHAR17	MSING052
ucl-mgmt	HHAR17	MSING028-B1
ucl-mgmt	HHAR17	MSING062
ucl-mgmt	HHAR17	MSING024B
ucl-mgmt	LSHE17	MSING028-B2
ucl-mgmt	LSHE17	MSING007
ucl-mgmt	LSHE17	MSING025
ucl-mgmt	LSHE17	MSING062
ucl-mgmt	LSHE17	MSING024B
ucl-mgmt	ERAN17	MSING025
ucl-mgmt	ERAN17	MSING007
ucl-mgmt	ERAN17	MSING024B
\.


COPY attendance (client_id, client_reference, schedule_id, block_id) FROM stdin;
ucl-mgmt	WDAL17	MSING062	01
ucl-mgmt	WDAL17	MSING062	02
ucl-mgmt	WDAL17	MSING062	03
ucl-mgmt	WDAL17	MSING062	04
ucl-mgmt	WDAL17	MSING062	06
ucl-mgmt	WDAL17	MSING062	07
ucl-mgmt	WDAL17	MSING062	09
ucl-mgmt	WDAL17	MSING062	10
ucl-mgmt	WDAL17	MSING062	11
ucl-mgmt	WDAL17	MSING024B	01
ucl-mgmt	WDAL17	MSING024B	02
ucl-mgmt	WDAL17	MSING024B	03
ucl-mgmt	WDAL17	MSING024B	04
ucl-mgmt	WDAL17	MSING024B	05
ucl-mgmt	WDAL17	MSING024B	06
ucl-mgmt	WDAL17	MSING024B	07
ucl-mgmt	WDAL17	MSING024B	09
ucl-mgmt	WDAL17	MSING024B	10
ucl-mgmt	WDAL17	MSING024B	11
ucl-mgmt	TCOB17	MSING062	01
ucl-mgmt	TCOB17	MSING062	02
ucl-mgmt	TCOB17	MSING062	03
ucl-mgmt	TCOB17	MSING062	04
ucl-mgmt	TCOB17	MSING062	05
ucl-mgmt	TCOB17	MSING062	06
ucl-mgmt	TCOB17	MSING062	07
ucl-mgmt	TCOB17	MSING062	08
ucl-mgmt	TCOB17	MSING062	09
ucl-mgmt	TCOB17	MSING062	10
ucl-mgmt	TCOB17	MSING062	11
ucl-mgmt	JHEN17	MSING024B	01
ucl-mgmt	JHEN17	MSING024B	02
ucl-mgmt	JHEN17	MSING024B	03
ucl-mgmt	JHEN17	MSING024B	05
ucl-mgmt	JHEN17	MSING024B	08
ucl-mgmt	JHEN17	MSING024B	09
ucl-mgmt	JHEN17	MSING024B	11
ucl-mgmt	AWAL17	MSING024B	01
ucl-mgmt	AWAL17	MSING024B	02
ucl-mgmt	AWAL17	MSING024B	03
ucl-mgmt	AWAL17	MSING024B	04
ucl-mgmt	AWAL17	MSING024B	05
ucl-mgmt	AWAL17	MSING024B	06
ucl-mgmt	AWAL17	MSING024B	08
ucl-mgmt	AWAL17	MSING024B	09
ucl-mgmt	AWAL17	MSING024B	10
ucl-mgmt	AWAL17	MSING024B	11
ucl-mgmt	HHAR17	MSING062	01
ucl-mgmt	HHAR17	MSING062	02
ucl-mgmt	HHAR17	MSING062	03
ucl-mgmt	HHAR17	MSING062	04
ucl-mgmt	HHAR17	MSING062	06
ucl-mgmt	HHAR17	MSING062	07
ucl-mgmt	HHAR17	MSING062	08
ucl-mgmt	HHAR17	MSING062	09
ucl-mgmt	HHAR17	MSING062	10
ucl-mgmt	HHAR17	MSING062	11
ucl-mgmt	HHAR17	MSING024B	01
ucl-mgmt	HHAR17	MSING024B	02
ucl-mgmt	HHAR17	MSING024B	03
ucl-mgmt	HHAR17	MSING024B	04
ucl-mgmt	HHAR17	MSING024B	06
ucl-mgmt	HHAR17	MSING024B	07
ucl-mgmt	HHAR17	MSING024B	09
ucl-mgmt	HHAR17	MSING024B	11
ucl-mgmt	LSHE17	MSING062	01
ucl-mgmt	LSHE17	MSING062	02
ucl-mgmt	LSHE17	MSING062	03
ucl-mgmt	LSHE17	MSING062	04
ucl-mgmt	LSHE17	MSING062	05
ucl-mgmt	LSHE17	MSING062	06
ucl-mgmt	LSHE17	MSING062	07
ucl-mgmt	LSHE17	MSING062	08
ucl-mgmt	LSHE17	MSING062	09
ucl-mgmt	LSHE17	MSING062	10
ucl-mgmt	LSHE17	MSING062	11
ucl-mgmt	LSHE17	MSING024B	01
ucl-mgmt	LSHE17	MSING024B	02
ucl-mgmt	LSHE17	MSING024B	03
ucl-mgmt	LSHE17	MSING024B	04
ucl-mgmt	LSHE17	MSING024B	05
ucl-mgmt	LSHE17	MSING024B	06
ucl-mgmt	LSHE17	MSING024B	07
ucl-mgmt	LSHE17	MSING024B	08
ucl-mgmt	LSHE17	MSING024B	09
ucl-mgmt	LSHE17	MSING024B	10
ucl-mgmt	LSHE17	MSING024B	11
ucl-mgmt	ERAN17	MSING024B	01
ucl-mgmt	ERAN17	MSING024B	02
ucl-mgmt	ERAN17	MSING024B	03
ucl-mgmt	ERAN17	MSING024B	04
ucl-mgmt	ERAN17	MSING024B	05
ucl-mgmt	ERAN17	MSING024B	06
ucl-mgmt	ERAN17	MSING024B	07
ucl-mgmt	ERAN17	MSING024B	08
ucl-mgmt	ERAN17	MSING024B	09
ucl-mgmt	ERAN17	MSING024B	10
ucl-mgmt	ERAN17	MSING024B	11
\.

COPY attendance_override (client_id, client_reference, schedule_id, block_id, status, operator, override_time) FROM stdin;
ucl-mgmt	WDAL17	MSING062	08	present	test	2018-04-01 12:00:00
ucl-mgmt	WDAL17	MSING062	05	auth-absent	test	2018-04-01 12:00:00
ucl-mgmt	LSHE17	MSING062	04	absent	test	2018-04-01 12:00:00
ucl-mgmt	HHAR17	MSING062	08	absent	test	2018-04-01 12:00:00
ucl-mgmt	TCOB17	MSING062	04	absent	test	2018-04-01 12:00:00
ucl-mgmt	TCOB17	MSING062	04	present	test	2018-04-02 12:00:00
ucl-mgmt	TCOB17	MSING062	11	auth-absent	test	2018-04-01 12:00:00
ucl-mgmt	TCOB17	MSING062	11	absent	test	2018-04-02 12:00:00
\.
