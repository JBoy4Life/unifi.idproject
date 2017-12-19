import React, { Component } from 'react'
import { Route, Switch /* Redirect */ } from 'react-router'

import * as ROUTES from '../../utils/routes'

import { PageContent } from '../../components'
import { PageContainer, LinkedSideNavigation } from '../../smart-components'
import { Button, Modal } from '../../elements/index'


import NetworkDiscovery from './network-discovery'
import ActiveSite from './active-site'
import SitePlan from './site-plan'
import SiteCreation from './site-creation'

import './index.scss'

const menus = [{
  key: ROUTES.SITE_MANAGER,
  label: 'Active site',
},
{
  key: ROUTES.SITE_MANAGER_NETOWRK,
  label: 'Network',
},
{
  key: ROUTES.SITE_PLAN,
  label: 'Site plan',
}]

export default class ClientRegistryContainer extends Component {
  handleCreateNewSiteClick = () => {
    Modal.confirm({
      title: 'Are you sure?',
      content: 'This will replace your current site',
      onOk: () => {
        this.handleCreateNewSiteConfirm()
        return Promise.resolve()
      },
      onCancel() {},
    })
  }

  handleCreateNewSiteConfirm = () => {
    const { history } = this.props
    history.push(ROUTES.SITE_CREATE)
  }


  render() {
    const { location } = this.props
    return (
      <PageContainer className="site-manager-container">
        <PageContent>
          { location.pathname !== ROUTES.SITE_CREATE &&
            <PageContent.Sidebar>
              <LinkedSideNavigation menus={menus} />
              <Button
                onClick={this.handleCreateNewSiteClick}
                className="new-site-button"
                size="small"
              >
                Create new site
              </Button>
            </PageContent.Sidebar>
          }
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
              <Route
                exact
                path={ROUTES.SITE_PLAN}
                component={SitePlan}
              />
              <Route
                exact
                path={ROUTES.SITE_CREATE}
                component={SiteCreation}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}
