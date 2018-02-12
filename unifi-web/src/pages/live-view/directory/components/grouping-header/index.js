import React from 'react'
import { Tabs } from 'elements'

const { TabPane } = Tabs

const GroupingHeader = ({ onChange, groupValue }) => (
  <Tabs onChange={onChange} activeKey={groupValue || 'all'} className="directory-grouping-header">
    <TabPane tab="ALL" key="all" />
    <TabPane tab="ZONES" key="zones" />
  </Tabs>
)

export default GroupingHeader
