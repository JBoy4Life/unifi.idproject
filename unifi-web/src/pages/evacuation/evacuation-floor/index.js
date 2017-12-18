import React, { Component } from 'react'

import { FlippedCardsStack, FloorSection, EqualSpaceFloorLayout } from '../../../elements'
import { EvacuationProgressBar } from '../../../components'

import './index.scss'


const mockData = [
  {
    subsections: [
      {
        label: 'SA1',
        stairsPosition: 'top-right',
        capacityCount: 100,
        currentCount: 105,
      },
      {
        label: 'SA2',
        capacityCount: 100,
        currentCount: 90,
      },
      {
        label: 'SA3',
        capacityCount: 100,
        currentCount: 40,
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SA1',
        stairsPosition: 'top-right',
        capacityCount: 100,
        currentCount: 40,
      },
      {
        label: 'SA2',
        capacityCount: 100,
        currentCount: 90,
      },
      {
        label: 'SA3',
        capacityCount: 100,
        currentCount: 60,
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SB1',
        stairsPosition: 'top-right',
        capacityCount: 150,
        currentCount: 30,
      },
      {
        label: 'SB2',
        capacityCount: 150,
        currentCount: 40,
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SC1',
        stairsPosition: 'top-right',
        capacityCount: 150,
        currentCount: 40,
      },
      {
        label: 'SC2',
        capacityCount: 150,
        currentCount: 40,
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SD1',
        stairsPosition: 'bottom-left',
        capacityCount: 150,
        currentCount: 60,
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SE1',
        stairsPosition: 'bottom-left',
        capacityCount: 100,
        currentCount: 90,
      },

      {
        label: 'SE2',
        capacityCount: 100,
        currentCount: 40,
      },

      {
        label: 'SE3',
        stairsPosition: 'bottom-left',
        capacityCount: 100,
        currentCount: 140,
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SC1',
        capacityCount: 100,
        currentCount: 35,
      },

      {
        label: 'SC2',
        capacityCount: 100,
        currentCount: 70,
      },
    ],
  },
]

const getTotalCapacity = floor => floor.subsections.reduce((acc, item) => {
  acc += item.capacityCount
  return acc
}, 0)

const getCurrentCount = floor => floor.subsections.reduce((acc, item) => {
  acc += item.currentCount
  return acc
}, 0)

export default class EvacuationFloor extends Component {
  renderFloorNumber = (floor, idx) => <div>{(idx === 0 ? 'G' : idx.toString())}</div>

  renderFloorSection = section => <FloorSection {...section} />

  renderFloor = floor => (
    <EqualSpaceFloorLayout>
      {floor.subsections.map(this.renderFloorSection)}
    </EqualSpaceFloorLayout>
  )

  renderCapaictyIndicator = floor => (
    <div>
      <EvacuationProgressBar percentage={
        (getCurrentCount(floor) / getTotalCapacity(floor)) * 100}
      />
    </div>
  )

  render() {
    return (
      <div className="site-plan-container">
        <h2>Site plan</h2>
        <div className="site-plan">
          <div className="floor-indicator">
            {mockData.map(this.renderFloorNumber)}
          </div>
          <FlippedCardsStack>
            {mockData.map(this.renderFloor)}
          </FlippedCardsStack>
          <div className="capacity-indicator">
            {mockData.map(this.renderCapaictyIndicator)}
          </div>
        </div>
      </div>
    )
  }
}
