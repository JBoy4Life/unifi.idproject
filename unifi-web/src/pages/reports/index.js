import React, {Component} from 'react'
import { compose } from 'redux'
import { Route, Switch, Redirect } from 'react-router'

import * as ROUTES from 'config/routes'
import { PageContent, SideNavigation } from 'components'
import { PageContainer, LinkedSideNavigation } from 'smart-components'

import { userIsAuthenticatedRedir } from 'hocs/auth'

import './index.scss'

const dashboards = [
  {
    key: '/reports/july-central-working',
    label: 'July Central Working',
    published_link: '987cc85b7388c0961b3d4074388f7985/july-central-working-'
  },
  {
    key: '/reports/unifi-dashboard',
    label: 'unifi.id Dashboard',
    published_link: '80f3fec2ea433ae9aff3b48513527e93/unifiid-dashboard'
  }
]

const ReportsDashboards = ({ publishedLink }) => (
  <iframe src={`https://app.klipfolio.com/published/${publishedLink}`} />
)

const Reports = () => (
  <PageContainer className="reports__page">
    <PageContent>
      <PageContent.Sidebar>
        <LinkedSideNavigation
          menus={dashboards} />
      </PageContent.Sidebar>
      <PageContent.Main>
        <Switch>
          <Redirect exact from={ROUTES.REPORTS} to={dashboards[0].key} />
          {
            dashboards.map( (dashboard, index) =>
              <Route
                key={index}
                path={dashboard.key}
                render={() => <ReportsDashboards publishedLink={dashboard.published_link} />}
              />
            )
          }
        </Switch>
      </PageContent.Main>
    </PageContent>
  </PageContainer>
);

export default compose(
  userIsAuthenticatedRedir
)(Reports)
