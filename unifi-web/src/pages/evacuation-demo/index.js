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

  constructor (props) {
    super(props)
    this.state = {
      evacuationActive: false
    }
  }

  storeEvacuationVideoRef = (ref) => {
    this.videoRef = ref
  }

  toggleEvacuation = () => {
    this.setState((oldState) => {
      oldState.evacuationActive
        ? this.switchVideoPlaybackStatus(this.videoRef, false)
        : this.switchVideoPlaybackStatus(this.videoRef, true)
      return ({evacuationActive: ! oldState.evacuationActive})
    })
  }

  switchVideoPlaybackStatus = (videoRef, status) => {
    videoRef.currentTime = 0
    status
      ? videoRef.play()
      : videoRef.pause()
  }

  render = () => (
    <PageContainer>
      {/*NOTE: In the actual implementation this banner should be in the
          `TopNavigation` component.*/}
      {this.state.evacuationActive
          && <div className='evacuation-demo--banner'>Evacuation in Progress</div>}
      <PageContent className='evacuation-demo--page-content'>
        <PageContent.Sidebar>
          {/*TODO: Create different styles for `Button` element.*/}
          <Button className='evacuation-demo--end-button' onClick={this.toggleEvacuation}>
            {this.state.evacuationActive
              ? 'End Evacuation'
              : 'Begin Evacuation'}
          </Button>
          <LinkedSideNavigation menus={menus} />
        </PageContent.Sidebar>
        <PageContent.Main>
          <Switch>
            <Route
              exact
              path={ROUTES.EVACUATION_DEMO}
              render={() => <EvacuationDemoDashboard refCallback={this.storeEvacuationVideoRef}/>}
            />
          </Switch>
        </PageContent.Main>
      </PageContent>
    </PageContainer>
  )

  componentDidMount = () => {
    this.toggleEvacuation()
  }

}

export default EvacuationDemo
