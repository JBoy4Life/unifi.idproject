import React, { Component } from 'react'

import { Table } from '../../../elements'

const columns = [{
  title: 'Client name',
  dataIndex: 'displayName',
  key: 'displayName',
  render: (text, record) => (
    <div>
      <img src={record.logo} alt={text} />
      <span>{text}</span>
    </div>
  ),
}]


export default class ClientListing extends Component {
  render() {
    return (
      <Table dataSource={this.props.clientList} columns={columns} pagination={false} />
    )
  }
}
