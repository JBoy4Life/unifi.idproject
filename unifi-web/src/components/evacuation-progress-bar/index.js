import React from 'react'

import { CircularProgressBar } from '../../elements'

import './index.scss'

const getStatusClass = (percentage) => {
  if (percentage < 50) {
    return 'status-good'
  }
  if (percentage <= 100) {
    return 'status-warning'
  }

  return 'status-critical'
}

const EvacuationProgressBar = ({ percentage = 0 }) => (
  <div className={`evacuation-progress-bar ${getStatusClass(percentage)}`}>
    <div className="evacuation-progress-bar-circle">
      <CircularProgressBar strokeWidth={10} percentage={Math.round(percentage * 100) / 100} />
    </div>
    <div className="evacuation-progress-background" />
  </div>
)

export default EvacuationProgressBar
