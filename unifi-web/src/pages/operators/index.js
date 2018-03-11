import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { Route, Switch } from 'react-router'

import * as ROUTES from 'utils/routes'
import OperatorList from './operator-list'
import { listOperators } from 'redux/operator/actions'
import { PageContent } from 'components'
import { PageContainer } from 'smart-components'
import { withClientId } from 'hocs'
import { userIsAuthenticatedRedir } from 'hocs/auth'

class Directory extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    listOperators: PropTypes.func.isRequired
  };

  componentDidMount() {
    const { clientId, listOperators } = this.props
    listOperators({ clientId })
  }

  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <Switch>
              <Route
                exact
                path={ROUTES.OPERATORS}
                component={OperatorList}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export const actions = {
  listOperators
}

export default compose(
  userIsAuthenticatedRedir,
  withClientId,
  connect(null, actions),
)(Directory)
