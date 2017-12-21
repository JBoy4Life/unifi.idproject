--
-- PostgreSQL database dump
--

-- Dumped from database version 10.1
-- Dumped by pg_dump version 10.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = core, pg_catalog;

--
-- Data for Name: client; Type: TABLE DATA; Schema: core; Owner: coudy
--

COPY client (client_id, display_name, logo, register_date) FROM stdin;
deloitte	Deloitte	\\x	2017-12-21 01:07:26.142889
\.


--
-- Data for Name: site; Type: TABLE DATA; Schema: core; Owner: coudy
--

COPY site (client_id, site_id, description, address) FROM stdin;
deloitte	1nss	1 New Square Street	London
\.


--
-- Data for Name: reader; Type: TABLE DATA; Schema: core; Owner: coudy
--

COPY reader (client_id, site_id, reader_sn, host_name) FROM stdin;
deloitte	1nss	370-17-09-0611	192.168.42.161
deloitte	1nss	370-17-09-0612	192.168.42.162
deloitte	1nss	370-17-09-0613	192.168.42.163
deloitte	1nss	370-17-09-0614	192.168.42.164
deloitte	1nss	370-17-09-0615	192.168.42.165
\.


--
-- Data for Name: carrier; Type: TABLE DATA; Schema: core; Owner: coudy
--

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


--
-- Data for Name: holder; Type: TABLE DATA; Schema: core; Owner: coudy
--

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
\.


--
-- Data for Name: assignment; Type: TABLE DATA; Schema: core; Owner: coudy
--

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


--
-- Data for Name: contact; Type: TABLE DATA; Schema: core; Owner: coudy
--

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
\.


--
-- Data for Name: detectable; Type: TABLE DATA; Schema: core; Owner: coudy
--

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

COPY zone (client_id, site_id, zone_id, name, description) FROM stdin;
deloitte	1nss	1A	Floor 1/A	
deloitte	1nss	1B	Floor 1/B	
deloitte	1nss	2A	Floor 2/A	
deloitte	1nss	2B	Floor 2/B	
deloitte	1nss	3A	Floor 3/A	
deloitte	1nss	3B	Floor 3/B	
deloitte	1nss	3C	Floor 3/C	
\.


--
-- Data for Name: antenna; Type: TABLE DATA; Schema: core; Owner: coudy
--

COPY antenna (client_id, site_id, reader_sn, port_number, zone_id) FROM stdin;
deloitte	1nss	370-17-09-0611	1	1A
deloitte	1nss	370-17-09-0611	2	1A
deloitte	1nss	370-17-09-0611	3	1B
deloitte	1nss	370-17-09-0611	4	2A
deloitte	1nss	370-17-09-0612	1	2A
deloitte	1nss	370-17-09-0612	2	2B
deloitte	1nss	370-17-09-0612	3	2B
deloitte	1nss	370-17-09-0613	1	2B
deloitte	1nss	370-17-09-0613	2	3A
deloitte	1nss	370-17-09-0613	4	3A
deloitte	1nss	370-17-09-0614	1	3A
deloitte	1nss	370-17-09-0614	2	3B
deloitte	1nss	370-17-09-0615	1	3B
deloitte	1nss	370-17-09-0615	2	2B
deloitte	1nss	370-17-09-0615	3	3C
deloitte	1nss	370-17-09-0615	4	3C
\.


--
-- PostgreSQL database dump complete
--

