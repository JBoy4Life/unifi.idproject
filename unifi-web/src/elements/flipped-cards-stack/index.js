import React from 'react'

import './index.scss'


const CardStack = props => (
  <div className="flipped-card-stack">
    {React.Children.map(props.children, child => (
      <div className="flipped-card-container">
        <div className="flipped-card">{child}</div>
      </div>
      ))}
  </div>
)

export default CardStack
