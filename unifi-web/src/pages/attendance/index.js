import React, { Component } from 'react';
import { Route, Switch /* Redirect */ } from 'react-router';

import * as ROUTES from '../../utils/routes';

import { PageContent } from '../../components';
import { PageContainer, LinkedSideNavigation } from '../../smart-components';

import AttendanceSchedules from './attendance-schedules';
import AttendanceScheduleDetail from './attendance-schedule-detail';
import AttendanceScheduleBlockDrilldown from './attendance-schedule-block-drilldown';
import AttendanceReports from './attendance-reports';
import AttendanceCustomReports from './attendance-custom-reports';

const menus = [{
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
];

export default class SitemapContainer extends Component {
  render() {
    return (
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
              {/*<Route*/}
                {/*exact*/}
                {/*path={ROUTES.ATTENDANCE_CUSTOM_REPORTS}*/}
                {/*component={AttendanceCustomReports}*/}
              {/*/>*/}
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

