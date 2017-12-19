import React, { Component } from 'react'

import { FlippedCardsStack, FloorSection, EqualSpaceFloorLayout } from '../../elements'
import { EvacuationProgressBar } from '../'

import './index.scss'

const getTotalCapacity = floor => floor.subsections.reduce((acc, item) => {
  acc += item.capacityCount
  return acc
}, 0)

const getCurrentCount = floor => floor.subsections.reduce((acc, item) => {
  acc += item.currentCount
  return acc
}, 0)

const getSectionStatus = (section) => {
  const { currentCount, capacityCount } = section

  if (currentCount < capacityCount / 2) {
    return FloorSection.STATUS.GOOD
  }

  if (currentCount <= capacityCount) {
    return FloorSection.STATUS.WARNING
  }

  return FloorSection.STATUS.CRITICAL
}

export default class EvacuationFloor extends Component {
  renderFloorNumber = (floor, idx) => <div>{(idx === 0 ? 'G' : idx.toString())}</div>

  renderFloorSection = section => (
    <FloorSection status={getSectionStatus(section)} {...section} />
  )

  renderFloor = floor => (
    <EqualSpaceFloorLayout>
      {floor.subsections.map(this.renderFloorSection)}
    </EqualSpaceFloorLayout>
  )

  renderCapaictyIndicator = (floor) => {
    const floorCurrentCount = getCurrentCount(floor)
    const floorCapacity = getTotalCapacity(floor)
    const valueBasedPercentage = (floorCurrentCount / floorCapacity) * 100
    return (
      <div>
        <div className="progress-indicator-wrapper">
          <EvacuationProgressBar percentage={valueBasedPercentage} />
        </div>
        <div className="people-count-container">
          <div className="people-label">
            People
          </div>
          <div className="people-count-value">
            {floorCurrentCount}
          </div>
        </div>
      </div>
    )
  }

  render() {
    const { floors } = this.props
    return (
      <div className="live-site-floor-plan">
        <div className="floor-indicator">
          {floors.map(this.renderFloorNumber)}
        </div>
        <FlippedCardsStack>
          {floors.map(this.renderFloor)}
        </FlippedCardsStack>
        <div className="capacity-indicator">
          {floors.map(this.renderCapaictyIndicator)}
        </div>
      </div>
    )
  }
}
