import React, {Component} from 'react'
import { compose } from 'redux'
import { Route, Switch, Redirect } from 'react-router'

import * as ROUTES from 'config/routes'
import { PageContent, SideNavigation } from 'components'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import { withClientId } from 'hocs'
import { userIsAuthenticatedRedir } from 'hocs/auth'

import './index.scss'

const reports = [
  {
    name: 'unifi.centralworking.dashboard',
    key: '/reports/unifi-dashboard',
    label: 'unifi.id Dashboard',
    published_link: '498e4fc986e09a430e88054a82735e4f/unificentralworkingproddesktop2'
  },
  {
    name: 'unifi.test-club.dashboard',
    key: '/reports/test-club',
    label: 'Test Club Dashboard',
    published_link: '498e4fc986e09a430e88054a82735e4f/unificentralworkingproddesktop2'
  }
]

const ReportsDashboards = ({ publishedLink }) => (
  <iframe src={`https://app.klipfolio.com/published/${publishedLink}`} />
)

class Reports extends Component {
  constructor(props) {
    super(props)
    this.state = {
      reportsList: []
    }
  }

  componentWillMount() {
    this.setState({ reportsList: this.filterReportsByClientId(this.props.clientId) })
  }

  filterReportsByClientId = (clientId) => reports.filter( report => report.name.split('.')[1] === clientId )

  render() {
    const { reportsList } = this.state
    return (
      <PageContainer className="reports__page">
        <PageContent>
          <PageContent.Sidebar>
            <LinkedSideNavigation
              menus={reportsList} />
          </PageContent.Sidebar>
          <PageContent.Main>
            {
              reportsList.length > 0 && (
                <Switch>
                  <Redirect exact from={ROUTES.REPORTS} to={reportsList[0].key} />
                  {
                    reportsList.map( (dashboard, index) =>
                      <Route
                        key={index}
                        path={dashboard.key}
                        render={() => <ReportsDashboards publishedLink={dashboard.published_link} />}
                      />
                    )
                  }
                </Switch>
              )
            }
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export default compose(
  userIsAuthenticatedRedir,
  withClientId
)(Reports)
