import React, { Component } from 'react'
import { Route, Switch /* Redirect */ } from 'react-router'

import * as ROUTES from '../../utils/routes'

import { PageContent } from '../../components'
import { PageContainer, LinkedSideNavigation } from '../../smart-components'

import Dashboard from './dashboard'
import FloorView from './floor-view'
import DirectoryView from './directory'

const menus = [
  {
    key: ROUTES.LIVE_VIEW,
    label: 'Dashboard',
  },
  {
    key: ROUTES.LIVE_VIEW_DIRECTORY,
    label: 'Directory',
  },
  {
    key: ROUTES.LIVE_VIEW_FLOOR_VIEW,
    label: 'Floor view',
  },
]

export default class LiveViewContainer extends Component {
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
                path={ROUTES.LIVE_VIEW}
                component={Dashboard}
              />
              <Route
                exact
                path={ROUTES.LIVE_VIEW_FLOOR_VIEW}
                component={FloorView}
              />
              <Route
                exact
                path={ROUTES.LIVE_VIEW_DIRECTORY}
                component={DirectoryView}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}
