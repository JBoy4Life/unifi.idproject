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
    published_link_desktop: '498e4fc986e09a430e88054a82735e4f/unificentralworkingproddesktop2',
    published_link_mobile: '7c49c3af642df0cc3ee1880a84243b36/unificentralworkingprodmobile1'
  },
  {
    name: 'unifi.test-club.dashboard',
    key: '/reports/test-club',
    label: 'Test Club Dashboard',
    published_link_desktop: '498e4fc986e09a430e88054a82735e4f/unificentralworkingproddesktop2',
    published_link_mobile: '7c49c3af642df0cc3ee1880a84243b36/unificentralworkingprodmobile1'
  }
]

const ReportsDashboards = ({ publishedLink }) => (
  <div className="iframe__container">
    <iframe src={`https://app.klipfolio.com/published/${publishedLink}`} />
  </div>
)

class Reports extends Component {
  constructor(props) {
    super(props)
    this.mobileWidth = 600
    this.state = {
      reportsList: [],
      width: window.innerWidth
    }
  }

  componentWillMount() {
    this.setState({ reportsList: this.filterReportsByClientId(this.props.clientId) })
    window.addEventListener('resize', this.handleWindowSizeChange)
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleWindowSizeChange);
  }

  handleWindowSizeChange = () => {
    this.setState({ width: window.innerWidth });
  }

  filterReportsByClientId = (clientId) => reports.filter( report => report.name.split('.')[1] === clientId )

  render() {
    const { reportsList, width } = this.state
    const publishedLink = width <= this.mobileWidth ? 'published_link_mobile' : 'published_link_desktop'

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
                        render={() => <ReportsDashboards publishedLink={dashboard[publishedLink]} />}
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
