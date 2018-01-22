import React, { Component } from 'react'
import fp from 'lodash/fp'
import moment from "moment"

import { Button, Icon } from 'elements'

const getFirstMonthFromBlock = ({ blocks }) =>
  fp.compose(
    fp.get('[0]'),
    fp.sortBy(item => item),
    fp.map(block => moment(block.startTime))
  )(blocks) || moment()

export class ScheduleCalendar extends Component {
  constructor(props) {
    super(props)

    this.state = {
      selectedMonth: getFirstMonthFromBlock(props)
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.blocks !== nextProps.blocks) {
      this.setState({
        selectedMonth: getFirstMonthFromBlock(nextProps)
      })
    }
  }

  generateCalendar(props) {
    // Get number of days in month.
    const { selectedMonth } = this.state

    let daysInMonth = selectedMonth.daysInMonth()

    // Generate the calendar, indexed by date of month
    let calendar = new Array(daysInMonth).fill([])

    // Populate the calendar.
    props.blocks.filter((block) => {
      // Selected month blocks only.
      let d = moment(block.startTime)
      return d.year() === this.state.selectedMonth.year() &&
        d.month() === this.state.selectedMonth.month()
    }).forEach((block) => {
      // Insert into calendar.
      let i = moment(block.startTime).date()
      calendar[i - 1] = calendar[i - 1].concat(block)
    })

    return calendar
  }

  generateWeekRows(calendar) {
    // Generate a “flat grid” by figuring out how many week rows we’ll have.
    // Use ISO week numbers — dividing by 7 won’t work
    let sw = this.state.selectedMonth.startOf("month").isoWeek()
    let ew = this.state.selectedMonth.endOf("month").isoWeek()
    if (ew < sw) {
      // Spillover into new year.
      ew = 53
    }
    let rows  = (ew - sw) + 1
    let grid  = Array.from(Array(rows * 7).keys()).map((key) => <td key={`empty-${key}`} />)

    // Offset into the grid by start-of-month weekday number.
    // Sunday is zero, which is bloody annoying, hence the weird arithmetic.
    let offset = (this.state.selectedMonth.startOf("month").day() + 6) % 7

    // Populate the grid. Yes, it’s mutation, so what?
    for (let g = offset, i = 0; i < calendar.length; i++, g++) {
      grid[g] = (
        <td key={i}>
          <p className="numeral">{i + 1}</p>
          {calendar[i].map((block) => {
            let st = moment(block.startTime).format("HH:mm")
            let et = moment(block.endTime).format("HH:mm")
            return (
              <p key={block.blockId} className="block">
                <span className="time">{st}–{et}</span>
                <br />
                {block.name}
              </p>
            )
          })}
          <p className="block">{' '}</p>
        </td>
      )
    }

    // Now chunk it up into week rows.
    let chunked = Array.from(Array(rows).keys()).map((row, index) => {
      let s = row * 7
      let e = s + 7
      return (
        <tr key={index}>
          {grid.slice(s, e)}
        </tr>
      )
    })

    // I think we’re done.
    return chunked
  }

  handlePrevMonthClick = () => {
    this.setState({
      selectedMonth: this.state.selectedMonth.subtract(1, "month")
    })
  }

  handleNextMonthClick = () => {
    this.setState({
      selectedMonth: this.state.selectedMonth.add(1, "month")
    })
  }

  render() {
    const calendar = this.generateCalendar(this.props)

    return (
      <div className="schedule">
        <div className="controls">
          <h2>{this.state.selectedMonth.format("MMM Y")}</h2>
          <Button className="arrow" onClick={this.handlePrevMonthClick}>
            <Icon type="left" />
          </Button>
          <Button className="arrow" onClick={this.handleNextMonthClick}>
            <Icon type="right" />
          </Button>
          <Button className="addBlock"><Icon type="plus-circle-o" /> Add a lecture</Button>
        </div>
        <table className="timetable">
          <thead>
          <tr>
            <th>M</th>
            <th>T</th>
            <th>W</th>
            <th>T</th>
            <th>F</th>
            <th>S</th>
            <th>S</th>
          </tr>
          </thead>
          <tbody>
          {this.generateWeekRows(calendar)}
          </tbody>
        </table>
      </div>
    )
  }
}

export default ScheduleCalendar
