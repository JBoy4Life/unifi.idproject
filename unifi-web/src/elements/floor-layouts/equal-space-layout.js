import React from 'react'
import FloorLayout from './floor-layout'

const EqualSpaceLayout = (props) => {
  const { children } = props

  return (
    <FloorLayout className="equal-space-layout">
      {children}
    </FloorLayout>
  )
}

export default EqualSpaceLayout
