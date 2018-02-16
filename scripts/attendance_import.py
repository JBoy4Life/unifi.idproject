# Import script used for UCL SoM

# Requires Python 2.7, pytz

# Current directory must contain schedules.csv, blocks.csv and contacts.csv
# with schedule_headers, block_headers and contacts_headers, respectively,
# in the first row.

# Standard output can be piped straight into psql as long as the client and
# its zones have already been defined.


from __future__ import print_function
import csv
import sys
import codecs
from pytz import timezone, utc
from uuid import uuid4
from itertools import product
from collections import Counter

from datetime import datetime


schedule_headers = frozenset(['schedule_id', 'name'])
block_headers = frozenset(['schedule_id', 'block_id', 'start_time', 'end_time', 'site_id', 'zone_id'])
contact_headers = frozenset(['client_reference', 'name', 'metadata', 'schedule_id'])
assignment_headers = frozenset(['client_reference', 'detectable_id:mifare-csn', 'detectable_id:uhf-epc'])

detectable_types = ['mifare-csn', 'uhf-epc']
london_tz = timezone('Europe/London')


def get_column_map(headers, first_row):
    m = {}
    for h in headers:
        try:
            m[h] = first_row.index(h)
        except ValueError:
            print("'{0}' column missing in first row: {1}".format(h, first_row), file=sys.stderr)
            exit(1)
    return m


def utf8_csv_reader(source, dialect=None, **kwargs):
    csv_reader = csv.reader(utf_8_encoder(source), dialect, **kwargs)
    for row in csv_reader:
        yield [unicode(cell, 'utf-8').lstrip(u'\ufeff').strip() for cell in row]  # strip UTF-8 BOM if present


def utf_8_encoder(unicode_csv_data):
    for line in unicode_csv_data:
        yield line.encode('utf-8')


def utf8_open(filename):
    return codecs.open(filename, 'r', encoding='utf-8')


def dump_schedules(client_id, schedule_names, blocks):
    dump('schedule (client_id, schedule_id, name)',
         ((client_id, schedule_id, name) for schedule_id, name in schedule_names.viewitems()))
    dump('block (client_id, schedule_id, block_id, name)',
         ((client_id, b['schedule_id'], b['block_id'], 'Lecture') for b in blocks))
    dump('block_time (client_id, schedule_id, block_id, start_time, end_time)',
         ((client_id, b['schedule_id'], b['block_id'], b['start_time'], b['end_time']) for b in blocks))
    dump('block_zone (client_id, schedule_id, block_id, site_id, zone_id)',
         ((client_id, b['schedule_id'], b['block_id'], b['site_id'], b['zone_id']) for b in blocks))


def dump_contacts(client_id, contacts):
    dump('holder (client_id, client_reference, holder_type, name)',
         ((client_id, c['client_reference'], 'contact', c['name']) for c in contacts))
    dump('holder_metadata (client_id, client_reference, metadata)',
         ((client_id, c['client_reference'], c['metadata']) for c in contacts))
    dump('contact (client_id, client_reference, holder_type)',
         ((client_id, c['client_reference'], 'contact') for c in contacts))
    dump('attendance.assignment (client_id, client_reference, schedule_id)',
         ((client_id, c['client_reference'], schedule_id) for c in contacts for schedule_id in c['schedule_id']))


def dump_assignments(client_id, assignments):
    dump('detectable (client_id, detectable_id, detectable_type, description)',
         ((client_id, a[t], t, '') for a, t in product(assignments, detectable_types)
          if a[t]))
    dump('core.assignment (client_id, detectable_id, detectable_type, client_reference)',
         ((client_id, a[t], t, a['client_reference']) for a, t in product(assignments, detectable_types)
          if a[t]))


def dump(row_def, data):
    print('COPY {0} FROM stdin;'.format(row_def).encode('utf-8'))
    for row in data:
        print(u'\t'.join(row).encode('utf-8'))
    print('\.')
    print()
    print()


