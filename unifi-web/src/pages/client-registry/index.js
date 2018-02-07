import React, { Component } from 'react'
import { Route, Switch /* Redirect */ } from 'react-router'

import { compose, bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { withRouter } from 'react-router-dom'

import { actions as clientActions, selectors as clientSelectors } from 'redux/clients'

import * as ROUTES from '../../utils/routes'


import { PageContent } from '../../components'
import { PageContainer, LinkedSideNavigation } from '../../smart-components'

import ClientListing from './client-listing'
import ClientAdd from './client-add'

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

export const mapStateToProps = state => ({
  clientList: clientSelectors.getClients(state),
})
export const mapDispatch = dispatch => bindActionCreators(clientActions, dispatch)

export default compose(
  withRouter,
  connect(mapStateToProps, mapDispatch),
)(ClientRegistryContainer)
