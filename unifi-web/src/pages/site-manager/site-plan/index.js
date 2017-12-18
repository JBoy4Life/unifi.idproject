import React, { Component } from 'react'

import { FlippedCardsStack, FloorSection, EqualSpaceFloorLayout } from '../../../elements'

import './index.scss'

const mockData = [
  {
    subsections: [
      {
        label: 'SA1',
        stairsPosition: 'top-right',
      },
      {
        label: 'SA2',
      },
      {
        label: 'SA3',
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SA1',
        stairsPosition: 'top-right',
      },
      {
        label: 'SA2',
      },
      {
        label: 'SA3',
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SB1',
        stairsPosition: 'top-right',
      },
      {
        label: 'SB2',
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SC1',
        stairsPosition: 'top-right',
      },
      {
        label: 'SC2',
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SD1',
        stairsPosition: 'bottom-left',
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SE1',
        stairsPosition: 'bottom-left',
      },

      {
        label: 'SE2',
      },

      {
        label: 'SE3',
        stairsPosition: 'bottom-left',
      },
    ],
  },

  {
    subsections: [
      {
        label: 'SC1',
      },

      {
        label: 'SC2',
      },
    ],
  },
]

export default class SitePlan extends Component {
  renderFloorNumber = (floor, idx) => <div>{(idx === 0 ? 'G' : idx.toString())}</div>

  renderFloorSection = section => <FloorSection {...section} />

  renderFloor = floor => (
    <EqualSpaceFloorLayout>
      {floor.subsections.map(this.renderFloorSection)}
    </EqualSpaceFloorLayout>
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
        </div>
      </div>
    )
  }
}
