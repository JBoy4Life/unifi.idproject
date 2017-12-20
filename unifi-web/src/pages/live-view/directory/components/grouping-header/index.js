import React from 'react'
import { Tabs } from '../../../../../elements'

const { TabPane } = Tabs

const GroupingHeader = () => (
  <Tabs className="directory-grouping-header">
    <TabPane tab="ALL" key="1" />
    <TabPane tab="FLOOR" key="2" />
    <TabPane tab="ZONES" key="3" />
  </Tabs>
)

export default GroupingHeader
