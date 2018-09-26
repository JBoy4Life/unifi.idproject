import React, { Component } from 'react'
import { Route, Switch } from 'react-router'

import * as ROUTES from 'config/routes'
import { PageContent } from 'components'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import EvacuationDemoDashboard from './evacuation-demo-dashboard'
import Button from '../../elements/button'

import './index.scss'

const menus = [{
  key: '/evacuation-demo',
  label: 'Dashboard',
},
{
  key: '/evacuation-demo/directory',
  label: 'Directory View',
},
{
  key: '/evacuation-demo/floor',
  label: 'Floor View',
}]

class EvacuationDemo extends Component {
  render() {
    return (
      <PageContainer>
        {/*NOTE: In the actual implementation this banner should be in the
            `TopNavigation` component.*/}
        <div className='evacuation-demo--banner'><span style={{verticalAlign: 'middle'}}>Evacuation in Progress</span></div>
        <PageContent className='evacuation-demo--page-content'>
          <PageContent.Sidebar>
            {/*TODO: Create different styles for `Button` element.*/}
            <Button className='evacuation-demo--end-button'>End Evacuation</Button>
            <LinkedSideNavigation menus={menus} />
          </PageContent.Sidebar>
          <PageContent.Main>
            <Switch>
              <Route
                exact
                path={ROUTES.EVACUATION_DEMO}
                component={EvacuationDemoDashboard}
              />
            </Switch>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export default EvacuationDemo
