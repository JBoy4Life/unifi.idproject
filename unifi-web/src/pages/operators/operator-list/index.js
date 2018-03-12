import React, { Component } from 'react'
import fp from 'lodash/fp'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import ListView from './components/list-view'
import FilterBar from './components/filter-bar'
import { API_PENDING, API_SUCCESS, API_FAIL } from 'redux/api/request'
import { Col, Row, Spinner, TextInput } from 'elements'
import { operatorListSelector, operatorListStatusSelector } from 'redux/operator/selectors'
import { jsonToQueryString, parseQueryString } from 'utils/helpers'
import { listOperators } from 'redux/operator/actions'
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

class OperatorList extends Component {
  static propTypes = {
    history: PropTypes.object.isRequired,
    listOperators: PropTypes.func.isRequired,
    location: PropTypes.object.isRequired,
    operatorList: PropTypes.array.isRequired,
    operatorListStatus: PropTypes.string.isRequired
  };

  constructor(props) {
    super(props)
    this.state = {
      search: null
    }
  }

  componentDidMount() {
    const { clientId, listOperators } = this.props
    listOperators({ clientId })
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
    const { operatorList, operatorListStatus, location } = this.props
    const params = parseQueryString(location.search)
    const { view = 'list' } = params
    const criteria = {
      ...params,
      search: this.state.search || params.search
    }
    const filteredOperators = fp.filter(predicate(criteria))(operatorList)
    return (
      <div>
        <FilterBar setURLHref={this.setURLHref} onSearchChange={this.handleSearchChange} />
        {API_PENDING === operatorListStatus && <Spinner />}
        {API_SUCCESS === operatorListStatus && <ListView operators={filteredOperators} />}
        {API_FAIL === operatorListStatus && <h2>Failed to load operators</h2>}
      </div>
    )
  }
}

export const selector = createStructuredSelector({
  operatorList: fp.compose(
    fp.sortBy('name'),
    operatorListSelector
  ),
  operatorListStatus: operatorListStatusSelector
})

export const actions = {
  listOperators
}

export default compose(
  withRouter,
  withClientId,
  connect(selector, actions),
)(OperatorList)
