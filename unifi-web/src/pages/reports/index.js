import React, {Component} from 'react'
import { compose } from 'redux'
import { Route, Switch, Redirect } from 'react-router'

import * as ROUTES from 'config/routes'
import { PageContent, SideNavigation } from 'components'
import { PageContainer, LinkedSideNavigation } from 'smart-components'

import { userIsAuthenticatedRedir } from 'hocs/auth'

import ReportsKlipsList from './reports-klips-list'

import './index.css'

const dashboards = [
  {
    key: '/reports/july-central-working',
    label: 'July Central Working',
    klips: [
      {
        id: "50203518da421deb48296ac33760b129",
        title: "Drop-In Member Hours",
      },
      {
        id: "dc3d6490a595733bd47824a843bcb921",
        title: "Drop In Members Visits",
      },
      {
        id: "f422f62777d804503204d95ecfaf2a65",
        title: "Average Hours Per Site, Per Day, for Drop In Members",
      },
      {
        id: "ac78524539a379e221c34d2183491ed0",
        title: "July Drop In Visits By Site",
      },
    ]
  },
  {
    key: '/reports/unifi-dashboard',
    label: 'unifi.id Dashboard',
    klips: [
      {
        id: "1cf81a0a0f706e3b998ea766ca51cab3",
        title: "Organisation Period Usage Summary",
      },
      {
        id: "0d11786514cd1b823a288e2abf9c635d",
        title: "Organisation Visits in period",
      },
      {
        id: "995665b6d43eb1503ac8c26b84d41868",
        title: "Average Hours Per Site, Per Day, for Drop In Members",
      },
      {
        id: "b0d8d4750b8114f4fe4b7f13f054554f",
        title: "Drop In Members Visits",
      },
      {
        id: "308ab35add8d1d8841a064115c8b0559",
        title: "July Drop In Visits By Site",
      },
    ]
  }
]

class Reports extends Component {
  render() {
    console.log(this.props);
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Sidebar>
            <LinkedSideNavigation
              menus={dashboards} />
          </PageContent.Sidebar>
          <PageContent.Main>
            {/*<Redirect exact from={ROUTES.REPORTS} to={dashboards[0].key} />*/}
            {
              dashboards.map( (dashboard, index) =>
                <Route
                  key={index}
                  path={dashboard.key}
                  render={() => <ReportsKlipsList klips={dashboard.klips} />}
                />
              )
            }
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    );
  }
}

export default compose(
  userIsAuthenticatedRedir
)(Reports)
