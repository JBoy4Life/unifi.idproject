import React, { Component } from 'react'
import cn from 'classnames'
import PropTypes from 'prop-types'

import { CircularProgressBar } from '../../elements'

import './index.scss'

const getStatusClass = ({ percentage, warningThreshold, criticalThreshold }) => {
  if (percentage < criticalThreshold) {
    return 'status-critical';
  } else if (percentage < warningThreshold) {
    return 'status-warning';
  } else {
    return 'status-good';
  }
}

export default class EvacuationProgressBar extends Component {
  static propTypes = {
    criticalThreshold: PropTypes.number,
    percentage: PropTypes.number,
    tbd: PropTypes.bool,
    warningThreshold: PropTypes.number
  }

  handleTextForPercentage = (pct) => {
    const { tbd } = this.props
    return tbd ? 'TBD' : `${pct}%`
  }

  render() {
    const { percentage } = this.props

    return (
      <div className={cn('evacuation-progress-bar', getStatusClass(this.props))}>
        <div className="evacuation-progress-bar-circle">
          <CircularProgressBar
            strokeWidth={10}
            percentage={Math.round(percentage * 100) / 100}
            textForPercentage={this.handleTextForPercentage}
          />
        </div>
        <div className="evacuation-progress-background"/>
      </div>
    );
  }
}
