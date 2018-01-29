import React, { Component } from 'react'
import PropTypes from 'prop-types'
import groupBy from 'lodash/groupBy'
import find from 'lodash/find'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import ReportFilterForm from '../report-filter-form'
import ScheduleTable from '../schedule-table'
import { Collapse } from 'elements'
import { lowAttendanceReportSelector } from 'reducers/attendance/selectors'
import { reportLowAttendanceByMetadata } from 'reducers/attendance/actions'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'ar-results-list'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

export class ResultsList extends Component {
  static propTypes = {
    clientId: PropTypes.string,
    holdersList: PropTypes.array,
    programme: PropTypes.string,
    schedules: PropTypes.array
  }

  componentWillMount() {
    const { clientId, programme, reportLowAttendanceByMetadata } = this.props
    reportLowAttendanceByMetadata(clientId, programme)
  }

  render() {
    const { holdersList, lowAttendanceReport, programme, schedules } = this.props
    const groupedAttendance = groupBy(lowAttendanceReport.attendance, (item) => item.scheduleId)

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        {programme && <h2 className={bemE('title')}>{programme}</h2>}
        <ReportFilterForm programme={programme} />
        <Collapse bordered={false} prefixCls="ant-collapse">
          {Object.keys(groupedAttendance).map((scheduleId) => {
            const schedule = find(schedules, { scheduleId })
            return (
              <Collapse.Panel header={schedule.name} key={scheduleId}>
                <ScheduleTable
                  schedule={schedule}
                  report={groupedAttendance[scheduleId]}
                  holdersList={holdersList}
                />
              </Collapse.Panel>
            )
          })}
        </Collapse>
      </div>
    )
  }
}

const selector = createStructuredSelector({
  lowAttendanceReport: lowAttendanceReportSelector
})

const actions = {
  reportLowAttendanceByMetadata
}

export default compose(
  connect(selector, actions)
)(ResultsList)
