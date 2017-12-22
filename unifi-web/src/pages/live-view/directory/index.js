import React, { Component } from 'react'

import { compose, bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { withRouter } from 'react-router-dom'

import { actions as zonesActions } from '../../../reducers/zones'

import * as ROUTES from '../../../utils/routes'

import { PageContentTitle, PageContentUnderTitle } from '../../../components'
import { Collapse } from '../../../elements'
import { getQueryParams } from '../../../utils/helpers'
import { groupItems } from './utils/helpers'

import FiltersHeader from './components/filters-header'
import GroupingHeader from './components/grouping-header'
import ViewModeHeader from './components/view-mode-header'
import AssetList from './components/asset-list'
import AssetGrid from './components/asset-grid'

import './index.scss'

const dataSource = [{
  key: '1',
  name: 'Jhon Doe',
  id: '56895',
  type: 'contractor',
  last_seen: new Date().getTime(),
  last_seen_location: 'S1B',
}, {
  key: '2',
  name: 'Jhon Doe',
  id: 'ap12854',
  type: 'visitor',
  last_seen: new Date().getTime(),
  last_seen_location: 'S1B',
}, {
  key: '3',
  name: 'Jhon Doe',
  id: '10231',
  type: 'asset',
  last_seen: new Date().getTime(),
  last_seen_location: 'S1A',
}, {
  key: '4',
  name: 'Jhon Doe',
  id: '2341151',
  type: 'asset',
  last_seen: new Date().getTime(),
  last_seen_location: 'S1A',
}, {
  key: '5',
  name: 'Jhon Doe',
  type: 'contractor',
  last_seen: new Date().getTime(),
  last_seen_location: 'S1A',
}]

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
    listZones()
    listHolder()
    listenToSubscriptions()
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

  renderContent() {
    const { grouping } = this.state.queryParams
    const itemGroups = groupItems(dataSource, grouping)
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

    return (
      <div className="directory-view-container">
        <PageContentTitle>Directory View</PageContentTitle>
        <PageContentUnderTitle>Last update 15 mins ago</PageContentUnderTitle>
        <FiltersHeader
          onSearch={this.handleSearch}
          onChange={this.handleFilterChange}
          searchValue={search ? decodeURIComponent(search) : ''}
          filterValues={filters}
        />
        <GroupingHeader onChange={this.handleGroupingChange} groupValue={grouping} />
        <ViewModeHeader onChange={this.handleViewModeChange} viewValue={view} />
        {this.renderContent()}
      </div>
    )
  }
}

export const mapStateToProps = (state) => {
  console.log(state)
  return state
}

export const mapDispatch = dispatch => (bindActionCreators(zonesActions, dispatch))

export default compose(
  withRouter,
  connect(mapStateToProps, mapDispatch),
)(DirectoryView)
