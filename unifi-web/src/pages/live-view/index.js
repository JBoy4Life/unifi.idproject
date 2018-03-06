import React, { PureComponent } from 'react'
import fp from 'lodash/fp'
import moment from 'moment'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import TileView from './components/tile-view'
import ViewModeHeader from './components/view-mode-header'
import ZoneFilter from './components/zone-filter'
import { getDiscoveredList } from './utils/helpers'
import { liveViewEnabledRedir } from 'hocs/auth'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import { PageContent } from 'components'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'
import { withClientId } from 'hocs'
import { ZONE_ENTITIES_VALIDATE_INTERVAL } from 'config/constants'

import {
  actions as zonesActions,
  selectors as zoneSelectors,
} from 'redux/zones'

import {
  actions as settingsActions,
  selectors as settingsSelectors
} from 'redux/settings'

import {
  actions as holdersActions,
  selectors as holdersSelectors
} from 'redux/holders'

const zonesSelector = fp.compose(
  (zonesInfo) => (
    fp.compose(
      fp.sortBy('name'),
      fp.map(key => zonesInfo[key]),
      fp.keys
    )(zonesInfo)
  ),
  zoneSelectors.zonesInfoSelector
)

class LiveView extends PureComponent {
  constructor(props) {
    super(props)

    this.state = {
      queryParams: {
        view: 'large',
        ...parseQueryString(this.props.location.search)
      }
    }
  }

  componentDidMount() {
    const { listSites, listZones, listHolders, listenToSubscriptions, clientId } = this.props
    listSites(clientId)
      .then((result) => {
        const { siteId } = this.props
        listZones(clientId, siteId)
        listHolders(clientId, ['image'])
        listenToSubscriptions(clientId, siteId)
      })
      .catch(err => console.error(err))

    this.timerId = window.setInterval(
      this.props.clearInactiveEntities,
      ZONE_ENTITIES_VALIDATE_INTERVAL
    )
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.location.search !== this.props.location.search) {
      this.setState({
        queryParams: parseQueryString(nextProps.location.search),
      })
    }
  }

  componentWillUnmount() {
    this.timerId && window.clearInterval(this.timerId)
  }

  setURLHref(params) {
    const { history } = this.props
    history.push({
      location: ROUTES.LIVE_VIEW_DIRECTORY,
      search: jsonToQueryString(params)
    })
  }

  handleFilterChange = (value) => {
    const {
      filters, grouping, view, search,
    } = this.state.queryParams
    let newFilters = filters

    const key = Object.keys(value)[0]
    const position = filters.indexOf(key)
    if (value[key] && position === -1) {
      newFilters.push(key)
    }

    if (!value[key] && position !== -1) {
      newFilters = [...filters.slice(0, position), ...filters.slice(position + 1)]
    }

    this.setURLHref({
      filters: newFilters, grouping, view, search,
    })
  }

  handleViewModeChange = (view) => {
    this.setURLHref({
      ...this.state.queryParams,
      view
    })
  }

  handleSearch = (search) => {
    this.setURLHref({
      ...this.state.queryParams, search: encodeURIComponent(search),
    })
  }

  handleZoneChange = (zoneId) => {
    this.setURLHref({
      ...this.state.queryParams, zone: encodeURIComponent(zoneId),
    })
  }

  render() {
    const { itemsPerRow, queryParams: { view, zone: zoneId } } = this.state

    const {
      liveDiscoveryUpdate,
      discoveredList,
      zones
    } = this.props

    const zoneItems = fp.compose(
      fp.reverse,
      fp.sortBy('firstDetectionTime'),
      fp.filter(item => item.zone.zoneId === zoneId)
    )(discoveredList)

    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <div className="live-view">

              <ZoneFilter
                onZoneChange={this.handleZoneChange}
                zoneId={zoneId}
                zones={zones}
              />

              <ViewModeHeader
                onViewModeChange={this.handleViewModeChange}
                viewMode={view}
                zoneId={zoneId}
                resultCount={zoneItems.length}
              />

              <TileView items={zoneItems} viewMode={view || 'large'} />
            </div>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  discoveredList: getDiscoveredList,
  siteId: settingsSelectors.siteIdSelector,
  liveDiscoveryUpdate: compose(
    fp.get('liveDiscoveryUpdate'),
    zoneSelectors.getReducer
  ),
  zones: zonesSelector
})

export const actions = {
  ...zonesActions,
  listHolders: holdersActions.listHolders,
  listSites: settingsActions.listSites
}

export default compose(
  liveViewEnabledRedir,
  withRouter,
  withClientId,
  connect(selector, actions),
)(LiveView)
