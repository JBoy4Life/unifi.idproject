import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import ReportFilterForm from '../report-filter-form'
import { lowAttendanceReportSelector } from 'reducers/attendance/selectors'
import { reportLowAttendanceByMetadata } from 'reducers/attendance/actions'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'ar-results-list'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

export class ResultsList extends Component {
  static propTypes = {
    clientId: PropTypes.string,
    programme: PropTypes.string,
    schedules: PropTypes.array
  }

  componentWillMount() {
    const { clientId, programme, reportLowAttendanceByMetadata } = this.props
    reportLowAttendanceByMetadata(clientId, programme)
  }

  render() {
    const { lowAttendanceReport, programme } = this.props

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        {programme && <h2 className={bemE('title')}>{programme}</h2>}
        <ReportFilterForm programme={programme} />
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
