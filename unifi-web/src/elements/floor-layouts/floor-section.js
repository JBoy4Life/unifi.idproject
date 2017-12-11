import React from 'react'

import './floor-section.scss'

const FloorSection = props => (
  <div className={`${props.className || ''} floor-section`}>
    {props.children}
    <div className="floor-section-title">
      {props.label}
    </div>
  </div>
)

export default FloorSection
