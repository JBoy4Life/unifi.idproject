import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Route, Switch } from 'react-router'

import * as ROUTES from 'utils/routes'
import ContactDetails from './contact-details'
import ContactEdit from './contact-edit'
import ContactList from './contact-list'
import ContactNew from './contact-new'
import { PageContent } from 'components'
import { PageContainer } from 'smart-components'
import { userIsAuthenticatedRedir } from 'hocs/auth'

class Directory extends Component {
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
                path={ROUTES.DIRECTORY_HOLDER_NEW}
                component={ContactNew}
              />
              <Route
                exact
                path={ROUTES.DIRECTORY_HOLDER_DETAIL}
                component={ContactDetails}
              />
              <Route
                exact
                path={ROUTES.DIRECTORY_HOLDER_EDIT}
                component={ContactEdit}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export default userIsAuthenticatedRedir(Directory)
