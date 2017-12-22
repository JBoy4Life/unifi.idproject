import React from 'react'
import moment from 'moment'
import { Table } from '../../../../../elements'

const columns = [{
  title: 'Name',
  dataIndex: 'client.name',
  key: 'client.name',
}, {
  title: 'ID',
  dataIndex: 'clientReference',
  key: 'clientReference',
}, {
  title: 'Type',
  dataIndex: 'client.holderType',
  key: 'client.holderType',
}, {
  title: 'Last seen',
  dataIndex: 'zone.name',
  key: 'zone.name',
}, {
  title: 'Since',
  dataIndex: 'timestamp',
  key: 'timestamp',
  render(value) {
    const m = moment(value)
    return `${m.format('HH:MM:ss')} ${m.fromNow()}`
  },
}]

const AssetList = ({ items }) => (
  <Table dataSource={items} columns={columns} pagination={false} />
)

export default AssetList
