import React from 'react'
import { Route, Switch /*, Redirect*/ } from 'react-router'

import * as ROUTES from 'utils/routes'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import { PageContent } from 'components'

import AttendanceCustomReports from './attendance-custom-reports'
import AttendanceReports from './attendance-reports'
import AttendanceScheduleBlockDrilldown from './attendance-schedule-block-drilldown'
import AttendanceScheduleDetail from './attendance-schedule-detail'
import AttendanceSchedules from './attendance-schedules'

const menus = [
  {
    key: '/attendance/schedules',
    icon: 'calendar',
    label: 'Modules',
  },
  {
    key: '/attendance/reports',
    icon: 'bar-chart',
    label: 'Reports',
  },
  // {
  //   key: '/attendance/custom-reports',
  //   label: 'Custom Reports',
  // }
]

const SitemapContainer = () => (
  <PageContainer>
    <PageContent>
      <PageContent.Sidebar>
        <LinkedSideNavigation menus={menus} />
      </PageContent.Sidebar>
      <PageContent.Main>
        <Switch>
          <Route
            exact
            path={ROUTES.ATTENDANCE_SCHEDULES}
            component={AttendanceSchedules}
          />
          <Route
            exact
            path={ROUTES.ATTENDANCE_SCHEDULES_DETAIL}
            component={AttendanceScheduleDetail}
          />
          <Route
            exact
            path={ROUTES.ATTENDANCE_SCHEDULES_BLOCK_DRILLDOWN}
            component={AttendanceScheduleBlockDrilldown}
          />
          <Route
            exact
            path={ROUTES.ATTENDANCE_REPORTS}
            component={AttendanceReports}
          />
          {/* <Route
            exact
            path={ROUTES.ATTENDANCE_CUSTOM_REPORTS}
            component={AttendanceCustomReports}
          /> */}
        </Switch>
      </PageContent.Main>
    </PageContent>
  </PageContainer>
)

export default SitemapContainer
