import React, { Component } from 'react'
import moment from 'moment'
import fp from 'lodash/fp'

import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router-dom'

import {
  actions as zonesActions,
  selectors as zoneSelectors,
} from 'reducers/zones'

import * as ROUTES from 'utils/routes'

import { PageContentTitle, PageContentUnderTitle } from 'components'
import { Collapse } from 'elements'
import { getQueryParams } from 'utils/helpers'
import { groupItems, filterItems } from './utils/helpers'

import FiltersHeader from './components/filters-header'
import GroupingHeader from './components/grouping-header'
import ViewModeHeader from './components/view-mode-header'
import AssetList from './components/asset-list'
import AssetGrid from './components/asset-grid'

import './index.scss'

const getQueryString = (params) => {
  const {
    search, filters, grouping, view,
  } = params
  if (!filters.length && !grouping && !view) {
    return null
  }

  const result = {}
  if (filters.length) {
    result.filter = filters.join(',')
  }

  if (grouping) {
    result.grouping = grouping
  }

  if (view) {
    result.view = view
  }

  if (search) {
    result.search = search
  }

  const keys = Object.keys(result)
  if (keys.length === 0) {
    return ''
  }

  const resultString = `?${keys[0]}=${result[keys[0]]}`

  return keys.slice(1).reduce((acc, key) => {
    acc = `${acc}&${key}=${result[key]}`
    return acc
  }, resultString)
}

class DirectoryView extends Component {
  state = {
    queryParams: getQueryParams(this.props.location.search),
  }

  componentDidMount() {
    const { listZones, listHolder, listenToSubscriptions } = this.props
    Promise.all([
      listZones(),
      listHolder(),
    ])
      .then(() => listenToSubscriptions())
      .catch(err => console.error(err))
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.location.search !== this.props.location.search) {
      this.setState({
        queryParams: getQueryParams(nextProps.location.search),
      })
    }
  }

  setURLHref(params) {
    const { history } = this.props
    history.push(`${ROUTES.LIVE_VIEW_DIRECTORY}${getQueryString(params)}`)
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

  handleGroupingChange = (value) => {
    this.setURLHref({
      ...this.state.queryParams, grouping: value,
    })
  }

  handleViewModeChange = (ev) => {
    this.setURLHref({
      ...this.state.queryParams, view: ev.target.value,
    })
  }

  handleSearch = (search) => {
    this.setURLHref({
      ...this.state.queryParams, search: encodeURIComponent(search),
    })
  }

  renderContentList(items) {
    if (this.state.queryParams.view === 'list') {
      return <AssetList items={items} />
    }

    return <AssetGrid items={items} />
  }

  renderZoneGroupTitle(groupName, items) {
    return (
      <div>{groupName} {items.length} contacts</div>
    )
  }

  renderContent(filteredItems) {
    const { grouping } = this.state.queryParams
    const itemGroups = groupItems(filteredItems, grouping)
    const keys = Object.keys(itemGroups)

    if (grouping === 'zones') {
      return (
        <Collapse defaultActiveKey={keys.map((item, idx) => idx.toString())}>
          {
            keys.map((key, index) => (
              <Collapse.Panel
                key={index.toString()}
                header={this.renderZoneGroupTitle(key, itemGroups[key])}
              >
                {this.renderContentList(itemGroups[key])}
              </Collapse.Panel>
            ))
          }
        </Collapse>
      )
    }
    return this.renderContentList(itemGroups)
  }

  render() {
    const {
      filters, grouping, view, search,
    } = this.state.queryParams

    const {
      liveDiscoveryUpdate,
      discoveredList,
    } = this.props

    const filteredItems = filterItems(discoveredList, filters, search)
    return (
      <div className="directory-view-container">

        <PageContentTitle>Directory View</PageContentTitle>
        <PageContentUnderTitle>
          Last update {moment(liveDiscoveryUpdate).fromNow()}
        </PageContentUnderTitle>

        <FiltersHeader
          onSearch={this.handleSearch}
          onChange={this.handleFilterChange}
          searchValue={search ? decodeURIComponent(search) : ''}
          filterValues={filters}
        />

        <GroupingHeader
          onChange={this.handleGroupingChange}
          groupValue={grouping}
        />

        <ViewModeHeader
          onChange={this.handleViewModeChange}
          viewValue={view}
          resultCount={filteredItems.length}
        />

        {this.renderContent(filteredItems)}
      </div>
    )
  }
}

export const selector = createStructuredSelector({
  discoveredList: zoneSelectors.getDiscoveredList,
  liveDiscoveryUpdate: compose(
    fp.get('liveDiscoveryUpdate'),
    zoneSelectors.getReducer
  )
})

export const actions = {
  ...zonesActions
}

export default compose(
  withRouter,
  connect(selector, actions),
)(DirectoryView)
