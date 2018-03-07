import React, { Component } from 'react'
import { compose, bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Route, Switch /* Redirect */ } from 'react-router'
import { withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import ClientAdd from './client-add'
import ClientListing from './client-listing'
import { actions as clientActions, selectors as clientSelectors } from 'redux/clients'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import { PageContent } from 'components'

const menus = [{
  key: ROUTES.CLIENT_REGISTRY,
  label: 'list',
},
{
  key: ROUTES.CLIENT_REGISTRY_ADD,
  label: 'add client',
}]

class ClientRegistryContainer extends Component {
  state = {
    loading: true,
  }

  componentDidMount() {
    const { listClients } = this.props
    listClients().then(() => {
      this.setState({
        loading: false,
      })
    })
  }

  handleClientCreated = () => this.props.history.push(ROUTES.CLIENT_REGISTRY)

  preparedClientListing = routeProps => (
    <ClientListing clientList={this.props.clientList} {...routeProps} />
  )

  preparedClientAdd = routeProps => (
    <ClientAdd
      createClient={this.props.createClient}
      onCreatedClient={this.handleClientCreated}
      {...routeProps}
    />
  )

  render() {
    const { loading } = this.state

    return (
      <PageContainer>
        <PageContent>
          {
           loading ? '' : ([
             <PageContent.Sidebar>
               <LinkedSideNavigation menus={menus} />
             </PageContent.Sidebar>,
             <PageContent.Main>
               <Switch>
                 <Route
                   exact
                   path={ROUTES.CLIENT_REGISTRY}
                   render={this.preparedClientListing}
                 />
                 <Route
                   exact
                   path={ROUTES.CLIENT_REGISTRY_ADD}
                   render={this.preparedClientAdd}
                 />
               </Switch>
             </PageContent.Main>,
           ]
           )
         }
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  clientList: clientSelectors.clientsSelector,
});

export const actions = {
  ...clientActions
}

export default compose(
  withRouter,
  connect(selector, actions),
)(ClientRegistryContainer)
