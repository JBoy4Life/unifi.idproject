import React, { Component } from 'react'
import { Route, Switch /*, Redirect*/ } from 'react-router'

import * as ROUTES from 'utils/routes'
import ContactDetails from './contact-details'
import ContactList from './contact-list'
import { PageContent } from 'components'
import { PageContainer } from 'smart-components'

export default class Directory extends Component {
  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <Switch>
              <Route
                path={ROUTES.DIRECTORY}
                component={ContactList}
              />
              <Route
                exact
                path={ROUTES.DIRECTORY_CONTACT_DETAIL}
                component={ContactDetails}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}
