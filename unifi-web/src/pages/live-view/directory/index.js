import React, { Component } from 'react'

import { compose } from 'redux'
import { connect } from 'react-redux'
import { withRouter } from 'react-router-dom'


import { PageContentTitle, PageContentUnderTitle } from '../../../components'
import { parseQueryString } from '../../../utils/helpers'

import FiltersHeader from './components/filters-header'
import GroupingHeader from './components/grouping-header'
import ViewModeHeader from './components/view-mode-header'
import AssetList from './components/asset-list'
import AssetGrid from './components/asset-grid'


const dataSource = [{
  key: '1',
  name: 'Jhon Doe',
  id: '56895',
  type: 'contractor',
  last_seen: new Date().getTime(),
  last_seen_location: 'ZONE S1B',
}, {
  key: '2',
  name: 'Jhon Doe',
  id: 'ap12854',
  type: 'visitor',
  last_seen: new Date().getTime(),
  last_seen_location: 'ZONE S1B',
}, {
  key: '3',
  name: 'Jhon Doe',
  id: '10231',
  type: 'asset',
  last_seen: new Date().getTime(),
  last_seen_location: 'ZONE S1B',
}, {
  key: '4',
  name: 'Jhon Doe',
  id: '2341151',
  type: 'asset',
  last_seen: new Date().getTime(),
  last_seen_location: 'ZONE S1B',
}, {
  key: '5',
  name: 'Jhon Doe',
  type: 'contractor',
  last_seen: new Date().getTime(),
  last_seen_location: 'ZONE S1B',
}]


class DirectoryView extends Component {
  render() {
    const { filter = '', grouping, view } = parseQueryString(this.props.location.search)

    return (
      <div className="site-plan-container">
        <PageContentTitle>Directory View</PageContentTitle>
        <PageContentUnderTitle>Last update 15 mins ago</PageContentUnderTitle>
        <FiltersHeader filterValues={filter.split(',')} />
        <GroupingHeader groupValue={grouping} />
        <ViewModeHeader viewValue={view} />
        {
          view === 'list'
          ? <AssetList items={dataSource} />
          : <AssetGrid items={dataSource} />
        }
      </div>
    )
  }
}

export default compose(
  withRouter,
  connect(),
)(DirectoryView)
