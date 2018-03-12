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
import { Col, Row, TextInput } from 'elements'
import { operatorListSelector } from 'redux/operator/selectors'
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
    operators: PropTypes.array.isRequired
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
    const { operators, location } = this.props
    const params = parseQueryString(location.search)
    const { view = 'list' } = params
    const criteria = {
      ...params,
      search: this.state.search || params.search
    }
    const filteredOperators = fp.filter(predicate(criteria))(operators)
    return (
      <div>
        <FilterBar setURLHref={this.setURLHref} onSearchChange={this.handleSearchChange} />
        <ListView operators={filteredOperators} />
      </div>
    )
  }
}

export const selector = createStructuredSelector({
  operators: fp.compose(
    fp.sortBy('name'),
    operatorListSelector
  )
})

export const actions = {
  listOperators
}

export default compose(
  withRouter,
  withClientId,
  connect(selector, actions),
)(OperatorList)
