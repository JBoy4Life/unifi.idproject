import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import { Table } from 'elements'

const columns = [
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
    render: (name, row) => (
      <Link to={ROUTES.DIRECTORY_HOLDER_DETAIL.replace(':clientReference', row.clientReference)}>
        {name}
      </Link>
    ),
    sorter: (a, b) => a.name.localeCompare(b.name)
  },
  {
    title: 'ID',
    dataIndex: 'clientReference',
    key: 'clientReference',
  },
  {
    title: 'Status',
    dataIndex: 'active',
    key: 'active',
    render: active => active ? 'Active' : 'Deactivated',
  }
]

const ListView = ({ holders }) => (
  <Table dataSource={holders} columns={columns} pagination={false} rowKey="clientReference" />
)

export default ListView
