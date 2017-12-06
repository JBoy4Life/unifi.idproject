import React, { Component } from 'react'
import { Route, Switch /* Redirect */ } from 'react-router'

import * as ROUTES from '../../utils/routes'

import { PageContent } from '../../components'
import { PageContainer, LinkedSideNavigation } from '../../smart-components'

import EvacuationDashboard from './evacuation-dashboard'
import EvacuationDirectory from './evacuation-directory'
import EvacuationFloor from './evacuation-floor'

const menus = [{
  key: '/evacuation',
  label: 'Dashboard',
},
{
  key: '/evacuation/directory',
  label: 'Directory View',
},
{
  key: '/evacuation/floor',
  label: 'Floor View',
}]

export default class SitemapContainer extends Component {
  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Sidebar>
            <LinkedSideNavigation menus={menus} />
          </PageContent.Sidebar>
          <PageContent.Main>
            <Switch>
              <Route
                exact
                path={ROUTES.EVACUATION}
                component={EvacuationDashboard}
              />
              <Route
                exact
                path={ROUTES.EVACUATION_DIRECTORY_VIEW}
                component={EvacuationDirectory}
              />
              <Route
                exact
                path={ROUTES.EVACUATION_FLOOR_VIEW}
                component={EvacuationFloor}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

