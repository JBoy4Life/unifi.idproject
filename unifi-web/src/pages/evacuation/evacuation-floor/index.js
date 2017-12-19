import React, { Component } from 'react'

import { LiveSiteFloorView } from '../../../components'

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

export default class EvacuationFloor extends Component {
  render() {
    return (
      <div className="site-plan-container">
        <h2>Site plan</h2>
        <LiveSiteFloorView floors={mockData} />
      </div>
    )
  }
}
