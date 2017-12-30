SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = core, pg_catalog;


COPY client (client_id, display_name, logo, register_date) FROM stdin;
deloitte	Deloitte	\\x	2017-12-21 01:07:26.142889
ucl-mgmt	UCL School of Management	\\x	2017-12-30 14:22:26.142889
\.



COPY site (client_id, site_id, description, address) FROM stdin;
deloitte	1nss	1 New Square Street	London
ucl-mgmt	level38	Level 38	Level 38, One Canada Square, Canary Wharf, London, E14 5AA
\.


COPY reader (client_id, site_id, reader_sn, endpoint) FROM stdin;
deloitte	1nss	37017090611	192.168.42.161:5084
deloitte	1nss	37017090612	192.168.42.162:5084
deloitte	1nss	37017090613	192.168.42.163:5084
deloitte	1nss	37017090614	192.168.42.164:5084
deloitte	1nss	37017090615	192.168.42.165:5084
ucl-mgmt	level38	37016400949	192.168.1.101:5084
ucl-mgmt	level38	37011330048	192.168.1.102:5084
ucl-mgmt	level38	37017110229	192.168.1.103:5084
ucl-mgmt	level38	37016440780	192.168.1.104:5084
\.


COPY zone (client_id, site_id, zone_id, name, description) FROM stdin;
deloitte	1nss	1A	Floor 1/A	
deloitte	1nss	1B	Floor 1/B	
deloitte	1nss	2A	Floor 2/A	
deloitte	1nss	2B	Floor 2/B	
deloitte	1nss	3A	Floor 3/A	
deloitte	1nss	3B	Floor 3/B	
deloitte	1nss	3C	Floor 3/C	
ucl-mgmt	level38	north-east	North East Lecture Theatre	
ucl-mgmt	level38	seminar-suite	Seminar Suite	
ucl-mgmt	level38	south-east	South East Lecture Theatre	
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
ucl-mgmt	level38	37016400949	1	north-east
ucl-mgmt	level38	37016400949	2	north-east
ucl-mgmt	level38	37016400949	3	north-east
ucl-mgmt	level38	37016400949	4	north-east
ucl-mgmt	level38	37011330048	1	seminar-suite
ucl-mgmt	level38	37011330048	2	seminar-suite
ucl-mgmt	level38	37017110229	1	seminar-suite
ucl-mgmt	level38	37017110229	2	seminar-suite
ucl-mgmt	level38	37016440780	1	south-east
ucl-mgmt	level38	37016440780	2	south-east
ucl-mgmt	level38	37016440780	3	south-east
ucl-mgmt	level38	37016440780	4	south-east
\.


COPY carrier (client_id, carrier_id, carrier_type, active, since) FROM stdin;
deloitte	6895	card	t	2017-12-21 01:54:32.823979
deloitte	0001	card	t	2017-12-21 01:55:40.576122
deloitte	0002	card	t	2017-12-21 01:55:40.576122
deloitte	0003	card	t	2017-12-21 01:55:40.576122
deloitte	0004	card	t	2017-12-21 01:55:40.576122
deloitte	0005	card	t	2017-12-21 01:55:40.576122
deloitte	0006	card	t	2017-12-21 01:55:40.576122
deloitte	0007	card	t	2017-12-21 01:55:40.576122
deloitte	0008	card	t	2017-12-21 01:55:40.576122
deloitte	0009	card	t	2017-12-21 01:55:40.576122
deloitte	0010	card	t	2017-12-21 01:55:40.576122
deloitte	0011	card	t	2017-12-21 01:55:40.576122
deloitte	0012	card	t	2017-12-21 01:55:40.576122
deloitte	0013	card	t	2017-12-21 01:55:40.576122
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
ucl-mgmt	WDAL17	contact	Waylon Dalton	t
ucl-mgmt	TCOB17	contact	Thalia Cobb	t
ucl-mgmt	JHEN17	contact	Justine Henderson	t
ucl-mgmt	AWAL17	contact	Angela Walker	t
ucl-mgmt	HHAR17	contact	Hadassah Hartman	t
ucl-mgmt	LSHE17	contact	Lia Shelton	t
ucl-mgmt	ERAN17	contact	Eddie Randolph	t
\.


COPY assignment (client_id, carrier_id, carrier_type, client_reference, since) FROM stdin;
deloitte	6895	card	56895	2017-12-21 02:04:37.940159
deloitte	0001	card	10001	2017-12-21 02:06:37.573028
deloitte	0002	card	10002	2017-12-21 02:06:37.573028
deloitte	0003	card	10003	2017-12-21 02:06:37.573028
deloitte	0004	card	10004	2017-12-21 02:06:37.573028
deloitte	0005	card	10005	2017-12-21 02:06:37.573028
deloitte	0006	card	10006	2017-12-21 02:06:37.573028
deloitte	0007	card	10007	2017-12-21 02:06:37.573028
deloitte	0008	card	10008	2017-12-21 02:06:37.573028
deloitte	0009	card	10009	2017-12-21 02:06:37.573028
deloitte	0010	card	10010	2017-12-21 02:06:37.573028
deloitte	0011	card	10011	2017-12-21 02:06:37.573028
deloitte	0012	card	10012	2017-12-21 02:06:37.573028
deloitte	0013	card	10013	2017-12-21 02:06:37.573028
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
ucl-mgmt	WDAL17	contact
ucl-mgmt	TCOB17	contact
ucl-mgmt	JHEN17	contact
ucl-mgmt	AWAL17	contact
ucl-mgmt	HHAR17	contact
ucl-mgmt	LSHE17	contact
ucl-mgmt	ERAN17	contact
\.


COPY detectable (client_id, carrier_id, carrier_type, detectable_id, detectable_type) FROM stdin;
deloitte	0001	card	0001	uhf-epc
deloitte	0002	card	0002	uhf-epc
deloitte	0003	card	0003	uhf-epc
deloitte	0004	card	0004	uhf-epc
deloitte	0005	card	0005	uhf-epc
deloitte	0006	card	0006	uhf-epc
deloitte	0007	card	0007	uhf-epc
deloitte	0008	card	0008	uhf-epc
deloitte	0009	card	0009	uhf-epc
deloitte	0010	card	0010	uhf-epc
deloitte	0011	card	0011	uhf-epc
deloitte	0012	card	0012	uhf-epc
deloitte	0013	card	0013	uhf-epc
deloitte	6895	card	6895	uhf-epc
\.


COPY operator (client_id, username, email, active, since) FROM stdin;
deloitte	test	test@example.com	t	2017-12-27 13:49:25.159918
ucl-mgmt	test	test@example.com	t	2017-12-31 13:49:25.159918
\.


COPY operator_login_attempt (client_id, username, successful, attempt_time) FROM stdin;
\.


COPY operator_password (client_id, username, password_hash, algorithm, since) FROM stdin;
deloitte	test	\\x7363720c00040001096b58f4701504702636a4667f808c2f69e21b47c8067972e54db533800e77f835453b3bcdf613451254c03ec42b6923	scrypt	2017-12-27 13:50:16.505975
ucl-mgmt	test	\\x7363720c00040001096b58f4701504702636a4667f808c2f69e21b47c8067972e54db533800e77f835453b3bcdf613451254c03ec42b6923	scrypt	2017-12-30 13:50:16.505975
\.


COPY operator_password_reset (client_id, username, token_hash, algorithm, expiry_date, since) FROM stdin;
\.


COPY operator_password_reset_history (client_id, username, token_hash, algorithm, expiry_date, deletion_reason, since) FROM stdin;
\.
