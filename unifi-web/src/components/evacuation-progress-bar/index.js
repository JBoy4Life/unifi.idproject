import React, {Component} from 'react'

import { CircularProgressBar } from '../../elements'

import './index.scss'

function getStatusClass(percentage, warningThreshold, criticalThreshold) {
  if (percentage < criticalThreshold) {
    return 'status-critical';
  } else if (percentage < warningThreshold) {
    return 'status-warning';
  } else {
    return 'status-good';
  }
}

export default class EvacuationProgressBar extends Component {
  render() {
    return (
      <div className={`evacuation-progress-bar ${getStatusClass(this.props.percentage, this.props.warningThreshold, this.props.criticalThreshold)}`}>
        <div className="evacuation-progress-bar-circle">
          <CircularProgressBar strokeWidth={10}
                               percentage={Math.round(this.props.percentage * 100) / 100}/>
        </div>
        <div className="evacuation-progress-background"/>
      </div>
    );
  }
}
