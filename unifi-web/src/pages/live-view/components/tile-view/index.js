import React, { PureComponent } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import moment from 'moment-timezone'
import { createStructuredSelector } from 'reselect'

import * as ROUTES from 'config/routes'
import { HolderGrid } from 'components'
import { cacheHolder } from 'redux/modules/model/holder'
import { withClientId } from 'hocs'
import { holdersCacheSelector, currentUserSelector } from 'redux/selectors'
import { socketUri } from '../../../../index'

const GridItem = HolderGrid.Item

const formatTime = (value, timeZone) => moment(value).tz(timeZone).format('LTS')

class TileView extends PureComponent {
  constructor(props) {
    super(props)
    const {holdersCache} = this.props
    this.cachedHolders = new Set(Object.keys(holdersCache))
    this.assertedEndpoints = new Set()
    this.state = {
      endpointsAssertions: new Map()
    }
  }

  getHolderImageEndpoint = (holder) => {
    const {clientId, currentUser} = this.props
    return `//${socketUri}/api/v1/clients/${clientId}/holders/${holder}/image?_sessionToken=${encodeURIComponent(currentUser.token)}`
  }

  assertEndpoints = (endpoints) => {
    const unassertedEndpoints = endpoints.filter((endpoint) => (! this.assertedEndpoints.has(endpoint)))
    unassertedEndpoints.forEach((endpoint) => {
      const request = new XMLHttpRequest()
      request.onreadystatechange = () => {
        request.readyState >= 2
          && this.setState((state) => ({
            endpointsAssertions: new Map(state.endpointsAssertions)
              .set(endpoint, (request.status === 200)) })) }
      request.open('HEAD', endpoint)
      request.send()
      this.assertedEndpoints.add(endpoint)
    })
  }

  cacheHolders = (holders) => {
    const {clientId, cacheHolder} = this.props
    const uncachedHolders = holders.filter((holder) => (! this.cachedHolders.has(holder)))
    uncachedHolders.forEach((holder) => {
      cacheHolder({clientId, clientReference: holder})
      this.cachedHolders.add(holder)
    })
  }

  componentDidMount() {
    const {items} = this.props
    this.cacheHolders(items.map((item) => item.clientReference))
    this.assertEndpoints(items.map((item) => this.getHolderImageEndpoint(item.clientReference)))
  }

  componentDidUpdate() {
    const {items} = this.props
    this.cacheHolders(items.map((item) => item.clientReference))
    this.assertEndpoints(items.map((item) => this.getHolderImageEndpoint(item.clientReference)))
  }

  render() {
    const {items, viewMode, timeZone, holdersCache} = this.props
    return (
      <HolderGrid viewMode={viewMode}>
        {items.map(item => {
          const holderImageEndpoint = this.getHolderImageEndpoint(item.clientReference)
          return (
            <GridItem
                image={this.state.endpointsAssertions.get(holderImageEndpoint)
                    ? holderImageEndpoint
                    : null }
                key={item.clientReference}>
              <GridItem.Field>{(holdersCache[item.clientReference] && holdersCache[item.clientReference].name) || '...'}</GridItem.Field>
              <GridItem.Field>ID: {item.clientReference}</GridItem.Field>
              <GridItem.Field>{formatTime(item.detectionTime, timeZone)}</GridItem.Field>
            </GridItem> ) })}
      </HolderGrid>
    )
  }

}

export const selector = createStructuredSelector({
  currentUser: currentUserSelector,
  holdersCache: holdersCacheSelector
})

export const actions = {
  cacheHolder
}

export default compose(
  withClientId,
  connect(selector, actions)
)(TileView)
