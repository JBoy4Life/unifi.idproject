import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Route, Switch } from 'react-router'

import * as ROUTES from 'config/routes'
import OperatorEdit from './operator-edit'
import OperatorList from './operator-list'
import OperatorInvite from './operator-invite'
import { PageContent } from 'components'
import { PageContainer } from 'smart-components'
import { userIsAuthenticatedRedir } from 'hocs/auth'

const Operators = () => (
  <PageContainer>
    <PageContent>
      <PageContent.Main>
        <Switch>
          <Route
            exact
            path={ROUTES.OPERATORS}
            component={OperatorList}
          />
          <Route
            exact
            path={ROUTES.OPERATOR_EDIT}
            component={OperatorEdit}
          />
          <Route
            exact
            path={ROUTES.OPERATOR_INVITE}
            component={OperatorInvite}
          />
        </Switch>
      </PageContent.Main>
    </PageContent>
  </PageContainer>
)

export default userIsAuthenticatedRedir(Operators)
