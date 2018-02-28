import React, { Component } from 'react'
import filter from 'lodash/filter'
import pick from 'lodash/pick'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import ListView from './components/list-view'
import FilterBar from './components/filter-bar'
import TileView from './components/tile-view'
import ViewModeHeader from './components/view-mode-header'
import { Col, Row, TextInput } from 'elements'
import { holdersSelector } from 'redux/holders/selectors'
import { jsonToQueryString, parseQueryString } from 'utils/helpers'

const predicate = (criteria) => (item) => {
  if (criteria.search) {
    return item.name.includes(criteria.search)
  }
  if (!criteria.showAll) {
    return item.active
  }
  return true
}

class ContactList extends Component {
  static propTypes = {
    history: PropTypes.object.isRequired,
    holders: PropTypes.array.isRequired,
    location: PropTypes.object.isRequired
  };

  setURLHref = (params) => {
    const { history } = this.props
    history.push({
      location: ROUTES.DIRECTORY,
      search: jsonToQueryString(params)
    })
  };

  handleViewModeChange = (view) => {
    const { location } = this.props
    const params = {
      ...parseQueryString(location.search),
      view
    }
    this.setURLHref(params)
  };

  render() {
    const { holders, location } = this.props
    const params = parseQueryString(location.search)
    const { view = 'list' } = params
    const filteredHolders = filter(holders, predicate(params))

    return (
      <div>
        <FilterBar setURLHref={this.setURLHref} />
        <ViewModeHeader
          onViewModeChange={this.handleViewModeChange}
          viewMode={view}
          resultCount={filteredHolders.length}
        />
        {view === 'list' && <ListView holders={filteredHolders} />}
        {view === 'large-tile' && <TileView holders={filteredHolders} key='tile-view' viewMode="large" />}
        {view === 'small-tile' && <TileView holders={filteredHolders} key='tile-view' viewMode="small" />}
      </div>
    )
  }
}

export const selector = createStructuredSelector({
  holders: holdersSelector
})

export default compose(
  withRouter,
  connect(selector),
)(ContactList)
