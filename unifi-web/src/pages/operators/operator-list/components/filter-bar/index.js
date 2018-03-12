import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import { Checkbox, Col, Icon, Row, TextInput } from 'elements'
import { jsonToQueryString, parseQueryString } from 'utils/helpers'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-filter-bar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class FilterBar extends Component {
  static propTypes = {
    history: PropTypes.object.isRequired,
    location: PropTypes.object.isRequired,
    onSearchChange: PropTypes.func,
    setURLHref: PropTypes.func.isRequired
  };

  handleViewModeChange = (view) => {
    const { location, setURLHref } = this.props
    const params = {
      ...parseQueryString(location.search),
      view
    }
    setURLHref(params)
  };

  handleSearchEnter = (search) => {
    const { location, setURLHref } = this.props
    const params = {
      ...parseQueryString(location.search),
      search
    }
    setURLHref(params)
  };

  handleActiveChange = (e) => {
    const { location, setURLHref } = this.props
    const params = {
      ...parseQueryString(location.search),
      showAll: e.target.checked
    }
    setURLHref(params)
  }

  render() {
    const { location, onSearchChange } = this.props
    const params = parseQueryString(location.search)

    return (
      <Row gutter={40} align="middle" justify="center" type="flex" className={COMPONENT_CSS_CLASSNAME}>
        <Col xs={24} md={10} className={bemE('responsive-mb')}>
          <TextInput.Search
            defaultValue={params.search}
            placeholder="Search"
            onChange={onSearchChange}
            onSearch={this.handleSearchEnter}
            enterButton
          />
        </Col>
        <Col xs={12} md={7}>
          <Link to={ROUTES.OPERATOR_INVITE}>
            <Icon type="plus-circle-o" /> Invite an operator
          </Link>
        </Col>
        <Col xs={12} md={7} className="text-right">
          <Checkbox
            onChange={this.handleActiveChange}
            checked={Boolean(params.showAll)}
            className={bemE('ticker')}
          >
            Show deactivated
          </Checkbox>
        </Col>
      </Row>
    )
  }
}

export default withRouter(FilterBar)
