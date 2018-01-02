import React, { Component } from 'react';
import { Route, Switch /* Redirect */ } from 'react-router';

import * as ROUTES from '../../utils/routes';

import { PageContent } from '../../components';
import { PageContainer, LinkedSideNavigation } from '../../smart-components';

import AttendanceModules from './attendance-modules';
import AttendanceModuleDetail from './attendance-module-detail';
import AttendanceReports from './attendance-reports';
import AttendanceCustomReports from './attendance-custom-reports';

const menus = [{
  key: '/attendance/modules',
  label: 'Modules',
},
{
  key: '/attendance/reports',
  label: 'Reports',
},
{
  key: '/attendance/custom-reports',
  label: 'Custom Reports',
}];

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
                path={ROUTES.ATTENDANCE_MODULES}
                component={AttendanceModules}
              />
              <Route
                exact
                path={ROUTES.ATTENDANCE_MODULES_DETAIL}
                component={AttendanceModuleDetail}
              />
              <Route
                exact
                path={ROUTES.ATTENDANCE_REPORTS}
                component={AttendanceReports}
              />
              <Route
                exact
                path={ROUTES.ATTENDANCE_CUSTOM_REPORTS}
                component={AttendanceCustomReports}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

