import React from 'react'

import { CircularProgressBar } from '../../elements'

import './index.scss'

const EvacuationProgressBar = ({ percentage = 0 }) => (
  <div className="evacuation-progress-bar">
    <div className="evacuation-progress-bar-circle">
      <CircularProgressBar strokeWidth={10} percentage={percentage} />
    </div>
    <div className="evacuation-progress-background" />
  </div>
)

export default EvacuationProgressBar
