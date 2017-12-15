import React, { Component } from 'react'
import { Route, Switch /* Redirect */ } from 'react-router'

import * as ROUTES from '../../utils/routes'

import { PageContent } from '../../components'
import { PageContainer, LinkedSideNavigation } from '../../smart-components'

import NetworkDiscovery from './network-discovery'
import ActiveSite from './active-site'

const menus = [{
  key: ROUTES.SITE_MANAGER,
  label: 'Active site',
},
{
  key: ROUTES.SITE_MANAGER_NETOWRK,
  label: 'Network',
}]

export default class ClientRegistryContainer extends Component {
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
                path={ROUTES.SITE_MANAGER}
                component={ActiveSite}
              />
              <Route
                exact
                path={ROUTES.SITE_MANAGER_NETOWRK}
                component={NetworkDiscovery}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}
