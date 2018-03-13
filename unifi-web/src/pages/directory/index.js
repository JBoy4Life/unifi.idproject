import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { Route, Switch /*, Redirect*/ } from 'react-router'

import * as ROUTES from 'utils/routes'
import ContactDetails from './contact-details'
import ContactList from './contact-list'
import { listHolders } from 'redux/holders/actions'
import { PageContent } from 'components'
import { PageContainer } from 'smart-components'
import { withClientId } from 'hocs'
import { userIsAuthenticatedRedir } from 'hocs/auth'

class Directory extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    listHolders: PropTypes.func.isRequired
  };

  componentDidMount() {
    const { clientId, listHolders } = this.props
    listHolders({
      clientId,
      with: ['image', 'detectable-type']
    })
  }

  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <Switch>
              <Route
                exact
                path={ROUTES.DIRECTORY}
                component={ContactList}
              />
              <Route
                exact
                path={ROUTES.DIRECTORY_HOLDER_DETAIL}
                component={ContactDetails}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export const actions = {
  listHolders
}

export default compose(
  userIsAuthenticatedRedir,
  withClientId,
  connect(null, actions),
)(Directory)
