import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'

const ListView = ({ holders }) => (
  <div>
    <table className="unifi-table">
      <thead>
        <tr>
          <th width="70%">Name</th>
          <th width="15%">ID</th>
          <th width="15%">Status</th>
        </tr>
      </thead>
      <tbody>
        {holders.map((holder) => (
          <tr key={holder.clientReference}>
            <td>
              <Link to={ROUTES.DIRECTORY_HOLDER_DETAIL.replace(':clientReference', holder.clientReference)}>
                {holder.name}
              </Link>
            </td>
            <td>
              {holder.clientReference}
            </td>
            <td>
              {holder.active ? 'Active' : 'Deactivated'}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
)

export default ListView
