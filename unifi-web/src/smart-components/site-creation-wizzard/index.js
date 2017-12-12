import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Steps } from '../../elements'
import { SiteWizzardDetailsForm } from '../'
import SiteWizzardIntro from './components/site-wizzard-intro'
import SiteWizzardFloorPlan from './components/site-wizzard-floor-plan'
import SiteWizzardUpload from './components/site-wizzard-upload'
import SiteWizzardReview from './components/site-wizzard-review'

import { noop } from '../../utils/helpers'

const { Step } = Steps

export default class SiteCreationWizzard extends Component {
  static propTypes = {
    onWizzardDone: PropTypes.func,
    onStepChange: PropTypes.func,
  }

  static defaultProps = {
    onWizzardDone: noop,
    onStepChange: noop,
  }

  state = {
    currentTab: 0,
  }

  handleNextStep = () => {
    this.setState({ currentTab: this.state.currentTab + 1 }, () => {
      this.props.onStepChange(this.state.currentTab)
    })
  }

  handleDone = () => {
    this.props.onWizzardDone()
  }

  renderStepsHeader() {
    return (
      <Steps size="small" current={this.state.currentTab}>
        <Step title="Introduction" />
        <Step title="Details" />
        <Step title="Zones" />
        <Step title="Upload" />
        <Step title="Review" />
      </Steps>
    )
  }

  renderTabContent() {
    const { currentTab } = this.state
    switch (currentTab) {
      case 4:
        return <SiteWizzardReview onSubmit={this.handleDone} />
      case 3:
        return <SiteWizzardUpload onSubmit={this.handleNextStep} />
      case 2:
        return <SiteWizzardFloorPlan onSubmit={this.handleNextStep} />
      case 1:
        return <SiteWizzardDetailsForm onSubmit={this.handleNextStep} />
      case 0:
      default:
        return <SiteWizzardIntro onSubmit={this.handleNextStep} />
    }
  }

  render() {
    return (
      <div className="site-creation-wizzard">
        {this.renderStepsHeader()}
        {this.renderTabContent()}
      </div>
    )
  }
}
