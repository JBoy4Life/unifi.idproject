import React, { Component, Fragment } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import ResendInvite from '../resend-invite'
import { PageContentUnderTitle } from 'components'
import { Table } from 'elements'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'operator-list-view'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const StatusInvited = ({ row }) => (
  <Fragment>
    <span className={bemE('status-invited')}>Invited</span>
    <ResendInvite row={row} />
  </Fragment>
)

const columns = [
  {
    title: 'Username',
    dataIndex: 'username',
    render: (name, row) => (
      <Link to={ROUTES.OPERATOR_EDIT.replace(':username', row.username)}>
        {name}
      </Link>
    ),
    sorter: (a, b) => a.username.localeCompare(b.username)
  },
  {
    title: 'Name',
    dataIndex: 'name',
    sorter: (a, b) => a.name.localeCompare(b.name)
  },
  {
    title: 'Email Address',
    dataIndex: 'email',
    sorter: (a, b) => a.email.localeCompare(b.email)
  },
  {
    title: 'Status',
    dataIndex: 'active',
    render: (active, row) => active
      ? row.hasPassword
        ? 'Active'
        : <StatusInvited row={row} />
      : 'Deactivated',
    width: '20%',
  }
]

const ListView = ({ operators }) => (
  <div className={COMPONENT_CSS_CLASSNAME}>
    {operators.length > 0 && (
      <PageContentUnderTitle className={bemE('count')}>
        Showing {operators.length} contacts
      </PageContentUnderTitle>
    )}
    <Table dataSource={operators} columns={columns} pagination={false} rowKey="username" />
  </div>
)

export default ListView
