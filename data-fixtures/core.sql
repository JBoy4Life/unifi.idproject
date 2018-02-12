SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = core, pg_catalog;


COPY client (client_id, display_name) FROM stdin;
deloitte	Deloitte
ucl-som	UCL School of Management
\.


COPY site (client_id, site_id, description, address) FROM stdin;
deloitte	1nss	1 New Square Street	London
ucl-som	level38	Level 38	Level 38, One Canada Square, Canary Wharf, London, E14 5AA
\.


COPY reader (client_id, site_id, reader_sn, endpoint) FROM stdin;
deloitte	1nss	37017090611	192.168.42.161:5084
deloitte	1nss	37017090612	192.168.42.162:5084
deloitte	1nss	37017090613	192.168.42.163:5084
deloitte	1nss	37017090614	192.168.42.164:5084
deloitte	1nss	37017090615	192.168.42.165:5084
ucl-som	level38	37017090614	192.168.42.167:5084
\.


COPY zone (client_id, site_id, zone_id, name, description) FROM stdin;
deloitte	1nss	1A	Floor 1/A	
deloitte	1nss	1B	Floor 1/B	
deloitte	1nss	2A	Floor 2/A	
deloitte	1nss	2B	Floor 2/B	
deloitte	1nss	3A	Floor 3/A	
deloitte	1nss	3B	Floor 3/B	
deloitte	1nss	3C	Floor 3/C	
ucl-som	level38	north-east-lt	North East Lecture Theatre	
ucl-som	level38	seminar-room	Seminar Room S10	
ucl-som	level38	south-east-lt	South East Lecture Theatre	
\.


COPY antenna (client_id, site_id, reader_sn, port_number, zone_id) FROM stdin;
deloitte	1nss	37017090611	1	1A
deloitte	1nss	37017090611	2	1A
deloitte	1nss	37017090611	3	1B
deloitte	1nss	37017090611	4	2A
deloitte	1nss	37017090612	1	2A
deloitte	1nss	37017090612	2	2B
deloitte	1nss	37017090612	3	2B
deloitte	1nss	37017090613	1	2B
deloitte	1nss	37017090613	2	3A
deloitte	1nss	37017090613	4	3A
deloitte	1nss	37017090614	1	3A
deloitte	1nss	37017090614	2	3B
deloitte	1nss	37017090615	1	3B
deloitte	1nss	37017090615	2	2B
deloitte	1nss	37017090615	3	3C
deloitte	1nss	37017090615	4	3C
ucl-som	level38	37017090614	1	north-east-lt
\.


COPY holder (client_id, client_reference, holder_type, name, active) FROM stdin;
deloitte	56895	contact	Penny Rimbauld	t
deloitte	10001	contact	Davina Hatch	t
deloitte	10002	contact	Vanessa Cunningham	t
deloitte	10003	contact	Iain Graham	t
deloitte	10004	contact	Steven McMillan	t
deloitte	10005	contact	Leonardo González	t
deloitte	10006	contact	Kevin Smith	t
deloitte	10007	contact	Jennifer Brady	t
deloitte	10008	contact	Ben Daniels	t
deloitte	10009	contact	Cher Wang	t
deloitte	10010	contact	Cheryl Hubbard	t
deloitte	10011	contact	Diane McKenzie	t
deloitte	10012	contact	Kenneth Lin	t
deloitte	10013	contact	George Stewart	t
ucl-som	WDAL17	contact	Waylon Dalton	t
ucl-som	TCOB17	contact	Thalia Cobb	t
ucl-som	JHEN17	contact	Justine Henderson	t
ucl-som	AWAL17	contact	Angela Walker	t
ucl-som	HHAR17	contact	Hadassah Hartman	t
ucl-som	LSHE17	contact	Lia Shelton	t
ucl-som	ERAN17	contact	Eddie Randolph	t
\.


COPY detectable (client_id, detectable_id, detectable_type, description) FROM stdin;
deloitte	0001	uhf-epc	
deloitte	0002	uhf-epc	
deloitte	0003	uhf-epc	
deloitte	0004	uhf-epc	
deloitte	0005	uhf-epc	
deloitte	0006	uhf-epc	
deloitte	0007	uhf-epc	
deloitte	0008	uhf-epc	
deloitte	0009	uhf-epc	
deloitte	0010	uhf-epc	
deloitte	0011	uhf-epc	
deloitte	0012	uhf-epc	
deloitte	0013	uhf-epc	
deloitte	6895	uhf-epc	
ucl-som	111100000000000000000000	uhf-epc	
\.


COPY assignment (client_id, detectable_type, detectable_id, client_reference) FROM stdin;
deloitte	uhf-epc	0001	10001
deloitte	uhf-epc	0002	10002
deloitte	uhf-epc	0003	10003
deloitte	uhf-epc	0004	10004
deloitte	uhf-epc	0005	10005
deloitte	uhf-epc	0006	10006
deloitte	uhf-epc	0007	10007
deloitte	uhf-epc	0008	10008
deloitte	uhf-epc	0009	10009
deloitte	uhf-epc	0010	10010
deloitte	uhf-epc	0011	10011
deloitte	uhf-epc	0012	10012
deloitte	uhf-epc	0013	10013
deloitte	uhf-epc	6895	56895
ucl-som	uhf-epc	111100000000000000000000	WDAL17
\.


COPY contact (client_id, client_reference, holder_type) FROM stdin;
deloitte	56895	contact
deloitte	10001	contact
deloitte	10002	contact
deloitte	10003	contact
deloitte	10004	contact
deloitte	10005	contact
deloitte	10006	contact
deloitte	10007	contact
deloitte	10008	contact
deloitte	10009	contact
deloitte	10010	contact
deloitte	10011	contact
deloitte	10012	contact
deloitte	10013	contact
ucl-som	WDAL17	contact
ucl-som	TCOB17	contact
ucl-som	JHEN17	contact
ucl-som	AWAL17	contact
ucl-som	HHAR17	contact
ucl-som	LSHE17	contact
ucl-som	ERAN17	contact
\.


COPY operator (client_id, username, name, email, active, since) FROM stdin;
deloitte	test	Deloitte Tester	test@example.com	t	2017-12-27 13:49:25.159918
ucl-som	test	UCL SoM Tester	test@example.com	t	2017-12-31 13:49:25.159918
\.


COPY operator_password (client_id, username, password_hash, algorithm, since) FROM stdin;
deloitte	test	\\x7363720c00040001096b58f4701504702636a4667f808c2f69e21b47c8067972e54db533800e77f835453b3bcdf613451254c03ec42b6923	scrypt	2017-12-27 13:50:16.505975
ucl-som	test	\\x7363720c00040001096b58f4701504702636a4667f808c2f69e21b47c8067972e54db533800e77f835453b3bcdf613451254c03ec42b6923	scrypt	2017-12-30 13:50:16.505975
\.

