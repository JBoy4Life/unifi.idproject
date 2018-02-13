SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = attendance, core, pg_catalog;

COPY client (client_id, display_name) FROM stdin;
ucl-som	UCL School of Management
\.


COPY core.client_config (client_id, delete_detections_after, live_view_enabled) FROM stdin;
ucl-som	28 days	f
\.


COPY core.client_vertical (client_id, vertical_id) FROM stdin;
ucl-som	attendance
\.


COPY attendance.client_config (client_id, vertical_id, grace_period_before_block, grace_period_after_block) FROM stdin;
ucl-som	attendance	15 minutes	15 minutes
\.


COPY operator (client_id, username, name, email, active) FROM stdin;
ucl-som	test	Tester	test@example.org	t
\.


COPY operator_password (client_id, username, password_hash, algorithm) FROM stdin;
ucl-som	test	\\x7363720c00040001096b58f4701504702636a4667f808c2f69e21b47c8067972e54db533800e77f835453b3bcdf613451254c03ec42b6923	scrypt
\.


COPY site (client_id, site_id, description, address) FROM stdin;
ucl-som	level38	Level 38	Level 38, One Canada Square, Canary Wharf, London, E14 5AA
\.


COPY zone (client_id, site_id, zone_id, name, description) FROM stdin;
ucl-som	level38	north-east-lt	North East Lecture Theatre	
ucl-som	level38	seminar-room	Seminar Room S10	
ucl-som	level38	south-east-lt	South East Lecture Theatre	
\.


COPY reader (client_id, site_id, reader_sn, endpoint) FROM stdin;
ucl-som	level38	37016400949	128.16.22.241:5084
ucl-som	level38	37011330048	128.16.22.239:5084
ucl-som	level38	37017110229	128.16.22.21:5084
ucl-som	level38	37016440780	128.16.22.19:5084
\.


COPY antenna (client_id, site_id, reader_sn, port_number, zone_id) FROM stdin;
ucl-som	level38	37016400949	1	north-east-lt
ucl-som	level38	37016400949	2	north-east-lt
ucl-som	level38	37016400949	3	north-east-lt
ucl-som	level38	37016400949	4	north-east-lt
ucl-som	level38	37011330048	1	seminar-room
ucl-som	level38	37011330048	2	seminar-room
ucl-som	level38	37011330048	3	seminar-room
ucl-som	level38	37011330048	4	seminar-room
ucl-som	level38	37017110229	1	seminar-room
ucl-som	level38	37017110229	2	seminar-room
ucl-som	level38	37017110229	3	seminar-room
ucl-som	level38	37017110229	4	seminar-room
ucl-som	level38	37016440780	1	south-east-lt
ucl-som	level38	37016440780	2	south-east-lt
ucl-som	level38	37016440780	3	south-east-lt
ucl-som	level38	37016440780	4	south-east-lt
\.
