import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Route, Switch } from 'react-router'

import * as ROUTES from 'utils/routes'
import OperatorEdit from './operator-edit'
import OperatorList from './operator-list'
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
        </Switch>
      </PageContent.Main>
    </PageContent>
  </PageContainer>
)

export default userIsAuthenticatedRedir(Operators)
