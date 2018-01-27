import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import ResultsList from './components/results-list'
import withClientId from 'hocs/with-client-id'
import { listProgrammes } from 'reducers/settings/actions'
import { listScheduleStats } from 'reducers/attendance/actions'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'
import { programmesSelector } from 'reducers/settings/selectors'
import { schedulesSelector } from 'reducers/attendance/selectors'

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
    const { clientId, listProgrammes, listScheduleStats } = this.props
    listProgrammes(clientId)
    listScheduleStats()
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
    const { clientId, schedules, programmesList } = this.props
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
        {programme && (
          <ResultsList
            clientId={clientId}
            programme={programme}
            schedules={schedules}
          />
        )}
      </div>
    )
  }
}

const selector = createStructuredSelector({
  schedules: schedulesSelector,
  programmesList: programmesSelector
})

const actions = {
  listProgrammes,
  listScheduleStats
}

export default compose(
  withClientId,
  connect(selector, actions)
)(AttendanceReports)
