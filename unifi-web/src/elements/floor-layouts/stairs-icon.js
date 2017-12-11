import React from 'react'

import './stairs-icon.scss'

const getClassNames = (props) => {
  const { className, direction } = props
  const classNames = ['stairs-icon']
  if (className) {
    classNames.push(className)
  }

  if (direction) {
    classNames.push(`stairs-icon-direction-${direction}`)
  }

  return classNames.join(' ')
}

const StairsIcon = props => (
  <div className={getClassNames(props)}>
    <div className="stairs-icon-bar" />
    <div className="stairs-icon-bar" />
    <div className="stairs-icon-bar" />
    <div className="stairs-icon-bar" />
  </div>
)

export default StairsIcon
