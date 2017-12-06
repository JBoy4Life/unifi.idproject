import React from 'react'
import { Table } from '../../elements'

const dataSource = [{
  key: '1',
  floor: 'Muster zones',
  people: 32,
  change_per_minute: 12,
  last_detection: new Date().getTime(),
}, {
  key: '2',
  floor: 'Ground Floor',
  people: 20,
  change_per_minute: 7,
  last_detection: new Date().getTime(),
}, {
  key: '3',
  floor: 'Stairwells',
  people: 102,
  change_per_minute: -12,
  last_detection: new Date().getTime(),
}, {
  key: '4',
  floor: 'First floor',
  people: 52,
  change_per_minute: 9,
  last_detection: new Date().getTime(),
}, {
  key: '5',
  floor: 'Second floor',
  people: 35,
  change_per_minute: 30,
  last_detection: new Date().getTime(),
}]

const columns = [{
  title: 'Floor/zone',
  dataIndex: 'floor',
  key: 'floor',
}, {
  title: 'People',
  dataIndex: 'people',
  key: 'people',
}, {
  title: 'Change/Minute',
  dataIndex: 'change_per_minute',
  key: 'change_per_minute',
}, {
  title: 'Last detecton',
  dataIndex: 'last_detection',
  key: 'last_detection',
}]


const EvacuationTable = () => (
  <Table dataSource={dataSource} columns={columns} pagination={false} />
)

export default EvacuationTable
