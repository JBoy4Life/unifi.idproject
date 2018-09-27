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
import { protocol, socketUri } from '../../../../index'

const GridItem = HolderGrid.Item

const formatTime = (value, timeZone) => moment(value).tz(timeZone).format('LTS')

class TileView extends PureComponent {
  constructor(props) {
    super(props)
    const {holdersCache} = this.props
    this.cachedHolders = new Set(Object.keys(holdersCache))
  }

  getHolderImageEndpoint = (holder) => {
    const {clientId, currentUser} = this.props
    return `${protocol}://${socketUri}/api/v1/clients/${clientId}/holders/${holder}/image?_sessionToken=${encodeURIComponent(currentUser.token)}`
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
  }

  componentDidUpdate() {
    const {items} = this.props
    this.cacheHolders(items.map((item) => item.clientReference))
  }

  render() {
    const {items, viewMode, timeZone, holdersCache} = this.props
    return (
      <HolderGrid viewMode={viewMode}>
        {items.map(item => (
          <GridItem
              image={this.getHolderImageEndpoint(item.clientReference)}
              key={item.clientReference}>
            <GridItem.Field>{item.client.name}</GridItem.Field>
            <GridItem.Field>ID: {item.clientReference}</GridItem.Field>
            <GridItem.Field>{formatTime(item.detectionTime, timeZone)}</GridItem.Field>
          </GridItem>))}
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
