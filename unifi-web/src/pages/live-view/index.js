import React, { PureComponent } from 'react'
import fp from 'lodash/fp'
import moment from 'moment'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router-dom'
import _ from 'lodash'

import * as ROUTES from 'config/routes'
import TileView from './components/tile-view'
import ViewModeHeader from './components/view-mode-header'
import ZoneFilter from './components/zone-filter'
import { getDiscoveredList } from './utils/helpers'
import { listHolders } from 'redux/modules/model/holder'
import { listSites, listZones, listenToSubscriptions } from 'redux/modules/model/site'
import { liveViewEnabledRedir } from 'hocs/auth'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import { PageContent } from 'components'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'
import { siteIdSelector, sitesInfoSelector, zonesInfoSelector } from 'redux/selectors'
import { userIsAuthenticatedRedir } from 'hocs/auth'
import { withClientId } from 'hocs'
import { ZONE_ENTITIES_VALIDATE_INTERVAL } from 'config/constants'
import Loading from 'components/loading'

const zonesSelector = fp.compose(
  (zonesInfo) => (
    fp.compose(
      fp.sortBy('name'),
      fp.map(key => zonesInfo[key]),
      fp.keys
    )(zonesInfo)
  ),
  zonesInfoSelector
)

const sitesSelector = fp.compose(
  fp.sortBy('description'),
  sitesInfoSelector
)

class LiveView extends PureComponent {
  constructor(props) {
    super(props)

    this.state = {
      queryParams: {
        view: 'large',
        ...parseQueryString(this.props.location.search)
      },
      showZoneItems: false,
    }
  }

  componentDidMount() {
    const { listZones, listHolders, listSites, listenToSubscriptions, clientId } = this.props

    listSites({ clientId })
      .then((result) => {
        const siteId = this.state.queryParams.site
        // TODO: Check if siteId matches an existing site.
        if (siteId == undefined) {
          if (result.payload[0] && result.payload[0].siteId != undefined) {
            this.handleSiteChange(result.payload[0].siteId)
          }
        }
        else {
          listenToSubscriptions({ clientId, siteId })
        }

        // Make sure we show tiles only after metadata has been fetched to
        // avoid reading undefined properties.
        Promise.all([
          listZones({ clientId, siteId }),
          listHolders({ clientId, with: ['image'] }),
        ]).then(() => this.setShowZoneItems())
        .catch(err => console.error(err))
      })
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

  setShowZoneItems = () => this.setState({ showZoneItems: true })

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

  handleSiteChange = (siteId) => {
    const { listZones, clientId, listenToSubscriptions } = this.props
    
    this.handleSiteChangeURL(siteId)
    listZones({ clientId, siteId })
    listenToSubscriptions({ clientId, siteId })
  }

  handleSiteChangeURL = (siteId) => {
    this.setURLHref({
      ...this.state.queryParams, site: encodeURIComponent(siteId), zone: 'all',
    })
  }

  handleZoneChange = (zoneId) => {
    this.setURLHref({
      ...this.state.queryParams, zone: encodeURIComponent(zoneId),
    })
  }

  orderByDescDetectionTime = (list) => (
    _.orderBy(list, ['detectionTime'], ['desc'])
  )

  filterByZone = (list, zoneId) => (
    list.filter(item => item.zone && item.zone.zoneId === zoneId)
  )

  filterByZoneSite = (list, zones) => (
    list.filter(item => ( item.zone && _.find(zones, {zoneId: item.zone.zoneId}) ))
  )

  generateZoneItems = (discoveredList) => {
    const { queryParams: { zone: zoneId, site: siteId } } = this.state
    const { zones } = this.props
    let list = discoveredList

    if (zoneId !== 'all') {
      list = this.filterByZone(list, zoneId)
    } else if (siteId !== 'all') {
      list = this.filterByZoneSite(list, zones)
    }

    return this.orderByDescDetectionTime(list)
  }

  render() {
    const { showZoneItems, itemsPerRow, queryParams: { view, zone: zoneId, site: siteId } } = this.state

    const {
      discoveredList,
      zones,
      sites
    } = this.props

    const zoneItems = this.generateZoneItems(discoveredList)
    const selectedSite = sites.find(site => site.siteId === siteId)

    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <div className="live-view">

              <ZoneFilter
                onZoneChange={this.handleSiteChange}
                zoneId={siteId}
                zones={sites}
                placeholder="Select a site"
                idKey="siteId"
                nameKey="description"
              />

              <ZoneFilter
                onZoneChange={this.handleZoneChange}
                zoneId={zoneId}
                zones={zones}
                placeholder="Select a zone"
                idKey="zoneId"
                nameKey="name"
                disabled={siteId === 'all'}
              />

              <ViewModeHeader
                onViewModeChange={this.handleViewModeChange}
                viewMode={view}
                zoneId={zoneId}
                resultCount={zoneItems.length}
              />

            { showZoneItems ? <TileView items={zoneItems} viewMode={view || 'large'} timeZone={selectedSite.timeZone} /> : <Loading />}
            </div>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  discoveredList: getDiscoveredList,
  siteId: siteIdSelector,
  zones: zonesSelector,
  sites: sitesSelector
})

export const actions = {
  listHolders,
  listSites,
  listZones,
  listenToSubscriptions
}

export default compose(
  userIsAuthenticatedRedir,
  liveViewEnabledRedir,
  withRouter,
  withClientId,
  connect(selector, actions),
)(LiveView)
