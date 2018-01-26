import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import withClientId from 'hocs/with-client-id'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'

import {
  listProgrammes
} from 'reducers/settings/actions'

import {
  programmesSelector
} from 'reducers/settings/selectors'

import { Col, Row, Select } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'attendance-reports'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`
const Option = Select.Option;

export class AttendanceReports extends Component {
  static propTypes = {
    clientId: PropTypes.string,
    listProgrammes: PropTypes.func,
    location: PropTypes.object
  }

  componentWillMount() {
    const { clientId, listProgrammes } = this.props
    listProgrammes(clientId)
  }

  handleProgrammeChange = (programme) => {
    const { history, location } = this.props
    history.push({
      pathname: location.pathname,
      search: jsonToQueryString({ programme })
    })
  }

  handleFilterOption = (input, option) => {
    return option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
  }

  render() {
    const { programmesList, location } = this.props
    const { programme } = parseQueryString(location.search)

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <Row type="flex" justify="center" align="middle">
          <Col sm={18}>
            <h1>Attendance Alert Report</h1>
          </Col>
          <Col sm={6}>
            <Select
              showSearch
              value={programme}
              style={{ width: '100%' }}
              placeholder="Select a programme"
              optionFilterProp="children"
              onChange={this.handleProgrammeChange}
              filterOption={this.handleFilterOption}
            >
              {programmesList.map((programme, index) => (
                <Option value={programme} key={index}>{programme}</Option>
              ))}
            </Select>
          </Col>
        </Row>
      </div>
    )
  }
}

const selector = createStructuredSelector({
  programmesList: programmesSelector
})

const actions = {
  listProgrammes
}

export default compose(
  withClientId,
  connect(selector, actions)
)(AttendanceReports)
