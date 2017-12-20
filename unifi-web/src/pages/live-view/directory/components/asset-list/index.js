import React from 'react'
import { Table } from '../../../../../elements'

const columns = [{
  title: 'Name',
  dataIndex: 'name',
  key: 'name',
}, {
  title: 'ID',
  dataIndex: 'id',
  key: 'id',
}, {
  title: 'Type',
  dataIndex: 'type',
  key: 'type',
}, {
  title: 'Last seen',
  dataIndex: 'last_seen_location',
  key: 'last_seen_location',
}, {
  title: 'Since',
  dataIndex: 'last_seen',
  key: 'last_seen',
}]

const AssetList = ({ items }) => (
  <Table dataSource={items} columns={columns} pagination={false} />
)

export default AssetList
