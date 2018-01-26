import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import { parseQueryString, jsonToQueryString } from 'utils/helpers'

import {
  listScheduleStats,
  reportContactScheduleAttendance
} from 'reducers/attendance/actions'

import {
  contactScheduleReportSelector,
  schedulesSelector
} from 'reducers/attendance/selectors'

import { Col, Row, Select } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'ar-results-list'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

export class ResultsList extends Component {
  static propTypes = {
    clientId: PropTypes.string,
  }

  componentWillMount() {
    const { clientId } = this.props
  }

  render() {
    const { scheduleStats, location } = this.props

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
  scheduleStats: schedulesSelector
})

const actions = {
  listScheduleStats,
  reportContactScheduleAttendance
}

export default compose(
  withClientId,
  connect(selector, actions)
)(ResultsList)
