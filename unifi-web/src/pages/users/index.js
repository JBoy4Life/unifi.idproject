import React, { Component } from 'react'

import { PageContainer } from 'smart-components'
import { userIsAuthenticatedRedir } from 'hocs/auth'

class Users extends Component {
  render() {
    return (
      <PageContainer>
        <h1>Users</h1>
      </PageContainer>
    )
  }
}

export default userIsAuthenticatedRedir(Users)
