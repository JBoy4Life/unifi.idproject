import React, { Component } from 'react'
import fp from 'lodash/fp'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import FilterBar from './components/filter-bar'
import ListView from './components/list-view'
import TileView from './components/tile-view'
import ViewModeHeader from './components/view-mode-header'
import { Col, Row, TextInput } from 'elements'
import { holdersSelector } from 'redux/modules/holder/selectors'
import { jsonToQueryString, parseQueryString } from 'utils/helpers'
import { listHolders } from 'redux/modules/holder/actions'
import { withClientId } from 'hocs'

const predicate = (criteria) => (item) => {
  if (criteria.search) {
    return item.name.toUpperCase().includes(criteria.search.toUpperCase())
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

  constructor(props) {
    super(props)
    this.state = {
      search: null
    }
  }

  componentDidMount() {
    const { clientId, listHolders } = this.props
    listHolders({
      clientId,
      with: ['image', 'detectable-type']
    })
  }

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

  handleSearchChange = (e) => {
    this.setState({
      search: e.target.value
    })
  }

  render() {
    const { holders, location } = this.props
    const params = parseQueryString(location.search)
    const { view = 'list' } = params
    const criteria = {
      ...params,
      search: this.state.search || params.search
    }
    const filteredHolders = fp.filter(predicate(criteria))(holders)

    return (
      <div>
        <FilterBar setURLHref={this.setURLHref} onSearchChange={this.handleSearchChange} />
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
  holders: fp.compose(
    fp.sortBy('name'),
    holdersSelector
  )
})

export const actions = {
  listHolders
}

export default compose(
  withRouter,
  withClientId,
  connect(selector, actions),
)(ContactList)