def parse_timestamp(s): # TODO: maybe support other formats
    return london_tz.localize(datetime.strptime(s, '%d/%m/%y %H:%M')).astimezone(utc).isoformat()


def analyse(schedule_names, blocks, contacts, assignments):
    unknown_schedules_in_blocks = frozenset(b['schedule_id'] for b in blocks if b['schedule_id'] not in schedule_names)
    schedules_with_no_blocks = schedule_names.viewkeys() - frozenset(b['schedule_id'] for b in blocks)
    unknown_schedules_in_contacts = frozenset(schedule_id for c in contacts for schedule_id in c['schedule_id']
                                              if schedule_id not in schedule_names)
    unknown_client_references_in_assignments =\
        frozenset(a['client_reference'] for a in assignments
                  if a['client_reference'] not in [c['client_reference'] for c in contacts])

    duplicate_detectable_ids = [i for i, count in Counter([a['uhf-epc'] for a in assignments if a['uhf-epc']]).items() if count > 1]

    if unknown_schedules_in_blocks:
        print('Unknown schedule IDs in blocks: ', u', '.join(unknown_schedules_in_blocks), file=sys.stderr)
    if schedules_with_no_blocks:
        print('Schedules with no blocks: ', u', '.join(schedules_with_no_blocks), file=sys.stderr)
    if unknown_schedules_in_contacts:
        print('Unknown schedules in contacts: ', u', '.join(unknown_schedules_in_contacts), file=sys.stderr)
    if unknown_client_references_in_assignments:
        print('Unknown client references in assignments: ',
              u', '.join(unknown_client_references_in_assignments), file=sys.stderr)
    if duplicate_detectable_ids:
        print('Duplicate detectables: ', u', '.join(duplicate_detectable_ids), file=sys.stderr)


def main():
    if len(sys.argv) != 2:
        print('Usage: ', sys.argv[0], ' <client-id>', file=sys.stderr)
        exit(1)

    client_id = sys.argv[1]

    with utf8_open('schedules.csv') as f:
        reader = utf8_csv_reader(f)
        col = get_column_map(schedule_headers, reader.next())
        schedule_names = dict((row[col['schedule_id']], row[col['name']]) for row in reader)

    with utf8_open('blocks.csv') as f:
        reader = utf8_csv_reader(f)
        col = get_column_map(block_headers, reader.next())
        blocks = [{
            'schedule_id': row[col['schedule_id']],
            'block_id': row[col['block_id']] or str(uuid4()),
            'site_id': row[col['site_id']],
            'zone_id': row[col['zone_id']],
            'start_time': parse_timestamp(row[col['start_time']]),
            'end_time': parse_timestamp(row[col['end_time']]),
        } for row in reader]

        for block in blocks:
            block['start_time'] = block['start_time']
            if 'block_id' not in block:
                block['block_id'] = str(uuid4())

    with utf8_open('contacts.csv') as f:
        reader = utf8_csv_reader(f)
        col = get_column_map(contact_headers, reader.next())
        contacts = [{
            'client_reference': row[col['client_reference']],
            'name': row[col['name']],
            'metadata': row[col['metadata']],
            'schedule_id': frozenset(s.strip() for s in row[col['schedule_id']].split(u',') if s),
        } for row in reader]

    with utf8_open('assignments.csv') as f:
        reader = utf8_csv_reader(f)
        col = get_column_map(assignment_headers, reader.next())
        assignments = [{
            'client_reference': row[col['client_reference']],
            'mifare-csn': row[col['detectable_id:mifare-csn']],
            'uhf-epc': row[col['detectable_id:uhf-epc']],
        } for row in reader]
    analyse(schedule_names, blocks, contacts, assignments)

    print('''
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = attendance, core, pg_catalog;
''')
    dump_schedules(client_id, schedule_names, blocks)
    dump_contacts(client_id, contacts)
    dump_assignments(client_id, assignments)


if __name__ == "__main__":
    main()
