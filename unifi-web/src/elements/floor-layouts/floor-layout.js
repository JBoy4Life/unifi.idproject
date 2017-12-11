import React from 'react'

import './floor-layout.scss'

const FloorLayout = props => (
  <div className={`${props.className || ''} floor-layout`}>
    {props.children}
  </div>
)

export default FloorLayout
