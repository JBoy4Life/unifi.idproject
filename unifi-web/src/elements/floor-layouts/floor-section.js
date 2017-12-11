import React from 'react'
import StairsIcon from './stairs-icon'

import './floor-section.scss'

const getClassNames = (props) => {
  const classes = ['floor-section']

  if (props.className) {
    classes.push(props.className)
  }
  if (props.status) {
    classes.push(`floor-status-${props.status}`)
  }

  if (props.stairsPosition) {
    classes.push(`floor-stairs-${props.stairsPosition}`)
  }

  return classes.join(' ')
}

const getStairsDirection = props =>
  props.stairsPosition !== '' && props.stairsPosition.indexOf('right') !== -1 ? 'right' : 'left'

const FloorSection = props => (
  <div className={getClassNames(props)}>
    {props.children}
    {props.stairsPosition && <StairsIcon direction={getStairsDirection(props)} />}
    <div className="floor-section-title">
      {props.label}
    </div>
  </div>
)

export default FloorSection
